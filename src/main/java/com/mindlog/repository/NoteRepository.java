package com.mindlog.repository;

import com.mindlog.model.Note;
import com.mindlog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    // Find all Notes by a specific user
    List<Note> findByUser_UserId(Long userId);

    // Find Notes by user and date range
    List<Note> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
}