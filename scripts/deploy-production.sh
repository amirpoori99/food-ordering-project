#!/bin/bash
# Food Ordering System - Production Deployment Script
# Version: 1.0

set -e  # Exit on any error

echo "🚀 Starting Food Ordering System Production Deployment..."

# ==================== ENVIRONMENT SETUP ====================
export APP_ENVIRONMENT="production"
export SERVER_PORT="${SERVER_PORT:-8081}"
export SERVER_HOST="${SERVER_HOST:-0.0.0.0}"

# Security Settings (MUST be set in production)
if [ -z "$JWT_SECRET" ]; then
    echo "❌ ERROR: JWT_SECRET must be set in production!"
    echo "   Example: export JWT_SECRET='your-256-bit-secret-key'"
    exit 1
fi

# Database Settings
export DATABASE_URL="${DATABASE_URL:-jdbc:sqlite:food_ordering.db}"
export DATABASE_DRIVER="${DATABASE_DRIVER:-org.sqlite.JDBC}"

# SSL Settings (recommended for production)
export SSL_ENABLED="${SSL_ENABLED:-false}"
if [ "$SSL_ENABLED" = "true" ]; then
    if [ -z "$SSL_KEYSTORE_PATH" ] || [ -z "$SSL_KEYSTORE_PASSWORD" ]; then
        echo "❌ ERROR: SSL enabled but keystore settings not provided!"
        exit 1
    fi
fi

# Logging Settings
export LOG_LEVEL_ROOT="${LOG_LEVEL_ROOT:-INFO}"
export LOG_FILE_PATH="${LOG_FILE_PATH:-./logs}"

# ==================== PRE-DEPLOYMENT CHECKS ====================
echo "🔍 Running pre-deployment checks..."

# Check Java version
if ! command -v java &> /dev/null; then
    echo "❌ ERROR: Java not found. Please install Java 17+"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ ERROR: Java 17+ required. Found Java $JAVA_VERSION"
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ ERROR: Maven not found. Please install Maven 3.6+"
    exit 1
fi

# Check disk space (require at least 1GB)
AVAILABLE_SPACE=$(df . | tail -1 | awk '{print $4}')
if [ "$AVAILABLE_SPACE" -lt 1048576 ]; then
    echo "⚠️  WARNING: Low disk space. At least 1GB recommended."
fi

echo "✅ Pre-deployment checks passed"

# ==================== BACKUP EXISTING DATA ====================
if [ -f "food_ordering.db" ]; then
    echo "💾 Creating database backup..."
    BACKUP_NAME="food_ordering_backup_$(date +%Y%m%d_%H%M%S).db"
    cp food_ordering.db "$BACKUP_NAME"
    echo "✅ Database backed up to $BACKUP_NAME"
fi

# ==================== BUILD APPLICATION ====================
echo "🔨 Building backend application..."
cd backend
mvn clean package -DskipTests -q

if [ $? -ne 0 ]; then
    echo "❌ ERROR: Backend build failed!"
    exit 1
fi

echo "✅ Backend build successful"

echo "🔨 Building frontend application..."
cd ../frontend-javafx
mvn clean package -DskipTests -q

if [ $? -ne 0 ]; then
    echo "❌ ERROR: Frontend build failed!"
    exit 1
fi

echo "✅ Frontend build successful"
cd ..

# ==================== PREPARE DEPLOYMENT DIRECTORY ====================
DEPLOY_DIR="./deployment"
echo "📁 Preparing deployment directory: $DEPLOY_DIR"

mkdir -p "$DEPLOY_DIR"
mkdir -p "$DEPLOY_DIR/logs"
mkdir -p "$DEPLOY_DIR/config"
mkdir -p "$DEPLOY_DIR/lib"

