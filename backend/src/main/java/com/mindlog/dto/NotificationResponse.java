package com.mindlog.dto;

import com.mindlog.model.Comment;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long commentId,
        Long noteId,
        String notePreview,
        String commenterUsername,
        String commentText,
        LocalDateTime commentedAt,
        boolean seen
) {
    private static final int PREVIEW_MAX_LENGTH = 80;

    public static NotificationResponse from(Comment comment) {
        String content = comment.getNote().getContent();
        String preview = content.length() > PREVIEW_MAX_LENGTH
                ? content.substring(0, PREVIEW_MAX_LENGTH).stripTrailing() + "…"
                : content;

        return new NotificationResponse(
                comment.getId(),
                comment.getNote().getId(),
                preview,
                comment.getUser().getUsername(),
                comment.getText(),
                comment.getCreatedAt(),
                comment.isSeen()
        );
    }
}
