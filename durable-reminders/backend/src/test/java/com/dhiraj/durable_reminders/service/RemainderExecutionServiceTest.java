package com.dhiraj.durable_reminders.service;

import com.dhiraj.durable_reminders.entity.Reminder;
import com.dhiraj.durable_reminders.entity.ReminderAttempt;
import com.dhiraj.durable_reminders.enums.AttemptOutcome;
import com.dhiraj.durable_reminders.enums.ReminderStatus;
import com.dhiraj.durable_reminders.exception.TemporaryDeliveryException;
import com.dhiraj.durable_reminders.notification.NotificationDestination;
import com.dhiraj.durable_reminders.repository.ReminderAttemptRepository;
import com.dhiraj.durable_reminders.repository.ReminderRepository;
import com.dhiraj.durable_reminders.time.ApplicationClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReminderExecutionServiceTest {

    private ReminderRepository reminderRepository;
    private ReminderAttemptRepository attemptRepository;
    private NotificationDestination notificationDestination;
    private ApplicationClock clock;
    private RetryPolicy retryPolicy;

    private ReminderExecutionService executionService;

    private Reminder reminder;

    private final Instant now =
            Instant.parse("2026-09-20T18:00:00Z");

    @BeforeEach
    void setUp() {

        reminderRepository = mock(ReminderRepository.class);
        attemptRepository = mock(ReminderAttemptRepository.class);
        notificationDestination = mock(NotificationDestination.class);

        clock = new ApplicationClock();
        retryPolicy = new RetryPolicy();

        clock.setClock(
                Clock.fixed(
                        now,
                        ZoneOffset.UTC
                )
        );

        executionService = new ReminderExecutionService(
                reminderRepository,
                attemptRepository,
                notificationDestination,
                clock,
                retryPolicy
        );

        reminder = new Reminder();

        reminder.setId(UUID.randomUUID());
        reminder.setContent("Test reminder");

        reminder.setScheduledAt(
                now.minusSeconds(10)
        );

        reminder.setStatus(ReminderStatus.SCHEDULED);
        reminder.setVersion(1);
        reminder.setDeliveryKey(UUID.randomUUID());
        reminder.setAttemptCount(0);

        when(
                reminderRepository.findById(
                        reminder.getId()
                )
        ).thenReturn(
                Optional.of(reminder)
        );

        when(
                reminderRepository.claimReminder(
                        reminder.getId(),
                        now
                )
        ).thenAnswer(invocation -> {

            if (reminder.getStatus()
                    != ReminderStatus.SCHEDULED) {

                return 0;
            }

            reminder.setStatus(
                    ReminderStatus.RUNNING
            );

            return 1;
        });
    }

    @Test
    void temporaryFailureThenSuccess() {

        /*
         * First delivery attempt:
         * TEMPORARY_FAILURE
         *
         * Second delivery attempt:
         * SUCCESS
         */
        doThrow(
                new TemporaryDeliveryException(
                        "Temporary provider failure"
                )
        )
                .doNothing()
                .when(notificationDestination)
                .deliver(
                        any(UUID.class),
                        any(String.class)
                );

        // -------------------------
        // First execution
        // -------------------------

        boolean firstResult =
                executionService.executeReminder(
                        reminder.getId()
                );

        assertFalse(firstResult);

        assertEquals(
                ReminderStatus.SCHEDULED,
                reminder.getStatus()
        );

        assertEquals(
                1,
                reminder.getAttemptCount()
        );

        // -------------------------
        // Second execution
        // -------------------------

        boolean secondResult =
                executionService.executeReminder(
                        reminder.getId()
                );

        assertTrue(secondResult);

        assertEquals(
                ReminderStatus.DELIVERED,
                reminder.getStatus()
        );

        assertEquals(
                2,
                reminder.getAttemptCount()
        );

        // -------------------------
        // Verify notification calls
        // -------------------------

        verify(
                notificationDestination,
                times(2)
        ).deliver(
                reminder.getDeliveryKey(),
                reminder.getContent()
        );

        // -------------------------
        // Verify attempt history
        // -------------------------

        ArgumentCaptor<ReminderAttempt> attemptCaptor =
                ArgumentCaptor.forClass(
                        ReminderAttempt.class
                );

        verify(
                attemptRepository,
                times(4)
        ).save(
                attemptCaptor.capture()
        );

        var savedAttempts =
                attemptCaptor.getAllValues();

        /*
         * Each attempt is saved twice:
         *
         * Attempt 1:
         *   save when started
         *   save when completed
         *
         * Attempt 2:
         *   save when started
         *   save when completed
         */

        ReminderAttempt firstAttempt =
                savedAttempts.get(1);

        ReminderAttempt secondAttempt =
                savedAttempts.get(3);

        assertEquals(
                1,
                firstAttempt.getAttemptNumber()
        );

        assertEquals(
                AttemptOutcome.TEMPORARY_FAILURE,
                firstAttempt.getOutcome()
        );

        assertEquals(
                2,
                secondAttempt.getAttemptNumber()
        );

        assertEquals(
                AttemptOutcome.SUCCESS,
                secondAttempt.getOutcome()
        );

        // -------------------------
        // Verify reminder persistence
        // -------------------------

        verify(
                reminderRepository,
                atLeast(2)
        ).save(reminder);
    }

    @Test
    void threeTemporaryFailuresResultInTerminalFailure() {

        /*
         * Every delivery attempt will temporarily fail.
         */
        doThrow(
                new TemporaryDeliveryException(
                        "Temporary provider failure"
                )
        )
                .when(notificationDestination)
                .deliver(
                        any(UUID.class),
                        any(String.class)
                );

        // -------------------------
        // Attempt 1
        // -------------------------

        boolean firstResult =
                executionService.executeReminder(
                        reminder.getId()
                );

        assertFalse(firstResult);

        assertEquals(
                ReminderStatus.SCHEDULED,
                reminder.getStatus()
        );

        assertEquals(
                1,
                reminder.getAttemptCount()
        );

        // -------------------------
        // Attempt 2
        // -------------------------

        boolean secondResult =
                executionService.executeReminder(
                        reminder.getId()
                );

        assertFalse(secondResult);

        assertEquals(
                ReminderStatus.SCHEDULED,
                reminder.getStatus()
        );

        assertEquals(
                2,
                reminder.getAttemptCount()
        );

        // -------------------------
        // Attempt 3
        // -------------------------

        boolean thirdResult =
                executionService.executeReminder(
                        reminder.getId()
                );

        assertFalse(thirdResult);

        /*
         * Maximum attempts reached.
         * Reminder must now be permanently FAILED.
         */
        assertEquals(
                ReminderStatus.FAILED,
                reminder.getStatus()
        );

        assertEquals(
                3,
                reminder.getAttemptCount()
        );

        // -------------------------
        // Verify delivery attempts
        // -------------------------

        verify(
                notificationDestination,
                times(3)
        ).deliver(
                reminder.getDeliveryKey(),
                reminder.getContent()
        );
    }

    @Test
    void permanentFailureImmediatelyFailsReminder() {

        doThrow(
                new com.dhiraj.durable_reminders.exception.PermanentDeliveryException(
                        "Permanent provider failure"
                )
        )
                .when(notificationDestination)
                .deliver(
                        any(UUID.class),
                        any(String.class)
                );

        boolean result =
                executionService.executeReminder(
                        reminder.getId()
                );

        assertFalse(result);

        assertEquals(
                ReminderStatus.FAILED,
                reminder.getStatus()
        );

        assertEquals(
                1,
                reminder.getAttemptCount()
        );

        verify(
                notificationDestination,
                times(1)
        ).deliver(
                reminder.getDeliveryKey(),
                reminder.getContent()
        );
    }

    @Test
    void cancelledReminderCannotExecute() {

        reminder.setStatus(
                ReminderStatus.CANCELLED
        );

        boolean result =
                executionService.executeReminder(
                        reminder.getId()
                );

        assertFalse(result);

        assertEquals(
                ReminderStatus.CANCELLED,
                reminder.getStatus()
        );

        assertEquals(
                0,
                reminder.getAttemptCount()
        );

        verify(
                notificationDestination,
                never()
        ).deliver(
                any(UUID.class),
                any(String.class)
        );
    }
}