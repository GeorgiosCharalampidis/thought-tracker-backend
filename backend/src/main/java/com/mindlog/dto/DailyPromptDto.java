package com.mindlog.dto;

public record DailyPromptDto(
        int promptIndex,
        String question,
        String userAnswerText,
        Long userAnswerId,
        int userAnswerCommentCount,
        boolean saved,
        int totalAnswerCount
) {
}
