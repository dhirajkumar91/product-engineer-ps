package com.dhiraj.durable_reminders.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReminderRequest {
    @NotBlank
    @Size(max = 500)
    private String content;

    @NotBlank
    private String scheduledAt;

    @NotBlank
    private String timezone;
}
