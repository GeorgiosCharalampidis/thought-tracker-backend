package com.mindlog.service;

import com.mindlog.dto.NotificationResponse;
import com.mindlog.exception.BadCredentialsException;
import com.mindlog.model.Comment;
import com.mindlog.repository.CommentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class NotificationService {

    private final CommentRepository commentRepository;
    private final ResonanceService resonanceService;
    private final UserService userService;

    public NotificationService(CommentRepository commentRepository, ResonanceService resonanceService, UserService userService) {
        this.commentRepository = commentRepository;
        this.resonanceService = resonanceService;
        this.userService = userService;
    }

    public List<NotificationResponse> getNotifications(Authentication authentication) {
        Long userId = userService.getAuthenticatedUser(authentication).getId();

        List<NotificationResponse> comments = commentRepository
                .findByNote_User_IdAndSeenFalseAndUser_IdNotOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(NotificationResponse::fromComment)
                .collect(Collectors.toList());

        List<NotificationResponse> resonances = resonanceService.getUnseenResonancesForUser(userId)
                .stream()
                .map(NotificationResponse::fromResonance)
                .collect(Collectors.toList());

        return Stream.concat(comments.stream(), resonances.stream())
                .sorted(Comparator.comparing(NotificationResponse::getOccurredAt).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAllSeen(Authentication authentication) {
        Long userId = userService.getAuthenticatedUser(authentication).getId();
        commentRepository.markAllSeenForNoteOwner(userId);
        resonanceService.markAllSeenForUser(userId);
    }

    @Transactional
    public void markOneSeen(Long id, String type, Authentication authentication) {
        Long userId = userService.getAuthenticatedUser(authentication).getId();
        if ("RESONANCE".equalsIgnoreCase(type)) {
            resonanceService.markOneSeen(id, userId);
        } else {
            Comment comment = commentRepository.findById(id)
                    .orElseThrow(() -> new BadCredentialsException("Comment not found"));
            if (!comment.getNote().getUser().getId().equals(userId)) {
                throw new org.springframework.security.access.AccessDeniedException("Not your notification");
            }
            comment.setSeen(true);
            commentRepository.save(comment);
        }
    }
}
