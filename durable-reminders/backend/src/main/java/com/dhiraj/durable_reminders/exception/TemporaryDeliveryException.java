package com.dhiraj.durable_reminders.exception;

public class TemporaryDeliveryException extends RuntimeException {
    public TemporaryDeliveryException(String message) {
        super(message);
    }
}
