package com.adrin.fc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.adrin.fc.entity.OrderItem;
import com.adrin.fc.entity.Provider;
import com.adrin.fc.enums.MenuTag;
import com.adrin.fc.enums.OrderStatus;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    Page<OrderItem> findByProviderAndOrderStatus(Provider provider, OrderStatus status, Pageable pageable);

    Page<OrderItem> findByProviderAndOrderStatusAndMenuItemTag(Provider provider, OrderStatus status, MenuTag tag,
            Pageable pageable);

    Page<OrderItem> findByProvider(Provider provider, Pageable pageable);

    Page<OrderItem> findByProviderAndMenuItemTag(Provider provider, MenuTag tag, Pageable pageable);
}
