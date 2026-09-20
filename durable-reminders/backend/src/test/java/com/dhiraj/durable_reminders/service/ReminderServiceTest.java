package com.dhiraj.durable_reminders.service;

import com.dhiraj.durable_reminders.dto.UpdateReminderRequest;
import com.dhiraj.durable_reminders.entity.Reminder;
import com.dhiraj.durable_reminders.enums.ReminderStatus;
import com.dhiraj.durable_reminders.repository.ReminderRepository;
import com.dhiraj.durable_reminders.time.ApplicationClock;
import com.dhiraj.durable_reminders.time.TimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReminderServiceTest {

    private ReminderRepository reminderRepository;
    private TimeService timeService;
    private ApplicationClock clock;

    private ReminderService reminderService;

    private Reminder reminder;

    private final Instant now =
            Instant.parse("2026-09-20T18:00:00Z");

    @BeforeEach
    void setUp() {

        reminderRepository =
                mock(ReminderRepository.class);

        timeService = new TimeService();

        clock = new ApplicationClock();

        clock.setClock(
                Clock.fixed(
                        now,
                        ZoneOffset.UTC
                )
        );

        reminderService = new ReminderService(
                reminderRepository,
                timeService,
                clock
        );

        reminder = new Reminder();

        reminder.setId(UUID.randomUUID());
        reminder.setContent("Old content");

        reminder.setScheduledAt(
                now.plusSeconds(600)
        );

        reminder.setTimezone("Asia/Kolkata");
        reminder.setStatus(ReminderStatus.SCHEDULED);
        reminder.setVersion(1);

        UUID oldDeliveryKey = UUID.randomUUID();

        reminder.setDeliveryKey(oldDeliveryKey);
        reminder.setAttemptCount(0);
        reminder.setCreatedAt(now);
        reminder.setUpdatedAt(now);

        when(
                reminderRepository.findById(
                        reminder.getId()
                )
        ).thenReturn(
                Optional.of(reminder)
        );

        when(
                reminderRepository.save(reminder)
        ).thenReturn(reminder);
    }

    @Test
    void updateChangesVersionAndDeliveryKey() {

        UUID oldDeliveryKey =
                reminder.getDeliveryKey();

        UpdateReminderRequest request =
                new UpdateReminderRequest();

        request.setContent(
                "Updated content"
        );

        request.setScheduledAt(
                "2026-09-21T00:30"
        );

        request.setTimezone(
                "Asia/Kolkata"
        );

        Reminder updated =
                reminderService.updateReminder(
                        reminder.getId(),
                        request
                );

        assertEquals(
                "Updated content",
                updated.getContent()
        );

        assertEquals(
                2,
                updated.getVersion()
        );

        assertNotEquals(
                oldDeliveryKey,
                updated.getDeliveryKey()
        );

        assertEquals(
                ReminderStatus.SCHEDULED,
                updated.getStatus()
        );

        assertEquals(
                0,
                updated.getAttemptCount()
        );

        verify(
                reminderRepository,
                times(1)
        ).save(reminder);
    }

    @Test
    void cancelScheduledReminderChangesStatusToCancelled() {

        reminderService.cancelReminder(
                reminder.getId()
        );

        assertEquals(
                ReminderStatus.CANCELLED,
                reminder.getStatus()
        );

        verify(
                reminderRepository,
                times(1)
        ).save(reminder);
    }

    @Test
    void cancelledReminderCannotBeEdited() {

        reminder.setStatus(
                ReminderStatus.CANCELLED
        );

        UpdateReminderRequest request =
                new UpdateReminderRequest();

        request.setContent(
                "Updated content"
        );

        request.setScheduledAt(
                "2026-09-21T00:30"
        );

        request.setTimezone(
                "Asia/Kolkata"
        );

        assertThrows(
                IllegalStateException.class,
                () -> reminderService.updateReminder(
                        reminder.getId(),
                        request
                )
        );

        verify(
                reminderRepository,
                never()
        ).save(reminder);
    }

    @Test
    void deliveredReminderCannotBeCancelled() {

        reminder.setStatus(
                ReminderStatus.DELIVERED
        );

        assertThrows(
                IllegalStateException.class,
                () -> reminderService.cancelReminder(
                        reminder.getId()
                )
        );

        verify(
                reminderRepository,
                never()
        ).save(reminder);
    }
}