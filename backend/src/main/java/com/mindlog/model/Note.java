package com.mindlog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "notes")
public class Note {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, length = 5000)
    @Size(min = 1, max = 5000) // Limit text length
    private String text;

    @Setter
    @Column(nullable = false)
    private LocalDate date;
    
    @Setter
    @Column(nullable = true, length = 255)
    private String subject;
    
    @Setter
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // Default constructor (required by JPA)
    public Note() {
    }

    // Parameterized constructor
    public Note(String text, LocalDate date, User user, String subject) {
        this.text = text;
        this.date = date;
        this.subject = subject;
        this.user = user;
    }

    // toString() method (optional, for debugging)
    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", date=" + date +
                ", subject='" + subject + '\'' +
                ", user=" + user +
                '}';
    }
}