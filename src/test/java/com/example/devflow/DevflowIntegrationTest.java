package com.example.devflow;

import com.example.devflow.dto.request.CreateProjectRequest;
import com.example.devflow.dto.request.CreateTaskRequest;
import com.example.devflow.dto.request.LoginRequest;
import com.example.devflow.dto.request.RegisterRequest;
import com.example.devflow.dto.request.UpdateTaskStatusRequest;
import com.example.devflow.dto.response.ApiResponse;
import com.example.devflow.dto.response.AuthResponse;
import com.example.devflow.dto.response.ProjectResponse;
import com.example.devflow.dto.response.TaskResponse;
import com.example.devflow.model.TaskPriority;
import com.example.devflow.model.TaskStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test for DevFlow API.
 * <p>
 * Uses Testcontainers to spin up a real PostgreSQL 15 instance, ensuring
 * the test environment matches production. Tests cover the complete flow:
 * register → login → create project → create task → update status →
 * verify activity log is created automatically by AOP.
 * <p>
 * Uses TestRestTemplate for real HTTP calls (not mock MVC), which exercises
 * the full Spring Boot application context including Security, JPA, and AOP.
 * <p>
 * Paginated responses use {@code Map<String, Object>} instead of {@code Page<T>}
 * because Spring Boot's VIA_DTO serialization mode produces a custom DTO format
 * that Jackson cannot deserialize into the Page interface directly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Testcontainers
class DevflowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("devflow_test")
            .withUsername("test")
            .withPassword("test");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
        System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders authHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    /** POST /api/auth/register or /api/auth/login — no auth required. */
    private ResponseEntity<ApiResponse<AuthResponse>> postAuth(String path, Object body) {
        return restTemplate.exchange(
                url(path), HttpMethod.POST, new HttpEntity<>(body),
                new ParameterizedTypeReference<ApiResponse<AuthResponse>>() {});
    }

    /** POST with Bearer token, expecting ProjectResponse. */
    private ResponseEntity<ApiResponse<ProjectResponse>> postProject(String path, Object body, String token) {
        return restTemplate.exchange(
                url(path), HttpMethod.POST, new HttpEntity<>(body, authHeader(token)),
                new ParameterizedTypeReference<ApiResponse<ProjectResponse>>() {});
    }

    /** POST with Bearer token, expecting TaskResponse. */
    private ResponseEntity<ApiResponse<TaskResponse>> postTask(String path, Object body, String token) {
        return restTemplate.exchange(
                url(path), HttpMethod.POST, new HttpEntity<>(body, authHeader(token)),
                new ParameterizedTypeReference<ApiResponse<TaskResponse>>() {});
    }

    /** GET with Bearer token, expecting ProjectResponse. */
    private ResponseEntity<ApiResponse<ProjectResponse>> getProject(String path, String token) {
        return restTemplate.exchange(
                url(path), HttpMethod.GET, new HttpEntity<>(authHeader(token)),
                new ParameterizedTypeReference<ApiResponse<ProjectResponse>>() {});
    }

    /** GET with Bearer token, expecting TaskResponse. */
    private ResponseEntity<ApiResponse<TaskResponse>> getTask(String path, String token) {
        return restTemplate.exchange(
                url(path), HttpMethod.GET, new HttpEntity<>(authHeader(token)),
                new ParameterizedTypeReference<ApiResponse<TaskResponse>>() {});
    }

    /**
     * GET paginated response — uses Map because Page<T> is an interface
     * and cannot be deserialized by Jackson directly.
     */
    private ResponseEntity<ApiResponse<Map<String, Object>>> getPage(String path, String token) {
        return restTemplate.exchange(
                url(path), HttpMethod.GET, new HttpEntity<>(authHeader(token)),
                new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {});
    }

    /** PUT with Bearer token, expecting ProjectResponse. */
    private ResponseEntity<ApiResponse<ProjectResponse>> putProject(String path, Object body, String token) {
        return restTemplate.exchange(
                url(path), HttpMethod.PUT, new HttpEntity<>(body, authHeader(token)),
                new ParameterizedTypeReference<ApiResponse<ProjectResponse>>() {});
    }

    /** PUT with Bearer token, expecting TaskResponse. */
    private ResponseEntity<ApiResponse<TaskResponse>> putTask(String path, Object body, String token) {
        return restTemplate.exchange(
                url(path), HttpMethod.PUT, new HttpEntity<>(body, authHeader(token)),
                new ParameterizedTypeReference<ApiResponse<TaskResponse>>() {});
    }

    /** PATCH with Bearer token, expecting TaskResponse. */
    private ResponseEntity<ApiResponse<TaskResponse>> patchTask(String path, Object body, String token) {
        return restTemplate.exchange(
                url(path), HttpMethod.PATCH, new HttpEntity<>(body, authHeader(token)),
                new ParameterizedTypeReference<ApiResponse<TaskResponse>>() {});
    }

    /** DELETE with Bearer token, expecting Void. */
    private ResponseEntity<ApiResponse<Void>> deleteResource(String path, String token) {
        return restTemplate.exchange(
                url(path), HttpMethod.DELETE, new HttpEntity<>(authHeader(token)),
                new ParameterizedTypeReference<ApiResponse<Void>>() {});
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractContent(Map<String, Object> pageData) {
        return (List<Map<String, Object>>) pageData.get("content");
    }

    private String registerAndLogin(String username, String password) {
        RegisterRequest req = RegisterRequest.builder()
                .username(username).password(password).build();
        ResponseEntity<ApiResponse<AuthResponse>> resp = postAuth("/api/auth/register", req);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return Objects.requireNonNull(resp.getBody()).getData().getToken();
    }

    private Long createProject(String name, String token) {
        CreateProjectRequest req = CreateProjectRequest.builder()
                .name(name).description("Integration test project").build();
        ResponseEntity<ApiResponse<ProjectResponse>> resp = postProject("/api/projects", req, token);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return Objects.requireNonNull(resp.getBody()).getData().getId();
    }

    private Long createTask(Long projectId, String title, String token) {
        CreateTaskRequest req = CreateTaskRequest.builder()
                .title(title).description("Integration test task")
                .priority(TaskPriority.HIGH).build();
        ResponseEntity<ApiResponse<TaskResponse>> resp = postTask(
                "/api/projects/" + projectId + "/tasks", req, token);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return Objects.requireNonNull(resp.getBody()).getData().getId();
    }

    // ========================================================================
    // Tests
    // ========================================================================

    @Nested
    @DisplayName("Authentication Flow")
    class AuthFlow {

        @Test
        @DisplayName("Register → Login → Use token successfully")
        void fullAuthFlow() {
            ResponseEntity<ApiResponse<AuthResponse>> registerResp = postAuth(
                    "/api/auth/register",
                    RegisterRequest.builder().username("authuser").password("password123").build());

            assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(registerResp.getBody()).isNotNull();
            assertThat(registerResp.getBody().isSuccess()).isTrue();
            assertThat(registerResp.getBody().getData().getToken()).isNotBlank();
            assertThat(registerResp.getBody().getData().getUsername()).isEqualTo("authuser");

            ResponseEntity<ApiResponse<AuthResponse>> loginResp = postAuth(
                    "/api/auth/login",
                    LoginRequest.builder().username("authuser").password("password123").build());

            assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(loginResp.getBody()).isNotNull();
            assertThat(loginResp.getBody().isSuccess()).isTrue();
            assertThat(loginResp.getBody().getData().getToken()).isNotBlank();

            String token = loginResp.getBody().getData().getToken();
            ResponseEntity<ApiResponse<Map<String, Object>>> projectsResp = getPage(
                    "/api/projects?page=0&size=10", token);
            assertThat(projectsResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Register with duplicate username → 400")
        void registerDuplicate() {
            RegisterRequest req = RegisterRequest.builder()
                    .username("duplicateuser").password("password123").build();

            ResponseEntity<ApiResponse<AuthResponse>> first = postAuth("/api/auth/register", req);
            assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            ResponseEntity<ApiResponse<AuthResponse>> second = postAuth("/api/auth/register", req);
            assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(second.getBody()).isNotNull();
            assertThat(second.getBody().isSuccess()).isFalse();
            assertThat(second.getBody().getMessage()).contains("Username already exists");
        }

        @Test
        @DisplayName("Login with wrong password → 401")
        void loginWrongPassword() {
            postAuth("/api/auth/register",
                    RegisterRequest.builder().username("loginuser").password("correctpass").build());

            ResponseEntity<ApiResponse<AuthResponse>> loginResp = postAuth(
                    "/api/auth/login",
                    LoginRequest.builder().username("loginuser").password("wrongpass").build());

            assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(loginResp.getBody()).isNotNull();
            assertThat(loginResp.getBody().isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Access protected endpoint without token → 401")
        void noToken() {
            ResponseEntity<ApiResponse<Void>> resp = restTemplate.exchange(
                    url("/api/projects"),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Void>>() {});
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Access protected endpoint with invalid token → 401")
        void invalidToken() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("invalid.jwt.token");
            ResponseEntity<ApiResponse<Void>> resp = restTemplate.exchange(
                    url("/api/projects"),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<ApiResponse<Void>>() {});
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("Project CRUD Flow")
    class ProjectFlow {

        @Test
        @DisplayName("Create → Get → Update → Delete project")
        void fullProjectLifecycle() {
            String token = registerAndLogin("projectowner", "password123");

            ResponseEntity<ApiResponse<ProjectResponse>> createResp = postProject(
                    "/api/projects",
                    CreateProjectRequest.builder().name("My Project").description("Description").build(),
                    token);

            assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Long projectId = createResp.getBody().getData().getId();
            assertThat(createResp.getBody().getData().getName()).isEqualTo("My Project");

            ResponseEntity<ApiResponse<ProjectResponse>> getResp = getProject(
                    "/api/projects/" + projectId, token);
            assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResp.getBody().getData().getName()).isEqualTo("My Project");

            ResponseEntity<ApiResponse<Map<String, Object>>> getAllResp = getPage(
                    "/api/projects?page=0&size=10", token);
            assertThat(getAllResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(extractContent(getAllResp.getBody().getData())).isNotEmpty();

            ResponseEntity<ApiResponse<ProjectResponse>> updateResp = putProject(
                    "/api/projects/" + projectId,
                    CreateProjectRequest.builder().name("Updated Project").description("Updated").build(),
                    token);
            assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateResp.getBody().getData().getName()).isEqualTo("Updated Project");

            ResponseEntity<ApiResponse<Void>> deleteResp = deleteResource(
                    "/api/projects/" + projectId, token);
            assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(deleteResp.getBody().isSuccess()).isTrue();

            ResponseEntity<ApiResponse<ProjectResponse>> getDeletedResp = getProject(
                    "/api/projects/" + projectId, token);
            assertThat(getDeletedResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Non-owner cannot update or delete project → 403")
        void nonOwnerAccessDenied() {
            String owner = registerAndLogin("owner1", "password123");
            String intruder = registerAndLogin("intruder", "password123");

            Long projectId = createProject("Secret Project", owner);

            ResponseEntity<ApiResponse<ProjectResponse>> updateResp = putProject(
                    "/api/projects/" + projectId,
                    CreateProjectRequest.builder().name("Hacked").description("Hacked").build(),
                    intruder);
            assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            ResponseEntity<ApiResponse<Void>> deleteResp = deleteResource(
                    "/api/projects/" + projectId, intruder);
            assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            ResponseEntity<ApiResponse<ProjectResponse>> ownerGet = getProject(
                    "/api/projects/" + projectId, owner);
            assertThat(ownerGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Get non-existent project → 404")
        void projectNotFound() {
            String token = registerAndLogin("notfounduser", "password123");
            ResponseEntity<ApiResponse<ProjectResponse>> resp = getProject(
                    "/api/projects/99999", token);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Task CRUD + AOP Activity Log Flow")
    class TaskFlow {

        @Test
        @DisplayName("Create → Get → Update → Update Status → Verify Activity Log → Delete")
        void fullTaskLifecycleWithActivityLog() {
            String token = registerAndLogin("taskowner", "password123");
            Long projectId = createProject("Task Project", token);

            Long taskId = createTask(projectId, "First Task", token);

            ResponseEntity<ApiResponse<TaskResponse>> getResp = getTask(
                    "/api/projects/" + projectId + "/tasks/" + taskId, token);
            assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResp.getBody().getData().getTitle()).isEqualTo("First Task");
            assertThat(getResp.getBody().getData().getStatus()).isEqualTo(TaskStatus.TODO);

            ResponseEntity<ApiResponse<Map<String, Object>>> listResp = getPage(
                    "/api/projects/" + projectId + "/tasks?page=0&size=10", token);
            assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(extractContent(listResp.getBody().getData())).hasSize(1);

            ResponseEntity<ApiResponse<Map<String, Object>>> filteredResp = getPage(
                    "/api/projects/" + projectId + "/tasks?status=TODO&page=0&size=10", token);
            assertThat(filteredResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(extractContent(filteredResp.getBody().getData())).hasSize(1);

            ResponseEntity<ApiResponse<TaskResponse>> updateResp = putTask(
                    "/api/projects/" + projectId + "/tasks/" + taskId,
                    CreateTaskRequest.builder()
                            .title("Updated Task").description("Updated description")
                            .priority(TaskPriority.LOW).build(),
                    token);
            assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateResp.getBody().getData().getTitle()).isEqualTo("Updated Task");
            assertThat(updateResp.getBody().getData().getPriority()).isEqualTo(TaskPriority.LOW);

            // Update status to IN_PROGRESS (triggers AOP activity log)
            ResponseEntity<ApiResponse<TaskResponse>> statusResp = patchTask(
                    "/api/projects/" + projectId + "/tasks/" + taskId + "/status",
                    UpdateTaskStatusRequest.builder().status(TaskStatus.IN_PROGRESS).build(),
                    token);
            assertThat(statusResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(statusResp.getBody().getData().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

            // Update status to DONE
            ResponseEntity<ApiResponse<TaskResponse>> doneResp = patchTask(
                    "/api/projects/" + projectId + "/tasks/" + taskId + "/status",
                    UpdateTaskStatusRequest.builder().status(TaskStatus.DONE).build(),
                    token);
            assertThat(doneResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(doneResp.getBody().getData().getStatus()).isEqualTo(TaskStatus.DONE);

            // Verify activity logs were created automatically by AOP
            ResponseEntity<ApiResponse<Map<String, Object>>> activityResp = getPage(
                    "/api/projects/" + projectId + "/activities?page=0&size=10&sort=createdAt,desc",
                    token);
            assertThat(activityResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(extractContent(activityResp.getBody().getData())).hasSize(2);

            List<Map<String, Object>> activities = extractContent(activityResp.getBody().getData());
            List<String> contents = activities.stream()
                    .map(a -> (String) a.get("content"))
                    .toList();
            assertThat(contents).anyMatch(c -> c.contains("Updated Task") && c.contains("IN_PROGRESS"));
            assertThat(contents).anyMatch(c -> c.contains("Updated Task") && c.contains("DONE"));

            // Delete task
            ResponseEntity<ApiResponse<Void>> deleteResp = deleteResource(
                    "/api/projects/" + projectId + "/tasks/" + taskId, token);
            assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Verify task is deleted
            ResponseEntity<ApiResponse<TaskResponse>> getDeletedResp = getTask(
                    "/api/projects/" + projectId + "/tasks/" + taskId, token);
            assertThat(getDeletedResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Non-owner cannot create/update/delete tasks → 403")
        void nonOwnerTaskAccessDenied() {
            String owner = registerAndLogin("taskowner2", "password123");
            String intruder = registerAndLogin("taskintruder", "password123");

            Long projectId = createProject("Task Project 2", owner);
            Long taskId = createTask(projectId, "Owner Task", owner);

            ResponseEntity<ApiResponse<TaskResponse>> createResp = postTask(
                    "/api/projects/" + projectId + "/tasks",
                    CreateTaskRequest.builder().title("Intruder Task").build(),
                    intruder);
            assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            ResponseEntity<ApiResponse<TaskResponse>> updateResp = putTask(
                    "/api/projects/" + projectId + "/tasks/" + taskId,
                    CreateTaskRequest.builder().title("Hacked").build(),
                    intruder);
            assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            ResponseEntity<ApiResponse<TaskResponse>> statusResp = patchTask(
                    "/api/projects/" + projectId + "/tasks/" + taskId + "/status",
                    UpdateTaskStatusRequest.builder().status(TaskStatus.DONE).build(),
                    intruder);
            assertThat(statusResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            ResponseEntity<ApiResponse<Void>> deleteResp = deleteResource(
                    "/api/projects/" + projectId + "/tasks/" + taskId, intruder);
            assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            ResponseEntity<ApiResponse<TaskResponse>> ownerGet = getTask(
                    "/api/projects/" + projectId + "/tasks/" + taskId, owner);
            assertThat(ownerGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Create task without assignee → assignee fields are null")
        void createTaskWithoutAssignee() {
            String token = registerAndLogin("taskowner3", "password123");
            Long projectId = createProject("Assignee Project", token);

            ResponseEntity<ApiResponse<TaskResponse>> resp = postTask(
                    "/api/projects/" + projectId + "/tasks",
                    CreateTaskRequest.builder().title("Unassigned Task").description("No assignee").build(),
                    token);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getBody().getData().getAssigneeId()).isNull();
            assertThat(resp.getBody().getData().getAssigneeUsername()).isNull();
        }

        @Test
        @DisplayName("Get tasks with status filter → only matching status returned")
        void taskStatusFilter() {
            String token = registerAndLogin("filterowner", "password123");
            Long projectId = createProject("Filter Project", token);

            createTask(projectId, "Task TODO", token);

            ResponseEntity<ApiResponse<TaskResponse>> doingResp = postTask(
                    "/api/projects/" + projectId + "/tasks",
                    CreateTaskRequest.builder().title("Task IN_PROGRESS").description("In progress").build(),
                    token);
            Long doingTaskId = doingResp.getBody().getData().getId();

            patchTask(
                    "/api/projects/" + projectId + "/tasks/" + doingTaskId + "/status",
                    UpdateTaskStatusRequest.builder().status(TaskStatus.IN_PROGRESS).build(),
                    token);

            ResponseEntity<ApiResponse<Map<String, Object>>> todoResp = getPage(
                    "/api/projects/" + projectId + "/tasks?status=TODO&page=0&size=10", token);
            assertThat(extractContent(todoResp.getBody().getData())).hasSize(1);
            assertThat(
                ((Map<String, Object>) extractContent(todoResp.getBody().getData()).get(0)).get("title")
            ).isEqualTo("Task TODO");

            ResponseEntity<ApiResponse<Map<String, Object>>> inProgressResp = getPage(
                    "/api/projects/" + projectId + "/tasks?status=IN_PROGRESS&page=0&size=10", token);
            assertThat(extractContent(inProgressResp.getBody().getData())).hasSize(1);
            assertThat(
                ((Map<String, Object>) extractContent(inProgressResp.getBody().getData()).get(0)).get("title")
            ).isEqualTo("Task IN_PROGRESS");
        }
    }

    @Nested
    @DisplayName("Validation Error Flow")
    class ValidationFlow {

        @Test
        @DisplayName("Register with blank username → 400")
        void registerBlankUsername() {
            ResponseEntity<ApiResponse<AuthResponse>> resp = postAuth(
                    "/api/auth/register",
                    RegisterRequest.builder().username("").password("password123").build());
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resp.getBody().isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Register with short password → 400")
        void registerShortPassword() {
            ResponseEntity<ApiResponse<AuthResponse>> resp = postAuth(
                    "/api/auth/register",
                    RegisterRequest.builder().username("validuser").password("12345").build());
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resp.getBody().isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Create project with blank name → 400")
        void createProjectBlankName() {
            String token = registerAndLogin("valuser", "password123");
            ResponseEntity<ApiResponse<ProjectResponse>> resp = postProject(
                    "/api/projects",
                    CreateProjectRequest.builder().name("").build(),
                    token);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Create task with blank title → 400")
        void createTaskBlankTitle() {
            String token = registerAndLogin("valuser2", "password123");
            Long projectId = createProject("Validation Project", token);

            ResponseEntity<ApiResponse<TaskResponse>> resp = postTask(
                    "/api/projects/" + projectId + "/tasks",
                    CreateTaskRequest.builder().title("").build(),
                    token);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Update task status with null status → 400")
        void updateStatusNull() {
            String token = registerAndLogin("valuser3", "password123");
            Long projectId = createProject("Status Project", token);
            Long taskId = createTask(projectId, "Status Task", token);

            ResponseEntity<ApiResponse<TaskResponse>> resp = patchTask(
                    "/api/projects/" + projectId + "/tasks/" + taskId + "/status",
                    UpdateTaskStatusRequest.builder().status(null).build(),
                    token);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Create task in non-existent project → 404")
        void taskInNonExistentProject() {
            String token = registerAndLogin("edgeuser", "password123");
            ResponseEntity<ApiResponse<TaskResponse>> resp = postTask(
                    "/api/projects/99999/tasks",
                    CreateTaskRequest.builder().title("Orphan Task").build(),
                    token);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Get activities for project with no logs → empty page")
        void emptyActivityLog() {
            String token = registerAndLogin("edgeuser2", "password123");
            Long projectId = createProject("Empty Log Project", token);

            ResponseEntity<ApiResponse<Map<String, Object>>> resp = getPage(
                    "/api/projects/" + projectId + "/activities?page=0&size=10", token);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(extractContent(resp.getBody().getData())).isEmpty();
        }

        @Test
        @DisplayName("Delete project removes associated tasks and activity logs")
        void cascadeDeleteProject() {
            String token = registerAndLogin("cascadeuser", "password123");
            Long projectId = createProject("Cascade Project", token);
            createTask(projectId, "Task 1", token);
            createTask(projectId, "Task 2", token);

            ResponseEntity<ApiResponse<Map<String, Object>>> tasksResp = getPage(
                    "/api/projects/" + projectId + "/tasks?page=0&size=10", token);
            List<Map<String, Object>> tasks = extractContent(tasksResp.getBody().getData());
            Long task1Id = ((Number) tasks.get(0).get("id")).longValue();
            patchTask(
                    "/api/projects/" + projectId + "/tasks/" + task1Id + "/status",
                    UpdateTaskStatusRequest.builder().status(TaskStatus.DONE).build(),
                    token);

            ResponseEntity<ApiResponse<Void>> deleteResp = deleteResource(
                    "/api/projects/" + projectId, token);
            assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.OK);

            ResponseEntity<ApiResponse<ProjectResponse>> getProjectResp = getProject(
                    "/api/projects/" + projectId, token);
            assertThat(getProjectResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
