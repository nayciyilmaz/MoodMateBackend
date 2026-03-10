package com.moodmate.backend.service;

import com.moodmate.backend.dto.ChangePasswordRequestDto;
import com.moodmate.backend.dto.UserRequestDto;
import com.moodmate.backend.dto.UserResponseDto;
import com.moodmate.backend.entity.User;
import com.moodmate.backend.exception.BusinessException;
import com.moodmate.backend.exception.ErrorCode;
import com.moodmate.backend.mapper.UserMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper mapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private ChangePasswordRequestDto changePasswordRequestDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Yılmaz")
                .lastName("Naycı")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        userRequestDto = UserRequestDto.builder()
                .first_name("Yılmaz")
                .last_name("Naycı")
                .email("test@example.com")
                .password("password123")
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .first_name("Yılmaz")
                .last_name("Naycı")
                .email("test@example.com")
                .token("test-token")
                .build();

        changePasswordRequestDto = ChangePasswordRequestDto.builder()
                .currentPassword("password123")
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();
    }

    private void mockSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
    }

    @Test
    void registerUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(mapper.mapToEntity(any(UserRequestDto.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("test-token");
        when(mapper.mapToDto(any(User.class), anyString())).thenReturn(userResponseDto);

        UserResponseDto result = userService.registerUser(userRequestDto);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken(anyString());
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                userService.registerUser(userRequestDto)
        );

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("test-token");
        when(mapper.mapToDto(any(User.class), anyString())).thenReturn(userResponseDto);

        UserResponseDto result = userService.loginUser("test@example.com", "password123");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(jwtUtil, times(1)).generateToken(anyString());
    }

    @Test
    void loginUser_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                userService.loginUser("test@example.com", "password123")
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void loginUser_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                userService.loginUser("test@example.com", "wrongpassword")
        );

        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void changePassword_Success() {
        mockSecurityContext();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        assertDoesNotThrow(() -> userService.changePassword(changePasswordRequestDto));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void changePassword_UserNotFound_ThrowsException() {
        mockSecurityContext();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                userService.changePassword(changePasswordRequestDto)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_CurrentPasswordIncorrect_ThrowsException() {
        mockSecurityContext();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                userService.changePassword(changePasswordRequestDto)
        );

        assertEquals(ErrorCode.CURRENT_PASSWORD_INCORRECT, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_PasswordsDoNotMatch_ThrowsException() {
        mockSecurityContext();
        changePasswordRequestDto.setConfirmPassword("differentPassword123");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                userService.changePassword(changePasswordRequestDto)
        );

        assertEquals(ErrorCode.PASSWORDS_DO_NOT_MATCH, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_NewPasswordSameAsCurrent_ThrowsException() {
        mockSecurityContext();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", "encodedPassword")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                userService.changePassword(changePasswordRequestDto)
        );

        assertEquals(ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }
}