package com.mindlog.dto;

import com.mindlog.model.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String text,
        String username,
        LocalDateTime createdAt,
        boolean own
) {
    public static CommentResponse from(Comment comment, Long currentUserId) {
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getUser().getUsername(),
                comment.getCreatedAt(),
                comment.getUser().getId().equals(currentUserId)
        );
    }
}
