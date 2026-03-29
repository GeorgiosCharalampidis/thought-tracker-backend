package com.rumino.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_answer_comments")
public class PromptAnswerComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_id", nullable = false)
    private Long answerId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public PromptAnswerComment() {}

    public PromptAnswerComment(Long answerId, User user, String text) {
        this.answerId = answerId;
        this.user = user;
        this.text = text;
    }

    public Long getId() { return id; }
    public Long getAnswerId() { return answerId; }
    public User getUser() { return user; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
