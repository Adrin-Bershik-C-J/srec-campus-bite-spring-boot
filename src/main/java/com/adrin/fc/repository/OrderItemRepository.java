package com.adrin.fc.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adrin.fc.entity.OrderItem;
import com.adrin.fc.entity.Provider;
import com.adrin.fc.enums.MenuTag;
import com.adrin.fc.enums.OrderStatus;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    Page<OrderItem> findByProvider(Provider provider, Pageable pageable);

    Page<OrderItem> findByProviderAndMenuItemTag(Provider provider, MenuTag tag, Pageable pageable);

    @Query("""
                SELECT oi FROM OrderItem oi
                WHERE oi.provider = :provider
                  AND oi.orderStatus = :status
                  AND oi.order.createdAt BETWEEN :startOfDay AND :endOfDay
            """)
    Page<OrderItem> findByProviderAndOrderStatusAndOrderCreatedAtBetween(
            @Param("provider") Provider provider,
            @Param("status") OrderStatus status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            Pageable pageable);

}
