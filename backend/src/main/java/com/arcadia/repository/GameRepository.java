package com.arcadia.repository;

import com.arcadia.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("""
            SELECT g FROM Game g
            WHERE g.id = :id
              AND g.isPublic = true
              AND g.isHidden = false
            """)
    Optional<Game> findPublishedById(@Param("id") Long id);

    @Query("""
            SELECT g FROM Game g LEFT JOIN g.category c
            WHERE g.isPublic = true
              AND g.isHidden = false
              AND (:search IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:categorySlug IS NULL OR c.slug = :categorySlug)
            ORDER BY g.createdAt DESC
            """)
    List<Game> findCatalog(@Param("search") String search, @Param("categorySlug") String categorySlug);
}
