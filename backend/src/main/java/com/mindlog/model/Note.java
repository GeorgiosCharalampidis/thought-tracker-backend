package com.mindlog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Getter
@Entity
@Table(name = "notes")
public class Note {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noteId")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Setter
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Comment> comments = new ArrayList<>();

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
    @Column(nullable = true, length = 255)
    private String subCategory;
    
    @Setter
    @Column(name = "embedding", columnDefinition = "TEXT")
    private String embeddingJson; // Store embedding as JSON string


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

    // Embedding utility methods
    public void setEmbedding(float[] embedding) {
        if (embedding == null) {
            this.embeddingJson = null;
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.embeddingJson = mapper.writeValueAsString(embedding);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize embedding", e);
        }
    }

    public float[] getEmbedding() {
        if (embeddingJson == null || embeddingJson.isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(embeddingJson, float[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize embedding", e);
        }
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