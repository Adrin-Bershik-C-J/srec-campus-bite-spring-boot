package com.adrin.fc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.data.domain.Page;

import com.adrin.fc.dto.request.OrderRequestDto;
import com.adrin.fc.dto.response.MenuItemDto;
import com.adrin.fc.dto.response.OrderHistoryResponseDto;
import com.adrin.fc.dto.response.PaginatedResponseDto;
import com.adrin.fc.dto.response.PaymentIntentResponseDto;
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

    @PostMapping("/payment/create-intent")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentIntentResponseDto> createPaymentIntent(@RequestBody OrderRequestDto request) {
        return ResponseEntity.ok(userService.createPaymentIntent(request));
    }

    @PostMapping("/orders/place")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> placeOrder(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody OrderRequestDto request) {
        try {
            System.out.println("Placing order for user: " + user.getUsername());
            System.out.println("Order request: " + request);
            userService.placeOrder(user.getUsername(), request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Order placed successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error placing order: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PatchMapping("/orders/{orderItemId}/complete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> markOrderItemDone(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long orderItemId) {

        userService.markOrderItemDone(user.getUsername(), orderItemId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Order item marked as DONE");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<OrderHistoryResponseDto>> getOrderHistory(
            @AuthenticationPrincipal UserDetails user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getUserOrderHistory(user.getUsername(), pageable));
    }

    @GetMapping("/orders/ready")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<OrderHistoryResponseDto>> getReadyOrders(
            @AuthenticationPrincipal UserDetails user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getUserReadyOrders(user.getUsername(), pageable));
    }

}
