package com.mindlog.repository;

import com.mindlog.model.Resonance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResonanceRepository extends JpaRepository<Resonance, Long> {

    List<Resonance> findByNote_User_IdOrderByCreatedAtDesc(Long userId);

    List<Resonance> findByNote_User_IdAndSeenFalseOrderByCreatedAtDesc(Long userId);

    List<Resonance> findByNote_IdOrderByCreatedAtDesc(Long noteId);

    boolean existsByNote_IdAndResonatingNote_Id(Long noteId, Long resonatingNoteId);

    @Modifying
    @Query("UPDATE Resonance r SET r.seen = true WHERE r.note.user.id = :userId AND r.seen = false")
    void markAllSeenForNoteOwner(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Resonance r WHERE r.note.id = :noteId OR r.resonatingNote.id = :noteId")
    void deleteByNoteId(@Param("noteId") Long noteId);
}
