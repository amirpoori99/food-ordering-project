package com.myapp.auth;

import com.myapp.auth.dto.RegisterRequest;
import com.myapp.auth.dto.ProfileResponse;
import com.myapp.auth.dto.UpdateProfileRequest;
import com.myapp.common.exceptions.DuplicatePhoneException;
import com.myapp.common.exceptions.InvalidCredentialsException;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * مجموعه تست‌های جامع AuthController
 * 
 * این کلاس تست تمام عملکردهای کنترلر احراز هویت را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Registration Controller Tests
 *    - ثبت‌نام کاربر جدید
 *    - ثبت‌نام با نقش‌های مختلف
 *    - مدیریت خطای تکرار شماره تلفن
 *    - اعتبارسنجی شماره‌های تلفن مختلف
 * 
 * 2. Login Controller Tests
 *    - ورود موفق کاربر
 *    - مدیریت اعتبارات نامعتبر
 *    - تست ورود با شماره‌های مختلف
 * 
 * 3. Profile Controller Tests
 *    - دریافت پروفایل کاربر
 *    - به‌روزرسانی پروفایل
 *    - مدیریت کاربر یافت نشده
 * 
 * Controller Layer Testing:
 * - Delegation به service layer
 * - Exception propagation
 * - DTO conversion
 * - Parameter validation
 * 
 * Testing Patterns:
 * - Service mocking
 * - Parameterized tests
 * - Nested test organization
 * - Exception assertion
 * - Data verification
 * 
 * Business Logic:
 * - Authentication workflow
 * - User registration flow
 * - Profile management
 * - Role-based operations
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("AuthController Comprehensive Tests")
class AuthControllerTest {

    /** Mock service برای تست‌ها */
    private AuthService service;
    
    /** Controller instance تحت تست */
    private AuthController controller;

    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - ایجاد mock service
     * - initialize کردن controller با mock
     */
    @BeforeEach
    void setUp() {
        service = Mockito.mock(AuthService.class);
        controller = new AuthController(service);
    }

    /**
     * تست‌های کنترلر ثبت‌نام
     * 
     * این دسته شامل تمام عملیات مربوط به ثبت‌نام کاربر جدید:
     * - delegation به service
     * - مدیریت exception ها
     * - تست با نقش‌های مختلف
     * - اعتبارسنجی شماره تلفن
     */
    @Nested
    @DisplayName("Registration Controller Tests")
    class RegistrationControllerTests {

        /**
         * تست موفق delegate کردن ثبت‌نام به service
         * 
         * Scenario: ثبت‌نام کاربر جدید با اطلاعات معتبر
         * Expected:
         * - controller درخواست را به service ارسال کند
         * - نتیجه service برگردانده شود
         * - تمام فیلدها صحیح باشند
         */
        @Test
        @DisplayName("Register delegates to service successfully")
        void register_delegatesToService_success() {
            // Arrange - آماده‌سازی درخواست و پاسخ مورد انتظار
            RegisterRequest req = new RegisterRequest("Test User", "09120000001", "test@example.com", "hash", User.Role.BUYER, "Address");
            User expected = new User(1L, "Test User", "09120000001", "test@example.com", "hash", User.Role.BUYER, "Address");
            when(service.register(req)).thenReturn(expected);

            // Act - فراخوانی متد controller
            User actual = controller.register(req);

            // Assert - بررسی delegation و نتیجه
            verify(service).register(req);
            assertThat(actual).isSameAs(expected);
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getFullName()).isEqualTo("Test User");
            assertThat(actual.getPhone()).isEqualTo("09120000001");
        }

        /**
         * تست ثبت‌نام با نقش‌های مختلف
         * 
         * @param role نقش کاربری برای تست
         * 
         * Scenario: ثبت‌نام کاربر با هر یک از نقش‌های موجود
         * Expected: تمام نقش‌ها صحیح delegate شوند
         */
        @EnumSource(User.Role.class)
        @ParameterizedTest
        @DisplayName("Register with different roles delegates correctly")
        void register_differentRoles_delegatesCorrectly(User.Role role) {
            // Arrange - کاربر با نقش مشخص
            RegisterRequest req = new RegisterRequest("Test User", "0912000000" + role.ordinal(), "test@example.com", "hash", role, "Address");
            User expected = new User(1L, "Test User", "0912000000" + role.ordinal(), "test@example.com", "hash", role, "Address");
            when(service.register(req)).thenReturn(expected);

            // Act
            User actual = controller.register(req);

            // Assert - بررسی نقش
            verify(service).register(req);
            assertThat(actual.getRole()).isEqualTo(role);
        }

