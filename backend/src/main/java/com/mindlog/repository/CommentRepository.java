package com.mindlog.repository;

import com.mindlog.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>{

    List<Comment> findByUserId(Long userId);

    List<Comment> findByNoteId(Long noteId);

    List<Comment> findByUserIdAndNoteId(Long userId, Long noteId);

}
