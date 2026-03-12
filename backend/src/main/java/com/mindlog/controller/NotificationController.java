package com.mindlog.controller;

import com.mindlog.dto.NotificationResponse;
import com.mindlog.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getNotifications(authentication));
    }

    @PostMapping("/mark-seen")
    public ResponseEntity<Void> markAllSeen(Authentication authentication) {
        notificationService.markAllSeen(authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/mark-seen")
    public ResponseEntity<Void> markOneSeen(
            @PathVariable Long commentId,
            Authentication authentication) {
        notificationService.markOneSeen(commentId, authentication);
        return ResponseEntity.noContent().build();
    }
}
