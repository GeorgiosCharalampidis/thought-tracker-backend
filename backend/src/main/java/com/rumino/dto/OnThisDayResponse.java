package com.rumino.dto;

import com.rumino.model.Note;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OnThisDayResponse {
    private List<Note> monthAgo;
    private List<Note> yearAgo;

    public OnThisDayResponse() {}

    public OnThisDayResponse(List<Note> monthAgo, List<Note> yearAgo) {
        this.monthAgo = monthAgo;
        this.yearAgo = yearAgo;
    }
}
