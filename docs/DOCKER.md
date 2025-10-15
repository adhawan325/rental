# Docker Deployment Guide for Jmix Rental Application

This guide explains how to deploy the Jmix Rental application using Docker and Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 2GB of available RAM
- RentCast API key (for production use)

## Quick Start

### 1. Environment Setup

Copy the example environment file and configure your settings:

```bash
cp .env.example .env
```

Edit `.env` and update the values, especially:
- `DB_PASSWORD` - Set a secure database password
- `RENTCAST_API_KEY` - Add your RentCast API key

### 2. Start the Application

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f rental

# Check service status
docker-compose ps
```

### 3. Access the Application

Once the services are healthy (may take 1-2 minutes):
- **Application URL**: http://localhost:8080
- **Default Credentials**: 
  - Username: `admin`
  - Password: `admin`

### 4. Stop the Application

```bash
# Stop services
docker-compose down

# Stop and remove volumes (WARNING: deletes all data)
docker-compose down -v
```

## Service Architecture

The docker-compose configuration includes two services:

### 1. `postgres` - PostgreSQL Database
- **Image**: postgres:15-alpine
- **Port**: 5432
- **Database**: rental
- **Volume**: `postgres_data` (persistent storage)

### 2. `rental` - Jmix Application
- **Build**: Multi-stage Dockerfile
- **Port**: 8080
- **Volume**: `rental_storage` (file storage)
- **Depends on**: postgres (with health check)

## Jmix-Specific Configuration

### Environment Variables

The application uses Jmix-standard environment variable naming:

#### Datasource Configuration
```yaml
MAIN_DATASOURCE_URL: jdbc:postgresql://postgres:5432/rental
MAIN_DATASOURCE_USERNAME: postgres
MAIN_DATASOURCE_PASSWORD: ${DB_PASSWORD}
```

These map to Jmix properties:
- `main.datasource.url`
- `main.datasource.username`
- `main.datasource.password`

#### File Storage
```yaml
JMIX_LOCALFS_STORAGE_DIR: /opt/rental/storage
```

Maps to: `jmix.localfs.storage-dir`

#### Custom Application Properties
All custom properties (like RentCast API configuration) use dot notation converted to uppercase with underscores:
- `rentcast.api.key` → `RENTCAST_API_KEY`
- `rentcast.api.base-url` → `RENTCAST_API_BASE_URL`

## Development vs Production

### Development Mode (Default)
```bash
# Uses dev profile with debug logging
docker-compose up -d
```

### Production Mode
```bash
# Set production profile
export SPRING_PROFILES_ACTIVE=prod
docker-compose up -d
```

Or update `.env`:
```
SPRING_PROFILES_ACTIVE=prod
```

## Health Checks

Both services include health checks:

### PostgreSQL
- **Check**: `pg_isready -U postgres`
- **Interval**: 10s
- **Timeout**: 5s
- **Retries**: 5

### Rental Application
- **Check**: `curl -f http://localhost:8080/actuator/health`
- **Interval**: 30s
- **Timeout**: 10s
- **Start Period**: 90s (allows time for startup)
- **Retries**: 5

## Troubleshooting

### Check Service Health
```bash
docker-compose ps
```

### View Application Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f rental
docker-compose logs -f postgres
```

### Database Connection Issues
```bash
# Check if postgres is ready
docker-compose exec postgres pg_isready -U postgres

# Connect to database
docker-compose exec postgres psql -U postgres -d rental
```

### Application Won't Start
1. Check if postgres is healthy: `docker-compose ps`
2. Verify environment variables: `docker-compose config`
3. Check application logs: `docker-compose logs rental`
4. Ensure ports 5432 and 8080 are not in use

### Reset Everything
```bash
# Stop and remove all containers, networks, and volumes
docker-compose down -v

# Remove built images
docker-compose down --rmi all -v

# Start fresh
docker-compose up -d --build
```

## Resource Management

### Memory Configuration

The application JVM is configured with:
```yaml
JAVA_OPTS: "-Xmx1g -Xms512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

Adjust based on your needs:
- **Development**: 512MB-1GB heap
- **Production**: 2GB-4GB heap (adjust `-Xmx` and `-Xms`)

### Volume Management

```bash
# List volumes
docker volume ls | grep rental

# Inspect volume
docker volume inspect rental_postgres_data
docker volume inspect rental_rental_storage

# Backup database volume
docker run --rm -v rental_postgres_data:/data -v $(pwd):/backup \
  alpine tar czf /backup/postgres_backup.tar.gz /data

# Restore database volume
docker run --rm -v rental_postgres_data:/data -v $(pwd):/backup \
  alpine tar xzf /backup/postgres_backup.tar.gz -C /
```

## Network Configuration

The application uses a custom bridge network `rental-network` for service communication.

Services communicate using service names:
- Application connects to database at: `postgres:5432`
- External access via: `localhost:5432` and `localhost:8080`

## Security Considerations

### Production Deployment Checklist

- [ ] Change default admin password
- [ ] Use strong database password
- [ ] Secure RentCast API key
- [ ] Use HTTPS (add reverse proxy like nginx)
- [ ] Restrict database port (remove `ports` mapping)
- [ ] Enable firewall rules
- [ ] Regular security updates
- [ ] Implement backup strategy
- [ ] Monitor logs and health endpoints

### Restrict Database Access

For production, remove the postgres port mapping:

```yaml
# Comment out or remove this in docker-compose.yml
# ports:
#   - "5432:5432"
```

This prevents external access to the database.

## Maintenance

### Update Application
```bash
# Pull latest code
git pull

# Rebuild and restart
docker-compose up -d --build rental
```

### Database Migrations

Jmix uses Liquibase for database migrations. Migrations run automatically on startup.

To check migration status:
```bash
docker-compose logs rental | grep liquibase
```

### Backup Strategy

1. **Database Backup** (automated):
```bash
# Create backup script
cat > backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="./backups"
mkdir -p $BACKUP_DIR
docker-compose exec -T postgres pg_dump -U postgres rental > \
  $BACKUP_DIR/rental_$(date +%Y%m%d_%H%M%S).sql
EOF

chmod +x backup.sh

# Add to crontab for daily backups
# 0 2 * * * /path/to/backup.sh
```

2. **File Storage Backup**:
```bash
docker run --rm -v rental_rental_storage:/data -v $(pwd)/backups:/backup \
  alpine tar czf /backup/storage_$(date +%Y%m%d_%H%M%S).tar.gz /data
```

## Performance Tuning

### PostgreSQL Tuning

Add to postgres service in docker-compose.yml:
```yaml
command:
  - "postgres"
  - "-c"
  - "max_connections=200"
  - "-c"
  - "shared_buffers=256MB"
  - "-c"
  - "effective_cache_size=1GB"
```

### Application Tuning

Adjust JVM options based on load:
```yaml
JAVA_OPTS: "-Xmx2g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

## Support

For issues specific to:
- **Jmix Framework**: https://docs.jmix.io
- **Docker**: https://docs.docker.com
- **Application**: Check application logs and GitHub issues