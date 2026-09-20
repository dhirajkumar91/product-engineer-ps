package com.dhiraj.durable_reminders.time;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;

@Service
public class TimeService {

    public Instant toInstant(String localDateTime, String timezone) {
        LocalDateTime local = LocalDateTime.parse(localDateTime);
        ZoneId zone = ZoneId.of(timezone);

        ZoneRules rules = zone.getRules();

        // Normal or ambiguous local time
        if (!rules.getValidOffsets(local).isEmpty()) {
            // If ambiguous, choose the earlier offset.
            ZoneOffset offset = rules.getValidOffsets(local).get(0);
            return local.toInstant(offset);
        }

        // Non-existent local time during a DST spring-forward transition.
        ZoneOffsetTransition transition = rules.getTransition(local);

        return transition.getDateTimeAfter()
                .atZone(zone)
                .toInstant();
    }
}
