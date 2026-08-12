package com.arcadia.session.service;

import com.arcadia.common.exception.GameNotFoundException;
import com.arcadia.common.exception.SessionNotFoundException;
import com.arcadia.entity.Game;
import com.arcadia.entity.LibraryItem;
import com.arcadia.entity.PlaySession;
import com.arcadia.entity.User;
import com.arcadia.repository.GameRepository;
import com.arcadia.repository.LibraryItemRepository;
import com.arcadia.repository.PlaySessionRepository;
import com.arcadia.repository.UserRepository;
import com.arcadia.session.dto.response.PlaySessionResponse;
import com.arcadia.session.mapper.SessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SessionService {

    private final PlaySessionRepository playSessionRepository;
    private final LibraryItemRepository libraryItemRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final SessionMapper sessionMapper;

    public SessionService(PlaySessionRepository playSessionRepository,
                          LibraryItemRepository libraryItemRepository,
                          UserRepository userRepository,
                          GameRepository gameRepository,
                          SessionMapper sessionMapper) {
        this.playSessionRepository = playSessionRepository;
        this.libraryItemRepository = libraryItemRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.sessionMapper = sessionMapper;
    }

    @Transactional
    public PlaySessionResponse start(User principal, Long gameId) {
        Game game = gameRepository.findPublishedById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found: " + gameId));
        PlaySession session = PlaySession.builder()
                .user(userRepository.getReferenceById(principal.getId()))
                .game(game)
                .build();
        return sessionMapper.toResponse(playSessionRepository.save(session));
    }

    @Transactional
    public PlaySessionResponse end(User principal, Long sessionId) {
        PlaySession session = playSessionRepository.findByIdAndUserId(sessionId, principal.getId())
                .orElseThrow(() -> new SessionNotFoundException("Play session not found: " + sessionId));

        if (session.getEndedAt() != null) {
            return sessionMapper.toResponse(session);
        }

        LocalDateTime endedAt = LocalDateTime.now();
        long durationSeconds = Math.max(Duration.between(session.getStartedAt(), endedAt).getSeconds(), 0);
        session.setEndedAt(endedAt);
        session.setDurationSeconds(durationSeconds);
        playSessionRepository.save(session);

        updateLibraryTime(principal.getId(), session.getGame().getId(), durationSeconds);

        return sessionMapper.toResponse(session);
    }

    private void updateLibraryTime(Long userId, Long gameId, long durationSeconds) {
        Optional<LibraryItem> libraryItem = libraryItemRepository.findByUserIdAndGameIdAndRemovedFalse(userId, gameId);
        libraryItem.ifPresent(item -> {
            item.setTimePlayedSeconds(item.getTimePlayedSeconds() + durationSeconds);
            item.setLastPlayedAt(LocalDateTime.now());
            libraryItemRepository.save(item);
        });
    }
}
