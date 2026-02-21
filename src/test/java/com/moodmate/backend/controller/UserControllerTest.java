package com.moodmate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodmate.backend.dto.LoginRequestDto;
import com.moodmate.backend.dto.UserRequestDto;
import com.moodmate.backend.dto.UserResponseDto;
import com.moodmate.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}