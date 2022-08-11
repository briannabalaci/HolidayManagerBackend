package com.internship.holiday_manager.mapper;

import com.internship.holiday_manager.dto.UserWithTeamIdDto;
import com.internship.holiday_manager.entity.User;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY)
public interface UserWithTeamIdMapper {

    UserWithTeamIdDto entityToDto(User entity);

    User dtoToEntity(UserWithTeamIdDto dto);

    List<UserWithTeamIdDto> entitiesToDtos(List<User> entities);

    List<User> dtosToEntities(List<UserWithTeamIdDto> dtos);
}
