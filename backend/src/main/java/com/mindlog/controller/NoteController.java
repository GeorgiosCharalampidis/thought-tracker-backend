package com.mindlog.controller;

import com.mindlog.service.NoteService;
import com.mindlog.service.UserService;
import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.model.SubCategory;
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
    public ResponseEntity<List<Note>> createNoteForUser(@PathVariable Long userId, @RequestBody Note note) {
        List<Note> createdNotes = NoteService.createNotesForUser(userId, List.of(note));
        if (createdNotes.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Note> notesOfSameSubject = NoteService.getNotesOfSameSubject(userId, createdNotes.get(0).getId());
        return ResponseEntity.ok(notesOfSameSubject);
    }

    @PutMapping("/{userId}/{noteId}")
    public ResponseEntity<List<Note>> updateNoteForUser(@PathVariable Long userId, @PathVariable Long noteId, @RequestBody Note note) {
        Note updatedNote = NoteService.updateNoteForUser(userId, noteId, note);
        if (updatedNote == null) {
            return ResponseEntity.notFound().build();
        }
        List<Note> notesOfSameSubject = NoteService.getNotesOfSameSubject(userId, noteId);
        return ResponseEntity.ok(notesOfSameSubject);
    }

    @PostMapping("/{userId}/batch")
    public ResponseEntity<List<Note>> createNotesForUser(@PathVariable Long userId, @RequestBody List<Note> notes) {
        List<Note> createdNotes = NoteService.createNotesForUser(userId, notes);
        return ResponseEntity.ok(createdNotes);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Note>> getNotesByUser(@PathVariable Long userId) {
        List<Note> Notes = NoteService.getNotesByUserId(userId);
        return ResponseEntity.ok(Notes);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<Note>> getNotesByUserAndDateRange(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        // Fetch user by ID
        User user = userService.getUserById(userId);
        List<Note> Notes = NoteService.getNotesByUserIdAndDateRange(user, startDate, endDate);
        return ResponseEntity.ok(Notes);
    }

    @GetMapping("/{userId}/subjects")
    public ResponseEntity<List<String>> listSubjects(@PathVariable Long userId) {
        List<String> subjects = NoteService.listSubjectsByUserId(userId);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{userId}/subject/{subject}")
    public ResponseEntity<List<Note>> getNotesBySubject(
            @PathVariable Long userId,
            @PathVariable String subject) {
        List<Note> notes = NoteService.getNotesByUserIdAndSubject(userId, subject);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{userId}/subject/{subject}/similar-to/{noteId}")
    public ResponseEntity<List<Note>> getNotesBySubjectSimilarTo(
            @PathVariable Long userId,
            @PathVariable String subject,
            @PathVariable Long noteId) {
        List<Note> notes = NoteService.getNotesOfSameSubject(userId, noteId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<String>> listAllowedSubjects() {
        return ResponseEntity.ok(com.mindlog.model.NoteCluster.allLabels());
    }

    @GetMapping("/subjects/{subject}/subcategories")
    public ResponseEntity<List<String>> listSubCategoriesForSubject(@PathVariable String subject) {
        try {
            String normalized = com.mindlog.model.NoteCluster.normalizeToLabelOrThrow(subject);
            List<String> subCategories = SubCategory.getSubCategoryLabelsForParent(normalized);
            return ResponseEntity.ok(subCategories);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}/{noteId}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Long userId, @PathVariable Long noteId) {
        NoteService.deleteNoteByIdAndUserId(userId, noteId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/all")
    public ResponseEntity<Void> deleteNotesByUser(@PathVariable Long userId) {
        NoteService.deleteNotesByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/summary")
    public String getUserNotesSummary(@PathVariable Long userId) {
        return userService.getAiReflection(userId);
    }
}