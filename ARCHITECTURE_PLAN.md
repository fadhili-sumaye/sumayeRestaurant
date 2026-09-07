# Restaurant Management System - Architecture & Design

## 1. SYSTEM ARCHITECTURE DIAGRAM

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER (Android)                        │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                 Android App (MVVM Architecture)               │  │
│  │  ┌─────────┐  ┌──────────┐  ┌────────────┐  ┌──────────┐   │  │
│  │  │   UI    │  │ViewModel│  │ Repository │  │ Network  │   │  │
│  │  │ Screens │  │  + State │  │ (Room DB)  │  │(Retrofit)│   │  │
│  │  └─────────┘  └──────────┘  └────────────┘  └──────────┘   │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────┬──────────────────────────────────────────────┘
                      │
                      │ REST API / WebSocket
                      │ (HTTPS/JWT Token)
                      │
┌─────────────────────▼──────────────────────────────────────────────┐
│                   API GATEWAY LAYER (Spring Boot)                   │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │              Spring Security + JWT Authentication             │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────┬──────────────────────────────────────────────┘
                      │
┌─────────────────────▼──────────────────────────────────────────────┐
│                  APPLICATION LAYER (Spring Boot)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │  Controller  │─▶│   Service    │─▶│  Repository  │            │
│  │  (REST APIs) │  │   (Business  │  │    (Data     │            │
│  │              │  │    Logic)    │  │   Access)    │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  WebSocket Handler (Real-time Order Status Updates)          │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────┬──────────────────────────────────────────────┘
                      │
                      │ SQL Queries (JDBC/JPA)
                      │
┌─────────────────────▼──────────────────────────────────────────────┐
│                   DATA LAYER (MySQL Database)                       │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Tables: Users, Roles, Restaurants, Branches, Tables,        │  │
│  │  Menu, Inventory, Orders, Payments, Reports, etc.            │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. COMPLETE MYSQL DATABASE DESIGN

### Entity Relationship Diagram (ERD)

```
RESTAURANTS (One-to-Many)
    ├─── BRANCHES (One-to-Many)
    │        ├─── TABLES (One-to-Many)
    │        ├─── STAFF/USERS (One-to-Many)
    │        ├─── INVENTORY (One-to-Many)
    │        ├─── ORDERS (One-to-Many)
    │        ├─── EXPENSES (One-to-Many)
    │        ├─── PURCHASES (One-to-Many)
    │        └─── RESERVATIONS (One-to-Many)
    │
    ├─── USERS (with roles)
    │        ├─── ROLES (Many-to-Many through USER_ROLES)
    │        └─── PERMISSIONS (through ROLE_PERMISSIONS)
    │
    ├─── MENU_CATEGORIES (One-to-Many)
    │        └─── MENU_ITEMS
    │             ├─── RECIPES (One-to-Many)
    │             │     └─── RECIPE_INGREDIENTS (Many-to-Many)
    │             │          └─── INGREDIENTS
    │             └─── ORDER_ITEMS (One-to-Many)
    │
    ├─── ORDERS (One-to-Many)
    │        ├─── ORDER_ITEMS (One-to-Many)
    │        ├─── KITCHEN_ORDERS (One-to-Many)
    │        │     └─── KITCHEN_ORDER_ITEMS (One-to-Many)
    │        └─── PAYMENTS (One-to-Many)
    │
    ├─── CUSTOMERS (One-to-Many)
    │        ├─── ORDERS (One-to-Many)
    │        └─── RESERVATIONS (One-to-Many)
    │
    ├─── SUPPLIERS (One-to-Many)
    │        └─── PURCHASES (One-to-Many)
    │
    └─── AUDIT_LOGS (One-to-Many)
```

### Database Tables Structure

