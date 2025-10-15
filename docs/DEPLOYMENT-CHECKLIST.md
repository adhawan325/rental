# ✅ Deployment Checklist

## Pre-Deployment Checklist

### 📋 Prerequisites

#### Local Environment
- [ ] IntelliJ IDEA with Jmix Studio installed
- [ ] Docker Desktop installed and running
- [ ] Docker plugin enabled in IntelliJ
- [ ] Java 17+ installed
- [ ] Gradle 8.5+ installed

#### AWS Account
- [ ] AWS account created
- [ ] AWS Access Key ID obtained
- [ ] AWS Secret Access Key obtained
- [ ] Billing alerts configured
- [ ] Budget set (recommended: $50-100/month for dev)

#### Application Configuration
- [ ] `.env` file created from `.env.example`
- [ ] Database password set in `.env`
- [ ] RentCast API key added to `.env`
- [ ] Default admin password changed (for production)
- [ ] Application builds successfully: `./gradlew build`

---

## Deployment Checklist (Jmix Studio)

### 🚀 Step-by-Step

#### Phase 1: Preparation (5 minutes)
- [ ] All code changes committed to Git
- [ ] Application tested locally
- [ ] `.env` file configured with correct values
- [ ] Docker Desktop is running
- [ ] AWS credentials ready

#### Phase 2: Jmix Studio Setup (3 minutes)
- [ ] Opened Jmix Tool Window (View → Tool Windows → Jmix)
- [ ] Navigated to Deployment section
- [ ] Clicked "Quick Cloud Deployment"
- [ ] Selected `docker-compose.yml` file

#### Phase 3: AWS Configuration (5 minutes)
- [ ] Selected AWS region (e.g., us-east-1)
- [ ] Chose instance type (t3.medium for dev)
- [ ] Created or selected SSH key pair
- [ ] Saved key pair file securely (.pem file)
- [ ] Reviewed security group settings:
  - [ ] Port 22 (SSH) - Restricted to my IP
  - [ ] Port 8080 (Application) - Open or restricted
  - [ ] Port 5432 (PostgreSQL) - Closed or restricted

#### Phase 4: Deployment (10-15 minutes)
- [ ] Clicked "Deploy" button
- [ ] Monitored deployment progress
- [ ] Noted EC2 instance public IP/DNS
- [ ] Waited for deployment to complete
- [ ] Verified no errors in deployment log

#### Phase 5: Verification (5 minutes)
- [ ] Application accessible at `http://<ec2-ip>:8080`
- [ ] Health check returns OK: `http://<ec2-ip>:8080/actuator/health`
- [ ] Can login with admin credentials
- [ ] Property search functionality works
- [ ] RentCast API integration working
- [ ] Database connection successful

---

## Post-Deployment Checklist

### 🔍 Immediate Verification (First 30 minutes)

#### Application Health
- [ ] Application loads without errors
- [ ] All pages accessible
- [ ] No JavaScript console errors
- [ ] Database queries working
- [ ] File uploads working (if applicable)

#### Security
- [ ] Changed default admin password
- [ ] Verified security group rules
- [ ] SSH access restricted to known IPs
- [ ] HTTPS configured (for production)
- [ ] Sensitive data not exposed in logs

#### Monitoring
- [ ] CloudWatch logs accessible
- [ ] Health check endpoint responding
- [ ] Application logs visible
- [ ] Database logs visible
- [ ] No critical errors in logs

### 📊 First Day Tasks

#### Documentation
- [ ] Documented EC2 instance details:
  - [ ] Instance ID
  - [ ] Public IP address
  - [ ] Region
  - [ ] Instance type
  - [ ] Key pair name
- [ ] Saved SSH key pair securely
- [ ] Documented database credentials
- [ ] Created runbook for common tasks

#### Backup & Recovery
- [ ] Tested database backup process
- [ ] Documented backup procedure
- [ ] Tested restore procedure
- [ ] Set up automated backups (if needed)

