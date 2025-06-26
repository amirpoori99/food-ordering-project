package com.myapp.auth;

import com.myapp.common.utils.JWTUtil;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.JwtException;

/**
 * میان‌افزار احراز هویت برای اعتبارسنجی JWT token
 * این کلاس متدهایی برای احراز هویت درخواست‌ها و استخراج اطلاعات کاربر فراهم می‌کند
 * تمام عملیات امنیتی مربوط به JWT در این کلاس متمرکز شده است
 */
public class AuthMiddleware {
    
    /**
     * احراز هویت درخواست با استفاده از JWT token از header Authorization
     * 
     * @param exchange HTTP exchange حاوی درخواست
     * @return AuthResult حاوی اطلاعات احراز هویت
     */
    public static AuthResult authenticate(HttpExchange exchange) {
        // استخراج header Authorization از درخواست
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        
        if (authHeader == null) {
            return AuthResult.unauthenticated("Missing Authorization header");
        }
        
        // استخراج token از header (فرمت: Bearer <token>)
        String token = JWTUtil.extractBearerToken(authHeader);
        if (token == null) {
            return AuthResult.unauthenticated("Invalid Authorization header format. Use 'Bearer <token>'");
        }
        
        return authenticateToken(token);  // اعتبارسنجی token
    }
    
    /**
     * احراز هویت مستقیم با استفاده از JWT token
     * 
     * @param token JWT token برای اعتبارسنجی
     * @return AuthResult حاوی اطلاعات احراز هویت
     */
    public static AuthResult authenticateToken(String token) {
        try {
            // بررسی معتبر بودن token
            if (!JWTUtil.validateToken(token)) {
                return AuthResult.unauthenticated("Invalid token");
            }
            
            // بررسی انقضای token
            if (JWTUtil.isTokenExpired(token)) {
                return AuthResult.unauthenticated("Token expired");
            }
            
            // بررسی نوع token (باید Access Token باشد)
            if (!JWTUtil.isAccessToken(token)) {
                return AuthResult.unauthenticated("Invalid token type. Access token required");
            }
            
            // استخراج اطلاعات کاربر از token
            Long userId = JWTUtil.getUserIdFromToken(token);
            String phone = JWTUtil.getPhoneFromToken(token);
            String role = JWTUtil.getRoleFromToken(token);
            
            return AuthResult.authenticated(userId, phone, role, token);
            
        } catch (JwtException e) {
            return AuthResult.unauthenticated("Token validation failed: " + e.getMessage());
        } catch (Exception e) {
            return AuthResult.unauthenticated("Authentication error: " + e.getMessage());
        }
    }
    
    /**
     * بررسی داشتن نقش مورد نیاز توسط کاربر
     * 
     * @param authResult نتیجه احراز هویت
     * @param requiredRole نقش مورد نیاز
     * @return true اگر کاربر نقش مورد نیاز را داشته باشد، در غیر اینصورت false
     */
    public static boolean hasRole(AuthResult authResult, String requiredRole) {
        return authResult.isAuthenticated() && requiredRole.equals(authResult.getRole());
    }
    
