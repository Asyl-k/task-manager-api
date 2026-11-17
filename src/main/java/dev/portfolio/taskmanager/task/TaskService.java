package dev.portfolio.taskmanager.task;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Task> findAll(TaskStatus status) {
        return status == null ? repository.findAll() : repository.findAllByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return repository.findById(id).orElseThrow(() -> notFound(id));
    }

    public Task create(TaskRequest request) {
        return repository.save(new Task(request.title().trim(), request.description(), request.status()));
    }

    public Task update(Long id, TaskRequest request) {
        Task task = findById(id);
        task.update(request.title().trim(), request.description(),
                request.status() == null ? TaskStatus.TODO : request.status());
        return repository.save(task);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) throw notFound(id);
        repository.deleteById(id);
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Task %d not found".formatted(id));
    }
}
