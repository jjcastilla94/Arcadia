package com.arcadia.session.mapper;

import com.arcadia.entity.PlaySession;
import com.arcadia.session.dto.response.PlaySessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SessionMapper {

    @Mapping(target = "gameId", source = "game.id")
    PlaySessionResponse toResponse(PlaySession session);
}
