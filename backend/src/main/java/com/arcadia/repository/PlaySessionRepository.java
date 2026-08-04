package com.arcadia.repository;

import com.arcadia.entity.PlaySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlaySessionRepository extends JpaRepository<PlaySession, Long> {

    List<PlaySession> findByUserId(Long userId);

    List<PlaySession> findByGameId(Long gameId);

    long countByGameId(Long gameId);

    @Query("SELECT COUNT(DISTINCT p.user.id) FROM PlaySession p WHERE p.game.id = :gameId")
    long countDistinctUsersByGameId(@Param("gameId") Long gameId);

    @Query("SELECT COALESCE(SUM(p.durationSeconds), 0) FROM PlaySession p WHERE p.game.id = :gameId")
    Optional<Long> sumDurationSecondsByGameId(@Param("gameId") Long gameId);

    @Query("""
            SELECT p.game.id, COUNT(p) FROM PlaySession p
            WHERE p.game.id IN :gameIds
            GROUP BY p.game.id
            """)
    List<Object[]> countByGameIds(@Param("gameIds") Collection<Long> gameIds);
}
