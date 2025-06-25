package com.myapp.ui.common;

/**
 * Frontend-specific constants for JavaFX application
 */
public final class FrontendConstants {
    
    // Private constructor to prevent instantiation
    private FrontendConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ==================== API Configuration ====================
    public static final class API {
        public static final String BASE_URL = "http://localhost:8000/api";
        public static final String AUTH_LOGIN = "/auth/login";
        public static final String AUTH_REGISTER = "/auth/register";
        public static final String AUTH_LOGOUT = "/auth/logout";
        public static final String AUTH_REFRESH = "/auth/refresh";
        
        public static final String RESTAURANTS = "/restaurants";
        public static final String ORDERS = "/orders";
        public static final String ITEMS = "/items";
        public static final String PAYMENTS = "/payments";
        public static final String PROFILE = "/auth/profile";
        
        private API() {}
    }
    
    // ==================== HTTP Configuration ====================
    public static final class HTTP {
        public static final int CONNECT_TIMEOUT_SECONDS = 30;
        public static final int READ_TIMEOUT_SECONDS = 60;
        public static final int WRITE_TIMEOUT_SECONDS = 60;
        public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        
        private HTTP() {}
    }
    
    // ==================== UI Constants ====================
    public static final class UI {
        // Window dimensions
        public static final double DEFAULT_WINDOW_WIDTH = 1200;
        public static final double DEFAULT_WINDOW_HEIGHT = 800;
        public static final double MIN_WINDOW_WIDTH = 800;
        public static final double MIN_WINDOW_HEIGHT = 600;
        
        // Colors
        public static final String PRIMARY_COLOR = "#007bff";
        public static final String SUCCESS_COLOR = "#28a745";
        public static final String WARNING_COLOR = "#ffc107";
        public static final String DANGER_COLOR = "#dc3545";
        public static final String INFO_COLOR = "#17a2b8";
        public static final String SECONDARY_COLOR = "#6c757d";
        
        // CSS Classes
        public static final String SUCCESS_STYLE = "-fx-background-color: #d4edda; -fx-text-fill: #155724;";
        public static final String ERROR_STYLE = "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;";
        public static final String WARNING_STYLE = "-fx-background-color: #fff3cd; -fx-text-fill: #856404;";
        public static final String INFO_STYLE = "-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460;";
        
        // Animation durations (milliseconds)
        public static final int FADE_DURATION = 300;
        public static final int SLIDE_DURATION = 500;
        public static final int LOADING_ANIMATION_DURATION = 1000;
        
        private UI() {}
    }
    
    // ==================== Messages ====================
    public static final class MESSAGES {
        // Persian messages for UI
        public static final String LOGIN_SUCCESS = "ورود موفقیت‌آمیز بود";
        public static final String LOGIN_FAILED = "خطا در ورود";
        public static final String REGISTER_SUCCESS = "ثبت‌نام موفقیت‌آمیز بود";
        public static final String REGISTER_FAILED = "خطا در ثبت‌نام";
        public static final String LOGOUT_SUCCESS = "خروج موفقیت‌آمیز بود";
        public static final String PROFILE_UPDATED = "پروفایل بروزرسانی شد";
        public static final String ORDER_PLACED = "سفارش ثبت شد";
        public static final String PAYMENT_SUCCESS = "پرداخت موفقیت‌آمیز بود";
        public static final String PAYMENT_FAILED = "خطا در پرداخت";
        
        // Error messages
        public static final String NETWORK_ERROR = "خطا در ارتباط با سرور";
        public static final String INVALID_CREDENTIALS = "شماره تلفن یا رمز عبور اشتباه است";
        public static final String REQUIRED_FIELD = "این فیلد الزامی است";
        public static final String INVALID_EMAIL = "فرمت ایمیل صحیح نیست";
        public static final String INVALID_PHONE = "فرمت شماره تلفن صحیح نیست";
        public static final String WEAK_PASSWORD = "رمز عبور باید حداقل ۸ کاراکتر باشد";
        public static final String PASSWORD_MISMATCH = "رمز عبور و تکرار آن یکسان نیستند";
        
        // Loading messages
        public static final String LOADING = "در حال بارگذاری...";
        public static final String PROCESSING = "در حال پردازش...";
        public static final String CONNECTING = "در حال اتصال...";
        public static final String SAVING = "در حال ذخیره...";
        
        private MESSAGES() {}
    }
    
    // ==================== Validation ====================
    public static final class VALIDATION {
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_PASSWORD_LENGTH = 128;
        public static final int PHONE_LENGTH = 11;
        public static final String PHONE_PREFIX = "09";
        public static final int MAX_NAME_LENGTH = 100;
        public static final int MAX_EMAIL_LENGTH = 100;
        public static final int MAX_ADDRESS_LENGTH = 500;
        
        // Regex patterns
        public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        public static final String PHONE_PATTERN = "^09\\d{9}$";
        public static final String PERSIAN_NAME_PATTERN = "^[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF\\s\\w.-]+$";
        
        private VALIDATION() {}
    }
    
    // ==================== File System ====================
    public static final class FILES {
        public static final String USER_DATA_DIR = System.getProperty("user.home") + "/.food-ordering";
        public static final String SETTINGS_FILE = "settings.json";
        public static final String CACHE_DIR = "cache";
        public static final String LOGS_DIR = "logs";
        
        private FILES() {}
    }
    
    // ==================== Currency & Formatting ====================
    public static final class FORMAT {
        public static final String CURRENCY_SYMBOL = "تومان";
        public static final String DECIMAL_FORMAT = "#,##0";
        public static final String DECIMAL_FORMAT_WITH_FRACTION = "#,##0.00";
        public static final String DATE_FORMAT = "yyyy/MM/dd";
        public static final String TIME_FORMAT = "HH:mm";
        public static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm";
        
        private FORMAT() {}
    }
    
    // ==================== Test Configuration ====================
    public static final class TEST {
        public static final int DEFAULT_TIMEOUT_SECONDS = 30;
        public static final int LONG_TIMEOUT_SECONDS = 60;
        public static final int UI_WAIT_TIME_MS = 100;
        public static final int NETWORK_TIMEOUT_MS = 5000;
        
        // Test data
        public static final String TEST_PHONE = "09123456789";
        public static final String TEST_EMAIL = "test@example.com";
        public static final String TEST_PASSWORD = "TestPass123@";
        public static final String TEST_NAME = "تست کاربر";
        
        private TEST() {}
    }
    
    // ==================== Performance ====================
    public static final class PERFORMANCE {
        public static final int MAX_CACHE_SIZE = 100;
        public static final int CACHE_EXPIRY_MINUTES = 30;
        public static final int MAX_CONCURRENT_REQUESTS = 10;
        public static final int REQUEST_RETRY_COUNT = 3;
        public static final int REQUEST_RETRY_DELAY_MS = 1000;
        
        private PERFORMANCE() {}
    }
} 