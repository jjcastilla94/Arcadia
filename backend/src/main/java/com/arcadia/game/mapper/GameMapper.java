package com.arcadia.game.mapper;

import com.arcadia.admin.dto.response.AdminGameResponse;
import com.arcadia.entity.Achievement;
import com.arcadia.entity.Category;
import com.arcadia.entity.Game;
import com.arcadia.entity.GameImage;
import com.arcadia.entity.GameVersion;
import com.arcadia.game.dto.response.AchievementResponse;
import com.arcadia.game.dto.response.CategoryResponse;
import com.arcadia.game.dto.response.GameDetailsResponse;
import com.arcadia.game.dto.response.GameImageResponse;
import com.arcadia.game.dto.response.GameSummaryResponse;
import com.arcadia.game.dto.response.GameVersionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameMapper {

    GameSummaryResponse toSummary(Game game, long playCount);

    default AdminGameResponse toAdmin(Game game) {
        CategoryResponse category = game.getCategory() != null ? toCategory(game.getCategory()) : null;
        return new AdminGameResponse(
                game.getId(),
                game.getTitle(),
                game.getSlug(),
                game.getDescription(),
                game.getThumbnailPath(),
                game.getCoverUrl(),
                game.getFilePath(),
                game.getVersion(),
                game.getFileSize(),
                category,
                game.isPublic(),
                game.isHidden(),
                game.getCreatedAt(),
                game.getUpdatedAt()
        );
    }

    @Mapping(target = "images", source = "gameImages")
    @Mapping(target = "versions", source = "gameVersions")
    @Mapping(target = "achievements", source = "gameAchievements")
    GameDetailsResponse toDetails(Game game,
                                  List<GameImageResponse> gameImages,
                                  List<GameVersionResponse> gameVersions,
                                  List<AchievementResponse> gameAchievements,
                                  long playCount,
                                  long playerCount,
                                  long totalPlayTimeSeconds);

    CategoryResponse toCategory(Category category);

    GameImageResponse toImage(GameImage image);

    GameVersionResponse toVersion(GameVersion version);

    AchievementResponse toAchievement(Achievement achievement);
}
