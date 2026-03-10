package com.moodmate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodmate.backend.dto.ChangePasswordRequestDto;
import com.moodmate.backend.dto.LoginRequestDto;
import com.moodmate.backend.dto.UserRequestDto;
import com.moodmate.backend.dto.UserResponseDto;
import com.moodmate.backend.exception.BusinessException;
import com.moodmate.backend.exception.ErrorCode;
import com.moodmate.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRequestDto userRequestDto;
    private LoginRequestDto loginRequestDto;
    private UserResponseDto userResponseDto;
    private ChangePasswordRequestDto changePasswordRequestDto;

    @BeforeEach
    void setUp() {
        userRequestDto = UserRequestDto.builder()
                .first_name("Yılmaz")
                .last_name("Naycı")
                .email("test@example.com")
                .password("password123")
                .build();

        loginRequestDto = LoginRequestDto.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .first_name("Yılmaz")
                .last_name("Naycı")
                .email("test@example.com")
                .token("test-token")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        changePasswordRequestDto = ChangePasswordRequestDto.builder()
                .currentPassword("password123")
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();
    }

    @Test
    void registerUser_Success() throws Exception {
        when(userService.registerUser(any(UserRequestDto.class))).thenReturn(userResponseDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    void registerUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        userRequestDto.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_EmptyFields_ReturnsBadRequest() throws Exception {
        userRequestDto.setFirst_name("");
        userRequestDto.setLast_name("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUser_Success() throws Exception {
        when(userService.loginUser(anyString(), anyString())).thenReturn(userResponseDto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    void loginUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        loginRequestDto.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUser_EmptyPassword_ReturnsBadRequest() throws Exception {
        loginRequestDto.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_Success() throws Exception {
        doNothing().when(userService).changePassword(any(ChangePasswordRequestDto.class));

        mockMvc.perform(put("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isOk());

        verify(userService, times(1)).changePassword(any(ChangePasswordRequestDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_EmptyCurrentPassword_ReturnsBadRequest() throws Exception {
        changePasswordRequestDto.setCurrentPassword("");

        mockMvc.perform(put("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any(ChangePasswordRequestDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_EmptyNewPassword_ReturnsBadRequest() throws Exception {
        changePasswordRequestDto.setNewPassword("");

        mockMvc.perform(put("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any(ChangePasswordRequestDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_EmptyConfirmPassword_ReturnsBadRequest() throws Exception {
        changePasswordRequestDto.setConfirmPassword("");

        mockMvc.perform(put("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any(ChangePasswordRequestDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_NewPasswordTooShort_ReturnsBadRequest() throws Exception {
        changePasswordRequestDto.setNewPassword("123");

        mockMvc.perform(put("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any(ChangePasswordRequestDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_CurrentPasswordIncorrect_ReturnsBadRequest() throws Exception {
        doThrow(new BusinessException(ErrorCode.CURRENT_PASSWORD_INCORRECT))
                .when(userService).changePassword(any(ChangePasswordRequestDto.class));

        mockMvc.perform(put("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).changePassword(any(ChangePasswordRequestDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_PasswordsDoNotMatch_ReturnsBadRequest() throws Exception {
        doThrow(new BusinessException(ErrorCode.PASSWORDS_DO_NOT_MATCH))
                .when(userService).changePassword(any(ChangePasswordRequestDto.class));

        mockMvc.perform(put("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).changePassword(any(ChangePasswordRequestDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_NewPasswordSameAsCurrent_ReturnsBadRequest() throws Exception {
        doThrow(new BusinessException(ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT))
                .when(userService).changePassword(any(ChangePasswordRequestDto.class));

        mockMvc.perform(put("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).changePassword(any(ChangePasswordRequestDto.class));
    }
}