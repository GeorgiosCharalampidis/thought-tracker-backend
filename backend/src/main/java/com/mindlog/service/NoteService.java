package com.mindlog.service;

import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.model.NoteCluster;
import com.mindlog.model.SubCategory;
import com.mindlog.repository.NoteRepository;
import com.mindlog.exception.BadCredentialsException;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    private final NoteRepository NoteRepository;
    private final UserService userService;
    private final EmbeddingService embeddingService;

    public NoteService(NoteRepository NoteRepository, UserService userService, EmbeddingService embeddingService) {
        this.NoteRepository = NoteRepository;
        this.userService = userService;
        this.embeddingService = embeddingService;
    }

    public List<Note> createNotesForUser(Long userId, List<Note> notes) {
        // Do not allow more than one note per user per day
//        LocalDate today = LocalDate.now();
//        boolean exists = NoteRepository.findByUser_UserIdAndDate(userId, today).stream().findAny().isPresent();
//        if (exists) {
//            throw new BadCredentialsException("User with ID " + userId + " has already created a note for today");
//        }

        User user = userService.getUserById(userId);
        if (notes == null || notes.isEmpty()) {
            throw new BadCredentialsException("Note list cannot be empty");
        }

        for (Note note : notes) {
            if (note.getText() == null || note.getText().isEmpty()) {
                throw new BadCredentialsException("Note text cannot be empty");
            }
            note.setDate(LocalDate.now());
            note.setUser(user);
        }
        autoClusterUserNotes(notes);

        return NoteRepository.saveAll(notes);
    }

    public Note updateNoteForUser(Long userId, Long noteId, Note updatedNote) {
        Note existingNote = NoteRepository.findByUser_UserId(userId)
                .stream()
                .filter(n -> n.getId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("Note with ID " + noteId + " not found for user with ID " + userId));

        if (updatedNote.getText() != null && !updatedNote.getText().isEmpty()) {
            existingNote.setText(updatedNote.getText());
        }

        // Re-cluster the note if text was changed
        autoClusterUserNotes(Collections.singletonList(existingNote));

        return NoteRepository.save(existingNote);
    }

    public List<Note> getNotesByUserId(Long userId) {
        return NoteRepository.findByUser_UserId(userId);
    }

    public List<Note> getNotesByUserIdAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return NoteRepository.findByUserAndDateBetween(user, startDate, endDate);
    }

    public List<String> listSubjectsByUserId(Long userId) {
        return NoteRepository.findDistinctSubjectsByUserId(userId);
    }

    public List<Note> getNotesByUserIdAndSubject(Long userId, String subject) {
        String normalized;
        try {
            normalized = NoteCluster.normalizeToLabelOrThrow(subject);
        } catch (IllegalArgumentException ex) {
            throw new BadCredentialsException(ex.getMessage());
        }
        List<Note> notes = NoteRepository.findByUser_UserIdAndSubjectIgnoreCase(userId, normalized);
        return orderNotesBySimilarity(notes);
    }

    public List<Note> getNotesOfSameSubject(Long userId, Long noteId) {
        Note note = NoteRepository.findById(noteId)
                .orElseThrow(() -> new BadCredentialsException("Note with ID " + noteId + " not found"));
        String subject = note.getSubject();
        if (subject == null || subject.isEmpty()) {
            throw new BadCredentialsException("Note with ID " + noteId + " has no subject");
        }
        logger.info("Fetching notes with subject: {}", subject);

        List<Note> sameSubjectNotes = NoteRepository.findBySubject(subject)
                .stream()
                .filter(n -> !n.getId().equals(noteId))
                .collect(Collectors.toList());

        return orderNotesBySimilarityToReference(sameSubjectNotes, note);
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

    private void autoClusterUserNotes(List<Note> notes) {
        // Step 1: Main category clustering
        List<String> themes = NoteCluster.allLabels();
        List<String> themeDescriptions = NoteCluster.allDescriptions();
        List<float[]> themeEmbeddings = embeddingService.embedAll(themeDescriptions);

        // Get embeddings for all note texts
        List<String> noteTexts = notes.stream().map(Note::getText).collect(Collectors.toList());
        List<float[]> noteEmbeddings = embeddingService.embedAll(noteTexts);

        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            float[] noteEmbedding = noteEmbeddings.get(i);
            
            // Store the embedding in the note
            note.setEmbedding(noteEmbedding);
            
            // Step 1: Find the best matching main theme
            String bestTheme = findBestMatchingTheme(note.getText(), themes, themeEmbeddings);
            note.setSubject(bestTheme);
            
            // Step 2: Find the best matching sub-category within that theme
            String bestSubCategory = findBestSubCategory(note.getText(), bestTheme, noteEmbedding);
            note.setSubCategory(bestSubCategory);
        }
    }

    private String findBestSubCategory(String text, String parentCategory, float[] noteEmbedding) {
        List<SubCategory> subCategories = SubCategory.getSubCategoriesForParent(parentCategory);
        
        if (subCategories.isEmpty()) {
            return "General"; // Fallback if no sub-categories defined
        }
        
        // Get descriptions for sub-categories
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
        Note note = NoteRepository.findByUser_UserId(userId)
                .stream()
                .filter(n -> n.getId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("Note with ID " + noteId + " not found for user with ID " + userId));

        NoteRepository.delete(note);
    }

    public void deleteNotesByUserId(Long userId) {
        List<Note> notes = NoteRepository.findByUser_UserId(userId);
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
