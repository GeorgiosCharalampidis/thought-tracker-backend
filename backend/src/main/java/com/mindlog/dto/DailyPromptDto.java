package com.mindlog.dto;

public record DailyPromptDto(
        int promptIndex,
        String question,
        String userAnswerText
) {
}
