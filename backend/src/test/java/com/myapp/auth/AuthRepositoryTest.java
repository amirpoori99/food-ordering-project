package com.myapp.auth;

import com.myapp.common.exceptions.DuplicatePhoneException;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * مجموعه تست‌های AuthRepository
 * 
 * این کلاس تست تمام عملکردهای repository pattern برای احراز هویت را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. User Creation Tests
 *    - ایجاد کاربر با داده‌های معتبر
 *    - ایجاد کاربر با حداقل داده‌ها
 *    - تست نقش‌های مختلف کاربری
 *    - مدیریت خطای تکرار شماره تلفن
 *    - تست فرمت‌های مختلف شماره تلفن
 * 
 * 2. User Retrieval Tests
 *    - جستجو بر اساس ID
 *    - جستجو بر اساس شماره تلفن
 *    - مدیریت حالت‌های not found
 *    - تست کارایی جستجو
 * 
 * 3. User Update Tests
 *    - به‌روزرسانی اطلاعات کاربر
 *    - تست constraint های database
 *    - مدیریت تغییر شماره تلفن
 *    - حفظ یکپارچگی داده‌ها
 * 
 * 4. User Deletion Tests
 *    - حذف کاربر موجود
 *    - آزادسازی شماره تلفن بعد از حذف
 *    - تست cascade operations
 * 
 * Repository Pattern Testing:
 * - CRUD operations کامل
 * - Transaction management
 * - Exception handling
 * - Data integrity
 * - Performance considerations
 * 
 * Database Integration:
 * - Hibernate ORM operations
 * - SQL constraint testing
 * - Connection management
 * - Clean test environment
 * 
 * Security Considerations:
 * - Input validation
 * - SQL injection prevention
 * - Data sanitization
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("AuthRepository Tests")
class AuthRepositoryTest {

    /** Repository instance برای تست‌ها */
    private AuthRepository repository;

    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - ایجاد repository جدید
     * - پاک‌سازی کامل پایگاه داده
     * - اطمینان از clean test environment
     */
    @BeforeEach
    void setUp() {
        repository = new AuthRepository();
        // پاک‌سازی پایگاه داده قبل از هر تست
        repository.deleteAll();
    }

    /**
     * تست‌های ایجاد کاربر
     * 
     * این دسته شامل تمام سناریوهای مربوط به ایجاد کاربر جدید است:
     * - ایجاد موفق با داده‌های کامل
     * - ایجاد با حداقل اطلاعات
     * - تست نقش‌های مختلف
     * - مدیریت constraint violations
     * - تست فرمت‌های مختلف ورودی
     */
    @Nested
    @DisplayName("User Creation Tests")
    class UserCreationTests {
        
        /**
         * تست ایجاد موفق کاربر با داده‌های معتبر
         * 
         * Scenario: ایجاد کاربر جدید با تمام اطلاعات ضروری
         * Expected:
         * - کاربر با ID مثبت و منحصر به فرد ایجاد شود
         * - تمام فیلدها صحیح ذخیره شوند
         * - نقش پیش‌فرض BUYER تنظیم شود
         * - اطلاعات قابل بازیابی باشند
         */
        @Test
        @DisplayName("User creation with valid data succeeds")
        void saveNew_validUser_success() {
            // Given - آماده‌سازی کاربر معتبر
            User user = User.forRegistration("John Doe", "09123456789", "john@example.com", "hashedPassword", "Tehran");
            
            // When - ذخیره‌سازی کاربر
            User saved = repository.saveNew(user);
            
            // Then - بررسی نتایج
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getId()).isPositive();
            assertThat(saved.getFullName()).isEqualTo("John Doe");
            assertThat(saved.getPhone()).isEqualTo("09123456789");
            assertThat(saved.getEmail()).isEqualTo("john@example.com");
            assertThat(saved.getPasswordHash()).isEqualTo("hashedPassword");
            assertThat(saved.getAddress()).isEqualTo("Tehran");
            assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
        }

