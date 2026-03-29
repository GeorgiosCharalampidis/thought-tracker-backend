package com.rumino.controller;

import com.rumino.dto.ChatRequest;
import com.rumino.dto.OnThisDayResponse;
import com.rumino.dto.ResonanceSnippetResponse;
import com.rumino.dto.SimilarThoughtsResponse;
import com.rumino.service.NoteService;
import com.rumino.service.ResonanceService;
import com.rumino.service.UserService;
import com.rumino.model.Note;
import com.rumino.model.User;
import com.rumino.model.Category;
import com.rumino.model.SubCategory;
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
    private final ResonanceService resonanceService;

    public NoteController(NoteService NoteService, UserService userService, ResonanceService resonanceService) {
        this.NoteService = NoteService;
        this.userService = userService;
        this.resonanceService = resonanceService;
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

    @PostMapping("/{userId}/quick-save")
    public ResponseEntity<Void> quickSaveNote(@PathVariable Long userId, @RequestBody java.util.Map<String, String> body, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        NoteService.quickSaveNote(userId, body.get("content"));
        return ResponseEntity.ok().build();
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

    @GetMapping("/{userId}/similar-to/{noteId}")
    public ResponseEntity<SimilarThoughtsResponse> getSimilarThoughtsForNote(
            @PathVariable Long userId,
            @PathVariable Long noteId,
            Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        SimilarThoughtsResponse response = NoteService.getNotesOfSameCategory(userId, noteId);
        return ResponseEntity.ok(response);
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

    @GetMapping("/{userId}/{noteId}/resonances")
    public ResponseEntity<List<ResonanceSnippetResponse>> getResonancesForNote(
            @PathVariable Long userId,
            @PathVariable Long noteId,
            Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        return ResponseEntity.ok(resonanceService.getResonancesForNote(noteId, userId));
    }

    @GetMapping("/{userId}/on-this-day")
    public ResponseEntity<OnThisDayResponse> getOnThisDayNotes(
            @PathVariable Long userId,
            Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        OnThisDayResponse response = NoteService.getOnThisDayNotes(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/chat")
    public ResponseEntity<String> chatWithJournal(
            @PathVariable Long userId,
            @RequestBody ChatRequest request,
            Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        String response = userService.chatWithJournal(userId, request.getMessages());
        return ResponseEntity.ok(response);
    }
}