package com.adrin.fc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import com.adrin.fc.dto.response.MenuItemDto;
import com.adrin.fc.dto.response.PaginatedResponseDto;
import com.adrin.fc.dto.response.ProviderResponseDto;
import com.adrin.fc.enums.MenuTag;
import com.adrin.fc.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/providers")
    public ResponseEntity<List<ProviderResponseDto>> getAllProviders() {
        return ResponseEntity.ok(userService.getAllProviders());
    }

    @GetMapping("/providers/{providerId}/menu")
    public ResponseEntity<PaginatedResponseDto<MenuItemDto>> getProviderMenu(@PathVariable Long providerId,
            @RequestParam(required = false) MenuTag tag,
            @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllMenuItems(providerId, tag, pageable));
    }
}
