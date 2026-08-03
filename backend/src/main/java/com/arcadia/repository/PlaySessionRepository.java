package com.arcadia.repository;

import com.arcadia.entity.PlaySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaySessionRepository extends JpaRepository<PlaySession, Long> {

    List<PlaySession> findByUserId(Long userId);

    List<PlaySession> findByGameId(Long gameId);
}
