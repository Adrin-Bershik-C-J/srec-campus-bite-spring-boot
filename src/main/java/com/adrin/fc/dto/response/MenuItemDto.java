package com.adrin.fc.dto.response;

import com.adrin.fc.enums.MenuTag;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MenuItemDto {
    private Long id;
    private String itemName;
    private double price;
    private boolean available;
    private MenuTag tag;
    private Long providerId;
    private String providerName;
}
