package com.moodtracker.controller;

import com.moodtracker.service.NoteService;
import com.moodtracker.service.UserService;
import com.moodtracker.model.Note;
import com.moodtracker.model.User;
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

    @PostMapping("/{userName}")
    public ResponseEntity<Note> createNoteForUser(@PathVariable String userName, @RequestBody Note Note) {
        Note createdNote = NoteService.createNoteForUser(userName, Note);
        return ResponseEntity.ok(createdNote);
    }

    @GetMapping("/{userName}")
    public ResponseEntity<List<Note>> getNotesByUser(@PathVariable String userName) {
        // Fetch user by ID (you’ll need a UserService for this)
        User user = userService.getUserByUsername(userName);
        List<Note> Notes = NoteService.getNotesByUser(user);
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

    @DeleteMapping("/{userName}/{NoteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long NoteId) {
        NoteService.deleteNote(NoteId);
        return ResponseEntity.noContent().build(); // 204 No Content on success
    }

    // delete all Notes
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotes() {
        NoteService.deleteAllNotes();
        return ResponseEntity.noContent().build(); // 204 No Content on success
    }

    @GetMapping("/{username}/summary")
    public String getUserNotesSummary(@PathVariable String username) {
        return userService.getDeepSeekNotes(username);
    }

}