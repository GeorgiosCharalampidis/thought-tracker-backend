package com.mindlog.service;

import com.mindlog.dto.AnsweredPromptSummary;
import com.mindlog.dto.DailyPromptDto;
import com.mindlog.dto.PromptAnswerDto;
import com.mindlog.exception.BadCredentialsException;
import com.mindlog.model.PromptAnswer;
import com.mindlog.repository.PromptAnswerCommentRepository;
import com.mindlog.repository.PromptAnswerRepository;
import com.mindlog.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DailyPromptService {

    private static final List<String> PROMPTS = List.of(
            "What emotion has been following you around today, and what might it be trying to tell you?",
            "Describe a moment this week when you felt most like yourself.",
            "What is one thing you're holding onto that might be worth letting go of?",
            "What does rest look like for you right now, and are you getting enough of it?",
            "What are you most grateful for in your life at this moment?",
            "What's a small win you had recently that you haven't given yourself credit for?",
            "What relationship in your life needs a little more attention right now?",
            "What would you tell your past self from one year ago?",
            "What does your ideal day look like, and how close are you to it?",
            "What fear is holding you back from something you want?",
            "What habit or routine has been helping you most lately?",
            "How have you changed in the last six months?",
            "What does success mean to you right now, as opposed to before?",
            "What part of your life feels the most out of balance?",
            "What boundaries do you need to set or strengthen?",
            "What are you most proud of achieving this year so far?",
            "When did you last do something purely for the joy of it?",
            "What word best describes how you feel about your life right now?",
            "What would make tomorrow feel like a good day?",
            "Who in your life makes you feel truly seen and heard?",
            "What story do you keep telling yourself that may no longer be true?",
            "What does your body need right now that you've been ignoring?",
            "What is something you've been avoiding thinking about?",
            "What are you learning about yourself through a current challenge?",
            "What does kindness to yourself look like today?",
            "What memory made you smile recently?",
            "What do you wish other people understood about how you're feeling?",
            "What's something new you'd like to try in the next month?",
            "How do you recharge when you're emotionally drained?",
            "What part of your daily routine brings you the most peace?",
            "What value is most important to you right now, and are you living it?",
            "What would a more compassionate version of yourself do differently today?",
            "When was the last time you said no to something, and how did it feel?",
            "What do you need to forgive yourself for?",
            "What connection have you been craving lately?",
            "What part of your life feels the most alive and energized?",
            "What conversation have you been putting off having?",
            "What does loneliness feel like for you, and when do you feel it most?",
            "What has surprised you most about yourself recently?",
            "What is one thing you'd change about your daily life if you could?",
            "What does your inner critic say most often, and is it true?",
            "How are you coping with uncertainty in your life right now?",
            "What would you do with an extra hour each day?",
            "What are you looking forward to in the coming weeks?",
            "How do you feel about where you are in life right now compared to your dreams?",
            "What experience has shaped you most in the past year?",
            "What is something you've been meaning to say to someone?",
            "How does your current environment affect your mood?",
            "What creative outlet do you wish you spent more time on?",
            "What does 'home' mean to you right now?",
            "What part of your past are you still processing?",
            "What are you doing when time seems to fly by?",
            "What does anxiety feel like in your body, and what helps?",
            "What goal have you quietly given up on, and is it worth revisiting?",
            "What person in your life deserves more of your appreciation?",
            "What would you do differently if you knew no one was judging you?",
            "What's the most important lesson you've learned from a difficult experience?",
            "What does your gut feeling tell you about a current decision?",
            "What brings you a sense of purpose right now?",
            "What part of life are you treating as temporary that might be worth embracing?",
            "What would you like to be remembered for?"
    );

    private final PromptAnswerRepository promptAnswerRepository;
    private final PromptAnswerCommentRepository promptAnswerCommentRepository;
    private final UserRepository userRepository;

    public DailyPromptService(PromptAnswerRepository promptAnswerRepository,
                              PromptAnswerCommentRepository promptAnswerCommentRepository,
                              UserRepository userRepository) {
        this.promptAnswerRepository = promptAnswerRepository;
        this.promptAnswerCommentRepository = promptAnswerCommentRepository;
        this.userRepository = userRepository;
    }

    public int getTodaysPromptIndex() {
        int dayOfYear = LocalDate.now().getDayOfYear();
        return dayOfYear % PROMPTS.size();
    }

    public DailyPromptDto getTodaysPrompt(Long userId) {
        int index = getTodaysPromptIndex();
        String question = PROMPTS.get(index);
        Optional<PromptAnswer> existing = promptAnswerRepository.findByUserIdAndPromptIndex(userId, index);
        String userAnswerText = existing.map(PromptAnswer::getAnswerText).orElse(null);
        boolean saved = existing.map(PromptAnswer::isSaved).orElse(false);
        return new DailyPromptDto(index, question, userAnswerText, saved);
    }

    public void savePrompt(Long userId, int promptIndex) {
        promptAnswerRepository.findByUserIdAndPromptIndex(userId, promptIndex).ifPresent(a -> {
            a.setSaved(true);
            promptAnswerRepository.save(a);
        });
    }

    public void unsavePrompt(Long userId, int promptIndex) {
        promptAnswerRepository.findByUserIdAndPromptIndex(userId, promptIndex).ifPresent(a -> {
            a.setSaved(false);
            promptAnswerRepository.save(a);
        });
    }

    public PromptAnswerDto submitAnswer(Long userId, int promptIndex, String answerText) {
        if (promptIndex < 0 || promptIndex >= PROMPTS.size()) {
            throw new BadCredentialsException("Invalid prompt index");
        }
        if (answerText == null || answerText.isBlank()) {
            throw new BadCredentialsException("Answer cannot be empty");
        }
        String trimmed = answerText.trim();
        if (trimmed.length() > 500) {
            throw new BadCredentialsException("Answer must be 500 characters or fewer");
        }

        Optional<PromptAnswer> existing = promptAnswerRepository.findByUserIdAndPromptIndex(userId, promptIndex);
        if (existing.isPresent()) {
            throw new BadCredentialsException("You have already answered this prompt");
        }

        PromptAnswer answer = new PromptAnswer(userId, promptIndex, trimmed);
        PromptAnswer saved = promptAnswerRepository.save(answer);
        return PromptAnswerDto.from(saved);
    }

    public List<PromptAnswerDto> getAnswersForPrompt(int promptIndex, Long excludeUserId) {
        return promptAnswerRepository.findByPromptIndex(promptIndex).stream()
                .filter(a -> !a.getUserId().equals(excludeUserId))
                .map(a -> {
                    String username = userRepository.findById(a.getUserId())
                            .map(u -> u.getUsername())
                            .orElse("anonymous");
                    return new PromptAnswerDto(
                            a.getId(),
                            a.getAnswerText(),
                            a.getCreatedAt(),
                            promptAnswerCommentRepository.countByAnswerId(a.getId()),
                            username
                    );
                })
                .toList();
    }

    public List<AnsweredPromptSummary> getMyAnsweredPrompts(Long userId) {
        List<PromptAnswer> myAnswers = promptAnswerRepository.findByUserId(userId);
        return myAnswers.stream()
                .filter(PromptAnswer::isSaved)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(myAnswer -> {
                    int index = myAnswer.getPromptIndex();
                    String question = PROMPTS.get(index);
                    List<PromptAnswerDto> allAnswers = promptAnswerRepository.findByPromptIndex(index).stream()
                            .filter(a -> !a.getUserId().equals(userId))
                            .map(a -> {
                                String username = userRepository.findById(a.getUserId())
                                        .map(u -> u.getUsername())
                                        .orElse("anonymous");
                                return new PromptAnswerDto(
                                        a.getId(),
                                        a.getAnswerText(),
                                        a.getCreatedAt(),
                                        promptAnswerCommentRepository.countByAnswerId(a.getId()),
                                        username
                                );
                            })
                            .toList();
                    return new AnsweredPromptSummary(index, question, myAnswer.getAnswerText(), allAnswers);
                })
                .toList();
    }
}
