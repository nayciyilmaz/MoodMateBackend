package com.moodmate.backend.mapper;

import com.moodmate.backend.dto.AdviceResponseDto;
import com.moodmate.backend.entity.Advice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdviceMapper {
    AdviceResponseDto mapToDto(Advice advice);
}