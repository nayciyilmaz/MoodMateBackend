package com.moodmate.backend.mapper;

import com.moodmate.backend.dto.MoodRequestDto;
import com.moodmate.backend.dto.MoodResponseDto;
import com.moodmate.backend.entity.Mood;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MoodMapper {
    //Android’den gelen veriyi → veritabanına
    //veritabanındaki veriyi → Android’e dönüştürür.

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Mood mapToEntity(MoodRequestDto dto);

    MoodResponseDto mapToDto(Mood mood);
}