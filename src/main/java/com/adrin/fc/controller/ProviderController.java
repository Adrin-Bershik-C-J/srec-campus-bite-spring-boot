package com.adrin.fc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import com.adrin.fc.dto.request.MenuItemRequestDto;
import com.adrin.fc.dto.response.MenuItemDto;
import com.adrin.fc.dto.response.OrderItemDto;
import com.adrin.fc.dto.response.PaginatedResponseDto;
import com.adrin.fc.enums.MenuTag;
import com.adrin.fc.enums.OrderStatus;
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

    @PutMapping("/menu/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<MenuItemDto> updateMenuItem(@AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @RequestBody MenuItemRequestDto request) {
        return ResponseEntity.ok(providerService.updateMenuItem(user.getUsername(), id, request));
    }

    @PatchMapping("/menu/{id}/toggle")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<MenuItemDto> toggleAvailability(@AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        return ResponseEntity.ok(providerService.toggleAvailability(user.getUsername(), id));
    }

    @GetMapping("/menu")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Page<MenuItemDto>> getAllMenuItems(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) MenuTag tag,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(providerService.getAllMenuItems(user.getUsername(), tag, pageable));
    }

    @DeleteMapping("/menu/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<?> deleteMenuItem(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        providerService.deleteMenuItem(user.getUsername(), id);
        return ResponseEntity.ok().body(Map.of("message", "Menu item deleted successfully", "itemId", id));
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<PaginatedResponseDto<OrderItemDto>> getPlacedOrders(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) MenuTag tag,
            @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity
                .ok(providerService.getOrdersByStatus(user.getUsername(), OrderStatus.PLACED, tag, pageable));
    }

    @GetMapping("/orders/today")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<PaginatedResponseDto<OrderItemDto>> getTodayOrders(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) MenuTag tag,
            @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(providerService.getTodayOrders(user.getUsername(), tag, pageable));
    }

    @GetMapping("/orders/history")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<PaginatedResponseDto<OrderItemDto>> getOrderHistory(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) MenuTag tag,
            @RequestParam(required = false) String date,
            @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(providerService.getAllOrders(user.getUsername(), tag, date, pageable));
    }

    @PatchMapping("/orders/{orderItemId}/ready")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, String>> markOrderReady(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long orderItemId) {
        providerService.markOrderReady(user.getUsername(), orderItemId);
        return ResponseEntity.ok(Map.of("message", "Order item marked as READY"));
    }

    @PatchMapping("/orders/{orderItemId}/done")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, String>> markOrderDone(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long orderItemId) {
        providerService.markOrderDone(user.getUsername(), orderItemId);
        return ResponseEntity.ok(Map.of("message", "Order item marked as DONE"));
    }

    @PatchMapping("/toggle-status")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> toggleProviderStatus(
            @AuthenticationPrincipal UserDetails user) {
        boolean newStatus = providerService.toggleProviderStatus(user.getUsername());
        return ResponseEntity.ok(Map.of(
            "message", "Provider status updated successfully",
            "active", newStatus
        ));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> getProviderStatus(
            @AuthenticationPrincipal UserDetails user) {
        boolean isActive = providerService.getProviderStatus(user.getUsername());
        return ResponseEntity.ok(Map.of("active", isActive));
    }

}
