package com.myapp.vendor;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Restaurant;
import com.myapp.common.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API Controller for Vendor Operations (Customer Perspective)
 * 
 * Endpoints:
 * GET    /api/vendors                     - List all active vendors
 * GET    /api/vendors/search              - Search vendors by keyword
 * GET    /api/vendors/{id}                - Get vendor details and menu
 * GET    /api/vendors/{id}/menu           - Get vendor menu organized by categories
 * GET    /api/vendors/{id}/stats          - Get vendor statistics
 * GET    /api/vendors/{id}/available      - Check if vendor is accepting orders
 * GET    /api/vendors/location/{location} - Get vendors by location
 * GET    /api/vendors/category/{category} - Get vendors by food category
 * GET    /api/vendors/featured            - Get featured/popular vendors
 * POST   /api/vendors/filter              - Filter vendors by multiple criteria
 */
public class VendorController implements HttpHandler {
    
    private final VendorService vendorService;
    
    public VendorController() {
        this.vendorService = new VendorService();
    }
    
    // Constructor for dependency injection (testing)
    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange, path);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (NotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    // ==================== GET ENDPOINTS ====================
    
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/vendors")) {
            // GET /api/vendors - List all active vendors
            getAllVendors(exchange);
        } else if (path.equals("/api/vendors/search")) {
            // GET /api/vendors/search?q={keyword} - Search vendors
            searchVendors(exchange);
        } else if (path.matches("/api/vendors/\\d+$")) {
            // GET /api/vendors/{id} - Get vendor details and menu
            try {
                Long vendorId = extractIdFromPath(path, "/api/vendors/");
                getVendorDetails(exchange, vendorId);
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        } else if (path.matches("/api/vendors/\\d+/menu")) {
            // GET /api/vendors/{id}/menu - Get vendor menu
            try {
                Long vendorId = extractIdFromPath(path, "/api/vendors/", "/menu");
                getVendorMenu(exchange, vendorId);
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        } else if (path.matches("/api/vendors/\\d+/stats")) {
            // GET /api/vendors/{id}/stats - Get vendor statistics
            try {
                Long vendorId = extractIdFromPath(path, "/api/vendors/", "/stats");
                getVendorStats(exchange, vendorId);
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        } else if (path.matches("/api/vendors/\\d+/available")) {
            // GET /api/vendors/{id}/available - Check if vendor is accepting orders
            try {
                Long vendorId = extractIdFromPath(path, "/api/vendors/", "/available");
                checkVendorAvailability(exchange, vendorId);
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        } else if (path.matches("/api/vendors/location/.+")) {
            // GET /api/vendors/location/{location} - Get vendors by location
            String location = extractStringFromPath(path, "/api/vendors/location/");
            getVendorsByLocation(exchange, location);
        } else if (path.matches("/api/vendors/category/.+")) {
            // GET /api/vendors/category/{category} - Get vendors by category
            String category = extractStringFromPath(path, "/api/vendors/category/");
            getVendorsByCategory(exchange, category);
        } else if (path.equals("/api/vendors/featured")) {
            // GET /api/vendors/featured - Get featured vendors
            getFeaturedVendors(exchange);
        } else if (path.matches("/api/vendors/[^/]+$") && !path.matches("/api/vendors/\\d+$")) {
            // Handle invalid vendor IDs (non-numeric)
            sendErrorResponse(exchange, 500, "Internal server error");
        } else if (path.matches("/api/vendors/[^/]+/(menu|stats|available)") && !path.matches("/api/vendors/\\d+/(menu|stats|available)")) {
            // Handle invalid vendor IDs in sub-endpoints
            sendErrorResponse(exchange, 500, "Internal server error");
        } else if (path.matches("/api/vendors/location/?$")) {
            // Handle empty location
            sendErrorResponse(exchange, 400, "Location cannot be empty");
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void getAllVendors(HttpExchange exchange) throws IOException {
        List<Restaurant> vendors = vendorService.getAllVendors();
        sendJsonResponse(exchange, 200, vendors);
    }
    
    private void searchVendors(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String searchTerm = "";
        
        if (query != null && query.contains("q=")) {
            searchTerm = extractQueryParam(query, "q");
        }
        
        List<Restaurant> vendors = vendorService.searchVendors(searchTerm);
        Map<String, Object> response = Map.of(
            "vendors", vendors,
            "searchTerm", searchTerm,
            "count", vendors.size()
        );
        sendJsonResponse(exchange, 200, response);
    }
    
    private void getVendorDetails(HttpExchange exchange, Long vendorId) throws IOException {
        try {
            if (vendorId <= 0) {
                sendErrorResponse(exchange, 400, "Vendor ID must be positive");
                return;
            }
            Restaurant vendor = vendorService.getVendor(vendorId);
            sendJsonResponse(exchange, 200, vendor);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals("NotFoundException")) {
                sendErrorResponse(exchange, 404, e.getMessage());
            } else {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        }
    }
    
    private void getVendorMenu(HttpExchange exchange, Long vendorId) throws IOException {
        Map<String, Object> menuData = vendorService.getVendorMenu(vendorId);
        sendJsonResponse(exchange, 200, menuData);
    }
    
    private void getVendorStats(HttpExchange exchange, Long vendorId) throws IOException {
        VendorService.VendorStats stats = vendorService.getVendorStats(vendorId);
        sendJsonResponse(exchange, 200, stats);
    }
    
    private void checkVendorAvailability(HttpExchange exchange, Long vendorId) throws IOException {
        boolean isAvailable = vendorService.isVendorAcceptingOrders(vendorId);
        Map<String, Object> response = Map.of(
            "vendorId", vendorId,
            "acceptingOrders", isAvailable
        );
        sendJsonResponse(exchange, 200, response);
    }
    
    private void getVendorsByLocation(HttpExchange exchange, String location) throws IOException {
        try {
            List<Restaurant> vendors = vendorService.getVendorsByLocation(location);
            Map<String, Object> response = Map.of(
                "vendors", vendors,
                "location", location,
                "count", vendors.size()
            );
            sendJsonResponse(exchange, 200, response);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    private void getVendorsByCategory(HttpExchange exchange, String category) throws IOException {
        List<Restaurant> vendors = vendorService.getVendorsByCategory(category);
        Map<String, Object> response = Map.of(
            "vendors", vendors,
            "category", category,
            "count", vendors.size()
        );
        sendJsonResponse(exchange, 200, response);
    }
    
    private void getFeaturedVendors(HttpExchange exchange) throws IOException {
        List<Restaurant> vendors = vendorService.getFeaturedVendors();
        Map<String, Object> response = Map.of(
            "vendors", vendors,
            "count", vendors.size()
        );
        sendJsonResponse(exchange, 200, response);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/vendors/filter")) {
            // POST /api/vendors/filter - Filter vendors by multiple criteria
            filterVendors(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void filterVendors(HttpExchange exchange) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        String location = getOptionalStringFromMap(requestData, "location", null);
        String category = getOptionalStringFromMap(requestData, "category", null);
        String search = getOptionalStringFromMap(requestData, "search", null);
        
        // Use VendorRepository's filter method
        VendorRepository vendorRepository = new VendorRepository();
        List<Restaurant> vendors = vendorRepository.findByFilters(location, category, search);
        
        Map<String, Object> response = new HashMap<>();
        response.put("vendors", vendors);
        response.put("count", vendors.size());
        response.put("filters", Map.of(
            "location", location != null ? location : "",
            "category", category != null ? category : "",
            "search", search != null ? search : ""
        ));
        
        sendJsonResponse(exchange, 200, response);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private Long extractIdFromPath(String path, String prefix) {
        return Long.parseLong(path.substring(prefix.length()));
    }
    
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        int start = prefix.length();
        int end = path.indexOf(suffix, start);
        return Long.parseLong(path.substring(start, end));
    }
    
    private String extractStringFromPath(String path, String prefix) {
        return java.net.URLDecoder.decode(path.substring(prefix.length()), java.nio.charset.StandardCharsets.UTF_8);
    }
    
    private String extractQueryParam(String query, String param) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2); // Split into at most 2 parts to handle values with "="
            if (kv.length == 2 && kv[0].equals(param)) {
                return java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "";
    }
    
    private Map<String, Object> parseJsonRequest(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        return JsonUtil.fromJson(requestBody, Map.class);
    }
    
    private String getOptionalStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            return defaultValue;
        }
        return value.toString().trim();
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
    
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = Map.of(
            "error", message,
            "status", statusCode,
            "timestamp", java.time.Instant.now().toString()
        );
        sendJsonResponse(exchange, statusCode, errorResponse);
    }
}
