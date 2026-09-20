package com.dhiraj.durable_reminders.benchmark;

import com.dhiraj.durable_reminders.entity.Reminder;
import com.dhiraj.durable_reminders.enums.ReminderStatus;
import com.dhiraj.durable_reminders.notification.FakeNotificationDestination;
import com.dhiraj.durable_reminders.repository.NotificationDeliveryRepository;
import com.dhiraj.durable_reminders.repository.ReminderAttemptRepository;
import com.dhiraj.durable_reminders.repository.ReminderRepository;
import com.dhiraj.durable_reminders.service.ReminderExecutionService;
import com.dhiraj.durable_reminders.time.ApplicationClock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ReminderBenchmarkTest {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private ReminderAttemptRepository attemptRepository;

    @Autowired
    private NotificationDeliveryRepository deliveryRepository;

    @Autowired
    private ReminderExecutionService executionService;

    @Autowired
    private ApplicationClock clock;

    @Test
    void benchmarkTwentyReminders() {

        List<UUID> reminderIds = new ArrayList<>();

        /*
         * Create 20 already-due reminders
         * across two IANA timezones.
         */
        for (int i = 0; i < 20; i++) {

            Reminder reminder = new Reminder();

            reminder.setContent(
                    "Benchmark reminder " + i
            );

            reminder.setScheduledAt(
                    clock.now().minusSeconds(60)
            );

            reminder.setTimezone(
                    i % 2 == 0
                            ? "Asia/Kolkata"
                            : "America/New_York"
            );

            reminder.setStatus(
                    ReminderStatus.SCHEDULED
            );

            reminder.setVersion(1);
            reminder.setDeliveryKey(UUID.randomUUID());
            reminder.setAttemptCount(0);
            reminder.setCreatedAt(clock.now());
            reminder.setUpdatedAt(clock.now());

            Reminder saved =
                    reminderRepository.save(reminder);

            reminderIds.add(saved.getId());
        }

        long start = System.nanoTime();

        /*
         * Execute all 20 reminders.
         */
        for (UUID id : reminderIds) {
            executionService.executeReminder(id);
        }

        long elapsed =
                System.nanoTime() - start;

        /*
         * Verify every reminder reached DELIVERED.
         */
        long delivered =
                reminderIds.stream()
                        .map(reminderRepository::findById)
                        .filter(java.util.Optional::isPresent)
                        .map(java.util.Optional::get)
                        .filter(r ->
                                r.getStatus()
                                        == ReminderStatus.DELIVERED
                        )
                        .count();

        long attempts =
                reminderIds.stream()
                        .mapToLong(id ->
                                attemptRepository
                                        .findByReminderIdOrderByAttemptNumberAsc(
                                                id
                                        )
                                        .size()
                        )
                        .sum();

        long deliveries =
                reminderIds.stream()
                        .map(reminderRepository::findById)
                        .filter(java.util.Optional::isPresent)
                        .map(java.util.Optional::get)
                        .mapToLong(r ->
                                deliveryRepository
                                        .findByDeliveryKey(
                                                r.getDeliveryKey()
                                        )
                                        .isPresent()
                                        ? 1
                                        : 0
                        )
                        .sum();

        System.out.println();
        System.out.println(
                "========== REMINDER BENCHMARK =========="
        );

        System.out.println(
                "Reminders          : 20"
        );

        System.out.println(
                "Timezones          : Asia/Kolkata, America/New_York"
        );

        System.out.println(
                "Delivered          : " + delivered
        );

        System.out.println(
                "Attempt records    : " + attempts
        );

        System.out.println(
                "Logical deliveries : " + deliveries
        );

        System.out.println(
                "Execution time     : "
                        + (elapsed / 1_000_000)
                        + " ms"
        );

        System.out.println(
                "=========================================="
        );

        assertTrue(
                delivered == 20,
                "All 20 reminders should be delivered"
        );

        assertTrue(
                deliveries == 20,
                "Exactly one logical delivery per reminder"
        );

        assertTrue(
                attempts == 20,
                "Each successful reminder should have one attempt"
        );
    }
}