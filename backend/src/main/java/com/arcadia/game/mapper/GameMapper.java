package com.arcadia.game.mapper;

import com.arcadia.entity.Category;
import com.arcadia.entity.Game;
import com.arcadia.game.dto.response.CategoryResponse;
import com.arcadia.game.dto.response.GameSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameMapper {

    GameSummaryResponse toSummary(Game game, long playCount);

    CategoryResponse toCategory(Category category);
}
