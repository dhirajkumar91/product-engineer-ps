package com.dhiraj.durable_reminders.notification;

import com.dhiraj.durable_reminders.entity.NotificationDelivery;
import com.dhiraj.durable_reminders.enums.DeliveryStatus;
import com.dhiraj.durable_reminders.repository.NotificationDeliveryRepository;
import com.dhiraj.durable_reminders.time.ApplicationClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FakeNotificationDestinationTest {

    private NotificationDeliveryRepository deliveryRepository;
    private ApplicationClock clock;

    private FakeNotificationDestination destination;

    private final Instant now =
            Instant.parse("2026-09-20T18:00:00Z");

    @BeforeEach
    void setUp() {

        deliveryRepository =
                mock(NotificationDeliveryRepository.class);

        clock = new ApplicationClock();

        clock.setClock(
                Clock.fixed(
                        now,
                        ZoneOffset.UTC
                )
        );

        destination = new FakeNotificationDestination(
                deliveryRepository,
                clock
        );
    }

    @Test
    void duplicateDeliveryKeyIsIgnored() {

        UUID deliveryKey = UUID.randomUUID();

        /*
         * First call:
         * delivery does not exist yet.
         */
        when(
                deliveryRepository.existsByDeliveryKey(
                        deliveryKey
                )
        ).thenReturn(false);

        destination.deliver(
                deliveryKey,
                "Test notification"
        );

        /*
         * Verify that the delivery was persisted.
         */
        verify(
                deliveryRepository,
                times(1)
        ).save(
                any(NotificationDelivery.class)
        );

        /*
         * Second call:
         * same logical delivery key already exists.
         */
        when(
                deliveryRepository.existsByDeliveryKey(
                        deliveryKey
                )
        ).thenReturn(true);

        destination.deliver(
                deliveryKey,
                "Test notification"
        );

        /*
         * Still only one save.
         *
         * The second delivery must not create
         * another logical notification record.
         */
        verify(
                deliveryRepository,
                times(1)
        ).save(
                any(NotificationDelivery.class)
        );
    }
}