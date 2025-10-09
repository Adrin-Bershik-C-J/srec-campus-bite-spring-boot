package com.adrin.fc.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import com.adrin.fc.dto.request.ProviderRegisterRequestDto;
import com.adrin.fc.dto.response.PaginatedResponseDto;
import com.adrin.fc.dto.response.ProviderResponseDto;
import com.adrin.fc.dto.response.UserDto;
import com.adrin.fc.enums.Role;
import com.adrin.fc.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderResponseDto> register(@Valid @RequestBody ProviderRegisterRequestDto request) {
        return ResponseEntity.ok(adminService.registerProvider(request));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponseDto<UserDto>> getAllUsers(@RequestParam(required = false) Role role,
            @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(role, pageable));
    }

    @DeleteMapping("/providers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProvider(@PathVariable Long id) {
        adminService.deleteProvider(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Provider deleted successfully");
        return ResponseEntity.ok(response);
    }

}
