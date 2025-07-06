#!/bin/bash

# Production Deployment Script - Food Ordering System
# This script automates the complete production deployment process

echo "==========================================="
echo "🚀 Food Ordering System - Production Deployment"
echo "==========================================="

# Configuration
PROJECT_NAME="food-ordering-system"
PROJECT_VERSION="1.0.0"
DEPLOY_PATH="/opt/food-ordering"
BACKUP_PATH="/opt/backups/food-ordering"
LOG_PATH="/var/log/food-ordering"
SERVICE_NAME="food-ordering"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_error "This script must be run as root"
        exit 1
    fi
}

# Create necessary directories
create_directories() {
    log_info "Creating necessary directories..."
    
    mkdir -p $DEPLOY_PATH
    mkdir -p $BACKUP_PATH
    mkdir -p $LOG_PATH
    mkdir -p /etc/systemd/system
    mkdir -p /etc/nginx/sites-available
    mkdir -p /etc/nginx/sites-enabled
    
    log_info "Directories created successfully"
}

# Install Java 17
install_java() {
    log_info "Installing Java 17..."
    
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        if [[ $JAVA_VERSION =~ ^17\. ]]; then
            log_info "Java 17 is already installed"
            return
        fi
    fi
    
    apt update
    apt install -y openjdk-17-jdk
    
    # Set JAVA_HOME
    echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64" >> /etc/environment
    source /etc/environment
    
    log_info "Java 17 installed successfully"
}

# Install PostgreSQL
install_postgresql() {
    log_info "Installing PostgreSQL..."
    
    if command -v psql &> /dev/null; then
        log_info "PostgreSQL is already installed"
        return
    fi
    
    apt update
    apt install -y postgresql postgresql-contrib
    
    # Start and enable PostgreSQL
    systemctl start postgresql
    systemctl enable postgresql
    
    log_info "PostgreSQL installed successfully"
}

# Install Redis
install_redis() {
    log_info "Installing Redis..."
    
    if command -v redis-server &> /dev/null; then
        log_info "Redis is already installed"
        return
    fi
    
    apt update
    apt install -y redis-server
    
    # Configure Redis
    sed -i 's/^# maxmemory <bytes>/maxmemory 256mb/' /etc/redis/redis.conf
    sed -i 's/^# maxmemory-policy noeviction/maxmemory-policy allkeys-lru/' /etc/redis/redis.conf
    
    # Start and enable Redis
    systemctl start redis
    systemctl enable redis
    
    log_info "Redis installed successfully"
}

# Install Nginx
install_nginx() {
    log_info "Installing Nginx..."
    
    if command -v nginx &> /dev/null; then
        log_info "Nginx is already installed"
        return
    fi
    
    apt update
    apt install -y nginx
    
    # Start and enable Nginx
    systemctl start nginx
    systemctl enable nginx
    
    log_info "Nginx installed successfully"
}

# Setup Database
setup_database() {
    log_info "Setting up database..."
    
    # Create database user and database
    sudo -u postgres psql -c "CREATE USER food_ordering_user WITH PASSWORD 'secure_password_2024';"
    sudo -u postgres psql -c "CREATE DATABASE food_ordering_db OWNER food_ordering_user;"
    sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE food_ordering_db TO food_ordering_user;"
    
    log_info "Database setup completed"
}

