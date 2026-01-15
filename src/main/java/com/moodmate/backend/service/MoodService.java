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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoodService {
    //Kullanıcıyı tespit eder, moodları ona özel olarak kaydeder.

    private final MoodRepository moodRepository;
    private final UserRepository userRepository;
    private final MoodMapper moodMapper;

    public MoodResponseDto createMood(MoodRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Mood mood = moodMapper.mapToEntity(dto);
        mood.setUser(user);

        return moodMapper.mapToDto(moodRepository.save(mood));
    }

    public List<MoodResponseDto> getUserMoods() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return moodRepository.findAllByUserIdOrderByEntryDateDesc(user.getId())
                .stream()
                .map(moodMapper::mapToDto)
                .collect(Collectors.toList());
    }
}