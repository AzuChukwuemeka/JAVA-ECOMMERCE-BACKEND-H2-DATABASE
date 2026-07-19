package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.request.ProductRequest;
import com.ecommerce.backend.dto.response.PageResponse;
import com.ecommerce.backend.dto.response.ProductResponse;
import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the product catalog: browsing, searching, and admin CRUD operations.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(String categorySlug, String search, Pageable pageable) {
        Page<Product> page;
        boolean hasCategory = StringUtils.hasText(categorySlug);
        boolean hasSearch = StringUtils.hasText(search);

        if (hasCategory && hasSearch) {
            page = productRepository.findByCategory_SlugAndTitleContainingIgnoreCase(categorySlug, search, pageable);
        } else if (hasCategory) {
            page = productRepository.findByCategory_Slug(categorySlug, pageable);
        } else if (hasSearch) {
            page = productRepository.findByTitleContainingIgnoreCase(search, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return toResponse(findProductOrThrow(id));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryService.findCategoryOrThrow(request.getCategorySlug());
        Product product = Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPercentage(request.getDiscountPercentage() != null ? request.getDiscountPercentage() : 0.0)
                .rating(0.0)
                .stock(request.getStock())
                .brand(request.getBrand())
                .thumbnail(request.getThumbnail())
                .category(category)
                .images(request.getImages() != null ? request.getImages() : List.of())
                .build();
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);
        Category category = categoryService.findCategoryOrThrow(request.getCategorySlug());

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        if (request.getDiscountPercentage() != null) {
            product.setDiscountPercentage(request.getDiscountPercentage());
        }
        product.setStock(request.getStock());
        product.setBrand(request.getBrand());
        product.setThumbnail(request.getThumbnail());
        product.setCategory(category);
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPercentage(product.getDiscountPercentage())
                .rating(product.getRating())
                .stock(product.getStock())
                .brand(product.getBrand())
                .thumbnail(product.getThumbnail())
                .category(product.getCategory().getSlug())
                .images(new ArrayList<>(product.getImages()))
                .build();
    }
}