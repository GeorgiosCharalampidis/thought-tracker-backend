package com.mindlog.controller;

import com.mindlog.dto.AnsweredPromptSummary;
import com.mindlog.dto.DailyPromptDto;
import com.mindlog.dto.PromptAnswerDto;
import com.mindlog.model.User;
import com.mindlog.service.DailyPromptService;
import com.mindlog.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/daily-prompt")
public class DailyPromptController {

    private final DailyPromptService dailyPromptService;
    private final UserService userService;

    public DailyPromptController(DailyPromptService dailyPromptService, UserService userService) {
        this.dailyPromptService = dailyPromptService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<DailyPromptDto> getTodaysPrompt(Authentication authentication) {
        User user = userService.getAuthenticatedUser(authentication);
        return ResponseEntity.ok(dailyPromptService.getTodaysPrompt(user.getId()));
    }

    @PostMapping("/{promptIndex}/answer")
    public ResponseEntity<PromptAnswerDto> submitAnswer(
            @PathVariable int promptIndex,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        User user = userService.getAuthenticatedUser(authentication);
        String answerText = body.get("answerText");
        PromptAnswerDto result = dailyPromptService.submitAnswer(user.getId(), promptIndex, answerText);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{promptIndex}/answers")
    public ResponseEntity<List<PromptAnswerDto>> getAnswers(
            @PathVariable int promptIndex,
            Authentication authentication) {
        User user = userService.getAuthenticatedUser(authentication);
        return ResponseEntity.ok(dailyPromptService.getAnswersForPrompt(promptIndex, user.getId()));
    }

    @PostMapping("/{promptIndex}/save")
    public ResponseEntity<Void> savePrompt(@PathVariable int promptIndex, Authentication authentication) {
        User user = userService.getAuthenticatedUser(authentication);
        dailyPromptService.savePrompt(user.getId(), promptIndex);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{promptIndex}/save")
    public ResponseEntity<Void> unsavePrompt(@PathVariable int promptIndex, Authentication authentication) {
        User user = userService.getAuthenticatedUser(authentication);
        dailyPromptService.unsavePrompt(user.getId(), promptIndex);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-answers")
    public ResponseEntity<List<AnsweredPromptSummary>> getMyAnswers(Authentication authentication) {
        User user = userService.getAuthenticatedUser(authentication);
        return ResponseEntity.ok(dailyPromptService.getMyAnsweredPrompts(user.getId()));
    }
}
