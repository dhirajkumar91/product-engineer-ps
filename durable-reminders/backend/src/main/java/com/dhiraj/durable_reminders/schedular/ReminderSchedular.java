package com.dhiraj.durable_reminders.schedular;

import com.dhiraj.durable_reminders.entity.Reminder;
import com.dhiraj.durable_reminders.enums.ReminderStatus;
import com.dhiraj.durable_reminders.repository.ReminderRepository;
import com.dhiraj.durable_reminders.service.ReminderExecutionService;
import com.dhiraj.durable_reminders.time.ApplicationClock;
import jakarta.transaction.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ReminderSchedular {

    private final ReminderRepository reminderRepository;
    private final ReminderExecutionService executionService;
    private final ApplicationClock clock;

    public ReminderSchedular(
            ReminderRepository reminderRepository,
            ReminderExecutionService executionService,
            ApplicationClock clock
    ) {
        this.reminderRepository = reminderRepository;
        this.executionService = executionService;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void recoverRunningReminders() {

        int recovered = reminderRepository.recoverRunningReminders(
                clock.now()
        );

        if (recovered > 0) {
            System.out.println(
                    "Recovered " + recovered + " running reminder(s)"
            );
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void processDueReminders() {

        Instant now = clock.now();

        List<Reminder> dueReminders =
                reminderRepository.findByStatusAndScheduledAtLessThanEqual(
                        ReminderStatus.SCHEDULED,
                        now
                );

        for (Reminder reminder : dueReminders) {

            executionService.executeReminder(
                    reminder.getId()
            );
        }
    }
}