```sql
-- Core/System Tables
1. RESTAURANTS
2. BRANCHES
3. USERS
4. ROLES
5. ROLE_PERMISSIONS
6. PERMISSIONS
7. USER_ROLES

-- Location & Setup Tables
8. TABLES
9. RESERVATIONS

-- Menu Management
10. MENU_CATEGORIES
11. MENU_ITEMS
12. RECIPES
13. RECIPE_INGREDIENTS

-- Inventory Management
14. INGREDIENTS
15. INVENTORY
16. INVENTORY_TRANSACTIONS

-- Procurement
17. SUPPLIERS
18. PURCHASES
19. PURCHASE_ITEMS

-- Operations
20. CUSTOMERS
21. ORDERS
22. ORDER_ITEMS
23. KITCHEN_ORDERS
24. KITCHEN_ORDER_ITEMS
25. PAYMENTS
26. EXPENSES

-- Analytics & Audit
27. AUDIT_LOGS
28. NOTIFICATIONS
```

---

## 3. ERD EXPLANATION

### User Hierarchy
```
RESTAURANTS
    └── BRANCHES
            └── USERS (Waiter, Cashier, Kitchen, Manager, etc.)
                    └── ROLES (ADMIN, MANAGER, WAITER, CASHIER, KITCHEN)
                            └── PERMISSIONS (CREATE_ORDER, VIEW_REPORT, etc.)
```

### Order Flow
```
TABLE (Available) 
    └── ORDER (NEW)
            ├── ORDER_ITEMS (Menu items with quantities)
            │
            ├── KITCHEN_ORDER (Sent to kitchen)
            │        └── KITCHEN_ORDER_ITEMS
            │                └── Status: ACCEPTED → PREPARING → READY
            │
            └── PAYMENT (When ready)
                    └── Status: PENDING → COMPLETED
                    
After Payment:
    → TABLE (Available again)
    → INVENTORY_TRANSACTIONS (Deduct ingredients)
```

### Menu & Inventory
```
MENU_ITEM 
    └── RECIPE
            └── RECIPE_INGREDIENTS
                    └── INGREDIENTS (with quantities)
                            └── INVENTORY (branch-specific stock)
                                    └── INVENTORY_TRANSACTIONS (audit trail)
```

---

## 4. SPRING BOOT BACKEND FOLDER STRUCTURE

