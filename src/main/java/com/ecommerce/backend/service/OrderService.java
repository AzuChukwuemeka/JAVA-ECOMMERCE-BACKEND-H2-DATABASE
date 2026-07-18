package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.request.CheckoutRequest;
import com.ecommerce.backend.dto.response.OrderItemResponse;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.dto.response.PageResponse;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Converts a user's cart into a placed order (checkout) and provides order history lookup.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse checkout(User user, CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUser_Id(user.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot checkout an empty cart");
        }

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getTitle());
            }
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productTitle(product.getTitle())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            order.addItem(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByUser_Id(user.getId());

        return toResponse(saved);
    }

    public PageResponse<OrderResponse> getOrdersForUser(Long userId, Pageable pageable) {
        Page<Order> page = orderRepository.findByUser_Id(userId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    public OrderResponse getOrderForUser(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return toResponse(order);
    }

    /** Admin: list every order in the system, optionally filtered by status. */
    public PageResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page = status != null
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    /** Admin: fetch any order by id, regardless of which user placed it. */
    public OrderResponse getOrderForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return toResponse(order);
    }

    /** Admin: transition an order's status, e.g. CONFIRMED -> SHIPPED -> DELIVERED. */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot change status of an order that is already "
                    + order.getStatus().name().toLowerCase());
        }

        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productTitle(item.getProductTitle())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(items)
                .build();
    }
}
