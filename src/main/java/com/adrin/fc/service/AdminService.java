package com.adrin.fc.service;

import com.adrin.fc.dto.request.RegisterRequestDto;
import com.adrin.fc.dto.response.PaginatedResponseDto;
import com.adrin.fc.dto.response.UserDto;
import com.adrin.fc.entity.User;
import com.adrin.fc.enums.Role;
import com.adrin.fc.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto registerUser(RegisterRequestDto request) {
        if (userRepository.findByRollNumber(request.getRollNumber()).isPresent()) {
            throw new DataIntegrityViolationException("User with this roll number already exists");
        }

        User user = new User();
        user.setRollNumber(request.getRollNumber());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        if (savedUser == null) {
            throw new EntityNotFoundException("Failed to save user");
        }

        return new UserDto(
                savedUser.getId(),
                savedUser.getRollNumber(),
                savedUser.getName(),
                savedUser.getRole());
    }

    public PaginatedResponseDto<UserDto> getAllUsers(Role role, Pageable pageable) {
        Page<User> usersPage;

        if (role != null) {
            // Filter by role
            usersPage = userRepository.findByRole(role, pageable);
        } else {
            // No filter, get all
            usersPage = userRepository.findAll(pageable);
        }

        // Convert User -> UserDto
        Page<UserDto> dtoPage = usersPage.map(this::convertToDto);

        return new PaginatedResponseDto<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isLast());
    }

    private UserDto convertToDto(User user) {
        return new UserDto(user.getId(), user.getRollNumber(), user.getName(), user.getRole());
    }
}
