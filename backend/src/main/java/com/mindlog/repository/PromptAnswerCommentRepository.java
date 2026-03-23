package com.mindlog.repository;

import com.mindlog.model.PromptAnswerComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptAnswerCommentRepository extends JpaRepository<PromptAnswerComment, Long> {
    List<PromptAnswerComment> findByAnswerIdOrderByCreatedAtAsc(Long answerId);
    int countByAnswerId(Long answerId);
}
