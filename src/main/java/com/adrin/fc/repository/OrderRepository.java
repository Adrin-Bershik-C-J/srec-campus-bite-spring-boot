package com.adrin.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.adrin.fc.entity.Order;
import com.adrin.fc.entity.Provider;
import com.adrin.fc.entity.User;
import com.adrin.fc.enums.OrderStatus;

import jakarta.persistence.LockModeType;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByTokenNumber(String tokenNumber);

    @Query("SELECT COUNT(o) FROM Order o WHERE EXISTS (SELECT 1 FROM OrderItem oi WHERE oi.order = o AND oi.provider = :provider) AND o.createdAt BETWEEN :start AND :end")
    long countByProviderAndCreatedAtBetween(@Param("provider") Provider provider, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end ORDER BY o.createdAt DESC")
    Page<Order> findLastOrderForDayWithLock(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE o.user = :user AND oi.orderStatus = :status ORDER BY o.createdAt DESC")
    Page<Order> findByUserAndOrderItemsOrderStatusOrderByCreatedAtDesc(@Param("user") User user, @Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE o.user = :user AND oi.orderStatus IN :statuses ORDER BY o.createdAt DESC")
    Page<Order> findByUserAndOrderItemsOrderStatusInOrderByCreatedAtDesc(@Param("user") User user, @Param("statuses") java.util.List<OrderStatus> statuses, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o WHERE o.user = :user AND NOT EXISTS (SELECT 1 FROM OrderItem oi WHERE oi.order = o AND oi.orderStatus != 'DONE') ORDER BY o.createdAt DESC")
    Page<Order> findByUserAndAllOrderItemsDoneOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

}