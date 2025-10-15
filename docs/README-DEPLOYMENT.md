# 🚀 Rental Application - Deployment Documentation

## 📚 Documentation Index

This directory contains comprehensive deployment documentation for the Rental application.

---

## 📖 Available Guides

### 1. **[QUICK-START-DEV.md](./QUICK-START-DEV.md)** ⚡
**Quick reference for deploying to development environment**

- 5-minute setup guide
- Three deployment options
- Essential commands
- Troubleshooting tips

**Start here if:** You want to deploy quickly without reading details.

---

### 2. **[DEPLOYMENT-DEV.md](./DEPLOYMENT-DEV.md)** 📘
**Complete guide for development/staging deployment**

- Detailed step-by-step instructions
- Jmix Studio Quick Cloud Deployment
- Manual Docker deployment
- Configuration details
- Monitoring and maintenance
- Security considerations
- Cost estimation

**Start here if:** You want comprehensive deployment instructions.

---

### 3. **[DEPLOYMENT-COMPARISON.md](./DEPLOYMENT-COMPARISON.md)** 📊
**Comparison of all deployment options**

- Feature comparison matrix
- Recommendations by environment
- Cost breakdown
- Decision tree
- Migration path
- Scaling comparison

**Start here if:** You want to understand which deployment option to choose.

---

## 🎯 Quick Decision Guide

### I want to deploy for...

#### **Development/Testing** → Use Jmix Quick Deploy
- ⚡ Fastest setup (5-10 minutes)
- 💰 Cheapest (~$30/month)
- 📖 Guide: [QUICK-START-DEV.md](./QUICK-START-DEV.md)

#### **Staging/Pre-Production** → Use AWS Elastic Beanstalk
- 🔄 Auto-scaling enabled
- 🛡️ Production-like environment
- 📖 Guide: Coming soon (or see AWS docs)

#### **Production (Small-Medium)** → Use AWS Elastic Beanstalk
- 🚀 Proven reliability
- 💰 Cost-effective
- 📖 Guide: Coming soon (or see AWS docs)

#### **Production (Large Scale)** → Use AWS ECS or Kubernetes
- 📈 Maximum scalability
- 🎛️ Fine-grained control
- 📖 Guide: Coming soon (or see AWS docs)

---

## 📁 Configuration Files

### Created for You:

| File | Purpose | Required |
|------|---------|----------|
| `application-dev.properties` | Dev environment config | ✅ Yes |
| `Dockerfile` | Container image definition | ✅ Yes |
| `docker-compose.yml` | Multi-container orchestration | ✅ Yes |
| `.dockerignore` | Files to exclude from image | ✅ Yes |
| `.env.example` | Environment variables template | ✅ Yes |
| `.env` | Your actual environment variables | ⚠️ Create from .env.example |

### How to Use:

```bash
# 1. Create your environment file
cp .env.example .env

# 2. Edit with your credentials
nano .env

# 3. Deploy using your chosen method
# See QUICK-START-DEV.md for options
```

---

## 🔧 What Was Set Up

### 1. **Spring Profile Configuration**
- Created `application-dev.properties` for dev environment
- Configured to use environment variables
- Added health check endpoints
- Optimized for AWS deployment

### 2. **Docker Configuration**
- Multi-stage Dockerfile for optimized builds
- Docker Compose with PostgreSQL and application
- Health checks configured
- Volume management for data persistence

### 3. **AWS Integration**
- Ready for Jmix Studio Quick Cloud Deployment
- Environment variable configuration
- Security group recommendations
- Cost optimization tips

### 4. **Monitoring & Health Checks**
- Spring Boot Actuator enabled
- Health check endpoints configured
- Docker health checks implemented
- Logging configuration optimized

---

## 🚀 Getting Started (3 Steps)

### Step 1: Prepare Environment
```bash
# Create environment file
cp .env.example .env

# Edit with your values
nano .env
```

### Step 2: Choose Deployment Method

**Option A: Jmix Studio (Recommended)**
```
1. Open IntelliJ IDEA
2. View → Tool Windows → Jmix
3. Deployment → Quick Cloud Deployment
4. Follow wizard
```

**Option B: Local Docker Test**
```bash
docker-compose up -d
open http://localhost:8080
```

**Option C: Manual EC2**
```bash
# See DEPLOYMENT-DEV.md for full instructions
```

### Step 3: Verify Deployment
```bash
# Check health
curl http://<your-ip>:8080/actuator/health

# Access application
open http://<your-ip>:8080

# Login with: admin / admin
```

---

## 📊 Architecture Overview

### Development Environment (Current Setup)

