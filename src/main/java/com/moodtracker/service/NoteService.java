package com.moodtracker.service;

import com.moodtracker.model.Note;
import com.moodtracker.model.User;
import com.moodtracker.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository NoteRepository;
    private final UserService userService;

    public NoteService(NoteRepository NoteRepository, UserService userService) {
        this.NoteRepository = NoteRepository;
        this.userService = userService;
    }

    public Note createNoteForUser(String username, Note Note) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        } else {
            System.out.println("User found: " + user.getUsername());
        }

        if (Note.getText() == null || Note.getText().isEmpty()) {
            throw new RuntimeException("Note text cannot be empty");
        }

        Note.setUser(user);
        Note.setDate(LocalDate.now()); // Auto-set the date
        return NoteRepository.save(Note);
    }

    public List<Note> getNotesByUser(User user) {
        return NoteRepository.findByUser(user);
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

    public void deleteAllNotes() {
        NoteRepository.deleteAll();
    }
}
