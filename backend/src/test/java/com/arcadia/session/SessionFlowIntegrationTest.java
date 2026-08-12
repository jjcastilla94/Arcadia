package com.arcadia.session;

import com.arcadia.entity.Game;
import com.arcadia.repository.GameRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void sessionVisibilityAndOwnership() throws Exception {
        Game published = saveGame("Published", "pub-game", true, false);
        Game unpublished = saveGame("Unpublished", "unpub-game", false, false);
        Game hidden = saveGame("Hidden", "hidden-game", true, true);

        String register = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"sessuser","email":"sess@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = "Bearer " + objectMapper.readTree(register).path("data").path("accessToken").asText();

        mockMvc.perform(post("/api/play-sessions/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d}".formatted(published.getId())))
                .andExpect(status().isUnauthorized());

        String startJson = mockMvc.perform(post("/api/play-sessions/start")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d}".formatted(published.getId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long sessionId = objectMapper.readTree(startJson).path("data").path("id").asLong();

        mockMvc.perform(post("/api/play-sessions/end")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":%d}".formatted(sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.endedAt", not("")))
                .andExpect(jsonPath("$.data.durationSeconds").isNumber());

        mockMvc.perform(post("/api/play-sessions/start")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d}".formatted(unpublished.getId())))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/play-sessions/start")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d}".formatted(hidden.getId())))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/play-sessions/start")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":99999}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/play-sessions/start")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d}".formatted(published.getId())))
                .andExpect(status().isUnauthorized());
    }

    private Game saveGame(String title, String slug, boolean isPublic, boolean isHidden) {
        return gameRepository.save(Game.builder()
                .title(title)
                .slug(slug)
                .filePath("/uploads/games/" + slug + "/index.html")
                .isPublic(isPublic)
                .isHidden(isHidden)
                .build());
    }
}
