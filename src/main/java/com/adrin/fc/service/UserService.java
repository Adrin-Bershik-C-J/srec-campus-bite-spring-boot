package com.adrin.fc.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adrin.fc.dto.request.OrderRequestDto;
import com.adrin.fc.dto.response.MenuItemDto;
import com.adrin.fc.dto.response.OrderHistoryItemDto;
import com.adrin.fc.dto.response.OrderHistoryResponseDto;
import com.adrin.fc.dto.response.PaginatedResponseDto;
import com.adrin.fc.dto.response.PaymentIntentResponseDto;
import com.adrin.fc.dto.response.ProviderResponseDto;
import com.adrin.fc.entity.MenuItem;
import com.adrin.fc.entity.Order;
import com.adrin.fc.entity.OrderItem;
import com.adrin.fc.entity.Provider;
import com.adrin.fc.entity.User;
import com.adrin.fc.enums.MenuTag;
import com.adrin.fc.enums.OrderStatus;
import com.adrin.fc.exception.InvalidOperationException;
import com.adrin.fc.exception.PaymentProcessingException;
import com.adrin.fc.exception.ResourceNotFoundException;
import com.adrin.fc.repository.MenuItemRepository;
import com.adrin.fc.repository.OrderItemRepository;
import com.adrin.fc.repository.OrderRepository;
import com.adrin.fc.repository.ProviderRepository;
import com.adrin.fc.repository.UserRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ProviderRepository providerRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public List<ProviderResponseDto> getAllProviders() {
        List<Provider> providers = providerRepository.findAll();
        return providers.stream().map(this::toDto).collect(Collectors.toList());
    }

    public PaginatedResponseDto<MenuItemDto> getAllMenuItems(Long providerId, MenuTag tag, Pageable pageable) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        Page<MenuItem> items;
        if (tag != null) {
            items = menuItemRepository.findByProviderAndTag(provider, tag, pageable);
        } else {
            items = menuItemRepository.findByProvider(provider, pageable);
        }
        List<MenuItemDto> menuItems = items.stream().map(this::toDto).collect(Collectors.toList());
        return new PaginatedResponseDto<>(
                menuItems,
                items.getNumber(),
                items.getSize(),
                items.getTotalElements(),
                items.getTotalPages(),
                items.isLast());
    }

    public PaymentIntentResponseDto createPaymentIntent(OrderRequestDto request) {
        try {
            // Validate that all menu items are available
            for (var itemReq : request.getItems()) {
                MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Menu item not found: " + itemReq.getMenuItemId()));

                if (!menuItem.isAvailable()) {
                    throw new InvalidOperationException(
                            "Menu item '" + menuItem.getItemName() + "' is currently unavailable");
                }
            }

            double totalAmount = calculateTotal(request);
            
            System.out.println("Creating payment intent for total: " + totalAmount);
            
            // For testing purposes, simulate successful payment intent
            // In production, you would integrate with a payment gateway that supports small amounts
            String simulatedClientSecret = "pi_test_" + System.currentTimeMillis();
            return new PaymentIntentResponseDto(simulatedClientSecret);
        } catch (Exception e) {
            System.err.println("Error creating payment intent: " + e.getMessage());
            e.printStackTrace();
            throw new PaymentProcessingException("Error creating payment intent: " + e.getMessage());
        }
    }

    @Transactional
    public void placeOrder(String email, OrderRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Ensure all menu items are available
        for (var itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Menu item not found: " + itemReq.getMenuItemId()));

            if (!menuItem.isAvailable()) {
                throw new InvalidOperationException(
                        "Menu item '" + menuItem.getItemName() + "' is currently unavailable");
            }
        }

        // Generate unique session ID for this checkout
        String orderSessionId = "SESSION-" + System.currentTimeMillis() + "-" + user.getId();
        
        // Group items by provider
        Map<Provider, List<OrderRequestDto.OrderItemDto>> itemsByProvider = request.getItems().stream()
                .collect(Collectors.groupingBy(itemReq -> {
                    MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemReq.getMenuItemId()));
                    return menuItem.getProvider();
                }));

        // Create separate order for each provider
        for (Map.Entry<Provider, List<OrderRequestDto.OrderItemDto>> entry : itemsByProvider.entrySet()) {
            Provider provider = entry.getKey();
            List<OrderRequestDto.OrderItemDto> providerItems = entry.getValue();

            // Calculate total for this provider
            double providerTotal = providerItems.stream()
                    .mapToDouble(itemReq -> {
                        MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemReq.getMenuItemId()));
                        return menuItem.getPrice() * itemReq.getQuantity();
                    })
                    .sum();

            // Generate provider-specific token
            String token = generateProviderDailyToken(provider);
            String qrData = "QR-" + UUID.randomUUID().toString().substring(0, 10);

            Order order = new Order();
            order.setUser(user);
            order.setTotalPrice(providerTotal);
            order.setTokenNumber(token);
            order.setQrCodeData(qrData);
            order.setOrderSessionId(orderSessionId);

            List<OrderItem> orderItems = providerItems.stream().map(itemReq -> {
                MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemReq.getMenuItemId()));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setMenuItem(menuItem);
                orderItem.setQuantity(itemReq.getQuantity());
                orderItem.setSubtotal(menuItem.getPrice() * itemReq.getQuantity());
                orderItem.setProvider(provider);
                orderItem.setOrderStatus(OrderStatus.PLACED);
                return orderItem;
            }).collect(Collectors.toList());

            order.setOrderItems(orderItems);
            orderRepository.save(order);
        }
    }

    @Transactional
    public void markOrderItemDone(String email, Long orderItemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        System.out.println("Order item status: " + orderItem.getOrderStatus());
        System.out.println("Order item ID: " + orderItemId);
        System.out.println("User ID: " + user.getId());
        System.out.println("Order user ID: " + orderItem.getOrder().getUser().getId());

        if (!orderItem.getOrder().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You cannot update someone else's order");
        }

        if (orderItem.getOrderStatus() == OrderStatus.DONE) {
            throw new InvalidOperationException("Order already DONE");
        }

        if (orderItem.getOrderStatus() != OrderStatus.READY) {
            throw new InvalidOperationException("Only READY orders can be marked as DONE. Current status: " + orderItem.getOrderStatus());
        }

        orderItem.setOrderStatus(OrderStatus.DONE);
        orderItemRepository.save(orderItem);
    }

    public Page<OrderHistoryResponseDto> getUserOrderHistory(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Page<Order> orders = orderRepository.findByUserAndAllOrderItemsDoneOrderByCreatedAtDesc(user, pageable);

        return orders.map(order -> {
            List<OrderHistoryItemDto> itemDtos = order.getOrderItems().stream()
                    .map(oi -> new OrderHistoryItemDto(
                            oi.getId(),
                            oi.getMenuItem().getId(),
                            oi.getMenuItem().getItemName(),
                            oi.getQuantity(),
                            oi.getOrderStatus()))
                    .toList();

            String sessionId = order.getOrderSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = "LEGACY-" + order.getId();
            }

            return new OrderHistoryResponseDto(
                    order.getId(),
                    order.getTokenNumber(),
                    order.getTotalPrice(),
                    order.getCreatedAt(),
                    sessionId,
                    itemDtos);
        });
    }

    public Page<OrderHistoryResponseDto> getUserPendingOrders(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Page<Order> orders = orderRepository.findByUserAndOrderItemsOrderStatusInOrderByCreatedAtDesc(user, List.of(OrderStatus.PLACED, OrderStatus.READY), pageable);

        return orders.map(order -> {
            List<OrderHistoryItemDto> itemDtos = order.getOrderItems().stream()
                    .filter(oi -> oi.getOrderStatus() == OrderStatus.PLACED || oi.getOrderStatus() == OrderStatus.READY)
                    .map(oi -> new OrderHistoryItemDto(
                            oi.getId(),
                            oi.getMenuItem().getId(),
                            oi.getMenuItem().getItemName(),
                            oi.getQuantity(),
                            oi.getOrderStatus()))
                    .toList();

            String sessionId = order.getOrderSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = "LEGACY-" + order.getId();
            }

            return new OrderHistoryResponseDto(
                    order.getId(),
                    order.getTokenNumber(),
                    order.getTotalPrice(),
                    order.getCreatedAt(),
                    sessionId,
                    itemDtos);
        });
    }

    private double calculateTotal(OrderRequestDto request) {
        return request.getItems().stream()
                .mapToDouble(i -> {
                    MenuItem item = menuItemRepository.findById(i.getMenuItemId())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Menu item not found: " + i.getMenuItemId()));
                    return item.getPrice() * i.getQuantity();
                })
                .sum();
    }

    private ProviderResponseDto toDto(Provider provider) {
        return ProviderResponseDto.builder()
                .providerName(provider.getProviderName())
                .providerId(provider.getId())
                .contact(provider.getContact())
                .userId(provider.getUser().getId())
                .name(provider.getUser().getName())
                .email(provider.getUser().getEmail())
                .role(provider.getUser().getRole())
                .active(provider.isActive())
                .build();
    }

    private MenuItemDto toDto(MenuItem item) {
        return new MenuItemDto(
                item.getId(),
                item.getItemName(),
                item.getPrice(),
                item.isAvailable(),
                item.getTag(),
                item.getProvider().getId(),
                item.getProvider().getProviderName());
    }

    private String generateProviderDailyToken(Provider provider) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // Get count of orders for this provider TODAY only
        long providerOrderCount = orderRepository.countByProviderAndCreatedAtBetween(provider, startOfDay, endOfDay);
        int nextNumber = (int) providerOrderCount + 1;
        
        System.out.println("Provider: " + provider.getProviderName() + ", Order count today: " + providerOrderCount + ", Next number: " + nextNumber);
        
        return String.format("P%d-O%03d", provider.getId(), nextNumber);
    }
}
