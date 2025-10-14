---
description: Repository Information Overview
alwaysApply: true
---

# Rental Application Information

## Summary
A Jmix-based rental property management application with RentCast API integration for property data, value estimates, and rent estimates. Built with Jmix 2.6.2, it includes comprehensive caching, address normalization, and scheduled maintenance features.

## Structure
- **src/main** - Main application source code
  - **java/com/non/rental** - Java source files
  - **resources** - Configuration and resource files
- **src/test** - Test source code
- **gradle** - Gradle wrapper files

## Language & Runtime
**Language**: Java
**Version**: Java 17+
**Framework**: Jmix 2.6.2, Spring Boot
**Build System**: Gradle
**Package Manager**: Gradle (Java), npm (Frontend)

## Dependencies

### Backend Dependencies
- **Jmix Core**: `io.jmix.core:jmix-core-starter`
- **Jmix Data**: `io.jmix.data:jmix-eclipselink-starter`
- **Jmix Security**: `io.jmix.security:jmix-security-starter`
- **Jmix FlowUI**: `io.jmix.flowui:jmix-flowui-starter`
- **Spring Boot**: `org.springframework.boot:spring-boot-starter-web`
- **Jackson**: `com.fasterxml.jackson.core:jackson-databind`
- **PostgreSQL**: `org.postgresql:postgresql`

### Frontend Dependencies
- **Vaadin**: `@vaadin/bundles:24.7.12`
- **React**: `react:18.3.1`, `react-dom:18.3.1`
- **Lit**: `lit:3.3.0`
- **TypeScript**: `typescript:5.7.3`

## Build & Installation
```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

## Main Application Components

### Entry Point
The main application entry point is `RentalApplication.java`, which is a standard Spring Boot application with Jmix and Vaadin Flow UI configuration.

### Key Features
- **RentCast API Integration** - Property data, value estimates, and rent estimates
- **Smart Address Normalization** - Prevents duplicate API calls
- **Two-Level Caching** - In-memory (Spring Cache) + Database (PostgreSQL)
- **REST API Endpoints** - HTTP access to functionality
- **Scheduled Jobs** - Automatic cache maintenance

## Testing
**Framework**: JUnit 5 with Spring Boot Test
**Test Location**: `src/test/java/com/non/rental`
**Naming Convention**: `*Test.java` for unit tests, `*IntegrationTest.java` for integration tests
**Run Command**:
```bash
./gradlew test
```

### Test Types
- **Unit Tests**: Tests for individual components
- **Integration Tests**: Tests for API integration with RentCast
- **UI Tests**: Tests for Vaadin Flow UI components

## Database
**Database**: PostgreSQL
**Configuration**: Spring Boot DataSource with Hikari connection pool
**Entities**: JPA entities with EclipseLink as the provider

## API Integration
**External API**: RentCast API for property data
**Base URL**: https://api.rentcast.io/v1
**Authentication**: API key-based authentication
**Caching**: 30-day TTL with proactive refresh