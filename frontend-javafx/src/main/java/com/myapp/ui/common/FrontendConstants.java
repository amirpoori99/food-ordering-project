package com.myapp.ui.common;

/**
 * کلاس ثابت‌های اختصاصی فرانت‌اند برای برنامه JavaFX
 * 
 * این کلاس شامل تمام ثابت‌ها، پیکربندی‌ها و تنظیمات مورد نیاز
 * لایه رابط کاربری است:
 * 
 * - تنظیمات API و HTTP
 * - ثابت‌های رابط کاربری (UI)
 * - پیام‌های فارسی سیستم
 * - قوانین اعتبارسنجی
 * - تنظیمات فایل‌ها و فولدرها
 * - فرمت‌های نمایش و واحدها
 * - پیکربندی تست و عملکرد
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 * 
 * Design Patterns Used:
 * - Utility Class Pattern: کلاس utility با متدهای static
 * - Nested Classes Pattern: گروه‌بندی منطقی ثابت‌ها
 * - Constants Pattern: تمرکز تمام ثابت‌ها در یک مکان
 * 
 * Best Practices:
 * - تمام nested class ها final و private constructor دارند
 * - نام‌گذاری معنادار و واضح
 * - گروه‌بندی منطقی ثابت‌ها
 * - استفاده از فارسی برای متن‌های کاربرپسند
 */
public final class FrontendConstants {
    
    /**
     * سازنده private برای جلوگیری از ایجاد instance
     * این کلاس فقط برای نگهداری ثابت‌های static طراحی شده است
     */
    private FrontendConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ==================== API Configuration ====================
    
    /**
     * کلاس ثابت‌های پیکربندی API
     * شامل URL ها و endpoint های مختلف بک‌اند
     */
    public static final class API {
        /** URL پایه سرور بک‌اند */
        public static final String BASE_URL = "http://localhost:8081/api";
        
        /** endpoint ورود کاربر */
        public static final String AUTH_LOGIN = "/auth/login";
        
        /** endpoint ثبت نام کاربر */
        public static final String AUTH_REGISTER = "/auth/register";
        
        /** endpoint خروج کاربر */
        public static final String AUTH_LOGOUT = "/auth/logout";
        
        /** endpoint نوسازی token */
        public static final String AUTH_REFRESH = "/auth/refresh";
        
        /** endpoint مدیریت رستوران‌ها */
        public static final String RESTAURANTS = "/restaurants";
        
        /** endpoint مدیریت سفارشات */
        public static final String ORDERS = "/orders";
        
        /** endpoint مدیریت آیتم‌های غذایی */
        public static final String ITEMS = "/items";
        
        /** endpoint مدیریت پرداخت‌ها */
        public static final String PAYMENTS = "/payments";
        
        /** endpoint پروفایل کاربر */
        public static final String PROFILE = "/auth/profile";
        
        /**
         * سازنده private برای جلوگیری از instantiation
         */
        private API() {}
    }
    
    // ==================== HTTP Configuration ====================
    
    /**
     * کلاس ثابت‌های پیکربندی HTTP
     * شامل timeout ها، header ها و تنظیمات درخواست
     */
    public static final class HTTP {
        /** زمان انتظار برای برقراری اتصال (ثانیه) */
        public static final int CONNECT_TIMEOUT_SECONDS = 30;
        
        /** زمان انتظار برای خواندن داده (ثانیه) */
        public static final int READ_TIMEOUT_SECONDS = 60;
        
        /** زمان انتظار برای نوشتن داده (ثانیه) */
        public static final int WRITE_TIMEOUT_SECONDS = 60;
        
        /** نوع محتوای JSON با charset UTF-8 */
        public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
        
        /** نام header احراز هویت */
        public static final String AUTHORIZATION_HEADER = "Authorization";
        
        /** پیشوند Bearer برای JWT token */
        public static final String BEARER_PREFIX = "Bearer ";
        
        /**
         * سازنده private برای جلوگیری از instantiation
         */
        private HTTP() {}
    }
    