# Deploy Application
deploy_application() {
    log_info "Deploying application..."
    
    # Stop existing service if running
    if systemctl is-active --quiet $SERVICE_NAME; then
        log_info "Stopping existing service..."
        systemctl stop $SERVICE_NAME
    fi
    
    # Create backup of current deployment
    if [ -d "$DEPLOY_PATH/current" ]; then
        log_info "Creating backup of current deployment..."
        mv $DEPLOY_PATH/current $BACKUP_PATH/backup-$(date +%Y%m%d-%H%M%S)
    fi
    
    # Deploy new version
    mkdir -p $DEPLOY_PATH/current
    cp -r backend/* $DEPLOY_PATH/current/
    
    # Build application
    cd $DEPLOY_PATH/current
    ./mvnw clean package -DskipTests
    
    # Set permissions
    chown -R $SERVICE_NAME:$SERVICE_NAME $DEPLOY_PATH
    chmod +x $DEPLOY_PATH/current/target/$PROJECT_NAME-$PROJECT_VERSION.jar
    
    log_info "Application deployed successfully"
}

# Create systemd service
create_systemd_service() {
    log_info "Creating systemd service..."
    
    cat > /etc/systemd/system/$SERVICE_NAME.service << EOF
[Unit]
Description=Food Ordering System
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=$SERVICE_NAME
Group=$SERVICE_NAME
WorkingDirectory=$DEPLOY_PATH/current
ExecStart=/usr/bin/java -jar target/$PROJECT_NAME-$PROJECT_VERSION.jar
Restart=always
RestartSec=10

# Environment variables
Environment=JAVA_OPTS="-Xmx2g -Xms1g"
Environment=SPRING_PROFILES_ACTIVE=production

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=$SERVICE_NAME

[Install]
WantedBy=multi-user.target
EOF
    
    # Create service user
    if ! id "$SERVICE_NAME" &>/dev/null; then
        useradd -r -s /bin/false $SERVICE_NAME
    fi
    
    # Reload systemd and start service
    systemctl daemon-reload
    systemctl enable $SERVICE_NAME
    systemctl start $SERVICE_NAME
    
    log_info "Systemd service created and started"
}

# Configure Nginx
configure_nginx() {
    log_info "Configuring Nginx..."
    
    # Copy production configuration
    cp ../config/nginx/nginx-production.conf /etc/nginx/sites-available/food-ordering
    
    # Enable site
    ln -sf /etc/nginx/sites-available/food-ordering /etc/nginx/sites-enabled/
    
    # Remove default site
    rm -f /etc/nginx/sites-enabled/default
    
    # Test configuration
    nginx -t
    
    if [ $? -eq 0 ]; then
        systemctl reload nginx
        log_info "Nginx configured successfully"
    else
        log_error "Nginx configuration test failed"
        exit 1
    fi
}

# Setup SSL with Let's Encrypt
setup_ssl() {
    log_info "Setting up SSL with Let's Encrypt..."
    
    # Install certbot
    apt install -y certbot python3-certbot-nginx
    
    # Note: This requires domain configuration
    log_warn "SSL setup requires domain configuration. Please run:"
    log_warn "certbot --nginx -d your-domain.com"
}

# Install monitoring
install_monitoring() {
    log_info "Installing monitoring tools..."
    
    # Install Prometheus
    wget https://github.com/prometheus/prometheus/releases/latest/download/prometheus-*-linux-amd64.tar.gz
    tar -xzf prometheus-*-linux-amd64.tar.gz
    mv prometheus-*/prometheus /usr/local/bin/
    mv prometheus-*/promtool /usr/local/bin/
    
    # Copy monitoring configuration
    cp ../config/monitoring/prometheus.yml /etc/prometheus/
    
    # Create Prometheus service
    cat > /etc/systemd/system/prometheus.service << EOF
[Unit]
Description=Prometheus
After=network.target

[Service]
Type=simple
User=prometheus
Group=prometheus
ExecStart=/usr/local/bin/prometheus --config.file=/etc/prometheus/prometheus.yml --storage.tsdb.path=/var/lib/prometheus
Restart=always

[Install]
WantedBy=multi-user.target
EOF
    
    # Create prometheus user
    useradd -r -s /bin/false prometheus
    mkdir -p /var/lib/prometheus
    chown prometheus:prometheus /var/lib/prometheus
    
    systemctl daemon-reload
    systemctl enable prometheus
    systemctl start prometheus
    
    log_info "Monitoring tools installed"
}

# Verify deployment
verify_deployment() {
    log_info "Verifying deployment..."
    
    # Check service status
    if systemctl is-active --quiet $SERVICE_NAME; then
        log_info "✅ Service is running"
    else
        log_error "❌ Service is not running"
        systemctl status $SERVICE_NAME
        exit 1
    fi
    
    # Check health endpoint
    sleep 30  # Wait for service to start
    
    if curl -f http://localhost:8080/api/health > /dev/null 2>&1; then
        log_info "✅ Health check passed"
    else
        log_error "❌ Health check failed"
        exit 1
    fi
    
    # Check database connection
    if sudo -u postgres psql -d food_ordering_db -c "SELECT 1;" > /dev/null 2>&1; then
        log_info "✅ Database connection working"
    else
        log_error "❌ Database connection failed"
        exit 1
    fi
    
    # Check Redis connection
    if redis-cli ping > /dev/null 2>&1; then
        log_info "✅ Redis connection working"
    else
        log_error "❌ Redis connection failed"
        exit 1
    fi
    
    log_info "✅ All checks passed! Deployment successful"
}

# Main deployment function
main() {
    echo "Starting production deployment..."
    
    check_root
    create_directories
    install_java
    install_postgresql
    install_redis
    install_nginx
    setup_database
    deploy_application
    create_systemd_service
    configure_nginx
    setup_ssl
    install_monitoring
    verify_deployment
    
    echo ""
    echo "==========================================="
    echo "🎉 Deployment completed successfully!"
    echo "==========================================="
    echo "Application URL: http://localhost"
    echo "Admin Panel: http://localhost/admin"
    echo "API Documentation: http://localhost/api/docs"
    echo "Monitoring: http://localhost:9090"
    echo ""
    echo "Service management commands:"
    echo "  Start:   systemctl start $SERVICE_NAME"
    echo "  Stop:    systemctl stop $SERVICE_NAME"
    echo "  Restart: systemctl restart $SERVICE_NAME"
    echo "  Status:  systemctl status $SERVICE_NAME"
    echo "  Logs:    journalctl -u $SERVICE_NAME -f"
    echo ""
}

# Run main function
main "$@" 