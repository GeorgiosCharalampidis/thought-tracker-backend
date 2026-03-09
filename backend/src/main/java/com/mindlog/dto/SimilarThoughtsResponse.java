package com.mindlog.dto;

import com.mindlog.model.Note;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SimilarThoughtsResponse {
    private String categoryMessage;
    private List<Note> notes;
    private List<Note> ownNotes;
    private boolean inputAccepted;
    private String validationMessage;

    public SimilarThoughtsResponse() {}

    public SimilarThoughtsResponse(String categoryMessage, List<Note> notes, List<Note> ownNotes) {
        this.categoryMessage = categoryMessage;
        this.notes = notes;
        this.ownNotes = ownNotes;
        this.inputAccepted = true;
    }

    public SimilarThoughtsResponse(String categoryMessage, List<Note> notes, List<Note> ownNotes, boolean inputAccepted, String validationMessage) {
        this.categoryMessage = categoryMessage;
        this.notes = notes;
        this.ownNotes = ownNotes;
        this.inputAccepted = inputAccepted;
        this.validationMessage = validationMessage;
    }
}
