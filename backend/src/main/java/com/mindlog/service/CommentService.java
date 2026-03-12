package com.mindlog.service;

import com.mindlog.dto.CommentResponse;
import com.mindlog.exception.BadCredentialsException;
import com.mindlog.model.Comment;
import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.repository.CommentRepository;
import com.mindlog.repository.NoteRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final NoteRepository noteRepository;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, NoteRepository noteRepository, UserService userService) {
        this.commentRepository = commentRepository;
        this.noteRepository = noteRepository;
        this.userService = userService;
    }

    public List<CommentResponse> getCommentsForNote(Long noteId, Authentication authentication) {
        noteRepository.findById(noteId)
                .orElseThrow(() -> new BadCredentialsException("Note with ID " + noteId + " not found"));

        Long currentUserId = resolveCurrentUserId(authentication);

        return commentRepository.findByNote_IdOrderByCreatedAtAsc(noteId)
                .stream()
                .map(c -> CommentResponse.from(c, currentUserId))
                .collect(Collectors.toList());
    }

    public CommentResponse addComment(Long noteId, String text, Authentication authentication) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Comment text cannot be empty");
        }
        if (text.length() > 500) {
            throw new IllegalArgumentException("Comment text cannot exceed 500 characters");
        }

        User commenter = userService.getAuthenticatedUser(authentication);

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BadCredentialsException("Note with ID " + noteId + " not found"));

        Comment comment = new Comment(text.strip(), note, commenter);
        Comment saved = commentRepository.save(comment);

        // Keep comment_count in sync
        int newCount = (note.getCommentCount() == null ? 0 : note.getCommentCount()) + 1;
        note.setCommentCount(newCount);
        noteRepository.save(note);

        logger.info("User {} added comment {} on note {}", commenter.getId(), saved.getId(), noteId);
        return CommentResponse.from(saved, commenter.getId());
    }

    public void deleteComment(Long commentId, Authentication authentication) {
        User requester = userService.getAuthenticatedUser(authentication);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BadCredentialsException("Comment with ID " + commentId + " not found"));

        if (!comment.getUser().getId().equals(requester.getId())) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        Note note = comment.getNote();
        commentRepository.delete(comment);

        // Keep comment_count in sync
        if (note != null && note.getCommentCount() != null && note.getCommentCount() > 0) {
            note.setCommentCount(note.getCommentCount() - 1);
            noteRepository.save(note);
        }

        logger.info("User {} deleted comment {}", requester.getId(), commentId);
    }

    public void deleteCommentsByUserId(Long userId) {
        commentRepository.deleteAll(commentRepository.findByUser_Id(userId));
    }

    public void deleteCommentsByNoteId(Long noteId) {
        commentRepository.deleteAll(commentRepository.findByNote_Id(noteId));
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
