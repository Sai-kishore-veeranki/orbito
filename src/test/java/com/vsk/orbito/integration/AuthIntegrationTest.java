package com.vsk.orbito.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsk.orbito.dto.request.LoginRequest;
import com.vsk.orbito.dto.request.RegisterRequest;
import com.vsk.orbito.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Auth Integration Tests — real MySQL + MongoDB")
class AuthIntegrationTest {

    // Testcontainers spins up real MySQL and MongoDB in Docker
    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("orbito_test")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    static MongoDBContainer mongodb =
            new MongoDBContainer("mongo:6.0");

    // tell Spring to use the container URLs
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // shared token between tests
    static String accessToken;

    @Test
    @Order(1)
    @DisplayName("register — new user — returns 200 with JWT")
    void register_newUser_returns200WithJwt() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Sai Kishore");
        request.setEmail("sai@orbito.com");
        request.setPassword("password123");
        request.setRole(Role.DEVELOPER);

        MvcResult result = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.email")
                        .value("sai@orbito.com"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(response)
                .path("data").path("accessToken").asText();

        assertThat(accessToken).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("register — duplicate email — returns 400")
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Sai Kishore");
        request.setEmail("sai@orbito.com"); // same email
        request.setPassword("password123");
        request.setRole(Role.DEVELOPER);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(3)
    @DisplayName("login — valid credentials — returns JWT")
    void login_validCredentials_returnsJwt() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("sai@orbito.com");
        request.setPassword("password123");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("login — wrong password — returns 401")
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("sai@orbito.com");
        request.setPassword("wrong_password");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @DisplayName("protected endpoint — with valid token — returns 200")
    void protectedEndpoint_withToken_returns200() throws Exception {
        mockMvc.perform(
                        get("/api/workspaces/my")
                                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    @DisplayName("protected endpoint — without token — returns 403")
    void protectedEndpoint_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/workspaces/my"))
                .andExpect(status().isForbidden());
    }
}