package com.moodtracker.service;

import com.moodtracker.model.Note;
import com.moodtracker.model.User;
import com.moodtracker.repository.NoteRepository;
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

    public Note createNoteForUser(Long userId, Note Note) {
        User user = userService.getUserById(userId);
        if (Note.getText() == null || Note.getText().isEmpty()) {
            throw new RuntimeException("Note text cannot be empty");
        }

        logger.info("Creating note for user: {} with text: {}", user.getUsername(), Note.getText());
        Note.setUser(user);
        Note.setDate(LocalDate.now());

        return NoteRepository.save(Note);
    }

    public List<Note> getNotesByUser(Long userId) {
        return NoteRepository.findByUser_UserId(userId);
    }

    public List<Note> getNotesByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return NoteRepository.findByUserAndDateBetween(user, startDate, endDate);
    }

    public void deleteNote(Long NoteId) {
        if (!NoteRepository.existsById(NoteId)) {
            throw new RuntimeException("Note not found with ID: " + NoteId);
        }
        NoteRepository.deleteById(NoteId);
    }
}
