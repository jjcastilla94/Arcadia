package com.arcadia.progress;

import com.arcadia.entity.Game;
import com.arcadia.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProgressFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void progressCloudSaveFlow() throws Exception {
        Game published = saveGame("Published", "prog-published", true, false);
        Game unpublished = saveGame("Unpublished", "prog-unpublished", false, false);
        Game hidden = saveGame("Hidden", "prog-hidden", true, true);

        String register = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"proguser","email":"prog@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = "Bearer " + objectMapper.readTree(register).path("data").path("accessToken").asText();

        String registerOther = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"progother","email":"progother@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String otherToken = "Bearer " + objectMapper.readTree(registerOther).path("data").path("accessToken").asText();

        // 401 without token
        mockMvc.perform(post("/api/progress/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d,\"data\":{\"score\":1500}}".formatted(published.getId())))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/progress/%d".formatted(published.getId())))
                .andExpect(status().isUnauthorized());

        // 404 for unpublished, hidden and non-existent games
        mockMvc.perform(post("/api/progress/save")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d,\"data\":{\"score\":1}}".formatted(unpublished.getId())))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/progress/save")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d,\"data\":{\"score\":1}}".formatted(hidden.getId())))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/progress/save")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":99999,\"data\":{\"score\":1}}"))
                .andExpect(status().isNotFound());

        // 400 for invalid data (not object/array, null, missing)
        mockMvc.perform(post("/api/progress/save")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d,\"data\":\"hola\"}".formatted(published.getId())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/progress/save")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d,\"data\":123}".formatted(published.getId())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/progress/save")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d,\"data\":null}".formatted(published.getId())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/progress/save")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d}".formatted(published.getId())))
                .andExpect(status().isBadRequest());

        // 404 when no save exists yet
        mockMvc.perform(get("/api/progress/%d".formatted(published.getId()))
                        .header("Authorization", token))
                .andExpect(status().isNotFound());

        // creation
        mockMvc.perform(post("/api/progress/save")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d,\"data\":{\"score\":1500,\"level\":3}}".formatted(published.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gameId").value(published.getId()))
                .andExpect(jsonPath("$.data.data.score").value(1500))
                .andExpect(jsonPath("$.data.data.level").value(3))
                .andExpect(jsonPath("$.data.updatedAt", not("")));

        // read after creation
        mockMvc.perform(get("/api/progress/%d".formatted(published.getId()))
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gameId").value(published.getId()))
                .andExpect(jsonPath("$.data.data.score").value(1500))
                .andExpect(jsonPath("$.data.data.level").value(3));

        // update (upsert)
        mockMvc.perform(post("/api/progress/save")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d,\"data\":[{\"id\":1,\"level\":5}]}".formatted(published.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].id").value(1));

        mockMvc.perform(get("/api/progress/%d".formatted(published.getId()))
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].level").value(5));

        // ownership: another user cannot read this save
        mockMvc.perform(get("/api/progress/%d".formatted(published.getId()))
                        .header("Authorization", otherToken))
                .andExpect(status().isNotFound());

        // ownership: other user can save their own independent save
        mockMvc.perform(post("/api/progress/save")
                        .header("Authorization", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":%d,\"data\":{\"score\":999}}".formatted(published.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data.score").value(999));

        // original save untouched by the other user
        mockMvc.perform(get("/api/progress/%d".formatted(published.getId()))
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].id").value(1));
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
