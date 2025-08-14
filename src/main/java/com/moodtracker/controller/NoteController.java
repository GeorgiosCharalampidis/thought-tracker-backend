package com.moodtracker.controller;

import com.moodtracker.service.NoteService;
import com.moodtracker.service.UserService;
import com.moodtracker.model.Note;
import com.moodtracker.model.User;
import com.moodtracker.exception.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService NoteService;
    private final UserService userService;

    public NoteController(NoteService NoteService, UserService userService) {
        this.NoteService = NoteService;
        this.userService = userService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Note> createNoteForUser(@PathVariable Long userId, @RequestBody Note Note) {
        Note createdNote = NoteService.createNoteForUser(userId, Note);
        return ResponseEntity.ok(createdNote);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Note>> getNotesByUser(@PathVariable Long userId) {
        List<Note> Notes = NoteService.getNotesByUser(userId);
        return ResponseEntity.ok(Notes);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<Note>> getNotesByUserAndDateRange(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        // Fetch user by ID
        User user = userService.getUserById(userId);
        List<Note> Notes = NoteService.getNotesByUserAndDateRange(user, startDate, endDate);
        return ResponseEntity.ok(Notes);
    }

    @DeleteMapping("/{NoteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long NoteId) {
        NoteService.deleteNote(NoteId);
        return ResponseEntity.noContent().build(); // 204 No Content on success
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteNotesByUser(@PathVariable Long userId) {
        List<Note> notes = NoteService.getNotesByUser(userId);
        for (Note note : notes) {
            NoteService.deleteNote(note.getId());
        }
        return ResponseEntity.noContent().build(); // 204 No Content on success
    }

    @GetMapping("/{userId}/summary")
    public String getUserNotesSummary(@PathVariable Long userId) {
        return userService.getDeepSeekNotes(userId);
    }

}