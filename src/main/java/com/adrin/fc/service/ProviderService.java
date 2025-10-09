package com.adrin.fc.service;

import org.springframework.stereotype.Service;

import com.adrin.fc.dto.request.MenuItemRequestDto;
import com.adrin.fc.dto.response.MenuItemDto;
import com.adrin.fc.entity.MenuItem;
import com.adrin.fc.entity.Provider;
import com.adrin.fc.entity.User;
import com.adrin.fc.exception.ResourceNotFoundException;
import com.adrin.fc.repository.MenuItemRepository;
import com.adrin.fc.repository.ProviderRepository;
import com.adrin.fc.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

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
