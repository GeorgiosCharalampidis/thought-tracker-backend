package com.mindlog.service;

import com.mindlog.exception.UserNotFoundException;

import com.mindlog.model.Comment;
import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.repository.CommentRepository;
import com.mindlog.repository.NoteRepository;
import com.mindlog.repository.UserRepository;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, NoteRepository noteRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
    }

    public Comment createComment(Comment comment) {
        if (comment == null || comment.getText() == null) {
            throw new IllegalArgumentException("Comment cannot be null or empty");
        }
        if (comment.getNote() == null || comment.getNote().getId() == null) {
            throw new IllegalArgumentException("Comment must be associated with a valid note");
        }
        if (comment.getUser() == null || comment.getUser().getUserId() == null) {
            throw new IllegalArgumentException("Comment must be associated with a valid user");
        }
        
        // Verify that the note exists
        Note note = noteRepository.findById(comment.getNote().getId())
            .orElseThrow(() -> new IllegalArgumentException("Note with ID " + comment.getNote().getId() + " not found"));
        
        // Verify that the user exists
        User user = userRepository.findById(comment.getUser().getUserId())
            .orElseThrow(() -> new UserNotFoundException("User with ID " + comment.getUser().getUserId() + " not found"));

        comment.setNote(note);
        comment.setUser(user);
        
        return this.commentRepository.save(comment);
    }

    public List<Comment> getCommentsByNoteId(Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException("Note ID cannot be null");
        }
        return commentRepository.findByNoteId(noteId);
    }

    public List<Comment> getCommentsByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return commentRepository.findByUserId(userId);
    }

    public Comment getCommentById(Long commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }
        return commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment with ID " + commentId + " not found"));
    }

    public void deleteComment(Long commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }
        
        if (!commentRepository.existsById(commentId)) {
            throw new IllegalArgumentException("Comment with ID " + commentId + " not found");
        }
        
        commentRepository.deleteById(commentId);
    }

    public void deleteCommentsByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        List<Comment> comments = commentRepository.findByUserId(userId);
        commentRepository.deleteAll(comments);
    }

    public void deleteCommentsByNoteId(Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException("Note ID cannot be null");
        }

        List<Comment> comments = commentRepository.findByNoteId(noteId);
        commentRepository.deleteAll(comments);
    }


}
