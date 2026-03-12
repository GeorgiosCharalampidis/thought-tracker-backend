package com.mindlog.service;

import com.mindlog.dto.NotificationResponse;
import com.mindlog.exception.BadCredentialsException;
import com.mindlog.model.Comment;
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

    public List<NotificationResponse> getNotifications(Authentication authentication) {
        Long userId = userService.getAuthenticatedUser(authentication).getId();
        return commentRepository
                .findByNote_User_IdAndUser_IdNotOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAllSeen(Authentication authentication) {
        Long userId = userService.getAuthenticatedUser(authentication).getId();
        commentRepository.markAllSeenForNoteOwner(userId);
    }

    @Transactional
    public void markOneSeen(Long commentId, Authentication authentication) {
        Long userId = userService.getAuthenticatedUser(authentication).getId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BadCredentialsException("Comment not found"));
        if (!comment.getNote().getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not your notification");
        }
        comment.setSeen(true);
        commentRepository.save(comment);
    }
}
