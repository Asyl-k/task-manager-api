package dev.asyl.portfolio.taskmanager.task;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Task %d not found".formatted(id));
    }
}
