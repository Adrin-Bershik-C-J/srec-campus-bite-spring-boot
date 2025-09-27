package com.adrin.fc.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.adrin.fc.dto.request.LoginRequestDto;
import com.adrin.fc.dto.response.LoginResponseDto;
import com.adrin.fc.entity.User;
import com.adrin.fc.repository.UserRepository;
import com.adrin.fc.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDto login(LoginRequestDto request) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getRollNumber(), request.getPassword()));

        User user = userRepository.findByRollNumber(request.getRollNumber())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getRollNumber(), user.getRole().name());

        return new LoginResponseDto(token, user.getRole().name(), user.getRollNumber(), user.getName());

    }

}
