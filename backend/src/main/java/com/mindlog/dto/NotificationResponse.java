package com.mindlog.dto;

import com.mindlog.model.Comment;
import com.mindlog.model.Resonance;

import java.time.LocalDateTime;

public class NotificationResponse {

    private static final int PREVIEW_MAX_LENGTH = 80;

    private Long id;
    private String type;
    private Long noteId;
    private String notePreview;
    private String actorUsername;
    private String bodyText;
    private LocalDateTime occurredAt;
    private boolean seen;

    public NotificationResponse() {
    }

    public NotificationResponse(Long id, String type, Long noteId, String notePreview,
                                String actorUsername, String bodyText, LocalDateTime occurredAt, boolean seen) {
        this.id = id;
        this.type = type;
        this.noteId = noteId;
        this.notePreview = notePreview;
        this.actorUsername = actorUsername;
        this.bodyText = bodyText;
        this.occurredAt = occurredAt;
        this.seen = seen;
    }

    public static NotificationResponse fromComment(Comment comment) {
        String content = comment.getNote().getContent();
        String preview = content.length() > PREVIEW_MAX_LENGTH
                ? content.substring(0, PREVIEW_MAX_LENGTH).stripTrailing() + "…"
                : content;

        return new NotificationResponse(
                comment.getId(),
                "COMMENT",
                comment.getNote().getId(),
                preview,
                comment.getUser().getUsername(),
                comment.getText(),
                comment.getCreatedAt(),
                comment.isSeen()
        );
    }

    public static NotificationResponse fromResonance(Resonance resonance) {
        String noteContent = resonance.getNote().getContent();
        String notePreview = noteContent.length() > PREVIEW_MAX_LENGTH
                ? noteContent.substring(0, PREVIEW_MAX_LENGTH).stripTrailing() + "…"
                : noteContent;

        String resonatingContent = resonance.getResonatingNote().getContent();
        String bodyText = resonatingContent.length() > PREVIEW_MAX_LENGTH
                ? resonatingContent.substring(0, PREVIEW_MAX_LENGTH).stripTrailing() + "…"
                : resonatingContent;

        return new NotificationResponse(
                resonance.getId(),
                "RESONANCE",
                resonance.getNote().getId(),
                notePreview,
                null,
                bodyText,
                resonance.getCreatedAt(),
                resonance.isSeen()
        );
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public Long getNoteId() { return noteId; }
    public String getNotePreview() { return notePreview; }
    public String getActorUsername() { return actorUsername; }
    public String getBodyText() { return bodyText; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public boolean isSeen() { return seen; }
}
