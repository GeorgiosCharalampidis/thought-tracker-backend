package com.mindlog.controller;

import com.mindlog.dto.UserResponse;
import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(UserResponse.from(userService.getAuthenticatedUser(authentication)));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Validate user input
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId, Authentication authentication) {
        User user = userService.requireAuthorizedUser(userId, authentication);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/notes")
    public ResponseEntity<List<Note>> getNotesByUser(@PathVariable Long userId, Authentication authentication) {
        User user = userService.requireAuthorizedUser(userId, authentication);
        List<Note> Notes = userService.getNotesByUser(user);
        return ResponseEntity.ok(Notes);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication authentication) {
        User user = userService.getAuthenticatedUser(authentication);
        if (!"ROLE_ADMIN".equals(user.getAuthority())) {
            throw new AccessDeniedException("Admin access required");
        }
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody User userDetails, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        User updatedUser = userService.updateUser(userId, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, Authentication authentication) {
        userService.requireAuthorizedUser(userId, authentication);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}