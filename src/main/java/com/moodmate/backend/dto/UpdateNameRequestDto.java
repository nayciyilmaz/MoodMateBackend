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
public class UpdateNameRequestDto {

    @NotBlank(message = "{validation.firstName.notBlank}")
    @Size(min = 3, message = "{validation.firstName.minLength}")
    private String first_name;

    @NotBlank(message = "{validation.lastName.notBlank}")
    @Size(min = 3, message = "{validation.lastName.minLength}")
    private String last_name;
}
