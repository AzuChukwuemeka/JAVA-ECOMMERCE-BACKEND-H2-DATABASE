package com.ecommerce.backend.config;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Role;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

/**
 * Populates the embedded H2 database with a realistic product catalog and two
 * demo accounts (admin/user) the first time the application starts.
 * <p>
 * This runs only when the database is empty, so it is safe across restarts
 * of the (file-based) H2 store.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random(42);

    /** slug, display name, brands, product name templates */
    private record CategorySeed(String slug, String name, String[] brands, String[] templates) {}

    private static final List<CategorySeed> CATEGORY_SEEDS = List.of(
            new CategorySeed("smartphones", "Smartphones",
                    new String[]{"Apex", "Novatech", "Orion", "Pulsar"},
                    new String[]{"%s X12 Smartphone", "%s Nova 5G Phone", "%s Edge Lite", "%s Prime Max"}),
            new CategorySeed("laptops", "Laptops",
                    new String[]{"Vertex", "Silverline", "Cobalt", "Nimbus"},
                    new String[]{"%s Book Pro 14\"", "%s Air Slim 13\"", "%s Studio 16\"", "%s Flex Convertible"}),
            new CategorySeed("fragrances", "Fragrances",
                    new String[]{"Lumière", "Velour", "Amber & Oak", "Bloom"},
                    new String[]{"%s Eau de Parfum", "%s Signature Cologne", "%s Night Mist", "%s Citrus Splash"}),
            new CategorySeed("skincare", "Skincare",
                    new String[]{"PureGlow", "DermaCare", "Botanika", "AquaSoft"},
                    new String[]{"%s Vitamin C Serum", "%s Hydrating Cream", "%s Clay Mask", "%s Gentle Cleanser"}),
            new CategorySeed("groceries", "Groceries",
                    new String[]{"FarmFresh", "GreenValley", "Harvest Co.", "PurePantry"},
                    new String[]{"%s Organic Rice 5kg", "%s Extra Virgin Olive Oil", "%s Whole Wheat Pasta", "%s Roasted Coffee Beans"}),
            new CategorySeed("home-decoration", "Home Decoration",
                    new String[]{"CasaViva", "Nordic Nest", "Willow & Stone", "Artisan Loft"},
                    new String[]{"%s Ceramic Vase", "%s Wall Art Canvas", "%s Scented Candle Set", "%s Woven Wall Hanging"}),
            new CategorySeed("furniture", "Furniture",
                    new String[]{"Oakwood", "UrbanForm", "Cloudline", "Maple & Co."},
                    new String[]{"%s Accent Armchair", "%s Coffee Table", "%s Bookshelf Unit", "%s Dining Chair Set"}),
            new CategorySeed("tops", "Tops",
                    new String[]{"Threadline", "UrbanFit", "CottonCraft", "Northline"},
                    new String[]{"%s Crew Neck T-Shirt", "%s Linen Blouse", "%s Striped Polo", "%s Graphic Tee"}),
            new CategorySeed("womens-dresses", "Women's Dresses",
                    new String[]{"Aurelie", "Bellevue", "Lumen", "Rosalind"},
                    new String[]{"%s Floral Midi Dress", "%s Wrap Dress", "%s Evening Gown", "%s Summer Sundress"}),
            new CategorySeed("womens-shoes", "Women's Shoes",
                    new String[]{"Stridewell", "Velora", "Chateau", "Luxstep"},
                    new String[]{"%s Ankle Boots", "%s Strappy Heels", "%s Ballet Flats", "%s Canvas Sneakers"}),
            new CategorySeed("mens-shirts", "Men's Shirts",
                    new String[]{"Oxford Row", "Greyline", "Heritage Co.", "Stonebridge"},
                    new String[]{"%s Oxford Button-Down", "%s Flannel Shirt", "%s Linen Shirt", "%s Slim Fit Dress Shirt"}),
            new CategorySeed("mens-shoes", "Men's Shoes",
                    new String[]{"Trailhide", "Cobblestone", "Ironstep", "Marchwood"},
                    new String[]{"%s Leather Oxfords", "%s Running Sneakers", "%s Chukka Boots", "%s Loafers"}),
            new CategorySeed("mens-watches", "Men's Watches",
                    new String[]{"Chronova", "Ironpoint", "Meridian", "Blacksteel"},
                    new String[]{"%s Chronograph Watch", "%s Automatic Steel Watch", "%s Minimalist Leather Watch", "%s Dive Watch"}),
            new CategorySeed("womens-watches", "Women's Watches",
                    new String[]{"Elira", "Petalline", "Gold Hour", "Serene"},
                    new String[]{"%s Rose Gold Watch", "%s Mother of Pearl Watch", "%s Bangle Watch", "%s Minimalist Mesh Watch"}),
            new CategorySeed("womens-bags", "Women's Bags",
                    new String[]{"Maison Lyre", "Terra Nova", "Velvet Row", "Everly"},
                    new String[]{"%s Leather Tote", "%s Crossbody Bag", "%s Clutch Purse", "%s Weekender Bag"}),
            new CategorySeed("womens-jewellery", "Women's Jewellery",
                    new String[]{"Lumora", "Opaline", "Gilded Leaf", "Solstice"},
                    new String[]{"%s Pearl Necklace", "%s Gold Hoop Earrings", "%s Charm Bracelet", "%s Gemstone Ring"}),
            new CategorySeed("sunglasses", "Sunglasses",
                    new String[]{"Solstice Eyewear", "Horizon", "Umbra", "Coastline"},
                    new String[]{"%s Aviator Sunglasses", "%s Round Retro Shades", "%s Polarized Wayfarers", "%s Oversized Cat Eye"}),
            new CategorySeed("automotive", "Automotive",
                    new String[]{"TorqueMax", "RoadGuard", "DriveTech", "AutoPrime"},
                    new String[]{"%s All-Weather Floor Mats", "%s LED Headlight Kit", "%s Car Vacuum Cleaner", "%s Dash Camera"}),
            new CategorySeed("motorcycle", "Motorcycle",
                    new String[]{"GearRider", "SpeedForge", "TrailBlaze", "Ironclad Moto"},
                    new String[]{"%s Full-Face Helmet", "%s Riding Gloves", "%s Motorcycle Cover", "%s Saddle Bag Set"}),
            new CategorySeed("lighting", "Lighting",
                    new String[]{"Luminaire", "Brightwave", "Glowline", "Ambient Co."},
                    new String[]{"%s LED Floor Lamp", "%s Pendant Ceiling Light", "%s Smart Bulb 4-Pack", "%s String Fairy Lights"})
    );

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Database already seeded, skipping data initialization.");
            return;
        }

        log.info("Seeding database with categories, products, and demo accounts...");
        seedCategoriesAndProducts();
        seedUsers();
        log.info("Database seeding complete.");
    }

    private void seedCategoriesAndProducts() {
        for (CategorySeed seed : CATEGORY_SEEDS) {
            Category category = categoryRepository.save(Category.builder()
                    .slug(seed.slug())
                    .name(seed.name())
                    .build());

            for (int i = 0; i < seed.templates().length; i++) {
                String brand = seed.brands()[i % seed.brands().length];
                String title = String.format(seed.templates()[i], brand);
                productRepository.save(buildProduct(title, brand, category));
            }
        }
    }

    private Product buildProduct(String title, String brand, Category category) {
        double basePrice = 15 + random.nextDouble() * 480;
        BigDecimal price = BigDecimal.valueOf(basePrice).setScale(2, RoundingMode.HALF_UP);
        double discount = Math.round(random.nextDouble() * 25 * 10) / 10.0;
        double rating = Math.round((3.3 + random.nextDouble() * 1.7) * 10) / 10.0;
        int stock = 5 + random.nextInt(150);
        String seedSlug = category.getSlug() + "-" + Math.abs(title.hashCode() % 1000);

        return Product.builder()
                .title(title)
                .description("Premium " + category.getName().toLowerCase() + " item from " + brand
                        + ", designed for everyday quality and durability.")
                .price(price)
                .discountPercentage(discount)
                .rating(rating)
                .stock(stock)
                .brand(brand)
                .thumbnail("https://picsum.photos/seed/" + seedSlug + "/480/480")
                .category(category)
                .images(List.of(
                        "https://picsum.photos/seed/" + seedSlug + "-1/800/800",
                        "https://picsum.photos/seed/" + seedSlug + "-2/800/800"
                ))
                .build();
    }

    private void seedUsers() {
        userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@example.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .build());

        userRepository.save(User.builder()
                .name("Demo Shopper")
                .email("user@example.com")
                .password(passwordEncoder.encode("User@123"))
                .role(Role.USER)
                .build());

        log.info("Demo accounts created -> admin: admin@example.com / Admin@123 | user: user@example.com / User@123");
    }
}
