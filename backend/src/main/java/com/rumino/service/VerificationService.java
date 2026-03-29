package com.rumino.service;

import com.rumino.exception.BadCredentialsException;
import com.rumino.exception.UserNotFoundException;
import com.rumino.model.EmailVerificationToken;
import com.rumino.model.User;
import com.rumino.repository.EmailVerificationTokenRepository;
import com.rumino.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class VerificationService {

    private static final int TOKEN_EXPIRY_HOURS = 24;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public VerificationService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void createAndSendVerificationToken(User user) {
        tokenRepository.deleteAllByUser(user);

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS);
        tokenRepository.save(new EmailVerificationToken(user, token, expiresAt));

        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);
    }

    @Transactional
    public void verifyToken(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid verification link."));

        if (verificationToken.isUsed()) {
            throw new BadCredentialsException("This verification link has already been used.");
        }
        if (verificationToken.isExpired()) {
            throw new BadCredentialsException("This verification link has expired. Please request a new one.");
        }

        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("No account found with that email address."));

        if (user.isVerified()) {
            throw new BadCredentialsException("This email address is already verified.");
        }

        createAndSendVerificationToken(user);
    }
}
