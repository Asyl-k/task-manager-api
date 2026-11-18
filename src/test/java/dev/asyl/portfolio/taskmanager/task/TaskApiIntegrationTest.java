package dev.asyl.portfolio.taskmanager.task;

import dev.asyl.portfolio.taskmanager.AbstractIntegrationTest;
import dev.asyl.portfolio.taskmanager.auth.AccountRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
class TaskApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void userCanCreateAndReadOwnTask() throws Exception {
        String token = register(unique("alice"), "password123");

        long taskId = createTask(token, "Write integration tests");

        mvc.perform(get("/api/tasks/{id}", taskId).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Write integration tests"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void userCannotReadAnotherUsersTask() throws Exception {
        String alice = register(unique("alice"), "password123");
        String bob = register(unique("bob"), "password123");
        long taskId = createTask(alice, "Private task");

        mvc.perform(get("/api/tasks/{id}", taskId).header(HttpHeaders.AUTHORIZATION, bearer(bob)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));

        mvc.perform(get("/api/tasks").header(HttpHeaders.AUTHORIZATION, bearer(bob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void listIsPaginated() throws Exception {
        String token = register(unique("alice"), "password123");
        createTask(token, "Task 1");
        createTask(token, "Task 2");
        createTask(token, "Task 3");

        mvc.perform(get("/api/tasks?page=0&size=2")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        mvc.perform(get("/api/tasks?page=1&size=2")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void statusTransitionsAreEnforced() throws Exception {
        String token = register(unique("alice"), "password123");
        long taskId = createTask(token, "Move me");

        mvc.perform(patch("/api/tasks/{id}/status", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("invalid_transition"));

        changeStatus(token, taskId, "IN_PROGRESS", 200);
        changeStatus(token, taskId, "DONE", 200);
        changeStatus(token, taskId, "IN_PROGRESS", 200);
    }

    @Test
    void adminCanListAllTasksButRegularUserCannot() throws Exception {
        String alice = register(unique("alice"), "password123");
        createTask(alice, "Visible to admin");

        String adminName = unique("admin");
        createUser(adminName, "password123", AccountRole.ADMIN);
        String admin = login(adminName, "password123");

        mvc.perform(get("/api/admin/tasks").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").isNumber());

        mvc.perform(get("/api/admin/tasks").header(HttpHeaders.AUTHORIZATION, bearer(alice)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));
    }

    private long createTask(String token, String title) throws Exception {
        String response = mvc.perform(post("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", title,
                                "description", "Created by an integration test"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void changeStatus(String token, long taskId, String statusValue, int expectedStatus) throws Exception {
        mvc.perform(patch("/api/tasks/{id}/status", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", statusValue))))
                .andExpect(status().is(expectedStatus));
    }

    private String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
