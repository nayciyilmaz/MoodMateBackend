package com.moodmate.backend.controller;

import com.moodmate.backend.dto.AdviceResponseDto;
import com.moodmate.backend.service.AdviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdviceService adviceService;

    private AdviceResponseDto adviceResponseDto;

    @BeforeEach
    void setUp() {
        adviceResponseDto = AdviceResponseDto.builder()
                .id(1L)
                .advice("Test advice")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void generateAdvice_Success() throws Exception {
        when(adviceService.generateAdvice()).thenReturn(adviceResponseDto);

        mockMvc.perform(post("/api/advice/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.advice").value("Test advice"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getLatestAdvice_Success() throws Exception {
        when(adviceService.getLatestAdvice()).thenReturn(adviceResponseDto);

        mockMvc.perform(get("/api/advice/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.advice").value("Test advice"));
    }
}