package com.mindlog.service;

import com.mindlog.dto.NotificationResponse;
import com.mindlog.repository.CommentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final CommentRepository commentRepository;
    private final UserService userService;

    public NotificationService(CommentRepository commentRepository, UserService userService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
    }

    public List<NotificationResponse> getUnseenNotifications(Authentication authentication) {
        Long userId = userService.getAuthenticatedUser(authentication).getId();
        return commentRepository
                .findByNote_User_IdAndSeenFalseAndUser_IdNotOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAllSeen(Authentication authentication) {
        Long userId = userService.getAuthenticatedUser(authentication).getId();
        commentRepository.markAllSeenForNoteOwner(userId);
    }
}
