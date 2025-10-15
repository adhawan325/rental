# ============================================
# Dockerfile for Jmix Rental Application
# ============================================
# Multi-stage build for optimized image size

# Stage 1: Build the application
FROM gradle:8.7-jdk17 AS builder

WORKDIR /app

# Copy Gradle files first for better caching
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src
COPY frontend ./frontend
COPY package.json tsconfig.json vite.config.ts types.d.ts ./

# Build the application
RUN gradle bootJar --no-daemon -x test

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-jammy

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r rental && useradd -r -g rental rental

# Create application directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create storage directory
RUN mkdir -p /opt/rental/storage && \
    chown -R rental:rental /app /opt/rental

# Switch to non-root user
USER rental

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]