#### Team Access
- [ ] Shared application URL with team
- [ ] Provided login credentials (securely)
- [ ] Documented deployment process
- [ ] Trained team on basic operations

### 📈 First Week Tasks

#### Monitoring & Alerts
- [ ] Set up CloudWatch alarms:
  - [ ] CPU utilization > 80%
  - [ ] Memory utilization > 80%
  - [ ] Disk space < 20%
  - [ ] Application health check failures
- [ ] Configured email notifications
- [ ] Set up log aggregation (optional)

#### Performance
- [ ] Monitored application performance
- [ ] Checked database query performance
- [ ] Verified API response times
- [ ] Identified any bottlenecks
- [ ] Optimized if necessary

#### Cost Management
- [ ] Reviewed AWS billing dashboard
- [ ] Verified costs are within budget
- [ ] Set up cost alerts
- [ ] Identified cost optimization opportunities
- [ ] Documented actual costs vs. estimates

---

## Maintenance Checklist

### 🔄 Weekly Tasks

- [ ] Review application logs for errors
- [ ] Check disk space usage
- [ ] Verify backups are running
- [ ] Review security group rules
- [ ] Check for application updates
- [ ] Monitor costs

### 📅 Monthly Tasks

- [ ] Review and rotate logs
- [ ] Update dependencies (if needed)
- [ ] Review security patches
- [ ] Optimize database (vacuum, analyze)
- [ ] Review and optimize costs
- [ ] Test disaster recovery procedure

### 🎯 Quarterly Tasks

- [ ] Review architecture for improvements
- [ ] Evaluate scaling needs
- [ ] Update documentation
- [ ] Review security posture
- [ ] Plan for capacity growth
- [ ] Evaluate alternative deployment options

---

## Troubleshooting Checklist

### ❌ Application Won't Start

- [ ] Check Docker logs: `docker-compose logs rental-app`
- [ ] Verify database is running: `docker-compose ps postgres`
- [ ] Check environment variables in `.env`
- [ ] Verify port 8080 is not in use
- [ ] Check disk space: `df -h`
- [ ] Review application logs for errors
- [ ] Verify Java version: `java -version`

### ❌ Can't Access Application

- [ ] Verify EC2 instance is running
- [ ] Check security group allows port 8080
- [ ] Verify public IP address is correct
- [ ] Test from EC2 itself: `curl http://localhost:8080`
- [ ] Check if application is listening: `netstat -tlnp | grep 8080`
- [ ] Review firewall rules
- [ ] Check VPC and subnet configuration

### ❌ Database Connection Issues

- [ ] Verify PostgreSQL is running: `docker-compose ps postgres`
- [ ] Check database credentials in `.env`
- [ ] Test database connection: `docker-compose exec postgres psql -U postgres -d rental`
- [ ] Review database logs: `docker-compose logs postgres`
- [ ] Check database disk space
- [ ] Verify connection string format

### ❌ Performance Issues

- [ ] Check CPU usage: `top` or CloudWatch
- [ ] Check memory usage: `free -m` or CloudWatch
- [ ] Check disk I/O: `iostat` or CloudWatch
- [ ] Review slow query logs
- [ ] Check for memory leaks
- [ ] Review application logs for errors
- [ ] Consider scaling up instance size

---

## Rollback Checklist

### 🔙 If Deployment Fails

#### Immediate Actions
- [ ] Document the error/issue
- [ ] Take screenshots of error messages
- [ ] Save logs for analysis
- [ ] Notify team of issue

#### Rollback Steps
- [ ] Stop current deployment
- [ ] Restore from previous backup (if applicable)
- [ ] Revert to previous version:
  ```bash
  docker-compose down
  git checkout <previous-commit>
  docker-compose up -d
  ```
- [ ] Verify rollback successful
- [ ] Test application functionality
- [ ] Notify team of rollback

