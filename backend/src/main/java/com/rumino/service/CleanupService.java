package com.rumino.service;

import com.rumino.model.User;
import com.rumino.repository.EmailVerificationTokenRepository;
import com.rumino.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;

    public CleanupService(UserRepository userRepository, EmailVerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void deleteUnverifiedAccounts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<User> staleUsers = userRepository.findByVerifiedFalseAndCreatedAtBefore(cutoff);
        for (User user : staleUsers) {
            tokenRepository.deleteAllByUser(user);
        }
        userRepository.deleteAll(staleUsers);
        logger.info("Cleaned up {} unverified accounts older than 24 hours", staleUsers.size());
    }
}
