package com.myapp;

import com.myapp.auth.AuthRepository;
import com.myapp.auth.AuthService;
import com.myapp.restaurant.RestaurantRepository;
import com.myapp.common.utils.DatabaseUtil;
import com.myapp.common.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
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
    
    public static void main(String[] args) throws IOException {
        System.out.println("🚀 Starting Food Ordering Backend Server...");
        
        // Initialize services
        AuthRepository authRepo = new AuthRepository();
        authService = new AuthService(authRepo);
        
        RestaurantRepository restaurantRepo = new RestaurantRepository();
        
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
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Add contexts (endpoints)
        server.createContext("/api/test", new TestHandler());
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/restaurants", new RestaurantHandler());
        server.createContext("/health", new HealthHandler());
        
        // Configure thread pool
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Start server
        server.start();
        System.out.println("🚀 Server started on http://localhost:8080");
        System.out.println("📋 Available endpoints:");
        System.out.println("   GET  /health - Health check");
        System.out.println("   GET  /api/test - Simple test");
        System.out.println("   POST /api/auth/register - User registration");
        System.out.println("   POST /api/auth/login - User login");
        System.out.println("   GET  /api/restaurants - List restaurants");
        
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
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String response = "{\"message\":\"Login endpoint ready\",\"status\":\"TODO\"}";
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    
    static class RestaurantHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = "{\"restaurants\":[],\"message\":\"Restaurant endpoint ready\"}";
                sendResponse(exchange, 200, response);
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