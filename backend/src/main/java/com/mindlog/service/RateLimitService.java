package com.mindlog.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MILLIS = 60 * 60 * 1_000L; // 1 hour

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_WINDOW_MILLIS = 15 * 60 * 1_000L; // 15 minutes

    private final ConcurrentHashMap<String, Deque<Long>> attempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deque<Long>> loginAttempts = new ConcurrentHashMap<>();

    public synchronized boolean isAllowed(String ip) {
        return checkAndRecord(ip, attempts, MAX_ATTEMPTS, WINDOW_MILLIS);
    }

    public synchronized boolean isLoginAllowed(String ip) {
        return checkAndRecord(ip, loginAttempts, MAX_LOGIN_ATTEMPTS, LOGIN_WINDOW_MILLIS);
    }

    private boolean checkAndRecord(String key, ConcurrentHashMap<String, Deque<Long>> map, int max, long windowMillis) {
        long now = Instant.now().toEpochMilli();
        Deque<Long> timestamps = map.computeIfAbsent(key, k -> new ArrayDeque<>());

        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMillis) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= max) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }
}
