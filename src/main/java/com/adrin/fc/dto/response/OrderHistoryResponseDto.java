package com.adrin.fc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistoryResponseDto {
    private Long orderId;
    private String tokenNumber;
    private Double totalAmount;
    private LocalDateTime placedAt;
    private String orderSessionId;
    private List<OrderHistoryItemDto> items;
}
