package com.adrin.fc.service;

import com.adrin.fc.dto.request.RegisterRequestDto;
import com.adrin.fc.dto.response.RegisterResponseDto;
import com.adrin.fc.entity.User;
import com.adrin.fc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponseDto registerUser(RegisterRequestDto request) {
        if (userRepository.findByRollNumber(request.getRollNumber()).isPresent()) {
            throw new RuntimeException("User with this roll number already exists");
        }

        User user = new User();
        user.setRollNumber(request.getRollNumber());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        return new RegisterResponseDto(
                savedUser.getId(),
                savedUser.getRollNumber(),
                savedUser.getName(),
                savedUser.getRole());
    }
}
