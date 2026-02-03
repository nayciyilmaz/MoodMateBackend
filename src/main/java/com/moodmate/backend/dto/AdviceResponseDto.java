package com.moodmate.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdviceResponseDto {
    private Long id;
    private String advice;
    private LocalDateTime createdAt;
}