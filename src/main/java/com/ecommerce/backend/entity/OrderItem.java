package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * A snapshot of a purchased product within an {@link Order}, preserving the
 * price at time of purchase regardless of later product price changes.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@ToString(exclude = {"order", "product"})
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Product title, captured at purchase time. */
    @Column(nullable = false)
    private String productTitle;

    @Column(nullable = false)
    private Integer quantity;

    /** Unit price at the time of purchase. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
}
