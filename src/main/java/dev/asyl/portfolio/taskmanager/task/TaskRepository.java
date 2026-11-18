package dev.asyl.portfolio.taskmanager.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = "owner")
    Page<Task> findByOwnerUsername(String username, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    Page<Task> findByOwnerUsernameAndStatus(String username, TaskStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "owner")
    Page<Task> findAll(Pageable pageable);
}
