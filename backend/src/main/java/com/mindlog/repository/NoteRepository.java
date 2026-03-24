package com.mindlog.repository;

import com.mindlog.model.Note;
import com.mindlog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    // Quick insert without embedding column (avoids NULL::vector cast issue)
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO notes (user_id, content, date) VALUES (:userId, :content, :date)", nativeQuery = true)
    void quickInsert(@Param("userId") Long userId, @Param("content") String content, @Param("date") LocalDate date);

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

    // Community mood: count notes by category for past N days
    @Query(value = """
            SELECT category, COUNT(*) FROM notes
            WHERE date >= :since
              AND category IS NOT NULL
            GROUP BY category
            """, nativeQuery = true)
    List<Object[]> countByCategoryAndDateAfter(@Param("since") LocalDate since);

    // Vector similarity queries using pgvector <=> (cosine distance) operator
    // distanceThreshold = 1 - similarityThreshold (cosine distance is inverse of cosine similarity)

    @Query(value = """
            SELECT * FROM notes
            WHERE user_id = :userId
              AND (:excludeId = -1 OR id != :excludeId)
              AND embedding IS NOT NULL
              AND (embedding <=> CAST(:embedding AS vector)) <= :distanceThreshold
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Note> findSimilarByUserId(
            @Param("userId") Long userId,
            @Param("excludeId") Long excludeId,
            @Param("embedding") String embedding,
            @Param("distanceThreshold") float distanceThreshold,
            @Param("limit") int limit);

    @Query(value = """
            SELECT * FROM notes
            WHERE user_id = :userId
              AND category = :category
              AND (:excludeId = -1 OR id != :excludeId)
              AND embedding IS NOT NULL
              AND (embedding <=> CAST(:embedding AS vector)) <= :distanceThreshold
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Note> findSimilarByUserIdAndCategory(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("excludeId") Long excludeId,
            @Param("embedding") String embedding,
            @Param("distanceThreshold") float distanceThreshold,
            @Param("limit") int limit);

    @Query(value = """
            SELECT * FROM notes
            WHERE category = :category
              AND user_id != :userId
              AND embedding IS NOT NULL
              AND (embedding <=> CAST(:embedding AS vector)) <= :distanceThreshold
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Note> findSimilarByCategoryExcludingUser(
            @Param("category") String category,
            @Param("userId") Long userId,
            @Param("embedding") String embedding,
            @Param("distanceThreshold") float distanceThreshold,
            @Param("limit") int limit);

    @Query(value = """
            SELECT * FROM notes
            WHERE category = :category
              AND embedding IS NOT NULL
              AND (embedding <=> CAST(:embedding AS vector)) <= :distanceThreshold
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Note> findSimilarByCategory(
            @Param("category") String category,
            @Param("embedding") String embedding,
            @Param("distanceThreshold") float distanceThreshold,
            @Param("limit") int limit);

    @Query(value = """
            SELECT * FROM notes
            WHERE user_id != :userId
              AND embedding IS NOT NULL
              AND (embedding <=> CAST(:embedding AS vector)) <= :distanceThreshold
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Note> findSimilarExcludingUser(
            @Param("userId") Long userId,
            @Param("embedding") String embedding,
            @Param("distanceThreshold") float distanceThreshold,
            @Param("limit") int limit);
}