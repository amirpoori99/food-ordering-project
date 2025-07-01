package com.myapp.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * کلاس ابزاری JWT - مدیریت تولید، اعتبارسنجی و پردازش token ها
 * این کلاس عملکرد احراز هویت امن JWT را فراهم می‌کند
 * شامل دو نوع token: Access Token و Refresh Token
 */
public class JWTUtil {
    
    // کلید مخفی برای امضای JWT (در محیط production باید از متغیر محیطی دریافت شود)
    private static final String SECRET_KEY = "mySecretKeyForJWTThatShouldBeVeryLongAndSecureInProductionEnvironment123456789";
    
    // زمان اعتبار token ها
    private static final long ACCESS_TOKEN_VALIDITY = TimeUnit.HOURS.toMillis(24); // 24 ساعت
    private static final long REFRESH_TOKEN_VALIDITY = TimeUnit.DAYS.toMillis(7);  // 7 روز
    
    // صادرکننده JWT
    private static final String ISSUER = "food-ordering-app";
    
    /**
     * دریافت کلید امضای رمزنگاری شده
     * این متد کلید مخفی را به فرمت SecretKey تبدیل می‌کند
     * 
     * @return کلید امضای رمزنگاری شده
     */
    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
    
    /**
     * تولید Access Token برای کاربر احراز هویت شده
     * این token برای دسترسی به API ها استفاده می‌شود
     * 
     * @param userId شناسه کاربر
     * @param phone شماره تلفن کاربر
     * @param role نقش کاربر (customer, seller, delivery)
     * @return JWT Access Token
     * @throws IllegalArgumentException در صورت null بودن پارامترها
     */
    public static String generateAccessToken(Long userId, String phone, String role) {
        // اعتبارسنجی ورودی‌ها
        if (userId == null || phone == null || role == null) {
            throw new IllegalArgumentException("UserId, phone, and role cannot be null");
        }
        
        Date now = new Date();                                        // زمان فعلی
        Date expirationDate = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY); // زمان انقضا
        
