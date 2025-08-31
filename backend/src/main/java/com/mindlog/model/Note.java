package com.mindlog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5000)
    @Size(min = 1, max = 5000) // Limit text length
    private String text;

    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = true, length = 255)
    private String subject;
    
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDate getDate() {
        return date;
    }

    public User getUser() {
        return user;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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