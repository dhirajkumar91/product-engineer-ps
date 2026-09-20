package com.dhiraj.durable_reminders.entity;

import com.dhiraj.durable_reminders.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notification_deliveries", uniqueConstraints = {
        @UniqueConstraint(name = "uk_delivery_key", columnNames = "delivery_key")
})
public class NotificationDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "delivery_key", nullable = false)
    private UUID deliveryKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "delivered_at", nullable = false)
    private Instant deliveredAt;
}
