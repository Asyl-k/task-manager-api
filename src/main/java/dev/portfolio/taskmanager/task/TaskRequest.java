package dev.portfolio.taskmanager.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
    @NotBlank @Size(max = 160) String title,
    @Size(max = 2000) String description,
    TaskStatus status
) {}
