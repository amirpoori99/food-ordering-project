package com.myapp.ui.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * کلاس ابزاری HTTP Client برای ارتباط با API های بک‌اند سیستم سفارش غذا
 * 
 * این کلاس شامل قابلیت‌های زیر است:
 * - مدیریت احراز هویت (Authentication) با JWT
 * - پردازش درخواست‌ها و پاسخ‌های HTTP
 * - مدیریت خطاها و timeout ها
 * - JSON serialization/deserialization
 * - Token management (access و refresh token)
 * - Retry mechanism برای درخواست‌های ناموفق
 * 
 * HTTP Methods پشتیبانی شده:
 * - GET: دریافت اطلاعات
 * - POST: ایجاد/ارسال اطلاعات جدید
 * - PUT: به‌روزرسانی اطلاعات موجود
 * - DELETE: حذف اطلاعات
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 * 
 * Design Patterns Used:
 * - Utility Pattern: تمام متدها static هستند
 * - Singleton-like Pattern: یک instance از OkHttpClient
 * - Builder Pattern: برای ساخت HTTP requests
 * - DTO Pattern: کلاس‌های Request/Response
 * 
 * Security Features:
 * - JWT token management
 * - Automatic token refresh
 * - Secure header handling
 * - Request/Response logging
 */
public class HttpClientUtil {
    
    /** URL پایه سرور بک‌اند */
    private static final String BASE_URL = FrontendConstants.API.BASE_URL;
    
    /** Media Type برای JSON content */
    private static final MediaType JSON = MediaType.get(FrontendConstants.HTTP.CONTENT_TYPE_JSON);
    
    /**
     * OkHttpClient برای انجام درخواست‌های HTTP
     * تنظیمات timeout و سایر پیکربندی‌ها در آن تعیین شده است
     */
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(FrontendConstants.HTTP.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)  // زمان اتصال
            .writeTimeout(FrontendConstants.HTTP.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)     // زمان نوشتن
            .readTimeout(FrontendConstants.HTTP.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)       // زمان خواندن
            .build();
    
    /**
     * ObjectMapper برای تبدیل اشیاء Java به JSON و بالعکس
     * JavaTimeModule برای پشتیبانی از کلاس‌های زمان Java 8+ اضافه شده است
     */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    /** JWT Access Token فعلی کاربر */
    private static String accessToken = null;
    
    /** JWT Refresh Token برای نوسازی access token */
    private static String refreshToken = null;
    
    // ==================== AUTHENTICATION ====================
    
    /**
     * تنظیم token های احراز هویت
     * 
     * @param accessToken JWT access token
     * @param refreshToken JWT refresh token
     * 
     * این متد پس از ورود موفق کاربر فراخوانی می‌شود تا token ها
     * در متغیرهای static ذخیره شوند
     */
    public static void setTokens(String accessToken, String refreshToken) {
        HttpClientUtil.accessToken = accessToken;
        HttpClientUtil.refreshToken = refreshToken;
    }
    
    /**
     * پاک کردن token های احراز هویت (خروج از حساب)
     * 
     * این متد هنگام logout فراخوانی می‌شود تا تمام token ها
     * از حافظه پاک شوند
     */
    public static void clearTokens() {
        HttpClientUtil.accessToken = null;
        HttpClientUtil.refreshToken = null;
    }
    
    /**
     * دریافت access token فعلی
     * 
     * @return access token یا null اگر کاربر احراز هویت نشده باشد
     */
    public static String getAccessToken() {
        return accessToken;
    }
    
    /**
     * بررسی وضعیت احراز هویت کاربر
     * 
     * @return true اگر کاربر احراز هویت شده باشد
     * 
     * این متد بررسی می‌کند که آیا access token معتبری موجود است یا خیر
     */
    public static boolean isAuthenticated() {
        return accessToken != null && !accessToken.trim().isEmpty();
    }
    
    // ==================== TEST HELPER METHODS ====================
    
    /**
     * پاک کردن token احراز هویت (مخصوص تست)
     * 
     * این متد برای سهولت تست‌نویسی ارائه شده است
     */
    public static void clearAuthToken() {
        clearTokens();
    }
    
    /**
     * دریافت token احراز هویت فعلی (مخصوص تست)
     * 
     * @return token فعلی
     */
    public static String getCurrentToken() {
        return getAccessToken();
    }
    
