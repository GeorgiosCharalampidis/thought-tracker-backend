package com.mindlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindlog.dto.RegisterRequest;
import com.mindlog.model.User;
import com.mindlog.security.MindlogUserDetailsService;
import com.mindlog.security.SecurityConfig;
import com.mindlog.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, com.mindlog.exception.GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private MindlogUserDetailsService mindlogUserDetailsService;

    @Test
    void registerReturnsCreatedUserPayload() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("secret123");

        User user = new User("alice", "alice@example.com", "encoded", "ROLE_USER");
        setUserId(user, 7L);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "alice",
                "secret123",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        given(userService.registerUser(any(RegisterRequest.class))).willReturn(user);
        given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);
        given(userService.getUserByUsername(eq("alice"))).willReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.user.id").value(7))
                .andExpect(jsonPath("$.user.username").value("alice"))
                .andExpect(jsonPath("$.user.email").value("alice@example.com"))
                .andExpect(jsonPath("$.user.authority").value("ROLE_USER"));
    }

    @Test
    void loginReturnsUserPayload() throws Exception {
        User user = new User("alice", "alice@example.com", "encoded", "ROLE_USER");
        setUserId(user, 7L);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "alice",
                "secret123",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);
        given(userService.getUserByUsername(eq("alice"))).willReturn(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "identifier", "alice",
                                "password", "secret123"
                        )))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.user.username").value("alice"));
    }

    @Test
    @WithMockUser(username = "alice")
    void meReturnsAuthenticatedUser() throws Exception {
        User user = new User("alice", "alice@example.com", "encoded", "ROLE_USER");
        setUserId(user, 7L);
        given(userService.getAuthenticatedUser(any(Authentication.class))).willReturn(user);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    private void setUserId(User user, Long id) throws Exception {
        var field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }
}
