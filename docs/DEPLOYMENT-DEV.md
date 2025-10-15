# 🚀 Development Environment Deployment Guide

## AWS EC2 Deployment using Jmix Studio Quick Cloud Deployment

This guide covers deploying your Rental application to AWS EC2 for development/staging purposes using Jmix Studio's built-in Quick Cloud Deployment feature.

---

## 📋 Prerequisites

### 1. AWS Account Setup
- Active AWS account with access to AWS Console
- AWS Access Key ID and Secret Access Key
- Permissions to create EC2 instances, Security Groups, and Key Pairs

### 2. Local Development Environment
- IntelliJ IDEA with Jmix Studio plugin installed
- Docker Desktop installed and running
- Docker plugin enabled in IntelliJ IDEA

### 3. Application Requirements
- Java 17+
- Gradle 8.5+
- PostgreSQL 15+ (will be deployed in Docker)

---

## 🎯 Deployment Options

### **Option 1: Jmix Studio Quick Cloud Deployment (Recommended for Dev)**

This is the easiest method for development environments.

#### Step 1: Prepare Your Application

1. **Ensure all files are in place:**
   ```bash
   # Check that these files exist:
   ls -la Dockerfile
   ls -la docker-compose.yml
   ls -la src/main/resources/application-dev.properties
   ```

2. **Create environment file:**
   ```bash
   cp .env.example .env
   ```

3. **Edit `.env` file with your credentials:**
   ```bash
   # Edit the file
   nano .env
   
   # Set your values:
   DB_PASSWORD=your_secure_password_here
   RENTCAST_API_KEY=your_rentcast_api_key
   ```

#### Step 2: Deploy via Jmix Studio

1. **Open Jmix Tool Window** in IntelliJ IDEA
   - View → Tool Windows → Jmix

2. **Navigate to Deployment**
   - In Jmix tool window, expand "Deployment" node
   - Click "Quick Cloud Deployment"

3. **Configure Deployment Settings**
   
   **Docker Compose File:**
   - Select or generate: `docker-compose.yml` (already created)
   
   **AWS Configuration:**
   - **Region**: Choose closest to your users (e.g., `us-east-1`)
   - **Instance Type**: 
     - Dev: `t3.medium` (2 vCPU, 4 GB RAM) - ~$30/month
     - Staging: `t3.large` (2 vCPU, 8 GB RAM) - ~$60/month
   - **Key Pair**: Create new or select existing
   - **Security Group**: Will be created automatically with ports:
     - 22 (SSH)
     - 8080 (Application)
     - 5432 (PostgreSQL - optional, for external access)

4. **Start Deployment**
   - Click "Deploy" button
   - Studio will:
     - Create EC2 instance
     - Install Docker and Docker Compose
     - Upload your application
     - Start containers
     - Configure security groups

5. **Access Your Application**
   - Once deployed, you'll get the public IP/DNS
   - Access at: `http://<ec2-public-ip>:8080`
   - Default login: `admin` / `admin`

---

### **Option 2: Manual Docker Deployment (More Control)**

If you prefer manual deployment or need more control:

#### Step 1: Build the Application Locally

```bash
# Build the JAR file
./gradlew clean bootJar

# Verify the build
ls -lh build/libs/
```

#### Step 2: Test Locally with Docker Compose

```bash
# Create .env file
cp .env.example .env

# Edit with your values
nano .env

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f rental-app

# Access locally
open http://localhost:8080

# Stop services
docker-compose down
```

#### Step 3: Deploy to EC2 Manually

1. **Launch EC2 Instance:**
   ```bash
   # Instance specifications:
   - AMI: Ubuntu 22.04 LTS
   - Instance Type: t3.medium or larger
   - Storage: 30 GB gp3
   - Security Group: Allow ports 22, 8080, 5432
   ```

2. **Connect to EC2:**
   ```bash
   ssh -i your-key.pem ubuntu@<ec2-public-ip>
   ```

3. **Install Docker on EC2:**
   ```bash
   # Update system
   sudo apt-get update
   sudo apt-get upgrade -y

   # Install Docker
   curl -fsSL https://get.docker.com -o get-docker.sh
   sudo sh get-docker.sh
   sudo usermod -aG docker ubuntu

   # Install Docker Compose
   sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   sudo chmod +x /usr/local/bin/docker-compose

   # Verify installation
   docker --version
   docker-compose --version

   # Logout and login again for group changes
   exit
   ```

4. **Upload Application Files:**
   ```bash
   # From your local machine
   scp -i your-key.pem -r . ubuntu@<ec2-public-ip>:~/rental-app
   ```

5. **Start Application on EC2:**
   ```bash
   # SSH back into EC2
   ssh -i your-key.pem ubuntu@<ec2-public-ip>

   # Navigate to app directory
   cd ~/rental-app

   # Create .env file
   nano .env
   # Add your environment variables

   # Start services
   docker-compose up -d

   # Check status
   docker-compose ps
   docker-compose logs -f
   ```

---

## 🔧 Configuration Details

### Environment Variables

