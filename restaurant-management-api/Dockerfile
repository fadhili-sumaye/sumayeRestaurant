# ===================================================================
# MULTI-STAGE DOCKERFILE FOR SPRING BOOT BACKEND
# Stage 1: Build the JAR with Maven
# Stage 2: Create a secure, lightweight runtime container
# ===================================================================

# -------------------------------------------------------------------
# STAGE 1: BUILD
# -------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production package
COPY src ./src
RUN mvn clean package -DskipTests -B

# -------------------------------------------------------------------
# STAGE 2: RUNTIME
# -------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Security: Create non-root system user and group
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Server port
EXPOSE 8080

# Configure JVM memory limits & environment options
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Healthcheck for container orchestrators
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
