package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.request.CartItemRequest;
import com.ecommerce.backend.dto.response.CartItemResponse;
import com.ecommerce.backend.dto.response.CartResponse;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Manages the authenticated user's shopping cart.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return toCartResponse(cartItemRepository.findByUser_Id(userId));
    }

    @Transactional
    public CartResponse addToCart(User user, CartItemRequest request) {
        Product product = productService.findProductOrThrow(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product: " + product.getTitle());
        }

        CartItem item = cartItemRepository.findByUser_IdAndProduct_Id(user.getId(), product.getId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.getQuantity());
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(request.getQuantity())
                        .build());

        cartItemRepository.save(item);
        return toCartResponse(cartItemRepository.findByUser_Id(user.getId()));
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findByIdAndUser_Id(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (item.getProduct().getStock() < quantity) {
            throw new BadRequestException("Insufficient stock for product: " + item.getProduct().getTitle());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return toCartResponse(cartItemRepository.findByUser_Id(userId));
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findByIdAndUser_Id(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
        cartItemRepository.delete(item);
        return toCartResponse(cartItemRepository.findByUser_Id(userId));
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUser_Id(userId);
    }

    private CartResponse toCartResponse(List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream().map(this::toItemResponse).toList();
        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = itemResponses.stream().mapToInt(CartItemResponse::getQuantity).sum();

        return CartResponse.builder()
                .items(itemResponses)
                .totalAmount(total)
                .totalItems(totalItems)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productTitle(item.getProduct().getTitle())
                .productThumbnail(item.getProduct().getThumbnail())
                .unitPrice(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}