```
restaurant-management-api/
│
├── src/main/java/com/sumaye/restaurant/
│   │
│   ├── config/
│   │   ├── SecurityConfig.java          (Spring Security + JWT)
│   │   ├── JwtConfig.java              (JWT configuration)
│   │   ├── WebSocketConfig.java        (WebSocket/STOMP setup)
│   │   ├── CorsConfig.java             (CORS for mobile)
│   │   └── DatabaseConfig.java         (JPA/Hibernate config)
│   │
│   ├── controller/
│   │   ├── AuthController.java         (Login, logout, registration)
│   │   ├── BranchController.java
│   │   ├── UserController.java
│   │   ├── TableController.java
│   │   ├── MenuController.java
│   │   ├── OrderController.java
│   │   ├── KitchenController.java       (KOT operations)
│   │   ├── PaymentController.java
│   │   ├── InventoryController.java
│   │   ├── ReportController.java
│   │   ├── CustomerController.java
│   │   ├── ReservationController.java
│   │   └── SettingsController.java
│   │
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── BranchService.java
│   │   ├── UserService.java
│   │   ├── TableService.java
│   │   ├── MenuService.java
│   │   ├── OrderService.java
│   │   ├── KitchenService.java
│   │   ├── PaymentService.java
│   │   ├── InventoryService.java
│   │   ├── ReportService.java
│   │   ├── CustomerService.java
│   │   ├── SupplierService.java
│   │   ├── PurchaseService.java
│   │   ├── NotificationService.java
│   │   └── AuditService.java
│   │
│   ├── repository/
│   │   ├── RestaurantRepository.java
│   │   ├── BranchRepository.java
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   ├── PermissionRepository.java
│   │   ├── TableRepository.java
│   │   ├── MenuCategoryRepository.java
│   │   ├── MenuItemRepository.java
│   │   ├── RecipeRepository.java
│   │   ├── RecipeIngredientRepository.java
│   │   ├── IngredientRepository.java
│   │   ├── InventoryRepository.java
│   │   ├── InventoryTransactionRepository.java
│   │   ├── OrderRepository.java
│   │   ├── OrderItemRepository.java
│   │   ├── KitchenOrderRepository.java
│   │   ├── KitchenOrderItemRepository.java
│   │   ├── PaymentRepository.java
│   │   ├── CustomerRepository.java
│   │   ├── SupplierRepository.java
│   │   ├── PurchaseRepository.java
│   │   ├── ReservationRepository.java
│   │   ├── AuditLogRepository.java
│   │   └── NotificationRepository.java
│   │
│   ├── model/entity/
│   │   ├── Restaurant.java
│   │   ├── Branch.java
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Permission.java
│   │   ├── Table.java
│   │   ├── MenuCategory.java
│   │   ├── MenuItem.java
│   │   ├── Recipe.java
│   │   ├── RecipeIngredient.java
│   │   ├── Ingredient.java
│   │   ├── Inventory.java
│   │   ├── InventoryTransaction.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── KitchenOrder.java
│   │   ├── KitchenOrderItem.java
│   │   ├── Payment.java
│   │   ├── Customer.java
│   │   ├── Supplier.java
│   │   ├── Purchase.java
│   │   ├── PurchaseItem.java
│   │   ├── Reservation.java
│   │   ├── AuditLog.java
│   │   └── Notification.java
│   │
│   ├── model/dto/
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── UserDTO.java
│   │   ├── OrderDTO.java
│   │   ├── OrderItemDTO.java
│   │   ├── PaymentDTO.java
│   │   ├── MenuItemDTO.java
│   │   ├── ReportDTO.java
│   │   └── ErrorResponse.java
│   │
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtTokenFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   └── SecurityUtil.java
│   │
│   ├── websocket/
│   │   ├── WebSocketController.java      (Real-time notifications)
│   │   └── OrderStatusListener.java
│   │
│   ├── exception/
│   │   ├── ApiException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   └── GlobalExceptionHandler.java
│   │
│   ├── util/
│   │   ├── Constants.java
│   │   ├── ValidationUtil.java
│   │   └── DateUtil.java
│   │
│   └── RestaurantManagementApplication.java
│
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   ├── application-prod.properties
│   └── db/
│       └── migration/
│           ├── V1__initial_schema.sql
│           ├── V2__add_indexes.sql
│           └── V3__sample_data.sql
│
├── pom.xml (or build.gradle)
└── README.md
```

---

## 5. ANDROID JAVA FOLDER STRUCTURE

