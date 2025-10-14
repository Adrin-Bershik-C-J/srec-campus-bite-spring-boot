package com.adrin.fc.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adrin.fc.dto.request.ForgotPasswordRequestDto;
import com.adrin.fc.dto.request.LoginRequestDto;
import com.adrin.fc.dto.request.RegisterRequestDto;
import com.adrin.fc.dto.request.ResetPasswordRequestDto;
import com.adrin.fc.dto.request.VerifyOtpRequestDto;
import com.adrin.fc.dto.response.LoginResponseDto;
import com.adrin.fc.dto.response.UserDto;
import com.adrin.fc.dto.response.VerifyOtpResponseDto;
import com.adrin.fc.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<LoginResponseDto> verifyEmail(@Valid @RequestBody VerifyOtpRequestDto request) {
        LoginResponseDto response = authService.verifyEmail(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        authService.sendPasswordResetOtp(request.getEmail());
        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent to your email");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        return ResponseEntity.ok(response);
    }
}