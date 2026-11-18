package dev.asyl.portfolio.taskmanager.task;

import jakarta.validation.constraints.NotNull;

public record ChangeTaskStatusRequest(@NotNull TaskStatus status) {
}
