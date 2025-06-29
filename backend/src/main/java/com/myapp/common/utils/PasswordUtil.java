package com.myapp.common.utils;

// وارد کردن کتابخانه‌های امنیتی و رمزنگاری
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * کلاس ابزاری برای عملیات رمز عبور
 * شامل hash کردن امن، تأیید و اعتبارسنجی رمز عبور
 * از الگوریتم SHA-256 با Salt تصادفی استفاده می‌کند
 */
public class PasswordUtil {
    
    // الگوریتم hash مورد استفاده
    private static final String HASH_ALGORITHM = "SHA-256";
    
    // طول Salt تصادفی (32 بایت)
    private static final int SALT_LENGTH = 32;
    
    // مولد عدد تصادفی امن
    private static final SecureRandom random = new SecureRandom();
    
    // الگوی اعتبارسنجی رمز عبور (حداقل 8 کاراکتر، شامل حروف کوچک، بزرگ، عدد و کاراکتر خاص)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );
    
    /**
     * تولید hash امن برای رمز عبور با Salt تصادفی
     * 
     * @param password رمز عبور متنی
     * @return رشته حاوی Salt و رمز عبور hash شده جدا شده با ':'
     * @throws IllegalArgumentException در صورت null یا خالی بودن رمز عبور
     */
    public static String hashPassword(String password) {
        // اعتبارسنجی ورودی
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            // تولید Salt تصادفی
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // hash کردن رمز عبور با Salt
            String hashedPassword = hashPasswordWithSalt(password, salt);
            
            // برگرداندن در فرمت salt:hashedPassword
            return Base64.getEncoder().encodeToString(salt) + ":" + hashedPassword;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /**
     * تأیید رمز عبور در مقابل hash ذخیره شده
     * 
     * @param password رمز عبور متنی برای تأیید
     * @param storedHash hash ذخیره شده در فرمت "salt:hashedPassword"
     * @return true اگر رمز عبور مطابقت داشته باشد، در غیر اینصورت false
     */
    public static boolean verifyPassword(String password, String storedHash) {
        // بررسی null بودن ورودی‌ها
        if (password == null || storedHash == null) {
            return false;
        }
        
        try {
            // تجزیه hash ذخیره شده برای دریافت Salt و hash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false; // فرمت نامعتبر
            }
            
            // رمزگشایی Salt
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            String expectedHash = parts[1];
            
            // hash کردن رمز عبور ارائه شده با همان Salt
            String actualHash = hashPasswordWithSalt(password, salt);
            
            // مقایسه hash ها با روش constant-time برای جلوگیری از timing attack
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            // در برنامه واقعی باید exception را log کرد
            return false;
        }
    }
    
    /**
     * hash کردن رمز عبور با Salt مشخص
     * متد کمکی برای hash کردن با Salt داده شده
     */
    private static String hashPasswordWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        
        // افزودن Salt به digest
        md.update(salt);
        
        // hash کردن رمز عبور
        byte[] hashedPassword = md.digest(password.getBytes());
        
