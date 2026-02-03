package com.moodmate.backend.controller;

import com.moodmate.backend.dto.AdviceResponseDto;
import com.moodmate.backend.service.AdviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advice")
@RequiredArgsConstructor
public class AdviceController {

    private final AdviceService adviceService;

    @PostMapping("/generate")
    public ResponseEntity<AdviceResponseDto> generateAdvice() {
        return ResponseEntity.ok(adviceService.generateAdvice());
    }

    @GetMapping("/latest")
    public ResponseEntity<AdviceResponseDto> getLatestAdvice() {
        return ResponseEntity.ok(adviceService.getLatestAdvice());
    }
}