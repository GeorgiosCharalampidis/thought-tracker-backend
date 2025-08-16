package com.moodtracker.service;

import com.moodtracker.exception.UserNotFoundException;
import com.moodtracker.model.Note;
import com.moodtracker.model.User;
import com.moodtracker.repository.UserRepository;
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


    public UserService(UserRepository userRepository, AiService aiService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.passwordEncoder = passwordEncoder;
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
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found!");
        }
        user.validateCredentials();
        user.setEmail(userDetails.getEmail());
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found!");
        }
        userRepository.delete(user);
    }

    public String getDeepSeekNotes(Long userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found!");
        }
        logger.info("Getting notes for user: {}", user.getUsername());
        StringBuilder NotesBuilder = new StringBuilder();
        user.getNotes().forEach(Note -> NotesBuilder.append(Note.getText()).append("\n"));
        return aiService.getNotesFromDeepSeek(NotesBuilder.toString());
    }
}