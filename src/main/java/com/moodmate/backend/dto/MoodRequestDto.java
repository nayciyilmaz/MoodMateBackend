package com.moodmate.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MoodRequestDto {

    @NotBlank(message = "{validation.emoji.notBlank}")
    private String emoji;

    @NotNull(message = "{validation.score.notNull}")
    private Integer score;

    @NotBlank(message = "{validation.note.notBlank}")
    private String note;

    private LocalDateTime entryDate;
}