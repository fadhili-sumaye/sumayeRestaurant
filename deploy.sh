#!/usr/bin/env bash
# ==============================================================================
# SUMAYE RESTAURANT MANAGEMENT SYSTEM - DIGITALOCEAN AUTOMATED DEPLOYMENT SCRIPT
# Target OS: Ubuntu 22.04 LTS / 24.04 LTS
# Usage:
#   chmod +x deploy.sh
#   ./deploy.sh
# ==============================================================================

set -e

# Color helpers
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}======================================================================${NC}"
echo -e "${BLUE}  SUMAYE RESTAURANT MANAGEMENT SYSTEM - CLOUD DEPLOYMENT SETUP       ${NC}"
echo -e "${BLUE}======================================================================${NC}"

# Check root / sudo
if [ "$EUID" -ne 0 ]; then
  echo -e "${RED}[ERROR] Please run this script with sudo: sudo ./deploy.sh${NC}"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 1. Check .env file
echo -e "\n${YELLOW}[Step 1/6] Checking environment configuration...${NC}"
if [ ! -f .env ]; then
  if [ -f .env.example ]; then
    echo -e "${YELLOW}  No .env file found. Creating from .env.example...${NC}"
    cp .env.example .env
    
    # Auto-generate secure JWT Secret
    GENERATED_JWT=$(openssl rand -base64 48 | tr -dc 'a-zA-Z0-9' | head -c 64)
    GENERATED_DB_PASS=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 20)
    GENERATED_ROOT_PASS=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 24)
    
    sed -i "s|YOUR_SECURE_RANDOMLY_GENERATED_JWT_SECRET_KEY_AT_LEAST_64_CHARACTERS_LONG|${GENERATED_JWT}|g" .env
    sed -i "s|ChangeThisToAStrongRandomPassword123!|${GENERATED_DB_PASS}|g" .env
    sed -i "s|ChangeThisToAStrongRootPassword456!|${GENERATED_ROOT_PASS}|g" .env
    
    echo -e "${GREEN}  Generated secure production credentials in .env!${NC}"
    echo -e "${YELLOW}  IMPORTANT: Review .env to set your actual domain if needed.${NC}"
  else
    echo -e "${RED}[ERROR] Neither .env nor .env.example found in $SCRIPT_DIR!${NC}"
    exit 1
  fi
else
  echo -e "${GREEN}  .env file detected.${NC}"
fi

# 2. Install Docker, Docker Compose, Nginx, Certbot if missing
echo -e "\n${YELLOW}[Step 2/6] Verifying system packages (Docker, Nginx, Certbot, UFW)...${NC}"
apt-get update -qq

PACKAGES_TO_INSTALL=""

if ! command -v docker &> /dev/null; then
  PACKAGES_TO_INSTALL="$PACKAGES_TO_INSTALL docker.io"
fi

if ! docker compose version &> /dev/null; then
  PACKAGES_TO_INSTALL="$PACKAGES_TO_INSTALL docker-compose-v2"
fi

if ! command -v nginx &> /dev/null; then
  PACKAGES_TO_INSTALL="$PACKAGES_TO_INSTALL nginx"
fi

if ! command -v certbot &> /dev/null; then
  PACKAGES_TO_INSTALL="$PACKAGES_TO_INSTALL certbot python3-certbot-nginx"
fi

if [ -n "$PACKAGES_TO_INSTALL" ]; then
  echo -e "  Installing missing packages:${PACKAGES_TO_INSTALL}..."
  apt-get install -y -qq $PACKAGES_TO_INSTALL
fi

systemctl enable docker --now
systemctl enable nginx --now
echo -e "${GREEN}  Docker & Nginx services active.${NC}"

# 3. Configure Firewall (UFW)
echo -e "\n${YELLOW}[Step 3/6] Hardening Firewall (UFW)...${NC}"
ufw allow OpenSSH > /dev/null 2>&1 || ufw allow 22/tcp > /dev/null 2>&1
ufw allow 80/tcp > /dev/null 2>&1
ufw allow 443/tcp > /dev/null 2>&1

# Explicitly ensure ports 3306 & 8080 are NOT accessible externally
ufw delete allow 3306/tcp > /dev/null 2>&1 || true
ufw delete allow 8080/tcp > /dev/null 2>&1 || true

ufw --force enable > /dev/null 2>&1
echo -e "${GREEN}  Firewall configured: Only Ports 22, 80, and 443 open.${NC}"

# 4. Configure Host Nginx
echo -e "\n${YELLOW}[Step 4/6] Setting up Nginx Reverse Proxy...${NC}"
if [ -f nginx/restaurant-api.conf ]; then
  cp nginx/restaurant-api.conf /etc/nginx/sites-available/restaurant-api
  ln -sf /etc/nginx/sites-available/restaurant-api /etc/nginx/sites-enabled/restaurant-api
  # Remove default site if present
  rm -f /etc/nginx/sites-enabled/default
  nginx -t && systemctl reload nginx
  echo -e "${GREEN}  Nginx reverse proxy configured successfully.${NC}"
else
  echo -e "${YELLOW}  nginx/restaurant-api.conf not found. Skipping host nginx copy.${NC}"
fi

# 5. Build and Launch Docker Containers
echo -e "\n${YELLOW}[Step 5/6] Building and starting Docker containers...${NC}"
docker compose down || true
docker compose build
docker compose up -d

# 6. Verify Health
echo -e "\n${YELLOW}[Step 6/6] Verifying Backend Health...${NC}"
echo -e "  Waiting for Spring Boot & MySQL to complete startup..."
HEALTHY=false
for i in {1..30}; do
  if curl -sf http://127.0.0.1:8080/api/health > /dev/null 2>&1; then
    HEALTHY=true
    break
  fi
  sleep 3
  echo -n "."
done
echo ""

if [ "$HEALTHY" = true ]; then
  echo -e "${GREEN}======================================================================${NC}"
  echo -e "${GREEN}  DEPLOYMENT SUCCESSFUL! SPRING BOOT & MYSQL ARE HEALTHY!            ${NC}"
  echo -e "${GREEN}======================================================================${NC}"
  docker compose ps
  echo -e "\nBackend API test:"
  curl -s http://127.0.0.1:8080/api/health | python3 -m json.tool || curl -s http://127.0.0.1:8080/api/health
  echo -e "\n${BLUE}Next Step for SSL:${NC}"
  echo -e "Run: ${YELLOW}sudo certbot --nginx -d yourdomain.com${NC} to secure HTTPS."
else
  echo -e "${RED}[WARNING] Backend health check did not respond in 90 seconds.${NC}"
  echo -e "Checking container logs:"
  docker compose logs --tail=40 api
fi