    // ==================== UI Constants ====================
    
    /**
     * کلاس ثابت‌های رابط کاربری
     * شامل ابعاد پنجره، رنگ‌ها، استایل‌ها و انیمیشن‌ها
     */
    public static final class UI {
        // ==================== ابعاد پنجره ====================
        
        /** عرض پیش‌فرض پنجره (پیکسل) */
        public static final double DEFAULT_WINDOW_WIDTH = 1200;
        
        /** ارتفاع پیش‌فرض پنجره (پیکسل) */
        public static final double DEFAULT_WINDOW_HEIGHT = 800;
        
        /** حداقل عرض پنجره (پیکسل) */
        public static final double MIN_WINDOW_WIDTH = 800;
        
        /** حداقل ارتفاع پنجره (پیکسل) */
        public static final double MIN_WINDOW_HEIGHT = 600;
        
        // ==================== پالت رنگ‌ها ====================
        
        /** رنگ اصلی (آبی) */
        public static final String PRIMARY_COLOR = "#007bff";
        
        /** رنگ موفقیت (سبز) */
        public static final String SUCCESS_COLOR = "#28a745";
        
        /** رنگ هشدار (زرد) */
        public static final String WARNING_COLOR = "#ffc107";
        
        /** رنگ خطر (قرمز) */
        public static final String DANGER_COLOR = "#dc3545";
        
        /** رنگ اطلاعات (آبی فیروزه‌ای) */
        public static final String INFO_COLOR = "#17a2b8";
        
        /** رنگ ثانویه (خاکستری) */
        public static final String SECONDARY_COLOR = "#6c757d";
        
        // ==================== استایل‌های CSS ====================
        
        /** استایل CSS برای پیام‌های موفقیت */
        public static final String SUCCESS_STYLE = "-fx-background-color: #d4edda; -fx-text-fill: #155724;";
        
        /** استایل CSS برای پیام‌های خطا */
        public static final String ERROR_STYLE = "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;";
        
        /** استایل CSS برای پیام‌های هشدار */
        public static final String WARNING_STYLE = "-fx-background-color: #fff3cd; -fx-text-fill: #856404;";
        
        /** استایل CSS برای پیام‌های اطلاعاتی */
        public static final String INFO_STYLE = "-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460;";
        
        // ==================== مدت زمان انیمیشن‌ها (میلی‌ثانیه) ====================
        
        /** مدت زمان انیمیشن fade (محو/ظهور) */
        public static final int FADE_DURATION = 300;
        
        /** مدت زمان انیمیشن slide (کشیدن) */
        public static final int SLIDE_DURATION = 500;
        
        /** مدت زمان انیمیشن loading (بارگذاری) */
        public static final int LOADING_ANIMATION_DURATION = 1000;
        
        /**
         * سازنده private برای جلوگیری از instantiation
         */
        private UI() {}
    }
    
    // ==================== Messages ====================
    
    /**
     * کلاس پیام‌های فارسی سیستم
     * شامل تمام متن‌های قابل نمایش به کاربر
     */
    public static final class MESSAGES {
        // ==================== پیام‌های موفقیت ====================
        
        /** پیام ورود موفقیت‌آمیز */
        public static final String LOGIN_SUCCESS = "ورود موفقیت‌آمیز بود";
        
        /** پیام خطا در ورود */
        public static final String LOGIN_FAILED = "خطا در ورود";
        
        /** پیام ثبت‌نام موفقیت‌آمیز */
        public static final String REGISTER_SUCCESS = "ثبت‌نام موفقیت‌آمیز بود";
        
        /** پیام خطا در ثبت‌نام */
        public static final String REGISTER_FAILED = "خطا در ثبت‌نام";
        
        /** پیام خروج موفقیت‌آمیز */
        public static final String LOGOUT_SUCCESS = "خروج موفقیت‌آمیز بود";
        
        /** پیام بروزرسانی پروفایل */
        public static final String PROFILE_UPDATED = "پروفایل بروزرسانی شد";
        
