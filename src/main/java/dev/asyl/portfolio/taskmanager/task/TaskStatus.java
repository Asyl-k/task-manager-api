package dev.asyl.portfolio.taskmanager.task;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE;

    public boolean canTransitionTo(TaskStatus target) {
        if (target == null) {
            return false;
        }
        return switch (this) {
            case TODO -> target == IN_PROGRESS;
            case IN_PROGRESS -> target == DONE || target == TODO;
            case DONE -> target == IN_PROGRESS;
        };
    }
}
