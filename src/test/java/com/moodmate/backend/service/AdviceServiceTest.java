package com.moodmate.backend.service;

import com.moodmate.backend.entity.Advice;
import com.moodmate.backend.entity.Mood;
import com.moodmate.backend.entity.User;
import com.moodmate.backend.exception.BusinessException;
import com.moodmate.backend.exception.ErrorCode;
import com.moodmate.backend.mapper.AdviceMapper;
import com.moodmate.backend.repository.AdviceRepository;
import com.moodmate.backend.repository.MoodRepository;
import com.moodmate.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdviceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MoodRepository moodRepository;

    @Mock
    private AdviceRepository adviceRepository;

    @Mock
    private AdviceMapper adviceMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdviceService adviceService;

    private User testUser;
    private Mood testMood;
    private Advice testAdvice;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Yılmaz")
                .lastName("Naycı")
                .email("test@example.com")
                .build();

        testMood = Mood.builder()
                .id(1L)
                .emoji("😊")
                .score(8)
                .note("Bugün harika bir gün geçirdim")
                .entryDate(LocalDateTime.now())
                .user(testUser)
                .build();

        testAdvice = Advice.builder()
                .id(1L)
                .user(testUser)
                .advice("Test advice")
                .createdAt(LocalDateTime.now())
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        ReflectionTestUtils.setField(adviceService, "geminiApiKey", "test-api-key");
    }

    @Test
    void getLatestAdvice_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(adviceRepository.findTopByUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.of(testAdvice));

        assertDoesNotThrow(() -> adviceService.getLatestAdvice());

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(adviceRepository, times(1)).findTopByUserIdOrderByCreatedAtDesc(anyLong());
        verify(adviceMapper, times(1)).mapToDto(any(Advice.class));
    }

    @Test
    void getLatestAdvice_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adviceService.getLatestAdvice()
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(adviceRepository, never()).findTopByUserIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getLatestAdvice_NoAdviceFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(adviceRepository.findTopByUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adviceService.getLatestAdvice()
        );

        assertEquals(ErrorCode.NO_ADVICE_FOUND, exception.getErrorCode());
        verify(adviceRepository, times(1)).findTopByUserIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void generateAdvice_NoMoodData_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findAllByUserIdOrderByEntryDateDesc(anyLong()))
                .thenReturn(Arrays.asList());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adviceService.generateAdvice()
        );

        assertEquals(ErrorCode.NO_MOOD_DATA, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(moodRepository, times(1)).findAllByUserIdOrderByEntryDateDesc(anyLong());
        verify(adviceRepository, never()).save(any(Advice.class));
    }
}