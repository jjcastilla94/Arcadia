package com.arcadia.repository;

import com.arcadia.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByGameId(Long gameId);

    Optional<Achievement> findByGameIdAndTitle(Long gameId, String title);
}
