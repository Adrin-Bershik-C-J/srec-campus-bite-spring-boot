package com.adrin.fc.dto.response;

import com.adrin.fc.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistoryItemDto {
    private Long orderItemId;
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private OrderStatus status;
}
