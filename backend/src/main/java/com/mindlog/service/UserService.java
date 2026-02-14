package com.mindlog.service;

import com.mindlog.config.AiServiceConfig;
import com.mindlog.exception.UserNotFoundException;
import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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

    public User createUser(User user)
    {
        user.validateCredentials();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found!"));
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
        user.validateCredentials();
        user.setEmail(userDetails.getEmail());
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
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
        // Currently logging all the notes of the user, should change later
        logger.trace("Compiling AI reflection for user ID: {} with {} notes", userId, NotesBuilder);

        try {
            return aiService.getNotesFromModel(NotesBuilder.toString(), config.getModel());
        } catch (Exception e) {
            return "AI service is currently unavailable. Please try again later.";
        }
    }

}