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
import com.myapp.common.utils.PasswordUtil;
import com.myapp.common.models.User;
import com.myapp.common.constants.ApplicationConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * کلاس اصلی سرور پروژه سیستم سفارش غذا
 * این کلاس مسئول راه‌اندازی و مدیریت کل سرور backend است
 * شامل تمام endpoint های REST API و مدیریت درخواست‌ها
 */
public class ServerApp {
    
    // تعریف متغیرهای static برای سرویس‌ها و کنترلرهای مختلف
    private static AuthService authService;                    // سرویس احراز هویت
    private static AdminController adminController;            // کنترلر پنل مدیریت
    private static RestaurantController restaurantController;  // کنترلر مدیریت رستوران‌ها
    private static OrderController orderController;            // کنترلر مدیریت سفارشات
    private static PaymentController paymentController;        // کنترلر سیستم پرداخت
    private static WalletController walletController;          // کنترلر کیف پول
    private static TransactionController transactionController; // کنترلر تراکنش‌ها
    private static DeliveryController deliveryController;      // کنترلر سیستم تحویل
    private static ItemController itemController;              // کنترلر مدیریت آیتم‌ها
    private static MenuController menuController;              // کنترلر مدیریت منو
    private static VendorController vendorController;          // کنترلر سیستم فروشندگان
    private static FavoritesController favoritesController;    // کنترلر علاقه‌مندی‌ها
    private static NotificationController notificationController; // کنترلر اعلان‌ها
    
