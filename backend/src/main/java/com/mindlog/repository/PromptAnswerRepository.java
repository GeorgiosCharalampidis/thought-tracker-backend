package com.mindlog.repository;

import com.mindlog.model.PromptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptAnswerRepository extends JpaRepository<PromptAnswer, Long> {

    Optional<PromptAnswer> findByUserIdAndPromptIndex(Long userId, int promptIndex);

    List<PromptAnswer> findByPromptIndex(int promptIndex);

    List<PromptAnswer> findByUserId(Long userId);
}
