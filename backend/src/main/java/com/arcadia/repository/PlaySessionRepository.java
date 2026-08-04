package com.arcadia.repository;

import com.arcadia.entity.PlaySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PlaySessionRepository extends JpaRepository<PlaySession, Long> {

    List<PlaySession> findByUserId(Long userId);

    List<PlaySession> findByGameId(Long gameId);

    @Query("""
            SELECT p.game.id, COUNT(p) FROM PlaySession p
            WHERE p.game.id IN :gameIds
            GROUP BY p.game.id
            """)
    List<Object[]> countByGameIds(@Param("gameIds") Collection<Long> gameIds);
}
