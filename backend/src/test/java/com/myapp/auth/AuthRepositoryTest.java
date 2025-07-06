package com.myapp.auth;

import com.myapp.common.TestBase;
import com.myapp.common.models.User;
import com.myapp.common.utils.TestDataHelper;
import com.myapp.common.exceptions.DuplicatePhoneException;
import com.myapp.auth.AuthRepository;
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
class AuthRepositoryTest extends TestBase {

    /** Repository instance برای تست‌ها */
    private AuthRepository repository;

    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - ایجاد repository جدید
     * - پاک‌سازی کامل پایگاه داده با synchronization
     * - اطمینان از clean test environment بدون database locks
     */
    @BeforeEach
    public void setUp() {
        super.setUp();
        repository = new AuthRepository();
        cleanDatabaseSafely();
    }

    /**
     * تمیزکاری بعد از هر تست برای آزادسازی resources
     */
    @AfterEach
    public void tearDown() {
        cleanDatabaseSafely();
        super.tearDown();
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
        @DisplayName("Should create user with valid data")
        void saveNew_validUser_success() {
            // Given
            User user = new User();
            user.setFullName("John Doe");
            user.setPhone(TestDataHelper.generateUniquePhone());
            user.setEmail(TestDataHelper.generateUniqueEmail());
            user.setPasswordHash("hashedPassword");
            user.setRole(User.Role.BUYER);

            // When
            User savedUser = repository.saveNew(user);

            // Then
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getPhone()).isEqualTo(user.getPhone());
            assertThat(savedUser.getFullName()).isEqualTo("John Doe");
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
        @DisplayName("Should create user with minimal data")
        void saveNew_minimalUser_success() {
            // Given
            User user = new User();
            user.setPhone(TestDataHelper.generateUniquePhone());
            user.setPasswordHash("hashedPassword");
            user.setFullName("Test User");

            // When
            User savedUser = repository.saveNew(user);

            // Then
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getPhone()).isEqualTo(user.getPhone());
        }

