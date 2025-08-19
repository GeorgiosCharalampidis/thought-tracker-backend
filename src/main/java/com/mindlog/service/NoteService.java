package com.mindlog.service;

import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.repository.NoteRepository;
import com.mindlog.exception.BadCredentialsException;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;


@Service
public class NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    private final NoteRepository NoteRepository;
    private final UserService userService;

    public NoteService(NoteRepository NoteRepository, UserService userService) {
        this.NoteRepository = NoteRepository;
        this.userService = userService;
    }

    public List<Note> createNotesForUser(Long userId, List<Note> notes) {
        User user = userService.getUserById(userId);
        if (notes == null || notes.isEmpty()) {
            throw new BadCredentialsException("Note list cannot be empty");
        }

        for (Note note : notes) {
            if (note.getText() == null || note.getText().isEmpty()) {
                throw new BadCredentialsException("Note text cannot be empty");
            }
            note.setUser(user);
            note.setDate(LocalDate.now());
        }

        return NoteRepository.saveAll(notes);
    }

    public List<Note> getNotesByUserId(Long userId) {
        return NoteRepository.findByUser_UserId(userId);
    }

    public List<Note> getNotesByUserIdAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return NoteRepository.findByUserAndDateBetween(user, startDate, endDate);
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
