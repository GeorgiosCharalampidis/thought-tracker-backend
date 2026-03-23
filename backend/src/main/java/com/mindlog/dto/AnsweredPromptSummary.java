package com.mindlog.dto;

import java.util.List;

public record AnsweredPromptSummary(
        int promptIndex,
        String question,
        String myAnswer,
        List<PromptAnswerDto> allAnswers
) {}
