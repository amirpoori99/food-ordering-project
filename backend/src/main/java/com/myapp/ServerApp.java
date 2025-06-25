package com.myapp;

import com.myapp.auth.AuthRepository;
import com.myapp.auth.AuthService;
import com.myapp.auth.AuthResult;
import com.myapp.auth.AuthMiddleware;
import com.myapp.restaurant.RestaurantRepository;
import com.myapp.restaurant.RestaurantController;
import com.myapp.admin.AdminRepository;
import com.myapp.admin.AdminService;
import com.myapp.admin.AdminController;
import com.myapp.order.OrderRepository;
import com.myapp.order.OrderController;
import com.myapp.payment.PaymentRepository;
import com.myapp.payment.PaymentController;
import com.myapp.payment.WalletController;
import com.myapp.payment.WalletService;
import com.myapp.payment.TransactionController;
import com.myapp.courier.DeliveryRepository;
import com.myapp.courier.DeliveryController;
import com.myapp.item.ItemController;
import com.myapp.menu.MenuController;
import com.myapp.vendor.VendorController;
import com.myapp.favorites.FavoritesRepository;
import com.myapp.favorites.FavoritesService;
import com.myapp.favorites.FavoritesController;
import com.myapp.notification.NotificationRepository;
import com.myapp.notification.NotificationService;
import com.myapp.notification.NotificationController;
import com.myapp.common.utils.DatabaseUtil;
import com.myapp.common.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServerApp {
    
    private static AuthService authService;
    private static AdminController adminController;
    private static RestaurantController restaurantController;
    private static OrderController orderController;
    private static PaymentController paymentController;
    private static WalletController walletController;
    private static TransactionController transactionController;
    private static DeliveryController deliveryController;
    private static ItemController itemController;
    private static MenuController menuController;
    private static VendorController vendorController;
    private static FavoritesController favoritesController;
    private static NotificationController notificationController;
    
    public static void main(String[] args) throws IOException {
        System.out.println("🚀 Starting Food Ordering Backend Server...");
        
        // Initialize services
        AuthRepository authRepo = new AuthRepository();
        authService = new AuthService(authRepo);
        
        RestaurantRepository restaurantRepo = new RestaurantRepository();
        OrderRepository orderRepo = new OrderRepository();
        PaymentRepository paymentRepo = new PaymentRepository();
        DeliveryRepository deliveryRepo = new DeliveryRepository();
        FavoritesRepository favoritesRepo = new FavoritesRepository();
        NotificationRepository notificationRepo = new NotificationRepository();
        
        // Initialize Admin services
        AdminRepository adminRepo = new AdminRepository();
        AdminService adminService = new AdminService(adminRepo, authRepo, restaurantRepo, orderRepo, paymentRepo, deliveryRepo);
        adminController = new AdminController(adminService);
        
        // Initialize Favorites service
        FavoritesService favoritesService = new FavoritesService(favoritesRepo, authRepo, restaurantRepo);
        
        // Initialize Notification service
        NotificationService notificationService = new NotificationService(notificationRepo, authRepo);
        
        // Initialize Wallet service for Transaction controller
        WalletService walletService = new WalletService(paymentRepo, authRepo);
        
        // Initialize other controllers
        restaurantController = new RestaurantController();
        orderController = new OrderController();
        paymentController = new PaymentController();
        walletController = new WalletController();
        transactionController = new TransactionController(walletService, paymentRepo);
        deliveryController = new DeliveryController();
        itemController = new ItemController();
        menuController = new MenuController();
        vendorController = new VendorController();
        favoritesController = new FavoritesController(favoritesService);
        notificationController = new NotificationController(notificationService);
        
        // Test database connection
        System.out.println("Testing Hibernate connection...");
        try {
            DatabaseUtil.getSessionFactory();
            System.out.println("✅ Database connection successful!");
        } catch (Exception e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        
        // Add contexts (endpoints)
        server.createContext("/api/test", new TestHandler());
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/refresh", new RefreshTokenHandler());
        server.createContext("/api/auth/validate", new ValidateTokenHandler());
        server.createContext("/api/auth/logout", new LogoutHandler());
        server.createContext("/health", new HealthHandler());
        
        // Controller endpoints
        server.createContext("/api/admin/", adminController);
        server.createContext("/api/restaurants/", restaurantController);
        server.createContext("/api/orders/", orderController);
        server.createContext("/api/payments/", paymentController);
        server.createContext("/api/wallet/", walletController);
        server.createContext("/api/transactions/", transactionController);
        server.createContext("/api/deliveries/", deliveryController);
        server.createContext("/api/items/", itemController);
        server.createContext("/api/menu/", menuController);
        server.createContext("/api/vendors/", vendorController);
        server.createContext("/api/favorites/", favoritesController);
        server.createContext("/api/notifications/", notificationController);
        server.createContext("/api/notification/", notificationController);
        
        // Configure thread pool
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Start server
        server.start();
        System.out.println("🚀 Server started on http://localhost:8081");
        System.out.println("📋 Available endpoints:");
        System.out.println("   GET  /health - Health check");
        System.out.println("   GET  /api/test - Simple test");
        System.out.println("   POST /api/auth/register - User registration");
        System.out.println("   POST /api/auth/login - User login (with JWT tokens)");
        System.out.println("   POST /api/auth/refresh - Refresh access token");
        System.out.println("   GET  /api/auth/validate - Validate access token");
        System.out.println("   POST /api/auth/logout - User logout");
        System.out.println("   🔧 Admin Dashboard (18+ endpoints):");
        System.out.println("   GET  /api/admin/dashboard - Admin statistics");
        System.out.println("   GET  /api/admin/users - User management");
        System.out.println("   GET  /api/admin/restaurants - Restaurant management");
        System.out.println("   GET  /api/admin/orders - Order management");
        System.out.println("   GET  /api/admin/transactions - Transaction monitoring");
        System.out.println("   GET  /api/admin/deliveries - Delivery tracking");
        System.out.println("   🏪 Restaurant Management (16+ endpoints):");
        System.out.println("   GET  /api/restaurants/ - All restaurants");
        System.out.println("   POST /api/restaurants/ - Create restaurant");
        System.out.println("   🛒 Order Management (20+ endpoints):");
        System.out.println("   GET  /api/orders/ - All orders");
        System.out.println("   POST /api/orders/ - Create order");
        System.out.println("   💳 Payment System (8+ endpoints):");
        System.out.println("   GET  /api/payments/ - Payment history");
        System.out.println("   POST /api/payments/ - Process payment");
        System.out.println("   💰 Wallet System (6+ endpoints):");
        System.out.println("   GET  /api/wallet/ - Wallet balance");
        System.out.println("   POST /api/wallet/ - Add funds");
        System.out.println("   💸 Transaction System (5+ endpoints):");
        System.out.println("   GET  /api/transactions/wallet/history - Transaction history");
        System.out.println("   GET  /api/transactions/wallet/statistics - Wallet statistics");
        System.out.println("   🚚 Delivery System (16+ endpoints):");
        System.out.println("   GET  /api/deliveries/ - All deliveries");
        System.out.println("   POST /api/deliveries/ - Create delivery");
        System.out.println("   🍔 Item Management (13+ endpoints):");
        System.out.println("   GET  /api/items/ - All items");
        System.out.println("   POST /api/items/ - Create item");
        System.out.println("   📋 Menu Management (6+ endpoints):");
        System.out.println("   GET  /api/menu/ - Menu items");
        System.out.println("   POST /api/menu/ - Add menu item");
        System.out.println("   🏬 Vendor System (10+ endpoints):");
        System.out.println("   GET  /api/vendors/ - All vendors");
        System.out.println("   GET  /api/vendors/search - Search vendors");
        System.out.println("   ⭐ Favorites System (6+ endpoints):");
        System.out.println("   GET  /api/favorites/ - User favorites");
        System.out.println("   POST /api/favorites/add - Add to favorites");
        System.out.println("   DELETE /api/favorites/remove - Remove favorite");
        System.out.println("   🔔 Notification System (6+ endpoints):");
        System.out.println("   GET  /api/notifications/{userId} - User notifications");
        System.out.println("   POST /api/notifications/ - Create notification");
        System.out.println("   PUT  /api/notification/{id}/read - Mark as read");
        System.out.println("   DELETE /api/notification/{id} - Delete notification");
        
        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🛑 Shutting down server...");
            server.stop(2);
            DatabaseUtil.shutdown();
            System.out.println("✅ Server stopped gracefully");
        }));
    }
    
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"UP\",\"service\":\"food-ordering-backend\"}";
            sendResponse(exchange, 200, response);
        }
    }
    
    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"message\":\"Hello from Food Ordering Backend!\",\"timestamp\":\"" + 
                            java.time.Instant.now() + "\"}";
            sendResponse(exchange, 200, response);
        }
    }
    
    static class RegisterHandler implements HttpHandler {
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // Read request body
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    System.out.println("📥 Registration request received");
                    
                    // Parse JSON
                    JsonNode json = objectMapper.readTree(requestBody);
                    
                    // Validate required fields
                    if (!json.has("fullName") || !json.has("phone") || !json.has("password")) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing required fields: fullName, phone, password\"}");
                        return;
                    }
                    
                    // Extract fields
                    String fullName = json.get("fullName").asText();
                    String phone = json.get("phone").asText();
                    String email = json.has("email") ? json.get("email").asText() : "";
                    String password = json.get("password").asText();
                    String address = json.has("address") ? json.get("address").asText() : "";
                    
                    // Validate data
                    if (fullName.trim().isEmpty() || phone.trim().isEmpty() || password.trim().isEmpty()) {
                        sendResponse(exchange, 400, "{\"error\":\"fullName, phone, and password cannot be empty\"}");
                        return;
                    }
                    
                    // Simple password hash (in production, use proper hashing)
                    String passwordHash = "hashed_" + password;
                    
                    // Create user entity
                    User user = User.forRegistration(fullName, phone, email, passwordHash, address);
                    
                    // Save to database
                    User savedUser = authService.registerUser(user);
                    
                    // Create response
                    String response = "{\"message\":\"User registered successfully!\",\"status\":\"success\",\"userId\":" + 
                                    savedUser.getId() + ",\"fullName\":\"" + savedUser.getFullName() + "\",\"phone\":\"" + savedUser.getPhone() + "\"}";
                    sendResponse(exchange, 201, response);
                    
                    System.out.println("✅ User registered: " + savedUser.getFullName() + " (ID: " + savedUser.getId() + ")");
                    
                } catch (Exception e) {
                    System.err.println("❌ Registration error: " + e.getMessage());
                    e.printStackTrace();
                    String errorResponse = "{\"error\":\"Registration failed\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                    sendResponse(exchange, 500, errorResponse);
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    
    static class LoginHandler implements HttpHandler {
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // Read request body
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    System.out.println("📥 Login request received");
                    
                    // Parse JSON
                    JsonNode json = objectMapper.readTree(requestBody);
                    
                    // Validate required fields
                    if (!json.has("phone") || !json.has("password")) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing required fields: phone, password\"}");
                        return;
                    }
                    
                    // Extract fields
                    String phone = json.get("phone").asText();
                    String password = json.get("password").asText();
                    
                    // Validate data
                    if (phone.trim().isEmpty() || password.trim().isEmpty()) {
                        sendResponse(exchange, 400, "{\"error\":\"Phone and password cannot be empty\"}");
                        return;
                    }
                    
                    // Simple password hash (should match registration logic)
                    String passwordHash = "hashed_" + password;
                    
                    // Authenticate user with JWT tokens
                    AuthResult authResult = authService.loginWithTokens(phone, passwordHash);
                    
                    if (!authResult.isAuthenticated()) {
                        sendResponse(exchange, 401, "{\"error\":\"" + authResult.getErrorMessage() + "\"}");
                        return;
                    }
                    
                    // Create response with JWT tokens
                    String response = String.format(
                        "{\"message\":\"Login successful!\",\"status\":\"success\",\"userId\":%d," +
                        "\"fullName\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\"," +
                        "\"accessToken\":\"%s\",\"refreshToken\":\"%s\"," +
                        "\"tokenType\":\"Bearer\",\"expiresIn\":%d}",
                        authResult.getUserId(),
                        authResult.getPhone().replace("\"", "\\\""),
                        authResult.getPhone(),
                        authResult.getRole(),
                        authResult.getAccessToken(),
                        authResult.getRefreshToken(),
                        com.myapp.common.utils.JWTUtil.getAccessTokenValidity() / 1000 // in seconds
                    );
                    sendResponse(exchange, 200, response);
                    
                    System.out.println("✅ User logged in with JWT tokens: " + authResult.getPhone() + " (ID: " + authResult.getUserId() + ")");
                    
                } catch (com.myapp.common.exceptions.InvalidCredentialsException e) {
                    sendResponse(exchange, 401, "{\"error\":\"Invalid phone or password\"}");
                } catch (Exception e) {
                    System.err.println("❌ Login error: " + e.getMessage());
                    e.printStackTrace();
                    String errorResponse = "{\"error\":\"Login failed\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                    sendResponse(exchange, 500, errorResponse);
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    
    static class RefreshTokenHandler implements HttpHandler {
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    JsonNode json = objectMapper.readTree(requestBody);
                    
                    if (!json.has("refreshToken")) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing refreshToken field\"}");
                        return;
                    }
                    
                    String refreshToken = json.get("refreshToken").asText();
                    AuthResult authResult = authService.refreshToken(refreshToken);
                    
                    if (!authResult.isAuthenticated()) {
                        sendResponse(exchange, 401, "{\"error\":\"" + authResult.getErrorMessage() + "\"}");
                        return;
                    }
                    
                    String response = String.format(
                        "{\"message\":\"Token refreshed successfully\",\"status\":\"success\"," +
                        "\"accessToken\":\"%s\",\"refreshToken\":\"%s\"," +
                        "\"tokenType\":\"Bearer\",\"expiresIn\":%d}",
                        authResult.getAccessToken(),
                        authResult.getRefreshToken(),
                        com.myapp.common.utils.JWTUtil.getAccessTokenValidity() / 1000
                    );
                    sendResponse(exchange, 200, response);
                    
                } catch (Exception e) {
                    System.err.println("❌ Token refresh error: " + e.getMessage());
                    String errorResponse = "{\"error\":\"Token refresh failed\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                    sendResponse(exchange, 500, errorResponse);
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    
    static class ValidateTokenHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    AuthResult authResult = AuthMiddleware.authenticate(exchange);
                    
                    if (!authResult.isAuthenticated()) {
                        sendResponse(exchange, 401, "{\"error\":\"" + authResult.getErrorMessage() + "\"}");
                        return;
                    }
                    
                    String response = String.format(
                        "{\"valid\":true,\"userId\":%d,\"phone\":\"%s\",\"role\":\"%s\"}",
                        authResult.getUserId(),
                        authResult.getPhone(),
                        authResult.getRole()
                    );
                    sendResponse(exchange, 200, response);
                    
                } catch (Exception e) {
                    System.err.println("❌ Token validation error: " + e.getMessage());
                    String errorResponse = "{\"valid\":false,\"error\":\"Token validation failed\"}";
                    sendResponse(exchange, 401, errorResponse);
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    
    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    AuthResult authResult = AuthMiddleware.authenticate(exchange);
                    
                    if (!authResult.isAuthenticated()) {
                        sendResponse(exchange, 401, "{\"error\":\"" + authResult.getErrorMessage() + "\"}");
                        return;
                    }
                    
                    String message = authService.logout(authResult.getUserId());
                    String response = "{\"message\":\"" + message + "\",\"status\":\"success\"}";
                    sendResponse(exchange, 200, response);
                    
                    System.out.println("✅ User logged out: " + authResult.getPhone() + " (ID: " + authResult.getUserId() + ")");
                    
                } catch (Exception e) {
                    System.err.println("❌ Logout error: " + e.getMessage());
                    String errorResponse = "{\"error\":\"Logout failed\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                    sendResponse(exchange, 500, errorResponse);
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    

    
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}