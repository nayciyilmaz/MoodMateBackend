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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdviceService {

    private final UserRepository userRepository;
    private final MoodRepository moodRepository;
    private final AdviceRepository adviceRepository;
    private final AdviceMapper adviceMapper;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent";

    public AdviceService(
            UserRepository userRepository,
            MoodRepository moodRepository,
            AdviceRepository adviceRepository,
            AdviceMapper adviceMapper
    ) {
        this.userRepository = userRepository;
        this.moodRepository = moodRepository;
        this.adviceRepository = adviceRepository;
        this.adviceMapper = adviceMapper;
        this.restTemplate = new RestTemplate();
    }

    public AdviceResponseDto generateAdvice() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Mood> recentMoods = moodRepository.findAllByUserIdOrderByEntryDateDesc(user.getId());

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<Mood> lastThreeDaysMoods = recentMoods.stream()
                .filter(mood -> mood.getEntryDate().isAfter(threeDaysAgo))
                .collect(Collectors.toList());

        if (lastThreeDaysMoods.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_MOOD_DATA);
        }

        String prompt = buildPrompt(lastThreeDaysMoods);
        String adviceText = callGeminiApi(prompt);

        Advice advice = Advice.builder()
                .user(user)
                .advice(adviceText)
                .build();

        Advice savedAdvice = adviceRepository.save(advice);
        return adviceMapper.mapToDto(savedAdvice);
    }

    public AdviceResponseDto getLatestAdvice() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Advice advice = adviceRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_ADVICE_FOUND));

        return adviceMapper.mapToDto(advice);
    }

    private String buildPrompt(List<Mood> moods) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sen bir empati sahibi, destekleyici ve anlayışlı bir ruh sağlığı danışmanı asistanısın. ")
                .append("Kullanıcı bir akıllı günlük uygulaması kullanıyor ve son birkaç günlük duygularını kaydetmiş. ")
                .append("Aşağıda son 3 gün içindeki günlük kayıtları var. Her kayıtta emoji, puan (1-10 arası) ve kullanıcının yazdığı not bulunuyor.\n\n");

        sb.append("Günlük Kayıtlar:\n");
        for (Mood mood : moods) {
            sb.append("- Emoji: ").append(mood.getEmoji())
                    .append(" | Puan: ").append(mood.getScore()).append("/10")
                    .append(" | Tarih: ").append(mood.getEntryDate().toLocalDate())
                    .append(" | Not: ").append(mood.getNote())
                    .append("\n");
        }

        sb.append("\nYukarıdaki günlük kayıtlarını dikkatlice oku ve şunları yap:\n")
                .append("1. Kullanıcının son 3 gündeki genel ruh halini kısaca özetle.\n")
                .append("2. Notlarında seni öne çıkan olumlu veya olumsuz kalıpları belirle.\n")
                .append("3. Kullanıcıya nazik, destekleyici ve yapıcı tavsiyelerde bulun.\n")
                .append("4. Eğer olumsuz duygular yoğun görünüyorsa, profesyonel destek almalarını nazikçe öner.\n\n")
                .append("Cevabını Türkçe yaz. Samimi, sıcak ve motivasyonel bir ton kullan. 2-3 paragraf olsun, çok uzun olmasın.");

        return sb.toString();
    }

    private String callGeminiApi(String prompt) {
        String url = GEMINI_URL + "?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(
                    url,
                    httpEntity,
                    Map.class
            );

            if (response == null) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
            }

            Object candidatesObj = response.get("candidates");
            if (candidatesObj == null) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) candidatesObj;
            if (candidates.isEmpty()) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
            }

            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> candidateContent = (Map<String, Object>) firstCandidate.get("content");

            if (candidateContent == null) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
            }

            List<Map<String, Object>> responseParts = (List<Map<String, Object>>) candidateContent.get("parts");

            if (responseParts == null || responseParts.isEmpty()) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
            }

            String text = (String) responseParts.get(0).get("text");
            if (text == null || text.isBlank()) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
            }

            return text;

        } catch (BusinessException e) {
            throw e;
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new BusinessException(ErrorCode.AI_RATE_LIMIT);
        } catch (HttpClientErrorException e) {
            System.err.println("Gemini API HTTP Hatası: " + e.getStatusCode() + " - " + e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
        } catch (Exception e) {
            System.err.println("Gemini API Hatası: " + e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
        }
    }
}