package com.arcadia.game.service;

import com.arcadia.entity.Game;
import com.arcadia.game.dto.response.CategoryResponse;
import com.arcadia.game.dto.response.GameSummaryResponse;
import com.arcadia.game.mapper.GameMapper;
import com.arcadia.repository.CategoryRepository;
import com.arcadia.repository.GameRepository;
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
    private final PlaySessionRepository playSessionRepository;
    private final GameMapper gameMapper;

    public GameService(GameRepository gameRepository,
                       CategoryRepository categoryRepository,
                       PlaySessionRepository playSessionRepository,
                       GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.categoryRepository = categoryRepository;
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
