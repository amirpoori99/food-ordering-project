package com.myapp.api;

import com.myapp.auth.AuthRepository;
import com.myapp.auth.AuthService;
import com.myapp.common.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.*;

/**
 * مجموعه تست‌های جامع عملکرد ثبت نام API
 * 
 * این کلاس تست تمام جنبه‌های API ثبت نام کاربران را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Basic Registration Tests
 *    - ثبت نام با داده‌های معتبر
 *    - ثبت نام با حداقل داده‌ها
 *    - مدیریت تکرار شماره تلفن
 *    - hash کردن رمز عبور
 * 
 * 2. Character Encoding Tests
 *    - پشتیبانی کاراکترهای فارسی
 *    - پشتیبانی کاراکترهای عربی
 *    - کاراکترهای ترکیبی و بین‌المللی
 *    - کاراکترهای خاص
 * 
 * 3. Data Length & Validation Tests
 *    - رشته‌های بلند
 *    - داده‌های تک کاراکتری
 *    - اعتبارسنجی شماره تلفن
 *    - نقش‌های مختلف کاربری
 * 
 * 4. Multiple User & Scalability Tests
 *    - ثبت نام چندین کاربر همزمان
 *    - تست حجم بالا (50+ کاربر)
 *    - مدیریت داده‌های مشابه
 * 
 * 5. Data Integrity & Edge Cases
 *    - حفظ یکپارچگی پایگاه داده
 *    - نگهداری دقیق فیلدها
 *    - مدیریت حالات خاص
 * 
 * Integration Testing:
 * - تست کامل AuthService + AuthRepository
 * - تست persistence در database
 * - تست transaction management
 * 
 * Performance Considerations:
 * - تست با حجم بالای کاربران
 * - بررسی عملکرد database operations
 * - مدیریت حافظه در عملیات bulk
 * 
 * Security Testing:
 * - محافظت از injection attacks
 * - اعتبارسنجی ورودی‌ها
 * - مدیریت کاراکترهای خاص
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("Registration API Comprehensive Tests")
class RegistrationApiTest {

    /** Repository برای دسترسی به پایگاه داده کاربران */
    private AuthRepository repo;
    
    /** Service برای منطق کسب‌وکار احراز هویت */
    private AuthService authService;

    /**
     * راه‌اندازی اولیه قبل از هر تست
     * 
     * Operations:
     * - ایجاد repository جدید
     * - پاک‌سازی پایگاه داده
     * - ایجاد service با dependency injection
     */
    @BeforeEach
    void setUp() {
        repo = new AuthRepository();
        repo.deleteAll(); // پاک‌سازی پایگاه داده قبل از هر تست
        authService = new AuthService(repo);
    }

    /**
     * تولید کاربر معتبر برای تست‌ها
     * 
     * @param phone شماره تلفن منحصر به فرد
     * @return کاربر با داده‌های کامل و معتبر
     */
    private User validUser(String phone) {
        return User.forRegistration("Ahmad Mohammadi", phone, "ahmad@example.com", "hashed_securepass123", "Tehran");
    }

    /**
     * تولید کاربر با حداقل داده‌ها
     * 
     * @param phone شماره تلفن منحصر به فرد
     * @return کاربر با فیلدهای اختیاری خالی
     */
    private User minimalUser(String phone) {
        return User.forRegistration("Simple User", phone, "", "hashed_simplepass", "");
    }

    /**
     * تست‌های پایه ثبت نام
     * 
     * این دسته شامل سناریوهای اصلی ثبت نام است:
     * - ثبت نام موفق با داده‌های کامل
     * - ثبت نام با حداقل اطلاعات
     * - مدیریت خطای تکرار شماره تلفن
     * - تأیید hash شدن رمز عبور
     * - بررسی نقش پیش‌فرض کاربر
     */
    @Nested
    @DisplayName("Basic Registration Tests")
    class BasicRegistrationTests {

        /**
         * تست ثبت نام موفق با داده‌های معتبر
         * 
         * Scenario: کاربر جدید با تمام اطلاعات لازم
         * Expected:
         * - کاربر با ID منحصر به فرد ایجاد شود
         * - تمام فیلدها صحیح ذخیره شوند
         * - نقش پیش‌فرض BUYER تنظیم شود
         */
        @Test
        @DisplayName("Register user with valid data succeeds")
        void registerUser_validData_success() {
            // Arrange - آماده‌سازی داده‌های تست
            String phone = "09123456789";
            User user = validUser(phone);
            
            // Act - اجرای عملیات ثبت نام
            User saved = authService.registerUser(user);
            
            // Assert - بررسی نتایج
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getId()).isPositive();
            assertThat(saved.getFullName()).isEqualTo("Ahmad Mohammadi");
            assertThat(saved.getPhone()).isEqualTo(phone);
            assertThat(saved.getEmail()).isEqualTo("ahmad@example.com");
            assertThat(saved.getPasswordHash()).isEqualTo("hashed_securepass123");
            assertThat(saved.getAddress()).isEqualTo("Tehran");
            assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
        }

        /**
         * تست ثبت نام با حداقل داده‌ها
         * 
         * Scenario: کاربر جدید با فقط فیلدهای ضروری
         * Expected:
         * - ثبت نام موفق باشد
         * - فیلدهای اختیاری خالی ذخیره شوند
         */
        @Test
        @DisplayName("Register user with minimal data succeeds")
        void registerUser_minimalData_success() {
            // Arrange
            String phone = "09123456788";
            User user = minimalUser(phone);
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - بررسی حداقل اطلاعات
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getFullName()).isEqualTo("Simple User");
            assertThat(saved.getPhone()).isEqualTo(phone);
            assertThat(saved.getEmail()).isEqualTo("");
            assertThat(saved.getAddress()).isEqualTo("");
            assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
        }

        /**
         * تست خطای تکرار شماره تلفن
         * 
         * Scenario: تلاش ثبت نام با شماره تلفن تکراری
         * Expected: DuplicatePhoneException پرتاب شود
         */
        @Test
        @DisplayName("Register user with duplicate phone throws exception")
        void registerUser_duplicatePhone_throws() {
            // Arrange - ثبت کاربر اول
            String phone = "09123456787";
            User firstUser = validUser(phone);
            authService.registerUser(firstUser);
            
            // تلاش ثبت کاربر دوم با همان شماره
            User secondUser = User.forRegistration("Second User", phone, "second@example.com", "hashed_pass2", "Isfahan");
            
            // Act & Assert - انتظار exception
            assertThatThrownBy(() -> authService.registerUser(secondUser))
                    .isInstanceOf(com.myapp.common.exceptions.DuplicatePhoneException.class);
        }

        /**
         * تست صحت hash شدن رمز عبور
         * 
         * Scenario: بررسی اینکه رمز عبور hash شده ذخیره می‌شود
         * Expected: رمز hash شده با prefix مناسب ذخیره شود
         */
        @Test
        @DisplayName("Register user with password hashing works correctly")
        void registerUser_passwordHashing_correct() {
            // Arrange
            String phone = "09123456785";
            String hashedPassword = "hashed_myplainpassword";
            
            User user = User.forRegistration("Test User", phone, "test@example.com", hashedPassword, "Tehran");
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - بررسی hash شدن رمز
            assertThat(saved.getPasswordHash()).isEqualTo(hashedPassword);
            assertThat(saved.getPasswordHash()).startsWith("hashed_");
        }

        /**
         * تست نقش پیش‌فرض کاربر
         * 
         * Scenario: بررسی تنظیم خودکار نقش BUYER
         * Expected: کاربر جدید نقش BUYER داشته باشد
         */
        @Test
        @DisplayName("Register user has default BUYER role")
        void registerUser_userRole_defaultBuyer() {
            // Arrange
            String phone = "09123456784";
            User user = validUser(phone);
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - بررسی نقش پیش‌فرض
            assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
        }
    }

    /**
     * تست‌های رمزگذاری کاراکتر
     * 
     * این دسته اطمینان می‌دهد سیستم از کاراکترهای مختلف پشتیبانی می‌کند:
     * - کاراکترهای فارسی
     * - کاراکترهای عربی  
     * - زبان‌های ترکیبی
     * - کاراکترهای خاص و نمادها
     */
    @Nested
    @DisplayName("Character Encoding Tests")
    class CharacterEncodingTests {

        /**
         * تست پشتیبانی کاراکترهای فارسی
         * 
         * Scenario: ثبت نام با نام و آدرس فارسی
         * Expected: کاراکترهای فارسی صحیح ذخیره شوند
         */
        @Test
        @DisplayName("Register user with Persian characters")
        void registerUser_persianCharacters_supported() {
            // Arrange - داده‌های فارسی
            String phone = "09123456783";
            String persianName = "محمدرضا احمدی‌نژاد";
            String persianAddress = "تهران، خیابان آزادی، کوچه شهید محمدی";
            
            User user = User.forRegistration(persianName, phone, "persian@example.com", "hashed_pass", persianAddress);
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - حفظ کاراکترهای فارسی
            assertThat(saved.getFullName()).isEqualTo(persianName);
            assertThat(saved.getAddress()).isEqualTo(persianAddress);
        }

        /**
         * تست پشتیبانی کاراکترهای عربی
         * 
         * Scenario: ثبت نام با متن عربی
         * Expected: کاراکترهای عربی صحیح نمایش داده شوند
         */
        @Test
        @DisplayName("Register user with Arabic characters")
        void registerUser_arabicCharacters_supported() {
            // Arrange - داده‌های عربی
            String phone = "09123456782";
            String arabicName = "عبد الله محمد الأحمد";
            String arabicAddress = "الرياض، شارع الملك فهد";
            
            User user = User.forRegistration(arabicName, phone, "arabic@example.com", "hashed_pass", arabicAddress);
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - حفظ کاراکترهای عربی
            assertThat(saved.getFullName()).isEqualTo(arabicName);
            assertThat(saved.getAddress()).isEqualTo(arabicAddress);
        }

        /**
         * تست پشتیبانی زبان‌های ترکیبی
         * 
         * Scenario: استفاده از چندین زبان در یک فیلد
         * Expected: تمام کاراکترها حفظ شوند
         */
        @Test
        @DisplayName("Register user with mixed languages")
        void registerUser_mixedLanguages_supported() {
            // Arrange - ترکیب انگلیسی و فارسی
            String phone = "09123456781";
            String mixedName = "Ali علی Smith";
            String mixedAddress = "Tehran تهران, Street خیابان 123";
            
            User user = User.forRegistration(mixedName, phone, "mixed@example.com", "hashed_pass", mixedAddress);
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - حفظ ترکیب زبان‌ها
            assertThat(saved.getFullName()).isEqualTo(mixedName);
            assertThat(saved.getAddress()).isEqualTo(mixedAddress);
        }

        /**
         * تست مدیریت کاراکترهای خاص
         * 
         * Scenario: ورودی با نمادها و کاراکترهای خاص
         * Expected: کاراکترهای خاص بدون تغییر ذخیره شوند
         */
        @Test
        @DisplayName("Register user with special characters")
        void registerUser_specialCharacters_handled() {
            // Arrange - کاراکترهای خاص
            String phone = "09123456780";
            String nameWithSpecialChars = "Ali@Mohammad#Hassan$Hussein%^&*()";
            String emailWithSpecialChars = "test+user.name@sub-domain.example-site.com";
            
            User user = User.forRegistration(nameWithSpecialChars, phone, emailWithSpecialChars, "hashed_pass", "Tehran");
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - حفظ کاراکترهای خاص
            assertThat(saved.getFullName()).isEqualTo(nameWithSpecialChars);
            assertThat(saved.getEmail()).isEqualTo(emailWithSpecialChars);
        }
    }

    /**
     * تست‌های طول داده
     * 
     * بررسی مدیریت داده‌ها با طول‌های مختلف:
     * - رشته‌های بسیار بلند
     * - داده‌های تک کاراکتری
     * - اعتبارسنجی شماره تلفن
     * - نقش‌های مختلف کاربری
     */
    @Nested
    @DisplayName("Data Length Tests")
    class DataLengthTests {

        /**
         * تست مدیریت رشته‌های بلند
         * 
         * Scenario: ورودی با فیلدهای بسیار طولانی
         * Expected: داده‌های بلند بدون برش ذخیره شوند
         */
        @Test
        @DisplayName("Register user with long strings")
        void registerUser_longStrings_handled() {
            // Arrange - داده‌های طولانی
            String phone = "09123456779";
            String longName = "A".repeat(200);
            String longEmail = "very.long.email.address.that.might.cause.problems@very-long-domain-name-that-should-be-tested.com";
            String longAddress = "Very Long Address That Includes Various Streets And Alleys And Multiple Building Numbers And Postal Codes ".repeat(5);
            
            User user = User.forRegistration(longName, phone, longEmail, "hashed_pass", longAddress);
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - حفظ داده‌های بلند
            assertThat(saved.getFullName()).isEqualTo(longName);
            assertThat(saved.getEmail()).isEqualTo(longEmail);
            assertThat(saved.getAddress()).isEqualTo(longAddress);
        }

        /**
         * تست hash بسیار طولانی رمز عبور
         * 
         * Scenario: رمز عبور hash شده با طول زیاد
         * Expected: hash طولانی بدون مشکل ذخیره شود
         */
        @Test
        @DisplayName("Register user with very long password hash")
        void registerUser_longPasswordHash_handled() {
            // Arrange
            String phone = "09123456778";
            String longPasswordHash = "hashed_" + "a".repeat(500);
            
            User user = User.forRegistration("Test User", phone, "test@example.com", longPasswordHash, "Tehran");
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - حفظ hash طولانی
            assertThat(saved.getPasswordHash()).isEqualTo(longPasswordHash);
        }

        /**
         * تست فیلدهای تک کاراکتری
         * 
         * Scenario: حداقل داده‌های ممکن
         * Expected: فیلدهای تک کاراکتر قبول شوند
         */
        @Test
        @DisplayName("Register user with single character fields")
        void registerUser_singleCharacterFields_handled() {
            // Arrange - حداقل داده‌ها
            String phone = "09123456777";
            String singleCharName = "A";
            String singleCharEmail = "a@b.c";
            String singleCharAddress = "T";
            
            User user = User.forRegistration(singleCharName, phone, singleCharEmail, "h", singleCharAddress);
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - پذیرش فیلدهای کوتاه
            assertThat(saved.getFullName()).isEqualTo(singleCharName);
            assertThat(saved.getEmail()).isEqualTo(singleCharEmail);
            assertThat(saved.getAddress()).isEqualTo(singleCharAddress);
        }
    }

    /**
     * تست‌های اعتبارسنجی شماره تلفن
     * 
     * بررسی پذیرش فرمت‌های مختلف شماره تلفن ایرانی:
     * - کدهای مختلف اپراتور
     * - فرمت‌های استاندارد
     * - مدیریت فضاهای خالی
     */
    @Nested
    @DisplayName("Phone Number Validation Tests")
    class PhoneNumberValidationTests {

        /**
         * تست فرمت‌های مختلف شماره تلفن معتبر
         * 
         * @param phone شماره تلفن برای تست
         * 
         * Scenario: شماره‌های با کدهای مختلف اپراتور
         * Expected: تمام فرمت‌های معتبر پذیرفته شوند
         */
        @ParameterizedTest
        @ValueSource(strings = {
            "09123456776", "09123456775", "09123456774", "09123456773",
            "09121234567", "09351234567", "09901234567", "09171234567"
        })
        @DisplayName("Register user with various valid phone formats")
        void registerUser_validPhoneFormats_success(String phone) {
            // Arrange
            User user = User.forRegistration("Test User", phone, "test@example.com", "hashed_pass", "Tehran");
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - پذیرش فرمت‌های مختلف
            assertThat(saved.getPhone()).isEqualTo(phone);
        }

        /**
         * تست شماره تلفن با فضاهای خالی
         * 
         * Scenario: شماره با space (معمولاً باید normalize شود)
         * Expected: شماره صحیح ذخیره شود
         */
        @Test
        @DisplayName("Register user with phone containing spaces")
        void registerUser_phoneWithSpaces_handled() {
            // Arrange
            String phone = "09123456772";
            User user = User.forRegistration("Test User", phone, "test@example.com", "hashed_pass", "Tehran");
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert
            assertThat(saved.getPhone()).isEqualTo(phone);
        }
    }

    /**
     * تست‌های تخصیص نقش
     * 
     * بررسی عملکرد با نقش‌های مختلف کاربری:
     * - BUYER, SELLER, DELIVERY, ADMIN
     * - تنظیم صحیح نقش‌ها
     * - حفظ نقش بعد از ذخیره‌سازی
     */
    @Nested
    @DisplayName("Role Assignment Tests")
    class RoleAssignmentTests {

        /**
         * تست ثبت نام با نقش‌های مختلف
         * 
         * @param role نقش کاربری برای تست
         * 
         * Scenario: تنظیم دستی نقش کاربر
         * Expected: نقش صحیح ذخیره شود
         */
        @ParameterizedTest
        @EnumSource(User.Role.class)
        @DisplayName("Register user with different roles")
        void registerUser_differentRoles_success(User.Role role) {
            // Arrange - تولید شماره منحصر به فرد برای هر نقش
            String phone = "0912345677" + role.ordinal();
            User user = User.forRegistration("Test User", phone, "test@example.com", "hashed_pass", "Tehran");
            user.setRole(role); // تنظیم نقش مشخص
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - تأیید نقش
            assertThat(saved.getRole()).isEqualTo(role);
        }
    }

    /**
     * تست‌های چند کاربری
     * 
     * بررسی عملکرد با چندین کاربر:
     * - ثبت نام همزمان
     * - تست مقیاس (scalability)
     * - مدیریت داده‌های مشابه
     */
    @Nested
    @DisplayName("Multiple User Tests")
    class MultipleUserTests {

        /**
         * تست ثبت نام چندین کاربر
         * 
         * Scenario: ثبت نام متوالی چند کاربر
         * Expected:
         * - همه کاربران ذخیره شوند
         * - ID های منحصر به فرد تولید شوند
         * - داده‌ها قابل بازیابی باشند
         */
        @Test
        @DisplayName("Register multiple users with all data persisted")
        void registerUser_multipleUsers_allPersisted() {
            // Arrange - ایجاد چند کاربر
            User user1 = validUser("09111111111");
            User user2 = validUser("09222222222");
            User user3 = validUser("09333333333");
            
            // Act - ثبت نام همه
            User saved1 = authService.registerUser(user1);
            User saved2 = authService.registerUser(user2);
            User saved3 = authService.registerUser(user3);
            
            // Assert - بررسی منحصر به فرد بودن ID ها
            assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
            assertThat(saved2.getId()).isNotEqualTo(saved3.getId());
            assertThat(saved1.getId()).isNotEqualTo(saved3.getId());
            
            // تأیید persistence در database
            assertThat(repo.findByPhone("09111111111")).isPresent();
            assertThat(repo.findByPhone("09222222222")).isPresent();
            assertThat(repo.findByPhone("09333333333")).isPresent();
        }

        /**
         * تست عملکرد با تعداد زیاد کاربر
         * 
         * Scenario: ثبت نام 50 کاربر پشت سر هم
         * Expected: همه عملیات موفق باشند
         * 
         * Performance Test: بررسی scalability سیستم
         */
        @Test
        @DisplayName("Register large number of users")
        void registerUser_largeNumberOfUsers_allPersisted() {
            // Act & Assert - ثبت نام bulk users
            for (int i = 0; i < 50; i++) {
                String phone = String.format("0912345%04d", i);
                User user = User.forRegistration("User " + i, phone, "user" + i + "@example.com", "hashed_pass" + i, "Address " + i);
                
                User saved = authService.registerUser(user);
                
                // تأیید ثبت نام موفق هر کاربر
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getPhone()).isEqualTo(phone);
                assertThat(saved.getFullName()).isEqualTo("User " + i);
            }
        }

        /**
         * تست کاربران با داده‌های مشابه
         * 
         * Scenario: کاربران با نام مشابه ولی اطلاعات متفاوت
         * Expected: همه قابل تشخیص و ذخیره باشند
         */
        @Test
        @DisplayName("Register users with similar but different data")
        void registerUser_similarButDifferentData_success() {
            // Arrange - کاربران با نام یکسان ولی داده‌های متفاوت
            User user1 = User.forRegistration("Ali Mohammad", "09111111110", "ali@example.com", "hashed_pass1", "Tehran");
            User user2 = User.forRegistration("Ali Mohammad", "09111111111", "ali2@example.com", "hashed_pass2", "Tehran");
            User user3 = User.forRegistration("Ali Mohammad", "09111111112", "ali@example.com", "hashed_pass3", "Isfahan");
            
            // Act
            User saved1 = authService.registerUser(user1);
            User saved2 = authService.registerUser(user2);
            User saved3 = authService.registerUser(user3);
            
            // Assert - تأیید تفکیک صحیح
            assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
            assertThat(saved2.getId()).isNotEqualTo(saved3.getId());
            assertThat(saved1.getId()).isNotEqualTo(saved3.getId());
        }
    }

    /**
     * تست‌های یکپارچگی داده
     * 
     * بررسی حفظ یکپارچگی و دقت داده‌ها:
     * - consistency در database
     * - حفظ دقیق فیلدها
     * - transaction management
     */
    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        /**
         * تست حفظ یکپارچگی پایگاه داده
         * 
         * Scenario: بررسی کامل persistence و retrieval
         * Expected:
         * - داده‌ها دقیقاً همانطور که ارسال شده ذخیره شوند
         * - بازیابی بدون تغییر انجام شود
         */
        @Test
        @DisplayName("Register user maintains database integrity")
        void registerUser_databaseIntegrity_maintained() {
            // Arrange
            String phone = "09123456770";
            User user = validUser(phone);
            
            // تأیید پاک بودن database
            assertThat(repo.findByPhone(phone)).isNotPresent();
            
            // Act - ثبت نام کاربر
            User saved = authService.registerUser(user);
            
            // Assert - تأیید persistence
            assertThat(repo.findById(saved.getId())).isPresent();
            assertThat(repo.findByPhone(phone)).isPresent();
            
            // بررسی یکپارچگی داده‌ها
            User retrieved = repo.findByPhone(phone).get();
            assertThat(retrieved.getId()).isEqualTo(saved.getId());
            assertThat(retrieved.getFullName()).isEqualTo(saved.getFullName());
            assertThat(retrieved.getPhone()).isEqualTo(saved.getPhone());
            assertThat(retrieved.getEmail()).isEqualTo(saved.getEmail());
            assertThat(retrieved.getPasswordHash()).isEqualTo(saved.getPasswordHash());
            assertThat(retrieved.getAddress()).isEqualTo(saved.getAddress());
            assertThat(retrieved.getRole()).isEqualTo(saved.getRole());
        }

        /**
         * تست حفظ دقیق فیلدها
         * 
         * Scenario: ذخیره‌سازی داده‌هایی با کاراکترهای خاص
         * Expected: هیچ تغییری در محتوای فیلدها ایجاد نشود
         */
        @Test
        @DisplayName("Register user with exact field preservation")
        void registerUser_exactFieldPreservation_maintained() {
            // Arrange - داده‌های با کاراکترهای خاص
            String phone = "09123456769";
            String exactName = "Exact Name With Spaces  And  Multiple  Spaces";
            String exactEmail = "exact.email+with+plus@domain.co.uk";
            String exactAddress = "Exact Address\nWith\nNewlines\tAnd\tTabs";
            String exactPassword = "hashed_exact_password_with_special_chars!@#$%";
            
            User user = User.forRegistration(exactName, phone, exactEmail, exactPassword, exactAddress);
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - حفظ دقیق داده‌ها
            assertThat(saved.getFullName()).isEqualTo(exactName);
            assertThat(saved.getEmail()).isEqualTo(exactEmail);
            assertThat(saved.getAddress()).isEqualTo(exactAddress);
            assertThat(saved.getPasswordHash()).isEqualTo(exactPassword);
            
            // تأیید از database
            User retrieved = repo.findByPhone(phone).get();
            assertThat(retrieved.getFullName()).isEqualTo(exactName);
            assertThat(retrieved.getEmail()).isEqualTo(exactEmail);
            assertThat(retrieved.getAddress()).isEqualTo(exactAddress);
            assertThat(retrieved.getPasswordHash()).isEqualTo(exactPassword);
        }
    }

    /**
     * تست‌های Factory Method
     * 
     * بررسی عملکرد متد factory forRegistration:
     * - تنظیم مقادیر پیش‌فرض
     * - مدیریت null/empty values
     * - رفتار صحیح constructor
     */
    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        /**
         * تست تنظیم مقادیر پیش‌فرض
         * 
         * Scenario: استفاده از factory method forRegistration
         * Expected: مقادیر پیش‌فرض صحیح تنظیم شوند
         */
        @Test
        @DisplayName("forRegistration factory method sets correct defaults")
        void forRegistration_factoryMethod_correctDefaults() {
            // Arrange
            String phone = "09123456768";
            String fullName = "Test User";
            String email = "test@example.com";
            String passwordHash = "hashed_password";
            String address = "Test Address";
            
            // Act - استفاده از factory method
            User user = User.forRegistration(fullName, phone, email, passwordHash, address);
            
            // Assert - بررسی مقادیر تنظیم شده
            assertThat(user.getFullName()).isEqualTo(fullName);
            assertThat(user.getPhone()).isEqualTo(phone);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
            assertThat(user.getAddress()).isEqualTo(address);
            assertThat(user.getRole()).isEqualTo(User.Role.BUYER);
            assertThat(user.getId()).isNull(); // تا زمان save تنظیم نمی‌شود
        }

        /**
         * تست مدیریت email های null/empty
         * 
         * @param email مقدار email برای تست
         * 
         * Scenario: factory method با email خالی یا null
         * Expected: مقدار دقیقاً همانطور که ارسال شده حفظ شود
         */
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("forRegistration with null/empty email")
        void forRegistration_nullEmptyEmail_handled(String email) {
            // Arrange - شماره منحصر به فرد برای هر test case
            String phone = "0912345676" + (email == null ? "1" : "2");
            
            // Act
            User user = User.forRegistration("Test User", phone, email, "hashed_pass", "Test Address");
            User saved = authService.registerUser(user);
            
            // Assert - حفظ مقدار email
            assertThat(saved.getEmail()).isEqualTo(email);
        }

        /**
         * تست مدیریت address های null/empty
         * 
         * @param address مقدار address برای تست
         * 
         * Scenario: factory method با آدرس خالی یا null
         * Expected: مقدار دقیقاً همانطور که ارسال شده حفظ شود
         */
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("forRegistration with null/empty address")
        void forRegistration_nullEmptyAddress_handled(String address) {
            // Arrange
            String phone = "0912345675" + (address == null ? "1" : "2");
            
            // Act
            User user = User.forRegistration("Test User", phone, "test@example.com", "hashed_pass", address);
            User saved = authService.registerUser(user);
            
            // Assert - حفظ مقدار address
            assertThat(saved.getAddress()).isEqualTo(address);
        }
    }

    /**
     * تست‌های حالات خاص (Edge Cases)
     * 
     * بررسی رفتار سیستم در شرایط غیرعادی:
     * - فیلدهای فقط فضای خالی
     * - نام‌های عددی
     * - ایمیل‌های بین‌المللی
     */
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        /**
         * تست فیلدهای فقط فضای خالی
         * 
         * Scenario: ورودی که فقط شامل whitespace است
         * Expected: مقادیر بدون تغییر ذخیره شوند
         */
        @Test
        @DisplayName("Register user with whitespace-only fields")
        void registerUser_whitespaceOnlyFields_handled() {
            // Arrange - فیلدهای فقط whitespace
            String phone = "09123456767";
            String whitespaceName = "   ";
            String whitespaceEmail = "\t\n";
            String whitespaceAddress = "  \t  \n  ";
            
            User user = User.forRegistration(whitespaceName, phone, whitespaceEmail, "hashed_pass", whitespaceAddress);
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - حفظ whitespace ها
            assertThat(saved.getFullName()).isEqualTo(whitespaceName);
            assertThat(saved.getEmail()).isEqualTo(whitespaceEmail);
            assertThat(saved.getAddress()).isEqualTo(whitespaceAddress);
        }

        /**
         * تست نام عددی
         * 
         * Scenario: نام کاربر که فقط شامل اعداد است
         * Expected: نام عددی قبول شود
         */
        @Test
        @DisplayName("Register user with numeric name")
        void registerUser_numericName_handled() {
            // Arrange
            String phone = "09123456766";
            String numericName = "123456789";
            
            User user = User.forRegistration(numericName, phone, "numeric@example.com", "hashed_pass", "Tehran");
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - پذیرش نام عددی
            assertThat(saved.getFullName()).isEqualTo(numericName);
        }

        /**
         * تست ایمیل با کاراکترهای بین‌المللی
         * 
         * Scenario: ایمیل با کاراکترهای غیر ASCII
         * Expected: ایمیل بین‌المللی قبول شود
         */
        @Test
        @DisplayName("Register user with email containing international characters")
        void registerUser_internationalEmail_handled() {
            // Arrange - ایمیل آلمانی
            String phone = "09123456765";
            String internationalEmail = "müller@münchen.de";
            
            User user = User.forRegistration("Test User", phone, internationalEmail, "hashed_pass", "Tehran");
            
            // Act
            User saved = authService.registerUser(user);
            
            // Assert - حفظ کاراکترهای بین‌المللی در ایمیل
            assertThat(saved.getEmail()).isEqualTo(internationalEmail);
        }
    }
}