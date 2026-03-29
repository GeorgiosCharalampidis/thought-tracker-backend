package com.rumino.dto;

import com.rumino.model.PromptAnswer;

import java.time.LocalDateTime;

public record PromptAnswerDto(
        Long id,
        String answerText,
        LocalDateTime createdAt,
        int commentCount,
        String username
) {
    public static PromptAnswerDto from(PromptAnswer answer) {
        return new PromptAnswerDto(answer.getId(), answer.getAnswerText(), answer.getCreatedAt(), 0, null);
    }
}
