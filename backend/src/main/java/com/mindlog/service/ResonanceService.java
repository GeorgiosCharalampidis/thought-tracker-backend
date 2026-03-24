package com.mindlog.service;

import com.mindlog.dto.ResonanceSnippetResponse;
import com.mindlog.exception.BadCredentialsException;
import com.mindlog.model.Note;
import com.mindlog.model.Resonance;
import com.mindlog.repository.NoteRepository;
import com.mindlog.repository.ResonanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResonanceService {

    private static final Logger logger = LoggerFactory.getLogger(ResonanceService.class);

    private final ResonanceRepository resonanceRepository;
    private final NoteRepository noteRepository;

    public ResonanceService(ResonanceRepository resonanceRepository, NoteRepository noteRepository) {
        this.resonanceRepository = resonanceRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional
    public void createResonancesForCommunityNotes(Note resonatingNote, List<Note> communityNotes) {
        if (resonatingNote.getId() == null) return;
        // Use managed proxies so Hibernate doesn't complain about detached entities
        Note managedResonatingNote = noteRepository.getReferenceById(resonatingNote.getId());

        for (Note communityNote : communityNotes) {
            try {
                if (communityNote.getId() == null) continue;
                // Skip self-resonance (same user)
                if (communityNote.getUser() != null && resonatingNote.getUser() != null
                        && communityNote.getUser().getId().equals(resonatingNote.getUser().getId())) {
                    continue;
                }
                if (resonanceRepository.existsByNote_IdAndResonatingNote_Id(communityNote.getId(), resonatingNote.getId())) {
                    continue;
                }
                Note managedCommunityNote = noteRepository.getReferenceById(communityNote.getId());
                Resonance resonance = new Resonance(managedCommunityNote, managedResonatingNote);
                resonanceRepository.save(resonance);
            } catch (Exception e) {
                logger.warn("Failed to create resonance for note {}: {}", communityNote.getId(), e.getMessage());
            }
        }
    }

    public List<Resonance> getResonancesForUser(Long userId) {
        return resonanceRepository.findByNote_User_IdOrderByCreatedAtDesc(userId);
    }

    public List<Resonance> getUnseenResonancesForUser(Long userId) {
        return resonanceRepository.findByNote_User_IdAndSeenFalseOrderByCreatedAtDesc(userId);
    }

    public List<ResonanceSnippetResponse> getResonancesForNote(Long noteId, Long requestingUserId) {
        return resonanceRepository.findByNote_IdOrderByCreatedAtDesc(noteId)
                .stream()
                .filter(r -> r.getNote().getUser().getId().equals(requestingUserId))
                .map(r -> {
                    String body = r.getResonatingNote().getContent();
                    String snippet = body.length() > 120 ? body.substring(0, 120).stripTrailing() + "…" : body;
                    return new ResonanceSnippetResponse(snippet, r.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteResonancesForNote(Long noteId) {
        resonanceRepository.deleteByNoteId(noteId);
    }

    @Transactional
    public void markAllSeenForUser(Long userId) {
        resonanceRepository.markAllSeenForNoteOwner(userId);
    }

    @Transactional
    public void markOneSeen(Long resonanceId, Long requestingUserId) {
        Resonance resonance = resonanceRepository.findById(resonanceId)
                .orElseThrow(() -> new BadCredentialsException("Resonance not found"));
        if (!resonance.getNote().getUser().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("Not your notification");
        }
        resonance.setSeen(true);
        resonanceRepository.save(resonance);
    }
}