```
┌─────────────────────────────────────┐
│         AWS EC2 Instance            │
│  ┌───────────────────────────────┐  │
│  │     Docker Compose            │  │
│  │  ┌─────────────────────────┐  │  │
│  │  │  Rental Application     │  │  │
│  │  │  (Spring Boot + Vaadin) │  │  │
│  │  │  Port: 8080             │  │  │
│  │  └──────────┬──────────────┘  │  │
│  │             │                  │  │
│  │  ┌──────────▼──────────────┐  │  │
│  │  │  PostgreSQL Database    │  │  │
│  │  │  Port: 5432             │  │  │
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
         │
         ▼
   Internet Access
   http://<ec2-ip>:8080
```

### Future Production Architecture

```
┌──────────────┐
│   Internet   │
└──────┬───────┘
       │
┌──────▼────────────┐
│  Load Balancer    │
└──────┬────────────┘
       │
   ┌───┴────┐
   │        │
┌──▼──┐  ┌─▼───┐
│ App │  │ App │  (Auto-scaled)
└──┬──┘  └─┬───┘
   │        │
   └────┬───┘
        │
   ┌────▼─────┐
   │   RDS    │
   │PostgreSQL│
   └──────────┘
```

---

## 🔐 Security Checklist

### Before Deploying:

- [ ] Created `.env` file (not committed to Git)
- [ ] Changed default admin password
- [ ] Set strong database password
- [ ] Protected RentCast API key
- [ ] Reviewed security group rules

### After Deploying:

- [ ] Verified HTTPS is enabled (production)
- [ ] Restricted SSH access to your IP
- [ ] Enabled CloudWatch monitoring
- [ ] Set up automated backups
- [ ] Configured log retention

---

## 💰 Cost Summary

### Development Environment
- **EC2 Instance (t3.medium):** $30/month
- **Storage (30 GB):** $3/month
- **Data Transfer:** $1/month
- **Total:** ~$34/month

💡 **Tip:** Stop instance when not in use to save ~$30/month!

### Production Environment (Estimated)
- **Small (< 10K users):** $200-300/month
- **Medium (10K-100K users):** $500-800/month
- **Large (> 100K users):** $1000+/month

See [DEPLOYMENT-COMPARISON.md](./DEPLOYMENT-COMPARISON.md) for detailed breakdown.

---

## 🐛 Common Issues

### "Application won't start"
```bash
# Check logs
docker-compose logs rental-app

# Usually: Wait 30 seconds for database initialization
```

### "Can't access from browser"
```bash
# Check security group allows port 8080
# Verify EC2 public IP
# Test from EC2: curl http://localhost:8080
```

### "Database connection failed"
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check credentials in .env file
```

See [DEPLOYMENT-DEV.md](./DEPLOYMENT-DEV.md) for more troubleshooting.

---

## 📈 Next Steps

### Immediate (Now):
1. ✅ Deploy to dev environment using Jmix Studio
2. ✅ Test all application features
3. ✅ Verify RentCast API integration works

### Short-term (1-2 weeks):
1. Set up automated backups
2. Configure monitoring alerts
3. Document any custom configurations
4. Train team on deployment process

### Medium-term (1-3 months):
1. Plan staging environment setup
2. Implement CI/CD pipeline
3. Set up automated testing
4. Prepare for production deployment

### Long-term (3-6 months):
1. Migrate to Elastic Beanstalk for production
2. Implement auto-scaling
3. Set up multi-region deployment (if needed)
4. Optimize costs based on usage patterns

---

## 📞 Support & Resources

### Documentation
- [Jmix Documentation](https://docs.jmix.io/)
- [AWS Documentation](https://docs.aws.amazon.com/)
- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)

### Jmix Resources
- [Jmix Quick Cloud Deployment](https://docs.jmix.io/jmix/studio/quick-cloud-deployment.html)
- [Jmix Forum](https://forum.jmix.io/)
- [Jmix Slack](https://join.slack.com/t/jmixplatform/shared_invite/zt-2juulpoaf-7yp7F~m_fhFDmZ6GXHr83A)

### AWS Resources
- [AWS Free Tier](https://aws.amazon.com/free/)
- [AWS Cost Calculator](https://calculator.aws/)
- [AWS Support](https://aws.amazon.com/support/)

---

## 🎉 You're Ready!

You now have everything you need to deploy your Rental application to AWS EC2 for development.

**Recommended first step:**
1. Read [QUICK-START-DEV.md](./QUICK-START-DEV.md)
2. Create your `.env` file
3. Deploy using Jmix Studio

**Questions?** Check the troubleshooting sections in the guides or review [DEPLOYMENT-COMPARISON.md](./DEPLOYMENT-COMPARISON.md) to ensure you're using the right deployment method.

---

## 📝 Change Log

### 2024-01-XX - Initial Setup
- Created development deployment configuration
- Added Docker and Docker Compose files
- Configured Spring profiles for dev environment
- Added comprehensive documentation
- Set up health checks and monitoring

---

**Happy Deploying! 🚀**