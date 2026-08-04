package com.arcadia.repository;

import com.arcadia.entity.GameVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameVersionRepository extends JpaRepository<GameVersion, Long> {

    Optional<GameVersion> findByGameIdAndVersion(Long gameId, String version);

    List<GameVersion> findByGameIdOrderByUploadedAtDesc(Long gameId);
}
