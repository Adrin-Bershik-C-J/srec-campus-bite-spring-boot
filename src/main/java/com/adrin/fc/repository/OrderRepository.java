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
import com.adrin.fc.entity.User;

import jakarta.persistence.LockModeType;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Order> findLastOrderForDayWithLock(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}