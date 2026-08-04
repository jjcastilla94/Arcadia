package com.arcadia.game.service;

import com.arcadia.common.exception.GameNotFoundException;
import com.arcadia.entity.Achievement;
import com.arcadia.entity.Game;
import com.arcadia.entity.GameImage;
import com.arcadia.entity.GameVersion;
import com.arcadia.game.dto.response.AchievementResponse;
import com.arcadia.game.dto.response.CategoryResponse;
import com.arcadia.game.dto.response.GameDetailsResponse;
import com.arcadia.game.dto.response.GameImageResponse;
import com.arcadia.game.dto.response.GameSummaryResponse;
import com.arcadia.game.dto.response.GameVersionResponse;
import com.arcadia.game.mapper.GameMapper;
import com.arcadia.repository.AchievementRepository;
import com.arcadia.repository.CategoryRepository;
import com.arcadia.repository.GameImageRepository;
import com.arcadia.repository.GameRepository;
import com.arcadia.repository.GameVersionRepository;
import com.arcadia.repository.PlaySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final CategoryRepository categoryRepository;
    private final GameImageRepository gameImageRepository;
    private final GameVersionRepository gameVersionRepository;
    private final AchievementRepository achievementRepository;
    private final PlaySessionRepository playSessionRepository;
    private final GameMapper gameMapper;

    public GameService(GameRepository gameRepository,
                       CategoryRepository categoryRepository,
                       GameImageRepository gameImageRepository,
                       GameVersionRepository gameVersionRepository,
                       AchievementRepository achievementRepository,
                       PlaySessionRepository playSessionRepository,
                       GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.categoryRepository = categoryRepository;
        this.gameImageRepository = gameImageRepository;
        this.gameVersionRepository = gameVersionRepository;
        this.achievementRepository = achievementRepository;
        this.playSessionRepository = playSessionRepository;
        this.gameMapper = gameMapper;
    }

    @Transactional(readOnly = true)
    public List<GameSummaryResponse> listGames(String search, String categorySlug) {
        List<Game> games = gameRepository.findCatalog(normalize(search), normalize(categorySlug));
        Map<Long, Long> playCounts = countPlays(games);
        return games.stream()
                .map(game -> gameMapper.toSummary(game, playCounts.getOrDefault(game.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(gameMapper::toCategory)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameDetailsResponse getBySlug(String slug) {
        Game game = gameRepository.findBySlug(slug)
                .orElseThrow(() -> new GameNotFoundException("Game not found: " + slug));
        if (!game.isPublic() || game.isHidden()) {
            throw new GameNotFoundException("Game not found: " + slug);
        }

        List<GameImageResponse> images = gameImageRepository.findByGameIdOrderByPositionAsc(game.getId()).stream()
                .map(gameMapper::toImage)
                .toList();
        List<GameVersionResponse> versions = gameVersionRepository.findByGameIdOrderByUploadedAtDesc(game.getId()).stream()
                .map(gameMapper::toVersion)
                .toList();
        List<AchievementResponse> achievements = achievementRepository.findByGameId(game.getId()).stream()
                .map(gameMapper::toAchievement)
                .toList();

        long playCount = playSessionRepository.countByGameId(game.getId());
        long playerCount = playSessionRepository.countDistinctUsersByGameId(game.getId());
        long totalPlayTimeSeconds = playSessionRepository.sumDurationSecondsByGameId(game.getId()).orElse(0L);

        return gameMapper.toDetails(game, images, versions, achievements,
                playCount, playerCount, totalPlayTimeSeconds);
    }

    private Map<Long, Long> countPlays(List<Game> games) {
        if (games.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = games.stream().map(Game::getId).toList();
        return playSessionRepository.countByGameIds(ids).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
