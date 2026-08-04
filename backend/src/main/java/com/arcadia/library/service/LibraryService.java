package com.arcadia.library.service;

import com.arcadia.common.exception.ConflictException;
import com.arcadia.common.exception.GameNotFoundException;
import com.arcadia.common.exception.LibraryItemNotFoundException;
import com.arcadia.entity.Game;
import com.arcadia.entity.LibraryItem;
import com.arcadia.entity.LibraryStatus;
import com.arcadia.entity.User;
import com.arcadia.library.dto.response.LibraryItemResponse;
import com.arcadia.library.mapper.LibraryMapper;
import com.arcadia.repository.GameRepository;
import com.arcadia.repository.LibraryItemRepository;
import com.arcadia.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibraryService {

    private final LibraryItemRepository libraryItemRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final LibraryMapper libraryMapper;

    public LibraryService(LibraryItemRepository libraryItemRepository,
                          UserRepository userRepository,
                          GameRepository gameRepository,
                          LibraryMapper libraryMapper) {
        this.libraryItemRepository = libraryItemRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.libraryMapper = libraryMapper;
    }

    @Transactional
    public LibraryItemResponse addToLibrary(User principal, Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found: " + gameId));
        if (libraryItemRepository.existsByUserIdAndGameIdAndRemovedFalse(principal.getId(), gameId)) {
            throw new ConflictException("Game already in library: " + gameId);
        }
        return libraryItemRepository.findByUserIdAndGameId(principal.getId(), gameId)
                .map(item -> {
                    item.setRemoved(false);
                    return libraryMapper.toResponse(libraryItemRepository.save(item));
                })
                .orElseGet(() -> {
                    LibraryItem item = LibraryItem.builder()
                            .user(userRepository.getReferenceById(principal.getId()))
                            .game(game)
                            .build();
                    return libraryMapper.toResponse(libraryItemRepository.save(item));
                });
    }

    @Transactional
    public void removeFromLibrary(User principal, Long gameId) {
        LibraryItem item = findItem(principal.getId(), gameId);
        item.setRemoved(true);
        libraryItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<LibraryItemResponse> getMyLibrary(User principal) {
        return libraryItemRepository.findByUserIdAndRemovedFalseOrderByAddedAtDesc(principal.getId()).stream()
                .map(libraryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isInLibrary(User principal, Long gameId) {
        return libraryItemRepository.existsByUserIdAndGameIdAndRemovedFalse(principal.getId(), gameId);
    }

    @Transactional
    public LibraryItemResponse updateStatus(User principal, Long gameId, LibraryStatus status) {
        LibraryItem item = findItem(principal.getId(), gameId);
        item.setStatus(status);
        return libraryMapper.toResponse(libraryItemRepository.save(item));
    }

    @Transactional
    public LibraryItemResponse updateRating(User principal, Long gameId, Integer rating) {
        LibraryItem item = findItem(principal.getId(), gameId);
        item.setRating(rating);
        return libraryMapper.toResponse(libraryItemRepository.save(item));
    }

    private LibraryItem findItem(Long userId, Long gameId) {
        return libraryItemRepository.findByUserIdAndGameIdAndRemovedFalse(userId, gameId)
                .orElseThrow(() -> new LibraryItemNotFoundException("Game not found in library: " + gameId));
    }
}
