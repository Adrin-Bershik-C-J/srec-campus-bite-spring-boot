package com.adrin.fc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProviderRegisterRequestDto {

    @NotBlank(message = "Provider name is required")
    private String providerName;    //eg.,SUBBU MESS

    @NotBlank(message = "Contact is required")
    private String contact;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;
}
