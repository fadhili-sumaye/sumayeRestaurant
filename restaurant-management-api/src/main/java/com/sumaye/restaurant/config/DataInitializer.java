package com.sumaye.restaurant.config;

import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Seeds the database with default data on startup (DEV profile only).
 * This ensures the app is always testable even after a fresh database.
 * Run once on first boot; safe to run repeatedly (uses findOrCreate pattern).
 */
@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;
    private final BranchRepository branchRepository;
    private final RestaurantTableRepository tableRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("=== DataInitializer: Checking dev data... ===");

            // 1. Ensure all roles exist
            Role adminRole       = findOrCreateRole("ROLE_ADMIN",       "System administrator");
            Role ownerRole       = findOrCreateRole("ROLE_OWNER",       "Restaurant owner");
            Role managerRole     = findOrCreateRole("ROLE_MANAGER",     "Branch manager");
            Role waiterRole      = findOrCreateRole("ROLE_WAITER",      "Waiter");
            Role cashierRole     = findOrCreateRole("ROLE_CASHIER",     "Cashier");
            Role kitchenRole     = findOrCreateRole("ROLE_KITCHEN",     "Kitchen staff");
            Role storekeeperRole = findOrCreateRole("ROLE_STOREKEEPER", "Storekeeper");
            Role deliveryRole    = findOrCreateRole("ROLE_DELIVERY",    "Delivery staff");

            // 2. Ensure restaurant exists
            Restaurant restaurant = restaurantRepository.findAll().stream().findFirst()
                    .orElseGet(() -> {
                        Restaurant r = new Restaurant();
                        r.setName("Sumaye Restaurant");
                        r.setEmail("info@sumaye.com");
                        r.setPhoneNumber("+255 123 456 789");
                        r.setAddress("New Street 123, Dar es Salaam");
                        log.info("Created restaurant: Sumaye Restaurant");
                        return restaurantRepository.save(r);
                    });

            // 3. Ensure branch exists
            Branch branch = branchRepository.findAll().stream().findFirst()
                    .orElseGet(() -> {
                        Branch b = new Branch();
                        b.setRestaurant(restaurant);
                        b.setName("Downtown Branch");
                        b.setLocation("Dar es Salaam, Tanzania");
                        b.setPhoneNumber("+255 111 111 111");
                        log.info("Created branch: Downtown Branch");
                        return branchRepository.save(b);
                    });

            // 4. Ensure test users exist
            createUserIfMissing("admin",       "Admin",   "User",     "admin@sumaye.com",       "admin123",    Set.of(adminRole),       branch, restaurant);
            createUserIfMissing("manager",     "Manager", "User",     "manager@sumaye.com",     "manager123",  Set.of(managerRole),     branch, restaurant);
            createUserIfMissing("waiter",      "Waiter",  "User",     "waiter@sumaye.com",      "waiter123",   Set.of(waiterRole),      branch, restaurant);
            createUserIfMissing("cashier",     "Cashier", "User",     "cashier@sumaye.com",     "cashier123",  Set.of(cashierRole),     branch, restaurant);
            createUserIfMissing("kitchen",     "Kitchen", "Staff",    "kitchen@sumaye.com",     "kitchen123",  Set.of(kitchenRole),     branch, restaurant);
            createUserIfMissing("owner",       "Owner",   "User",     "owner@sumaye.com",       "owner123",    Set.of(ownerRole),       branch, restaurant);
            createUserIfMissing("storekeeper", "Store",   "Keeper",   "storekeeper@sumaye.com", "store123",    Set.of(storekeeperRole), branch, restaurant);
            createUserIfMissing("delivery",    "Juma",    "Delivery", "delivery@sumaye.com",    "delivery123", Set.of(deliveryRole),    branch, restaurant);

            // 5. Ensure default restaurant tables exist
            createTableIfMissing(branch, 1, 4, RestaurantTable.TableStatus.AVAILABLE);
            createTableIfMissing(branch, 2, 2, RestaurantTable.TableStatus.AVAILABLE);
            createTableIfMissing(branch, 3, 6, RestaurantTable.TableStatus.OCCUPIED);
            createTableIfMissing(branch, 4, 4, RestaurantTable.TableStatus.RESERVED);
            createTableIfMissing(branch, 5, 8, RestaurantTable.TableStatus.CLEANING);
            createTableIfMissing(branch, 6, 4, RestaurantTable.TableStatus.AVAILABLE);

            // 6. Ensure default menu categories & items exist
            MenuCategory mainDishes = findOrCreateCategory(branch, "Vyakula Vikuu", "Chakula kikuu cha mchana na usiku");
            MenuCategory drinks     = findOrCreateCategory(branch, "Vinywaji", "Vinywaji baridi na moto");
            MenuCategory snacks     = findOrCreateCategory(branch, "Vitafunio", "Vitafunio mbalimbali");

            createMenuItemIfMissing(branch, mainDishes, "Pilau Kuku", "Pilau ya kuku wa kienyeji na kachumbari", new BigDecimal("8000.00"), 20);
            createMenuItemIfMissing(branch, mainDishes, "Ugali Samaki", "Ugali moto na samaki wa kukaanga na mboga za majani", new BigDecimal("10000.00"), 25);
            createMenuItemIfMissing(branch, mainDishes, "Chips Kuku", "Chips kukaangwa na robo ya kuku choma", new BigDecimal("7000.00"), 15);
            createMenuItemIfMissing(branch, mainDishes, "Biryani Ng'ombe", "Biryani ya nyama ya ng'ombe na sosi ya kando", new BigDecimal("9000.00"), 20);

            createMenuItemIfMissing(branch, drinks, "Chai ya Maziwa", "Chai ya viungo na maziwa safi", new BigDecimal("1500.00"), 5);
            createMenuItemIfMissing(branch, drinks, "Juisi ya Embe", "Juisi safi ya embe baridi", new BigDecimal("2500.00"), 5);
            createMenuItemIfMissing(branch, drinks, "Soda", "Soda baridi ya chupa (350ml)", new BigDecimal("1500.00"), 2);
            createMenuItemIfMissing(branch, drinks, "Maji ya Kunywa", "Maji safi ya chupa (500ml)", new BigDecimal("1000.00"), 1);

            createMenuItemIfMissing(branch, snacks, "Sambusa ya Nyama", "Sambusa mbili zilizojazwa nyama ya kusaga", new BigDecimal("2000.00"), 10);
            createMenuItemIfMissing(branch, snacks, "Mandazi", "Mandazi mawili ya nazi", new BigDecimal("1000.00"), 5);

            log.info("=== DataInitializer: Done. ===");
            log.info("Test credentials: admin/admin123 | manager/manager123 | waiter/waiter123 | cashier/cashier123 | kitchen/kitchen123 | owner/owner123 | storekeeper/store123 | delivery/delivery123");
        };
    }

    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role r = new Role(name);
            log.info("Created role: {}", name);
            return roleRepository.save(r);
        });
    }

    private void createUserIfMissing(String username, String firstName, String lastName,
                                     String email, String rawPassword,
                                     Set<Role> roles, Branch branch, Restaurant restaurant) {
        if (userRepository.existsByUsername(username)) {
            log.debug("User already exists: {}", username);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setRoles(new HashSet<>(roles));
        user.setBranch(branch);
        user.setRestaurant(restaurant);
        userRepository.save(user);
        log.info("Created test user: {} ({})", username, roles.iterator().next().getName());
    }

    private void createTableIfMissing(Branch branch, Integer tableNumber, Integer capacity, RestaurantTable.TableStatus status) {
        if (tableRepository.existsByBranchAndTableNumber(branch, tableNumber)) {
            log.debug("Table already exists: Meza {}", tableNumber);
            return;
        }

        RestaurantTable table = new RestaurantTable();
        table.setBranch(branch);
        table.setTableNumber(tableNumber);
        table.setCapacity(capacity);
        table.setStatus(status != null ? status : RestaurantTable.TableStatus.AVAILABLE);
        table.setCreatedAt(LocalDateTime.now());
        tableRepository.save(table);
        log.info("Created test table: Meza {} (capacity={}, status={})", tableNumber, capacity, status);
    }

    private MenuCategory findOrCreateCategory(Branch branch, String name, String description) {
        return menuCategoryRepository.findByBranchOrderByNameAsc(branch).stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    MenuCategory c = new MenuCategory();
                    c.setBranch(branch);
                    c.setName(name);
                    c.setDescription(description);
                    c.setActive(true);
                    c.setCreatedAt(LocalDateTime.now());
                    log.info("Created menu category: {}", name);
                    return menuCategoryRepository.save(c);
                });
    }

    private void createMenuItemIfMissing(Branch branch, MenuCategory category, String name, String description,
                                         BigDecimal price, int prepTime) {
        boolean exists = menuItemRepository.findByBranchOrderByNameAsc(branch).stream()
                .anyMatch(m -> m.getName().equalsIgnoreCase(name));
        if (exists) {
            return;
        }

        MenuItem item = new MenuItem();
        item.setBranch(branch);
        item.setCategory(category);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setPreparationTimeMinutes(prepTime);
        item.setAvailable(true);
        item.setCreatedAt(LocalDateTime.now());
        menuItemRepository.save(item);
        log.info("Created menu item: {} - {} TZS", name, price);
    }
}
