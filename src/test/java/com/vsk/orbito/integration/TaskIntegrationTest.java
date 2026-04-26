package com.vsk.orbito.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsk.orbito.dto.request.RegisterRequest;
import com.vsk.orbito.enums.Role;
import com.vsk.orbito.task.dto.CreateTaskRequest;
import com.vsk.orbito.task.dto.UpdateTaskRequest;
import com.vsk.orbito.task.enums.TaskPriority;
import com.vsk.orbito.task.enums.TaskStatus;
import com.vsk.orbito.workspace.dto.CreateWorkspaceRequest;
import com.vsk.orbito.project.dto.CreateProjectRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Task Integration Tests")
class TaskIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("orbito_test")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    static MongoDBContainer mongodb =
            new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.mongodb.uri",
                mongodb::getReplicaSetUrl);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    static String token;
    static Long workspaceId;
    static Long projectId;
    static Long taskId;

    @Test
    @Order(1)
    @DisplayName("setup — register user and create workspace + project")
    void setup() throws Exception {
        // register
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Kishore");
        reg.setEmail("kishore@orbito.com");
        reg.setPassword("pass123");
        reg.setRole(Role.DEVELOPER);

        MvcResult regResult = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andReturn();

        token = objectMapper.readTree(
                        regResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // create workspace
        CreateWorkspaceRequest ws = new CreateWorkspaceRequest();
        ws.setName("Test Workspace");

        MvcResult wsResult = mockMvc.perform(
                        post("/api/workspaces")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ws)))
                .andExpect(status().isOk())
                .andReturn();

        workspaceId = objectMapper.readTree(
                        wsResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // create project
        CreateProjectRequest proj = new CreateProjectRequest();
        proj.setName("Test Project");
        proj.setWorkspaceId(workspaceId);

        MvcResult projResult = mockMvc.perform(
                        post("/api/projects")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(proj)))
                .andExpect(status().isOk())
                .andReturn();

        projectId = objectMapper.readTree(
                        projResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("createTask — returns 200 with TODO status")
    void createTask_returns200() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setTitle("Implement search");
        req.setProjectId(projectId);
        req.setPriority(TaskPriority.HIGH);

        MvcResult result = mockMvc.perform(
                        post("/api/tasks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TODO"))
                .andExpect(jsonPath("$.data.title")
                        .value("Implement search"))
                .andReturn();

        taskId = objectMapper.readTree(
                        result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test
    @Order(3)
    @DisplayName("updateTask — TODO to IN_PROGRESS — valid")
    void updateTask_validTransition() throws Exception {
        UpdateTaskRequest req = new UpdateTaskRequest();
        req.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(
                        patch("/api/tasks/" + taskId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status")
                        .value("IN_PROGRESS"));
    }

    @Test
    @Order(4)
    @DisplayName("updateTask — IN_PROGRESS to DONE — invalid transition")
    void updateTask_invalidTransition_returns400() throws Exception {
        UpdateTaskRequest req = new UpdateTaskRequest();
        req.setStatus(TaskStatus.DONE); // skip IN_REVIEW — invalid

        mockMvc.perform(
                        patch("/api/tasks/" + taskId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(5)
    @DisplayName("getTasks — paginated — returns task list")
    void getTasks_paginated_returnsList() throws Exception {
        mockMvc.perform(
                        get("/api/tasks/project/" + projectId
                                + "?page=0&size=10")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()")
                        .value(1));
    }
}