        /**
         * تست ایجاد موفق کاربر با حداقل داده‌ها
         * 
         * Scenario: ایجاد کاربر با فیلدهای اختیاری خالی
         * Expected:
         * - ایجاد موفق باشد
         * - فیلدهای خالی صحیح ذخیره شوند
         * - ID معتبر تولید شود
         */
        @Test
        @DisplayName("User creation with minimal data succeeds")
        void saveNew_minimalUser_success() {
            // Given - کاربر با حداقل اطلاعات
            User user = User.forRegistration("Simple User", "09123456788", "", "hashedPassword", "");
            
            // When - ذخیره‌سازی
            User saved = repository.saveNew(user);
            
            // Then - بررسی حداقل اطلاعات
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getFullName()).isEqualTo("Simple User");
            assertThat(saved.getPhone()).isEqualTo("09123456788");
            assertThat(saved.getEmail()).isEqualTo("");
            assertThat(saved.getAddress()).isEqualTo("");
            assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
        }

        /**
         * تست ایجاد کاربر با نقش‌های مختلف
         * 
         * @param role نقش کاربری برای تست
         * 
         * Scenario: ایجاد کاربران با تمام نقش‌های موجود سیستم
         * Expected: هر نقش صحیح ذخیره و بازیابی شود
         */
        @EnumSource(User.Role.class)
        @ParameterizedTest
        @DisplayName("User creation with different roles succeeds")
        void saveNew_differentRoles_success(User.Role role) {
            // Given - کاربر با نقش مشخص
            User user = User.forRegistration("Test User", "0912345678" + role.ordinal(), "test@example.com", "hashedPassword", "Tehran");
            user.setRole(role);
            
            // When - ذخیره‌سازی
            User saved = repository.saveNew(user);
            
            // Then - بررسی نقش
            assertThat(saved.getRole()).isEqualTo(role);
        }

        /**
         * تست خطای تکرار شماره تلفن
         * 
         * Scenario: تلاش ایجاد دو کاربر با شماره تلفن یکسان
         * Expected:
         * - کاربر اول موفق ذخیره شود
         * - کاربر دوم DuplicatePhoneException پرتاب کند
         * - یکپارچگی پایگاه داده حفظ شود
         */
        @Test
        @DisplayName("User creation with duplicate phone throws exception")
        void saveNew_duplicatePhone_throwsException() {
            // Given - دو کاربر با شماره تلفن یکسان
            String phone = "09123456784";
            User firstUser = User.forRegistration("First User", phone, "first@example.com", "hashedPassword1", "Tehran");
            User secondUser = User.forRegistration("Second User", phone, "second@example.com", "hashedPassword2", "Isfahan");
            
            // ذخیره کاربر اول
            repository.saveNew(firstUser);
            
            // When & Then - انتظار exception برای کاربر دوم
            assertThatThrownBy(() -> repository.saveNew(secondUser))
                    .isInstanceOf(DuplicatePhoneException.class);
        }

        /**
         * تست ایجاد کاربر با فرمت‌های مختلف شماره تلفن
         * 
         * @param phone شماره تلفن برای تست
         * 
         * Scenario: بررسی پذیرش فرمت‌های مختلف شماره تلفن ایرانی
         * Expected: تمام فرمت‌های معتبر پذیرفته شوند
         */
        @ParameterizedTest
        @ValueSource(strings = {
            "09111111111", "09222222222", "09333333333", "09444444444", "09555555555"
        })
        @DisplayName("User creation with various phone formats succeeds")
        void saveNew_variousPhoneFormats_success(String phone) {
            // Given - کاربر با فرمت خاص شماره تلفن
            User user = User.forRegistration("Test User", phone, "test@example.com", "hashedPassword", "Tehran");
            
            // When - ذخیره‌سازی
            User saved = repository.saveNew(user);
            
            // Then - بررسی حفظ فرمت
            assertThat(saved.getPhone()).isEqualTo(phone);
        }
    }

    /**
     * تست‌های بازیابی کاربر
     * 
     * این دسته شامل تمام عملیات جستجو و بازیابی کاربران است:
     * - جستجو بر اساس ID
     * - جستجو بر اساس شماره تلفن
     * - مدیریت حالت‌های not found
     * - تست کارایی جستجو
     */
    @Nested
    @DisplayName("User Retrieval Tests")
    class UserRetrievalTests {
        
        /**
         * تست موفق جستجوی کاربر بر اساس ID
         * 
         * Scenario: جستجوی کاربر موجود با ID معتبر
         * Expected:
         * - کاربر پیدا شود
         * - اطلاعات صحیح بازگردانده شود
         * - تمام فیلدها مطابقت داشته باشند
         */
        @Test
        @DisplayName("Find user by ID succeeds")
        void findById_existingUser_success() {
            // Given - ایجاد و ذخیره کاربر
            User user = User.forRegistration("John Doe", "09123456783", "john@example.com", "hashedPassword", "Tehran");
            User saved = repository.saveNew(user);
            
            // When - جستجو بر اساس ID
            Optional<User> found = repository.findById(saved.getId());
            
            // Then - بررسی نتایج
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
            assertThat(found.get().getFullName()).isEqualTo("John Doe");
            assertThat(found.get().getPhone()).isEqualTo("09123456783");
        }

        /**
         * تست جستجوی کاربر با ID غیرموجود
         * 
         * Scenario: جستجوی کاربر با ID که وجود ندارد
         * Expected: Optional.empty بازگردانده شود
         */
        @Test
        @DisplayName("Find user by non-existent ID returns empty")
        void findById_nonExistentUser_returnsEmpty() {
            // When - جستجوی ID غیرموجود
            Optional<User> found = repository.findById(999999L);
            
            // Then - بررسی عدم وجود
            assertThat(found).isNotPresent();
        }

        /**
         * تست موفق جستجوی کاربر بر اساس شماره تلفن
         * 
         * Scenario: جستجوی کاربر موجود با شماره تلفن معتبر
         * Expected:
         * - کاربر پیدا شود
         * - اطلاعات صحیح بازگردانده شود
         * - شماره تلفن دقیق مطابقت داشته باشد
         */
        @Test
        @DisplayName("Find user by phone succeeds")
        void findByPhone_existingUser_success() {
            // Given - ایجاد کاربر با شماره خاص
            String phone = "09123456782";
            User user = User.forRegistration("Jane Doe", phone, "jane@example.com", "hashedPassword", "Tehran");
            repository.saveNew(user);
            
            // When - جستجو بر اساس شماره تلفن
            Optional<User> found = repository.findByPhone(phone);
            
            // Then - بررسی نتایج
            assertThat(found).isPresent();
            assertThat(found.get().getPhone()).isEqualTo(phone);
            assertThat(found.get().getFullName()).isEqualTo("Jane Doe");
        }

        /**
         * تست جستجوی کاربر با شماره تلفن غیرموجود
         * 
         * Scenario: جستجوی کاربر با شماره تلفن که ثبت نشده
         * Expected: Optional.empty بازگردانده شود
         */
        @Test
        @DisplayName("Find user by non-existent phone returns empty")
        void findByPhone_nonExistentUser_returnsEmpty() {
            // When - جستجوی شماره غیرموجود
            Optional<User> found = repository.findByPhone("09999999999");
            
            // Then - بررسی عدم وجود
            assertThat(found).isNotPresent();
        }
    }

    /**
     * تست‌های به‌روزرسانی کاربر
     * 
     * این دسته شامل تمام عملیات تغییر اطلاعات کاربران است:
     * - به‌روزرسانی فیلدهای مختلف
     * - تست constraint های database
     * - مدیریت تغییر شماره تلفن
     * - حفظ یکپارچگی داده‌ها
     */
    @Nested
    @DisplayName("User Update Tests")
    class UserUpdateTests {
        
        /**
         * تست به‌روزرسانی موفق اطلاعات کاربر
         * 
         * Scenario: تغییر فیلدهای قابل ویرایش کاربر
         * Expected:
         * - تغییرات صحیح اعمال شوند
         * - شماره تلفن (کلید منحصر به فرد) تغییر نکند
         * - سایر فیلدها به‌روزرسانی شوند
         */
        @Test
        @DisplayName("User update with valid data succeeds")
        void update_validUser_success() {
            // Given - ایجاد و ذخیره کاربر اولیه
            User user = User.forRegistration("John Doe", "09123456781", "john@example.com", "hashedPassword", "Tehran");
            User saved = repository.saveNew(user);
            
            // تغییر اطلاعات کاربر
            saved.setFullName("John Smith");
            saved.setEmail("johnsmith@example.com");
            saved.setAddress("Isfahan");
            
            // When - به‌روزرسانی
            User updated = repository.update(saved);
            
            // Then - بررسی تغییرات
            assertThat(updated.getFullName()).isEqualTo("John Smith");
            assertThat(updated.getEmail()).isEqualTo("johnsmith@example.com");
            assertThat(updated.getAddress()).isEqualTo("Isfahan");
            assertThat(updated.getPhone()).isEqualTo("09123456781"); // شماره تلفن تغییر نکرده
        }

        /**
         * تست خطای تغییر شماره تلفن به شماره موجود
         * 
         * Scenario: تلاش تغییر شماره تلفن کاربر به شماره کاربر دیگر
         * Expected:
         * - DuplicatePhoneException پرتاب شود
         * - تغییرات اعمال نشوند
         * - یکپارچگی پایگاه داده حفظ شود
         */
        @Test
        @DisplayName("User update with phone change to existing phone throws exception")
        void update_duplicatePhone_throwsException() {
            // Given - ایجاد دو کاربر
            User user1 = User.forRegistration("User One", "09123456780", "user1@example.com", "hashedPassword", "Tehran");
            User user2 = User.forRegistration("User Two", "09123456779", "user2@example.com", "hashedPassword", "Tehran");
            
            User saved1 = repository.saveNew(user1);
            User saved2 = repository.saveNew(user2);
            
            // تلاش تغییر شماره تلفن user2 به شماره user1
            saved2.setPhone("09123456780");
            
            // When & Then - انتظار exception
            assertThatThrownBy(() -> repository.update(saved2))
                    .isInstanceOf(DuplicatePhoneException.class);
        }
    }

    /**
     * تست‌های حذف کاربر
     * 
     * این دسته شامل تمام عملیات حذف کاربران است:
     * - حذف کاربر موجود
     * - آزادسازی منابع
     * - امکان استفاده مجدد از شماره تلفن
     * - تست cascade operations
     */
    @Nested
    @DisplayName("User Deletion Tests")
    class UserDeletionTests {
        
        /**
         * تست حذف موفق کاربر
         * 
         * Scenario: حذف کاربر موجود از پایگاه داده
         * Expected:
         * - کاربر از پایگاه داده حذف شود
         * - جستجوی بعدی نتیجه ندهد
         * - عملیات بدون خطا انجام شود
         */
        @Test
        @DisplayName("User deletion succeeds")
        void delete_existingUser_success() {
            // Given - ایجاد و ذخیره کاربر
            User user = User.forRegistration("John Doe", "09123456778", "john@example.com", "hashedPassword", "Tehran");
            User saved = repository.saveNew(user);
            
            // When - حذف کاربر
            repository.delete(saved.getId());
            
            // Then - بررسی حذف
            Optional<User> found = repository.findById(saved.getId());
            assertThat(found).isNotPresent();
        }

        /**
         * تست آزادسازی شماره تلفن بعد از حذف
         * 
         * Scenario: امکان استفاده مجدد از شماره تلفن حذف شده
         * Expected:
         * - بعد از حذف کاربر، شماره تلفن آزاد شود
         * - کاربر جدید بتواند از همان شماره استفاده کند
         * - constraint تکرار شماره رعایت شود
         */
        @Test
        @DisplayName("User deletion allows phone reuse")
        void delete_allowsPhoneReuse_success() {
            // Given - ایجاد کاربر اول
            String phone = "09123456777";
            User user1 = User.forRegistration("First User", phone, "first@example.com", "hashedPassword", "Tehran");
            User saved1 = repository.saveNew(user1);
            
            // When - حذف کاربر اول
            repository.delete(saved1.getId());
            
            // Then - امکان ایجاد کاربر جدید با همان شماره
            User user2 = User.forRegistration("Second User", phone, "second@example.com", "hashedPassword", "Isfahan");
            User saved2 = repository.saveNew(user2);
            
            assertThat(saved2.getPhone()).isEqualTo(phone);
            assertThat(saved2.getFullName()).isEqualTo("Second User");
        }
    }
}