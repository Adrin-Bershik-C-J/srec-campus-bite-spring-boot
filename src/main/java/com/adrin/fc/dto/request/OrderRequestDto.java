package com.adrin.fc.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class OrderRequestDto {
    private List<OrderItemDto> items;

    @Data
    public static class OrderItemDto {
        private Long menuItemId;
        private int quantity;
    }
}