        return Jwts.builder()
                .subject(userId.toString())           // شناسه کاربر به عنوان subject
                .claim("phone", phone)                // شماره تلفن در claim
                .claim("role", role)                  // نقش کاربر در claim
                .claim("type", "access")              // نوع token
                .issuer(ISSUER)                       // صادرکننده
                .issuedAt(now)                        // زمان صدور
                .expiration(expirationDate)           // زمان انقضا
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // امضا با HMAC-SHA256
                .compact();                           // تولید نهایی token
    }
    
    /**
     * تولید Refresh Token برای کاربر
     * این token برای تجدید Access Token استفاده می‌شود
     * 
     * @param userId شناسه کاربر
     * @return JWT Refresh Token
     * @throws IllegalArgumentException در صورت null بودن userId
     */
    public static String generateRefreshToken(Long userId) {
        // اعتبارسنجی ورودی
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        
        Date now = new Date();                                         // زمان فعلی
        Date expirationDate = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY); // زمان انقضا
        
        return Jwts.builder()
                .subject(userId.toString())           // شناسه کاربر
                .claim("type", "refresh")             // نوع token
                .issuer(ISSUER)                       // صادرکننده
                .issuedAt(now)                        // زمان صدور
                .expiration(expirationDate)           // زمان انقضا
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // امضا
                .compact();                           // تولید نهایی token
    }
    
    /**
     * اعتبارسنجی JWT token
     * بررسی امضا، صادرکننده و ساختار token
     * 
     * @param token JWT token برای اعتبارسنجی
     * @return true اگر token معتبر باشد، در غیر اینصورت false
     */
    public static boolean validateToken(String token) {
        try {
            // پردازش و اعتبارسنجی token
            Jwts.parser()
                .verifyWith(getSigningKey())        // بررسی امضا
                .requireIssuer(ISSUER)              // بررسی صادرکننده
                .build()
                .parseSignedClaims(token);          // پردازش claims
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // token نامعتبر
        }
    }
    
    /**
     * دریافت شناسه کاربر از JWT token
     * 
     * @param token JWT token
     * @return شناسه کاربر
     * @throws JwtException در صورت نامعتبر بودن token
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject()); // تبدیل subject به Long
    }
    
    /**
     * دریافت شماره تلفن کاربر از JWT token
     * 
     * @param token JWT token
     * @return شماره تلفن کاربر
     * @throws JwtException در صورت نامعتبر بودن token یا عدم وجود phone
     */
    public static String getPhoneFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String phone = claims.get("phone", String.class);
        if (phone == null) {
            throw new JwtException("Token does not contain phone claim");
        }
        return phone;
    }
    
    /**
     * دریافت نقش کاربر از JWT token
     * 
     * @param token JWT token
     * @return نقش کاربر
     * @throws JwtException در صورت نامعتبر بودن token یا عدم وجود role
     */
    public static String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String role = claims.get("role", String.class);
        if (role == null) {
            throw new JwtException("Token does not contain role claim");
        }
        return role;
    }
    
    /**
     * دریافت نوع token (access/refresh) از JWT token
     * 
     * @param token JWT token
     * @return نوع token
     * @throws JwtException در صورت نامعتبر بودن token یا عدم وجود type
     */
    public static String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        String type = claims.get("type", String.class);
        if (type == null) {
            throw new JwtException("Token does not contain type claim");
        }
        return type;
    }
    
    /**
     * بررسی انقضای token
     * 
     * @param token JWT token
     * @return true اگر token منقضی شده باشد، در غیر اینصورت false
     */
    public static boolean isTokenExpired(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return true; // token های null یا خالی را منقضی در نظر می‌گیریم
            }
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date()); // مقایسه با زمان فعلی
        } catch (JwtException e) {
            return true; // token های نامعتبر را منقضی در نظر می‌گیریم
        }
    }
    
    /**
     * دریافت تاریخ انقضا از JWT token
     * 
     * @param token JWT token
     * @return تاریخ انقضا
     * @throws JwtException در صورت نامعتبر بودن token
     */
    public static Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
    
    /**
     * دریافت تاریخ صدور از JWT token
     * 
     * @param token JWT token
     * @return تاریخ صدور
     * @throws JwtException در صورت نامعتبر بودن token
     */
    public static Date getIssuedAtFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getIssuedAt();
    }
    
    /**
     * بررسی اینکه آیا token از نوع Access است
     * 
     * @param token JWT token
     * @return true اگر Access Token باشد، در غیر اینصورت false
     */
    public static boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenType(token));
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * بررسی اینکه آیا token از نوع Refresh است
     * 
     * @param token JWT token
     * @return true اگر Refresh Token باشد، در غیر اینصورت false
     */
    public static boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(getTokenType(token));
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * استخراج Bearer Token از header Authorization
     * 
     * @param authorizationHeader مقدار header Authorization
     * @return JWT token یا null در صورت نامعتبر بودن فرمت
     */
    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim(); // حذف "Bearer " از ابتدا
            return token.isEmpty() ? null : token;
        }
        return null;
    }
    
    /**
     * دریافت زمان باقیمانده تا انقضای token (بر حسب میلی‌ثانیه)
     * 
     * @param token JWT token
     * @return زمان باقیمانده بر حسب میلی‌ثانیه، یا 0 در صورت انقضا/نامعتبر بودن
     */
    public static long getRemainingTimeToExpire(String token) {
        if (token == null || token.trim().isEmpty()) {
            return 0;
        }
        try {
            Date expiration = getExpirationDateFromToken(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining); // حداقل 0 برگردان
        } catch (JwtException | IllegalArgumentException e) {
            return 0;
        }
    }
    
    /**
     * بررسی داشتن نقش خاص توسط کاربر
     * 
     * @param token JWT token
     * @param requiredRole نقش مورد نیاز
     * @return true اگر کاربر نقش مورد نیاز را داشته باشد، در غیر اینصورت false
     */
    public static boolean hasRole(String token, String requiredRole) {
        try {
            String userRole = getRoleFromToken(token);
            return requiredRole.equals(userRole);
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * دریافت تمام claims از JWT token
     * متد کمکی برای استخراج اطلاعات از token
     * 
     * @param token JWT token
     * @return شیء Claims
     * @throws JwtException در صورت نامعتبر بودن token
     */
    private static Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())        // بررسی امضا
                .requireIssuer(ISSUER)              // بررسی صادرکننده
                .build()
                .parseSignedClaims(token)           // پردازش token
                .getPayload();                      // دریافت payload (claims)
    }
    
    /**
     * تولید هر دو نوع token (Access و Refresh) برای کاربر
     * این متد هر دو token مورد نیاز را به صورت همزمان تولید می‌کند
     * 
     * @param userId شناسه کاربر
     * @param phone شماره تلفن کاربر
     * @param role نقش کاربر
     * @return آرایه شامل [accessToken, refreshToken]
     */
    public static String[] generateTokenPair(Long userId, String phone, String role) {
        String accessToken = generateAccessToken(userId, phone, role);  // تولید Access Token
        String refreshToken = generateRefreshToken(userId);             // تولید Refresh Token
        return new String[]{accessToken, refreshToken};                 // برگرداندن هر دو
    }
    
    /**
     * دریافت دوره اعتبار Access Token بر حسب میلی‌ثانیه
     * 
     * @return دوره اعتبار Access Token
     */
    public static long getAccessTokenValidity() {
        return ACCESS_TOKEN_VALIDITY;
    }
    
    /**
     * دریافت دوره اعتبار Refresh Token بر حسب میلی‌ثانیه
     * 
     * @return دوره اعتبار Refresh Token
     */
    public static long getRefreshTokenValidity() {
        return REFRESH_TOKEN_VALIDITY;
    }
}
