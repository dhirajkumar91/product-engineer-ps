package com.dhiraj.durable_reminders.repository;

import com.dhiraj.durable_reminders.entity.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {
    Optional<NotificationDelivery> findByDeliveryKey(UUID deliveryKey);
    boolean existsByDeliveryKey(UUID deliveryKey);
}
