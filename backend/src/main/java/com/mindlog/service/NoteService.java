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

    public NoteService(NoteRepository NoteRepository, UserService userService, EmbeddingService embeddingService, AiService aiService) {
        this.NoteRepository = NoteRepository;
        this.userService = userService;
        this.embeddingService = embeddingService;
        this.aiService = aiService;
    }

    public SimilarThoughtsResponse createNoteWithValidation(Long userId, Note note) {
        userService.getUserById(userId);

        String validationMessage = validateThoughtContent(note);
        if (validationMessage != null) {
            return createInvalidInputResponse(validationMessage);
        }

        try {
            Note createdNote = createNoteForUser(userId, note);
            if (createdNote == null || createdNote.getId() == null) {
                return createInvalidInputResponse("Failed to create note.");
            }
            return buildSimilarThoughtsResponse(createdNote);
        } catch (Exception e) {
            return createInvalidInputResponse("Something went wrong. Please try again.");
        }
    }

    public SimilarThoughtsResponse previewNoteWithValidation(Note note) {
        String validationMessage = validateThoughtContent(note);
        if (validationMessage != null) {
            return createInvalidInputResponse(validationMessage);
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
        float[] referenceEmbedding = referenceNote.getEmbedding();

        // Own notes: user's past notes sorted by similarity, thresholded to avoid weak matches.
        List<Note> ownNotes = Collections.emptyList();
        if (currentUserId != null && referenceEmbedding != null) {
            ownNotes = NoteRepository.findByUser_Id(currentUserId)
                    .stream()
                    .filter(n -> referenceNoteId == null || !referenceNoteId.equals(n.getId()))
                    .filter(n -> n.getEmbedding() != null)
                    .filter(n -> com.mindlog.util.VectorUtils.cosine(n.getEmbedding(), referenceEmbedding) >= OWN_NOTE_SIMILARITY_THRESHOLD)
                    .sorted((n1, n2) -> Float.compare(
                            com.mindlog.util.VectorUtils.cosine(n2.getEmbedding(), referenceEmbedding),
                            com.mindlog.util.VectorUtils.cosine(n1.getEmbedding(), referenceEmbedding)))
                    .limit(MAX_OWN_NOTES)
                    .collect(Collectors.toList());
        }

        List<Note> candidateNotes;
        if (Category.OPEN_REFLECTIONS.getDisplayName().equals(category)) {
            candidateNotes = NoteRepository.findAll();
        } else if (currentUserId != null) {
            candidateNotes = NoteRepository.findByCategoryAndUser_IdNot(category, currentUserId);
        } else {
            candidateNotes = NoteRepository.findByCategory(category);
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

    private String validateThoughtContent(Note note) {
        if (note == null || note.getContent() == null || note.getContent().isEmpty()) {
            return "Seems like you didn't share any thought.";
        }

        boolean isValid = TextValidator.isMeaningfulThought(note.getContent());
        if (!isValid) {
            return TextValidator.getValidationMessage(note.getContent());
        }

        try {
            isValid = aiService.isValidThought(note.getContent());
            logger.info("AI text validation result for '{}': {}", note.getContent().substring(0, Math.min(50, note.getContent().length())), isValid);
        } catch (Exception e) {
            logger.error("AI text validation failed {}, will proceed with local validation result: {}", e.getMessage(), isValid);
        }

        if (!isValid) {
            return TextValidator.getValidationMessage(note.getContent());
        }

        return null;
    }

    private SimilarThoughtsResponse createInvalidInputResponse(String validationMessage) {
        return new SimilarThoughtsResponse("", Collections.emptyList(), Collections.emptyList(), false, validationMessage);
    }

    private void autoClusterUserNotes(List<Note> notes) {
        List<String> themes = Category.allLabels();
        List<String> themeDescriptions = Category.allDescriptions();
        List<float[]> themeEmbeddings = embeddingService.embedAll(themeDescriptions);

        List<String> noteTexts = notes.stream().map(Note::getContent).collect(Collectors.toList());
        List<float[]> noteEmbeddings = embeddingService.embedAll(noteTexts);

        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            float[] noteEmbedding = noteEmbeddings.get(i);

            note.setEmbedding(noteEmbedding);

            ScoredMatch bestThemeMatch = findBestMatchingTheme(noteEmbedding, themes, themeEmbeddings);
            String bestTheme = bestThemeMatch.isConfident(CATEGORY_CONFIDENCE_THRESHOLD, CATEGORY_CONFIDENCE_MARGIN)
                    ? bestThemeMatch.label()
                    : Category.OPEN_REFLECTIONS.getDisplayName();
            note.setCategory(bestTheme);

            String bestSubCategory = findBestSubCategory(bestTheme, noteEmbedding);
            note.setSubCategory(bestSubCategory);
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

    private List<Note> orderNotesBySimilarityToReference(List<Note> notes, Note referenceNote) {
        if (notes.size() <= 1 || referenceNote.getEmbedding() == null) {
            return notes;
        }

        float[] referenceEmbedding = referenceNote.getEmbedding();

        // Sort by similarity to reference note (most similar first)
        return notes.stream()
                .sorted((note1, note2) -> {
                    float[] emb1 = note1.getEmbedding();
                    float[] emb2 = note2.getEmbedding();
                    
                    if (emb1 == null && emb2 == null) return 0;
                    if (emb1 == null) return 1; // Notes without embeddings go last
                    if (emb2 == null) return -1;
                    
                    float sim1 = com.mindlog.util.VectorUtils.cosine(emb1, referenceEmbedding);
                    float sim2 = com.mindlog.util.VectorUtils.cosine(emb2, referenceEmbedding);
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
