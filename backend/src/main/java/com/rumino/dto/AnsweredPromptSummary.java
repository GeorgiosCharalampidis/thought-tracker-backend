package com.rumino.dto;

import java.util.List;

public record AnsweredPromptSummary(
        int promptIndex,
        String question,
        String myAnswer,
        Long myAnswerId,
        int myAnswerCommentCount,
        List<PromptAnswerDto> allAnswers
) {}
