package com.myapp.common.constants;

/**
 * ثابت‌های کل برنامه برای جلوگیری از استفاده مستقیم از مقادیر
 * این کلاس حاوی تمام مقادیر ثابت مورد استفاده در پروژه است
 * از الگوی Utility Class استفاده می‌کند
 */
public final class ApplicationConstants {
    
    // سازنده خصوصی برای جلوگیری از نمونه‌سازی
    private ApplicationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ==================== ثابت‌های API ====================
    /**
     * مسیرهای API endpoint ها
     * تمام مسیرهای دسترسی به API در این کلاس تعریف شده‌اند
     */
    public static final class API {
        public static final String BASE_PATH = "/api";               // مسیر پایه API
        public static final String AUTH_PATH = "/api/auth";          // احراز هویت
        public static final String RESTAURANTS_PATH = "/api/restaurants"; // رستوران‌ها
        public static final String ORDERS_PATH = "/api/orders";      // سفارشات
        public static final String ITEMS_PATH = "/api/items";        // آیتم‌های غذایی
        public static final String PAYMENTS_PATH = "/api/payments";  // پرداخت‌ها
        public static final String DELIVERY_PATH = "/api/delivery";  // تحویل
        public static final String COUPONS_PATH = "/api/coupons";    // کوپن‌ها
        public static final String FAVORITES_PATH = "/api/favorites"; // علاقه‌مندی‌ها
        public static final String RATINGS_PATH = "/api/ratings";    // امتیازدهی
        public static final String ADMIN_PATH = "/api/admin";        // مدیریت
        public static final String VENDORS_PATH = "/api/vendors";    // فروشندگان
        public static final String NOTIFICATIONS_PATH = "/api/notifications"; // اعلان‌ها
        
        private API() {} // جلوگیری از نمونه‌سازی
    }
    
    // ==================== ثابت‌های دیتابیس ====================
    /**
     * تنظیمات و ثابت‌های مربوط به دیتابیس SQLite
     * شامل URL اتصال و تنظیمات بهینه‌سازی
     */
    public static final class DATABASE {
        // URL اتصال به SQLite با تنظیمات بهینه‌سازی
        public static final String SQLITE_URL = "jdbc:sqlite:food_ordering.db?journal_mode=WAL&synchronous=NORMAL&busy_timeout=30000";
        public static final int CONNECTION_POOL_SIZE = 5;     // اندازه connection pool
        public static final int JDBC_BATCH_SIZE = 20;         // اندازه batch برای عملیات bulk
        public static final int MAX_RETRY_ATTEMPTS = 3;       // حداکثر تلاش مجدد
        public static final int RETRY_DELAY_MS = 50;          // تأخیر بین تلاش‌ها (میلی‌ثانیه)
        public static final int BUSY_TIMEOUT_MS = 30000;      // timeout برای دیتابیس busy (30 ثانیه)
        
        private DATABASE() {} // جلوگیری از نمونه‌سازی
    }
    
    // ==================== ثابت‌های منطق کسب‌وکار ====================
    /**
     * قوانین و محدودیت‌های کسب‌وکار
     * شامل محدودیت‌های مالی، وضعیت‌ها و نقش‌های کاربری
     */
    public static final class BUSINESS {
        public static final double MAX_WALLET_CHARGE = 10000.0;      // حداکثر شارژ کیف پول
        public static final double MIN_ORDER_AMOUNT = 10.0;          // حداقل مبلغ سفارش
        public static final double DELIVERY_FEE = 15.0;              // هزینه تحویل
        public static final int MAX_COUPON_USAGE_PER_USER = 1;       // حداکثر استفاده از کوپن
        public static final int PASSWORD_MIN_LENGTH = 8;             // حداقل طول رمز عبور
        public static final int PHONE_NUMBER_LENGTH = 11;            // طول شماره تلفن
        public static final String PHONE_PREFIX = "09";              // پیشوند شماره ایران
        
        // چرخه وضعیت سفارش
        public static final String ORDER_STATUS_PENDING = "PENDING";       // در انتظار
        public static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";   // تأیید شده
        public static final String ORDER_STATUS_PREPARING = "PREPARING";   // در حال آماده‌سازی
        public static final String ORDER_STATUS_READY = "READY";           // آماده
        public static final String ORDER_STATUS_PICKED_UP = "PICKED_UP";   // تحویل گرفته شده
        public static final String ORDER_STATUS_DELIVERED = "DELIVERED";   // تحویل داده شده
        public static final String ORDER_STATUS_CANCELLED = "CANCELLED";   // لغو شده
        
