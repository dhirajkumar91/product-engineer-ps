package com.dhiraj.durable_reminders.dto;

import com.dhiraj.durable_reminders.entity.Reminder;
import com.dhiraj.durable_reminders.enums.ReminderStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ReminderResponse {
    private UUID id;
    private String content;
    private Instant scheduledAt;
    private String timezone;
    private ReminderStatus status;
    private Integer version;
    private Integer attemptCount;

    public ReminderResponse(Reminder reminder) {
        this.id = reminder.getId();
        this.content = reminder.getContent();
        this.scheduledAt = reminder.getScheduledAt();
        this.timezone = reminder.getTimezone();
        this.status = reminder.getStatus();
        this.version = reminder.getVersion();
        this.attemptCount = reminder.getAttemptCount();
    }
}
