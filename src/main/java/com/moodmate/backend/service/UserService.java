package com.moodmate.backend.service;

import com.moodmate.backend.dto.ChangePasswordRequestDto;
import com.moodmate.backend.dto.EmailUpdateResponseDto;
import com.moodmate.backend.dto.UpdateEmailRequestDto;
import com.moodmate.backend.dto.UpdateNameRequestDto;
import com.moodmate.backend.dto.UserRequestDto;
import com.moodmate.backend.dto.UserResponseDto;
import com.moodmate.backend.entity.User;
import com.moodmate.backend.exception.BusinessException;
import com.moodmate.backend.exception.ErrorCode;
import com.moodmate.backend.mapper.UserMapper;
import com.moodmate.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper mapper;

    public UserResponseDto registerUser(UserRequestDto dto) {
        log.info("Kayıt isteği: email={}", dto.getEmail());

        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            log.warn("Kayıt başarısız, email zaten mevcut: email={}", dto.getEmail());
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = mapper.mapToEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        User savedUser = userRepository.save(user);

        log.info("Kayıt başarılı: email={}", savedUser.getEmail());

        String token = jwtUtil.generateToken(savedUser.getEmail());
        return mapper.mapToDto(savedUser, token);
    }

    public UserResponseDto loginUser(String email, String password) {
        log.info("Giriş isteği: email={}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("Giriş başarısız, kullanıcı bulunamadı: email={}", email);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Giriş başarısız, hatalı şifre: email={}", email);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        log.info("Giriş başarılı: email={}", email);

        String token = jwtUtil.generateToken(user.getEmail());
        return mapper.mapToDto(user, token);
    }

    public void changePassword(ChangePasswordRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Şifre değiştirme isteği: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            log.warn("Şifre değiştirme başarısız, mevcut şifre hatalı: email={}", email);
            throw new BusinessException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            log.warn("Şifre değiştirme başarısız, şifreler eşleşmiyor: email={}", email);
            throw new BusinessException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            log.warn("Şifre değiştirme başarısız, yeni şifre eskiyle aynı: email={}", email);
            throw new BusinessException(ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        log.info("Şifre değiştirme başarılı: email={}", email);
    }

    public void updateName(UpdateNameRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Ad soyad güncelleme isteği: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getFirstName().equals(dto.getFirst_name()) && user.getLastName().equals(dto.getLast_name())) {
            log.warn("Ad soyad güncelleme başarısız, yeni bilgiler mevcutla aynı: email={}", email);
            throw new BusinessException(ErrorCode.NEW_NAME_SAME_AS_CURRENT);
        }

        user.setFirstName(dto.getFirst_name());
        user.setLastName(dto.getLast_name());
        userRepository.save(user);

        log.info("Ad soyad güncelleme başarılı: email={}", email);
    }

    public EmailUpdateResponseDto updateEmail(UpdateEmailRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("E-posta güncelleme isteği: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (dto.getEmail().equals(user.getEmail())) {
            log.warn("E-posta güncelleme başarısız, yeni e-posta mevcutla aynı: email={}", email);
            throw new BusinessException(ErrorCode.NEW_EMAIL_SAME_AS_CURRENT);
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            log.warn("E-posta güncelleme başarısız, email zaten mevcut: email={}", dto.getEmail());
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        user.setEmail(dto.getEmail());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        log.info("E-posta güncelleme başarılı: email={}", dto.getEmail());

        return EmailUpdateResponseDto.builder()
                .email(user.getEmail())
                .token(token)
                .build();
    }
}