package com.arcadia.progress.service;

import com.arcadia.common.exception.BadRequestException;
import com.arcadia.common.exception.GameNotFoundException;
import com.arcadia.common.exception.SavedGameNotFoundException;
import com.arcadia.entity.Game;
import com.arcadia.entity.SavedGame;
import com.arcadia.entity.SavedGameId;
import com.arcadia.entity.User;
import com.arcadia.progress.dto.request.SaveProgressRequest;
import com.arcadia.progress.dto.response.ProgressResponse;
import com.arcadia.repository.GameRepository;
import com.arcadia.repository.SavedGameRepository;
import com.arcadia.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class ProgressService {

    private final SavedGameRepository savedGameRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ProgressService(SavedGameRepository savedGameRepository,
                           GameRepository gameRepository,
                           UserRepository userRepository,
                           ObjectMapper objectMapper) {
        this.savedGameRepository = savedGameRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProgressResponse save(User principal, SaveProgressRequest request) {
        JsonNode data = request.data();
        if (data == null || data.isNull() || data.isMissingNode() || !(data.isObject() || data.isArray())) {
            throw new BadRequestException("data must be a valid JSON object or array");
        }

        Game game = gameRepository.findPublishedById(request.gameId())
                .orElseThrow(() -> new GameNotFoundException("Game not found: " + request.gameId()));

        SavedGameId id = new SavedGameId(principal.getId(), game.getId());
        SavedGame savedGame = savedGameRepository.findById(id)
                .map(existing -> {
                    existing.setData(data.toString());
                    return existing;
                })
                .orElseGet(() -> {
                    SavedGame created = new SavedGame(userRepository.getReferenceById(principal.getId()), game);
                    created.setData(data.toString());
                    return created;
                });

        return toResponse(savedGameRepository.save(savedGame));
    }

    @Transactional(readOnly = true)
    public ProgressResponse get(User principal, Long gameId) {
        Game game = gameRepository.findPublishedById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found: " + gameId));

        SavedGame savedGame = savedGameRepository.findById(new SavedGameId(principal.getId(), game.getId()))
                .orElseThrow(() -> new SavedGameNotFoundException("No saved game for game: " + gameId));

        return toResponse(savedGame);
    }

    private ProgressResponse toResponse(SavedGame savedGame) {
        JsonNode data;
        try {
            data = objectMapper.readTree(savedGame.getData());
        } catch (Exception e) {
            throw new BadRequestException("Stored progress is not valid JSON");
        }
        return new ProgressResponse(savedGame.getId().getGameId(), data, savedGame.getUpdatedAt());
    }
}
