package com.mindlog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "resonances", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"note_id", "resonating_note_id"})
})
public class Resonance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "note_id", nullable = false)
    @JsonBackReference("resonance-note")
    private Note note;

    @Setter
    @ManyToOne
    @JoinColumn(name = "resonating_note_id", nullable = false)
    @JsonBackReference("resonance-resonating-note")
    private Note resonatingNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(nullable = false)
    private boolean seen = false;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Resonance() {
    }

    public Resonance(Note note, Note resonatingNote) {
        this.note = note;
        this.resonatingNote = resonatingNote;
    }
}
