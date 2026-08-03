package com.arcadia.repository;

import com.arcadia.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndGameId(Long userId, Long gameId);

    List<Review> findByGameId(Long gameId);
}
