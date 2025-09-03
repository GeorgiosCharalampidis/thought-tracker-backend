package com.mindlog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Getter
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commentId")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "noteId", nullable = false)
    @JsonBackReference
    private Note note;

    @Setter
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Setter
    private String text;

    public Comment() {
    }

    public Comment(String text, Note note, User user) {
        this.text = text;
        this.note = note;
        this.user = user;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", noteId=" + (note != null ? note.getId() : null) +
                ", userId=" + (user != null ? user.getUserId() : null) +
                '}';
    }
}
