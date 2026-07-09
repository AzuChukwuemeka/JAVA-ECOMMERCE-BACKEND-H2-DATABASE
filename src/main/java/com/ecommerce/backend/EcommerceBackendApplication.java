package com.ecommerce.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the E-Commerce Backend API.
 * <p>
 * Provides REST endpoints for product catalog browsing, user authentication,
 * shopping cart management, and order checkout — backed by an embedded H2 database.
 */
@SpringBootApplication
public class EcommerceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceBackendApplication.class, args);
    }
}
