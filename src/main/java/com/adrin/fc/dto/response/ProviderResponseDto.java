package com.adrin.fc.dto.response;

import com.adrin.fc.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponseDto {

    private Long providerId;
    private String providerName;
    private String contact;

    private Long userId;
    private String name;
    private String email;
    private Role role;
    private boolean active;
    private boolean verified;
}
