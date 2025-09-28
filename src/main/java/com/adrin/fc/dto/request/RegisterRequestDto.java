package com.adrin.fc.dto.request;

import com.adrin.fc.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email; 

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 15, message = "Password must be at between 8 to 15 characters long")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;
}