```
app/
│
├── src/main/java/com/example/sumayerestaurant/
│   │
│   ├── MainActivity.java               (Entry point)
│   │
│   ├── ui/
│   │   ├── login/
│   │   │   ├── LoginActivity.java
│   │   │   ├── LoginViewModel.java
│   │   │   └── login_activity.xml
│   │   │
│   │   ├── dashboard/
│   │   │   ├── DashboardActivity.java
│   │   │   ├── DashboardViewModel.java
│   │   │   └── dashboard_activity.xml
│   │   │
│   │   ├── tables/
│   │   │   ├── TablesFragment.java
│   │   │   ├── TablesViewModel.java
│   │   │   ├── TableAdapter.java
│   │   │   └── tables_fragment.xml
│   │   │
│   │   ├── menu/
│   │   │   ├── MenuFragment.java
│   │   │   ├── MenuViewModel.java
│   │   │   ├── MenuCategoryAdapter.java
│   │   │   ├── MenuItemAdapter.java
│   │   │   └── menu_fragment.xml
│   │   │
│   │   ├── orders/
│   │   │   ├── OrdersFragment.java
│   │   │   ├── OrderDetailActivity.java
│   │   │   ├── OrdersViewModel.java
│   │   │   ├── OrderAdapter.java
│   │   │   ├── orders_fragment.xml
│   │   │   └── order_detail_activity.xml
│   │   │
│   │   ├── cart/
│   │   │   ├── CartFragment.java
│   │   │   ├── CartViewModel.java
│   │   │   ├── CartAdapter.java
│   │   │   └── cart_fragment.xml
│   │   │
│   │   ├── kitchen/
│   │   │   ├── KitchenActivity.java
│   │   │   ├── KitchenViewModel.java
│   │   │   ├── KitchenOrderAdapter.java
│   │   │   └── kitchen_activity.xml
│   │   │
│   │   ├── billing/
│   │   │   ├── BillingActivity.java
│   │   │   ├── BillingViewModel.java
│   │   │   └── billing_activity.xml
│   │   │
│   │   ├── payments/
│   │   │   ├── PaymentActivity.java
│   │   │   ├── PaymentViewModel.java
│   │   │   └── payment_activity.xml
│   │   │
│   │   ├── settings/
│   │   │   ├── SettingsFragment.java
│   │   │   ├── SettingsViewModel.java
│   │   │   └── settings_fragment.xml
│   │   │
│   │   └── theme/
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   │
│   ├── data/
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── Restaurant.java
│   │   │   ├── Branch.java
│   │   │   ├── Table.java
│   │   │   ├── MenuItem.java
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   ├── Payment.java
│   │   │   └── KitchenOrder.java
│   │   │
│   │   ├── api/
│   │   │   ├── ApiClient.java           (Retrofit setup)
│   │   │   ├── ApiService.java          (API endpoints)
│   │   │   ├── OkHttpClient.java        (Interceptor for token)
│   │   │   └── response/
│   │   │       ├── ApiResponse.java
│   │   │       ├── LoginResponse.java
│   │   │       └── ErrorResponse.java
│   │   │
│   │   ├── database/
│   │   │   ├── AppDatabase.java         (Room database)
│   │   │   ├── dao/
│   │   │   │   ├── UserDao.java
│   │   │   │   ├── OrderDao.java
│   │   │   │   ├── MenuItemDao.java
│   │   │   │   └── CartItemDao.java
│   │   │   └── entity/
│   │   │       ├── UserEntity.java
│   │   │       ├── OrderEntity.java
│   │   │       ├── CartItemEntity.java
│   │   │       └── MenuItemEntity.java
│   │   │
│   │   ├── repository/
│   │   │   ├── AuthRepository.java
│   │   │   ├── MenuRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   ├── TableRepository.java
│   │   │   ├── CartRepository.java
│   │   │   └── PaymentRepository.java
│   │   │
│   │   ├── local/
│   │   │   ├── PreferencesManager.java   (SharedPreferences)
│   │   │   └── TokenManager.java         (JWT token storage)
│   │   │
│   │   └── websocket/
│   │       ├── WebSocketManager.java
│   │       └── WebSocketListener.java
│   │
│   ├── utils/
│   │   ├── Constants.java
│   │   ├── DateUtil.java
│   │   ├── ValidationUtil.java
│   │   ├── CurrencyUtil.java
│   │   ├── StringUtil.java
│   │   ├── LogUtil.java
│   │   └── PermissionUtil.java
│   │
│   ├── common/
│   │   ├── BaseActivity.java
│   │   ├── BaseFragment.java
│   │   ├── BaseViewModel.java
│   │   └── Status.java                  (LOADING, SUCCESS, ERROR, EMPTY)
│   │
│   └── app/
│       └── SumayeRestaurantApp.java     (Application class)
│
├── src/main/res/
│   ├── layout/
│   ├── drawable/
│   ├── menu/
│   ├── values/
│   │   ├── strings.xml                  (English & Kiswahili)
│   │   ├── colors.xml
│   │   ├── themes.xml
│   │   ├── dimens.xml
│   │   └── styles.xml
│   ├── values-sw/
│   │   └── strings.xml                  (Kiswahili translations)
│   └── values-night/
│       ├── colors.xml
│       └── themes.xml
│
└── build.gradle.kts
```

