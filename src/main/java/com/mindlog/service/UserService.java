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
import java.util.Map;


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

    public String getAiReflection(Long userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found!");
        }
        logger.info("Getting notes for user: {}", user.getUsername());
        StringBuilder NotesBuilder = new StringBuilder();
        user.getNotes().forEach(Note -> NotesBuilder.append(Note.getText()).append("\n"));
        
        // Check if AI service is available first
        if (!aiService.isAiServiceAvailable()) {
            logger.warn("AI service is not available, returning fallback response");
            return generateFallbackReflection(user.getNotes());
        }
        
        try {
            return aiService.getNotesFromModel(NotesBuilder.toString(), config.getModel());
        } catch (IllegalStateException e) {
            logger.error("AI service error: {}", e.getMessage());
            return generateFallbackReflection(user.getNotes());
        }
    }

    private String generateFallbackReflection(List<Note> notes) {
        if (notes.isEmpty()) {
            return "You haven't written any notes yet. Start by writing your first note to get insights about your thoughts and feelings.";
        }

        StringBuilder reflection = new StringBuilder();
        reflection.append("Based on your notes, here are some observations:\n\n");

        // Count notes by date
        Map<String, Long> notesByDate = notes.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                note -> note.getDate().toString(),
                java.util.stream.Collectors.counting()
            ));

        reflection.append("📊 Activity Summary:\n");
        reflection.append("- Total notes: ").append(notes.size()).append("\n");
        reflection.append("- Days with notes: ").append(notesByDate.size()).append("\n");

        if (notesByDate.size() > 1) {
            double avgNotesPerDay = (double) notes.size() / notesByDate.size();
            reflection.append("- Average notes per day: ").append(String.format("%.1f", avgNotesPerDay)).append("\n");
        }

        // Show recent activity
        List<Note> recentNotes = notes.stream()
            .sorted((n1, n2) -> n2.getDate().compareTo(n1.getDate()))
            .limit(3)
            .toList();

        if (!recentNotes.isEmpty()) {
            reflection.append("\n📝 Recent Activity:\n");
            for (Note note : recentNotes) {
                String preview = note.getText().length() > 100
                    ? note.getText().substring(0, 100) + "..."
                    : note.getText();
                reflection.append("- ").append(note.getDate())
                    .append(": ").append(preview).append("\n");
            }
        }

        reflection.append("\n💭 Reflection:\n");
        reflection.append("Your notes show a thoughtful approach to self-reflection. ");
        if (notes.size() > 10) {
            reflection.append("You've been consistently documenting your thoughts, which is a great habit for personal growth. ");
        } else if (notes.size() > 5) {
            reflection.append("You're building a good foundation for self-reflection. ");
        } else {
            reflection.append("You're just getting started with your journaling journey. ");
        }
        reflection.append("Consider what patterns you notice in your thoughts and feelings over time.");

        return reflection.toString();
    }

}