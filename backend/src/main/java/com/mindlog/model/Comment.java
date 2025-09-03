package com.mindlog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Getter
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @ManyToOne
    @JoinColumn(name = "note_id", nullable = false)
    @JsonBackReference
    private Note note;

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
                ", userId=" + (user != null ? user.getId() : null) +
                '}';
    }
}
