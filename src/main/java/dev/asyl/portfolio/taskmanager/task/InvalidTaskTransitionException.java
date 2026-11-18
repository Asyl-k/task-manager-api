package dev.asyl.portfolio.taskmanager.task;

public class InvalidTaskTransitionException extends RuntimeException {

    public InvalidTaskTransitionException(Long taskId, TaskStatus from, TaskStatus to) {
        super("Task %s cannot transition from %s to %s".formatted(taskId, from, to));
    }
}