# Copy backend JAR and dependencies
cp backend/target/food-ordering-backend-*.jar "$DEPLOY_DIR/"
cp backend/target/lib/*.jar "$DEPLOY_DIR/lib/" 2>/dev/null || true

# Copy frontend JAR
cp frontend-javafx/target/food-ordering-frontend-*.jar "$DEPLOY_DIR/"

# Copy configuration files
cp backend/src/main/resources/application.properties "$DEPLOY_DIR/config/"
cp backend/src/main/resources/logback.xml "$DEPLOY_DIR/config/"

# ==================== CREATE STARTUP SCRIPTS ====================
echo "📜 Creating startup scripts..."

# Backend startup script
cat > "$DEPLOY_DIR/start-backend.sh" << 'EOF'
#!/bin/bash
# Food Ordering Backend Startup Script

BACKEND_JAR=$(ls food-ordering-backend-*.jar 2>/dev/null | head -1)
if [ -z "$BACKEND_JAR" ]; then
    echo "❌ Backend JAR not found!"
    exit 1
fi

CLASSPATH="$BACKEND_JAR:lib/*"

echo "🚀 Starting Food Ordering Backend..."
echo "📍 JAR: $BACKEND_JAR"
echo "📍 Port: ${SERVER_PORT:-8081}"
echo "📍 Environment: ${APP_ENVIRONMENT:-production}"

java -cp "$CLASSPATH" \
     -Dfile.encoding=UTF-8 \
     -Djava.awt.headless=true \
     -Xms512m -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -Dlogback.configurationFile=config/logback.xml \
     com.myapp.ServerApp
EOF

# Frontend startup script  
cat > "$DEPLOY_DIR/start-frontend.sh" << 'EOF'
#!/bin/bash
# Food Ordering Frontend Startup Script

FRONTEND_JAR=$(ls food-ordering-frontend-*.jar 2>/dev/null | head -1)
if [ -z "$FRONTEND_JAR" ]; then
    echo "❌ Frontend JAR not found!"
    exit 1
fi

echo "🚀 Starting Food Ordering Frontend..."
echo "📍 JAR: $FRONTEND_JAR"

# Check if display is available
if [ -z "$DISPLAY" ]; then
    echo "⚠️  WARNING: No DISPLAY set. GUI may not work."
fi

java -jar "$FRONTEND_JAR" \
     -Dfile.encoding=UTF-8 \
     -Xms256m -Xmx1g
EOF

# Make scripts executable
chmod +x "$DEPLOY_DIR/start-backend.sh"
chmod +x "$DEPLOY_DIR/start-frontend.sh"

# ==================== CREATE SYSTEMD SERVICE (optional) ====================
if command -v systemctl &> /dev/null; then
    echo "🔧 Creating systemd service..."
    
    cat > "$DEPLOY_DIR/food-ordering-backend.service" << EOF
[Unit]
Description=Food Ordering System Backend
After=network.target

[Service]
Type=simple
User=food-ordering
WorkingDirectory=/opt/food-ordering
ExecStart=/opt/food-ordering/start-backend.sh
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

# Environment variables
Environment=APP_ENVIRONMENT=production
Environment=SERVER_PORT=${SERVER_PORT:-8081}
Environment=LOG_LEVEL_ROOT=${LOG_LEVEL_ROOT:-INFO}

[Install]
WantedBy=multi-user.target
EOF

    echo "📋 To install systemd service:"
    echo "   sudo cp $DEPLOY_DIR/food-ordering-backend.service /etc/systemd/system/"
    echo "   sudo systemctl enable food-ordering-backend"
    echo "   sudo systemctl start food-ordering-backend"
fi

# ==================== DEPLOYMENT SUMMARY ====================
echo ""
echo "🎉 Deployment completed successfully!"
echo ""
echo "📂 Deployment Directory: $DEPLOY_DIR"
echo "🔧 Configuration: $DEPLOY_DIR/config/"
echo "📋 Logs will be written to: $DEPLOY_DIR/logs/"
echo ""
echo "🚀 To start the application:"
echo "   Backend:  cd $DEPLOY_DIR && ./start-backend.sh"
echo "   Frontend: cd $DEPLOY_DIR && ./start-frontend.sh"
echo ""
echo "🌐 Backend will be available at: http://${SERVER_HOST:-localhost}:${SERVER_PORT:-8081}"
echo "🔍 Health check: http://${SERVER_HOST:-localhost}:${SERVER_PORT:-8081}/api/health"
echo ""

# ==================== SECURITY REMINDERS ====================
echo "🔒 SECURITY REMINDERS FOR PRODUCTION:"
echo "   ✓ Set strong JWT_SECRET environment variable"
echo "   ✓ Enable SSL (set SSL_ENABLED=true)"
echo "   ✓ Use PostgreSQL for production database"
echo "   ✓ Configure firewall to allow only necessary ports"
echo "   ✓ Set up monitoring and log rotation"
echo "   ✓ Regularly backup database"
echo ""
echo "📚 For detailed instructions, see DEPLOYMENT_GUIDE.md"
echo ""
echo "✅ Deployment script completed successfully!" 