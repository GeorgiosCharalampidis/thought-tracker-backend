package com.mindlog.repository;

import com.mindlog.model.Note;
import com.mindlog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    // Find all Notes by a specific user
    List<Note> findByUser_UserId(Long userId);

    // Find notes by user and exact date
    List<Note> findByUser_UserIdAndDate(Long userId, LocalDate date);

    // Find Notes by user and date range
    List<Note> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    // Find notes by user and subject (case-insensitive match)
    List<Note> findByUser_UserIdAndSubjectIgnoreCase(Long userId, String subject);

    // Find notes by subject (case-insensitive match)
    List<Note> findBySubject(String subject);

    // List distinct subjects for a user
    @Query("select distinct n.subject from Note n where n.user.userId = :userId and n.subject is not null")
    List<String> findDistinctSubjectsByUserId(@Param("userId") Long userId);
}