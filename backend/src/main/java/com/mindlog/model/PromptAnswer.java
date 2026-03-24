package com.mindlog.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_answers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "prompt_index"})
})
public class PromptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "prompt_index", nullable = false)
    private int promptIndex;

    @Column(name = "answer_text", nullable = false, length = 500)
    private String answerText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "saved")
    private Boolean saved = false;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public PromptAnswer() {
    }

    public PromptAnswer(Long userId, int promptIndex, String answerText) {
        this.userId = userId;
        this.promptIndex = promptIndex;
        this.answerText = answerText;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public int getPromptIndex() { return promptIndex; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isSaved() { return saved != null && saved; }
    public void setSaved(boolean saved) { this.saved = saved; }
}
