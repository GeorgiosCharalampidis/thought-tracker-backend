package com.mindlog.repository;

import com.mindlog.model.Note;
import com.mindlog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    // Find all Notes by a specific user
    List<Note> findByUser_Id(Long userId);

    // Find notes by user and exact date
    List<Note> findByUser_IdAndDate(Long userId, LocalDate date);

    // Find Notes by user and date range
    List<Note> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    // Find notes by user and category (case-insensitive match)
    List<Note> findByUser_IdAndCategoryIgnoreCase(Long userId, String category);

    // Find notes by category (case-insensitive match)
    List<Note> findByCategory(String category);

    Optional<Note> findByIdAndUser_Id(Long id, Long userId);

    List<Note> findByCategoryAndUser_IdNot(String category, Long userId);

    // List distinct categories for a user
    @Query("select distinct n.category from Note n where n.user.id = :userId and n.category is not null")
    List<String> findDistinctCategoriesByUserId(@Param("userId") Long userId);
}