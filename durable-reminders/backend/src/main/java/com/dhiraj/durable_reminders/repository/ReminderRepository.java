package com.dhiraj.durable_reminders.repository;

import com.dhiraj.durable_reminders.entity.Reminder;
import com.dhiraj.durable_reminders.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReminderRepository
        extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByStatusAndScheduledAtLessThanEqual(
            ReminderStatus status,
            Instant scheduledAt
    );

    @Modifying
    @Query("""
        UPDATE Reminder r
        SET r.status = com.dhiraj.durable_reminders.enums.ReminderStatus.RUNNING,
            r.updatedAt = :now
        WHERE r.id = :id
          AND r.status = com.dhiraj.durable_reminders.enums.ReminderStatus.SCHEDULED
          AND r.scheduledAt <= :now
    """)
    int claimReminder(
            @Param("id") UUID id,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
        UPDATE Reminder r
        SET r.status = com.dhiraj.durable_reminders.enums.ReminderStatus.SCHEDULED,
            r.updatedAt = :now
        WHERE r.status = com.dhiraj.durable_reminders.enums.ReminderStatus.RUNNING
    """)
    int recoverRunningReminders(
            @Param("now") Instant now
    );
}