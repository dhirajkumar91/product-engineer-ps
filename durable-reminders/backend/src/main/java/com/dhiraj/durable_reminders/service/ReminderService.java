package com.dhiraj.durable_reminders.service;

import com.dhiraj.durable_reminders.dto.CreateReminderRequest;
import com.dhiraj.durable_reminders.dto.UpdateReminderRequest;
import com.dhiraj.durable_reminders.entity.Reminder;
import com.dhiraj.durable_reminders.enums.ReminderStatus;
import com.dhiraj.durable_reminders.repository.ReminderRepository;
import com.dhiraj.durable_reminders.time.ApplicationClock;
import com.dhiraj.durable_reminders.time.TimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ReminderService {
    private final ReminderRepository reminderRepository;
    private final TimeService timeService;
    private final ApplicationClock clock;

    public ReminderService(ReminderRepository reminderRepository, TimeService timeService, ApplicationClock clock) {
        this.reminderRepository = reminderRepository;
        this.timeService = timeService;
        this.clock = clock;
    }

    @Transactional
    public Reminder createReminder(CreateReminderRequest request) {
        Instant scheduledAt = timeService.toInstant(
                request.getScheduledAt(),
                request.getTimezone()
        );

        Instant now = clock.now();
        if (!scheduledAt.isAfter(now)) {
            throw new IllegalArgumentException(
                    "Reminder time must be in the future"
            );
        }
        Reminder reminder = new Reminder();

        reminder.setContent(request.getContent());
        reminder.setScheduledAt(scheduledAt);
        reminder.setTimezone(request.getTimezone());

        reminder.setStatus(ReminderStatus.SCHEDULED);
        reminder.setVersion(1);
        reminder.setDeliveryKey(UUID.randomUUID());
        reminder.setAttemptCount(0);

        reminder.setCreatedAt(now);
        reminder.setUpdatedAt(now);

        return reminderRepository.save(reminder);
    }


    @Transactional(readOnly = true)
    public Reminder getReminder(UUID id) {

        return reminderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reminder not found: " + id
                        )
                );
    }

    @Transactional
    public Reminder updateReminder(
            UUID id,
            UpdateReminderRequest request
    ) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reminder not found: " + id
                        )
                );

        if (reminder.getStatus() != ReminderStatus.SCHEDULED) {
            throw new IllegalStateException(
                    "Only scheduled reminders can be edited"
            );
        }

        Instant scheduledAt = timeService.toInstant(
                request.getScheduledAt(),
                request.getTimezone()
        );

        Instant now = clock.now();

        if (!scheduledAt.isAfter(now)) {
            throw new IllegalArgumentException(
                    "Reminder time must be in the future"
            );
        }

        reminder.setContent(request.getContent());
        reminder.setScheduledAt(scheduledAt);
        reminder.setTimezone(request.getTimezone());

        reminder.setVersion(reminder.getVersion() + 1);

        /*
         * Important:
         * Editing creates a new logical delivery occurrence.
         */
        reminder.setDeliveryKey(UUID.randomUUID());

        reminder.setAttemptCount(0);
        reminder.setUpdatedAt(now);

        return reminderRepository.save(reminder);
    }

    @Transactional
    public void cancelReminder(UUID id) {

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reminder not found: " + id
                        )
                );

        if (reminder.getStatus() != ReminderStatus.SCHEDULED) {
            throw new IllegalStateException(
                    "Only scheduled reminders can be cancelled"
            );
        }

        reminder.setStatus(ReminderStatus.CANCELLED);
        reminder.setUpdatedAt(clock.now());

        reminderRepository.save(reminder);
    }
}
