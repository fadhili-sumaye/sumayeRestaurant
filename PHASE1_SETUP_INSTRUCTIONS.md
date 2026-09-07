# Phase 1: Spring Boot Backend Setup Instructions

## Prerequisites
- Java 11 or higher (preferably Java 17 or 21)
- Maven 3.9+
- MySQL 8.0+
- IDE: IntelliJ IDEA or VS Code with Spring Boot extensions
- Git

---

## Step 1: Create Spring Boot Project

### Option A: Using Spring Boot CLI
```bash
spring boot new --type maven --language java --build maven \
  --from-bing-starter https://start.spring.io \
  restaurant-management-api \
  --dependencies web,data-jpa,security,mysql,validation,websocket,lombok
```

### Option B: Using start.spring.io (Recommended for Beginners)
1. Go to https://start.spring.io
2. Fill in:
   - **Project**: Maven Project
   - **Language**: Java
   - **Spring Boot**: 3.2.0 (or latest stable)
   - **Project Metadata**:
     - Group: `com.sumaye`
     - Artifact: `restaurant-management-api`
     - Name: `Restaurant Management API`
     - Package name: `com.sumaye.restaurant`
     - Packaging: `Jar`
     - Java: 17 (or your version)

3. **Dependencies** to add:
   - Spring Web
   - Spring Data JPA
   - MySQL Driver
   - Spring Security
   - Validation
   - Spring WebSocket
   - Lombok
   - (Optional: Flyway Migration)

4. Click "GENERATE" and extract the ZIP file

### Option C: Manual Maven Project Creation
```bash
mkdir restaurant-management-api
cd restaurant-management-api

# Create basic structure
mvn archetype:generate \
  -DgroupId=com.sumaye.restaurant \
  -DartifactId=restaurant-management-api \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
```

---

## Step 2: Database Setup

### Create MySQL Database
```sql
CREATE DATABASE restaurant_management;
CREATE USER 'restaurant'@'localhost' IDENTIFIED BY 'restaurant123';
GRANT ALL PRIVILEGES ON restaurant_management.* TO 'restaurant'@'localhost';
FLUSH PRIVILEGES;
```

### Import Schema
```bash
mysql -u restaurant -p restaurant_management < database_schema_phase1.sql
```

### Verify
```sql
USE restaurant_management;
SELECT COUNT(*) as total_tables FROM information_schema.tables 
WHERE table_schema = 'restaurant_management';

-- Should show 9 tables in Phase 1
```

---

## Step 3: Project Structure

Create the following folder structure in `src/main/java/com/sumaye/restaurant/`:

```
src/main/java/com/sumaye/restaurant/
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── CorsConfig.java
├── controller/
│   └── AuthController.java
├── service/
│   ├── AuthService.java
│   └── UserService.java
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── PermissionRepository.java
├── model/
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Permission.java
│   │   ├── Restaurant.java
│   │   └── Branch.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── LoginResponse.java
│       └── ErrorResponse.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtTokenFilter.java
│   ├── CustomUserDetailsService.java
│   └── SecurityUtil.java
├── exception/
│   ├── ApiException.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── GlobalExceptionHandler.java
├── util/
│   └── Constants.java
└── RestaurantManagementApplication.java

src/main/resources/
├── application.properties
├── application-dev.properties
└── application-prod.properties
```

---

## Step 4: Update pom.xml

Replace the `<dependencies>` section in `pom.xml` with:

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.0.33</version>
        <scope>runtime</scope>
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

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## Step 5: Application Properties

### File: `src/main/resources/application.properties`

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=restaurant
spring.datasource.password=restaurant123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# Logging
logging.level.root=INFO
logging.level.com.sumaye.restaurant=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# JWT Configuration
jwt.secret=your-secret-key-change-this-in-production-make-it-long-and-random
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# CORS Configuration
cors.allowed-origins=http://localhost:3000,http://localhost:4200
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.max-age=3600

# Application Name
spring.application.name=Restaurant Management API

# Jackson Configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=Africa/Dar_es_Salaam

# Error Handling
server.error.include-message=always
server.error.include-binding-errors=always
```

### File: `src/main/resources/application-dev.properties`

```properties
# Development Profile
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.com.sumaye.restaurant=DEBUG
logging.level.org.springframework.security=DEBUG
```

### File: `src/main/resources/application-prod.properties`

```properties
# Production Profile
spring.jpa.show-sql=false
logging.level.com.sumaye.restaurant=WARN
logging.level.org.springframework.security=WARN
jwt.secret=${JWT_SECRET}
spring.datasource.hikari.maximum-pool-size=20
```

---

## Step 6: Build and Run

```bash
# Navigate to project directory
cd restaurant-management-api

# Clean and build
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/restaurant-management-api-0.0.1-SNAPSHOT.jar
```

### Expected Output
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2024-01-15 10:30:45.123  INFO 12345 --- [main] com.sumaye.restaurant.RestaurantManagementApplication
Started RestaurantManagementApplication in 4.567 seconds (JVM running for 5.234)
```

---

## Step 7: Verify Database Connection

Test with:
```bash
curl http://localhost:8080/api/auth/health
```

Should return:
```json
{
  "status": "UP",
  "database": "MySQL 8.0"
}
```

---

## Troubleshooting

### Issue: Cannot connect to MySQL
**Solution**: 
```bash
# Check if MySQL is running
mysql -u restaurant -p

# Verify credentials match in application.properties
# Ensure database is created
mysql -u root -p -e "SHOW DATABASES;"
```

### Issue: Port 8080 already in use
**Solution**: 
```properties
# Change port in application.properties
server.port=8081
```

### Issue: Hibernate validation error
**Solution**: 
```bash
# Ensure ddl-auto is set to 'validate' only after schema is created
# First time: use 'validate' only if schema already exists
# If schema doesn't exist, use 'create' or import SQL manually
```

### Issue: JWT not working
**Solution**: 
```properties
# Ensure jwt.secret is set to a long random string
# Change default:
jwt.secret=ThisIsAVeryLongSecretKeyThatShouldBeAtLeast32CharactersForHS256Algorithm
```

---

## Next Steps

After confirming the backend runs successfully:

1. ✅ Database running and schema created
2. ✅ Spring Boot starts without errors
3. ✅ Can connect to MySQL
4. Create entity classes (User, Role, Permission, etc.)
5. Create repositories
6. Implement JWT token provider
7. Create AuthService with BCrypt password hashing
8. Create AuthController with login endpoint
9. Test login endpoint with Postman

---

## Important Notes for Phase 1

⚠️ **Security**: 
- Change JWT secret to a long random string in production
- Use environment variables for sensitive data
- Enable HTTPS in production
- Implement rate limiting for login endpoint

⚠️ **Database**:
- Always backup before schema changes
- Use proper foreign keys
- Create indexes for frequently queried columns
- Monitor connection pool settings

⚠️ **Passwords**:
- Sample passwords in database are for testing only
- Always hash passwords with BCrypt
- Never store plaintext passwords
- Implement password expiration policies

---

## Verification Checklist

- [ ] Java installed and version ≥ 11
- [ ] Maven installed and working
- [ ] MySQL running
- [ ] Database created and schema imported
- [ ] Spring Boot project created
- [ ] Dependencies added to pom.xml
- [ ] application.properties configured
- [ ] Project builds without errors
- [ ] Application starts successfully
- [ ] Database connection verified

**Proceed to Phase 1 Implementation after all checks pass.**
