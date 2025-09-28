package com.adrin.fc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyOtpResponseDto {
    private String message;
    private boolean success;
}
