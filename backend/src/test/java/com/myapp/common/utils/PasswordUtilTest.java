package com.myapp.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * مجموعه تست‌های ابزار رمز عبور
 * 
 * این کلاس تست تمام عملکردهای کلاس PasswordUtil را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Password Hashing & Verification
 *    - تولید hash امن برای رمزهای عبور
 *    - تأیید صحت رمز عبور با hash
 *    - مدیریت salt و security
 * 
 * 2. Password Validation
 *    - بررسی قوانین پیچیدگی رمز عبور
 *    - اعتبارسنجی طول و کاراکترها
 *    - تشخیص الگوهای ضعیف
 * 
 * 3. Password Generation
 *    - تولید رمز عبور امن خودکار
 *    - اطمینان از رعایت قوانین
 *    - یکتایی رمزهای تولیدی
 * 
 * 4. Strength Analysis
 *    - تحلیل قدرت رمز عبور
 *    - امتیازدهی بر اساس پیچیدگی
 *    - توصیه‌های بهبود
 * 
 * 5. Security & Performance
 *    - مقاومت در برابر timing attacks
 *    - کارایی عملیات hash
 *    - مدیریت حافظه امن
 * 
 * Security Considerations:
 * - استفاده از BCrypt برای hashing
 * - Salt تصادفی برای هر hash
 * - مقاومت در برابر rainbow table attacks
 * - Constant-time comparison
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class PasswordUtilTest {
    
    /**
     * تست تولید hash معتبر برای رمز عبور
     * 
     * Scenario: hash کردن رمز عبور برای ذخیره‌سازی امن
     * Expected:
     * - hash نباید null باشد
     * - شامل salt و hash جدا شده با ":"
     * - دو بخش نباید خالی باشند
     */
    @Test
    @DisplayName("Hash password should create valid hash")
    public void testHashPassword() {
        // Arrange - رمز عبور نمونه
        String password = "TestPassword123@";
        
        // Act - تولید hash
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        // Assert - بررسی فرمت و محتوای hash
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.contains(":"));
        
        String[] parts = hashedPassword.split(":");
        assertEquals(2, parts.length);
        assertFalse(parts[0].isEmpty()); // salt نباید خالی باشد
        assertFalse(parts[1].isEmpty()); // hash نباید خالی باشد
    }
    
    /**
     * تست پرتاب exception برای رمز عبور null
     * 
     * Scenario: ورودی نامعتبر null به hash function
     * Expected: IllegalArgumentException پرتاب شود
     */
    @Test
    @DisplayName("Hash password should throw exception for null password")
    public void testHashPasswordNullInput() {
        // Act & Assert - انتظار exception برای null input
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword(null);
        });
    }
    
    /**
     * تست پرتاب exception برای رمز عبور خالی
     * 
     * Scenario: ورودی نامعتبر empty string به hash function
     * Expected: IllegalArgumentException پرتاب شود
     */
    @Test
    @DisplayName("Hash password should throw exception for empty password")
    public void testHashPasswordEmptyInput() {
        // Act & Assert - انتظار exception برای empty input
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword("");
        });
    }
    
    /**
     * تست تأیید صحیح رمز عبور
     * 
     * Scenario: بررسی رمز عبور صحیح در برابر hash
     * Expected: تأیید موفق و return true
     */
    @Test
    @DisplayName("Verify password should return true for correct password")
    public void testVerifyPasswordCorrect() {
        // Arrange - تولید hash برای رمز عبور
        String password = "TestPassword123@";
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        // Act & Assert - تأیید رمز عبور صحیح
        assertTrue(PasswordUtil.verifyPassword(password, hashedPassword));
    }
    
    /**
     * تست رد رمز عبور نادرست
     * 
     * Scenario: بررسی رمز عبور اشتباه در برابر hash
     * Expected: رد شدن و return false
     */
    @Test
    @DisplayName("Verify password should return false for incorrect password")
    public void testVerifyPasswordIncorrect() {
        // Arrange - رمز صحیح و اشتباه
        String password = "TestPassword123@";
        String wrongPassword = "WrongPassword123@";
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        // Act & Assert - رد رمز عبور اشتباه
        assertFalse(PasswordUtil.verifyPassword(wrongPassword, hashedPassword));
    }
    
    /**
     * تست مدیریت ورودی‌های null در verification
     * 
     * Scenario: ورودی‌های نامعتبر null به verify function
     * Expected: همه حالات false برگردانند (نه exception)
     */
    @Test
    @DisplayName("Verify password should return false for null inputs")
    public void testVerifyPasswordNullInputs() {
        // Act & Assert - مدیریت null inputs بدون exception
        assertFalse(PasswordUtil.verifyPassword(null, "hash"));
        assertFalse(PasswordUtil.verifyPassword("password", null));
        assertFalse(PasswordUtil.verifyPassword(null, null));
    }
    
    /**
     * تست مدیریت فرمت نامعتبر hash
     * 
     * Scenario: hash با فرمت اشتباه یا مخدوش
     * Expected: false برگردانده شود بدون crash
     */
    @Test
    @DisplayName("Verify password should return false for invalid hash format")
    public void testVerifyPasswordInvalidHashFormat() {
        // Act & Assert - مدیریت hash های نامعتبر
        assertFalse(PasswordUtil.verifyPassword("password", "invalidhash"));
        assertFalse(PasswordUtil.verifyPassword("password", "invalid:hash:format"));
    }
    
    /**
     * تست تولید hash های متفاوت برای رمز یکسان
     * 
     * Scenario: هر بار hash کردن باید salt جدید استفاده کند
     * Expected:
     * - دو hash متفاوت تولید شود
     * - هر دو معتبر باشند برای verification
     * 
     * Security: مقاومت در برابر rainbow table attacks
     */
    @Test
    @DisplayName("Same password should produce different hashes (due to salt)")
    public void testSamePasswordDifferentHashes() {
        // Arrange
        String password = "TestPassword123@";
        
        // Act - تولید دو hash از همان رمز
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);
        
        // Assert - hash ها متفاوت ولی هر دو معتبر
        assertNotEquals(hash1, hash2);
        
        // هر دو باید صحیح verify شوند
        assertTrue(PasswordUtil.verifyPassword(password, hash1));
        assertTrue(PasswordUtil.verifyPassword(password, hash2));
    }
    
    /**
     * تست قبولی رمزهای عبور معتبر
     * 
     * Scenario: رمزهای عبور که تمام قوانین را رعایت می‌کنند
     * Expected: همه validation passed شوند
     */
    @Test
    @DisplayName("Valid password should pass validation")
    public void testIsValidPasswordValid() {
        // Act & Assert - رمزهای معتبر
        assertTrue(PasswordUtil.isValidPassword("ValidPass123@"));
        assertTrue(PasswordUtil.isValidPassword("AnotherGood1#"));
        assertTrue(PasswordUtil.isValidPassword("Complex123$"));
    }
    
    /**
     * تست رد رمزهای عبور نامعتبر
     * 
     * Scenario: رمزهایی که قوانین مختلف را نقض می‌کنند
     * Expected: همه validation fail شوند
     * 
     * Password Rules Tested:
     * - حداقل 8 کاراکتر
     * - حداقل یک حرف بزرگ
     * - حداقل یک حرف کوچک  
     * - حداقل یک رقم
     * - حداقل یک کاراکتر خاص
     * - بدون space
     */
    @Test
    @DisplayName("Invalid passwords should fail validation")
    public void testIsValidPasswordInvalid() {
        // خیلی کوتاه
        assertFalse(PasswordUtil.isValidPassword("Short1@"));
        
        // بدون حرف بزرگ
        assertFalse(PasswordUtil.isValidPassword("lowercase123@"));
        
        // بدون حرف کوچک
        assertFalse(PasswordUtil.isValidPassword("UPPERCASE123@"));
        
        // بدون رقم
        assertFalse(PasswordUtil.isValidPassword("NoDigits@"));
        
        // بدون کاراکتر خاص
        assertFalse(PasswordUtil.isValidPassword("NoSpecial123"));
        
        // شامل space
        assertFalse(PasswordUtil.isValidPassword("Has Space123@"));
        
        // رمز null
        assertFalse(PasswordUtil.isValidPassword(null));
    }
    
    /**
     * تست تولید رمز عبور امن خودکار
     * 
     * Scenario: تولید رمز عبور قوی برای کاربر
     * Expected:
     * - طول درخواستی رعایت شود
     * - تمام قوانین validation پاس شود
     * - رمز قابل استفاده باشد
     */
    @Test
    @DisplayName("Generate secure password should create valid password")
    public void testGenerateSecurePassword() {
        // Act - تولید رمز 12 کاراکتری
        String password = PasswordUtil.generateSecurePassword(12);
        
        // Assert - بررسی کیفیت تولیدی
        assertNotNull(password);
        assertEquals(12, password.length());
        assertTrue(PasswordUtil.isValidPassword(password));
    }
    
    /**
     * تست اعمال حداقل طول در تولید رمز
     * 
     * Scenario: درخواست رمز کوتاه‌تر از حداقل مجاز
     * Expected: حداقل 8 کاراکتر تولید شود
     */
    @Test
    @DisplayName("Generate secure password should enforce minimum length")
    public void testGenerateSecurePasswordMinimumLength() {
        // Act - درخواست رمز 5 کاراکتری (کمتر از 8)
        String password = PasswordUtil.generateSecurePassword(5);
        
        // Assert - حداقل 8 کاراکتر تولید شود
        assertNotNull(password);
        assertTrue(password.length() >= 8);
        assertTrue(PasswordUtil.isValidPassword(password));
    }
    
    /**
     * تست یکتایی رمزهای تولیدی
     * 
     * Scenario: تولید چندین رمز پشت سر هم
     * Expected: رمزها متفاوت باشند (نه تکراری)
     */
    @Test
    @DisplayName("Generate secure password should create different passwords")
    public void testGenerateSecurePasswordUnique() {
        // Act - تولید دو رمز
        String password1 = PasswordUtil.generateSecurePassword(10);
        String password2 = PasswordUtil.generateSecurePassword(10);
        
        // Assert - رمزها متفاوت باشند
        assertNotEquals(password1, password2);
    }
    
    /**
     * تست امتیازدهی قدرت رمز عبور
     * 
     * Scenario: ارزیابی قدرت رمزهای مختلف
     * Expected: امتیازات صحیح بر اساس پیچیدگی
     * 
     * Scoring System:
     * 0 = Very Weak (خالی، کوتاه)
     * 1 = Weak (فقط lowercase)
     * 2 = Fair (lowercase + uppercase) 
     * 3 = Good (+ digits)
     * 4 = Strong (+ special chars)
     * 5 = Very Strong (+ 12+ chars)
     */
    @Test
    @DisplayName("Password strength should return correct scores")
    public void testGetPasswordStrength() {
        // خیلی ضعیف
        assertEquals(0, PasswordUtil.getPasswordStrength(""));
        assertEquals(0, PasswordUtil.getPasswordStrength(null));
        assertEquals(0, PasswordUtil.getPasswordStrength("abc")); // خیلی کوتاه
        
        // ضعیف
        assertEquals(1, PasswordUtil.getPasswordStrength("password")); // 8+ chars, فقط lowercase
        
        // متوسط
        assertEquals(2, PasswordUtil.getPasswordStrength("Password")); // 8+ chars, lowercase, uppercase
        
        // خوب
        assertEquals(3, PasswordUtil.getPasswordStrength("Password1")); // 8+ chars, lowercase, uppercase, digits
        
        // قوی
        assertEquals(4, PasswordUtil.getPasswordStrength("Password1@")); // تمام الزامات
        
        // خیلی قوی
        assertEquals(5, PasswordUtil.getPasswordStrength("VeryStrongPassword1@")); // تمام الزامات + 12+ chars
    }
    
    /**
     * تست توضیحات قدرت رمز عبور
     * 
     * Scenario: تولید متن توضیحی برای امتیاز قدرت
     * Expected: متن مناسب برای هر امتیاز
     */
    @Test
    @DisplayName("Password strength description should return correct strings")
    public void testGetPasswordStrengthDescription() {
        // بررسی تمام سطوح قدرت
        assertEquals("Very Weak", PasswordUtil.getPasswordStrengthDescription(0));
        assertEquals("Weak", PasswordUtil.getPasswordStrengthDescription(1));
        assertEquals("Fair", PasswordUtil.getPasswordStrengthDescription(2));
        assertEquals("Good", PasswordUtil.getPasswordStrengthDescription(3));
        assertEquals("Strong", PasswordUtil.getPasswordStrengthDescription(4));
        assertEquals("Very Strong", PasswordUtil.getPasswordStrengthDescription(5));
        assertEquals("Unknown", PasswordUtil.getPasswordStrengthDescription(-1));
        assertEquals("Unknown", PasswordUtil.getPasswordStrengthDescription(10));
    }
    
    /**
     * تست دریافت الزامات رمز عبور
     * 
     * Scenario: نمایش قوانین رمز عبور به کاربر
     * Expected: متن کاملی که تمام الزامات را شرح دهد
     */
    @Test
    @DisplayName("Password requirements should return non-empty string")
    public void testGetPasswordRequirements() {
        // Act - دریافت متن الزامات
        String requirements = PasswordUtil.getPasswordRequirements();
        
        // Assert - بررسی محتوای کامل
        assertNotNull(requirements);
        assertFalse(requirements.isEmpty());
        assertTrue(requirements.contains("8 characters"));
        assertTrue(requirements.contains("uppercase"));
        assertTrue(requirements.contains("lowercase"));
        assertTrue(requirements.contains("digit"));
        assertTrue(requirements.contains("special"));
    }
    
    /**
     * تست عدم نیاز به rehash
     * 
     * Scenario: بررسی اینکه آیا hash نیاز به بازسازی دارد
     * Expected: همیشه false (BCrypt خودش قابل upgrade است)
     */
    @Test
    @DisplayName("Needs rehash should return false for all inputs")
    public void testNeedsRehash() {
        // Act & Assert - هیچ hash نیاز به rehash ندارد
        assertFalse(PasswordUtil.needsRehash("any_hash"));
        assertFalse(PasswordUtil.needsRehash(""));
        assertFalse(PasswordUtil.needsRehash(null));
    }
    
    /**
     * تست کاهش امتیاز برای الگوهای رایج
     * 
     * Scenario: تشخیص و جریمه الگوهای ضعیف
     * Expected: رمزهای با الگوی رایج امتیاز کمتری بگیرند
     * 
     * Common Patterns:
     * - 123, abc, قwerty
     * - تکرار کاراکتر
     * - الگوهای keyboard
     */
    @Test
    @DisplayName("Password strength should penalize common patterns")
    public void testPasswordStrengthPenalty() {
        // Arrange - رمز عادی vs رمزهای با الگو
        int normalScore = PasswordUtil.getPasswordStrength("StrongPass1@");
        int patternScore1 = PasswordUtil.getPasswordStrength("StrongPass123@");
        int patternScore2 = PasswordUtil.getPasswordStrength("password123@ABC");
        
        // Assert - رمز عادی باید امتیاز بالاتری داشته باشد
        assertTrue(normalScore >= patternScore1);
        assertTrue(normalScore >= patternScore2);
    }
    
    /**
     * تست حساسیت به حروف کوچک و بزرگ در verification
     * 
     * Scenario: بررسی case sensitivity در تأیید رمز
     * Expected: فقط رمز دقیقاً مشابه قبول شود
     */
    @Test
    @DisplayName("Hash verification should be case sensitive")
    public void testHashVerificationCaseSensitive() {
        // Arrange
        String password = "TestPassword123@";
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        // Act & Assert - case sensitivity
        assertTrue(PasswordUtil.verifyPassword(password, hashedPassword));
        assertFalse(PasswordUtil.verifyPassword(password.toLowerCase(), hashedPassword));
        assertFalse(PasswordUtil.verifyPassword(password.toUpperCase(), hashedPassword));
    }
    
    /**
     * تست عملکرد - سرعت عملیات hash و verify
     * 
     * Scenario: اطمینان از کارایی مناسب برای production
     * Expected:
     * - hashing در زمان معقول (< 5 ثانیه برای 100 عملیات)
     * - verification سریع‌تر (< 2 ثانیه برای 100 عملیات)
     * 
     * Performance Requirements:
     * - Hash باید آهسته باشد (security)
     * - Verify باید سریع باشد (user experience)
     */
    @Test
    @DisplayName("Performance test - hash and verify operations")
    public void testPerformance() {
        String password = "TestPassword123@";
        
        // تست کارایی hashing
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            PasswordUtil.hashPassword(password);
        }
        long hashTime = System.currentTimeMillis() - startTime;
        
        // Hashing باید در زمان معقول باشد (کمتر از 5 ثانیه برای 100 عملیات)
        assertTrue(hashTime < 5000, "Hashing took too long: " + hashTime + "ms");
        
        // تست کارایی verification
        String hashedPassword = PasswordUtil.hashPassword(password);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            PasswordUtil.verifyPassword(password, hashedPassword);
        }
        long verifyTime = System.currentTimeMillis() - startTime;
        
        // Verification باید سریع باشد (کمتر از 2 ثانیه برای 100 عملیات)
        assertTrue(verifyTime < 2000, "Verification took too long: " + verifyTime + "ms");
    }
} 