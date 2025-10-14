package com.adrin.fc.dto.response;

import com.adrin.fc.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {

    private Long orderItemId; // OrderItem ID
    private Integer quantity; // Quantity ordered
    private Double subtotal; // Subtotal for this item
    private OrderStatus orderStatus; // Current status of order item

    private Long menuItemId; // MenuItem ID
    private String menuItemName; // MenuItem name
    private Double menuItemPrice; // MenuItem price

    private Long orderId; // Parent Order ID
    private Long providerId; // Provider ID
    private String providerName; // Provider name
}
