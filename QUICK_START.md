# Restaurant Management System - Quick Start Guide

## 📋 Documents Created for Phase 1

I have prepared comprehensive documentation and code for your Restaurant Management System. Here's what has been created:

### 1. **ARCHITECTURE_PLAN.md**
   - Complete system architecture diagram
   - Database ERD and design
   - ERD explanation
   - Spring Boot folder structure
   - Android folder structure
   - REST API endpoint list (50+ endpoints)
   - Required dependencies for backend and Android
   - Phase 1 implementation plan

### 2. **database_schema_phase1.sql**
   - Complete MySQL schema with 9 tables
   - Foreign keys and indexes
   - Default roles and permissions
   - Sample test data (admin, manager, waiter users)
   - Proper relationships and constraints
   - Ready to import into MySQL

### 3. **PHASE1_SETUP_INSTRUCTIONS.md**
   - Step-by-step backend setup guide
   - Database creation instructions
   - Maven project setup (3 options)
   - Dependency configuration
   - Properties file configuration
   - Build and run instructions
   - Troubleshooting guide
   - Verification checklist

### 4. **PHASE1_IMPLEMENTATION.md**
   - 29 complete Java source files
   - Entity classes (5): Restaurant, Branch, Role, Permission, User
   - DTOs (4): LoginRequest, LoginResponse, UserDTO, ErrorResponse
   - Repositories (5): RestaurantRepository, BranchRepository, UserRepository, RoleRepository, PermissionRepository
   - Services (2): AuthService, UserService
   - Controllers (1): AuthController
   - Security classes (4): JwtTokenProvider, JwtTokenFilter, CustomUserDetailsService, SecurityUtil
   - Configuration (2): SecurityConfig, CorsConfig
   - Exception handling (4): ApiException, ResourceNotFoundException, UnauthorizedException, GlobalExceptionHandler
   - Main application class
   - Testing instructions with cURL

---

## 🚀 Getting Started - Step by Step

### Phase 1A: Backend Setup (This Week)

#### Step 1: Prerequisites
```bash
# Verify Java installation
java -version

# Verify Maven installation
mvn -version

# Verify MySQL is running
mysql --version
```

#### Step 2: Create MySQL Database
```bash
# Connect to MySQL
mysql -u root -p

# Run the SQL file
mysql -u root -p < database_schema_phase1.sql

# Verify database was created
mysql -u restaurant -p restaurant_management -e "SELECT COUNT(*) as tables FROM information_schema.tables WHERE table_schema='restaurant_management';"
```

Expected output: Should show 9 tables

#### Step 3: Create Spring Boot Project
Go to https://start.spring.io and:
- Project: Maven
- Language: Java
- Spring Boot: 3.2.0 (latest stable)
- Group: `com.sumaye`
- Artifact: `restaurant-management-api`
- Java: 17
- Add Dependencies:
  - Spring Web
  - Spring Data JPA
  - MySQL Driver
  - Spring Security
  - Validation
  - Spring WebSocket
  - Lombok

Download and extract the ZIP file.

#### Step 4: Copy Code Files
Create all 29 Java files from **PHASE1_IMPLEMENTATION.md** in the correct package structure:
```
src/main/java/com/sumaye/restaurant/
├── config/
├── controller/
├── service/
├── repository/
├── model/
├── security/
├── exception/
├── util/
└── RestaurantManagementApplication.java
```

#### Step 5: Update Configuration
Update `src/main/resources/application.properties` with:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_management?useSSL=false&serverTimezone=UTC
spring.datasource.username=restaurant
spring.datasource.password=restaurant123
jwt.secret=ThisIsAVeryLongSecretKeyThatShouldBeAtLeast32CharactersForHS256Algorithm
```

#### Step 6: Build and Run
```bash
# Clean build
mvn clean package -DskipTests

