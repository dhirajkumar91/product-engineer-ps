package com.dhiraj.durable_reminders.controller;

import com.dhiraj.durable_reminders.dto.CreateReminderRequest;
import com.dhiraj.durable_reminders.dto.ReminderResponse;
import com.dhiraj.durable_reminders.dto.UpdateReminderRequest;
import com.dhiraj.durable_reminders.entity.Reminder;
import com.dhiraj.durable_reminders.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {
    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(
            @Valid @RequestBody CreateReminderRequest request
    ) {
        Reminder reminder = reminderService.createReminder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ReminderResponse(reminder));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponse> getReminder(
            @PathVariable UUID id
    ) {
        Reminder reminder = reminderService.getReminder(id);

        return ResponseEntity.ok(new ReminderResponse(reminder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> updateReminder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReminderRequest request
    ) {
        Reminder reminder = reminderService.updateReminder(id, request);

        return ResponseEntity.ok(new ReminderResponse(reminder));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReminder(
            @PathVariable UUID id
    ) {
        reminderService.cancelReminder(id);

        return ResponseEntity.noContent().build();
    }
}
