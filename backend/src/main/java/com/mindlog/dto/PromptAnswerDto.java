package com.mindlog.dto;

import com.mindlog.model.PromptAnswer;

import java.time.LocalDateTime;

public record PromptAnswerDto(
        String answerText,
        LocalDateTime createdAt
) {
    public static PromptAnswerDto from(PromptAnswer answer) {
        return new PromptAnswerDto(answer.getAnswerText(), answer.getCreatedAt());
    }
}
