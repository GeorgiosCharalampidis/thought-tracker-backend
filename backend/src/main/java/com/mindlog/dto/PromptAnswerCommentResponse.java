package com.mindlog.dto;

import com.mindlog.model.PromptAnswerComment;

import java.time.LocalDateTime;

public record PromptAnswerCommentResponse(
        Long id,
        String text,
        String username,
        LocalDateTime createdAt,
        boolean own
) {
    public static PromptAnswerCommentResponse from(PromptAnswerComment comment, Long currentUserId) {
        return new PromptAnswerCommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getUser().getUsername(),
                comment.getCreatedAt(),
                comment.getUser().getId().equals(currentUserId)
        );
    }
}