        /** پیام ثبت سفارش */
        public static final String ORDER_PLACED = "سفارش ثبت شد";
        
        /** پیام پرداخت موفقیت‌آمیز */
        public static final String PAYMENT_SUCCESS = "پرداخت موفقیت‌آمیز بود";
        
        /** پیام خطا در پرداخت */
        public static final String PAYMENT_FAILED = "خطا در پرداخت";
        
        // ==================== پیام‌های خطا ====================
        
        /** پیام خطای شبکه */
        public static final String NETWORK_ERROR = "خطا در ارتباط با سرور";
        
        /** پیام اطلاعات ورود نامعتبر */
        public static final String INVALID_CREDENTIALS = "شماره تلفن یا رمز عبور اشتباه است";
        
        /** پیام فیلد الزامی */
        public static final String REQUIRED_FIELD = "این فیلد الزامی است";
        
        /** پیام فرمت ایمیل نامعتبر */
        public static final String INVALID_EMAIL = "فرمت ایمیل صحیح نیست";
        
        /** پیام فرمت شماره تلفن نامعتبر */
        public static final String INVALID_PHONE = "فرمت شماره تلفن صحیح نیست";
        
        /** پیام رمز عبور ضعیف */
        public static final String WEAK_PASSWORD = "رمز عبور باید حداقل ۸ کاراکتر باشد";
        
        /** پیام عدم تطابق رمز عبور */
        public static final String PASSWORD_MISMATCH = "رمز عبور و تکرار آن یکسان نیستند";
        
        // ==================== پیام‌های بارگذاری ====================
        
        /** پیام بارگذاری عمومی */
        public static final String LOADING = "در حال بارگذاری...";
        
        /** پیام پردازش */
        public static final String PROCESSING = "در حال پردازش...";
        
        /** پیام اتصال */
        public static final String CONNECTING = "در حال اتصال...";
        
        /** پیام ذخیره */
        public static final String SAVING = "در حال ذخیره...";
        
        /**
         * سازنده private برای جلوگیری از instantiation
         */
        private MESSAGES() {}
    }
    
    // ==================== Validation ====================
    
    /**
     * کلاس ثابت‌های اعتبارسنجی
     * شامل قوانین و الگوهای مورد نیاز برای validation
     */
    public static final class VALIDATION {
        // ==================== محدودیت‌های طول ====================
        
        /** حداقل طول رمز عبور */
        public static final int MIN_PASSWORD_LENGTH = 8;
        
        /** حداکثر طول رمز عبور */
        public static final int MAX_PASSWORD_LENGTH = 128;
        
        /** طول شماره تلفن (11 رقم) */
        public static final int PHONE_LENGTH = 11;
        
        /** پیشوند شماره تلفن همراه */
        public static final String PHONE_PREFIX = "09";
        
        /** حداکثر طول نام */
        public static final int MAX_NAME_LENGTH = 100;
        
        /** حداکثر طول ایمیل */
        public static final int MAX_EMAIL_LENGTH = 100;
        
        /** حداکثر طول آدرس */
        public static final int MAX_ADDRESS_LENGTH = 500;
        
        // ==================== الگوهای Regex ====================
        
        /** الگوی regex برای اعتبارسنجی ایمیل */
        public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        
        /** الگوی regex برای اعتبارسنجی شماره تلفن همراه */
        public static final String PHONE_PATTERN = "^09\\d{9}$";
        
        /** الگوی regex برای نام‌های فارسی */
        public static final String PERSIAN_NAME_PATTERN = "^[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF\\s\\w.-]+$";
        
        /**
         * سازنده private برای جلوگیری از instantiation
         */
        private VALIDATION() {}
    }
    
    // ==================== File System ====================
    
    /**
     * کلاس ثابت‌های سیستم فایل
     * شامل مسیرهای فولدرها و فایل‌های برنامه
     */
    public static final class FILES {
        /** فولدر اصلی داده‌های کاربر در home directory */
        public static final String USER_DATA_DIR = System.getProperty("user.home") + "/.food-ordering";
        
