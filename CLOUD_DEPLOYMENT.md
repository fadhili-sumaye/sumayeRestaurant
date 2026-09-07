# Cloud Deployment Guide
## Sumaye Restaurant Management System
### DigitalOcean Ubuntu Server — Step-by-Step

---

> [!NOTE]
> This guide assumes you are deploying the system to a fresh **Ubuntu 22.04 LTS** server on DigitalOcean (Droplet). You will need a domain name (e.g., `api.yourdomain.com`) pointing to your server's IP address.

---

## Architecture Overview

```
Android App (Java)
       |
       | HTTPS + WSS
       ↓
  Nginx (Port 443, SSL)
       |
       ↓
 Spring Boot (Port 8080, localhost only)
       |
       ↓
 MySQL 8.0 (Port 3306, localhost only)
```

**Only ports 22 (SSH), 80 (HTTP→redirect), and 443 (HTTPS) are publicly accessible.**  
MySQL and Spring Boot are bound to `127.0.0.1` only.

---

## PART 1: CREATE THE CLOUD SERVER

### Step 1.1 — Create a DigitalOcean Droplet

1. Log in to [digitalocean.com](https://www.digitalocean.com).
2. Click **Create → Droplets**.
3. Choose:
   - **Region**: Choose the closest to Tanzania (e.g., Amsterdam or Frankfurt)
   - **Image**: Ubuntu 22.04 LTS
   - **Size**: Basic — 2 GB RAM / 1 vCPU / 50 GB SSD (sufficient for a restaurant system)
   - **Authentication**: SSH Key (recommended) or Password
4. Click **Create Droplet**.
5. Note your server's **public IP address** (e.g., `1.2.3.4`).

### Step 1.2 — Connect via SSH

On your Windows PC, open **PowerShell** or **Git Bash**:

```bash
# Replace 1.2.3.4 with your actual server IP
ssh root@1.2.3.4
```

You are now logged into your cloud server.

---

## PART 2: CONFIGURE THE SERVER

### Step 2.1 — Update the Server

```bash
# Update all installed packages to the latest versions
apt update && apt upgrade -y
```

### Step 2.2 — Configure Firewall (UFW)

```bash
# Allow SSH connections (so you don't lock yourself out)
ufw allow OpenSSH

# Allow HTTP (for Let's Encrypt certificate verification and redirect)
ufw allow 80/tcp

# Allow HTTPS (main API access from Android app)
ufw allow 443/tcp

# Enable the firewall
ufw enable

# Verify the rules
ufw status
```

Expected output:
```
Status: active
To                         Action      From
--                         ------      ----
OpenSSH                    ALLOW       Anywhere
80/tcp                     ALLOW       Anywhere
443/tcp                    ALLOW       Anywhere
```

> [!IMPORTANT]
> Do NOT expose port 8080 (Spring Boot) or 3306 (MySQL) to the internet. They must only be accessible on localhost (`127.0.0.1`).

---

## PART 3: INSTALL JAVA

Spring Boot requires Java 17.

### Step 3.1 — Install Java 17

```bash
# Install Java 17 (Eclipse Temurin, same as used in Dockerfile)
apt install -y wget apt-transport-https
mkdir -p /etc/apt/keyrings
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc
echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list
apt update
apt install -y temurin-17-jdk
```

### Step 3.2 — Verify Java Installation

```bash
java -version
```

Expected output:
```
openjdk version "17.x.x" ...
```

---

## PART 4: INSTALL MYSQL

### Step 4.1 — Install MySQL 8.0

```bash
# Install MySQL Server
apt install -y mysql-server

# Start MySQL and enable it to start on boot
systemctl start mysql
systemctl enable mysql
```

### Step 4.2 — Secure MySQL

```bash
# Run the security configuration wizard
mysql_secure_installation
```

Answer the prompts:
- **Validate password component**: No (or Yes with a strong root password)
- **Remove anonymous users**: Yes
- **Disallow root login remotely**: Yes
- **Remove test database**: Yes
- **Reload privilege tables**: Yes

### Step 4.3 — Create the Production Database and User

```bash
# Log into MySQL as root
mysql -u root -p
```

Inside MySQL, run these commands:

```sql
-- Create the database
CREATE DATABASE IF NOT EXISTS restaurant_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Create a dedicated database user (never use root for the app)
CREATE USER 'restaurant_user'@'localhost' IDENTIFIED BY 'YourStrongPasswordHere123!';

-- Grant only necessary permissions on the restaurant database
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP
  ON restaurant_management.*
  TO 'restaurant_user'@'localhost';

-- Apply the permissions
FLUSH PRIVILEGES;

-- Verify
SHOW GRANTS FOR 'restaurant_user'@'localhost';

-- Exit MySQL
EXIT;
```

> [!CAUTION]
> Choose a strong password for `restaurant_user`. Do not use simple passwords in production.

### Step 4.4 — Import the Database Schema

Run the Phase 1–6 SQL schema files to create all tables:

```bash
# Upload schema files from your Windows PC (run this on your Windows PC, not the server)
scp C:\Users\fadhi\Downloads\SUMAYERESTAURANT\database_schema_phase1.sql root@1.2.3.4:/tmp/
scp C:\Users\fadhi\Downloads\SUMAYERESTAURANT\database_schema_phase6.sql root@1.2.3.4:/tmp/

# On the SERVER, import the schemas
mysql -u restaurant_user -p restaurant_management < /tmp/database_schema_phase1.sql
mysql -u restaurant_user -p restaurant_management < /tmp/database_schema_phase6.sql
```

---

## PART 5: DEPLOY THE SPRING BOOT APPLICATION

### Step 5.1 — Build the JAR on Your Windows PC

On your Windows PC, open **PowerShell** in the `restaurant-management-api` folder:

```bash
cd C:\Users\fadhi\Downloads\SUMAYERESTAURANT\restaurant-management-api

# Build the production JAR (skipping tests for speed)
mvn clean package -DskipTests -P prod
```

This creates: `target/restaurant-management-api-0.0.1-SNAPSHOT.jar`

### Step 5.2 — Upload the JAR to the Server

```bash
# On your Windows PC, upload the JAR to the server
scp target\restaurant-management-api-0.0.1-SNAPSHOT.jar root@1.2.3.4:/opt/restaurant-api/restaurant-management-api.jar
```

### Step 5.3 — Create a Non-Root User for the Application

It is a security best practice not to run the app as root.

```bash
# On the SERVER:
# Create a system group and user called 'spring'
groupadd --system spring
useradd --system --gid spring --no-create-home spring

# Create the application directory
mkdir -p /opt/restaurant-api
chown spring:spring /opt/restaurant-api
```

---

## PART 6: CONFIGURE ENVIRONMENT VARIABLES

### Step 6.1 — Create the Protected Environment File

This file holds all secrets. It is ONLY readable by root (systemd reads it on behalf of the service).

```bash
# Create the environment file
nano /etc/restaurant-api.env
```

Paste and fill in your actual values:

```
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_HOST=localhost
DB_PORT=3306
DB_NAME=restaurant_management
DB_USERNAME=restaurant_user
DB_PASSWORD=YourStrongPasswordHere123!
JWT_SECRET=YOUR_64_CHAR_RANDOM_SECRET_HERE
JWT_EXPIRATION=86400000
APP_CORS_ALLOWED_ORIGINS=https://api.yourdomain.com
WS_ALLOWED_ORIGINS=https://api.yourdomain.com,*
```

Save and close: Press `Ctrl+X`, then `Y`, then `Enter`.

### Step 6.2 — Protect the File

```bash
# Only root can read the secrets file
chown root:root /etc/restaurant-api.env
chmod 600 /etc/restaurant-api.env
```

### Step 6.3 — Generate a Secure JWT Secret

```bash
# Generate a cryptographically secure 64-character random secret
openssl rand -base64 48
```

Copy the output and paste it as your `JWT_SECRET` in `/etc/restaurant-api.env`.

---

## PART 7: CONFIGURE SYSTEMD SERVICE

### Step 7.1 — Install the Service File

```bash
# Upload the service file from your Windows PC
scp C:\Users\fadhi\Downloads\SUMAYERESTAURANT\systemd\restaurant-api.service root@1.2.3.4:/etc/systemd/system/restaurant-api.service
```

Or create it manually:

```bash
nano /etc/systemd/system/restaurant-api.service
```

Paste the contents from [systemd/restaurant-api.service](file:///c:/Users/fadhi/Downloads/SUMAYERESTAURANT/systemd/restaurant-api.service).

### Step 7.2 — Enable and Start the Service

```bash
# Tell systemd about the new service
systemctl daemon-reload

# Enable auto-start on boot
systemctl enable restaurant-api

# Start the service now
systemctl start restaurant-api

# Check if it started successfully
systemctl status restaurant-api
```

Expected output shows `Active: active (running)`.

### Step 7.3 — View Live Logs

```bash
# Follow live logs (like watching the console)
journalctl -u restaurant-api -f

# View the last 100 lines of logs
journalctl -u restaurant-api -n 100
```

---

## PART 8: INSTALL AND CONFIGURE NGINX

### Step 8.1 — Install Nginx

```bash
apt install -y nginx
systemctl enable nginx
systemctl start nginx
```

### Step 8.2 — Upload the Nginx Configuration

```bash
# On your Windows PC, upload the Nginx config
scp C:\Users\fadhi\Downloads\SUMAYERESTAURANT\nginx\restaurant-api.conf root@1.2.3.4:/etc/nginx/sites-available/restaurant-api
```

### Step 8.3 — Enable the Site

```bash
# On the server, create a symlink to enable the site
ln -s /etc/nginx/sites-available/restaurant-api /etc/nginx/sites-enabled/restaurant-api

# Remove the default Nginx site (optional)
rm -f /etc/nginx/sites-enabled/default

# Test the configuration for syntax errors
nginx -t
```

Expected output: `nginx: configuration file ... is ok`

```bash
# Reload Nginx
systemctl reload nginx
```

---

## PART 9: CONFIGURE DOMAIN NAME

### Step 9.1 — Point Your Domain to the Server

In your domain registrar's DNS settings:

1. Add an **A Record**:
   - **Name**: `api` (or `@` if using root domain)
   - **Value**: `1.2.3.4` (your server IP)
   - **TTL**: 3600

2. Wait 5–30 minutes for DNS to propagate.

3. Test: `ping api.yourdomain.com` — should resolve to your server IP.

---

## PART 10: CONFIGURE HTTPS (SSL)

### Step 10.1 — Install Certbot (Let's Encrypt)

```bash
# Install Certbot and the Nginx plugin
apt install -y certbot python3-certbot-nginx
```

### Step 10.2 — Get a Free SSL Certificate

```bash
# Replace with your actual domain
certbot --nginx -d api.yourdomain.com
```

Follow the interactive prompts:
- Enter your email address (for renewal notifications)
- Agree to the Terms of Service
- Choose to redirect HTTP to HTTPS (recommended)

Certbot automatically:
- Obtains a free SSL certificate from Let's Encrypt
- Updates your Nginx configuration with SSL settings
- Sets up auto-renewal

### Step 10.3 — Verify Auto-Renewal

```bash
# Test that auto-renewal works
certbot renew --dry-run
```

Expected: `Congratulations, all simulated renewals succeeded`

---

## PART 11: CONFIGURE WEBSOCKET PROXYING

The Nginx configuration in `nginx/restaurant-api.conf` already includes WebSocket upgrade headers:

```nginx
location /ws {
    proxy_pass http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_read_timeout 3600s;
}
```

This ensures:
- `wss://api.yourdomain.com/ws` → Spring Boot STOMP endpoint
- Persistent connections maintained for real-time kitchen and waiter notifications

---

## PART 12: TEST THE DEPLOYMENT

### Step 12.1 — Test the Health Endpoint

```bash
# From your Windows PC
curl https://api.yourdomain.com/api/health
```

Expected response:
```json
{"status": "UP", "service": "restaurant-management-api", "timestamp": "..."}
```

### Step 12.2 — Test Login

```bash
curl -X POST https://api.yourdomain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Expected: JSON response with `token` field.

### Step 12.3 — Test Unauthorized Access

```bash
curl https://api.yourdomain.com/api/branches
```

Expected: `401 Unauthorized`

### Step 12.4 — Test with JWT Token

```bash
# Replace TOKEN with the JWT from the login response
curl -H "Authorization: Bearer TOKEN" https://api.yourdomain.com/api/branches
```

---

## PART 13: CONNECT ANDROID APPLICATION

### Step 13.1 — Update Android Constants

In [Constants.java](file:///c:/Users/fadhi/Downloads/SUMAYERESTAURANT/app/src/main/java/com/example/sumayerestaurant/util/Constants.java):

```java
// 1. Edit your production URL:
private static final String PRODUCTION_URL = "https://api.yourdomain.com/";

// 2. Switch the environment:
public static final Env ENV = Env.PRODUCTION;
```

### Step 13.2 — Rebuild and Install the App

1. In Android Studio, change `ENV` to `Env.PRODUCTION`.
2. Run the app on your test device.
3. Verify login works over HTTPS.

---

## PART 14: MYSQL BACKUP AND RESTORE

### Step 14.1 — Create a Backup

```bash
# Create a timestamped backup of the production database
mysqldump -u restaurant_user -p \
  --single-transaction \
  --routines \
  --triggers \
  restaurant_management > /opt/backups/restaurant_$(date +%Y%m%d_%H%M%S).sql

# Compress the backup
gzip /opt/backups/restaurant_*.sql
```

Create the backup directory first:
```bash
mkdir -p /opt/backups
```

### Step 14.2 — Restore a Backup

```bash
# Decompress if compressed
gunzip restaurant_20260902_120000.sql.gz

# Restore
mysql -u restaurant_user -p restaurant_management < restaurant_20260902_120000.sql
```

### Step 14.3 — Automated Daily Backup (Optional)

```bash
# Create a backup script
cat > /opt/scripts/backup-restaurant.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u restaurant_user -pYOUR_DB_PASSWORD \
  --single-transaction restaurant_management \
  | gzip > "$BACKUP_DIR/restaurant_$DATE.sql.gz"
# Keep only the last 14 days of backups
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +14 -delete
EOF

chmod +x /opt/scripts/backup-restaurant.sh

# Schedule it daily at 2 AM
(crontab -l 2>/dev/null; echo "0 2 * * * /opt/scripts/backup-restaurant.sh") | crontab -
```

---

## PART 15: SERVER MANAGEMENT

### Restart the Backend

```bash
systemctl restart restaurant-api
```

### Stop the Backend

```bash
systemctl stop restaurant-api
```

### View the Status

```bash
systemctl status restaurant-api
```

### View Live Logs

```bash
journalctl -u restaurant-api -f
```

### Restart Nginx

```bash
systemctl reload nginx
```

### Update the Application (Deploy New Version)

```bash
# 1. Build a new JAR on your Windows PC:
mvn clean package -DskipTests

# 2. Upload the new JAR to the server:
scp target\restaurant-management-api-0.0.1-SNAPSHOT.jar root@1.2.3.4:/opt/restaurant-api/restaurant-management-api-new.jar

# 3. On the server, stop the service, replace, and restart:
systemctl stop restaurant-api
mv /opt/restaurant-api/restaurant-management-api-new.jar /opt/restaurant-api/restaurant-management-api.jar
chown spring:spring /opt/restaurant-api/restaurant-management-api.jar
systemctl start restaurant-api

# 4. Verify:
systemctl status restaurant-api
journalctl -u restaurant-api -n 50
```

---

## PART 16: DOCKER DEPLOYMENT (ALTERNATIVE)

If you prefer to use Docker instead of the native setup:

### Step 16.1 — Install Docker on the Server

```bash
apt install -y docker.io docker-compose
systemctl enable docker
systemctl start docker
```

### Step 16.2 — Upload Files and Run

```bash
# On your Windows PC, copy the project to the server
scp -r C:\Users\fadhi\Downloads\SUMAYERESTAURANT\docker-compose.yml root@1.2.3.4:/opt/restaurant/
scp -r C:\Users\fadhi\Downloads\SUMAYERESTAURANT\restaurant-management-api root@1.2.3.4:/opt/restaurant/
scp -r C:\Users\fadhi\Downloads\SUMAYERESTAURANT\database_schema_phase1.sql root@1.2.3.4:/opt/restaurant/

# On the server, create the .env file
cp /opt/restaurant/restaurant-management-api/.env.example /opt/restaurant/.env
nano /opt/restaurant/.env   # fill in real values

# Start the services
cd /opt/restaurant
docker-compose up -d

# View logs
docker-compose logs -f api
```

---

## Environment Variables Reference

| Variable | Description | Example |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Spring profile to use | `prod` |
| `SERVER_PORT` | Backend HTTP port | `8080` |
| `DB_HOST` | MySQL hostname | `localhost` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | `restaurant_management` |
| `DB_USERNAME` | Database username | `restaurant_user` |
| `DB_PASSWORD` | Database password | *(strong password)* |
| `JWT_SECRET` | JWT signing secret (64+ chars) | *(openssl rand -base64 48)* |
| `JWT_EXPIRATION` | Token expiry in milliseconds | `86400000` (24h) |
| `APP_CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `https://api.yourdomain.com` |
| `WS_ALLOWED_ORIGINS` | Allowed WebSocket origins | `https://api.yourdomain.com,*` |

---

## Troubleshooting

### Backend won't start
```bash
journalctl -u restaurant-api -n 100 --no-pager
```
Look for database connection errors or missing environment variables.

### Cannot connect from Android
- Verify the domain resolves: `nslookup api.yourdomain.com`
- Verify HTTPS is working: `curl https://api.yourdomain.com/api/health`
- Check Nginx logs: `tail -f /var/log/nginx/error.log`

### WebSocket not connecting
- Verify the `/ws` location block is in Nginx
- Check that `proxy_set_header Upgrade $http_upgrade;` is present
- Test: `curl -i -N -H "Upgrade: websocket" https://api.yourdomain.com/ws`

### Database connection failed
```bash
mysql -u restaurant_user -p -h localhost restaurant_management
# If this fails, check the password in /etc/restaurant-api.env
```

### SSL Certificate expired
```bash
certbot renew
systemctl reload nginx
```
