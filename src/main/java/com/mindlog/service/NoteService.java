package com.mindlog.service;

import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.model.NoteCluster;
import com.mindlog.repository.NoteRepository;
import com.mindlog.exception.BadCredentialsException;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


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
        /*
        LocalDate today = LocalDate.now();
        boolean exists = NoteRepository.findByUser_UserIdAndDate(userId, today).stream().findAny().isPresent();
        if (exists) {
            throw new BadCredentialsException("User with ID " + userId + " has already created a note for today");
        }*/

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
        return NoteRepository.findByUser_UserIdAndSubjectIgnoreCase(userId, normalized);
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
        // Embed richer theme descriptions once
        List<String> themes = NoteCluster.allLabels();
        List<String> themeDescriptions = NoteCluster.allDescriptions();
        List<float[]> themeEmbeddings = embeddingService.embedAll(themeDescriptions);

        for (Note note : notes) {
            // Find the best matching theme
            String bestTheme = findBestMatchingTheme(note.getText(), themes, themeEmbeddings);
            note.setSubject(bestTheme);
        }
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

}
