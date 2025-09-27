package com.adrin.fc.dto.request;

import com.adrin.fc.enums.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {
    @NotBlank(message = "Roll number is required")
    @Pattern(regexp = "^[0-9]{11}$", message = "Roll number must be exactly 11 digits")
    private String rollNumber;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 15, message = "Password must be at between 8 to 15 characters long")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;
}