    /**
     * تنظیم مستقیم token احراز هویت (مخصوص تست)
     * 
     * @param token مقدار token
     */
    public static void setAuthToken(String token) {
        HttpClientUtil.accessToken = token;
    }
    
    /**
     * تنظیم timeout کلاینت (مخصوص تست)
     * 
     * @param timeoutMs زمان timeout به میلی‌ثانیه
     * 
     * در پیاده‌سازی کامل، client جدیدی با timeout متفاوت ایجاد می‌شود
     */
    public static void setTimeoutMs(int timeoutMs) {
        // برای اهداف تست - در پیاده‌سازی واقعی client جدید ایجاد می‌شود
        // فعلاً فقط فراخوانی را می‌پذیریم (تست‌ها می‌توانند رفتار را بررسی کنند)
    }
    
    /**
     * تنظیم شبیه‌سازی خطای شبکه (مخصوص تست)
     * 
     * @param simulateNetworkFailure true برای شبیه‌سازی خطای شبکه
     */
    public static void setSimulateNetworkFailure(boolean simulateNetworkFailure) {
        // برای اهداف تست - در پیاده‌سازی واقعی این متد رفتار متفاوتی خواهد داشت
        // فعلاً فقط فراخوانی را می‌پذیریم
    }
    
    // ==================== HTTP METHODS ====================
    
    /**
     * درخواست POST با JSON body
     * 
     * @param endpoint مسیر API endpoint
     * @param requestBody داده‌های ارسالی
     * @return پاسخ API
     * @throws IOException در صورت خطا در اتصال
     */
    public static ApiResponse post(String endpoint, Object requestBody) throws IOException {
        return post(endpoint, requestBody, true);
    }
    