        /** نام فایل تنظیمات */
        public static final String SETTINGS_FILE = "settings.json";
        
        /** نام فولدر cache */
        public static final String CACHE_DIR = "cache";
        
        /** نام فولدر لاگ‌ها */
        public static final String LOGS_DIR = "logs";
        
        /**
         * سازنده private برای جلوگیری از instantiation
         */
        private FILES() {}
    }
    
    // ==================== Currency & Formatting ====================
    
    /**
     * کلاس ثابت‌های فرمت‌بندی و نمایش
     * شامل واحدها، فرمت‌های تاریخ/زمان و نحوه نمایش اعداد
     */
    public static final class FORMAT {
        /** نماد واحد پولی */
        public static final String CURRENCY_SYMBOL = "تومان";
        
        /** فرمت نمایش اعداد بدون اعشار (با جداکننده هزارگان) */
        public static final String DECIMAL_FORMAT = "#,##0";
        
        /** فرمت نمایش اعداد با اعشار */
        public static final String DECIMAL_FORMAT_WITH_FRACTION = "#,##0.00";
        
        /** فرمت نمایش تاریخ (سال/ماه/روز) */
        public static final String DATE_FORMAT = "yyyy/MM/dd";
        
        /** فرمت نمایش زمان (ساعت:دقیقه) */
        public static final String TIME_FORMAT = "HH:mm";
        
        /** فرمت نمایش تاریخ و زمان */
        public static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm";
        
        /**
         * سازنده private برای جلوگیری از instantiation
         */
        private FORMAT() {}
    }
    
    // ==================== Test Configuration ====================
    
    /**
     * کلاس ثابت‌های پیکربندی تست
     * شامل timeout ها و داده‌های تستی
     */
    public static final class TEST {
        // ==================== زمان‌های انتظار ====================
        
        /** timeout پیش‌فرض تست‌ها (ثانیه) */
        public static final int DEFAULT_TIMEOUT_SECONDS = 30;
        
        /** timeout طولانی برای تست‌های پیچیده (ثانیه) */
        public static final int LONG_TIMEOUT_SECONDS = 60;
        
        /** زمان انتظار برای عناصر UI (میلی‌ثانیه) */
        public static final int UI_WAIT_TIME_MS = 100;
        
        /** timeout شبکه برای تست‌ها (میلی‌ثانیه) */
        public static final int NETWORK_TIMEOUT_MS = 5000;
        
        // ==================== داده‌های تستی ====================
        
        /** شماره تلفن تستی */
        public static final String TEST_PHONE = "09123456789";
        
        /** ایمیل تستی */
        public static final String TEST_EMAIL = "test@example.com";
        
        /** رمز عبور تستی */
        public static final String TEST_PASSWORD = "TestPass123@";
        
        /** نام تستی */
        public static final String TEST_NAME = "تست کاربر";
        
        /**
         * سازنده private برای جلوگیری از instantiation
         */
        private TEST() {}
    }
    
    // ==================== Performance ====================
    
    /**
     * کلاس ثابت‌های عملکرد و بهینه‌سازی
     * شامل تنظیمات cache، retry و محدودیت‌های درخواست
     */
    public static final class PERFORMANCE {
        /** حداکثر اندازه cache */
        public static final int MAX_CACHE_SIZE = 100;
        
        /** مدت انقضای cache (دقیقه) */
        public static final int CACHE_EXPIRY_MINUTES = 30;
        
        /** حداکثر تعداد درخواست‌های همزمان */
        public static final int MAX_CONCURRENT_REQUESTS = 10;
        
        /** تعداد تلاش مجدد درخواست در صورت شکست */
        public static final int REQUEST_RETRY_COUNT = 3;
        
        /** تأخیر بین تلاش‌های مجدد (میلی‌ثانیه) */
        public static final int REQUEST_RETRY_DELAY_MS = 1000;
        
        /**
         * سازنده private برای جلوگیری از instantiation
         */
        private PERFORMANCE() {}
    }
} 