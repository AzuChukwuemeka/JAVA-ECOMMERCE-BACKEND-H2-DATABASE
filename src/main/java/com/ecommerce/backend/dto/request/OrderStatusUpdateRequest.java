package com.ecommerce.backend.dto.request;

import com.ecommerce.backend.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}
