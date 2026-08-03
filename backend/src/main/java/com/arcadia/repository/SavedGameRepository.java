package com.arcadia.repository;

import com.arcadia.entity.SavedGame;
import com.arcadia.entity.SavedGameId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedGameRepository extends JpaRepository<SavedGame, SavedGameId> {
}
