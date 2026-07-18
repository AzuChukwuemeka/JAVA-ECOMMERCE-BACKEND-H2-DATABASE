package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.request.OrderStatusUpdateRequest;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.dto.response.PageResponse;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only endpoints for managing orders placed by any customer.
 * All routes here require role ADMIN — enforced in SecurityConfig via the
 * "/api/admin/**" matcher, not just documentation.
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin - Orders", description = "View and manage every order in the system (admin only)")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "List all orders across all customers, optionally filtered by status")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @Parameter(description = "Filter by order status, e.g. PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED")
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get any order by id, regardless of which customer placed it")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderForAdmin(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update an order's status, e.g. mark as SHIPPED or DELIVERED")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }
}
