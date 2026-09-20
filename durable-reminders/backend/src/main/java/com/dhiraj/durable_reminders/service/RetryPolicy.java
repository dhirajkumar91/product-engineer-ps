package com.dhiraj.durable_reminders.service;

import org.springframework.stereotype.Component;

@Component
public class RetryPolicy {
    private static final int MAX_ATTEMPTS = 3;

    public boolean shouldRetry(int attemptNumber) {
        return attemptNumber < MAX_ATTEMPTS;
    }

    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }
}
