package com.adrin.fc.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.adrin.fc.dto.response.MenuItemDto;
import com.adrin.fc.dto.response.PaginatedResponseDto;
import com.adrin.fc.dto.response.ProviderResponseDto;
import com.adrin.fc.entity.MenuItem;
import com.adrin.fc.entity.Provider;
import com.adrin.fc.enums.MenuTag;
import com.adrin.fc.exception.ResourceNotFoundException;
import com.adrin.fc.repository.MenuItemRepository;
import com.adrin.fc.repository.ProviderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ProviderRepository providerRepository;
    private final MenuItemRepository menuItemRepository;

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
                .verified(provider.getUser().isVerified())
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
}