The application uses these environment variables (set in `.env` or EC2 environment):

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_PROFILES_ACTIVE` | Spring profile to use | `dev` | Yes |
| `DATABASE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://postgres:5432/rental` | Yes |
| `DATABASE_USERNAME` | Database username | `postgres` | Yes |
| `DATABASE_PASSWORD` | Database password | - | Yes |
| `RENTCAST_API_KEY` | RentCast API key | - | Yes |
| `STORAGE_DIR` | File storage directory | `/opt/rental/storage` | No |
| `PORT` | Application port | `8080` | No |

### Application Profiles

The application supports multiple profiles:

- **default** (local development): Uses `application.properties`
- **dev** (AWS EC2 dev): Uses `application-dev.properties`
- **prod** (production): Create `application-prod.properties` when needed

To activate a profile:
```bash
# Via environment variable
export SPRING_PROFILES_ACTIVE=dev

# Or in docker-compose.yml
environment:
  SPRING_PROFILES_ACTIVE: dev
```

---

## 🔍 Monitoring and Maintenance

### Health Checks

The application exposes health check endpoints:

```bash
# Check application health
curl http://<ec2-ip>:8080/actuator/health

# Check detailed health (requires authentication)
curl -u admin:admin http://<ec2-ip>:8080/actuator/health
```

### View Logs

```bash
# SSH into EC2
ssh -i your-key.pem ubuntu@<ec2-public-ip>

# View application logs
docker-compose logs -f rental-app

# View database logs
docker-compose logs -f postgres

# View last 100 lines
docker-compose logs --tail=100 rental-app
```

### Database Access

```bash
# Connect to PostgreSQL from EC2
docker-compose exec postgres psql -U postgres -d rental

# Or from your local machine (if port 5432 is open)
psql -h <ec2-public-ip> -U postgres -d rental
```

### Backup Database

```bash
# Create backup
docker-compose exec postgres pg_dump -U postgres rental > backup_$(date +%Y%m%d).sql

# Restore backup
docker-compose exec -T postgres psql -U postgres rental < backup_20240101.sql
```

---

## 🔄 Updating the Application

### Update via Jmix Studio

1. Make your code changes
2. Open Jmix Tool Window → Deployment
3. Select your existing deployment
4. Click "Redeploy"

### Manual Update

```bash
# On your local machine
./gradlew clean bootJar

# Upload to EC2
scp -i your-key.pem build/libs/*.jar ubuntu@<ec2-ip>:~/rental-app/

# On EC2
ssh -i your-key.pem ubuntu@<ec2-ip>
cd ~/rental-app
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

---

## 🛡️ Security Considerations

### For Development Environment:

1. **Security Group Rules:**
   - Port 22: Restrict to your IP only
   - Port 8080: Open to your team/testers
   - Port 5432: Close or restrict to specific IPs

2. **Change Default Credentials:**
   ```properties
   # In application-dev.properties
   ui.login.defaultUsername=your_admin_username
   ui.login.defaultPassword=your_secure_password
   ```

3. **Use Strong Database Password:**
   ```bash
   # In .env file
   DB_PASSWORD=use_a_strong_random_password_here
   ```

4. **Protect API Keys:**
   - Never commit `.env` to Git
   - Use AWS Secrets Manager for production

---

## 💰 Cost Estimation

### AWS EC2 Development Environment:

| Resource | Specification | Monthly Cost (approx.) |
|----------|--------------|----------------------|
| EC2 Instance | t3.medium (2 vCPU, 4GB) | $30 |
| EBS Storage | 30 GB gp3 | $3 |
| Data Transfer | 10 GB/month | $1 |
| **Total** | | **~$34/month** |

**Cost Optimization Tips:**
- Stop instance when not in use (pay only for storage)
- Use Reserved Instances for 24/7 dev environments (save 30-40%)
- Use t3.small for light testing ($15/month)

---

## 🐛 Troubleshooting

### Application Won't Start

```bash
# Check logs
docker-compose logs rental-app

# Common issues:
# 1. Database not ready - wait 30 seconds and check again
# 2. Port already in use - change PORT in .env
# 3. Out of memory - increase instance size
```

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Test connection
docker-compose exec postgres psql -U postgres -d rental -c "SELECT 1;"
```

### Can't Access Application

```bash
# Check if application is running
docker-compose ps

# Check security group allows port 8080
# Check EC2 instance public IP is correct
# Try accessing from EC2 itself:
curl http://localhost:8080/actuator/health
```

---

## 📚 Additional Resources

- [Jmix Quick Cloud Deployment Documentation](https://docs.jmix.io/jmix/studio/quick-cloud-deployment.html)
- [AWS EC2 Documentation](https://docs.aws.amazon.com/ec2/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)

---

## 🎯 Next Steps

After successful deployment:

1. ✅ Test all application features
2. ✅ Set up automated backups
3. ✅ Configure monitoring (CloudWatch)
4. ✅ Set up CI/CD pipeline (GitHub Actions)
5. ✅ Plan for production deployment (see separate guide)

---

## 📞 Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review application logs
3. Check Jmix documentation
4. Contact your team lead or DevOps engineer