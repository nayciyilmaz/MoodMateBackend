package com.moodmate.backend.controller;

import com.moodmate.backend.dto.ChangePasswordRequestDto;
import com.moodmate.backend.dto.EmailUpdateResponseDto;
import com.moodmate.backend.dto.LoginRequestDto;
import com.moodmate.backend.dto.UpdateEmailRequestDto;
import com.moodmate.backend.dto.UpdateNameRequestDto;
import com.moodmate.backend.dto.UserRequestDto;
import com.moodmate.backend.dto.UserResponseDto;
import com.moodmate.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/auth/register")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody @Valid UserRequestDto user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<UserResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequest) {
        UserResponseDto user = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/api/user/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequestDto request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/user/update-name")
    public ResponseEntity<Void> updateName(@RequestBody @Valid UpdateNameRequestDto request) {
        userService.updateName(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/user/update-email")
    public ResponseEntity<EmailUpdateResponseDto> updateEmail(@RequestBody @Valid UpdateEmailRequestDto request) {
        return ResponseEntity.ok(userService.updateEmail(request));
    }
}