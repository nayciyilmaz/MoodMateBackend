package com.moodmate.backend.service;

import com.moodmate.backend.dto.ChangePasswordRequestDto;
import com.moodmate.backend.dto.UserRequestDto;
import com.moodmate.backend.dto.UserResponseDto;
import com.moodmate.backend.entity.User;
import com.moodmate.backend.exception.BusinessException;
import com.moodmate.backend.exception.ErrorCode;
import com.moodmate.backend.mapper.UserMapper;
import com.moodmate.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper mapper;

    public UserResponseDto registerUser(UserRequestDto dto) {
        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = mapper.mapToEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getEmail());
        return mapper.mapToDto(savedUser, token);
    }

    public UserResponseDto loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return mapper.mapToDto(user, token);
    }

    public void changePassword(ChangePasswordRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}