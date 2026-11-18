package dev.asyl.portfolio.taskmanager.task;

import dev.asyl.portfolio.taskmanager.auth.AccountRole;
import dev.asyl.portfolio.taskmanager.auth.AppUser;
import dev.asyl.portfolio.taskmanager.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository tasks;

    @Mock
    UserRepository users;

    @InjectMocks
    TaskService service;

    private final AppUser alice = new AppUser("alice", "hash", AccountRole.USER);
    private final AppUser bob = new AppUser("bob", "hash", AccountRole.USER);

    @Test
    void createTrimsFieldsAndUsesAuthenticatedOwner() {
        when(users.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(tasks.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = service.create("alice", new CreateTaskRequest("  Write tests  ", "  REST layer  "));

        assertThat(response.title()).isEqualTo("Write tests");
        assertThat(response.description()).isEqualTo("REST layer");
        assertThat(response.owner()).isEqualTo("alice");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void userCannotReadAnotherUsersTask() {
        Task task = new Task("Secret", null, bob);
        when(tasks.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> service.get(1L, "alice", false))
                .isInstanceOf(TaskAccessDeniedException.class);
    }

    @Test
    void adminCanReadAnotherUsersTask() {
        Task task = new Task("Secret", null, bob);
        when(tasks.findById(1L)).thenReturn(Optional.of(task));

        assertThat(service.get(1L, "admin", true).owner()).isEqualTo("bob");
    }

    @Test
    void missingTaskProducesNotFound() {
        when(tasks.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L, "alice", false))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void invalidTransitionPropagates() {
        Task task = new Task("Task", null, alice);
        when(tasks.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> service.changeStatus(
                1L,
                "alice",
                false,
                new ChangeTaskStatusRequest(TaskStatus.DONE)
        )).isInstanceOf(InvalidTaskTransitionException.class);
    }

    @Test
    void deleteChecksOwnershipBeforeRemoving() {
        Task task = new Task("Task", null, bob);
        when(tasks.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> service.delete(1L, "alice", false))
                .isInstanceOf(TaskAccessDeniedException.class);
        verify(tasks, never()).delete(any(Task.class));
    }
}
