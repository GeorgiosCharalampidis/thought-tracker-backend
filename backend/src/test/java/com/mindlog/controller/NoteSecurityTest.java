package com.mindlog.controller;

import com.mindlog.dto.SimilarThoughtsResponse;
import com.mindlog.exception.GlobalExceptionHandler;
import com.mindlog.model.User;
import com.mindlog.security.MindlogUserDetailsService;
import com.mindlog.security.SecurityConfig;
import com.mindlog.service.NoteService;
import com.mindlog.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class NoteSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MindlogUserDetailsService mindlogUserDetailsService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Test
    void previewEndpointAllowsAnonymousUser() throws Exception {
        given(noteService.previewNoteWithValidation(any())).willReturn(new SimilarThoughtsResponse("", List.of(), true, null));

        mockMvc.perform(post("/api/notes/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"I feel stuck today\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void notesEndpointRejectsAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice", authorities = "ROLE_USER")
    void notesEndpointAllowsAuthenticatedOwner() throws Exception {
        User user = new User("alice", "alice@example.com", "encoded", "ROLE_USER");
        setUserId(user, 1L);
        given(userService.requireAuthorizedUser(eq(1L), any())).willReturn(user);
        given(noteService.getNotesByUserId(1L)).willReturn(java.util.List.of());

        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isOk());
    }

    private void setUserId(User user, Long id) throws Exception {
        var field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }
}
