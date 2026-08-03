package com.arcadia.repository;

import com.arcadia.entity.GameImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameImageRepository extends JpaRepository<GameImage, Long> {

    List<GameImage> findByGameIdOrderByPositionAsc(Long gameId);
}
