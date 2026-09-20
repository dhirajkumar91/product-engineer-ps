package com.dhiraj.durable_reminders.time;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeServiceTest {

    private final TimeService timeService =
            new TimeService();

    @Test
    void convertsIndiaTimeToUtc() {

        Instant result =
                timeService.toInstant(
                        "2026-09-21T00:30",
                        "Asia/Kolkata"
                );

        assertEquals(
                Instant.parse("2026-09-20T19:00:00Z"),
                result
        );
    }

    @Test
    void handlesNewYorkSpringForward() {

        /*
         * 2026-03-08 02:30 does not exist in
         * America/New_York because the clock jumps
         * from 02:00 to 03:00.
         *
         * Our policy moves it to the next valid
         * local time: 03:00.
         */
        Instant result =
                timeService.toInstant(
                        "2026-03-08T02:30",
                        "America/New_York"
                );

        assertEquals(
                Instant.parse("2026-03-08T07:00:00Z"),
                result
        );
    }

    @Test
    void handlesNewYorkFallBack() {

        /*
         * 2026-11-01 01:30 occurs twice in
         * America/New_York.
         *
         * Our policy chooses the earlier offset.
         */
        Instant result =
                timeService.toInstant(
                        "2026-11-01T01:30",
                        "America/New_York"
                );

        assertEquals(
                Instant.parse("2026-11-01T05:30:00Z"),
                result
        );
    }
}