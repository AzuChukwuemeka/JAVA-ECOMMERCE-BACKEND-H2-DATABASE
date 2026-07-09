package com.ecommerce.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must not be negative")
    private BigDecimal price;

    private Double discountPercentage;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must not be negative")
    private Integer stock;

    private String brand;

    private String thumbnail;

    @NotBlank(message = "Category slug is required")
    private String categorySlug;

    private List<String> images;
}
