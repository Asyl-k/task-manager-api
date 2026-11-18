package dev.asyl.portfolio.taskmanager.task;

import dev.asyl.portfolio.taskmanager.auth.AccountRole;
import dev.asyl.portfolio.taskmanager.auth.AppUser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    private final AppUser owner = new AppUser("alice", "hash", AccountRole.USER);

    @Test
    void newTaskStartsAsTodo() {
        Task task = new Task("Write tests", null, owner);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(task.getOwner()).isSameAs(owner);
    }

    @Test
    void allowsTodoToInProgressToDone() {
        Task task = new Task("Write tests", null, owner);

        task.changeStatus(TaskStatus.IN_PROGRESS);
        task.changeStatus(TaskStatus.DONE);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void allowsReopeningDoneTask() {
        Task task = new Task("Write tests", null, owner);
        task.changeStatus(TaskStatus.IN_PROGRESS);
        task.changeStatus(TaskStatus.DONE);

        task.changeStatus(TaskStatus.IN_PROGRESS);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void rejectsSkippingInProgress() {
        Task task = new Task("Write tests", null, owner);

        assertThatThrownBy(() -> task.changeStatus(TaskStatus.DONE))
                .isInstanceOf(InvalidTaskTransitionException.class)
                .hasMessageContaining("TODO")
                .hasMessageContaining("DONE");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void rejectsNullTarget() {
        Task task = new Task("Write tests", null, owner);

        assertThatThrownBy(() -> task.changeStatus(null))
                .isInstanceOf(InvalidTaskTransitionException.class);
    }
}
