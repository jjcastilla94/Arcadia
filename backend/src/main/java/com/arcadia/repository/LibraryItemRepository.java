package com.arcadia.repository;

import com.arcadia.entity.LibraryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibraryItemRepository extends JpaRepository<LibraryItem, Long> {

    Optional<LibraryItem> findByUserIdAndGameId(Long userId, Long gameId);

    Optional<LibraryItem> findByUserIdAndGameIdAndRemovedFalse(Long userId, Long gameId);

    boolean existsByUserIdAndGameIdAndRemovedFalse(Long userId, Long gameId);

    List<LibraryItem> findByUserIdAndRemovedFalseOrderByAddedAtDesc(Long userId);
}
