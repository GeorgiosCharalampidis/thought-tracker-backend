package com.mindlog.service;

import com.mindlog.dto.SimilarThoughtsResponse;
import com.mindlog.exception.BadCredentialsException;
import com.mindlog.model.Category;
import com.mindlog.model.Note;
import com.mindlog.model.User;
import com.mindlog.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private AiService aiService;

    @InjectMocks
    private NoteService noteService;

    @Test
    void createNoteWithValidationFallsBackToOpenReflectionsWhenCategoryMatchIsTooAmbiguous() throws Exception {
        User user = user(7L, "alice");
        Note note = new Note();
        note.setContent("I feel pulled in two directions lately.");
        setId(note, 11L);

        given(userService.getUserById(7L)).willReturn(user);
        given(aiService.isValidThought(note.getContent())).willReturn(true);
        given(embeddingService.embedAll(anyList())).willAnswer(invocation -> {
            List<String> inputs = invocation.getArgument(0);
            if (inputs.size() == Category.allDescriptions().size()) {
                return ambiguousThemeEmbeddings();
            }
            if (inputs.size() == 1 && note.getContent().equals(inputs.get(0))) {
                return List.of(new float[]{1.0f, 0.0f});
            }
            return List.of(new float[]{0.0f, 1.0f});
        });
        given(noteRepository.save(note)).willReturn(note);
        given(noteRepository.findByUser_Id(7L)).willReturn(List.of(note));
        given(noteRepository.findAll()).willReturn(List.of(note));

        SimilarThoughtsResponse response = noteService.createNoteWithValidation(7L, note);

        assertTrue(response.isInputAccepted());
        assertEquals(Category.OPEN_REFLECTIONS.getDisplayName(), note.getCategory());
        assertEquals("General", note.getSubCategory());
        assertEquals(0, response.getNotes().size());
        assertEquals(0, response.getOwnNotes().size());
    }

    @Test
    void getNotesOfSameCategoryRejectsNotesOutsideTheRequestedUserScope() {
        given(noteRepository.findByIdAndUser_Id(99L, 7L)).willReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> noteService.getNotesOfSameCategory(7L, 99L));
    }

    @Test
    void getNotesOfSameCategoryReturnsOnlyStrongOwnAndCommunityMatches() throws Exception {
        User owner = user(7L, "alice");
        User otherUser = user(9L, "bob");

        Note reference = note(11L, owner, "Anxiety & Overthinking", "Overthinking", new float[]{1.0f, 0.0f}, "I cannot stop overthinking this.");
        Note ownStrong = note(12L, owner, "Anxiety & Overthinking", "Overthinking", new float[]{0.95f, 0.05f}, "This keeps looping in my head.");
        Note ownWeak = note(13L, owner, "Anxiety & Overthinking", "Overthinking", new float[]{0.40f, 0.92f}, "I was distracted by something else.");
        Note communityStrong = note(21L, otherUser, "Anxiety & Overthinking", "Overthinking", new float[]{0.82f, 0.18f}, "My mind keeps spiraling too.");
        Note communityRelaxedSameSubcategory = note(22L, otherUser, "Anxiety & Overthinking", "Overthinking", new float[]{0.63f, 0.78f}, "I get stuck replaying the same thoughts.");
        Note communityWeak = note(23L, otherUser, "Anxiety & Overthinking", "General Anxiety", new float[]{0.30f, 0.95f}, "I feel off but not in the same way.");

        given(noteRepository.findByIdAndUser_Id(11L, 7L)).willReturn(Optional.of(reference));
        given(noteRepository.findByUser_Id(7L)).willReturn(List.of(reference, ownStrong, ownWeak));
        given(noteRepository.findByCategoryAndUser_IdNot(eq("Anxiety & Overthinking"), eq(7L)))
                .willReturn(List.of(communityStrong, communityRelaxedSameSubcategory, communityWeak));

        SimilarThoughtsResponse response = noteService.getNotesOfSameCategory(7L, 11L);

        assertEquals(1, response.getOwnNotes().size());
        assertEquals(12L, response.getOwnNotes().get(0).getId());
        assertEquals(2, response.getNotes().size());
        assertEquals(List.of(21L, 22L), response.getNotes().stream().map(Note::getId).toList());
        assertEquals("You're not alone — 2 others have felt something similar.", response.getCategoryMessage());
    }

    private static List<float[]> ambiguousThemeEmbeddings() {
        List<float[]> embeddings = new ArrayList<>();
        for (int i = 0; i < Category.values().length; i++) {
            embeddings.add(new float[]{0.0f, 1.0f});
        }

        int anxietyIndex = Category.allLabels().indexOf(Category.ANXIETY_OVERTHINKING.getDisplayName());
        int stressIndex = Category.allLabels().indexOf(Category.STRESS_FEELING_OVERWHELMED.getDisplayName());
        embeddings.set(anxietyIndex, new float[]{1.0f, 0.0f});
        embeddings.set(stressIndex, new float[]{0.9999f, 0.01f});
        return embeddings;
    }

    private static User user(Long id, String username) throws Exception {
        User user = new User(username, username + "@example.com", "encoded", "ROLE_USER");
        setId(user, id);
        return user;
    }

    private static Note note(Long id, User user, String category, String subCategory, float[] embedding, String content) throws Exception {
        Note note = new Note();
        setId(note, id);
        note.setUser(user);
        note.setCategory(category);
        note.setSubCategory(subCategory);
        note.setContent(content);
        note.setDate(LocalDate.now());
        note.setEmbedding(embedding);
        return note;
    }

    private static void setId(Object target, Long id) throws Exception {
        var field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}

