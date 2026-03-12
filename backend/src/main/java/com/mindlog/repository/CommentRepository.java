package com.mindlog.repository;

import com.mindlog.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByUser_Id(Long userId);

    List<Comment> findByNote_Id(Long noteId);

    List<Comment> findByNote_IdOrderByCreatedAtAsc(Long noteId);

    List<Comment> findByUser_IdAndNote_Id(Long userId, Long noteId);

    // Unseen comments on notes owned by userId, excluding comments by the owner themselves
    List<Comment> findByNote_User_IdAndSeenFalseAndUser_IdNotOrderByCreatedAtDesc(Long noteOwnerId, Long commentAuthorId);

    // All comments on notes owned by userId, excluding comments by the owner themselves
    List<Comment> findByNote_User_IdAndUser_IdNotOrderByCreatedAtDesc(Long noteOwnerId, Long commentAuthorId);

    @Modifying
    @Query("UPDATE Comment c SET c.seen = true WHERE c.note.user.id = :userId AND c.seen = false AND c.user.id <> :userId")
    void markAllSeenForNoteOwner(@Param("userId") Long userId);
}
