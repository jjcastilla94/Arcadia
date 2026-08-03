package com.arcadia.auth.mapper;

import com.arcadia.auth.dto.response.UserResponse;
import com.arcadia.entity.Role;
import com.arcadia.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