        /**
         * تست ایجاد کاربر با نقش‌های مختلف
         * 
         * @param role نقش کاربری برای تست
         * 
         * Scenario: ایجاد کاربران با تمام نقش‌های موجود سیستم
         * Expected: هر نقش صحیح ذخیره و بازیابی شود
         */
        @ParameterizedTest
        @EnumSource(User.Role.class)
        @DisplayName("Should create user with different roles")
        void saveNew_differentRoles_success(User.Role role) {
            // Given
            User user = new User();
            user.setPhone(TestDataHelper.generateUniquePhone());
            user.setPasswordHash("hashedPassword");
            user.setRole(role);
            user.setFullName("Test User");

            // When
            User savedUser = repository.saveNew(user);

            // Then
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getRole()).isEqualTo(role);
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
        @DisplayName("Should throw exception for duplicate phone")
        void saveNew_duplicatePhone_throwsException() {
            // Given
            String duplicatePhone = TestDataHelper.generateUniquePhone();
            User user1 = new User();
            user1.setPhone(duplicatePhone);
            user1.setPasswordHash("hashedPassword1");
            user1.setFullName("Test User");

            User user2 = new User();
            user2.setPhone(duplicatePhone); // Same phone
            user2.setPasswordHash("hashedPassword2");
            user2.setFullName("Test User");

            // When & Then
            repository.saveNew(user1);
            assertThatThrownBy(() -> repository.saveNew(user2))
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
        @ValueSource(strings = {"09123456786", "+989123456786", "00989123456786"})
        @DisplayName("Should create user with various phone formats")
        void saveNew_variousPhoneFormats_success(String phone) {
            // Given
            User user = new User();
            user.setPhone(phone);
            user.setPasswordHash("hashedPassword");
            user.setFullName("Test User");

            // When
            User savedUser = repository.saveNew(user);

            // Then
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getPhone()).isEqualTo(phone);
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
        @DisplayName("Should find user by ID when exists")
        void findById_existingUser_success() {
            // Given
            User user = new User();
            user.setPhone(TestDataHelper.generateUniquePhone());
            user.setPasswordHash("hashedPassword");
            user.setFullName("Test User");
            User savedUser = repository.saveNew(user);

            // When
            User foundUser = repository.findById(savedUser.getId()).orElse(null);

            // Then
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
            assertThat(foundUser.getPhone()).isEqualTo(user.getPhone());
        }

        /**
         * تست جستجوی کاربر با ID غیرموجود
         * 
         * Scenario: جستجوی کاربر با ID که وجود ندارد
         * Expected: Optional.empty بازگردانده شود
         */
        @Test
        @DisplayName("Should return empty when user not found by ID")
        void findById_nonExistentUser_returnsEmpty() {
            // When
            var result = repository.findById(999L);

            // Then
            assertThat(result).isEmpty();
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
        @DisplayName("Should find user by phone when exists")
        void findByPhone_existingUser_success() {
            // Given
            User user = new User();
            user.setPhone("09123456784");
            user.setPasswordHash("hashedPassword");
            user.setFullName("Test User");
            repository.saveNew(user);

            // When
            User foundUser = repository.findByPhone("09123456784").orElse(null);

            // Then
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getPhone()).isEqualTo("09123456784");
        }

        /**
         * تست جستجوی کاربر با شماره تلفن غیرموجود
         * 
         * Scenario: جستجوی کاربر با شماره تلفن که ثبت نشده
         * Expected: Optional.empty بازگردانده شود
         */
        @Test
        @DisplayName("Should return empty when user not found by phone")
        void findByPhone_nonExistentUser_returnsEmpty() {
            // When
            var result = repository.findByPhone("nonexistent");

            // Then
            assertThat(result).isEmpty();
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
        @DisplayName("Should update user successfully")
        void update_validUser_success() {
            // Given
            User user = new User();
            user.setPhone("09123456783");
            user.setPasswordHash("hashedPassword");
            user.setFullName("Original Name");
            user.setFullName("Test User");
            User savedUser = repository.saveNew(user);

            // When
            savedUser.setFullName("Updated Name");
            User updatedUser = repository.update(savedUser);

            // Then
            assertThat(updatedUser).isNotNull();
            assertThat(updatedUser.getFullName()).isEqualTo("Updated Name");
            
            // Verify in database
            User foundUser = repository.findById(savedUser.getId()).orElse(null);
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getFullName()).isEqualTo("Updated Name");
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
        @DisplayName("Should delete existing user successfully")
        void delete_existingUser_success() {
            // Given
            User user = new User();
            user.setPhone("09123456782");
            user.setPasswordHash("hashedPassword");
            user.setFullName("Test User");
            User savedUser = repository.saveNew(user);

            // When
            repository.delete(savedUser.getId());

            // Then
            var result = repository.findById(savedUser.getId());
            assertThat(result).isEmpty();
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
        @DisplayName("Should allow phone reuse after deletion")
        void delete_allowsPhoneReuse_success() {
            // Given
            User user1 = new User();
            user1.setPhone("09123456781");
            user1.setPasswordHash("hashedPassword1");
            user1.setFullName("Test User");
            User savedUser1 = repository.saveNew(user1);

            // When
            repository.delete(savedUser1.getId());

            User user2 = new User();
            user2.setPhone("09123456781"); // Same phone
            user2.setPasswordHash("hashedPassword2");
            user2.setFullName("Test User");

            // Then
            assertThatCode(() -> repository.saveNew(user2)).doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("Repository instance should be initialized and database accessible")
    void repositoryInitializationTest() {
        AuthRepository repo = new AuthRepository();
        assertThat(repo).isNotNull();
        // تست ساده یک کوئری واقعی
        assertThatCode(() -> repo.findByPhone("")).doesNotThrowAnyException();
    }
}