# Run
mvn spring-boot:run
```

Expected: Server starts on port 8080

#### Step 7: Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Expected: Returns JWT token and user details

#### Test Users Available
- **admin** / **admin123** - Full access (ROLE_ADMIN)
- **manager** / **manager123** - Manager access (ROLE_MANAGER)
- **waiter** / **waiter123** - Waiter access (ROLE_WAITER)

(Password hashes are already set in the database)

---

### Phase 1B: Android Setup (After Backend Works)

Once Phase 1 backend is working:

#### Step 1: Create Android Project
- Open Android Studio
- File → New → New Project
- Select "Empty Activity"
- Name: `SumayeRestaurant`
- Package: `com.sumaye.restaurant`
- Language: Java
- SDK: API 35

#### Step 2: Update Dependencies
In `build.gradle.kts` (app level), add dependencies from **ARCHITECTURE_PLAN.md**

#### Step 3: Create Data Models
From **PHASE1_IMPLEMENTATION.md**:
- User.java
- LoginRequest.java
- LoginResponse.java

#### Step 4: Create API Service
Create Retrofit client and API endpoints

#### Step 5: Create Login UI
- LoginActivity
- login_activity.xml layout

#### Step 6: Test Connection
Run app and test login with admin credentials

---

## ✅ Phase 1 Verification Checklist

### Backend
- [ ] Java 11+ installed
- [ ] Maven 3.9+ installed
- [ ] MySQL 8.0+ running
- [ ] Database created with schema
- [ ] Spring Boot project created
- [ ] All 29 Java files created in correct structure
- [ ] application.properties configured
- [ ] Project builds without errors
- [ ] Application starts (no errors in console)
- [ ] Can connect to database (no connection errors)
- [ ] Login endpoint responds with JWT token
- [ ] All 3 test users can login

### Android
- [ ] Android Studio installed
- [ ] Android SDK 35 installed
- [ ] Project created
- [ ] Gradle dependencies added
- [ ] Models created
- [ ] API client configured
- [ ] Can reach backend (network working)
- [ ] Login works with JWT token

---

## 📊 Phase 1 Summary

### What You Have:
1. ✅ User authentication system with JWT
2. ✅ Role-based authorization
3. ✅ Multiple user roles (Admin, Owner, Manager, Waiter, Cashier, Kitchen)
4. ✅ Secure password hashing with BCrypt
5. ✅ Comprehensive error handling
6. ✅ CORS configuration for mobile access
7. ✅ Database with proper relationships
8. ✅ Ready for Android integration

### What's Missing (Phase 2+):
- Menu management
- Order management
- Table management
- Kitchen operations (KOT)
- Inventory management
- Payment processing
- Reporting
- WebSocket for real-time updates
- QR code ordering
- Multi-branch support

---

## ⚠️ Important Notes

### Security
- Change JWT secret to a strong random string before production
- Use environment variables for sensitive data
- Never commit passwords or secrets to git
- Enable HTTPS in production

### Database
- Backup your database regularly
- Sample passwords are for testing only
- Never expose database credentials in code

### Testing
- Use Postman to test APIs during development
- Keep test user data in database for development
- Create separate test database if needed

---

## 📞 Troubleshooting

### Issue: Maven build fails
```
Solution: Run "mvn clean install" and check Java version with "java -version"
```

### Issue: Cannot connect to MySQL
```
Solution: Verify MySQL is running and credentials in application.properties are correct
```

### Issue: JWT token not validating
```
Solution: Ensure jwt.secret in application.properties is long enough (32+ chars)
```

### Issue: CORS errors on Android
```
Solution: Verify CORS configuration in application.properties matches your Android app
```

---

## 🎯 Next Steps

1. **This Week**: Complete Phase 1 backend setup
2. **Next Week**: Verify all endpoints work with Postman
3. **Week 3**: Create Android login UI and test
4. **Week 4**: Move to Phase 2 (Branches, Tables, Menu)

---

## 📚 Files Reference

| File | Purpose | Lines |
|------|---------|-------|
| ARCHITECTURE_PLAN.md | System design overview | ~400 |
| database_schema_phase1.sql | MySQL schema | ~200 |
| PHASE1_SETUP_INSTRUCTIONS.md | Installation guide | ~300 |
| PHASE1_IMPLEMENTATION.md | Complete Java code | ~2000 |

**Total Code Lines**: ~3000 lines of production-ready Java code

---

## 🔗 Important Database Credentials

**Development Database**
```
Host: localhost
Port: 3306
Database: restaurant_management
Username: restaurant
Password: restaurant123
```

**Test Users**
```
Admin:    admin / admin123
Manager:  manager / manager123
Waiter:   waiter / waiter123
```

---

## 💡 Key Design Decisions

1. **No Firebase**: All authentication done with Spring Security + JWT
2. **Role-Based Access Control**: Granular permissions for each role
3. **Proper Database Design**: Normalized schema with foreign keys
4. **Scalable Architecture**: Ready for multi-branch operations
5. **Mobile-Friendly**: CORS configured for Android app
6. **Security First**: Passwords hashed with BCrypt, JWT for API authentication

---

## ❓ Questions to Verify

Before proceeding:
1. Can you start the Spring Boot application without errors?
2. Can you login with admin user and get a JWT token?
3. Can you see all 9 tables in the database?
4. Are all Java files created in correct package structure?

**Once all questions are "YES", Phase 1 is complete.**

---

## 📝 Next Phase Preview

### Phase 2: Android Dashboard (1-2 weeks)
- Login screen with JWT token storage
- Dashboard with user info
- Navigation between screens
- Local database with Room
- Token refresh mechanism

### Phase 3: Core Features (2-3 weeks)
- Branch and table management
- Menu categories and items
- Basic order management

### Phase 4: Order & Kitchen (2-3 weeks)
- Waiter ordering system
- Kitchen order tickets
- WebSocket for real-time updates
- Order status tracking

### Phase 5+: Advanced Features
- Billing and payments
- Inventory management
- Reports and analytics
- QR code ordering
- Multi-branch support

---

**You're all set! Start with Step 1 of Phase 1A and let me know when the backend is running.** 🚀

For any specific files or more detailed instructions, ask and I'll provide them immediately.
