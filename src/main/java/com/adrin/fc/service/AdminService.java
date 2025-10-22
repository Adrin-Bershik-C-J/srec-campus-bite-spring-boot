package com.adrin.fc.service;

import com.adrin.fc.dto.request.ProviderRegisterRequestDto;
import com.adrin.fc.dto.response.PaginatedResponseDto;
import com.adrin.fc.dto.response.ProviderResponseDto;
import com.adrin.fc.dto.response.UserDto;
import com.adrin.fc.entity.Provider;
import com.adrin.fc.entity.User;
import com.adrin.fc.enums.Role;
import com.adrin.fc.exception.InvalidRoleException;
import com.adrin.fc.exception.ResourceNotFoundException;
import com.adrin.fc.repository.ProviderRepository;
import com.adrin.fc.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProviderRepository providerRepository;

    public ProviderResponseDto registerProvider(ProviderRegisterRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DataIntegrityViolationException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PROVIDER);
        user.setVerified(true);

        User savedUser = userRepository.save(user);

        Provider provider = new Provider();
        provider.setProviderName(request.getProviderName());
        provider.setContact(request.getContact());
        provider.setUser(savedUser);
        provider.setActive(true);

        Provider savedProvider = providerRepository.save(provider);

        return ProviderResponseDto.builder()
                .providerId(savedProvider.getId())
                .providerName(savedProvider.getProviderName())
                .contact(savedProvider.getContact())
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .active(savedProvider.isActive())
                .verified(savedUser.isVerified())
                .build();
    }

    public PaginatedResponseDto<UserDto> getAllUsers(Role role, Pageable pageable) {
        Page<User> usersPage;

        if (role != null) {
            usersPage = userRepository.findByRoleAndVerified(role, true, pageable);
        } else {
            usersPage = userRepository.findByVerified(true, pageable);
        }
        Page<UserDto> dtoPage = usersPage.map(this::convertToDto);

        return new PaginatedResponseDto<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isLast());
    }

    @Transactional
    public void deleteProvider(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + providerId));

        User user = provider.getUser();

        if (user == null || user.getRole() != Role.PROVIDER) {
            throw new InvalidRoleException("Only providers can be deleted by admin");
        }

        providerRepository.delete(provider);

        userRepository.delete(user);
    }

    private UserDto convertToDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}
