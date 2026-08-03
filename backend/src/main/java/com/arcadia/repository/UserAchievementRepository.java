package com.arcadia.repository;

import com.arcadia.entity.UserAchievement;
import com.arcadia.entity.UserAchievementId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UserAchievementId> {
}
