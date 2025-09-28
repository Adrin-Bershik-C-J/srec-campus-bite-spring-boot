package com.adrin.fc.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adrin.fc.dto.request.LoginRequestDto;
import com.adrin.fc.dto.request.RegisterRequestDto;
import com.adrin.fc.dto.response.LoginResponseDto;
import com.adrin.fc.dto.response.UserDto;
import com.adrin.fc.entity.User;
import com.adrin.fc.enums.Role;
import com.adrin.fc.exception.EmailNotVerifiedException;
import com.adrin.fc.exception.InvalidOtpException;
import com.adrin.fc.exception.InvalidRoleException;
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
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto request) {

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with email: " + request.getEmail()));

        if (!user.isVerified()) {
            throw new EmailNotVerifiedException("Email not verified. Please verify before login.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponseDto(token, user.getRole().name(), user.getEmail(), user.getName());

    }

    public UserDto register(RegisterRequestDto request) {
        if (request.getRole() != Role.USER) {
            throw new InvalidRoleException("Only USER role can self-register");
        }

        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (existingUser != null) {
            if (existingUser.isVerified()) {
                // Already verified → reject
                throw new DataIntegrityViolationException("Email already in use");
            } else {
                // Not verified → resend OTP & update password if needed
                existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
                existingUser.setName(request.getName());
                userRepository.save(existingUser);

                otpService.sendOtp(existingUser.getEmail());
                return new UserDto(existingUser.getId(), existingUser.getEmail(), existingUser.getName(),
                        existingUser.getRole());
            }
        }

        // First time registration
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setVerified(false);

        User savedUser = userRepository.save(user);
        otpService.sendOtp(savedUser.getEmail());

        return new UserDto(savedUser.getId(), savedUser.getEmail(), savedUser.getName(), savedUser.getRole());
    }

    public void verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (otpService.verifyOtp(email, otp)) {
            user.setVerified(true);
            userRepository.save(user);
        } else {
            throw new InvalidOtpException("Invalid OTP");
        }
    }

}
