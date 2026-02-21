package com.moodmate.backend.service;

import com.moodmate.backend.dto.MoodRequestDto;
import com.moodmate.backend.dto.MoodResponseDto;
import com.moodmate.backend.entity.Mood;
import com.moodmate.backend.entity.User;
import com.moodmate.backend.exception.BusinessException;
import com.moodmate.backend.exception.ErrorCode;
import com.moodmate.backend.mapper.MoodMapper;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoodServiceTest {

    @Mock
    private MoodRepository moodRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MoodMapper moodMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MoodService moodService;

    private User testUser;
    private Mood testMood;
    private MoodRequestDto moodRequestDto;
    private MoodResponseDto moodResponseDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Yılmaz")
                .lastName("Naycı")
                .email("test@example.com")
                .password("password")
                .build();

        testMood = Mood.builder()
                .id(1L)
                .emoji("😊")
                .score(8)
                .note("Test note")
                .entryDate(LocalDateTime.now())
                .user(testUser)
                .build();

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

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
    }

    @Test
    void createMood_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodMapper.mapToEntity(any(MoodRequestDto.class))).thenReturn(testMood);
        when(moodRepository.save(any(Mood.class))).thenReturn(testMood);
        when(moodMapper.mapToDto(any(Mood.class))).thenReturn(moodResponseDto);

        MoodResponseDto result = moodService.createMood(moodRequestDto);

        assertNotNull(result);
        assertEquals("😊", result.getEmoji());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(moodRepository, times(1)).save(any(Mood.class));
        verify(moodMapper, times(1)).mapToDto(any(Mood.class));
    }

    @Test
    void createMood_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                moodService.createMood(moodRequestDto)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(moodRepository, never()).save(any(Mood.class));
    }

    @Test
    void getUserMoods_Success() {
        List<Mood> moods = Arrays.asList(testMood);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findAllByUserIdOrderByEntryDateDesc(anyLong())).thenReturn(moods);
        when(moodMapper.mapToDto(any(Mood.class))).thenReturn(moodResponseDto);

        List<MoodResponseDto> result = moodService.getUserMoods();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(moodRepository, times(1)).findAllByUserIdOrderByEntryDateDesc(anyLong());
        verify(moodMapper, times(1)).mapToDto(any(Mood.class));
    }

    @Test
    void updateMood_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findById(anyLong())).thenReturn(Optional.of(testMood));
        when(moodRepository.save(any(Mood.class))).thenReturn(testMood);
        when(moodMapper.mapToDto(any(Mood.class))).thenReturn(moodResponseDto);

        MoodResponseDto result = moodService.updateMood(1L, moodRequestDto);

        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(moodRepository, times(1)).findById(anyLong());
        verify(moodRepository, times(1)).save(any(Mood.class));
    }

    @Test
    void updateMood_MoodNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findById(anyLong())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                moodService.updateMood(1L, moodRequestDto)
        );

        assertEquals(ErrorCode.MOOD_NOT_FOUND, exception.getErrorCode());
        verify(moodRepository, times(1)).findById(anyLong());
        verify(moodRepository, never()).save(any(Mood.class));
    }

    @Test
    void updateMood_UnauthorizedAccess_ThrowsException() {
        User differentUser = User.builder()
                .id(2L)
                .email("different@example.com")
                .build();

        testMood.setUser(differentUser);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findById(anyLong())).thenReturn(Optional.of(testMood));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                moodService.updateMood(1L, moodRequestDto)
        );

        assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
        verify(moodRepository, never()).save(any(Mood.class));
    }

    @Test
    void deleteMood_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findById(anyLong())).thenReturn(Optional.of(testMood));

        assertDoesNotThrow(() -> moodService.deleteMood(1L));

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(moodRepository, times(1)).findById(anyLong());
        verify(moodRepository, times(1)).delete(any(Mood.class));
    }

    @Test
    void deleteMood_MoodNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findById(anyLong())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                moodService.deleteMood(1L)
        );

        assertEquals(ErrorCode.MOOD_NOT_FOUND, exception.getErrorCode());
        verify(moodRepository, times(1)).findById(anyLong());
        verify(moodRepository, never()).delete(any(Mood.class));
    }

    @Test
    void deleteMood_UnauthorizedAccess_ThrowsException() {
        User differentUser = User.builder()
                .id(2L)
                .email("different@example.com")
                .build();

        testMood.setUser(differentUser);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(moodRepository.findById(anyLong())).thenReturn(Optional.of(testMood));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                moodService.deleteMood(1L)
        );

        assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
        verify(moodRepository, never()).delete(any(Mood.class));
    }
}