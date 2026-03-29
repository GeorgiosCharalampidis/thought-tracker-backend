package com.rumino.controller;

import com.rumino.dto.AuthRequest;
import com.rumino.dto.AuthResponse;
import com.rumino.dto.RegisterRequest;
import com.rumino.dto.UserResponse;
import com.rumino.model.User;
import com.rumino.service.RateLimitService;
import com.rumino.service.UserService;
import com.rumino.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final VerificationService verificationService;
    private final RateLimitService rateLimitService;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, VerificationService verificationService, RateLimitService rateLimitService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.verificationService = verificationService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request
    ) {
        if (!rateLimitService.isAllowed(request.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many registration attempts. Please try again later."));
        }
        User user = userService.registerUser(registerRequest);
        verificationService.createAndSendVerificationToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "VERIFICATION_SENT", "email", user.getEmail()));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        verificationService.verifyToken(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully. You can now log in."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> body, HttpServletRequest request) {
        if (!rateLimitService.isAllowed(request.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again later."));
        }
        verificationService.resendVerificationEmail(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "Verification email resent."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest authRequest,
            HttpServletRequest request
    ) {
        if (!rateLimitService.isLoginAllowed(request.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new AuthResponse("Too many login attempts. Please try again in 15 minutes.", null));
        }
        User user = authenticateIntoSession(authRequest.getIdentifier(), authRequest.getPassword(), request);
        return ResponseEntity.ok(new AuthResponse("Login successful", UserResponse.from(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(UserResponse.from(userService.getAuthenticatedUser(authentication)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        request.getSession(false);
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    private User authenticateIntoSession(String identifier, String password, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier == null ? "" : identifier.trim(), password)
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return userService.getUserByUsername(authentication.getName());
    }
}

