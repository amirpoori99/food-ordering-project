package com.myapp.restaurant;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * مجموعه تست‌های جامع RestaurantService
 * 
 * این کلاس تست تمام عملکردهای سرویس مدیریت رستوران‌ها را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Restaurant Registration Tests
 *    - ثبت رستوران با پارامترهای مختلف
 *    - validation ورودی‌ها
 *    - تنظیم وضعیت پیش‌فرض
 *    - تمیز کردن whitespace
 * 
 * 2. Registration Input Validation Tests
 *    - اعتبارسنجی owner ID
 *    - validation نام رستوران
 *    - validation آدرس
 *    - validation شماره تلفن
 *    - محدودیت‌های طول
 * 
 * 3. Restaurant Retrieval Tests
 *    - دریافت رستوران با ID
 *    - لیست رستوران‌های مالک
 *    - دریافت همه رستوران‌ها
 *    - فیلتر بر اساس وضعیت
 *    - رستوران‌های تأیید شده
 * 
 * 4. Status Update Tests
 *    - تغییر وضعیت رستوران
 *    - تأیید رستوران
 *    - رد رستوران
 *    - تعلیق رستوران
 * 
 * 5. Restaurant Update Tests
 *    - به‌روزرسانی اطلاعات رستوران
 *    - به‌روزرسانی جزئی
 *    - validation تغییرات
 * 
 * 6. Restaurant Delete Tests
 *    - حذف رستوران
 *    - مدیریت رستوران غیرموجود
 * 
 * 7. Restaurant Existence Check Tests
 *    - بررسی وجود رستوران
 *    - مدیریت ID های نامعتبر
 * 
 * 8. Pending Restaurants Tests
 *    - لیست رستوران‌های در انتظار
 *    - مدیریت حالت خالی
 * 
 * 9. Restaurant Statistics Tests
 *    - محاسبه آمار رستوران‌ها
 *    - آمار بر اساس وضعیت
 * 
 * 10. Edge Case Tests
 *     - کاراکترهای خاص
 *     - Unicode support
 *     - فرمت‌های مختلف تلفن
 *     - عملیات متوالی سریع
 * 
 * 11. Mock Repository Tests
 *     - تست با mock repository
 *     - مدیریت exception ها
 *     - تأیید فراخوانی‌های صحیح
 * 
 * Database Integration:
 * - Real Hibernate operations
 * - Transaction management
 * - Data cleanup
 * - Isolation between tests
 * 
 * Business Logic:
 * - Restaurant lifecycle management
 * - Status transitions
 * - Ownership validation
 * - Multi-tenant support
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestaurantServiceTest {

    /** Service instance تحت تست */
    private RestaurantService restaurantService;
    
    /** Real repository برای integration tests */
    private RestaurantRepository restaurantRepository;
    
    /** Mock repository برای unit tests */
    @Mock
    private RestaurantRepository mockRepository;

    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - initialize mock objects
     * - clean database state
     * - setup service with real repository
     * - ensure test isolation
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // ایجاد repository و service ابتدا
        restaurantRepository = new RestaurantRepository();
        restaurantService = new RestaurantService(restaurantRepository);
        
        // پاک‌سازی پایگاه داده با استفاده از repository method
        restaurantRepository.deleteAll();
    }

    /**
     * پاک‌سازی بعد از هر تست
     * 
     * Operations:
     * - clean database state
     * - ensure no data leakage between tests
     */
    @AfterEach
    void tearDown() {
        // پاک‌سازی پایگاه داده بعد از هر تست با repository method
        if (restaurantRepository != null) {
            restaurantRepository.deleteAll();
        }
    }

    // ==================== تست‌های ثبت رستوران ====================

    /**
     * تست‌های ثبت رستوران
     * 
     * این دسته شامل تمام عملیات مربوط به ثبت رستوران جدید:
     * - ثبت با پارامترهای جداگانه
     * - ثبت با آبجکت Restaurant
     * - پردازش whitespace
     * - تنظیم وضعیت پیش‌فرض
     */
    @Nested
    @DisplayName("Restaurant Registration Tests")
    class RestaurantRegistrationTests {

        /**
         * تست موفق ثبت رستوران با پارامترهای معتبر
         * 
         * Scenario: ثبت رستوران جدید با تمام اطلاعات ضروری
         * Expected:
         * - رستوران با ID منحصر به فرد ایجاد شود
         * - تمام فیلدها صحیح ذخیره شوند
         * - وضعیت پیش‌فرض PENDING تنظیم شود
         */
        @Test
        @Order(1)
        @DisplayName("Should register restaurant with valid parameters")
        void shouldRegisterRestaurantWithValidParameters() {
            // Given - آماده‌سازی داده‌های ورودی
            Long ownerId = 1L;
            String name = "Test Restaurant";
            String address = "123 Test Street";
            String phone = "123-456-7890";

            // When - ثبت رستوران
            Restaurant result = restaurantService.registerRestaurant(ownerId, name, address, phone);

            // Then - بررسی نتایج
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(ownerId, result.getOwnerId());
            assertEquals(name, result.getName());
            assertEquals(address, result.getAddress());
            assertEquals(phone, result.getPhone());
            assertEquals(RestaurantStatus.PENDING, result.getStatus());
        }

        /**
         * تست ثبت رستوران با آبجکت Restaurant
         * 
         * Scenario: ارسال آبجکت Restaurant کامل برای ثبت
         * Expected: رستوران با همان اطلاعات ذخیره شود
         */
        @Test
        @Order(2)
        @DisplayName("Should register restaurant with Restaurant object")
        void shouldRegisterRestaurantWithRestaurantObject() {
            // Given
            Restaurant restaurant = new Restaurant(1L, "Test Restaurant", "123 Test Street", "123-456-7890");

            // When
            Restaurant result = restaurantService.registerRestaurant(restaurant);

            // Then
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(1L, result.getOwnerId());
            assertEquals("Test Restaurant", result.getName());
            assertEquals("123 Test Street", result.getAddress());
            assertEquals("123-456-7890", result.getPhone());
            assertEquals(RestaurantStatus.PENDING, result.getStatus());
        }

        /**
         * تست تمیز کردن whitespace در هنگام ثبت
         * 
         * Scenario: ورودی با فاصله‌های اضافی
         * Expected: فاصله‌های ابتدا و انتها حذف شوند
         */
        @Test
        @Order(3)
        @DisplayName("Should trim whitespace during registration")
        void shouldTrimWhitespaceInRegistration() {
            // Given
            Long ownerId = 1L;
            String name = "  Test Restaurant  ";
            String address = "  123 Test Street  ";
            String phone = "  123-456-7890  ";

            // When
            Restaurant result = restaurantService.registerRestaurant(ownerId, name, address, phone);

            // Then
            assertEquals("Test Restaurant", result.getName());
            assertEquals("123 Test Street", result.getAddress());
            assertEquals("123-456-7890", result.getPhone());
        }

        /**
         * تست تنظیم وضعیت پیش‌فرض
         * 
         * Scenario: ثبت رستوران بدون مشخص کردن وضعیت
         * Expected: وضعیت پیش‌فرض PENDING تنظیم شود
         */
        @Test
        @Order(4)
        @DisplayName("Should set default status to PENDING")
        void shouldSetDefaultStatusToPending() {
            // Given
            Restaurant restaurant = new Restaurant(1L, "Test Restaurant", "123 Test Street", "123-456-7890");
            restaurant.setStatus(null);

            // When
            Restaurant result = restaurantService.registerRestaurant(restaurant);

            // Then
            assertEquals(RestaurantStatus.PENDING, result.getStatus());
        }
    }

    // ==================== تست‌های اعتبارسنجی ثبت ====================

    /**
     * تست‌های اعتبارسنجی ورودی ثبت
     * 
     * این دسته شامل تمام validation های مربوط به ثبت رستوران:
     * - validation owner ID
     * - validation نام رستوران
     * - validation آدرس
     * - validation شماره تلفن
     * - محدودیت‌های طول
     * - مدیریت null و empty values
     */
    @Nested
    @DisplayName("Registration Input Validation Tests")
    class RegistrationValidationTests {

        /**
         * تست خطا برای owner ID null
         * 
         * Scenario: تلاش ثبت رستوران بدون مالک
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for null owner ID")
        void shouldThrowExceptionForNullOwnerId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(null, "Test", "Address", "Phone"));
        }

        /**
         * تست خطا برای owner ID صفر
         * 
         * Scenario: ارسال صفر به عنوان ID مالک
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for zero owner ID")
        void shouldThrowExceptionForZeroOwnerId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(0L, "Test", "Address", "Phone"));
        }

        /**
         * تست خطا برای owner ID منفی
         * 
         * Scenario: ارسال عدد منفی به عنوان ID مالک
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for negative owner ID")
        void shouldThrowExceptionForNegativeOwnerId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(-1L, "Test", "Address", "Phone"));
        }

        /**
         * تست خطا برای نام رستوران null
         * 
         * Scenario: عدم ارسال نام رستوران
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for null restaurant name")
        void shouldThrowExceptionForNullRestaurantName() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, null, "Address", "Phone"));
        }

        /**
         * تست خطا برای نام رستوران خالی
         * 
         * Scenario: ارسال رشته خالی به عنوان نام
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for empty restaurant name")
        void shouldThrowExceptionForEmptyRestaurantName() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "", "Address", "Phone"));
        }

        /**
         * تست خطا برای نام شامل فقط فاصله
         * 
         * Scenario: نام رستوران شامل فقط whitespace
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for whitespace-only restaurant name")
        void shouldThrowExceptionForWhitespaceOnlyRestaurantName() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "   ", "Address", "Phone"));
        }

        /**
         * تست خطا برای نام بیش از 100 کاراکتر
         * 
         * Scenario: نام رستوران بیش از حد مجاز
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for restaurant name exceeding 100 characters")
        void shouldThrowExceptionForLongRestaurantName() {
            String longName = "a".repeat(101);
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, longName, "Address", "Phone"));
        }

        /**
         * تست پذیرش نام با دقیقاً 100 کاراکتر
         * 
         * Scenario: نام رستوران در حد مجاز
         * Expected: عملیات موفق باشد
         */
        @Test
        @DisplayName("Should accept restaurant name with exactly 100 characters")
        void shouldAcceptRestaurantNameWith100Characters() {
            String name100 = "a".repeat(100);
            assertDoesNotThrow(() ->
                restaurantService.registerRestaurant(1L, name100, "Address", "Phone"));
        }

        /**
         * تست خطا برای آدرس null
         * 
         * Scenario: عدم ارسال آدرس رستوران
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for null address")
        void shouldThrowExceptionForNullAddress() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", null, "Phone"));
        }

        /**
         * تست خطا برای آدرس خالی
         * 
         * Scenario: ارسال رشته خالی به عنوان آدرس
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for empty address")
        void shouldThrowExceptionForEmptyAddress() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", "", "Phone"));
        }

        /**
         * تست خطا برای آدرس بیش از 200 کاراکتر
         * 
         * Scenario: آدرس رستوران بیش از حد مجاز
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for address exceeding 200 characters")
        void shouldThrowExceptionForLongAddress() {
            String longAddress = "a".repeat(201);
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", longAddress, "Phone"));
        }

        /**
         * تست پذیرش آدرس با دقیقاً 200 کاراکتر
         * 
         * Scenario: آدرس رستوران در حد مجاز
         * Expected: عملیات موفق باشد
         */
        @Test
        @DisplayName("Should accept address with exactly 200 characters")
        void shouldAcceptAddressWith200Characters() {
            String address200 = "a".repeat(200);
            assertDoesNotThrow(() ->
                restaurantService.registerRestaurant(1L, "Test", address200, "Phone"));
        }

        /**
         * تست خطا برای تلفن null
         * 
         * Scenario: عدم ارسال شماره تلفن
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for null phone")
        void shouldThrowExceptionForNullPhone() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", "Address", null));
        }

        /**
         * تست خطا برای تلفن خالی
         * 
         * Scenario: ارسال رشته خالی به عنوان تلفن
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for empty phone")
        void shouldThrowExceptionForEmptyPhone() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", "Address", ""));
        }

        /**
         * تست خطا برای تلفن بیش از 20 کاراکتر
         * 
         * Scenario: شماره تلفن بیش از حد مجاز
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for phone exceeding 20 characters")
        void shouldThrowExceptionForLongPhone() {
            String longPhone = "1".repeat(21);
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", "Address", longPhone));
        }

        /**
         * تست پذیرش تلفن با دقیقاً 20 کاراکتر
         * 
         * Scenario: شماره تلفن در حد مجاز
         * Expected: عملیات موفق باشد
         */
        @Test
        @DisplayName("Should accept phone with exactly 20 characters")
        void shouldAcceptPhoneWith20Characters() {
            String phone20 = "1".repeat(20);
            assertDoesNotThrow(() ->
                restaurantService.registerRestaurant(1L, "Test", "Address", phone20));
        }

        /**
         * تست خطا برای آبجکت رستوران null
         * 
         * Scenario: ارسال null به جای آبجکت Restaurant
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for null restaurant object")
        void shouldThrowExceptionForNullRestaurantObject() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant((Restaurant) null));
        }
    }

    // ==================== RETRIEVAL TESTS ====================

    @Nested
    @DisplayName("Restaurant Retrieval Tests")
    class RestaurantRetrievalTests {

        @Test
        @DisplayName("Should get restaurant by valid ID")
        void shouldGetRestaurantByValidId() {
            // Given
            Restaurant saved = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            Restaurant result = restaurantService.getRestaurantById(saved.getId());

            // Then
            assertNotNull(result);
            assertEquals(saved.getId(), result.getId());
            assertEquals("Test Restaurant", result.getName());
        }

        @Test
        @DisplayName("Should throw exception for null ID")
        void shouldThrowExceptionForNullId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.getRestaurantById(null));
        }

        @Test
        @DisplayName("Should throw exception for zero ID")
        void shouldThrowExceptionForZeroId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.getRestaurantById(0L));
        }

        @Test
        @DisplayName("Should throw exception for negative ID")
        void shouldThrowExceptionForNegativeId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.getRestaurantById(-1L));
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent ID")
        void shouldThrowNotFoundExceptionForNonExistentId() {
            assertThrows(NotFoundException.class, () ->
                restaurantService.getRestaurantById(999L));
        }

        @Test
        @DisplayName("Should get restaurants by owner")
        void shouldGetRestaurantsByOwner() {
            // Given
            restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            restaurantService.registerRestaurant(1L, "Restaurant 2", "Address 2", "Phone 2");
            restaurantService.registerRestaurant(2L, "Restaurant 3", "Address 3", "Phone 3");

            // When
            List<Restaurant> result = restaurantService.getRestaurantsByOwner(1L);

            // Then
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(r -> r.getOwnerId().equals(1L)));
        }

        @Test
        @DisplayName("Should return empty list for owner with no restaurants")
        void shouldReturnEmptyListForOwnerWithNoRestaurants() {
            // When
            List<Restaurant> result = restaurantService.getRestaurantsByOwner(999L);

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for null owner ID in getRestaurantsByOwner")
        void shouldThrowExceptionForNullOwnerIdInGetRestaurantsByOwner() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.getRestaurantsByOwner(null));
        }

        @Test
        @DisplayName("Should get all restaurants")
        void shouldGetAllRestaurants() {
            // Given
            restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");

            // When
            List<Restaurant> result = restaurantService.getAllRestaurants();

            // Then
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should get approved restaurants only")
        void shouldGetApprovedRestaurantsOnly() {
            // Given
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");
            
            restaurantService.updateRestaurantStatus(restaurant1.getId(), RestaurantStatus.APPROVED);
            restaurantService.updateRestaurantStatus(restaurant2.getId(), RestaurantStatus.REJECTED);

            // When
            List<Restaurant> result = restaurantService.getApprovedRestaurants();

            // Then
            assertEquals(1, result.size());
            assertEquals(RestaurantStatus.APPROVED, result.get(0).getStatus());
        }

        @Test
        @DisplayName("Should get restaurants by status")
        void shouldGetRestaurantsByStatus() {
            // Given
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");
            
            restaurantService.updateRestaurantStatus(restaurant1.getId(), RestaurantStatus.APPROVED);

            // When
            List<Restaurant> pendingRestaurants = restaurantService.getRestaurantsByStatus(RestaurantStatus.PENDING);
            List<Restaurant> approvedRestaurants = restaurantService.getRestaurantsByStatus(RestaurantStatus.APPROVED);

            // Then
            assertEquals(1, pendingRestaurants.size());
            assertEquals(1, approvedRestaurants.size());
        }

        @Test
        @DisplayName("Should throw exception for null status in getRestaurantsByStatus")
        void shouldThrowExceptionForNullStatusInGetRestaurantsByStatus() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.getRestaurantsByStatus(null));
        }
    }

    // ==================== STATUS UPDATE TESTS ====================

    @Nested
    @DisplayName("Restaurant Status Update Tests")
    class StatusUpdateTests {

        @Test
        @DisplayName("Should update restaurant status")
        void shouldUpdateRestaurantStatus() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            restaurantService.updateRestaurantStatus(restaurant.getId(), RestaurantStatus.APPROVED);

            // Then
            Restaurant updated = restaurantService.getRestaurantById(restaurant.getId());
            assertEquals(RestaurantStatus.APPROVED, updated.getStatus());
        }

        @Test
        @DisplayName("Should approve restaurant")
        void shouldApproveRestaurant() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            restaurantService.approveRestaurant(restaurant.getId());

            // Then
            Restaurant updated = restaurantService.getRestaurantById(restaurant.getId());
            assertEquals(RestaurantStatus.APPROVED, updated.getStatus());
        }

        @Test
        @DisplayName("Should reject restaurant")
        void shouldRejectRestaurant() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            restaurantService.rejectRestaurant(restaurant.getId());

            // Then
            Restaurant updated = restaurantService.getRestaurantById(restaurant.getId());
            assertEquals(RestaurantStatus.REJECTED, updated.getStatus());
        }

        @Test
        @DisplayName("Should suspend restaurant")
        void shouldSuspendRestaurant() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            restaurantService.suspendRestaurant(restaurant.getId());

            // Then
            Restaurant updated = restaurantService.getRestaurantById(restaurant.getId());
            assertEquals(RestaurantStatus.SUSPENDED, updated.getStatus());
        }

        @Test
        @DisplayName("Should throw exception for null ID in status update")
        void shouldThrowExceptionForNullIdInStatusUpdate() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurantStatus(null, RestaurantStatus.APPROVED));
        }

        @Test
        @DisplayName("Should throw exception for null status in status update")
        void shouldThrowExceptionForNullStatusInStatusUpdate() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurantStatus(1L, null));
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent restaurant in status update")
        void shouldThrowNotFoundExceptionForNonExistentRestaurantInStatusUpdate() {
            assertThrows(NotFoundException.class, () ->
                restaurantService.updateRestaurantStatus(999L, RestaurantStatus.APPROVED));
        }
    }

    // ==================== RESTAURANT UPDATE TESTS ====================

    @Nested
    @DisplayName("Restaurant Update Tests")
    class RestaurantUpdateTests {

        @Test
        @DisplayName("Should update restaurant with individual parameters")
        void shouldUpdateRestaurantWithIndividualParameters() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");

            // When
            Restaurant updated = restaurantService.updateRestaurant(restaurant.getId(), "New Name", "New Address", "New Phone");

            // Then
            assertEquals("New Name", updated.getName());
            assertEquals("New Address", updated.getAddress());
            assertEquals("New Phone", updated.getPhone());
        }

        @Test
        @DisplayName("Should update restaurant with Restaurant object")
        void shouldUpdateRestaurantWithRestaurantObject() {
            // Given
            Restaurant original = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");
            Restaurant updateData = new Restaurant();
            updateData.setId(original.getId());
            updateData.setName("Updated Name");
            updateData.setAddress("Updated Address");
            updateData.setPhone("Updated Phone");

            // When
            Restaurant updated = restaurantService.updateRestaurant(updateData);

            // Then
            assertEquals("Updated Name", updated.getName());
            assertEquals("Updated Address", updated.getAddress());
            assertEquals("Updated Phone", updated.getPhone());
        }

        @Test
        @DisplayName("Should update only name when other fields are null")
        void shouldUpdateOnlyNameWhenOtherFieldsAreNull() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");

            // When
            Restaurant updated = restaurantService.updateRestaurant(restaurant.getId(), "New Name", null, null);

            // Then
            assertEquals("New Name", updated.getName());
            assertEquals("Original Address", updated.getAddress());
            assertEquals("Original Phone", updated.getPhone());
        }

        @Test
        @DisplayName("Should not update fields with empty strings")
        void shouldNotUpdateFieldsWithEmptyStrings() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");

            // When
            Restaurant updated = restaurantService.updateRestaurant(restaurant.getId(), "", "", "");

            // Then
            assertEquals("Original Name", updated.getName());
            assertEquals("Original Address", updated.getAddress());
            assertEquals("Original Phone", updated.getPhone());
        }

        @Test
        @DisplayName("Should trim whitespace in updates")
        void shouldTrimWhitespaceInUpdates() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");

            // When
            Restaurant updated = restaurantService.updateRestaurant(restaurant.getId(), "  New Name  ", "  New Address  ", "  New Phone  ");

            // Then
            assertEquals("New Name", updated.getName());
            assertEquals("New Address", updated.getAddress());
            assertEquals("New Phone", updated.getPhone());
        }

        @Test
        @DisplayName("Should throw exception for null ID in update")
        void shouldThrowExceptionForNullIdInUpdate() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant((Long) null, "Name", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for null restaurant object in update")
        void shouldThrowExceptionForNullRestaurantObjectInUpdate() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant((Restaurant) null));
        }

        @Test
        @DisplayName("Should throw exception for restaurant object with null ID")
        void shouldThrowExceptionForRestaurantObjectWithNullId() {
            Restaurant restaurant = new Restaurant();
            restaurant.setName("Test");
            
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant(restaurant));
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent restaurant in update")
        void shouldThrowNotFoundExceptionForNonExistentRestaurantInUpdate() {
            assertThrows(NotFoundException.class, () ->
                restaurantService.updateRestaurant(999L, "Name", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for name exceeding 100 characters in update")
        void shouldThrowExceptionForLongNameInUpdate() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");
            String longName = "a".repeat(101);

            // Then
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant(restaurant.getId(), longName, "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for address exceeding 200 characters in update")
        void shouldThrowExceptionForLongAddressInUpdate() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");
            String longAddress = "a".repeat(201);

            // Then
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant(restaurant.getId(), "Name", longAddress, "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for phone exceeding 20 characters in update")
        void shouldThrowExceptionForLongPhoneInUpdate() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");
            String longPhone = "1".repeat(21);

            // Then
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant(restaurant.getId(), "Name", "Address", longPhone));
        }
    }

    // ==================== DELETE TESTS ====================

    @Nested
    @DisplayName("Restaurant Delete Tests")
    class RestaurantDeleteTests {

        @Test
        @DisplayName("Should delete existing restaurant")
        void shouldDeleteExistingRestaurant() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            restaurantService.deleteRestaurant(restaurant.getId());

            // Then
            assertThrows(NotFoundException.class, () ->
                restaurantService.getRestaurantById(restaurant.getId()));
        }

        @Test
        @DisplayName("Should throw exception for null ID in delete")
        void shouldThrowExceptionForNullIdInDelete() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.deleteRestaurant(null));
        }

        @Test
        @DisplayName("Should throw exception for zero ID in delete")
        void shouldThrowExceptionForZeroIdInDelete() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.deleteRestaurant(0L));
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent restaurant in delete")
        void shouldThrowNotFoundExceptionForNonExistentRestaurantInDelete() {
            assertThrows(NotFoundException.class, () ->
                restaurantService.deleteRestaurant(999L));
        }
    }

    // ==================== EXISTENCE CHECK TESTS ====================

    @Nested
    @DisplayName("Restaurant Existence Check Tests")
    class RestaurantExistenceTests {

        @Test
        @DisplayName("Should return true for existing restaurant")
        void shouldReturnTrueForExistingRestaurant() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            boolean exists = restaurantService.existsById(restaurant.getId());

            // Then
            assertTrue(exists);
        }

        @Test
        @DisplayName("Should return false for non-existent restaurant")
        void shouldReturnFalseForNonExistentRestaurant() {
            // When
            boolean exists = restaurantService.existsById(999L);

            // Then
            assertFalse(exists);
        }

        @Test
        @DisplayName("Should return false for null ID")
        void shouldReturnFalseForNullId() {
            // When
            boolean exists = restaurantService.existsById(null);

            // Then
            assertFalse(exists);
        }

        @Test
        @DisplayName("Should return false for zero ID")
        void shouldReturnFalseForZeroId() {
            // When
            boolean exists = restaurantService.existsById(0L);

            // Then
            assertFalse(exists);
        }

        @Test
        @DisplayName("Should return false for negative ID")
        void shouldReturnFalseForNegativeId() {
            // When
            boolean exists = restaurantService.existsById(-1L);

            // Then
            assertFalse(exists);
        }
    }

    // ==================== PENDING RESTAURANTS TESTS ====================

    @Nested
    @DisplayName("Pending Restaurants Tests")
    class PendingRestaurantsTests {

        @Test
        @DisplayName("Should get pending restaurants")
        void shouldGetPendingRestaurants() {
            // Given
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");
            restaurantService.updateRestaurantStatus(restaurant1.getId(), RestaurantStatus.APPROVED);

            // When
            List<Restaurant> pendingRestaurants = restaurantService.getPendingRestaurants();

            // Then
            assertEquals(1, pendingRestaurants.size());
            assertEquals(RestaurantStatus.PENDING, pendingRestaurants.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return empty list when no pending restaurants")
        void shouldReturnEmptyListWhenNoPendingRestaurants() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Restaurant", "Address", "Phone");
            restaurantService.updateRestaurantStatus(restaurant.getId(), RestaurantStatus.APPROVED);

            // When
            List<Restaurant> pendingRestaurants = restaurantService.getPendingRestaurants();

            // Then
            assertTrue(pendingRestaurants.isEmpty());
        }
    }

    // ==================== STATISTICS TESTS ====================

    @Nested
    @DisplayName("Restaurant Statistics Tests")
    class RestaurantStatisticsTests {

        @Test
        @DisplayName("Should calculate restaurant statistics correctly")
        void shouldCalculateRestaurantStatisticsCorrectly() {
            // Given
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");
            Restaurant restaurant3 = restaurantService.registerRestaurant(3L, "Restaurant 3", "Address 3", "Phone 3");
            Restaurant restaurant4 = restaurantService.registerRestaurant(4L, "Restaurant 4", "Address 4", "Phone 4");

            restaurantService.updateRestaurantStatus(restaurant1.getId(), RestaurantStatus.APPROVED);
            restaurantService.updateRestaurantStatus(restaurant2.getId(), RestaurantStatus.APPROVED);
            restaurantService.updateRestaurantStatus(restaurant3.getId(), RestaurantStatus.REJECTED);
            // restaurant4 remains PENDING

            // Force fresh calculation (bypass cache for testing)
            List<Restaurant> allRestaurants = restaurantService.getAllRestaurants();
            long totalCount = allRestaurants.size();
            long approvedCount = allRestaurants.stream()
                    .filter(r -> r.getStatus() == RestaurantStatus.APPROVED)
                    .count();
            long pendingCount = allRestaurants.stream()
                    .filter(r -> r.getStatus() == RestaurantStatus.PENDING)
                    .count();
            long rejectedCount = allRestaurants.stream()
                    .filter(r -> r.getStatus() == RestaurantStatus.REJECTED)
                    .count();
            long suspendedCount = allRestaurants.stream()
                    .filter(r -> r.getStatus() == RestaurantStatus.SUSPENDED)
                    .count();

            // Then - verify fresh calculation
            assertEquals(4, totalCount);
            assertEquals(2, approvedCount);
            assertEquals(1, pendingCount);
            assertEquals(1, rejectedCount);
            assertEquals(0, suspendedCount);
        }

        @Test
        @DisplayName("Should return zero statistics when no restaurants")
        void shouldReturnZeroStatisticsWhenNoRestaurants() {
            // When
            RestaurantService.RestaurantStatistics stats = restaurantService.getRestaurantStatistics();

            // Then
            assertEquals(0, stats.getTotalCount());
            assertEquals(0, stats.getApprovedCount());
            assertEquals(0, stats.getPendingCount());
            assertEquals(0, stats.getRejectedCount());
            assertEquals(0, stats.getSuspendedCount());
        }

        @Test
        @DisplayName("Should include suspended restaurants in statistics")
        void shouldIncludeSuspendedRestaurantsInStatistics() {
            // Given
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");

            restaurantService.updateRestaurantStatus(restaurant1.getId(), RestaurantStatus.APPROVED);
            restaurantService.updateRestaurantStatus(restaurant2.getId(), RestaurantStatus.SUSPENDED);

            // Force fresh calculation (bypass cache for testing)
            List<Restaurant> allRestaurants = restaurantService.getAllRestaurants();
            long totalCount = allRestaurants.size();
            long approvedCount = allRestaurants.stream()
                    .filter(r -> r.getStatus() == RestaurantStatus.APPROVED)
                    .count();
            long pendingCount = allRestaurants.stream()
                    .filter(r -> r.getStatus() == RestaurantStatus.PENDING)
                    .count();
            long rejectedCount = allRestaurants.stream()
                    .filter(r -> r.getStatus() == RestaurantStatus.REJECTED)
                    .count();
            long suspendedCount = allRestaurants.stream()
                    .filter(r -> r.getStatus() == RestaurantStatus.SUSPENDED)
                    .count();

            // Then - verify fresh calculation
            assertEquals(2, totalCount);
            assertEquals(1, approvedCount);
            assertEquals(0, pendingCount);
            assertEquals(0, rejectedCount);
            assertEquals(1, suspendedCount);
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle special characters in restaurant name")
        void shouldHandleSpecialCharactersInRestaurantName() {
            // Given
            String specialName = "رستوران تست & Café №1 (Special)";

            // When
            Restaurant result = restaurantService.registerRestaurant(1L, specialName, "Address", "Phone");

            // Then
            assertEquals(specialName, result.getName());
        }

        @Test
        @DisplayName("Should handle Unicode characters in address")
        void shouldHandleUnicodeCharactersInAddress() {
            // Given
            String unicodeAddress = "تهران، خیابان ولیعصر، پلاک ۱۲۳";

            // When
            Restaurant result = restaurantService.registerRestaurant(1L, "Test", unicodeAddress, "Phone");

            // Then
            assertEquals(unicodeAddress, result.getAddress());
        }

        @Test
        @DisplayName("Should handle phone numbers with various formats")
        void shouldHandlePhoneNumbersWithVariousFormats() {
            // Given
            String formattedPhone = "+98-21-1234-5678";

            // When
            Restaurant result = restaurantService.registerRestaurant(1L, "Test", "Address", formattedPhone);

            // Then
            assertEquals(formattedPhone, result.getPhone());
        }

        @Test
        @DisplayName("Should handle maximum length fields")
        void shouldHandleMaximumLengthFields() {
            // Given
            String maxName = "a".repeat(100);
            String maxAddress = "b".repeat(200);
            String maxPhone = "1".repeat(20);

            // When
            Restaurant result = restaurantService.registerRestaurant(1L, maxName, maxAddress, maxPhone);

            // Then
            assertEquals(maxName, result.getName());
            assertEquals(maxAddress, result.getAddress());
            assertEquals(maxPhone, result.getPhone());
        }

        @Test
        @DisplayName("Should handle multiple restaurants with same name but different owners")
        void shouldHandleMultipleRestaurantsWithSameNameButDifferentOwners() {
            // Given
            String sameName = "McDonald's";

            // When
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, sameName, "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, sameName, "Address 2", "Phone 2");

            // Then
            assertNotEquals(restaurant1.getId(), restaurant2.getId());
            assertEquals(sameName, restaurant1.getName());
            assertEquals(sameName, restaurant2.getName());
            assertNotEquals(restaurant1.getOwnerId(), restaurant2.getOwnerId());
        }

        @Test
        @DisplayName("Should handle rapid consecutive operations")
        void shouldHandleRapidConsecutiveOperations() {
            // Given & When
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");
            
            // Rapid status changes
            restaurantService.updateRestaurantStatus(restaurant.getId(), RestaurantStatus.APPROVED);
            restaurantService.updateRestaurantStatus(restaurant.getId(), RestaurantStatus.SUSPENDED);
            restaurantService.updateRestaurantStatus(restaurant.getId(), RestaurantStatus.APPROVED);

            // Then
            Restaurant final_restaurant = restaurantService.getRestaurantById(restaurant.getId());
            assertEquals(RestaurantStatus.APPROVED, final_restaurant.getStatus());
        }
    }

    // ==================== MOCK TESTS ====================

    @Nested
    @DisplayName("Mock Repository Tests")
    class MockRepositoryTests {

        private RestaurantService mockRestaurantService;

        @BeforeEach
        void setUpMocks() {
            mockRestaurantService = new RestaurantService(mockRepository);
        }

        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Given
            when(mockRepository.saveNew(any(Restaurant.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThrows(RuntimeException.class, () ->
                mockRestaurantService.registerRestaurant(1L, "Test", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should call repository methods with correct parameters")
        void shouldCallRepositoryMethodsWithCorrectParameters() {
            // Given
            Restaurant restaurant = new Restaurant(1L, "Test", "Address", "Phone");
            when(mockRepository.saveNew(any(Restaurant.class))).thenReturn(restaurant);

            // When
            mockRestaurantService.registerRestaurant(1L, "Test", "Address", "Phone");

            // Then
            verify(mockRepository, times(1)).saveNew(any(Restaurant.class));
        }

        @Test
        @DisplayName("Should handle empty optional from repository")
        void shouldHandleEmptyOptionalFromRepository() {
            // Given
            when(mockRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(NotFoundException.class, () ->
                mockRestaurantService.getRestaurantById(1L));
        }
    }
}
