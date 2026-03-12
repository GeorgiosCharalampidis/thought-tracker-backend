package com.mindlog.controller;

import com.mindlog.dto.CommentResponse;
import com.mindlog.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/notes/{noteId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long noteId,
            Authentication authentication) {
        return ResponseEntity.ok(commentService.getCommentsForNote(noteId, authentication));
    }

    @PostMapping("/notes/{noteId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long noteId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String text = body.get("text");
        CommentResponse response = commentService.addComment(noteId, text, authentication);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        commentService.deleteComment(commentId, authentication);
        return ResponseEntity.noContent().build();
    }
}
