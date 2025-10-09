package com.adrin.fc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adrin.fc.dto.request.MenuItemRequestDto;
import com.adrin.fc.dto.response.MenuItemDto;
import com.adrin.fc.service.ProviderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/provider")
@RequiredArgsConstructor
public class ProviderController {
    private final ProviderService providerService;

    @PostMapping("/menu")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<MenuItemDto> createMenuItem(@AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody MenuItemRequestDto request) {
        return ResponseEntity.ok(providerService.createMenuItem(user.getUsername(), request));
    }
}
