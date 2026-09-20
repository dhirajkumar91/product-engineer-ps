package com.dhiraj.durable_reminders.entity;

import com.dhiraj.durable_reminders.enums.AttemptOutcome;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "reminder_attempts")
public class ReminderAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reminder_id", nullable = false)
    private Reminder reminder;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "delivery_key", nullable = false)
    private UUID deliveryKey;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttemptOutcome outcome;

    @Column(name = "error_type", length = 100)
    private String errorType;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;
}