        // نقش‌های کاربری
        public static final String ROLE_BUYER = "BUYER";       // خریدار
        public static final String ROLE_SELLER = "SELLER";     // فروشنده
        public static final String ROLE_COURIER = "COURIER";   // پیک
        public static final String ROLE_ADMIN = "ADMIN";       // مدیر
        
        // روش‌های پرداخت
        public static final String PAYMENT_CARD = "CARD";      // کارت
        public static final String PAYMENT_WALLET = "WALLET";  // کیف پول
        public static final String PAYMENT_COD = "COD";        // پرداخت در محل
        
        private BUSINESS() {} // جلوگیری از نمونه‌سازی
    }
    
    // ==================== ثابت‌های امنیتی ====================
    /**
     * تنظیمات امنیتی JWT و رمزنگاری
     * شامل کلیدها، مدت اعتبار و سایر تنظیمات امنیتی
     */
    public static final class SECURITY {
        public static final String JWT_SECRET_KEY = "mySecretKey12345";        // کلید مخفی JWT
        public static final long ACCESS_TOKEN_VALIDITY = 3600000;              // 1 ساعت (میلی‌ثانیه)
        public static final long REFRESH_TOKEN_VALIDITY = 604800000;           // 7 روز (میلی‌ثانیه)
        public static final String AUTHORIZATION_HEADER = "Authorization";     // نام header احراز هویت
        public static final String BEARER_PREFIX = "Bearer ";                  // پیشوند Bearer Token
        public static final int BCRYPT_ROUNDS = 12;                            // دور رمزنگاری BCrypt
        
        private SECURITY() {} // جلوگیری از نمونه‌سازی
    }
    
    // ==================== ثابت‌های اعتبارسنجی ====================
    /**
     * محدودیت‌های اعتبارسنجی داده‌ها
     * شامل حداکثر و حداقل طول فیلدها، محدوده مقادیر و غیره
     */
    public static final class VALIDATION {
        public static final int MAX_NAME_LENGTH = 100;         // حداکثر طول نام
        public static final int MAX_DESCRIPTION_LENGTH = 500;  // حداکثر طول توضیحات
        public static final int MAX_ADDRESS_LENGTH = 255;      // حداکثر طول آدرس
        public static final int MAX_EMAIL_LENGTH = 100;        // حداکثر طول ایمیل
        public static final int MIN_RATING = 1;                // حداقل امتیاز
        public static final int MAX_RATING = 5;                // حداکثر امتیاز
        public static final double MIN_PRICE = 0.01;           // حداقل قیمت
        public static final double MAX_PRICE = 10000.0;        // حداکثر قیمت
        public static final int MAX_QUANTITY = 1000;           // حداکثر تعداد
        public static final int MIN_QUANTITY = 1;              // حداقل تعداد
        
        private VALIDATION() {} // جلوگیری از نمونه‌سازی
    }
    
    // ==================== پیام‌های خطا ====================
    /**
     * پیام‌های خطای استاندارد برنامه
     * برای یکنواختی پیام‌ها در سراسر برنامه
     */
    public static final class ERROR_MESSAGES {
        public static final String INVALID_CREDENTIALS = "Invalid phone or password";           // اطلاعات ورود نامعتبر
        public static final String USER_NOT_FOUND = "User not found";                          // کاربر یافت نشد
        public static final String RESTAURANT_NOT_FOUND = "Restaurant not found";              // رستوران یافت نشد
        public static final String ORDER_NOT_FOUND = "Order not found";                        // سفارش یافت نشد
        public static final String ITEM_NOT_FOUND = "Item not found";                          // آیتم یافت نشد
        public static final String INSUFFICIENT_STOCK = "Insufficient stock";                  // موجودی ناکافی
        public static final String INVALID_ORDER_STATUS = "Invalid order status";              // وضعیت سفارش نامعتبر
        public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";                // دسترسی غیرمجاز
        public static final String DUPLICATE_PHONE = "Phone number already exists";            // شماره تلفن تکراری
        public static final String INVALID_PHONE_FORMAT = "Invalid phone number format";       // فرمت شماره نامعتبر
        public static final String WEAK_PASSWORD = "Password does not meet security requirements"; // رمز عبور ضعیف
        public static final String INVALID_EMAIL_FORMAT = "Invalid email format";              // فرمت ایمیل نامعتبر
        public static final String REQUIRED_FIELD_MISSING = "Required field is missing";       // فیلد اجباری خالی
        public static final String INVALID_NUMBER_FORMAT = "Invalid number format";            // فرمت عدد نامعتبر
        public static final String WALLET_INSUFFICIENT_BALANCE = "Insufficient wallet balance"; // موجودی کیف پول ناکافی
        public static final String COUPON_ALREADY_USED = "Coupon already used by this user";   // کوپن قبلاً استفاده شده
        public static final String COUPON_EXPIRED = "Coupon has expired";                      // کوپن منقضی شده
        public static final String COUPON_NOT_ACTIVE = "Coupon is not active";                 // کوپن غیرفعال
        
