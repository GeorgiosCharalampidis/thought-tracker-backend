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
        Note note = NoteRepository.findById(noteId)
                .orElseThrow(() -> new BadCredentialsException("Note with ID " + noteId + " not found"));
        return buildSimilarThoughtsResponse(note);
    }

    private SimilarThoughtsResponse buildSimilarThoughtsResponse(Note referenceNote) {
        String category = referenceNote.getCategory();
        if (category == null || category.isEmpty()) {
            throw new BadCredentialsException("Note has no category");
        }
        logger.info("Fetching notes with category: {}", category);

        List<Note> sameCategoryNotes = NoteRepository.findByCategory(category);
        List<Note> orderedNotes = orderNotesBySimilarityToReference(sameCategoryNotes, referenceNote);

        logger.info("Reference note subcategory: {}", referenceNote.getSubCategory());
        orderedNotes.stream()
                .collect(Collectors.groupingBy(Note::getSubCategory, Collectors.counting()))
                .forEach((subCat, count) -> logger.info("Subcategory '{}': {} notes", subCat, count));

        String categoryMessage = getCategoryMessage(orderedNotes);
        return new SimilarThoughtsResponse(categoryMessage, orderedNotes);
    }

    private static @NotNull String getCategoryMessage(List<Note> orderedNotes) {
        String categoryMessage;
        if (orderedNotes.isEmpty()) {
            categoryMessage = "You are the first one to share a thought like this..";
        } else if (orderedNotes.size() == 1) {
            categoryMessage = "Someone else has shared a similar thought..";
        } else if (orderedNotes.size() <= 3) {
            categoryMessage = String.format("%d others have shared similar thoughts..", orderedNotes.size());
        } else {
            categoryMessage = String.format("%d others have shared similar thoughts..", orderedNotes.size());
        }
        return categoryMessage;
    }

    private String findBestMatchingTheme(String text, List<String> themes, List<float[]> themeEmbeddings) {
        List<float[]> noteEmbedding = embeddingService.embedAll(Collections.singletonList(text));
        float best = Float.NEGATIVE_INFINITY;
        int bestIdx = 0;
        float[] vec = noteEmbedding.get(0);
        for (int t = 0; t < themeEmbeddings.size(); t++) {
            float sim = com.mindlog.util.VectorUtils.cosine(vec, themeEmbeddings.get(t));
            if (sim > best) {
                best = sim;
                bestIdx = t;
            }
        }
        return themes.get(bestIdx);
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
        return new SimilarThoughtsResponse("", Collections.emptyList(), false, validationMessage);
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

            String bestTheme = findBestMatchingTheme(note.getContent(), themes, themeEmbeddings);
            note.setCategory(bestTheme);

            String bestSubCategory = findBestSubCategory(note.getContent(), bestTheme, noteEmbedding);
            note.setSubCategory(bestSubCategory);
        }
    }

    private String findBestSubCategory(String text, String parentCategory, float[] noteEmbedding) {
        List<SubCategory> subCategories = SubCategory.getSubCategoriesForParent(parentCategory);

        if (subCategories.isEmpty()) {
            return "General";
        }

        List<String> subDescriptions = subCategories.stream()
                .map(SubCategory::getDescription)
                .collect(Collectors.toList());

        List<float[]> subEmbeddings = embeddingService.embedAll(subDescriptions);

        float best = Float.NEGATIVE_INFINITY;
        int bestIdx = 0;

        for (int i = 0; i < subEmbeddings.size(); i++) {
            float sim = com.mindlog.util.VectorUtils.cosine(noteEmbedding, subEmbeddings.get(i));
            if (sim > best) {
                best = sim;
                bestIdx = i;
            }
        }

        return subCategories.get(bestIdx).getLabel();
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
