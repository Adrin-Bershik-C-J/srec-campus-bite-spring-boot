package com.adrin.fc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDto {

    @NotBlank(message = "Roll number is required")
    @Pattern(regexp = "^[0-9]{11}$", message = "Roll number must be exactly 11 digits")
    private String rollNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 15, message = "Password must be at between 8 to 15 characters long")
    private String password;
}
