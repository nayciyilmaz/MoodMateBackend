package com.moodmate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodmate.backend.dto.MoodRequestDto;
import com.moodmate.backend.dto.MoodResponseDto;
import com.moodmate.backend.service.MoodService;
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
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MoodService moodService;

    private MoodRequestDto moodRequestDto;
    private MoodResponseDto moodResponseDto;

    @BeforeEach
    void setUp() {
        moodRequestDto = MoodRequestDto.builder()
                .emoji("😊")
                .score(8)
                .note("Test note")
                .entryDate(LocalDateTime.now())
                .build();

        moodResponseDto = MoodResponseDto.builder()
                .id(1L)
                .emoji("😊")
                .score(8)
                .note("Test note")
                .entryDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addMood_Success() throws Exception {
        when(moodService.createMood(any(MoodRequestDto.class))).thenReturn(moodResponseDto);

        mockMvc.perform(post("/api/moods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moodRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.emoji").value("😊"))
                .andExpect(jsonPath("$.score").value(8));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getMyMoods_Success() throws Exception {
        List<MoodResponseDto> moods = Arrays.asList(moodResponseDto);
        when(moodService.getUserMoods()).thenReturn(moods);

        mockMvc.perform(get("/api/moods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].emoji").value("😊"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateMood_Success() throws Exception {
        when(moodService.updateMood(anyLong(), any(MoodRequestDto.class))).thenReturn(moodResponseDto);

        mockMvc.perform(put("/api/moods/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moodRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.emoji").value("😊"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteMood_Success() throws Exception {
        doNothing().when(moodService).deleteMood(anyLong());

        mockMvc.perform(delete("/api/moods/1"))
                .andExpect(status().isNoContent());

        verify(moodService, times(1)).deleteMood(1L);
    }
}