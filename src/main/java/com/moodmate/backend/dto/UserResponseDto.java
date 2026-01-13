package com.moodmate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String first_name;
    private String last_name;
    private String email;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}