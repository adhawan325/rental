# Docker Compose Jmix Compliance Changes

## Summary

The `docker-compose.yml` file has been updated to be fully Jmix-compliant, following Jmix framework conventions and best practices.

## Key Changes

### 1. Service Naming ✅

**Before:**
```yaml
services:
  rental-app:
    container_name: rental-app-dev
```

**After:**
```yaml
services:
  rental:  # Matches Jmix project ID from build.gradle
    container_name: rental-app
```

**Why:** The service name now matches the Jmix project ID (`rental`) defined in `build.gradle`, making it consistent with Jmix conventions.

### 2. Environment Variable Naming ✅

**Before:**
```yaml
DATABASE_URL: jdbc:postgresql://postgres:5432/rental
DATABASE_USERNAME: postgres
DATABASE_PASSWORD: ${DB_PASSWORD}
```

**After:**
```yaml
MAIN_DATASOURCE_URL: jdbc:postgresql://postgres:5432/rental
MAIN_DATASOURCE_USERNAME: postgres
MAIN_DATASOURCE_PASSWORD: ${DB_PASSWORD}
```

**Why:** Jmix uses specific property naming conventions:
- `main.datasource.url` → `MAIN_DATASOURCE_URL`
- `main.datasource.username` → `MAIN_DATASOURCE_USERNAME`
- `main.datasource.password` → `MAIN_DATASOURCE_PASSWORD`

### 3. Jmix-Specific Configuration ✅

Added proper Jmix configuration variables:

```yaml
# Jmix File Storage
JMIX_LOCALFS_STORAGE_DIR: /opt/rental/storage

# Vaadin Configuration
VAADIN_LAUNCH_BROWSER: "false"

# Server Configuration
SERVER_PORT: 8080
```

### 4. Application Properties Mapping ✅

All custom properties now follow Spring Boot environment variable conventions:

| Property File | Environment Variable |
|--------------|---------------------|
| `rentcast.api.key` | `RENTCAST_API_KEY` |
| `rentcast.api.base-url` | `RENTCAST_API_BASE_URL` |
| `rentcast.api.timeout` | `RENTCAST_API_TIMEOUT` |
| `rentcast.api.max-retries` | `RENTCAST_API_MAX_RETRIES` |
| `rentcast.cache.ttl-days` | `RENTCAST_CACHE_TTL_DAYS` |
| `rentcast.cache.enabled` | `RENTCAST_CACHE_ENABLED` |

### 5. Project Name Configuration ✅

Added `COMPOSE_PROJECT_NAME=rental` to `.env` file to set the Docker Compose project name to match the Jmix project ID.

### 6. Container Naming ✅

Simplified container names:
- `rental-postgres-dev` → `rental-postgres`
- `rental-app-dev` → `rental-app`

## New Files Created

### 1. `.env` (Active Environment File)
Contains actual environment variables used by docker-compose:
```bash
COMPOSE_PROJECT_NAME=rental
DB_PASSWORD=rental_dev_password
SPRING_PROFILES_ACTIVE=dev
RENTCAST_API_KEY=34fb2dfe78fb4d67a8a3fd6446e9fd73
# ... more variables
```

### 2. `.env.example` (Template)
Template for developers to create their own `.env` file:
```bash
cp .env.example .env
# Edit .env with your values
```

### 3. `DOCKER.md` (Comprehensive Guide)
Complete documentation covering:
- Quick start guide
- Service architecture
- Jmix-specific configuration
- Development vs Production modes
- Health checks
- Troubleshooting
- Resource management
- Security considerations
- Maintenance and backup strategies
- Performance tuning

### 4. `docker-compose.override.yml.example` (Local Overrides)
Example file for local development customizations:
```bash
cp docker-compose.override.yml.example docker-compose.override.yml
# Customize for your local environment
```

### 5. `Makefile` (Convenience Commands)
Quick commands for common Docker operations:
```bash
make up        # Start services
make down      # Stop services
make logs      # View logs
make health    # Check health
make backup    # Backup database
make rebuild   # Clean rebuild
```

## Usage Examples

### Basic Usage
```bash
# Start the application
docker-compose up -d

# View logs
docker-compose logs -f rental

# Check status
docker-compose ps

# Stop the application
docker-compose down
```

### Using Make Commands
```bash
# Start services
make up

# View application logs
make logs

# Check health
make health

# Backup database
make backup

# Clean rebuild
make rebuild
```

### Using Environment Variables
```bash
# Use custom environment file
docker-compose --env-file .env.production up -d

# Override specific variables
DB_PASSWORD=secure_password docker-compose up -d

# Production mode
SPRING_PROFILES_ACTIVE=prod docker-compose up -d
```

## Validation

The configuration has been validated:

```bash
# Validate docker-compose.yml syntax
docker-compose config

# Output shows all services properly configured
✓ Services: postgres, rental
✓ Networks: rental-network
✓ Volumes: postgres_data, rental_storage
✓ Environment variables properly set
✓ Health checks configured
```

## Benefits

1. **Jmix Convention Compliance**: Follows Jmix framework naming and configuration standards
2. **Consistency**: Service name matches project ID across all configurations
3. **Maintainability**: Clear, documented environment variables
4. **Flexibility**: Easy to override settings for different environments
5. **Developer Experience**: Makefile and comprehensive documentation
6. **Production Ready**: Proper health checks, resource limits, and security considerations

## Migration Guide

If you have existing containers running:

```bash
# Stop old containers
docker-compose down

# Remove old volumes (if needed)
docker-compose down -v

# Start with new configuration
docker-compose up -d
```

## Environment Variable Reference

### Required Variables
- `DB_PASSWORD` - Database password
- `RENTCAST_API_KEY` - RentCast API key (for production)

### Optional Variables
- `SPRING_PROFILES_ACTIVE` - Spring profile (default: dev)
- `RENTCAST_API_BASE_URL` - API base URL (default: https://api.rentcast.io/v1)
- `RENTCAST_API_TIMEOUT` - API timeout in ms (default: 30000)
- `RENTCAST_API_MAX_RETRIES` - Max retry attempts (default: 3)
- `RENTCAST_CACHE_TTL_DAYS` - Cache TTL in days (default: 7)
- `RENTCAST_CACHE_ENABLED` - Enable caching (default: true)

## Testing

To test the configuration:

```bash
# Validate configuration
docker-compose config

# Start services
docker-compose up -d

# Wait for services to be healthy
sleep 30

# Check health
curl http://localhost:8080/actuator/health

# Expected output:
# {"status":"UP"}

# Access application
open http://localhost:8080
```

## Support

For issues or questions:
1. Check `DOCKER.md` for detailed documentation
2. Review logs: `docker-compose logs rental`
3. Validate configuration: `docker-compose config`
4. Check service health: `make health`

## References

- Jmix Documentation: https://docs.jmix.io
- Spring Boot Environment Variables: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config
- Docker Compose Documentation: https://docs.docker.com/compose/