#### Post-Rollback
- [ ] Analyze root cause
- [ ] Document lessons learned
- [ ] Fix issues in development
- [ ] Test thoroughly before redeploying
- [ ] Plan next deployment attempt

---

## Upgrade Checklist

### 🆙 When Upgrading Application

#### Pre-Upgrade
- [ ] Backup database
- [ ] Backup application files
- [ ] Test upgrade in local environment
- [ ] Review changelog for breaking changes
- [ ] Plan rollback strategy
- [ ] Schedule maintenance window
- [ ] Notify users of downtime

#### During Upgrade
- [ ] Put application in maintenance mode (if applicable)
- [ ] Stop application: `docker-compose down`
- [ ] Pull latest code: `git pull`
- [ ] Build new version: `./gradlew build`
- [ ] Update Docker images: `docker-compose build`
- [ ] Start application: `docker-compose up -d`
- [ ] Monitor startup logs

#### Post-Upgrade
- [ ] Verify application starts successfully
- [ ] Test critical functionality
- [ ] Check for errors in logs
- [ ] Verify database migrations completed
- [ ] Test API endpoints
- [ ] Notify users upgrade is complete
- [ ] Monitor for issues

---

## Security Audit Checklist

### 🔒 Monthly Security Review

#### Access Control
- [ ] Review SSH key access
- [ ] Audit user accounts
- [ ] Review security group rules
- [ ] Check for unused access keys
- [ ] Verify MFA is enabled (AWS account)

#### Application Security
- [ ] Review application logs for suspicious activity
- [ ] Check for failed login attempts
- [ ] Verify HTTPS is enforced (production)
- [ ] Review API key usage
- [ ] Check for exposed secrets in logs

#### Infrastructure Security
- [ ] Review EC2 security groups
- [ ] Check for open ports
- [ ] Verify database is not publicly accessible
- [ ] Review IAM policies
- [ ] Check for security patches

#### Compliance
- [ ] Review data retention policies
- [ ] Verify backup encryption
- [ ] Check audit logs
- [ ] Review access logs
- [ ] Document security measures

---

## Cost Optimization Checklist

### 💰 Monthly Cost Review

#### Compute
- [ ] Review instance utilization
- [ ] Consider Reserved Instances for 24/7 workloads
- [ ] Stop instances when not in use (dev/staging)
- [ ] Right-size instances based on usage
- [ ] Consider Spot Instances for non-critical workloads

#### Storage
- [ ] Review EBS volume usage
- [ ] Delete unused snapshots
- [ ] Consider S3 for file storage
- [ ] Implement lifecycle policies
- [ ] Clean up old logs

#### Data Transfer
- [ ] Review data transfer costs
- [ ] Optimize API calls
- [ ] Implement caching
- [ ] Use CloudFront CDN (if applicable)
- [ ] Compress responses

#### Database
- [ ] Review database instance size
- [ ] Optimize queries
- [ ] Implement connection pooling
- [ ] Consider read replicas for read-heavy workloads
- [ ] Clean up old data

---

## 📝 Notes Section

### Deployment Details
```
Date: _______________
Deployed By: _______________
Instance ID: _______________
Public IP: _______________
Region: _______________
Instance Type: _______________
Key Pair: _______________
```

### Issues Encountered
```
Issue 1: _______________________________________________
Resolution: _______________________________________________

Issue 2: _______________________________________________
Resolution: _______________________________________________
```

### Lessons Learned
```
1. _______________________________________________
2. _______________________________________________
3. _______________________________________________
```

---

## 🎉 Completion

Once all items are checked:
- [ ] Deployment is complete and verified
- [ ] Documentation is updated
- [ ] Team is notified
- [ ] Monitoring is in place
- [ ] Backup strategy is implemented
- [ ] Ready for production use (if applicable)

**Congratulations! Your deployment is complete! 🚀**