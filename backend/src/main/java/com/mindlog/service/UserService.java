package com.mindlog.service;

import com.mindlog.config.AiServiceConfig;
import com.mindlog.dto.ChatMessage;
import com.mindlog.dto.RegisterRequest;
import com.mindlog.exception.BadCredentialsException;
import com.mindlog.exception.UserNotFoundException;
import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String DEFAULT_AUTHORITY = "ROLE_USER";

    private final UserRepository userRepository;
    private final AiService aiService;
    private final PasswordEncoder passwordEncoder;
    private final AiServiceConfig config;
    public UserService(UserRepository userRepository, AiService aiService, PasswordEncoder passwordEncoder, AiServiceConfig config) {
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.passwordEncoder = passwordEncoder;
        this.config = config;
    }

    public User createUser(User user) {
        String normalizedUsername = normalizeUsername(user.getUsername());
        String normalizedEmail = normalizeEmail(user.getEmail());
        String authority = normalizeAuthority(user.getAuthority());

        validateUniqueUser(normalizedUsername, normalizedEmail);

        User userToSave = new User(normalizedUsername, normalizedEmail, user.getPassword(), authority);
        userToSave.validateCredentials();
        userToSave.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(userToSave);
    }

    public User registerUser(RegisterRequest registerRequest) {
        String email = normalizeEmail(registerRequest.getEmail());
        String username = normalizeUsername(registerRequest.getUsername());
        validateUniqueUser(username, email);
        User user = new User(username, email, registerRequest.getPassword(), DEFAULT_AUTHORITY);
        user.validateCredentials();
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found!"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new UserNotFoundException("User with username '" + username + "' not found!"));
    }

    public User getUserByIdentifier(String identifier) {
        String normalizedIdentifier = identifier == null ? "" : identifier.trim();
        return userRepository.findByUsername(normalizedIdentifier)
                .or(() -> userRepository.findByEmail(normalizedIdentifier.toLowerCase()))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }
        return getUserByUsername(authentication.getName());
    }

    public User requireAuthorizedUser(Long userId, Authentication authentication) {
        User authenticatedUser = getAuthenticatedUser(authentication);
        if (!authenticatedUser.getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to access another user's data");
        }
        return authenticatedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Note> getNotesByUser(User user) {
        return user.getNotes();
    }

    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found!"));

        if (userDetails.getEmail() != null && !userDetails.getEmail().isBlank()) {
            String normalizedEmail = normalizeEmail(userDetails.getEmail());
            if (!normalizedEmail.equals(user.getEmail()) && userRepository.existsByEmail(normalizedEmail)) {
                throw new BadCredentialsException("Email is already in use");
            }
            user.setEmail(normalizedEmail);
        }

        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            if (userDetails.getPassword().length() < 4) {
                throw new BadCredentialsException("Password must be at least 4 characters long");
            }
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found!"));
        userRepository.delete(user);
    }

    public String getAiReflection(Long userId) {
        logger.info("Getting notes for user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found!"));
        StringBuilder NotesBuilder = new StringBuilder();
        user.getNotes().forEach(Note -> NotesBuilder.append(Note.getContent()).append("\n"));
        logger.trace("Compiling AI reflection for user ID: {} with {} notes", userId, NotesBuilder);

        try {
            return aiService.getNotesFromModel(NotesBuilder.toString(), config.getModel());
        } catch (Exception e) {
            return "AI service is currently unavailable. Please try again later.";
        }
    }

    public String chatWithJournal(Long userId, List<ChatMessage> messages) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found!"));

        StringBuilder notesContext = new StringBuilder();
        user.getNotes().forEach(note ->
                notesContext.append("[").append(note.getDate()).append("] ").append(note.getContent()).append("\n")
        );

        List<ChatMessage> effectiveMessages = messages.isEmpty()
                ? List.of(new ChatMessage("user", "Please reflect on my journal entries and share what you notice. Start a real conversation."))
                : messages;

        try {
            return aiService.chat(notesContext.toString(), effectiveMessages);
        } catch (Exception e) {
            return "AI service is currently unavailable. Please try again later.";
        }
    }

    private void validateUniqueUser(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BadCredentialsException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadCredentialsException("Email is already in use");
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeAuthority(String authority) {
        if (authority == null || authority.isBlank()) {
            return DEFAULT_AUTHORITY;
        }
        return authority.trim();
    }
}

