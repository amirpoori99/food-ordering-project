package com.myapp.ui.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP Client utility for communicating with the food ordering backend APIs
 * Handles authentication, request/response processing, and error handling
 */
public class HttpClientUtil {
    
    private static final String BASE_URL = "http://localhost:8081/api";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    private static String accessToken = null;
    private static String refreshToken = null;
    
    // ==================== AUTHENTICATION ====================
    
    /**
     * Set authentication tokens
     */
    public static void setTokens(String accessToken, String refreshToken) {
        HttpClientUtil.accessToken = accessToken;
        HttpClientUtil.refreshToken = refreshToken;
    }
    
    /**
     * Clear authentication tokens (logout)
     */
    public static void clearTokens() {
        HttpClientUtil.accessToken = null;
        HttpClientUtil.refreshToken = null;
    }
    
    /**
     * Get current access token
     */
    public static String getAccessToken() {
        return accessToken;
    }
    
    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        return accessToken != null && !accessToken.trim().isEmpty();
    }
    
    // ==================== HTTP METHODS ====================
    
    /**
     * POST request with JSON body
     */
    public static ApiResponse post(String endpoint, Object requestBody) throws IOException {
        String url = BASE_URL + endpoint;
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON));
        
        // Add authorization header if authenticated
        if (isAuthenticated()) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            return processResponse(response);
        }
    }
    
    /**
     * GET request
     */
    public static ApiResponse get(String endpoint) throws IOException {
        String url = BASE_URL + endpoint;
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();
        
        // Add authorization header if authenticated
        if (isAuthenticated()) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            return processResponse(response);
        }
    }
    
    /**
     * PUT request with JSON body
     */
    public static ApiResponse put(String endpoint, Object requestBody) throws IOException {
        String url = BASE_URL + endpoint;
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .put(RequestBody.create(jsonBody, JSON));
        
        // Add authorization header if authenticated
        if (isAuthenticated()) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            return processResponse(response);
        }
    }
    
    /**
     * DELETE request
     */
    public static ApiResponse delete(String endpoint) throws IOException {
        String url = BASE_URL + endpoint;
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .delete();
        
        // Add authorization header if authenticated
        if (isAuthenticated()) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            return processResponse(response);
        }
    }
    
    // ==================== AUTHENTICATION SPECIFIC METHODS ====================
    
    /**
     * Login user and store tokens
     */
    public static ApiResponse login(String phone, String password) throws IOException {
        LoginRequest loginRequest = new LoginRequest(phone, password);
        ApiResponse response = post("/auth/login", loginRequest);
        
        if (response.isSuccess() && response.getData() != null) {
            JsonNode data = response.getData();
            if (data.has("accessToken") && data.has("refreshToken")) {
                setTokens(data.get("accessToken").asText(), data.get("refreshToken").asText());
            }
        }
        
        return response;
    }
    
    /**
     * Register new user
     */
    public static ApiResponse register(String fullName, String phone, String email, String password, String address) throws IOException {
        RegisterRequest registerRequest = new RegisterRequest(fullName, phone, email, password, address);
        return post("/auth/register", registerRequest);
    }
    
    /**
     * Logout user
     */
    public static ApiResponse logout() throws IOException {
        if (!isAuthenticated()) {
            return new ApiResponse(true, 200, "Already logged out", null);
        }
        
        ApiResponse response = post("/auth/logout", new Object());
        clearTokens();
        return response;
    }
    
    /**
     * Refresh access token
     */
    public static ApiResponse refreshAccessToken() throws IOException {
        if (refreshToken == null) {
            throw new IllegalStateException("No refresh token available");
        }
        
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
        ApiResponse response = post("/auth/refresh", refreshRequest);
        
        if (response.isSuccess() && response.getData() != null) {
            JsonNode data = response.getData();
            if (data.has("accessToken") && data.has("refreshToken")) {
                setTokens(data.get("accessToken").asText(), data.get("refreshToken").asText());
            }
        }
        
        return response;
    }
    
    // ==================== RESPONSE PROCESSING ====================
    
    /**
     * Process HTTP response and create ApiResponse
     */
    private static ApiResponse processResponse(Response response) throws IOException {
        String responseBody = response.body() != null ? response.body().string() : "";
        boolean isSuccess = response.isSuccessful();
        int statusCode = response.code();
        
        JsonNode data = null;
        String message = null;
        
        if (!responseBody.isEmpty()) {
            try {
                JsonNode json = objectMapper.readTree(responseBody);
                
                if (json.has("message")) {
                    message = json.get("message").asText();
                }
                
                if (json.has("error") && !isSuccess) {
                    message = json.get("error").asText();
                } else {
                    data = json;
                }
            } catch (Exception e) {
                // If JSON parsing fails, use raw response body as message
                message = responseBody;
            }
        }
        
        return new ApiResponse(isSuccess, statusCode, message, data);
    }
    
    // ==================== REQUEST DTOs ====================
    
    public static class LoginRequest {
        public String phone;
        public String password;
        
        public LoginRequest(String phone, String password) {
            this.phone = phone;
            this.password = password;
        }
    }
    
    public static class RegisterRequest {
        public String fullName;
        public String phone;
        public String email;
        public String password;
        public String address;
        
        public RegisterRequest(String fullName, String phone, String email, String password, String address) {
            this.fullName = fullName;
            this.phone = phone;
            this.email = email;
            this.password = password;
            this.address = address;
        }
    }
    
    public static class RefreshTokenRequest {
        public String refreshToken;
        
        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
    
    // ==================== RESPONSE WRAPPER ====================
    
    public static class ApiResponse {
        private final boolean success;
        private final int statusCode;
        private final String message;
        private final JsonNode data;
        
        public ApiResponse(boolean success, int statusCode, String message, JsonNode data) {
            this.success = success;
            this.statusCode = statusCode;
            this.message = message;
            this.data = data;
        }
        
        public boolean isSuccess() { return success; }
        public int getStatusCode() { return statusCode; }
        public String getMessage() { return message; }
        public JsonNode getData() { return data; }
        
        @Override
        public String toString() {
            return String.format("ApiResponse{success=%s, statusCode=%d, message='%s'}", 
                               success, statusCode, message);
        }
    }
}
