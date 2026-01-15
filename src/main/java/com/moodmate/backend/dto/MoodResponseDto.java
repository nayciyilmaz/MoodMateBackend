package com.moodmate.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MoodResponseDto {
    private Long id;
    private String emoji;
    private Integer score;
    private String note;
    private LocalDateTime entryDate;
    private LocalDateTime createdAt;
}