        // برگرداندن hash encode شده با base64
        return Base64.getEncoder().encodeToString(hashedPassword);
    }
    
    /**
     * مقایسه رشته با زمان ثابت برای جلوگیری از timing attack
     * این روش از timing attack جلوگیری می‌کند
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i); // XOR برای مقایسه
        }
        
        return result == 0; // اگر همه کاراکترها یکسان باشند، result برابر 0 است
    }
    
    /**
     * اعتبارسنجی قدرت رمز عبور
     * 
     * @param password رمز عبور برای اعتبارسنجی
     * @return true اگر رمز عبور الزامات قدرت را برآورده کند
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        
        return PASSWORD_PATTERN.matcher(password).matches(); // بررسی با regex pattern
    }
    
    /**
     * دریافت الزامات قدرت رمز عبور به صورت رشته قابل خواندن
     * 
     * @return متن شامل الزامات رمز عبور
     */
    public static String getPasswordRequirements() {
        return "Password must contain:\n" +
               "- At least 8 characters\n" +                      // حداقل 8 کاراکتر
               "- At least one uppercase letter (A-Z)\n" +        // حداقل یک حرف بزرگ
               "- At least one lowercase letter (a-z)\n" +        // حداقل یک حرف کوچک
               "- At least one digit (0-9)\n" +                   // حداقل یک عدد
               "- At least one special character (@#$%^&+=)\n" +   // حداقل یک کاراکتر خاص
               "- No whitespace characters";                      // بدون فضای خالی
    }
    
    /**
     * تولید رمز عبور امن تصادفی
     * این متد رمز عبور قوی با طول مشخص تولید می‌کند
     * 
     * @param length طول رمز عبور مورد نظر (حداقل 8 کاراکتر)
     * @return رمز عبور تصادفی که الزامات قدرت را برآورده می‌کند
     */
    public static String generateSecurePassword(int length) {
        if (length < 8) {
            length = 8; // حداقل طول 8 کاراکتر
        }
        
        // مجموعه کاراکترهای مختلف
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";    // حروف بزرگ انگلیسی
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";    // حروف کوچک انگلیسی
        String digits = "0123456789";                       // اعداد
        String specialChars = "@#$%^&+=";                   // کاراکترهای خاص
        String allChars = upperCase + lowerCase + digits + specialChars; // همه کاراکترها
        
        StringBuilder password = new StringBuilder();
        
        // اطمینان از وجود حداقل یک کاراکتر از هر دسته الزامی
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        
        // پر کردن بقیه طول با کاراکترهای تصادفی
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // به هم ریختن ترتیب کاراکترها برای جلوگیری از الگوهای قابل پیش‌بینی
        return shuffleString(password.toString());
    }
    
    /**
     * به هم ریختن کاراکترهای یک رشته
     * این متد الگوریتم Fisher-Yates برای shuffle استفاده می‌کند
     * 
     * @param input رشته ورودی برای به هم ریختن
     * @return رشته با کاراکترهای به هم ریخته
     */
    private static String shuffleString(String input) {
        char[] chars = input.toCharArray(); // تبدیل رشته به آرایه کاراکتر
        
        // الگوریتم Fisher-Yates برای shuffle تصادفی
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1); // انتخاب ایندکس تصادفی
            
            // جابجایی chars[i] و chars[j]
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars); // تبدیل آرایه به رشته
    }
    
    /**
     * بررسی نیاز به hash مجدد رمز عبور (برای ارتقاء امنیتی)
     * در حال حاضر همیشه false برمی‌گرداند، اما می‌تواند برای تغییرات الگوریتم توسعه یابد
     * 
     * @param hashedPassword رمز عبور hash شده برای بررسی
     * @return true اگر نیاز به hash مجدد باشد، در غیر اینصورت false
     */
    public static boolean needsRehash(String hashedPassword) {
        // در نسخه‌های آینده، می‌تواند بررسی کند که آیا hash با الگوریتم‌های قدیمی ایجاد شده
        return false;
    }
    
    /**
     * دریافت امتیاز قدرت رمز عبور (0-5)
     * این متد قدرت رمز عبور را بر اساس معیارهای مختلف ارزیابی می‌کند
     * 
     * @param password رمز عبور برای ارزیابی
     * @return امتیاز قدرت: 0=خیلی ضعیف، 1=ضعیف، 2=متوسط، 3=خوب، 4=قوی، 5=خیلی قوی
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0; // رمز عبور خالی یا null
        }
        
        int score = 0;
        
        // بررسی طول رمز عبور
        if (password.length() >= 8) score++;   // امتیاز برای طول حداقل 8
        if (password.length() >= 12) score++;  // امتیاز اضافی برای طول 12+
        
        // بررسی تنوع کاراکترها
        if (password.matches(".*[a-z].*")) score++; // حروف کوچک
        if (password.matches(".*[A-Z].*")) score++; // حروف بزرگ
        if (password.matches(".*[0-9].*")) score++; // اعداد
        if (password.matches(".*[@#$%^&+=!*].*")) score++; // کاراکترهای خاص
        
        // کاهش امتیاز برای الگوهای رایج و ضعیف
        if (password.matches(".*123.*") || password.matches(".*abc.*") || 
            password.toLowerCase().contains("password")) {
            score = Math.max(0, score - 1); // کاهش یک امتیاز (حداقل 0)
        }
        
        return Math.min(5, score); // حداکثر امتیاز 5
    }
    
    /**
     * دریافت توضیح قدرت رمز عبور به صورت متنی
     * این متد امتیاز عددی قدرت را به توضیح فارسی تبدیل می‌کند
     * 
     * @param strength امتیاز قدرت رمز عبور (0-5)
     * @return توضیح فارسی قدرت رمز عبور
     */
    public static String getPasswordStrengthDescription(int strength) {
        switch (strength) {
            case 0: return "خیلی ضعیف";     // Very Weak
            case 1: return "ضعیف";         // Weak
            case 2: return "متوسط";        // Fair
            case 3: return "خوب";          // Good
            case 4: return "قوی";          // Strong
            case 5: return "خیلی قوی";     // Very Strong
            default: return "نامشخص";      // Unknown
        }
    }
}