package com.moodmate.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEmailRequestDto {

    @NotBlank(message = "{validation.email.notBlank}")
    @Size(min = 3, message = "{validation.email.minLength}")
    @Email(message = "{validation.email.invalid}")
    private String email;
}
