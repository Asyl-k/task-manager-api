package dev.asyl.portfolio.taskmanager.common;

import dev.asyl.portfolio.taskmanager.auth.AuthService;
import dev.asyl.portfolio.taskmanager.task.InvalidTaskTransitionException;
import dev.asyl.portfolio.taskmanager.task.TaskAccessDeniedException;
import dev.asyl.portfolio.taskmanager.task.TaskNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "One or more request fields are invalid",
                "validation_failed"
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                exception.getMessage(),
                "validation_failed"
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Malformed request",
                "The request body is missing or contains invalid JSON",
                "malformed_request"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter",
                "Parameter '%s' has an invalid value".formatted(exception.getName()),
                "invalid_parameter"
        );
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleInvalidSort(PropertyReferenceException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid sort parameter",
                exception.getMessage(),
                "invalid_sort"
        );
    }

    @ExceptionHandler(AuthService.UsernameAlreadyExistsException.class)
    public ProblemDetail handleUsernameConflict(AuthService.UsernameAlreadyExistsException exception) {
        return problem(HttpStatus.CONFLICT, "Username conflict", exception.getMessage(), "username_conflict");
    }

    @ExceptionHandler(AuthService.InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(AuthService.InvalidCredentialsException exception) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", exception.getMessage(), "invalid_credentials");
    }

    @ExceptionHandler(AuthService.InvalidUsernameException.class)
    public ProblemDetail handleInvalidUsername(AuthService.InvalidUsernameException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid username", exception.getMessage(), "invalid_username");
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail handleTaskNotFound(TaskNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Task not found", exception.getMessage(), "task_not_found");
    }

    @ExceptionHandler(TaskAccessDeniedException.class)
    public ProblemDetail handleTaskAccessDenied(TaskAccessDeniedException exception) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", exception.getMessage(), "forbidden");
    }

    @ExceptionHandler(InvalidTaskTransitionException.class)
    public ProblemDetail handleInvalidTransition(InvalidTaskTransitionException exception) {
        return problem(HttpStatus.CONFLICT, "Invalid task transition", exception.getMessage(), "invalid_transition");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        return problem(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission to perform this operation",
                "forbidden"
        );
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(OptimisticLockingFailureException exception) {
        return problem(
                HttpStatus.CONFLICT,
                "Concurrent modification",
                "The task was modified by another request; reload it and try again",
                "optimistic_lock_conflict"
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataConflict(DataIntegrityViolationException exception) {
        log.debug("Data integrity violation", exception);
        return problem(
                HttpStatus.CONFLICT,
                "Data conflict",
                "The operation conflicts with existing data",
                "data_conflict"
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNotFound(NoResourceFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Not found", "The requested resource does not exist", "not_found");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return problem(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method not allowed",
                exception.getMessage(),
                "method_not_allowed"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        log.error("Unhandled exception", exception);
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred",
                "internal_error"
        );
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
