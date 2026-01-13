package com.moodmate.backend.mapper;

import com.moodmate.backend.dto.UserRequestDto;
import com.moodmate.backend.dto.UserResponseDto;
import com.moodmate.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "first_name", target = "firstName")
    @Mapping(source = "last_name", target = "lastName")
    User mapToEntity(UserRequestDto requestDto);

    @Mapping(source = "user.firstName", target = "first_name")
    @Mapping(source = "user.lastName", target = "last_name")
    UserResponseDto mapToDto(User user, String token);
}