        private ERROR_MESSAGES() {} // جلوگیری از نمونه‌سازی
    }
    
    // ==================== پیام‌های موفقیت ====================
    /**
     * پیام‌های موفقیت‌آمیز عملیات
     * برای نمایش به کاربر هنگام انجام موفق عملیات
     */
    public static final class SUCCESS_MESSAGES {
        public static final String USER_REGISTERED = "User registered successfully";           // ثبت نام موفق
        public static final String LOGIN_SUCCESS = "Login successful";                         // ورود موفق
        public static final String PROFILE_UPDATED = "Profile updated successfully";           // به‌روزرسانی پروفایل موفق
        public static final String PASSWORD_CHANGED = "Password changed successfully";         // تغییر رمز عبور موفق
        public static final String ORDER_PLACED = "Order placed successfully";                 // ثبت سفارش موفق
        public static final String ORDER_UPDATED = "Order updated successfully";               // به‌روزرسانی سفارش موفق
        public static final String PAYMENT_PROCESSED = "Payment processed successfully";       // پردازش پرداخت موفق
        public static final String RESTAURANT_CREATED = "Restaurant created successfully";     // ایجاد رستوران موفق
        public static final String RESTAURANT_UPDATED = "Restaurant updated successfully";     // به‌روزرسانی رستوران موفق
        public static final String ITEM_ADDED = "Item added successfully";                     // افزودن آیتم موفق
        public static final String ITEM_UPDATED = "Item updated successfully";                 // به‌روزرسانی آیتم موفق
        public static final String ITEM_DELETED = "Item deleted successfully";                 // حذف آیتم موفق
        public static final String COUPON_APPLIED = "Coupon applied successfully";             // اعمال کوپن موفق
        public static final String FAVORITE_ADDED = "Added to favorites";                      // افزودن به علاقه‌مندی‌ها
        public static final String FAVORITE_REMOVED = "Removed from favorites";                // حذف از علاقه‌مندی‌ها
        public static final String RATING_SUBMITTED = "Rating submitted successfully";         // ثبت امتیاز موفق
        
        private SUCCESS_MESSAGES() {} // جلوگیری از نمونه‌سازی
    }
    
    // ==================== کدهای وضعیت HTTP ====================
    /**
     * کدهای وضعیت HTTP استاندارد
     * برای پاسخ‌های API
     */
    public static final class HTTP_STATUS {
        public static final int OK = 200;                        // موفق
        public static final int CREATED = 201;                   // ایجاد شده
        public static final int NO_CONTENT = 204;                // بدون محتوا
        public static final int BAD_REQUEST = 400;               // درخواست بد
        public static final int UNAUTHORIZED = 401;              // غیرمجاز
        public static final int FORBIDDEN = 403;                 // ممنوع
        public static final int NOT_FOUND = 404;                 // یافت نشد
        public static final int METHOD_NOT_ALLOWED = 405;        // متد مجاز نیست
        public static final int CONFLICT = 409;                  // تعارض
        public static final int INTERNAL_SERVER_ERROR = 500;     // خطای سرور
        public static final int SERVICE_UNAVAILABLE = 503;       // سرویس در دسترس نیست
        
        private HTTP_STATUS() {} // جلوگیری از نمونه‌سازی
    }
    
    // ==================== انواع محتوا ====================
    /**
     * انواع محتوای HTTP
     * برای تنظیم Content-Type در پاسخ‌ها
     */
    public static final class CONTENT_TYPE {
        public static final String APPLICATION_JSON = "application/json";  // JSON
        public static final String TEXT_PLAIN = "text/plain";              // متن ساده
        public static final String TEXT_HTML = "text/html";                // HTML
        
        private CONTENT_TYPE() {} // جلوگیری از نمونه‌سازی
    }
    
    // ==================== فرمت‌های تاریخ/زمان ====================
    /**
     * فرمت‌های تاریخ و زمان مختلف
     * برای نمایش و ذخیره تاریخ‌ها
     */
    public static final class DATE_FORMAT {
        public static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss"; // ISO استاندارد
        public static final String SIMPLE_DATE = "yyyy-MM-dd";              // تاریخ ساده
        public static final String TIMESTAMP = "yyyy-MM-dd HH:mm:ss";       // timestamp
        public static final String PERSIAN_DATE = "yyyy/MM/dd";             // تاریخ فارسی
        
        private DATE_FORMAT() {} // جلوگیری از نمونه‌سازی
    }
} 