---

## 6. REST API ENDPOINT LIST

### Authentication Endpoints
```
POST   /api/auth/login                    (Username + Password → JWT Token)
POST   /api/auth/logout                   (Invalidate token)
POST   /api/auth/refresh-token            (Refresh expired JWT)
POST   /api/auth/register                 (Admin only - create new user)
```

### User Management
```
GET    /api/users                         (List all users - Admin/Manager)
GET    /api/users/{id}                    (Get user details)
PUT    /api/users/{id}                    (Update user)
DELETE /api/users/{id}                    (Delete user - Admin)
GET    /api/users/me                      (Get current logged-in user)
```

### Branch Management
```
GET    /api/branches                      (List branches)
GET    /api/branches/{id}                 (Get branch details)
POST   /api/branches                      (Create branch - Admin/Owner)
PUT    /api/branches/{id}                 (Update branch - Manager+)
DELETE /api/branches/{id}                 (Delete branch - Admin)
```

### Table Management
```
GET    /api/branches/{branchId}/tables              (List tables)
GET    /api/branches/{branchId}/tables/{tableId}    (Get table details)
POST   /api/branches/{branchId}/tables              (Create table)
PUT    /api/branches/{branchId}/tables/{tableId}    (Update table status)
DELETE /api/branches/{branchId}/tables/{tableId}    (Delete table)
```

### Menu Management
```
GET    /api/branches/{branchId}/menu-categories                (List categories)
POST   /api/branches/{branchId}/menu-categories                (Create category)
PUT    /api/branches/{branchId}/menu-categories/{id}           (Update category)
DELETE /api/branches/{branchId}/menu-categories/{id}           (Delete category)

GET    /api/branches/{branchId}/menu-items                     (List menu items)
GET    /api/branches/{branchId}/menu-items/{id}                (Get menu item details)
POST   /api/branches/{branchId}/menu-items                     (Create menu item)
PUT    /api/branches/{branchId}/menu-items/{id}                (Update menu item)
DELETE /api/branches/{branchId}/menu-items/{id}                (Delete menu item)
```

### Recipe Management
```
GET    /api/menu-items/{menuItemId}/recipes                    (Get recipe)
POST   /api/menu-items/{menuItemId}/recipes                    (Create recipe)
PUT    /api/menu-items/{menuItemId}/recipes/{id}               (Update recipe)
POST   /api/recipes/{recipeId}/ingredients                     (Add ingredient to recipe)
DELETE /api/recipes/{recipeId}/ingredients/{ingredientId}      (Remove ingredient)
```

### Order Management
```
POST   /api/branches/{branchId}/orders                         (Create new order)
GET    /api/branches/{branchId}/orders                         (List orders)
GET    /api/branches/{branchId}/orders/{orderId}               (Get order details)
PUT    /api/branches/{branchId}/orders/{orderId}/status        (Update order status)
DELETE /api/branches/{branchId}/orders/{orderId}               (Cancel order)
GET    /api/branches/{branchId}/orders/table/{tableId}         (Get orders for table)

POST   /api/orders/{orderId}/items                             (Add items to order)
PUT    /api/orders/{orderId}/items/{itemId}                    (Update item quantity)
DELETE /api/orders/{orderId}/items/{itemId}                    (Remove item from order)
```

### Kitchen/KOT Management
```
GET    /api/branches/{branchId}/kitchen-orders                 (List KOT)
GET    /api/branches/{branchId}/kitchen-orders/{id}            (Get KOT details)
PUT    /api/branches/{branchId}/kitchen-orders/{id}/status     (Update KOT status)
GET    /api/kitchen-orders/status/new                          (Get new orders)
```

