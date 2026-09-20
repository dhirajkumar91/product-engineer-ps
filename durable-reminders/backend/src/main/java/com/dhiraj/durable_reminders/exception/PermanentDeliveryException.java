package com.dhiraj.durable_reminders.exception;

public class PermanentDeliveryException extends RuntimeException {
    public PermanentDeliveryException(String message) {
        super(message);
    }
}
