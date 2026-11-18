package dev.asyl.portfolio.taskmanager.task;

import dev.asyl.portfolio.taskmanager.auth.AppUser;
import dev.asyl.portfolio.taskmanager.auth.AuthService;
import dev.asyl.portfolio.taskmanager.auth.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository tasks;
    private final UserRepository users;

    public TaskService(TaskRepository tasks, UserRepository users) {
        this.tasks = tasks;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listForUser(String username, TaskStatus status, Pageable pageable) {
        Page<Task> page = status == null
                ? tasks.findByOwnerUsername(username, pageable)
                : tasks.findByOwnerUsernameAndStatus(username, status, pageable);
        return page.map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listAll(TaskStatus status, Pageable pageable) {
        Page<Task> page = status == null
                ? tasks.findAll(pageable)
                : tasks.findByStatus(status, pageable);
        return page.map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id, String username, boolean admin) {
        Task task = requireTask(id);
        checkAccess(task, username, admin);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse create(String username, CreateTaskRequest request) {
        AppUser owner = requireUser(username);
        Task task = new Task(request.title().trim(), normalizeDescription(request.description()), owner);
        return TaskResponse.from(tasks.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, String username, boolean admin, UpdateTaskRequest request) {
        Task task = requireTask(id);
        checkAccess(task, username, admin);
        task.update(request.title().trim(), normalizeDescription(request.description()));
        return TaskResponse.from(tasks.save(task));
    }

    @Transactional
    public TaskResponse changeStatus(Long id, String username, boolean admin, ChangeTaskStatusRequest request) {
        Task task = requireTask(id);
        checkAccess(task, username, admin);
        task.changeStatus(request.status());
        return TaskResponse.from(tasks.save(task));
    }

    @Transactional
    public void delete(Long id, String username, boolean admin) {
        Task task = requireTask(id);
        checkAccess(task, username, admin);
        tasks.delete(task);
    }

    private Task requireTask(Long id) {
        return tasks.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    private AppUser requireUser(String username) {
        return users.findByUsername(username)
                .orElseThrow(AuthService.InvalidCredentialsException::new);
    }

    private void checkAccess(Task task, String username, boolean admin) {
        if (!admin && !task.getOwner().getUsername().equals(username)) {
            throw new TaskAccessDeniedException(task.getId());
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}