    /**
     * متد اصلی main که نقطه شروع برنامه است
     * مسئول راه‌اندازی تمام سرویس‌ها، کنترلرها و سرور HTTP
     */
    public static void main(String[] args) throws IOException {
        // نمایش پیام شروع سرور
        System.out.println("🚀 Starting Food Ordering Backend Server...");
        
        // مرحله 1: راه‌اندازی Repository ها (لایه دسترسی به داده)
        AuthRepository authRepo = new AuthRepository();                   // Repository احراز هویت
        authService = new AuthService(authRepo);                          // سرویس احراز هویت
        
        RestaurantRepository restaurantRepo = new RestaurantRepository(); // Repository رستوران‌ها
        OrderRepository orderRepo = new OrderRepository();               // Repository سفارشات
        PaymentRepository paymentRepo = new PaymentRepository();         // Repository پرداخت‌ها
        DeliveryRepository deliveryRepo = new DeliveryRepository();       // Repository تحویل‌ها
        FavoritesRepository favoritesRepo = new FavoritesRepository();   // Repository علاقه‌مندی‌ها
        NotificationRepository notificationRepo = new NotificationRepository(); // Repository اعلان‌ها
        
        // مرحله 2: راه‌اندازی سرویس‌های مدیریت (Admin services)
        AdminRepository adminRepo = new AdminRepository();
        // ایجاد سرویس مدیریت با تمام Repository های لازم
        AdminService adminService = new AdminService(adminRepo, authRepo, restaurantRepo, orderRepo, paymentRepo, deliveryRepo);
        adminController = new AdminController(adminService);             // کنترلر پنل مدیریت
        
        // مرحله 3: راه‌اندازی سرویس علاقه‌مندی‌ها
        FavoritesService favoritesService = new FavoritesService(favoritesRepo, authRepo, restaurantRepo);
        
        // مرحله 4: راه‌اندازی سرویس اعلان‌ها
        NotificationService notificationService = new NotificationService(notificationRepo, authRepo);
        
        // مرحله 5: راه‌اندازی سرویس کیف پول برای کنترلر تراکنش‌ها
        WalletService walletService = new WalletService(paymentRepo, authRepo);
        
        // مرحله 6: راه‌اندازی سایر کنترلرها
        restaurantController = new RestaurantController();               // کنترلر رستوران‌ها
        orderController = new OrderController();                        // کنترلر سفارشات
        paymentController = new PaymentController();                    // کنترلر پرداخت‌ها
        walletController = new WalletController();                      // کنترلر کیف پول
        transactionController = new TransactionController(walletService, paymentRepo); // کنترلر تراکنش‌ها
        deliveryController = new DeliveryController();                  // کنترلر تحویل‌ها
        itemController = new ItemController();                          // کنترلر آیتم‌ها
        menuController = new MenuController();                          // کنترلر منو
        vendorController = new VendorController();                      // کنترلر فروشندگان
        favoritesController = new FavoritesController(favoritesService); // کنترلر علاقه‌مندی‌ها
        notificationController = new NotificationController(notificationService); // کنترلر اعلان‌ها
        
        // مرحله 7: تست اتصال به پایگاه داده
        System.out.println("Testing Hibernate connection...");
        try {
            // تلاش برای اتصال به پایگاه داده از طریق Hibernate
            DatabaseUtil.getSessionFactory();
            System.out.println("✅ Database connection successful!");
        } catch (Exception e) {
            // در صورت خطا در اتصال، نمایش پیام خطا و خروج از برنامه
            System.err.println("❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
            return; // خروج از برنامه در صورت عدم اتصال به دیتابیس
        }
        
        // مرحله 8: ایجاد سرور HTTP روی پورت پیکربندی شده
        int serverPort = Integer.parseInt(System.getProperty("server.port", "8081"));
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        
        // مرحله 9: اضافه کردن endpoint های اصلی (Authentication & Health)
        server.createContext("/api/test", new TestHandler());              // endpoint تست
        server.createContext("/api/auth/register", new RegisterHandler()); // ثبت نام کاربران
        server.createContext("/api/auth/login", new LoginHandler());       // ورود کاربران
        server.createContext("/api/auth/refresh", new RefreshTokenHandler()); // تجدید token
        server.createContext("/api/auth/validate", new ValidateTokenHandler()); // اعتبارسنجی token
        server.createContext("/api/auth/logout", new LogoutHandler());     // خروج کاربران
        server.createContext("/health", new HealthHandler());              // بررسی سلامت سرور
        
        // مرحله 10: اضافه کردن endpoint های کنترلرها (Business Logic)
        server.createContext("/api/admin/", adminController);              // endpoint های پنل مدیریت
        server.createContext("/api/restaurants/", restaurantController);   // endpoint های رستوران‌ها
        server.createContext("/api/orders/", orderController);             // endpoint های سفارشات
        server.createContext("/api/payments/", paymentController);         // endpoint های پرداخت
        server.createContext("/api/wallet/", walletController);            // endpoint های کیف پول
        server.createContext("/api/transactions/", transactionController); // endpoint های تراکنش‌ها
        server.createContext("/api/deliveries/", deliveryController);      // endpoint های تحویل
        server.createContext("/api/items/", itemController);               // endpoint های آیتم‌ها
        server.createContext("/api/menu/", menuController);                // endpoint های منو
        server.createContext("/api/vendors/", vendorController);           // endpoint های فروشندگان
        server.createContext("/api/favorites/", favoritesController);      // endpoint های علاقه‌مندی‌ها
        server.createContext("/api/notifications/", notificationController); // endpoint های اعلان‌ها
        server.createContext("/api/notification/", notificationController);  // endpoint جایگزین اعلان‌ها
        
        // مرحله 11: تنظیم Thread Pool برای پردازش همزمان درخواست‌ها
        server.setExecutor(Executors.newFixedThreadPool(10)); // حداکثر 10 thread همزمان
        
        // مرحله 12: شروع سرور و نمایش اطلاعات
        server.start();
        System.out.println("🚀 Server started on http://localhost:" + serverPort);
        System.out.println("📋 Available endpoints:");
        
        // نمایش لیست تمام endpoint های موجود برای راهنمایی توسعه‌دهندگان
        System.out.println("   GET  /health - Health check");
        System.out.println("   GET  /api/test - Simple test");
        System.out.println("   POST /api/auth/register - User registration");
        System.out.println("   POST /api/auth/login - User login (with JWT tokens)");
        System.out.println("   POST /api/auth/refresh - Refresh access token");
        System.out.println("   GET  /api/auth/validate - Validate access token");
        System.out.println("   POST /api/auth/logout - User logout");
        
        // نمایش endpoint های پنل مدیریت (18+ endpoint)
        System.out.println("   🔧 Admin Dashboard (18+ endpoints):");
        System.out.println("   GET  /api/admin/dashboard - Admin statistics");
        System.out.println("   GET  /api/admin/users - User management");
        System.out.println("   GET  /api/admin/restaurants - Restaurant management");
        System.out.println("   GET  /api/admin/orders - Order management");
        System.out.println("   GET  /api/admin/transactions - Transaction monitoring");
        System.out.println("   GET  /api/admin/deliveries - Delivery tracking");
        
        // نمایش endpoint های مدیریت رستوران (16+ endpoint)
        System.out.println("   🏪 Restaurant Management (16+ endpoints):");
        System.out.println("   GET  /api/restaurants/ - All restaurants");
        System.out.println("   POST /api/restaurants/ - Create restaurant");
        
        // نمایش endpoint های مدیریت سفارشات (20+ endpoint)
        System.out.println("   🛒 Order Management (20+ endpoints):");
        System.out.println("   GET  /api/orders/ - All orders");
        System.out.println("   POST /api/orders/ - Create order");
        
        // نمایش endpoint های سیستم پرداخت (8+ endpoint)
        System.out.println("   💳 Payment System (8+ endpoints):");
        System.out.println("   GET  /api/payments/ - Payment history");
        System.out.println("   POST /api/payments/ - Process payment");
        
        // نمایش endpoint های سیستم کیف پول (6+ endpoint)
        System.out.println("   💰 Wallet System (6+ endpoints):");
        System.out.println("   GET  /api/wallet/ - Wallet balance");
        System.out.println("   POST /api/wallet/ - Add funds");
        
        // نمایش endpoint های سیستم تراکنش‌ها (5+ endpoint)
        System.out.println("   💸 Transaction System (5+ endpoints):");
        System.out.println("   GET  /api/transactions/wallet/history - Transaction history");
        System.out.println("   GET  /api/transactions/wallet/statistics - Wallet statistics");
        
        // نمایش endpoint های سیستم تحویل (16+ endpoint)
        System.out.println("   🚚 Delivery System (16+ endpoints):");
        System.out.println("   GET  /api/deliveries/ - All deliveries");
        System.out.println("   POST /api/deliveries/ - Create delivery");
        
        // نمایش endpoint های مدیریت آیتم‌ها (13+ endpoint)
        System.out.println("   🍔 Item Management (13+ endpoints):");
        System.out.println("   GET  /api/items/ - All items");
        System.out.println("   POST /api/items/ - Create item");
        
        // نمایش endpoint های مدیریت منو (6+ endpoint)
        System.out.println("   📋 Menu Management (6+ endpoints):");
        System.out.println("   GET  /api/menu/ - Menu items");
        System.out.println("   POST /api/menu/ - Add menu item");
        
        // نمایش endpoint های سیستم فروشندگان (10+ endpoint)
        System.out.println("   🏬 Vendor System (10+ endpoints):");
        System.out.println("   GET  /api/vendors/ - All vendors");
        System.out.println("   GET  /api/vendors/search - Search vendors");
        
        // نمایش endpoint های سیستم علاقه‌مندی‌ها (6+ endpoint)
        System.out.println("   ⭐ Favorites System (6+ endpoints):");
        System.out.println("   GET  /api/favorites/ - User favorites");
        System.out.println("   POST /api/favorites/add - Add to favorites");
        System.out.println("   DELETE /api/favorites/remove - Remove favorite");
        
        // نمایش endpoint های سیستم اعلان‌ها (6+ endpoint)
        System.out.println("   🔔 Notification System (6+ endpoints):");
        System.out.println("   GET  /api/notifications/{userId} - User notifications");
        System.out.println("   POST /api/notifications/ - Create notification");
        System.out.println("   PUT  /api/notification/{id}/read - Mark as read");
        System.out.println("   DELETE /api/notification/{id} - Delete notification");
        
        // مرحله 13: تنظیم Graceful Shutdown برای خاموش کردن صحیح سرور
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🛑 Shutting down server...");
            server.stop(2); // توقف سرور با 2 ثانیه انتظار
            DatabaseUtil.shutdown(); // بستن اتصالات پایگاه داده
            System.out.println("✅ Server stopped gracefully");
        }));
    }
    
    /**
     * کلاس Handler برای endpoint بررسی سلامت سرور (/health)
     * این endpoint برای monitoring، health check و load balancer استفاده می‌شود
     * 
     * عملکرد این Handler:
     * - بازگشت وضعیت سرور (UP/DOWN)
     * - اطلاعات نام سرویس
     * - بدون نیاز به احراز هویت
     * 
     * این endpoint معمولاً توسط:
     * - Load balancers برای تشخیص سرورهای سالم
     * - Monitoring systems برای نظارت بر سرور
     * - CI/CD pipelines برای تست deployment
     * استفاده می‌شود
     * 
     * فرمت پاسخ:
     * <pre>
     * {
     *   "status": "UP",
     *   "service": "food-ordering-backend"
     * }
     * </pre>
     * 
     * @author Food Ordering System Team
     * @version 1.0
     * @since 2024
     */
    static class HealthHandler implements HttpHandler {
        
        /**
         * پردازش درخواست health check
         * همیشه وضعیت UP برمی‌گرداند مگر اینکه سرور خراب باشد
         * 
         * @param exchange شیء HttpExchange شامل اطلاعات درخواست
         * @throws IOException در صورت خطا در ارسال پاسخ
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // پاسخ JSON ساده برای نشان دادن سلامت سرور
            String response = "{\"status\":\"UP\",\"service\":\"food-ordering-backend\"}";
            sendResponse(exchange, 200, response);
        }
    }
    
    /**
     * کلاس Handler برای endpoint تست (/api/test)
     * این endpoint برای تست اولیه کارکرد سرور و API استفاده می‌شود
     * 
     * عملکرد این Handler:
     * - تست اتصال به API
     * - نمایش زمان فعلی سرور
     * - بررسی عملکرد JSON response
     * - بدون نیاز به احراز هویت
     * 
     * این endpoint مفید است برای:
     * - تست اولیه پس از راه‌اندازی سرور
     * - بررسی connectivity کلاینت به سرور
     * - Debug مشکلات اتصال
     * - مثال ساده برای توسعه‌دهندگان
     * 
     * فرمت پاسخ:
     * <pre>
     * {
     *   "message": "Hello from Food Ordering Backend!",
     *   "timestamp": "2024-01-01T12:00:00.000Z"
     * }
     * </pre>
     * 
     * @author Food Ordering System Team
     * @version 1.0
     * @since 2024
     */
    static class TestHandler implements HttpHandler {
        
        /**
         * پردازش درخواست تست
         * پیام خوش‌آمدگویی به همراه timestamp فعلی برمی‌گرداند
         * 
         * @param exchange شیء HttpExchange شامل اطلاعات درخواست
         * @throws IOException در صورت خطا در ارسال پاسخ
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // پاسخ JSON شامل پیام تست و زمان فعلی
            String response = "{\"message\":\"Hello from Food Ordering Backend!\",\"timestamp\":\"" + 
                            java.time.Instant.now() + "\"}";
            sendResponse(exchange, 200, response);
        }
    }
    
    /**
     * کلاس Handler برای ثبت نام کاربران (/api/auth/register)
     * مسئول پردازش درخواست‌های ثبت نام کاربران جدید در سیستم
     * 
     * عملکرد این Handler:
     * 1. دریافت و اعتبارسنجی داده‌های ثبت نام (fullName, phone, password)
     * 2. هش کردن رمز عبور با BCrypt برای امنیت
     * 3. ایجاد و ذخیره کاربر جدید در پایگاه داده
     * 4. بازگشت اطلاعات کاربر ثبت نام شده
     * 
     * فرمت درخواست JSON:
     * <pre>
     * {
     *   "fullName": "نام کامل کاربر",
     *   "phone": "09123456789",
     *   "password": "رمز عبور قوی",
     *   "email": "user@example.com",     // اختیاری
     *   "address": "آدرس کاربر"          // اختیاری
     * }
     * </pre>
     * 
     * @author Food Ordering System Team
     * @version 1.0
     * @since 2024
     */
    static class RegisterHandler implements HttpHandler {
        
        /**
         * ObjectMapper برای تبدیل JSON به Java objects و برعکس
         * استفاده از Jackson library برای پردازش سریع و دقیق JSON
         */
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // بررسی نوع درخواست - فقط POST قبول می‌شود
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // خواندن محتوای درخواست (request body)
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    System.out.println("📥 Registration request received");
                    
                    // تبدیل JSON به JsonNode برای پردازش
                    JsonNode json = objectMapper.readTree(requestBody);
                    
                    // اعتبارسنجی فیلدهای ضروری
                    if (!json.has("fullName") || !json.has("phone") || !json.has("password")) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing required fields: fullName, phone, password\"}");
                        return;
                    }
                    
                    // استخراج اطلاعات از JSON
                    String fullName = json.get("fullName").asText();          // نام و نام خانوادگی
                    String phone = json.get("phone").asText();                // شماره تلفن
                    String email = json.has("email") ? json.get("email").asText() : ""; // ایمیل (اختیاری)
                    String password = json.get("password").asText();          // رمز عبور
                    String address = json.has("address") ? json.get("address").asText() : ""; // آدرس (اختیاری)
                    
                    // اعتبارسنجی داده‌های دریافتی - بررسی خالی نبودن فیلدهای ضروری
                    if (fullName.trim().isEmpty() || phone.trim().isEmpty() || password.trim().isEmpty()) {
                        sendResponse(exchange, 400, "{\"error\":\"fullName, phone, and password cannot be empty\"}");
                        return;
                    }
                    
                    // هش کردن رمز عبور با BCrypt
                    String passwordHash = com.myapp.common.utils.PasswordUtil.hashPassword(password);
                    
                    // ایجاد شیء کاربر جدید
                    User user = User.forRegistration(fullName, phone, email, passwordHash, address);
                    
                    // ذخیره کاربر در پایگاه داده
                    User savedUser = authService.registerUser(user);
                    
                    // ایجاد پاسخ موفقیت‌آمیز
                    String response = "{\"message\":\"User registered successfully!\",\"status\":\"success\",\"userId\":" + 
                                    savedUser.getId() + ",\"fullName\":\"" + savedUser.getFullName() + "\",\"phone\":\"" + savedUser.getPhone() + "\"}";
                    sendResponse(exchange, 201, response);
                    
                    // نمایش پیام موفقیت در کنسول سرور
                    System.out.println("✅ User registered: " + savedUser.getFullName() + " (ID: " + savedUser.getId() + ")");
                    
                } catch (Exception e) {
                    // مدیریت خطاهای احتمالی در فرآیند ثبت نام
                    System.err.println("❌ Registration error: " + e.getMessage());
                    e.printStackTrace();
                    String errorResponse = "{\"error\":\"Registration failed\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                    sendResponse(exchange, 500, errorResponse);
                }
            } else {
                // اگر نوع درخواست POST نباشد، خطای Method Not Allowed برگردانده می‌شود
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    
    /**
     * کلاس Handler برای ورود کاربران (/api/auth/login)
     * مسئول پردازش درخواست‌های ورود و ایجاد JWT token برای احراز هویت
     * 
     * عملکرد این Handler:
     * 1. دریافت اطلاعات ورود (شماره تلفن و رمز عبور)
     * 2. احراز هویت کاربر با بررسی رمز عبور
     * 3. ایجاد Access Token و Refresh Token
     * 4. بازگشت token ها به همراه اطلاعات کاربر
     * 
     * فرمت درخواست JSON:
     * <pre>
     * {
     *   "phone": "09123456789",
     *   "password": "رمز عبور کاربر"
     * }
     * </pre>
     * 
     * فرمت پاسخ موفق:
     * <pre>
     * {
     *   "message": "Login successful!",
     *   "status": "success",
     *   "userId": 123,
     *   "fullName": "نام کاربر",
     *   "phone": "09123456789",
     *   "role": "BUYER",
     *   "accessToken": "eyJ0eXAi...",
     *   "refreshToken": "eyJ0eXAi...",
     *   "tokenType": "Bearer",
     *   "expiresIn": 900
     * }
     * </pre>
     * 
     * @author Food Ordering System Team
     * @version 1.0
     * @since 2024
     */
    static class LoginHandler implements HttpHandler {
        
        /**
         * ObjectMapper برای تبدیل JSON به Java objects و برعکس
         * استفاده از Jackson library برای پردازش دقیق JSON
         */
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // بررسی نوع درخواست - فقط POST قبول می‌شود
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // خواندن محتوای درخواست
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    System.out.println("📥 Login request received");
                    
                    // تبدیل JSON به JsonNode
                    JsonNode json = objectMapper.readTree(requestBody);
                    
                    // اعتبارسنجی فیلدهای ضروری
                    if (!json.has("phone") || !json.has("password")) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing required fields: phone, password\"}");
                        return;
                    }
                    
                    // استخراج اطلاعات ورود
                    String phone = json.get("phone").asText();        // شماره تلفن
                    String password = json.get("password").asText();  // رمز عبور
                    
                    // اعتبارسنجی داده‌های دریافتی
                    if (phone.trim().isEmpty() || password.trim().isEmpty()) {
                        sendResponse(exchange, 400, "{\"error\":\"Phone and password cannot be empty\"}");
                        return;
                    }
                    
                    // هش کردن رمز عبور برای مقایسه (در واقع باید از verify استفاده شود)
                    // String passwordHash = "hashed_" + password;
                    
                    // احراز هویت کاربر و دریافت JWT token
                    AuthResult authResult = authService.loginWithTokens(phone, password);
                    
                    // بررسی موفقیت احراز هویت
                    if (!authResult.isAuthenticated()) {
                        sendResponse(exchange, 401, "{\"error\":\"" + authResult.getErrorMessage() + "\"}");
                        return;
                    }
                    
                    // ایجاد پاسخ موفقیت‌آمیز شامل JWT token ها
                    String response = String.format(
                        "{\"message\":\"Login successful!\",\"status\":\"success\",\"userId\":%d," +
                        "\"fullName\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\"," +
                        "\"accessToken\":\"%s\",\"refreshToken\":\"%s\"," +
                        "\"tokenType\":\"Bearer\",\"expiresIn\":%d}",
                        authResult.getUserId(),                         // شناسه کاربر
                        authResult.getPhone().replace("\"", "\\\""),    // نام کاربر (با escape کردن ")
                        authResult.getPhone(),                          // شماره تلفن
                        authResult.getRole(),                           // نقش کاربر
                        authResult.getAccessToken(),                    // Access Token
                        authResult.getRefreshToken(),                   // Refresh Token
                        com.myapp.common.utils.JWTUtil.getAccessTokenValidity() / 1000 // مدت اعتبار token به ثانیه
                    );
                    sendResponse(exchange, 200, response);
                    
                    // نمایش پیام موفقیت در کنسول سرور
                    System.out.println("✅ User logged in with JWT tokens: " + authResult.getPhone() + " (ID: " + authResult.getUserId() + ")");
                    
                } catch (com.myapp.common.exceptions.InvalidCredentialsException e) {
                    // مدیریت خطای اعتبارات نامعتبر
                    sendResponse(exchange, 401, "{\"error\":\"Invalid phone or password\"}");
                } catch (Exception e) {
                    // مدیریت سایر خطاهای احتمالی
                    System.err.println("❌ Login error: " + e.getMessage());
                    e.printStackTrace();
                    String errorResponse = "{\"error\":\"Login failed\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                    sendResponse(exchange, 500, errorResponse);
                }
            } else {
                // خطای Method Not Allowed برای سایر نوع درخواست‌ها
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    
    /**
     * کلاس Handler برای تجدید JWT token (/api/auth/refresh)
     * مسئول تجدید Access Token با استفاده از Refresh Token معتبر
     * 
     * عملکرد این Handler:
     * 1. دریافت Refresh Token از کلاینت
     * 2. اعتبارسنجی Refresh Token
     * 3. ایجاد Access Token و Refresh Token جدید
     * 4. بازگشت token های جدید
     * 
     * این endpoint زمانی استفاده می‌شود که Access Token منقضی شده
     * ولی Refresh Token هنوز معتبر است
     * 
     * فرمت درخواست JSON:
     * <pre>
     * {
     *   "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
     * }
     * </pre>
     * 
     * @author Food Ordering System Team
     * @version 1.0
     * @since 2024
     */
    static class RefreshTokenHandler implements HttpHandler {
        
        /**
         * ObjectMapper برای پردازش JSON درخواست‌ها
         */
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // بررسی نوع درخواست - فقط POST قبول می‌شود
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // خواندن محتوای درخواست
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    JsonNode json = objectMapper.readTree(requestBody);
                    
                    // بررسی وجود Refresh Token
                    if (!json.has("refreshToken")) {
                        sendResponse(exchange, 400, "{\"error\":\"Missing refreshToken field\"}");
                        return;
                    }
                    
                    // استخراج Refresh Token
                    String refreshToken = json.get("refreshToken").asText();
                    
                    // تجدید token از طریق سرویس احراز هویت
                    AuthResult authResult = authService.refreshToken(refreshToken);
                    
                    // بررسی موفقیت تجدید token
                    if (!authResult.isAuthenticated()) {
                        sendResponse(exchange, 401, "{\"error\":\"" + authResult.getErrorMessage() + "\"}");
                        return;
                    }
                    
                    // ایجاد پاسخ شامل token های جدید
                    String response = String.format(
                        "{\"message\":\"Token refreshed successfully\",\"status\":\"success\"," +
                        "\"accessToken\":\"%s\",\"refreshToken\":\"%s\"," +
                        "\"tokenType\":\"Bearer\",\"expiresIn\":%d}",
                        authResult.getAccessToken(),                    // Access Token جدید
                        authResult.getRefreshToken(),                   // Refresh Token جدید
                        com.myapp.common.utils.JWTUtil.getAccessTokenValidity() / 1000 // مدت اعتبار
                    );
                    sendResponse(exchange, 200, response);
                    
                } catch (Exception e) {
                    // مدیریت خطاهای تجدید token
                    System.err.println("❌ Token refresh error: " + e.getMessage());
                    String errorResponse = "{\"error\":\"Token refresh failed\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                    sendResponse(exchange, 500, errorResponse);
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    
    /**
     * کلاس Handler برای اعتبارسنجی JWT token (/api/auth/validate)
     * مسئول بررسی معتبر بودن Access Token و بازگشت اطلاعات کاربر
     * 
     * عملکرد این Handler:
     * 1. دریافت Access Token از header Authorization
     * 2. اعتبارسنجی token (امضا، انقضا، ساختار)
     * 3. استخراج اطلاعات کاربر از token
     * 4. بازگشت اطلاعات کاربر در صورت معتبر بودن
     * 
     * این endpoint برای بررسی وضعیت احراز هویت کاربر
     * بدون نیاز به ورود مجدد استفاده می‌شود
     * 
     * Header مورد نیاز:
     * Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
     * 
     * فرمت پاسخ موفق:
     * <pre>
     * {
     *   "valid": true,
     *   "userId": 123,
     *   "phone": "09123456789",
     *   "role": "BUYER"
     * }
     * </pre>
     * 
     * @author Food Ordering System Team
     * @version 1.0
     * @since 2024
     */
    static class ValidateTokenHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // بررسی نوع درخواست - فقط GET قبول می‌شود
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    // اعتبارسنجی token از طریق middleware احراز هویت
                    AuthResult authResult = AuthMiddleware.authenticate(exchange);
                    
                    // بررسی موفقیت اعتبارسنجی
                    if (!authResult.isAuthenticated()) {
                        sendResponse(exchange, 401, "{\"error\":\"" + authResult.getErrorMessage() + "\"}");
                        return;
                    }
                    
                    // ایجاد پاسخ شامل اطلاعات کاربر
                    String response = String.format(
                        "{\"valid\":true,\"userId\":%d,\"phone\":\"%s\",\"role\":\"%s\"}",
                        authResult.getUserId(),    // شناسه کاربر
                        authResult.getPhone(),     // شماره تلفن کاربر
                        authResult.getRole()       // نقش کاربر
                    );
                    sendResponse(exchange, 200, response);
                    
                } catch (Exception e) {
                    // مدیریت خطاهای اعتبارسنجی
                    System.err.println("❌ Token validation error: " + e.getMessage());
                    String errorResponse = "{\"valid\":false,\"error\":\"Token validation failed\"}";
                    sendResponse(exchange, 401, errorResponse);
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    
    /**
     * کلاس Handler برای خروج کاربران (/api/auth/logout)
     * مسئول پردازش درخواست‌های خروج و invalidate کردن token های کاربر
     * 
     * عملکرد این Handler:
     * 1. احراز هویت کاربر از طریق Access Token
     * 2. invalidate کردن تمام token های کاربر
     * 3. پاک کردن session کاربر از سرور
     * 4. بازگشت تأیید خروج موفق
     * 
     * پس از خروج، کاربر باید مجدداً وارد شود تا بتواند
     * از سرویس‌های محافظت شده استفاده کند
     * 
     * Header مورد نیاز:
     * Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
     * 
     * فرمت پاسخ موفق:
     * <pre>
     * {
     *   "message": "Logged out successfully",
     *   "status": "success"
     * }
     * </pre>
     * 
     * @author Food Ordering System Team
     * @version 1.0
     * @since 2024
     */
    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // بررسی نوع درخواست - فقط POST قبول می‌شود
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // اعتبارسنجی token کاربر
                    AuthResult authResult = AuthMiddleware.authenticate(exchange);
                    
                    // بررسی معتبر بودن token
                    if (!authResult.isAuthenticated()) {
                        sendResponse(exchange, 401, "{\"error\":\"" + authResult.getErrorMessage() + "\"}");
                        return;
                    }
                    
                    // پردازش خروج کاربر از سیستم
                    String message = authService.logout(authResult.getUserId());
                    String response = "{\"message\":\"" + message + "\",\"status\":\"success\"}";
                    sendResponse(exchange, 200, response);
                    
                    // نمایش پیام خروج در کنسول سرور
                    System.out.println("✅ User logged out: " + authResult.getPhone() + " (ID: " + authResult.getUserId() + ")");
                    
                } catch (Exception e) {
                    // مدیریت خطاهای فرآیند خروج
                    System.err.println("❌ Logout error: " + e.getMessage());
                    String errorResponse = "{\"error\":\"Logout failed\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                    sendResponse(exchange, 500, errorResponse);
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
    
    /**
     * متد کمکی برای ارسال پاسخ HTTP استاندارد
     * این متد header های مناسب را تنظیم کرده و پاسخ را به کلاینت ارسال می‌کند
     * 
     * ویژگی‌های این متد:
     * - تنظیم Content-Type به application/json
     * - تنظیم CORS headers برای دسترسی cross-origin
     * - مدیریت صحیح طول محتوا
     * - بستن خودکار streams
     * 
     * کدهای وضعیت متداول:
     * - 200: OK - درخواست موفق
     * - 201: Created - منبع جدید ایجاد شد
     * - 400: Bad Request - درخواست نامعتبر
     * - 401: Unauthorized - احراز هویت لازم
     * - 404: Not Found - منبع یافت نشد
     * - 405: Method Not Allowed - HTTP method پشتیبانی نمی‌شود
     * - 500: Internal Server Error - خطای سرور
     * 
     * @param exchange شیء HttpExchange برای مدیریت درخواست/پاسخ HTTP
     * @param statusCode کد وضعیت HTTP (200, 400, 401, 404, 500, etc.)
     * @param response محتوای پاسخ (معمولاً JSON string)
     * @throws IOException در صورت خطا در نوشتن پاسخ به کلاینت
     * 
     * @author Food Ordering System Team
     * @version 1.0
     * @since 2024
     */
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        // تنظیم header Content-Type برای JSON
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        
        // تنظیم CORS header برای امکان دسترسی از سایر domain ها
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        
        // ارسال header های پاسخ شامل کد وضعیت و طول محتوا
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        
        // نوشتن محتوای پاسخ و بستن stream
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}