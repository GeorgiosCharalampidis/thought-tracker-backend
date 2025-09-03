package com.mindlog.repository;

import com.mindlog.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>{

    List<Comment> findByUser_Id(Long userId);

    List<Comment> findByNote_Id(Long noteId);

    List<Comment> findByUser_IdAndNote_Id(Long userId, Long noteId);

}
