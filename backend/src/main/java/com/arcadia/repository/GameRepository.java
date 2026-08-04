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
            WHERE g.isPublic = true
              AND g.isHidden = false
              AND (:search IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:categorySlug IS NULL OR g.category.slug = :categorySlug)
            ORDER BY g.createdAt DESC
            """)
    List<Game> findCatalog(@Param("search") String search, @Param("categorySlug") String categorySlug);
}