        /**
         * تست انتشار exception تکرار شماره تلفن
         * 
         * Scenario: تلاش ثبت‌نام با شماره تلفن تکراری
         * Expected:
         * - service exception پرتاب کند
         * - controller exception را propagate کند
         * - پیام خطا صحیح باشد
         */
        @Test
        @DisplayName("Register propagates DuplicatePhoneException from service")
        void register_duplicatePhone_propagatesException() {
            // Arrange
            RegisterRequest req = new RegisterRequest("Test User", "09120000002", "test@example.com", "hash", User.Role.BUYER, "Address");
            when(service.register(req)).thenThrow(new DuplicatePhoneException("Phone number already exists: 09120000002"));

            // Act & Assert - انتظار exception
            assertThatThrownBy(() -> controller.register(req))
                    .isInstanceOf(DuplicatePhoneException.class)
                    .hasMessageContaining("09120000002");

            verify(service).register(req);
        }

        /**
         * تست ثبت‌نام با شماره‌های تلفن مختلف
         * 
         * @param phone شماره تلفن برای تست
         * 
         * Scenario: تست فرمت‌های مختلف شماره تلفن ایرانی
         * Expected: تمام فرمت‌ها صحیح delegate شوند
         */
        @ParameterizedTest
        @ValueSource(strings = {
            "09111111111", "09222222222", "09333333333", "09444444444", "09555555555"
        })
        @DisplayName("Register with various phone numbers delegates correctly")
        void register_variousPhoneNumbers_delegatesCorrectly(String phone) {
            // Arrange
            RegisterRequest req = new RegisterRequest("Test User", phone, "test@example.com", "hash", User.Role.BUYER, "Address");
            User expected = new User(1L, "Test User", phone, "test@example.com", "hash", User.Role.BUYER, "Address");
            when(service.register(req)).thenReturn(expected);

            // Act
            User actual = controller.register(req);

            // Assert - بررسی حفظ شماره تلفن
            verify(service).register(req);
            assertThat(actual.getPhone()).isEqualTo(phone);
        }
    }

    /**
     * تست‌های کنترلر ورود
     * 
     * این دسته شامل تمام عملیات مربوط به ورود کاربر:
     * - ورود موفق
     * - مدیریت اعتبارات نامعتبر
     * - تست با شماره‌های مختلف
     */
    @Nested
    @DisplayName("Login Controller Tests")
    class LoginControllerTests {

        /**
         * تست موفق delegate کردن ورود به service
         * 
         * Scenario: ورود کاربر با اعتبارات معتبر
         * Expected:
         * - controller درخواست را به service ارسال کند
         * - اطلاعات کاربر صحیح برگردانده شود
         */
        @Test
        @DisplayName("Login delegates to service successfully")
        void login_delegatesToService_success() {
            // Arrange
            String phone = "09120000002";
            String passwordHash = "hash2";
            User expected = new User(2L, "Ali Mohammadi", phone, "ali@example.com", passwordHash, User.Role.BUYER, "Isfahan");
            when(service.login(phone, passwordHash)).thenReturn(expected);

            // Act
            User actual = controller.login(phone, passwordHash);

            // Assert - بررسی delegation و نتیجه
            verify(service).login(phone, passwordHash);
            assertThat(actual).isSameAs(expected);
            assertThat(actual.getId()).isEqualTo(2L);
            assertThat(actual.getFullName()).isEqualTo("Ali Mohammadi");
            assertThat(actual.getPhone()).isEqualTo(phone);
        }

        /**
         * تست انتشار exception اعتبارات نامعتبر
         * 
         * Scenario: تلاش ورود با رمز عبور اشتباه
         * Expected:
         * - service exception پرتاب کند
         * - controller exception را propagate کند
         */
        @Test
        @DisplayName("Login propagates InvalidCredentialsException from service")
        void login_invalidCredentials_propagatesException() {
            // Arrange
            String phone = "09120000003";
            String wrongPassword = "wrongPassword";
            when(service.login(phone, wrongPassword)).thenThrow(new InvalidCredentialsException());

            // Act & Assert
            assertThatThrownBy(() -> controller.login(phone, wrongPassword))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(service).login(phone, wrongPassword);
        }

        /**
         * تست ورود با شماره‌های تلفن مختلف
         * 
         * @param phone شماره تلفن برای تست
         * 
         * Scenario: تست ورود با فرمت‌های مختلف شماره
         * Expected: تمام فرمت‌ها صحیح کار کنند
         */
        @ParameterizedTest
        @ValueSource(strings = {
            "09111111111", "09222222222", "09333333333", "09444444444"
        })
        @DisplayName("Login with various phone numbers delegates correctly")
        void login_variousPhoneNumbers_delegatesCorrectly(String phone) {
            // Arrange
            String password = "password123";
            User expected = new User(1L, "Test User", phone, "test@example.com", password, User.Role.BUYER, "Address");
            when(service.login(phone, password)).thenReturn(expected);

            // Act
            User actual = controller.login(phone, password);

            // Assert
            verify(service).login(phone, password);
            assertThat(actual.getPhone()).isEqualTo(phone);
        }
    }

    /**
     * تست‌های کنترلر پروفایل
     * 
     * این دسته شامل عملیات مربوط به مدیریت پروفایل کاربر:
     * - دریافت پروفایل
     * - به‌روزرسانی پروفایل
     * - مدیریت کاربر یافت نشده
     */
    @Nested
    @DisplayName("Profile Controller Tests")
    class ProfileControllerTests {

        /**
         * تست موفق دریافت پروفایل
         * 
         * Scenario: دریافت پروفایل کاربر موجود
         * Expected:
         * - اطلاعات کامل کاربر در ProfileResponse
         * - mapping صحیح از User به DTO
         */
        @Test
        @DisplayName("Get profile delegates to service successfully")
        void getProfile_delegatesToService_success() {
            // Arrange
            long userId = 123L;
            User user = new User(userId, "John Doe", "09120000006", "john@example.com", "hash", User.Role.BUYER, "Tehran");
            when(service.getProfile(userId)).thenReturn(user);

            // Act
            ProfileResponse actual = controller.getProfile(userId);

            // Assert - بررسی delegation و mapping
            verify(service).getProfile(userId);
            assertThat(actual.id()).isEqualTo(userId);
            assertThat(actual.fullName()).isEqualTo("John Doe");
            assertThat(actual.phone()).isEqualTo("09120000006");
            assertThat(actual.email()).isEqualTo("john@example.com");
            assertThat(actual.address()).isEqualTo("Tehran");
            assertThat(actual.role()).isEqualTo(User.Role.BUYER);
        }

        /**
         * تست انتشار exception کاربر یافت نشده
         * 
         * Scenario: درخواست پروفایل کاربر غیرموجود
         * Expected:
         * - NotFoundException propagate شود
         * - پیام خطا مناسب باشد
         */
        @Test
        @DisplayName("Get profile propagates NotFoundException from service")
        void getProfile_userNotFound_propagatesException() {
            // Arrange
            long nonExistentUserId = 999999L;
            when(service.getProfile(nonExistentUserId)).thenThrow(new NotFoundException("User", nonExistentUserId));

            // Act & Assert
            assertThatThrownBy(() -> controller.getProfile(nonExistentUserId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User");

            verify(service).getProfile(nonExistentUserId);
        }

        /**
         * تست موفق به‌روزرسانی پروفایل
         * 
         * Scenario: به‌روزرسانی اطلاعات پروفایل کاربر
         * Expected:
         * - اطلاعات جدید در پاسخ
         * - delegation صحیح به service
         */
        @Test
        @DisplayName("Update profile delegates to service successfully")
        void updateProfile_delegatesToService_success() {
            // Arrange
            long userId = 456L;
            UpdateProfileRequest request = new UpdateProfileRequest("Updated Name", "updated@example.com", "Updated Address");
            User updatedUser = new User(userId, "Updated Name", "09120000008", "updated@example.com", "hash", User.Role.BUYER, "Updated Address");
            when(service.updateProfile(userId, request)).thenReturn(updatedUser);

            // Act
            ProfileResponse actual = controller.updateProfile(userId, request);

            // Assert - بررسی به‌روزرسانی
            verify(service).updateProfile(userId, request);
            assertThat(actual.id()).isEqualTo(userId);
            assertThat(actual.fullName()).isEqualTo("Updated Name");
            assertThat(actual.email()).isEqualTo("updated@example.com");
            assertThat(actual.address()).isEqualTo("Updated Address");
            assertThat(actual.phone()).isEqualTo("09120000008");
        }
    }

    // ==================== تست‌های Legacy ====================
    
    /**
     * تست legacy برای backward compatibility
     * 
     * این تست‌ها برای حفظ سازگاری با کدهای قدیمی نگه داشته می‌شوند
     */

    /**
     * تست legacy ثبت‌نام
     * 
     * Legacy test برای backward compatibility
     */
    @Test
    void register_delegatesToService() {
        // Arrange
        RegisterRequest req = new RegisterRequest("Test", "09120000001", "", "hash", User.Role.BUYER, "Addr");
        User expected = new User(1, "Test", "09120000001", "", "hash", User.Role.BUYER, "Addr");
        when(service.register(req)).thenReturn(expected);

        // Act
        User actual = controller.register(req);

        // Assert
        verify(service).register(req);
        assertThat(actual).isSameAs(expected);
    }

    /**
     * تست legacy ورود
     * 
     * Legacy test برای backward compatibility
     */
    @Test
    void login_delegatesToService() {
        // Arrange
        User expected = new User(2, "Ali", "09120000002", "", "hash2", User.Role.BUYER, "A");
        when(service.login("09120000002", "hash2")).thenReturn(expected);

        // Act
        User actual = controller.login("09120000002", "hash2");

        // Assert
        verify(service).login("09120000002", "hash2");
        assertThat(actual).isSameAs(expected);
    }
}
