package com.moodmate.backend.dto;

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
public class ChangePasswordRequestDto {

    @NotBlank(message = "{validation.currentPassword.notBlank}")
    private String currentPassword;

    @NotBlank(message = "{validation.newPassword.notBlank}")
    @Size(min = 6, message = "{validation.password.minLength}")
    private String newPassword;

    @NotBlank(message = "{validation.confirmPassword.notBlank}")
    private String confirmPassword;
}