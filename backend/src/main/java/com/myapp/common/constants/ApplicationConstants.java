package com.myapp.common.constants;

/**
 * Application-wide constants to avoid hardcoded values
 */
public final class ApplicationConstants {
    
    // Private constructor to prevent instantiation
    private ApplicationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ==================== API Constants ====================
    public static final class API {
        public static final String BASE_PATH = "/api";
        public static final String AUTH_PATH = "/api/auth";
        public static final String RESTAURANTS_PATH = "/api/restaurants";
        public static final String ORDERS_PATH = "/api/orders";
        public static final String ITEMS_PATH = "/api/items";
        public static final String PAYMENTS_PATH = "/api/payments";
        public static final String DELIVERY_PATH = "/api/delivery";
        public static final String COUPONS_PATH = "/api/coupons";
        public static final String FAVORITES_PATH = "/api/favorites";
        public static final String RATINGS_PATH = "/api/ratings";
        public static final String ADMIN_PATH = "/api/admin";
        public static final String VENDORS_PATH = "/api/vendors";
        public static final String NOTIFICATIONS_PATH = "/api/notifications";
        
        private API() {}
    }
    
    // ==================== Database Constants ====================
    public static final class DATABASE {
        public static final String SQLITE_URL = "jdbc:sqlite:food_ordering.db?journal_mode=WAL&synchronous=NORMAL&busy_timeout=30000";
        public static final int CONNECTION_POOL_SIZE = 5;
        public static final int JDBC_BATCH_SIZE = 20;
        public static final int MAX_RETRY_ATTEMPTS = 3;
        public static final int RETRY_DELAY_MS = 50;
        public static final int BUSY_TIMEOUT_MS = 30000;
        
        private DATABASE() {}
    }
    
    // ==================== Business Logic Constants ====================
    public static final class BUSINESS {
        public static final double MAX_WALLET_CHARGE = 10000.0;
        public static final double MIN_ORDER_AMOUNT = 10.0;
        public static final double DELIVERY_FEE = 15.0;
        public static final int MAX_COUPON_USAGE_PER_USER = 1;
        public static final int PASSWORD_MIN_LENGTH = 8;
        public static final int PHONE_NUMBER_LENGTH = 11;
        public static final String PHONE_PREFIX = "09";
        
        // Order status flow
        public static final String ORDER_STATUS_PENDING = "PENDING";
        public static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
        public static final String ORDER_STATUS_PREPARING = "PREPARING";
        public static final String ORDER_STATUS_READY = "READY";
        public static final String ORDER_STATUS_PICKED_UP = "PICKED_UP";
        public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
        public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
        
        // User roles
        public static final String ROLE_BUYER = "BUYER";
        public static final String ROLE_SELLER = "SELLER";
        public static final String ROLE_COURIER = "COURIER";
        public static final String ROLE_ADMIN = "ADMIN";
        
        // Payment methods
        public static final String PAYMENT_CARD = "CARD";
        public static final String PAYMENT_WALLET = "WALLET";
        public static final String PAYMENT_COD = "COD";
        
        private BUSINESS() {}
    }
    
    // ==================== Security Constants ====================
    public static final class SECURITY {
        public static final String JWT_SECRET_KEY = "mySecretKey12345";
        public static final long ACCESS_TOKEN_VALIDITY = 3600000; // 1 hour in milliseconds
        public static final long REFRESH_TOKEN_VALIDITY = 604800000; // 7 days in milliseconds
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final int BCRYPT_ROUNDS = 12;
        
        private SECURITY() {}
    }
    
    // ==================== Validation Constants ====================
    public static final class VALIDATION {
        public static final int MAX_NAME_LENGTH = 100;
        public static final int MAX_DESCRIPTION_LENGTH = 500;
        public static final int MAX_ADDRESS_LENGTH = 255;
        public static final int MAX_EMAIL_LENGTH = 100;
        public static final int MIN_RATING = 1;
        public static final int MAX_RATING = 5;
        public static final double MIN_PRICE = 0.01;
        public static final double MAX_PRICE = 10000.0;
        public static final int MAX_QUANTITY = 1000;
        public static final int MIN_QUANTITY = 1;
        
        private VALIDATION() {}
    }
    
    // ==================== Error Messages ====================
    public static final class ERROR_MESSAGES {
        public static final String INVALID_CREDENTIALS = "Invalid phone or password";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String RESTAURANT_NOT_FOUND = "Restaurant not found";
        public static final String ORDER_NOT_FOUND = "Order not found";
        public static final String ITEM_NOT_FOUND = "Item not found";
        public static final String INSUFFICIENT_STOCK = "Insufficient stock";
        public static final String INVALID_ORDER_STATUS = "Invalid order status";
        public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
        public static final String DUPLICATE_PHONE = "Phone number already exists";
        public static final String INVALID_PHONE_FORMAT = "Invalid phone number format";
        public static final String WEAK_PASSWORD = "Password does not meet security requirements";
        public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
        public static final String REQUIRED_FIELD_MISSING = "Required field is missing";
        public static final String INVALID_NUMBER_FORMAT = "Invalid number format";
        public static final String WALLET_INSUFFICIENT_BALANCE = "Insufficient wallet balance";
        public static final String COUPON_ALREADY_USED = "Coupon already used by this user";
        public static final String COUPON_EXPIRED = "Coupon has expired";
        public static final String COUPON_NOT_ACTIVE = "Coupon is not active";
        
        private ERROR_MESSAGES() {}
    }
    
    // ==================== Success Messages ====================
    public static final class SUCCESS_MESSAGES {
        public static final String USER_REGISTERED = "User registered successfully";
        public static final String LOGIN_SUCCESS = "Login successful";
        public static final String PROFILE_UPDATED = "Profile updated successfully";
        public static final String PASSWORD_CHANGED = "Password changed successfully";
        public static final String ORDER_PLACED = "Order placed successfully";
        public static final String ORDER_UPDATED = "Order updated successfully";
        public static final String PAYMENT_PROCESSED = "Payment processed successfully";
        public static final String RESTAURANT_CREATED = "Restaurant created successfully";
        public static final String RESTAURANT_UPDATED = "Restaurant updated successfully";
        public static final String ITEM_ADDED = "Item added successfully";
        public static final String ITEM_UPDATED = "Item updated successfully";
        public static final String ITEM_DELETED = "Item deleted successfully";
        public static final String COUPON_APPLIED = "Coupon applied successfully";
        public static final String FAVORITE_ADDED = "Added to favorites";
        public static final String FAVORITE_REMOVED = "Removed from favorites";
        public static final String RATING_SUBMITTED = "Rating submitted successfully";
        
        private SUCCESS_MESSAGES() {}
    }
    
    // ==================== HTTP Status Codes ====================
    public static final class HTTP_STATUS {
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int NO_CONTENT = 204;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int METHOD_NOT_ALLOWED = 405;
        public static final int CONFLICT = 409;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;
        
        private HTTP_STATUS() {}
    }
    
    // ==================== Content Types ====================
    public static final class CONTENT_TYPE {
        public static final String APPLICATION_JSON = "application/json";
        public static final String TEXT_PLAIN = "text/plain";
        public static final String TEXT_HTML = "text/html";
        
        private CONTENT_TYPE() {}
    }
    
    // ==================== Date/Time Formats ====================
    public static final class DATE_FORMAT {
        public static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";
        public static final String SIMPLE_DATE = "yyyy-MM-dd";
        public static final String TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
        public static final String PERSIAN_DATE = "yyyy/MM/dd";
        
        private DATE_FORMAT() {}
    }
} 