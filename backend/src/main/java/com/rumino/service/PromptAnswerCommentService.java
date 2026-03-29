package com.rumino.service;

import com.rumino.dto.PromptAnswerCommentResponse;
import com.rumino.exception.BadCredentialsException;
import com.rumino.model.PromptAnswerComment;
import com.rumino.model.User;
import com.rumino.repository.PromptAnswerCommentRepository;
import com.rumino.repository.PromptAnswerRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptAnswerCommentService {

    private final PromptAnswerCommentRepository commentRepository;
    private final PromptAnswerRepository answerRepository;
    private final UserService userService;

    public PromptAnswerCommentService(PromptAnswerCommentRepository commentRepository,
                                      PromptAnswerRepository answerRepository,
                                      UserService userService) {
        this.commentRepository = commentRepository;
        this.answerRepository = answerRepository;
        this.userService = userService;
    }

    public List<PromptAnswerCommentResponse> getComments(Long answerId, Authentication authentication) {
        answerRepository.findById(answerId)
                .orElseThrow(() -> new BadCredentialsException("Answer not found"));
        Long currentUserId = resolveCurrentUserId(authentication);
        return commentRepository.findByAnswerIdOrderByCreatedAtAsc(answerId)
                .stream()
                .map(c -> PromptAnswerCommentResponse.from(c, currentUserId))
                .toList();
    }

    public PromptAnswerCommentResponse addComment(Long answerId, String text, Authentication authentication) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Comment text cannot be empty");
        if (text.length() > 500) throw new IllegalArgumentException("Comment too long");
        answerRepository.findById(answerId)
                .orElseThrow(() -> new BadCredentialsException("Answer not found"));
        User commenter = userService.getAuthenticatedUser(authentication);
        PromptAnswerComment comment = new PromptAnswerComment(answerId, commenter, text.strip());
        PromptAnswerComment saved = commentRepository.save(comment);
        return PromptAnswerCommentResponse.from(saved, commenter.getId());
    }

    public void deleteComment(Long commentId, Authentication authentication) {
        User requester = userService.getAuthenticatedUser(authentication);
        PromptAnswerComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BadCredentialsException("Comment not found"));
        if (!comment.getUser().getId().equals(requester.getId())) {
            throw new AccessDeniedException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    private Long resolveCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        try {
            return userService.getAuthenticatedUser(authentication).getId();
        } catch (Exception e) {
            return null;
        }
    }
}