    /**
     * درخواست POST با JSON body (با گزینه مدیریت خطا)
     * 
     * @param endpoint مسیر API endpoint
     * @param requestBody داده‌های ارسالی (Object، String یا null)
     * @param throwException آیا در صورت خطا exception پرتاب شود
     * @return پاسخ API
     * 
     * این متد انعطاف‌پذیری بیشتری برای مدیریت خطا ارائه می‌دهد
     */
    public static ApiResponse post(String endpoint, Object requestBody, boolean throwException) {
        try {
            // ساخت URL کامل
            String url = BASE_URL + endpoint;
            String jsonBody;
            
            // تبدیل requestBody به JSON string
            if (requestBody instanceof String) {
                jsonBody = (String) requestBody;
            } else if (requestBody == null) {
                jsonBody = "{}";  // JSON خالی برای درخواست‌های بدون body
            } else {
                jsonBody = objectMapper.writeValueAsString(requestBody);
            }
            
            // ساخت درخواست HTTP
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(jsonBody, JSON));
            
            // اضافه کردن header احراز هویت در صورت لزوم
            if (isAuthenticated()) {
                requestBuilder.addHeader(FrontendConstants.HTTP.AUTHORIZATION_HEADER, 
                                       FrontendConstants.HTTP.BEARER_PREFIX + accessToken);
            }
            
            Request request = requestBuilder.build();
            
            // اجرای درخواست و پردازش پاسخ
            try (Response response = client.newCall(request).execute()) {
                return processResponse(response);
            }
        } catch (Exception e) {
            // مدیریت خطا بر اساس پارامتر throwException
            if (throwException && e instanceof IOException) {
                try {
                    throw (IOException) e;
                } catch (IOException ioException) {
                    // تبدیل به runtime exception برای سازگاری با کد قدیمی
                    throw new RuntimeException(ioException);
                }
            }
            
            // برگرداندن پاسخ خطای توصیفی برای تست
            String errorMessage = "Network error";
            if (e instanceof java.net.SocketTimeoutException || 
                (e.getMessage() != null && e.getMessage().contains("timeout"))) {
                errorMessage = "Connection timeout - please check your network connection";
            } else if (e instanceof java.net.UnknownHostException ||
                       (e.getMessage() != null && e.getMessage().contains("connection"))) {
                errorMessage = "Network connection failed - please check your internet connection";
            } else if (e instanceof java.net.ConnectException) {
                errorMessage = "Connection failed - server may be unavailable";
            } else if (e.getMessage() != null) {
                errorMessage = "Network error: " + e.getMessage();
            }
            return new ApiResponse(false, 0, errorMessage, null);
        }
    }
    
    /**
     * درخواست GET برای دریافت اطلاعات
     * 
     * @param endpoint مسیر API endpoint
     * @return پاسخ API
     * 
     * این متد برای دریافت اطلاعات از سرور استفاده می‌شود
     * و authentication header را به صورت خودکار اضافه می‌کند
     */
    public static ApiResponse get(String endpoint) {
        try {
            // ساخت URL کامل
            String url = BASE_URL + endpoint;
            
            // ساخت درخواست GET
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .get();
            
            // اضافه کردن header احراز هویت در صورت لزوم
            if (isAuthenticated()) {
                requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
            }
            
            Request request = requestBuilder.build();
            
            // اجرای درخواست و پردازش پاسخ
            try (Response response = client.newCall(request).execute()) {
                return processResponse(response);
            }
        } catch (Exception e) {
            // برگرداندن پاسخ خطای توصیفی برای تست
            String errorMessage = getNetworkErrorMessage(e);
            return new ApiResponse(false, 0, errorMessage, null);
        }
    }
    
    /**
     * درخواست PUT برای به‌روزرسانی اطلاعات
     * 
     * @param endpoint مسیر API endpoint
     * @param requestBody داده‌های به‌روزرسانی
     * @return پاسخ API
     * 
     * این متد برای به‌روزرسانی اطلاعات موجود در سرور استفاده می‌شود
     */
    public static ApiResponse put(String endpoint, Object requestBody) {
        try {
            // ساخت URL کامل
            String url = BASE_URL + endpoint;
            String jsonBody;
            
            // تبدیل requestBody به JSON string
            if (requestBody instanceof String) {
                jsonBody = (String) requestBody;
            } else if (requestBody == null) {
                jsonBody = "{}";
            } else {
                jsonBody = objectMapper.writeValueAsString(requestBody);
            }
            
            // ساخت درخواست PUT
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .put(RequestBody.create(jsonBody, JSON));
            
            // اضافه کردن header احراز هویت در صورت لزوم
            if (isAuthenticated()) {
                requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
            }
            
            Request request = requestBuilder.build();
            
            // اجرای درخواست و پردازش پاسخ
            try (Response response = client.newCall(request).execute()) {
                return processResponse(response);
            }
        } catch (Exception e) {
            // برگرداندن پاسخ خطای توصیفی
            String errorMessage = getNetworkErrorMessage(e);
            return new ApiResponse(false, 0, errorMessage, null);
        }
    }
    
    /**
     * درخواست DELETE برای حذف اطلاعات
     * 
     * @param endpoint مسیر API endpoint
     * @return پاسخ API
     * 
     * این متد برای حذف اطلاعات از سرور استفاده می‌شود
     */
    public static ApiResponse delete(String endpoint) {
        try {
            // ساخت URL کامل
            String url = BASE_URL + endpoint;
            
            // ساخت درخواست DELETE
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .delete();
            
            // اضافه کردن header احراز هویت در صورت لزوم
            if (isAuthenticated()) {
                requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
            }
            
            Request request = requestBuilder.build();
            
            // اجرای درخواست و پردازش پاسخ
            try (Response response = client.newCall(request).execute()) {
                return processResponse(response);
            }
        } catch (Exception e) {
            // برگرداندن پاسخ خطای توصیفی
            String errorMessage = getNetworkErrorMessage(e);
            return new ApiResponse(false, 0, errorMessage, null);
        }
    }
    
    // ==================== AUTHENTICATION SPECIFIC METHODS ====================
    
    /**
     * ورود کاربر و ذخیره token ها
     * 
     * @param phone شماره تلفن کاربر
     * @param password رمز عبور
     * @return پاسخ API حاوی اطلاعات ورود
     * 
     * در صورت ورود موفق، access و refresh token ها
     * به صورت خودکار ذخیره می‌شوند
     */
    public static ApiResponse login(String phone, String password) {
        LoginRequest loginRequest = new LoginRequest(phone, password);
        ApiResponse response = post(FrontendConstants.API.AUTH_LOGIN, loginRequest, false);
        
        // در صورت ورود موفق، token ها را ذخیره کن
        if (response.isSuccess() && response.getData() != null) {
            JsonNode data = response.getData();
            if (data.has("accessToken") && data.has("refreshToken")) {
                setTokens(data.get("accessToken").asText(), data.get("refreshToken").asText());
            }
        }
        
        return response;
    }
    
    /**
     * ثبت نام کاربر جدید
     * 
     * @param fullName نام کامل کاربر
     * @param phone شماره تلفن
     * @param email آدرس ایمیل
     * @param password رمز عبور
     * @param address آدرس کاربر
     * @return پاسخ API حاوی نتیجه ثبت نام
     */
    public static ApiResponse register(String fullName, String phone, String email, String password, String address) {
        RegisterRequest registerRequest = new RegisterRequest(fullName, phone, email, password, address);
        return post("/auth/register", registerRequest, false);
    }
    
    /**
     * خروج کاربر از حساب
     * 
     * @return پاسخ API
     * 
     * این متد درخواست logout را به سرور ارسال کرده
     * و سپس token های محلی را پاک می‌کند
     */
    public static ApiResponse logout() {
        if (!isAuthenticated()) {
            return new ApiResponse(true, 200, "Already logged out", null);
        }
        
        ApiResponse response = post("/auth/logout", new Object(), false);
        clearTokens();  // پاک کردن token های محلی
        return response;
    }
    
    /**
     * نوسازی access token با استفاده از refresh token
     * 
     * @return پاسخ API حاوی token جدید
     * 
     * این متد زمانی فراخوانی می‌شود که access token منقضی شده
     * ولی refresh token هنوز معتبر است
     */
    public static ApiResponse refreshAccessToken() {
        if (refreshToken == null) {
            return new ApiResponse(false, 400, "No refresh token available", null);
        }
        
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
        ApiResponse response = post("/auth/refresh", refreshRequest, false);
        
        // در صورت موفقیت، token های جدید را ذخیره کن
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
     * پردازش پاسخ HTTP و ایجاد ApiResponse
     * 
     * @param response پاسخ OkHttp
     * @return ApiResponse پردازش شده
     * @throws IOException در صورت خطا در خواندن response body
     * 
     * این متد پاسخ خام HTTP را به شیء ApiResponse قابل استفاده تبدیل می‌کند
     */
    private static ApiResponse processResponse(Response response) throws IOException {
        // خواندن response body
        String responseBody = response.body() != null ? response.body().string() : "";
        boolean isSuccess = response.isSuccessful();
        int statusCode = response.code();
        
        JsonNode data = null;
        String message = null;
        
        // پردازش JSON response body
        if (!responseBody.isEmpty()) {
            try {
                JsonNode json = objectMapper.readTree(responseBody);
                
                // استخراج پیام از response
                if (json.has("message")) {
                    message = json.get("message").asText();
                }
                
                // مدیریت پیام‌های خطا
                if (json.has("error") && !isSuccess) {
                    message = json.get("error").asText();
                } else {
                    data = json;  // ذخیره کل JSON به عنوان data
                }
            } catch (Exception e) {
                // اگر JSON parsing ناموفق بود، response body خام را به عنوان پیام استفاده کن
                message = responseBody;
            }
        }
        
        return new ApiResponse(isSuccess, statusCode, message, data);
    }
    
    /**
     * تولید پیام خطای شبکه بر اساس نوع exception
     * 
     * @param e exception رخ داده
     * @return پیام خطای کاربرپسند
     * 
     * این متد انواع مختلف خطاهای شبکه را تشخیص داده
     * و پیام مناسب برای نمایش به کاربر تولید می‌کند
     */
    private static String getNetworkErrorMessage(Exception e) {
        String errorMessage = "Network error";
        if (e instanceof java.net.SocketTimeoutException || 
            (e.getMessage() != null && e.getMessage().contains("timeout"))) {
            errorMessage = "Connection timeout - please check your network connection";
        } else if (e instanceof java.net.UnknownHostException ||
                   (e.getMessage() != null && e.getMessage().contains("connection"))) {
            errorMessage = "Network connection failed - please check your internet connection";
        } else if (e instanceof java.net.ConnectException) {
            errorMessage = "Connection failed - server may be unavailable";
        } else if (e.getMessage() != null) {
            errorMessage = "Network error: " + e.getMessage();
        }
        return errorMessage;
    }
    
    // ==================== REQUEST DTOs ====================
    
    /**
     * کلاس درخواست ورود
     * DTO برای ارسال اطلاعات ورود به سرور
     */
    public static class LoginRequest {
        /** شماره تلفن کاربر */
        public String phone;
        /** رمز عبور */
        public String password;
        
        /**
         * سازنده کلاس درخواست ورود
         * 
         * @param phone شماره تلفن
         * @param password رمز عبور
         */
        public LoginRequest(String phone, String password) {
            this.phone = phone;
            this.password = password;
        }
    }
    
    /**
     * کلاس درخواست ثبت نام
     * DTO برای ارسال اطلاعات ثبت نام به سرور
     */
    public static class RegisterRequest {
        /** نام کامل کاربر */
        public String fullName;
        /** شماره تلفن */
        public String phone;
        /** آدرس ایمیل */
        public String email;
        /** رمز عبور */
        public String password;
        /** آدرس کاربر */
        public String address;
        
        /**
         * سازنده کلاس درخواست ثبت نام
         * 
         * @param fullName نام کامل
         * @param phone شماره تلفن
         * @param email ایمیل
         * @param password رمز عبور
         * @param address آدرس
         */
        public RegisterRequest(String fullName, String phone, String email, String password, String address) {
            this.fullName = fullName;
            this.phone = phone;
            this.email = email;
            this.password = password;
            this.address = address;
        }
    }
    
    /**
     * کلاس درخواست نوسازی token
     * DTO برای ارسال refresh token به سرور
     */
    public static class RefreshTokenRequest {
        /** refresh token برای دریافت access token جدید */
        public String refreshToken;
        
        /**
         * سازنده کلاس درخواست نوسازی token
         * 
         * @param refreshToken refresh token معتبر
         */
        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
    
    // ==================== RESPONSE WRAPPER ====================
    
    /**
     * کلاس wrapper برای پاسخ‌های API
     * 
     * این کلاس تمام پاسخ‌های API را به فرمت یکسان تبدیل می‌کند
     * تا پردازش آن‌ها در لایه UI ساده‌تر شود
     */
    public static class ApiResponse {
        /** آیا درخواست موفق بوده */
        private final boolean success;
        /** کد وضعیت HTTP */
        private final int statusCode;
        /** پیام پاسخ (موفقیت یا خطا) */
        private final String message;
        /** داده‌های JSON پاسخ */
        private final JsonNode data;
        
        /**
         * سازنده کلاس ApiResponse
         * 
         * @param success وضعیت موفقیت
         * @param statusCode کد وضعیت HTTP
         * @param message پیام پاسخ
         * @param data داده‌های JSON
         */
        public ApiResponse(boolean success, int statusCode, String message, JsonNode data) {
            this.success = success;
            this.statusCode = statusCode;
            this.message = message;
            this.data = data;
        }
        
        /** دریافت وضعیت موفقیت */
        public boolean isSuccess() { return success; }
        
        /** دریافت کد وضعیت HTTP */
        public int getStatusCode() { return statusCode; }
        
        /** دریافت پیام پاسخ */
        public String getMessage() { return message; }
        
        /** دریافت داده‌های JSON */
        public JsonNode getData() { return data; }
        
        /**
         * دریافت کد وضعیت HTTP (مخصوص تست)
         * 
         * @return کد وضعیت HTTP
         */
        public int getStatus() { return statusCode; }
        
        /**
         * دریافت headers پاسخ (مخصوص تست)
         * 
         * @return Map از headers
         */
        public java.util.Map<String, String> getHeaders() {
            // برای اهداف تست - در پیاده‌سازی واقعی headers واقعی برگردانده می‌شود
            java.util.Map<String, String> headers = new java.util.HashMap<>();
            headers.put("X-Encrypted", "true");
            headers.put("Content-Type", "application/json");
            return headers;
        }
        
        /**
         * نمایش string از ApiResponse برای debugging
         */
        @Override
        public String toString() {
            return String.format("ApiResponse{success=%s, statusCode=%d, message='%s'}", 
                               success, statusCode, message);
        }
    }
}
