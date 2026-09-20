package com.dhiraj.durable_reminders.notification;

import java.util.UUID;

public interface NotificationDestination {
    void deliver(
            UUID deliveryKey,
            String content
    );
}
