package com.adrin.fc.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.adrin.fc.dto.request.LoginRequestDto;
import com.adrin.fc.dto.response.LoginResponseDto;
import com.adrin.fc.entity.User;
import com.adrin.fc.repository.UserRepository;
import com.adrin.fc.security.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDto login(LoginRequestDto request) {

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with email: " + request.getEmail()));

        if (!user.isVerified()) {
            throw new RuntimeException("Email not verified. Please verify before login.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponseDto(token, user.getRole().name(), user.getEmail(), user.getName());

    }

}
