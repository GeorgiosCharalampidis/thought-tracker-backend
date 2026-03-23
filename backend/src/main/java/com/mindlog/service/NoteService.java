package com.mindlog.service;

import com.mindlog.dto.SimilarThoughtsResponse;
import com.mindlog.exception.MeaninglessThought;
import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.model.Category;
import com.mindlog.model.SubCategory;
import com.mindlog.repository.NoteRepository;
import com.mindlog.exception.BadCredentialsException;
import com.mindlog.util.TextValidator;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);
    private static final float CATEGORY_CONFIDENCE_THRESHOLD = 0.30f;
    private static final float CATEGORY_CONFIDENCE_MARGIN = 0.015f;
    private static final float SUBCATEGORY_CONFIDENCE_THRESHOLD = 0.32f;
    private static final float SUBCATEGORY_CONFIDENCE_MARGIN = 0.015f;
    private static final float OWN_NOTE_SIMILARITY_THRESHOLD = 0.72f;
    private static final float COMMUNITY_NOTE_SIMILARITY_THRESHOLD = 0.68f;
    private static final float COMMUNITY_SUBCATEGORY_RELAXED_THRESHOLD = 0.62f;
    private static final int MAX_OWN_NOTES = 3;
    private static final int MAX_COMMUNITY_NOTES = 6;

    private final NoteRepository NoteRepository;
    private final UserService userService;
    private final EmbeddingService embeddingService;
    private final AiService aiService;
    private final ResonanceService resonanceService;

    public NoteService(NoteRepository NoteRepository, UserService userService, EmbeddingService embeddingService, AiService aiService, ResonanceService resonanceService) {
        this.NoteRepository = NoteRepository;
        this.userService = userService;
        this.embeddingService = embeddingService;
        this.aiService = aiService;
        this.resonanceService = resonanceService;
    }

    public SimilarThoughtsResponse createNoteWithValidation(Long userId, Note note) {
        userService.getUserById(userId);

        ValidationResult validation = validateThoughtContent(note);
        if (!validation.isValid()) {
            return createInvalidInputResponse(validation.errorMessage());
        }
        // Pre-set LLM-detected category so autoClusterUserNotes skips a second LLM call
        if (validation.llmCategory() != null) {
            note.setCategory(validation.llmCategory());
        }

        try {
            Note createdNote = createNoteForUser(userId, note);
            if (createdNote == null || createdNote.getId() == null) {
                return createInvalidInputResponse("Failed to create note.");
            }
            SimilarThoughtsResponse response = buildSimilarThoughtsResponse(createdNote);
            if (response.isInputAccepted() && response.getNotes() != null && !response.getNotes().isEmpty()) {
                try {
                    resonanceService.createResonancesForCommunityNotes(createdNote, response.getNotes());
                } catch (Exception e) {
                    logger.warn("Resonance creation failed (non-fatal): {}", e.getMessage());
                }
            }
            return response;
        } catch (Exception e) {
            return createInvalidInputResponse("Something went wrong. Please try again.");
        }
    }

    public SimilarThoughtsResponse previewNoteWithValidation(Note note) {
        ValidationResult validation = validateThoughtContent(note);
        if (!validation.isValid()) {
            return createInvalidInputResponse(validation.errorMessage());
        }
        if (validation.llmCategory() != null) {
            note.setCategory(validation.llmCategory());
        }

        try {
            Note previewNote = new Note();
            previewNote.setContent(note.getContent());
            previewNote.setDate(LocalDate.now());
            autoClusterUserNotes(Collections.singletonList(previewNote));
            return buildSimilarThoughtsResponse(previewNote);
        } catch (Exception e) {
            logger.error("Failed to preview note similarity: {}", e.getMessage(), e);
            return createInvalidInputResponse("Something went wrong. Please try again.");
        }
    }

    public Note createNoteForUser(Long userId, Note note) {
        User user = userService.getUserById(userId);
        note.setDate(LocalDate.now());
        note.setUser(user);
        autoClusterUserNotes(Collections.singletonList(note));

        return NoteRepository.save(note);
    }

    public List<Note> createNotesForUser(Long userId, List<Note> notes) {
        // Do not allow more than one note per user per day
//        LocalDate today = LocalDate.now();
//        boolean exists = NoteRepository.findByUser_IdAndDate(userId, today).stream().findAny().isPresent();
//        if (exists) {
//            throw new BadCredentialsException("User with ID " + userId + " has already created a note for today");
//        }

        User user = userService.getUserById(userId);
        if (notes == null || notes.isEmpty()) {
            throw new BadCredentialsException("Note list cannot be empty");
        }

        for (Note note : notes) {
            if (note.getContent() == null || note.getContent().isEmpty()) {
                throw new BadCredentialsException("Note content cannot be empty");
            }
            note.setDate(LocalDate.now());
            note.setUser(user);
        }
        autoClusterUserNotes(notes);

        return NoteRepository.saveAll(notes);
    }

    public Note updateNoteForUser(Long userId, Long noteId, Note updatedNote) {
        Note existingNote = NoteRepository.findByUser_Id(userId)
                .stream()
                .filter(n -> n.getId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("Note with ID " + noteId + " not found for user with ID " + userId));

        if (updatedNote.getContent() != null && !updatedNote.getContent().isEmpty()) {
            // Validate that the updated content is a meaningful thought

            boolean manualThoughtCheck = TextValidator.isMeaningfulThought(updatedNote.getContent());
            boolean aiServiceThoughtCheck = aiService.isValidThought(updatedNote.getContent());

            if (!manualThoughtCheck) {
                throw new MeaninglessThought(TextValidator.getValidationMessage(updatedNote.getContent()));
            }
            if (!aiServiceThoughtCheck) {
                throw new MeaninglessThought("Well the llm denies your thought :(");
            }
            existingNote.setContent(updatedNote.getContent());
        }

        // Re-cluster the note if content was changed
        autoClusterUserNotes(Collections.singletonList(existingNote));

        return NoteRepository.save(existingNote);
    }

    public void quickSaveNote(Long userId, String content) {
        userService.getUserById(userId);
        NoteRepository.quickInsert(userId, content, LocalDate.now());
    }

    public List<Note> getNotesByUserId(Long userId) {
        return NoteRepository.findByUser_Id(userId);
    }

    public List<Note> getNotesByUserIdAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return NoteRepository.findByUserAndDateBetween(user, startDate, endDate);
    }

    public List<String> listSubjectsByUserId(Long userId) {
        return NoteRepository.findDistinctCategoriesByUserId(userId);
    }

    public List<Note> getNotesByUserIdAndSubject(Long userId, String subject) {
        String normalized;
        try {
            normalized = Category.normalizeToLabelOrThrow(subject);
        } catch (IllegalArgumentException ex) {
            throw new BadCredentialsException(ex.getMessage());
        }
        List<Note> notes = NoteRepository.findByUser_IdAndCategoryIgnoreCase(userId, normalized);
        return orderNotesBySimilarity(notes);
    }

    public SimilarThoughtsResponse getNotesOfSameCategory(Long userId, Long noteId) {
        Note note = NoteRepository.findByIdAndUser_Id(noteId, userId)
                .orElseThrow(() -> new BadCredentialsException("Note with ID " + noteId + " not found for user with ID " + userId));
        return buildSimilarThoughtsResponse(note);
    }

    private SimilarThoughtsResponse buildSimilarThoughtsResponse(Note referenceNote) {
        String category = referenceNote.getCategory();
        if (category == null || category.isEmpty()) {
            category = Category.OPEN_REFLECTIONS.getDisplayName();
            referenceNote.setCategory(category);
        }
        logger.info("Fetching notes with category: {}", category);

        User currentUser = referenceNote.getUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        Long referenceNoteId = referenceNote.getId();
        String embeddingStr = referenceNote.getEmbeddingJson();

        // Own notes: search within the same category to avoid cross-category noise
        List<Note> ownNotes = Collections.emptyList();
        if (currentUserId != null && embeddingStr != null && category != null) {
            float ownDistThreshold = 1.0f - OWN_NOTE_SIMILARITY_THRESHOLD;
            ownNotes = NoteRepository.findSimilarByUserIdAndCategory(
                    currentUserId,
                    category,
                    referenceNoteId != null ? referenceNoteId : -1L,
                    embeddingStr,
                    ownDistThreshold,
                    MAX_OWN_NOTES);
        }

        // Community notes: fetch top candidates from DB, then apply subcategory boost filter in Java
        // Use relaxed threshold for DB query so subcategory-boosted matches are included
        float communityDistThreshold = 1.0f - COMMUNITY_SUBCATEGORY_RELAXED_THRESHOLD; // 1 - 0.62 = 0.38
        int candidateFetchLimit = MAX_COMMUNITY_NOTES * 4; // fetch extra for Java-side filtering

        List<Note> candidateNotes;
        if (Category.OPEN_REFLECTIONS.getDisplayName().equals(category) || embeddingStr == null) {
            if (embeddingStr == null) {
                candidateNotes = currentUserId != null
                        ? NoteRepository.findByCategoryAndUser_IdNot(category, currentUserId)
                        : NoteRepository.findByCategory(category);
            } else {
                long excludeUserId = currentUserId != null ? currentUserId : -1L;
                candidateNotes = NoteRepository.findSimilarExcludingUser(
                        excludeUserId, embeddingStr, communityDistThreshold, candidateFetchLimit);
            }
        } else if (currentUserId != null) {
            candidateNotes = NoteRepository.findSimilarByCategoryExcludingUser(
                    category, currentUserId, embeddingStr, communityDistThreshold, candidateFetchLimit);
        } else {
            candidateNotes = NoteRepository.findSimilarByCategory(
                    category, embeddingStr, communityDistThreshold, candidateFetchLimit);
        }

        List<Note> orderedNotes = orderNotesBySubcategoryAndSimilarity(candidateNotes, referenceNote).stream()
                .filter(n -> referenceNoteId == null || !referenceNoteId.equals(n.getId()))
                .filter(n -> currentUserId == null || n.getUser() == null || !currentUserId.equals(n.getUser().getId()))
                .filter(n -> isStrongCommunityMatch(n, referenceNote))
                .limit(MAX_COMMUNITY_NOTES)
                .collect(Collectors.toList());

        logger.info("Reference note subcategory: {}", referenceNote.getSubCategory());
        orderedNotes.stream()
                .collect(Collectors.groupingBy(Note::getSubCategory, Collectors.counting()))
                .forEach((subCat, count) -> logger.info("Subcategory '{}': {} notes", subCat, count));

        String categoryMessage = getCategoryMessage(orderedNotes);
        return new SimilarThoughtsResponse(categoryMessage, orderedNotes, ownNotes);
    }

    private static @NotNull String getCategoryMessage(List<Note> orderedNotes) {
        String categoryMessage;
        if (orderedNotes.isEmpty()) {
            categoryMessage = "You're the first to share something like this.";
        } else if (orderedNotes.size() == 1) {
            categoryMessage = "You're not alone — someone else has felt this way too.";
        } else if (orderedNotes.size() <= 5) {
            categoryMessage = String.format("You're not alone — %d others have felt something similar.", orderedNotes.size());
        } else {
            categoryMessage = String.format("%d people have shared thoughts like yours.", orderedNotes.size());
        }
        return categoryMessage;
    }

    private ScoredMatch findBestMatchingTheme(float[] noteEmbedding, List<String> themes, List<float[]> themeEmbeddings) {
        return findBestMatch(noteEmbedding, themes, themeEmbeddings);
    }

    private record ValidationResult(String errorMessage, String llmCategory) {
        boolean isValid() { return errorMessage == null; }
        static ValidationResult ok(String llmCategory) { return new ValidationResult(null, llmCategory); }
        static ValidationResult fail(String message) { return new ValidationResult(message, null); }
    }

    private ValidationResult validateThoughtContent(Note note) {
        if (note == null || note.getContent() == null || note.getContent().isEmpty()) {
            return ValidationResult.fail("Seems like you didn't share any thought.");
        }

        if (!TextValidator.isMeaningfulThought(note.getContent())) {
            return ValidationResult.fail(TextValidator.getValidationMessage(note.getContent()));
        }

        // Single LLM call: validate + categorize together
        try {
            AiService.ClassificationResult result = aiService.validateAndCategorize(note.getContent());
            if (result != null) {
                logger.info("LLM classified '{}...' — valid={}, category={}",
                        note.getContent().substring(0, Math.min(50, note.getContent().length())),
                        result.valid(), result.category());
                if (!result.valid()) {
                    return ValidationResult.fail(TextValidator.getValidationMessage(note.getContent()));
                }
                return ValidationResult.ok(result.category());
            }
        } catch (Exception e) {
            logger.error("validateAndCategorize failed, proceeding without LLM: {}", e.getMessage());
        }

        return ValidationResult.ok(null);
    }

    private SimilarThoughtsResponse createInvalidInputResponse(String validationMessage) {
        return new SimilarThoughtsResponse("", Collections.emptyList(), Collections.emptyList(), false, validationMessage);
    }

    private void autoClusterUserNotes(List<Note> notes) {
        List<String> noteTexts = notes.stream().map(Note::getContent).collect(Collectors.toList());
        List<float[]> noteEmbeddings = embeddingService.embedAll(noteTexts);

        // Lazily computed — only needed if LLM categorization fails for any note
        List<float[]> themeEmbeddings = null;

        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            float[] noteEmbedding = noteEmbeddings.get(i);
            note.setEmbedding(noteEmbedding);

            // Use pre-set category from validateAndCategorize if available (avoids a second LLM call)
            String bestTheme = note.getCategory();

            // Fall back to embedding-based categorization if no category was pre-set
            if (bestTheme == null) {
                logger.info("Falling back to embedding-based categorization for note");
                if (themeEmbeddings == null) {
                    themeEmbeddings = embeddingService.embedAll(Category.allDescriptions());
                }
                ScoredMatch bestThemeMatch = findBestMatchingTheme(noteEmbedding, Category.allLabels(), themeEmbeddings);
                bestTheme = bestThemeMatch.isConfident(CATEGORY_CONFIDENCE_THRESHOLD, CATEGORY_CONFIDENCE_MARGIN)
                        ? bestThemeMatch.label()
                        : Category.OPEN_REFLECTIONS.getDisplayName();
            }

            note.setCategory(bestTheme);
            note.setSubCategory(findBestSubCategory(bestTheme, noteEmbedding));
        }
    }

    private String findBestSubCategory(String parentCategory, float[] noteEmbedding) {
        if (Category.OPEN_REFLECTIONS.getDisplayName().equals(parentCategory)) {
            return "General";
        }

        List<SubCategory> subCategories = SubCategory.getSubCategoriesForParent(parentCategory);

        if (subCategories.isEmpty()) {
            return "General";
        }

        List<String> subDescriptions = subCategories.stream()
                .map(SubCategory::getDescription)
                .collect(Collectors.toList());

        List<float[]> subEmbeddings = embeddingService.embedAll(subDescriptions);

        List<String> subLabels = subCategories.stream()
                .map(SubCategory::getLabel)
                .collect(Collectors.toList());

        ScoredMatch bestSubCategory = findBestMatch(noteEmbedding, subLabels, subEmbeddings);
        if (!bestSubCategory.isConfident(SUBCATEGORY_CONFIDENCE_THRESHOLD, SUBCATEGORY_CONFIDENCE_MARGIN)) {
            return "General";
        }

        return bestSubCategory.label();
    }

    public void deleteNoteByIdAndUserId(Long userId, Long noteId) {
        Note note = NoteRepository.findByUser_Id(userId)
                .stream()
                .filter(n -> n.getId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("Note with ID " + noteId + " not found for user with ID " + userId));

        NoteRepository.delete(note);
    }

    public void deleteNotesByUserId(Long userId) {
        List<Note> notes = NoteRepository.findByUser_Id(userId);
        if (notes.isEmpty()) {
            throw new BadCredentialsException("No notes found for user with ID " + userId);
        }
        NoteRepository.deleteAll(notes);
    }

    private List<Note> orderNotesBySimilarity(List<Note> notes) {
        if (notes.size() <= 1) {
            return notes;
        }

        // Calculate centroid of all embeddings in this cluster
        float[] centroid = calculateCentroid(notes);
        if (centroid == null) {
            return notes; // Return original order if no embeddings available
        }

        // Sort by similarity to centroid (most similar first)
        return notes.stream()
                .sorted((note1, note2) -> {
                    float[] emb1 = note1.getEmbedding();
                    float[] emb2 = note2.getEmbedding();
                    
                    if (emb1 == null && emb2 == null) return 0;
                    if (emb1 == null) return 1; // Notes without embeddings go last
                    if (emb2 == null) return -1;
                    
                    float sim1 = com.mindlog.util.VectorUtils.cosine(emb1, centroid);
                    float sim2 = com.mindlog.util.VectorUtils.cosine(emb2, centroid);
                    return Float.compare(sim2, sim1); // Higher similarity first
                })
                .collect(Collectors.toList());
    }

    private List<Note> orderNotesBySubcategoryAndSimilarity(List<Note> notes, Note referenceNote) {
        if (notes.size() <= 1) {
            return notes;
        }

        String referenceSubCategory = referenceNote.getSubCategory();
        float[] referenceEmbedding = referenceNote.getEmbedding();

        // Sort by embedding similarity first, then apply small subcategory boost
        return notes.stream()
                .sorted((note1, note2) -> {
                    float[] emb1 = note1.getEmbedding();
                    float[] emb2 = note2.getEmbedding();
                    
                    if (emb1 == null && emb2 == null) return 0;
                    if (emb1 == null) return 1; // Notes without embeddings go last
                    if (emb2 == null) return -1;
                    if (referenceEmbedding == null) return 0;
                    
                    float sim1 = com.mindlog.util.VectorUtils.cosine(emb1, referenceEmbedding);
                    float sim2 = com.mindlog.util.VectorUtils.cosine(emb2, referenceEmbedding);
                    
                    // Small boost for same subcategory (but don't override major similarity differences)
                    if (Objects.equals(note1.getSubCategory(), referenceSubCategory)) {
                        sim1 += 0.05f; // Small boost
                    }
                    if (Objects.equals(note2.getSubCategory(), referenceSubCategory)) {
                        sim2 += 0.05f; // Small boost  
                    }
                    
                    return Float.compare(sim2, sim1); // Higher similarity first
                })
                .collect(Collectors.toList());
    }

    private boolean isStrongCommunityMatch(Note candidate, Note referenceNote) {
        if (candidate.getEmbedding() == null || referenceNote.getEmbedding() == null) {
            return false;
        }

        float similarity = com.mindlog.util.VectorUtils.cosine(candidate.getEmbedding(), referenceNote.getEmbedding());
        if (similarity >= COMMUNITY_NOTE_SIMILARITY_THRESHOLD) {
            return true;
        }

        return similarity >= COMMUNITY_SUBCATEGORY_RELAXED_THRESHOLD
                && Objects.equals(candidate.getSubCategory(), referenceNote.getSubCategory())
                && referenceNote.getSubCategory() != null
                && !referenceNote.getSubCategory().isBlank();
    }

    private ScoredMatch findBestMatch(float[] sourceEmbedding, List<String> labels, List<float[]> candidateEmbeddings) {
        float best = Float.NEGATIVE_INFINITY;
        float secondBest = Float.NEGATIVE_INFINITY;
        int bestIdx = 0;

        for (int i = 0; i < candidateEmbeddings.size(); i++) {
            float sim = com.mindlog.util.VectorUtils.cosine(sourceEmbedding, candidateEmbeddings.get(i));
            if (sim > best) {
                secondBest = best;
                best = sim;
                bestIdx = i;
            } else if (sim > secondBest) {
                secondBest = sim;
            }
        }

        return new ScoredMatch(labels.get(bestIdx), best, secondBest);
    }

    private record ScoredMatch(String label, float bestScore, float secondBestScore) {
        private boolean isConfident(float threshold, float margin) {
            return bestScore >= threshold && (bestScore - secondBestScore) >= margin;
        }
    }

    private float[] calculateCentroid(List<Note> notes) {
        List<float[]> embeddings = notes.stream()
                .map(Note::getEmbedding)
                .filter(embedding -> embedding != null)
                .collect(Collectors.toList());

        if (embeddings.isEmpty()) {
            return null;
        }

        int dimensions = embeddings.get(0).length;
        float[] centroid = new float[dimensions];

        // Sum all embeddings
        for (float[] embedding : embeddings) {
            for (int i = 0; i < dimensions; i++) {
                centroid[i] += embedding[i];
            }
        }

        // Average (divide by count)
        for (int i = 0; i < dimensions; i++) {
            centroid[i] /= embeddings.size();
        }

        return centroid;
    }

}
