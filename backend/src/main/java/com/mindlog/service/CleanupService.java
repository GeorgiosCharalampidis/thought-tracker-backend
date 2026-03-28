package com.mindlog.service;

import com.mindlog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);

    private final UserRepository userRepository;

    public CleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void deleteUnverifiedAccounts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        userRepository.deleteByVerifiedFalseAndCreatedAtBefore(cutoff);
        logger.info("Cleaned up unverified accounts older than 24 hours");
    }
}
