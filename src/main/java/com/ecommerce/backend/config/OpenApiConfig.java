package com.ecommerce.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the Swagger / OpenAPI documentation, including the JWT bearer
 * security scheme so requests can be authorized directly from Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    // Single source of truth for "what is my public URL", so the Swagger
    // "Try it out" calls hit the deployed Render URL instead of relative paths.
    // Set APP_BASE_URL in Render to your service URL, e.g. https://my-app.onrender.com
    @Value("${app.base-url}")
    private String appBaseUrl;

    // Publishes the seeded demo admin/user logins in the Swagger description
    // while app.demo-mode=true. Set DEMO_MODE=false in Render to stop showing these.
    @Value("${app.demo-mode}")
    private boolean demoMode;

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        String description = "REST API for product catalog browsing, authentication, shopping cart, and order checkout.";

        if (demoMode) {
            description += "\n\n**Demo accounts** (public demo — don't reuse these credentials anywhere real):\n"
                    + "- Admin: `admin@example.com` / `Admin@123` (manage products, categories, and all orders)\n"
                    + "- Shopper: `user@example.com` / `User@123` (browse, cart, checkout)\n\n"
                    + "Log in via POST /api/auth/login, then click **Authorize** above and paste the returned token.";
        }

        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Backend API")
                        .description(description)
                        .version("1.0.0")
                        .contact(new Contact().name("E-Commerce Backend")))
                .servers(List.of(
                        new Server().url(appBaseUrl).description("Active environment")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
