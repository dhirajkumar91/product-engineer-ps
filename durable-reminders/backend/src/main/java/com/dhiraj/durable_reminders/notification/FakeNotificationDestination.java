package com.dhiraj.durable_reminders.notification;

import com.dhiraj.durable_reminders.entity.NotificationDelivery;
import com.dhiraj.durable_reminders.enums.DeliveryStatus;
import com.dhiraj.durable_reminders.exception.PermanentDeliveryException;
import com.dhiraj.durable_reminders.exception.TemporaryDeliveryException;
import com.dhiraj.durable_reminders.repository.NotificationDeliveryRepository;
import com.dhiraj.durable_reminders.time.ApplicationClock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class FakeNotificationDestination
        implements NotificationDestination {

    private final NotificationDeliveryRepository deliveryRepository;
    private final ApplicationClock clock;

    private int temporaryFailuresRemaining = 0;
    private boolean permanentFailure = false;

    public FakeNotificationDestination(
            NotificationDeliveryRepository deliveryRepository,
            ApplicationClock clock
    ) {
        this.deliveryRepository = deliveryRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void deliver(UUID deliveryKey, String content) {

        /*
         * Durable idempotency check.
         */
        if (deliveryRepository.existsByDeliveryKey(deliveryKey)) {
            System.out.println(
                    "DUPLICATE DELIVERY IGNORED: " + deliveryKey
            );
            return;
        }

        /*
         * Simulate a permanent provider failure.
         */
        if (permanentFailure) {
            throw new PermanentDeliveryException(
                    "Simulated permanent delivery failure"
            );
        }

        /*
         * Simulate temporary provider failures.
         */
        if (temporaryFailuresRemaining > 0) {

            temporaryFailuresRemaining--;

            throw new TemporaryDeliveryException(
                    "Simulated temporary delivery failure"
            );
        }

        /*
         * Successful delivery.
         */
        System.out.println(
                "NOTIFICATION DELIVERED: " + content
        );

        NotificationDelivery delivery =
                new NotificationDelivery();

        delivery.setDeliveryKey(deliveryKey);
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(clock.now());

        deliveryRepository.save(delivery);
    }

    public void failNextTemporarily(int numberOfFailures) {
        this.temporaryFailuresRemaining = numberOfFailures;
        this.permanentFailure = false;
    }

    public void failPermanently() {
        this.permanentFailure = true;
        this.temporaryFailuresRemaining = 0;
    }

    public void clearFailures() {
        this.temporaryFailuresRemaining = 0;
        this.permanentFailure = false;
    }
}