    /**
     * بررسی داشتن هر یک از نقش‌های مورد نیاز توسط کاربر
     * 
     * @param authResult نتیجه احراز هویت
     * @param requiredRoles آرایه نقش‌های مورد نیاز
     * @return true اگر کاربر حداقل یکی از نقش‌ها را داشته باشد، در غیر اینصورت false
     */
    public static boolean hasAnyRole(AuthResult authResult, String... requiredRoles) {
        if (!authResult.isAuthenticated()) {
            return false;
        }
        
        String userRole = authResult.getRole();
        for (String role : requiredRoles) {
            if (role.equals(userRole)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * بررسی اینکه آیا کاربر احراز هویت شده همان کاربر درخواست شده است
     * یا اینکه کاربر دارای مجوز مدیریت است
     * 
     * @param authResult نتیجه احراز هویت
     * @param requestedUserId شناسه کاربر درخواست شده
     * @return true اگر همان کاربر باشد یا مجوز مدیریت داشته باشد
     */
    public static boolean isSameUserOrAdmin(AuthResult authResult, Long requestedUserId) {
        if (!authResult.isAuthenticated()) {
            return false;
        }
        
        // بررسی همان کاربر بودن
        if (authResult.getUserId().equals(requestedUserId)) {
            return true;
        }
        
        // بررسی مجوز مدیریت (برای پیاده‌سازی آینده نقش admin)
        return "admin".equals(authResult.getRole());
    }
    
    /**
     * تجدید Access Token با استفاده از Refresh Token
     * 
     * @param refreshToken Refresh Token برای تجدید
     * @param authRepository مخزن احراز هویت برای اعتبارسنجی کاربر
     * @return AuthResult حاوی Access Token جدید یا خطا
     */
    public static AuthResult refreshAccessToken(String refreshToken, AuthRepository authRepository) {
        try {
            // بررسی معتبر بودن Refresh Token
            if (!JWTUtil.validateToken(refreshToken)) {
                return AuthResult.unauthenticated("Invalid refresh token");
            }
            
            // بررسی انقضای Refresh Token
            if (JWTUtil.isTokenExpired(refreshToken)) {
                return AuthResult.unauthenticated("Refresh token expired");
            }
            
            // بررسی نوع token (باید Refresh Token باشد)
            if (!JWTUtil.isRefreshToken(refreshToken)) {
                return AuthResult.unauthenticated("Invalid token type. Refresh token required");
            }
            
            // استخراج شناسه کاربر از token
            Long userId = JWTUtil.getUserIdFromToken(refreshToken);
            
            // بررسی وجود کاربر در دیتابیس
            var userOpt = authRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return AuthResult.unauthenticated("User not found");
            }
            
            // تولید جفت token جدید
            var user = userOpt.get();
            String[] tokens = JWTUtil.generateTokenPair(user.getId(), user.getPhone(), user.getRole().toString());
            
            return AuthResult.refreshed(user.getId(), user.getPhone(), user.getRole().toString(), tokens[0], tokens[1]);
            
        } catch (JwtException e) {
            return AuthResult.unauthenticated("Token refresh failed: " + e.getMessage());
        } catch (Exception e) {
            return AuthResult.unauthenticated("Refresh error: " + e.getMessage());
        }
    }
    
    /**
     * استخراج شناسه کاربر از پارامتر مسیر درخواست
     * 
     * @param exchange HTTP exchange
     * @param paramName نام پارامتر (مثل "userId")
     * @return شناسه کاربر یا null در صورت عدم یافتن یا نامعتبر بودن
     */
    public static Long extractUserIdFromPath(HttpExchange exchange, String paramName) {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            
            // جستجو برای پارامتر مشخص شده
            for (int i = 0; i < segments.length - 1; i++) {
                if (segments[i].equals(paramName) && i + 1 < segments.length) {
                    return Long.parseLong(segments[i + 1]);
                }
            }
            
            // روش جایگزین: جستجو برای بخش عددی بعد از الگوهای شناخته شده
            if (path.contains("/users/") || path.contains("/user/")) {
                for (String segment : segments) {
                    try {
                        return Long.parseLong(segment);
                    } catch (NumberFormatException ignored) {
                        // ادامه جستجو
                    }
                }
            }
            
        } catch (Exception e) {
            // در محیط production باید خطا log شود
        }
        
        return null;
    }
    
    /**
     * بررسی اینکه آیا درخواست نیاز به احراز هویت دارد بر اساس مسیر
     * 
     * @param path مسیر درخواست
     * @return true اگر احراز هویت لازم باشد، در غیر اینصورت false
     */
    public static boolean requiresAuthentication(String path) {
        // endpoint های عمومی که نیاز به احراز هویت ندارند
        String[] publicPaths = {
            "/api/auth/register",    // ثبت نام
            "/api/auth/login",       // ورود
            "/api/auth/refresh",     // تجدید token
            "/api/health",           // بررسی سلامت
            "/api/status"            // وضعیت سیستم
        };
        
        // بررسی اینکه آیا مسیر در لیست عمومی است
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return false;
            }
        }
        
        // تمام endpoint های دیگر API نیاز به احراز هویت دارند
        return path.startsWith("/api/");
    }
    
    /**
     * دریافت نقش مورد نیاز برای endpoint خاص
     * 
     * @param path مسیر درخواست
     * @param method متد HTTP
     * @return نقش مورد نیاز یا null اگر نقش خاصی لازم نباشد
     */
    public static String getRequiredRole(String path, String method) {
        // مدیریت رستوران - فقط فروشندگان
        if (path.startsWith("/api/restaurants") && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            return "seller";
        }
        
        // مدیریت تحویل - فقط پیک‌ها
        if (path.startsWith("/api/delivery") && ("PUT".equals(method) || "POST".equals(method))) {
            return "delivery";
        }
        
        // endpoint های مدیریت (برای پیاده‌سازی آینده)
        if (path.startsWith("/api/admin/")) {
            return "admin";
        }
        
        // اکثر endpoint ها برای هر کاربر احراز هویت شده قابل دسترسی هستند
        return null;
    }
} 