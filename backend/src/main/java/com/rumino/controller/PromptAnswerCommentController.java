package com.rumino.controller;

import com.rumino.dto.PromptAnswerCommentResponse;
import com.rumino.service.PromptAnswerCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PromptAnswerCommentController {

    private final PromptAnswerCommentService commentService;

    public PromptAnswerCommentController(PromptAnswerCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/prompt-answers/{answerId}/comments")
    public ResponseEntity<List<PromptAnswerCommentResponse>> getComments(
            @PathVariable Long answerId,
            Authentication authentication) {
        return ResponseEntity.ok(commentService.getComments(answerId, authentication));
    }

    @PostMapping("/prompt-answers/{answerId}/comments")
    public ResponseEntity<PromptAnswerCommentResponse> addComment(
            @PathVariable Long answerId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        return ResponseEntity.ok(commentService.addComment(answerId, body.get("text"), authentication));
    }

    @DeleteMapping("/prompt-answer-comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        commentService.deleteComment(commentId, authentication);
        return ResponseEntity.noContent().build();
    }
}
