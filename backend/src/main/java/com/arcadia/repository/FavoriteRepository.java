package com.arcadia.repository;

import com.arcadia.entity.Favorite;
import com.arcadia.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    List<Favorite> findByUserId(Long userId);
}
