package com.dhiraj.durable_reminders.time;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class ApplicationClock {

    private Clock clock = Clock.systemUTC();

    public Instant now() {
        return clock.instant();
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void reset() {
        this.clock = Clock.systemUTC();
    }
}