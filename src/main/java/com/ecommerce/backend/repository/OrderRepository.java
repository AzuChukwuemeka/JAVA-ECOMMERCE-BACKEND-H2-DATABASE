package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser_Id(Long userId, Pageable pageable);
    Optional<Order> findByIdAndUser_Id(Long id, Long userId);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
