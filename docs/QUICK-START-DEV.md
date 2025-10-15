# 🚀 Quick Start - Dev Deployment

## Option 1: Jmix Studio (Easiest) ⭐

### Prerequisites
- ✅ IntelliJ IDEA with Jmix Studio
- ✅ Docker Desktop running
- ✅ AWS account credentials

### Steps
1. **Prepare environment:**
   ```bash
   cp .env.example .env
   nano .env  # Add your DB_PASSWORD and RENTCAST_API_KEY
   ```

2. **Deploy via Jmix Studio:**
   - Open: `View → Tool Windows → Jmix`
   - Click: `Deployment → Quick Cloud Deployment`
   - Select: `docker-compose.yml`
   - Configure AWS:
     - Region: `us-east-1` (or closest)
     - Instance: `t3.medium`
     - Key Pair: Create or select
   - Click: `Deploy`

3. **Access application:**
   - URL: `http://<ec2-public-ip>:8080`
   - Login: `admin` / `admin`

---

## Option 2: Local Docker Testing

### Test locally before deploying:

```bash
# 1. Create environment file
cp .env.example .env

# 2. Edit with your values
nano .env

# 3. Start services
docker-compose up -d

# 4. Check logs
docker-compose logs -f rental-app

# 5. Access application
open http://localhost:8080

# 6. Stop when done
docker-compose down
```

---

## Option 3: Manual EC2 Deployment

### If you prefer manual control:

```bash
# 1. Launch EC2 instance (Ubuntu 22.04, t3.medium)
# 2. SSH into instance
ssh -i your-key.pem ubuntu@<ec2-ip>

# 3. Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker ubuntu

# 4. Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 5. Upload application (from local machine)
scp -i your-key.pem -r . ubuntu@<ec2-ip>:~/rental-app

# 6. Start application (on EC2)
cd ~/rental-app
nano .env  # Add your credentials
docker-compose up -d
```

---

## 🔍 Useful Commands

### Check Status
```bash
docker-compose ps
docker-compose logs -f rental-app
curl http://localhost:8080/actuator/health
```

### Restart Application
```bash
docker-compose restart rental-app
```

### Update Application
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Database Backup
```bash
docker-compose exec postgres pg_dump -U postgres rental > backup.sql
```

---

## 📋 Environment Variables

Required in `.env` file:

```bash
DB_PASSWORD=your_secure_password
RENTCAST_API_KEY=your_api_key
```

---

## 💰 Cost

**AWS EC2 Dev Environment:**
- t3.medium: ~$30/month
- Storage (30GB): ~$3/month
- **Total: ~$33/month**

💡 **Tip:** Stop instance when not in use to save costs!

---

## 🐛 Troubleshooting

### Application won't start?
```bash
docker-compose logs rental-app
# Wait 30 seconds for database to initialize
```

### Can't access from browser?
- Check EC2 Security Group allows port 8080
- Verify EC2 public IP is correct
- Try: `curl http://localhost:8080` from EC2

### Database issues?
```bash
docker-compose logs postgres
docker-compose restart postgres
```

---

## 📚 Full Documentation

See `DEPLOYMENT-DEV.md` for complete guide.

---

## ✅ Checklist

Before deploying:
- [ ] Created `.env` file with credentials
- [ ] Docker Desktop is running (for local test)
- [ ] AWS credentials configured (for EC2)
- [ ] Security Group allows ports 22, 8080
- [ ] Changed default admin password in production

After deploying:
- [ ] Application accessible at `http://<ec2-ip>:8080`
- [ ] Can login with admin credentials
- [ ] Health check returns OK: `/actuator/health`
- [ ] Database connection working
- [ ] RentCast API integration working