# =========================================================================
# Multi-Stage Dockerfile for Railway / Cloud Deployment
# IoT-Based Home Automation System using ESP32 & Spring Boot 3
# =========================================================================

# Stage 1: Build JAR using Maven with Eclipse Temurin JDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Cache Maven dependencies
COPY backend/pom.xml backend/pom.xml
WORKDIR /workspace/backend
RUN mvn dependency:go-offline -B || true

# Copy source code and build production fat JAR
COPY backend/src src
RUN mvn clean package -DskipTests

# Stage 2: Minimal, secure JRE runtime container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-privileged user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built artifact from builder stage
COPY --from=builder /workspace/backend/target/iot-home-automation-1.0.0.jar app.jar

# Railway injects $PORT dynamically; default fallback to 8080
ENV PORT=8080
EXPOSE 8080

# Java runtime optimizations for container environments
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
