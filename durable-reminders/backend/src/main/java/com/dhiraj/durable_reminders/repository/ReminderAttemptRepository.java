package com.dhiraj.durable_reminders.repository;

import com.dhiraj.durable_reminders.entity.Reminder;
import com.dhiraj.durable_reminders.entity.ReminderAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReminderAttemptRepository extends JpaRepository<ReminderAttempt, UUID> {
    List<ReminderAttempt> findByReminderIdOrderByAttemptNumberAsc(
            UUID reminderId
    );
}
