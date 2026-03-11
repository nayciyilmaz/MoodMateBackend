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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoodService {

    private static final Logger log = LoggerFactory.getLogger(MoodService.class);

    private final MoodRepository moodRepository;
    private final UserRepository userRepository;
    private final MoodMapper moodMapper;

    public MoodResponseDto createMood(MoodRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Mood oluşturma isteği: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Mood mood = moodMapper.mapToEntity(dto);
        mood.setUser(user);

        MoodResponseDto response = moodMapper.mapToDto(moodRepository.save(mood));
        log.info("Mood oluşturuldu: email={}, moodId={}", email, response.getId());

        return response;
    }

    public List<MoodResponseDto> getUserMoods() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Mood listesi isteği: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<MoodResponseDto> moods = moodRepository.findAllByUserIdOrderByEntryDateDesc(user.getId())
                .stream()
                .map(moodMapper::mapToDto)
                .collect(Collectors.toList());

        log.info("Mood listesi döndürüldü: email={}, count={}", email, moods.size());

        return moods;
    }

    public MoodResponseDto updateMood(Long moodId, MoodRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Mood güncelleme isteği: email={}, moodId={}", email, moodId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Mood mood = moodRepository.findById(moodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOOD_NOT_FOUND));

        if (!mood.getUser().getId().equals(user.getId())) {
            log.warn("Yetkisiz mood güncelleme girişimi: email={}, moodId={}", email, moodId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        mood.setEmoji(dto.getEmoji());
        mood.setScore(dto.getScore());
        mood.setNote(dto.getNote());
        if (dto.getEntryDate() != null) {
            mood.setEntryDate(dto.getEntryDate());
        }

        MoodResponseDto response = moodMapper.mapToDto(moodRepository.save(mood));
        log.info("Mood güncellendi: email={}, moodId={}", email, moodId);

        return response;
    }

    public void deleteMood(Long moodId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Mood silme isteği: email={}, moodId={}", email, moodId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Mood mood = moodRepository.findById(moodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOOD_NOT_FOUND));

        if (!mood.getUser().getId().equals(user.getId())) {
            log.warn("Yetkisiz mood silme girişimi: email={}, moodId={}", email, moodId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        moodRepository.delete(mood);
        log.info("Mood silindi: email={}, moodId={}", email, moodId);
    }
}