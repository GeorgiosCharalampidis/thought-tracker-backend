package com.mindlog.controller;

import com.mindlog.dto.SimilarThoughtsResponse;
import com.mindlog.service.NoteService;
import com.mindlog.service.UserService;
import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.model.Category;
import com.mindlog.model.SubCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @PostMapping("/preview")
    public ResponseEntity<SimilarThoughtsResponse> previewNote(@RequestBody Note note) {
        SimilarThoughtsResponse response = NoteService.previewNoteWithValidation(note);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<SimilarThoughtsResponse> createNoteForUser(@PathVariable Long userId, @RequestBody Note note, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        SimilarThoughtsResponse response = NoteService.createNoteWithValidation(userId, note);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/{noteId}")
    public ResponseEntity<SimilarThoughtsResponse> updateNoteForUser(@PathVariable Long userId, @PathVariable Long noteId, @RequestBody Note note, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        Note updatedNote = NoteService.updateNoteForUser(userId, noteId, note);
        if (updatedNote == null) {
            return ResponseEntity.notFound().build();
        }
        SimilarThoughtsResponse response = NoteService.getNotesOfSameCategory(userId, noteId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/batch")
    public ResponseEntity<List<Note>> createNotesForUser(@PathVariable Long userId, @RequestBody List<Note> notes, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        List<Note> createdNotes = NoteService.createNotesForUser(userId, notes);
        return ResponseEntity.ok(createdNotes);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Note>> getNotesByUser(@PathVariable Long userId, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        List<Note> Notes = NoteService.getNotesByUserId(userId);
        return ResponseEntity.ok(Notes);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<Note>> getNotesByUserAndDateRange(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication authentication) {
        User user = userService.requireAuthorizedUser(userId, authentication);
        List<Note> Notes = NoteService.getNotesByUserIdAndDateRange(user, startDate, endDate);
        return ResponseEntity.ok(Notes);
    }

    @GetMapping("/{userId}/subjects")
    public ResponseEntity<List<String>> listSubjects(@PathVariable Long userId, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        List<String> subjects = NoteService.listSubjectsByUserId(userId);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{userId}/subject/{subject}")
    public ResponseEntity<List<Note>> getNotesBySubject(
            @PathVariable Long userId,
            @PathVariable String subject,
            Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        List<Note> notes = NoteService.getNotesByUserIdAndSubject(userId, subject);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{userId}/subject/{subject}/similar-to/{noteId}")
    public ResponseEntity<SimilarThoughtsResponse> getNotesBySubjectSimilarTo(
            @PathVariable Long userId,
            @PathVariable String subject,
            @PathVariable Long noteId,
            Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        SimilarThoughtsResponse response = NoteService.getNotesOfSameCategory(userId, noteId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<String>> listAllowedSubjects() {
        return ResponseEntity.ok(Category.allLabels());
    }

    @GetMapping("/subjects/{subject}/subcategories")
    public ResponseEntity<List<String>> listSubCategoriesForSubject(@PathVariable String subject) {
        try {
            String normalized = Category.normalizeToLabelOrThrow(subject);
            List<SubCategory> subCategories = SubCategory.getSubCategoriesForParent(normalized);
            List<String> subCategoryLabels = subCategories.stream()
                .map(SubCategory::getLabel)
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(subCategoryLabels);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}/{noteId}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Long userId, @PathVariable Long noteId, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        NoteService.deleteNoteByIdAndUserId(userId, noteId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/all")
    public ResponseEntity<Void> deleteNotesByUser(@PathVariable Long userId, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        NoteService.deleteNotesByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/summary")
    public String getUserNotesSummary(@PathVariable Long userId, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        return userService.getAiReflection(userId);
    }
}