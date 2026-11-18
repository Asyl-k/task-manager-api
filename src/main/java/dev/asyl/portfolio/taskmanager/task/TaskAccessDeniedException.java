package dev.asyl.portfolio.taskmanager.task;

public class TaskAccessDeniedException extends RuntimeException {

    public TaskAccessDeniedException(Long id) {
        super("You do not have access to task %d".formatted(id));
    }
}
