package com.dhiraj.durable_reminders.service;

import com.dhiraj.durable_reminders.entity.Reminder;
import com.dhiraj.durable_reminders.entity.ReminderAttempt;
import com.dhiraj.durable_reminders.enums.AttemptOutcome;
import com.dhiraj.durable_reminders.enums.ReminderStatus;
import com.dhiraj.durable_reminders.exception.PermanentDeliveryException;
import com.dhiraj.durable_reminders.exception.TemporaryDeliveryException;
import com.dhiraj.durable_reminders.notification.NotificationDestination;
import com.dhiraj.durable_reminders.repository.ReminderAttemptRepository;
import com.dhiraj.durable_reminders.repository.ReminderRepository;
import com.dhiraj.durable_reminders.time.ApplicationClock;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ReminderExecutionService {
    private final ReminderRepository reminderRepository;
    private final ReminderAttemptRepository attemptRepository;
    private final NotificationDestination notificationDestination;
    private final ApplicationClock clock;
    private final RetryPolicy retryPolicy;

    public ReminderExecutionService(
            ReminderRepository reminderRepository,
            ReminderAttemptRepository attemptRepository,
            NotificationDestination notificationDestination,
            ApplicationClock clock,
            RetryPolicy retryPolicy
    ) {
        this.reminderRepository = reminderRepository;
        this.attemptRepository = attemptRepository;
        this.notificationDestination = notificationDestination;
        this.clock = clock;
        this.retryPolicy = retryPolicy;
    }

    @Transactional
    public boolean executeReminder(UUID reminderId) {

        Instant now = clock.now();

        int claimed = reminderRepository.claimReminder(
                reminderId,
                now
        );

        /*
         * 0 means another execution already claimed it,
         * it was cancelled/delivered/failed, or it wasn't due.
         */
        if (claimed == 0) {
            return false;
        }

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElse(null);

        if (reminder == null) {
            return false;
        }

        int attemptNumber = reminder.getAttemptCount() + 1;

        reminder.setAttemptCount(attemptNumber);

        ReminderAttempt attempt = new ReminderAttempt();

        attempt.setReminder(reminder);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setDeliveryKey(reminder.getDeliveryKey());
        attempt.setStartedAt(now);
        attempt.setOutcome(AttemptOutcome.TEMPORARY_FAILURE);

        attemptRepository.save(attempt);

        try {

            notificationDestination.deliver(
                    reminder.getDeliveryKey(),
                    reminder.getContent()
            );

            Instant completedAt = clock.now();

            attempt.setCompletedAt(completedAt);
            attempt.setOutcome(AttemptOutcome.SUCCESS);

            reminder.setStatus(ReminderStatus.DELIVERED);
            reminder.setUpdatedAt(completedAt);

            attemptRepository.save(attempt);
            reminderRepository.save(reminder);

            return true;

        } catch (TemporaryDeliveryException exception) {

            handleTemporaryFailure(
                    reminder,
                    attempt,
                    exception
            );

            return false;

        } catch (PermanentDeliveryException exception) {

            handlePermanentFailure(
                    reminder,
                    attempt,
                    exception
            );

            return false;

        } catch (Exception exception) {

            handlePermanentFailure(
                    reminder,
                    attempt,
                    exception
            );

            return false;
        }
    }

    private void handleTemporaryFailure(
            Reminder reminder,
            ReminderAttempt attempt,
            TemporaryDeliveryException exception
    ) {

        Instant now = clock.now();

        attempt.setCompletedAt(now);
        attempt.setOutcome(AttemptOutcome.TEMPORARY_FAILURE);
        attempt.setErrorType(exception.getClass().getSimpleName());
        attempt.setErrorMessage(exception.getMessage());

        if (retryPolicy.shouldRetry(attempt.getAttemptNumber())) {

            reminder.setStatus(ReminderStatus.SCHEDULED);

        } else {

            reminder.setStatus(ReminderStatus.FAILED);
        }

        reminder.setUpdatedAt(now);

        attemptRepository.save(attempt);
        reminderRepository.save(reminder);
    }

    private void handlePermanentFailure(
            Reminder reminder,
            ReminderAttempt attempt,
            Exception exception
    ) {

        Instant now = clock.now();

        attempt.setCompletedAt(now);
        attempt.setOutcome(AttemptOutcome.PERMANENT_FAILURE);
        attempt.setErrorType(exception.getClass().getSimpleName());
        attempt.setErrorMessage(exception.getMessage());

        reminder.setStatus(ReminderStatus.FAILED);
        reminder.setUpdatedAt(now);

        attemptRepository.save(attempt);
        reminderRepository.save(reminder);
    }
}
