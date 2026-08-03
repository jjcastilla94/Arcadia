package com.arcadia.auth;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginRefreshFlow() throws Exception {
        String registerBody = """
                {"nickname":"testuser","email":"test@example.com","password":"password123"}
                """;

        String registerJson = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken", not("")))
                .andExpect(jsonPath("$.data.refreshToken", not("")))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.nickname").value("testuser"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.roles", containsInAnyOrder("ROLE_USER")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode register = objectMapper.readTree(registerJson).path("data");
        String accessToken = register.path("accessToken").asText();
        String refreshToken = register.path("refreshToken").asText();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isConflict());

        String loginBody = """
                {"identifier":"test@example.com","password":"password123"}
                """;
        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.nickname").value("testuser"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode login = objectMapper.readTree(loginJson).path("data");
        String refreshToken2 = login.path("refreshToken").asText();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"test@example.com","password":"wrongpassword"}
                                """))
                .andExpect(status().isUnauthorized());

        String refreshJson = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", not("")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode refresh = objectMapper.readTree(refreshJson).path("data");
        String refreshedAccessToken = refresh.path("accessToken").asText();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken2)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/protected-endpoint"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/protected-endpoint")
                        .header("Authorization", "Bearer " + refreshedAccessToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/protected-endpoint")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