### Payments
```
POST   /api/orders/{orderId}/payments                          (Process payment)
GET    /api/orders/{orderId}/payments                          (Get payment details)
GET    /api/branches/{branchId}/payments                       (List payments)
```

### Inventory Management
```
GET    /api/branches/{branchId}/inventory                      (List inventory)
GET    /api/branches/{branchId}/inventory/{id}                 (Get inventory item)
PUT    /api/branches/{branchId}/inventory/{id}                 (Update stock)
GET    /api/branches/{branchId}/inventory/low-stock            (Get low stock items)

POST   /api/branches/{branchId}/inventory-transactions         (Create transaction)
GET    /api/branches/{branchId}/inventory-transactions         (Get transactions)
```

### Ingredients & Suppliers
```
GET    /api/ingredients                                        (List all ingredients)
POST   /api/ingredients                                        (Create ingredient)
GET    /api/suppliers                                          (List suppliers)
POST   /api/suppliers                                          (Create supplier)

GET    /api/branches/{branchId}/purchases                      (List purchases)
POST   /api/branches/{branchId}/purchases                      (Create purchase)
```

### Reports
```
GET    /api/branches/{branchId}/reports/daily-sales            (Daily sales report)
GET    /api/branches/{branchId}/reports/sales-summary          (Sales by period)
GET    /api/branches/{branchId}/reports/best-sellers           (Best selling items)
GET    /api/branches/{branchId}/reports/inventory              (Inventory report)
GET    /api/branches/{branchId}/reports/expenses               (Expense report)
GET    /api/branches/{branchId}/reports/profit                 (Profit analysis)
```

### Customers & Reservations
```
GET    /api/customers                                          (List customers)
POST   /api/customers                                          (Create customer)
GET    /api/reservations                                       (List reservations)
POST   /api/reservations                                       (Create reservation)
```

### Settings
```
GET    /api/settings                                           (Get app settings)
PUT    /api/settings                                           (Update settings)
GET    /api/tax-rates                                          (Get tax configuration)
```

### WebSocket Endpoints (Real-time)
```
WS     /ws/kitchen/{branchId}                                  (Kitchen order updates)
WS     /ws/waiter/{branchId}                                   (Waiter notifications)
WS     /ws/table/{branchId}/{tableId}                          (Table-specific updates)
```

---

## 7. REQUIRED DEPENDENCIES

### Backend (Spring Boot - Maven)
```xml
<!-- Spring Boot Core -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- MySQL Driver -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Flyway for migrations -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Android (Gradle)
```gradle
dependencies {
    // Android Core
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    
    // Lifecycle & Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.2")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    
    // OkHttp (logging)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    
    // Security (for encryption)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // ViewPager2 (for fragments)
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

---

## 8. PHASE 1 IMPLEMENTATION PLAN

### Phase 1: Backend Setup, Authentication & JWT
**Duration**: 1-2 weeks
**Goals**:
- ✅ MySQL database setup
- ✅ Spring Boot project structure
- ✅ User & Role models
- ✅ JWT authentication
- ✅ Login API endpoint
- ✅ Spring Security configuration
- ✅ Role-based authorization

**Key Components to Build**:
1. Database schema (users, roles, permissions)
2. Entity classes (User, Role, Permission)
3. Repository interfaces
4. JWT token provider
5. AuthService with password hashing (BCrypt)
6. LoginRequest/LoginResponse DTOs
7. AuthController with login endpoint
8. Spring Security configuration
9. JWT filter for request validation
10. Exception handlers

**Deliverables**:
- [ ] MySQL database running
- [ ] Spring Boot backend running
- [ ] Login endpoint tested with Postman
- [ ] JWT token generation & validation working
- [ ] Roles properly assigned
- [ ] Password hashing verified

**Next Step**: Android login UI + token storage (Phase 2)

---

Ready to proceed? I'll now create the complete Phase 1 implementation with all the code files.
