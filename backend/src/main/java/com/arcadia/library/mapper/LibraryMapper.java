package com.arcadia.library.mapper;

import com.arcadia.entity.Category;
import com.arcadia.entity.Game;
import com.arcadia.entity.LibraryItem;
import com.arcadia.game.dto.response.CategoryResponse;
import com.arcadia.library.dto.response.LibraryGameResponse;
import com.arcadia.library.dto.response.LibraryItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LibraryMapper {

    LibraryItemResponse toResponse(LibraryItem item);

    LibraryGameResponse toGame(Game game);

    CategoryResponse toCategory(Category category);
}
