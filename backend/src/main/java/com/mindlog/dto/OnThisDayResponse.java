package com.mindlog.dto;

import com.mindlog.model.Note;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OnThisDayResponse {
    private List<Note> weekAgo;
    private List<Note> monthAgo;
    private List<Note> yearAgo;

    public OnThisDayResponse() {}

    public OnThisDayResponse(List<Note> weekAgo, List<Note> monthAgo, List<Note> yearAgo) {
        this.weekAgo = weekAgo;
        this.monthAgo = monthAgo;
        this.yearAgo = yearAgo;
    }
}
