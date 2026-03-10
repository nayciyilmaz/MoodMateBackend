package com.moodmate.backend.service;

import com.moodmate.backend.dto.AdviceResponseDto;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private Mood recentMood;
    private Mood oldMood;
    private Advice testAdvice;
    private AdviceResponseDto adviceResponseDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Yılmaz")
                .lastName("Naycı")
                .email("test@example.com")
                .build();

        recentMood = Mood.builder()
                .id(1L)
                .emoji("😊")
                .score(8)
                .note("Bugün harika bir gün geçirdim")
                .entryDate(LocalDateTime.now())
                .user(testUser)
                .build();

        oldMood = Mood.builder()
                .id(2L)
                .emoji("😔")
                .score(4)
                .note("Eski kayıt")
                .entryDate(LocalDateTime.now().minusDays(5))
                .user(testUser)
                .build();

        testAdvice = Advice.builder()
                .id(1L)
                .user(testUser)
                .advice("Test advice")
                .createdAt(LocalDateTime.now())
                .build();

        adviceResponseDto = AdviceResponseDto.builder()
                .id(1L)
                .advice("Test advice")
                .createdAt(LocalDateTime.now())
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        ReflectionTestUtils.setField(adviceService, "geminiApiKey", "test-api-key");
        ReflectionTestUtils.setField(adviceService, "restTemplate", restTemplate);
    }

    @Test
    void getLatestAdvice_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(adviceRepository.findTopByUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Optional.of(testAdvice));
        when(adviceMapper.mapToDto(any(Advice.class))).thenReturn(adviceResponseDto);

        AdviceResponseDto result = adviceService.getLatestAdvice();

        assertNotNull(result);
        assertEquals("Test advice", result.getAdvice());
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
    }

    @Test
    void generateAdvice_NoMoodData_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findAllByUserIdOrderByEntryDateDesc(anyLong()))
                .thenReturn(Collections.emptyList());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adviceService.generateAdvice()
        );

        assertEquals(ErrorCode.NO_MOOD_DATA, exception.getErrorCode());
        verify(adviceRepository, never()).save(any(Advice.class));
    }

    @Test
    void generateAdvice_OnlyOldMoodsExist_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findAllByUserIdOrderByEntryDateDesc(anyLong()))
                .thenReturn(Arrays.asList(oldMood));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adviceService.generateAdvice()
        );

        assertEquals(ErrorCode.NO_MOOD_DATA, exception.getErrorCode());
        verify(adviceRepository, never()).save(any(Advice.class));
    }

    @Test
    void generateAdvice_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findAllByUserIdOrderByEntryDateDesc(anyLong()))
                .thenReturn(Arrays.asList(recentMood));

        Map<String, Object> part = Map.of("text", "Test advice");
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> candidate = Map.of("content", content);
        Map<String, Object> apiResponse = Map.of("candidates", List.of(candidate));

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(apiResponse);
        when(adviceRepository.save(any(Advice.class))).thenReturn(testAdvice);
        when(adviceMapper.mapToDto(any(Advice.class))).thenReturn(adviceResponseDto);

        AdviceResponseDto result = adviceService.generateAdvice();

        assertNotNull(result);
        assertEquals("Test advice", result.getAdvice());
        verify(adviceRepository, times(1)).save(any(Advice.class));
        verify(adviceMapper, times(1)).mapToDto(any(Advice.class));
    }

    @Test
    void generateAdvice_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adviceService.generateAdvice()
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(moodRepository, never()).findAllByUserIdOrderByEntryDateDesc(anyLong());
        verify(adviceRepository, never()).save(any(Advice.class));
    }

    @Test
    void generateAdvice_ApiReturnsNull_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findAllByUserIdOrderByEntryDateDesc(anyLong()))
                .thenReturn(Arrays.asList(recentMood));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adviceService.generateAdvice()
        );

        assertEquals(ErrorCode.AI_SERVICE_ERROR, exception.getErrorCode());
        verify(adviceRepository, never()).save(any(Advice.class));
    }

    @Test
    void generateAdvice_RateLimitExceeded_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findAllByUserIdOrderByEntryDateDesc(anyLong()))
                .thenReturn(Arrays.asList(recentMood));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.TooManyRequests.class);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adviceService.generateAdvice()
        );

        assertEquals(ErrorCode.AI_RATE_LIMIT, exception.getErrorCode());
        verify(adviceRepository, never()).save(any(Advice.class));
    }
}