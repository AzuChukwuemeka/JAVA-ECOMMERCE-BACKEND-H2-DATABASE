package com.ecommerce.backend.entity;

/**
 * Lifecycle status of an {@link Order}.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
