package com.moodmate.backend.controller;

import com.moodmate.backend.dto.MoodRequestDto;
import com.moodmate.backend.dto.MoodResponseDto;
import com.moodmate.backend.service.MoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/moods")
@RequiredArgsConstructor
public class MoodController {
    //Giriş yapmış kullanıcının mood eklemesi ve moodlarını görmesi

    private final MoodService moodService;

    @PostMapping
    public ResponseEntity<MoodResponseDto> addMood(@RequestBody @Valid MoodRequestDto dto) {
        return ResponseEntity.ok(moodService.createMood(dto));
    }

    @GetMapping
    public ResponseEntity<List<MoodResponseDto>> getMyMoods() {
        return ResponseEntity.ok(moodService.getUserMoods());
    }
}