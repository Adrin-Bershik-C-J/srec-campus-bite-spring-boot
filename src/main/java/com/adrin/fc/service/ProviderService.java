package com.adrin.fc.service;

import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import com.adrin.fc.dto.request.MenuItemRequestDto;
import com.adrin.fc.dto.response.OrderItemDto;
import com.adrin.fc.dto.response.MenuItemDto;
import com.adrin.fc.dto.response.PaginatedResponseDto;
import com.adrin.fc.entity.MenuItem;
import com.adrin.fc.entity.OrderItem;
import com.adrin.fc.entity.Provider;
import com.adrin.fc.entity.User;
import com.adrin.fc.enums.MenuTag;
import com.adrin.fc.enums.OrderStatus;
import com.adrin.fc.exception.InvalidOperationException;
import com.adrin.fc.exception.ResourceNotFoundException;
import com.adrin.fc.repository.MenuItemRepository;
import com.adrin.fc.repository.OrderItemRepository;
import com.adrin.fc.repository.ProviderRepository;
import com.adrin.fc.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    private Provider getProviderByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return providerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found for user: " + email));
    }

    public MenuItemDto createMenuItem(String email, MenuItemRequestDto request) {
        Provider provider = getProviderByEmail(email);

        MenuItem item = new MenuItem();
        item.setItemName(request.getItemName());
        item.setPrice(request.getPrice());
        item.setTag(request.getTag());
        item.setAvailable(true);
        item.setProvider(provider);

        MenuItem saved = menuItemRepository.save(item);
        return toDto(saved);
    }

    public Page<MenuItemDto> getAllMenuItems(String email, MenuTag tag, Pageable pageable) {
        Provider provider = getProviderByEmail(email);

        Page<MenuItem> items;
        if (tag != null) {
            items = menuItemRepository.findByProviderAndTag(provider, tag, pageable);
        } else {
            items = menuItemRepository.findByProvider(provider, pageable);
        }

        return items.map(this::toDto);
    }

    @Transactional
    public MenuItemDto updateMenuItem(String email, Long id, MenuItemRequestDto request) {
        Provider provider = getProviderByEmail(email);

        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!item.getProvider().getId().equals(provider.getId())) {
            throw new AccessDeniedException("Unauthorized to update this item");
        }

        if (request.getItemName() != null && !request.getItemName().isBlank()) {
            item.setItemName(request.getItemName());
        }
        if (request.getPrice() != null && request.getPrice() > 0) {
            item.setPrice(request.getPrice());
        }
        if (request.getTag() != null && !request.getTag().name().isBlank()) {
            item.setTag(request.getTag());
        }

        MenuItem updated = menuItemRepository.save(item);
        return toDto(updated);
    }

    @Transactional
    public void deleteMenuItem(String email, Long itemId) {
        Provider provider = getProviderByEmail(email);

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + itemId));

        if (!item.getProvider().getId().equals(provider.getId())) {
            throw new AccessDeniedException("Unauthorized to delete this item");
        }

        menuItemRepository.delete(item);
    }

    @Transactional
    public MenuItemDto toggleAvailability(String email, Long itemId) {
        Provider provider = getProviderByEmail(email);

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!item.getProvider().getId().equals(provider.getId())) {
            throw new AccessDeniedException("Unauthorized to modify this item");
        }

        item.setAvailable(!item.isAvailable());
        MenuItem updated = menuItemRepository.save(item);
        return toDto(updated);
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDto<OrderItemDto> getOrdersByStatus(String email, OrderStatus status, MenuTag tag,
            Pageable pageable) {
        Provider provider = getProviderByEmail(email);
        Page<OrderItem> items;
        if (tag != null) {
            items = orderItemRepository.findByProviderAndOrderStatusAndMenuItemTag(provider, status, tag, pageable);
        } else {
            items = orderItemRepository.findByProviderAndOrderStatus(provider, status, pageable);
        }
        List<OrderItemDto> dtos = items.stream().map(this::toDto).collect(Collectors.toList());
        return new PaginatedResponseDto<>(
                dtos,
                items.getNumber(),
                items.getSize(),
                items.getTotalElements(),
                items.getTotalPages(),
                items.isLast());
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDto<OrderItemDto> getAllOrders(String email, MenuTag tag, Pageable pageable) {
        Provider provider = getProviderByEmail(email);
        Page<OrderItem> items;
        if (tag != null) {
            items = orderItemRepository.findByProviderAndMenuItemTag(provider, tag, pageable);
        } else {
            items = orderItemRepository.findByProvider(provider, pageable);
        }
        List<OrderItemDto> dtos = items.stream().map(this::toDto).collect(Collectors.toList());
        return new PaginatedResponseDto<>(
                dtos,
                items.getNumber(),
                items.getSize(),
                items.getTotalElements(),
                items.getTotalPages(),
                items.isLast());
    }

    @Transactional
    public void markOrderReady(String email, Long orderItemId) {
        Provider provider = getProviderByEmail(email);
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + orderItemId));

        if (!item.getProvider().getId().equals(provider.getId())) {
            throw new AccessDeniedException("Unauthorized to update this order item");
        }

        if (item.getOrderStatus() == OrderStatus.READY) {
            throw new InvalidOperationException("Order already READY");
        }

        if (item.getOrderStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Only PLACED items can be marked READY");
        }

        item.setOrderStatus(OrderStatus.READY);
        orderItemRepository.save(item);
    }

    private OrderItemDto toDto(OrderItem item) {
        return new OrderItemDto(
                item.getId(),
                item.getQuantity(),
                item.getSubtotal(),
                item.getOrderStatus(),
                item.getMenuItem().getId(),
                item.getMenuItem().getItemName(),
                item.getMenuItem().getPrice(),
                item.getOrder().getId(),
                item.getProvider().getId(),
                item.getProvider().getProviderName());
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

}
