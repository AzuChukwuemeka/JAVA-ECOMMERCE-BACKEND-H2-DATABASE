package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.request.CheckoutRequest;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.dto.response.PageResponse;
import com.ecommerce.backend.security.UserPrincipal;
import com.ecommerce.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Checkout and view order history")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Convert the current cart into a placed order")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(principal.getUser(), request));
    }

    @GetMapping
    @Operation(summary = "List the current user's order history")
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(@AuthenticationPrincipal UserPrincipal principal,
                                                                    @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersForUser(principal.getId(), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single order by id")
    public ResponseEntity<OrderResponse> getOrder(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderForUser(principal.getId(), id));
    }
}
