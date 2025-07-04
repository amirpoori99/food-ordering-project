package com.myapp.coupon;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Coupon;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.models.User;
import com.myapp.restaurant.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 * مجموعه تست‌های جامع CouponService
 * 
 * این کلاس تمام منطق کسب‌وکار، اعتبارسنجی‌ها و موارد خاص (edge cases) را آزمایش می‌کند
 * 
 * === دسته‌های تست ===
 * 1. Coupon Creation Tests - تست‌های ایجاد کوپن
 *    - ایجاد کوپن درصدی
 *    - ایجاد کوپن مبلغ ثابت
 *    - ایجاد کوپن اختصاصی رستوران
 *    - اعتبارسنجی ورودی‌ها
 *    - مدیریت مجوزها
 * 
 * 2. Coupon Application Tests - تست‌های اعمال کوپن
 *    - اعمال موفق کوپن‌ها
 *    - محاسبه صحیح تخفیف
 *    - اعتبارسنجی قوانین کسب‌وکار
 *    - مدیریت محدودیت‌ها
 *    - خطاهای اعمال کوپن
 * 
 * 3. Coupon Usage Tests - تست‌های استفاده از کوپن
 *    - افزایش شمارنده استفاده
 *    - بازگشت استفاده
 *    - ردیابی کامل استفاده
 *    - مدیریت کوپن‌های غیرموجود
 * 
 * 4. Coupon Management Tests - تست‌های مدیریت کوپن
 *    - عملیات CRUD
 *    - فعال‌سازی و غیرفعال‌سازی
 *    - حذف کوپن
 *    - مدیریت مجوزها
 *    - آمارگیری
 * 
 * 5. Edge Cases and Error Handling - موارد خاص و مدیریت خطا
 *    - ورودی‌های null
 *    - مقادیر مرزی
 *    - مقادیر بزرگ
 *    - محدودیت‌های صفر
 *    - مقادیر تخفیف کوچک
 * 
 * === ویژگی‌های تست ===
 * - Mock-based Testing: استفاده از mock objects برای ایزوله کردن تست‌ها
 * - Business Logic Validation: اعتبارسنجی کامل منطق کسب‌وکار
 * - Permission Testing: تست مجوزهای دسترسی
 * - Edge Case Coverage: پوشش موارد خاص و مرزی
 * - Error Scenario Testing: تست سناریوهای خطا
 * - Data Setup: راه‌اندازی داده‌های تست جامع
 * 
 * === Mock Objects ===
 * - CouponRepository: عملیات دیتابیس کوپن‌ها
 * - AuthRepository: احراز هویت و مجوزها
 * - RestaurantRepository: مدیریت رستوران‌ها
 * - CouponUsageRepository: ردیابی استفاده از کوپن‌ها
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("CouponService Tests")
public class CouponServiceTest {
    
    /** Mock repository برای عملیات کوپن‌ها */
    @Mock
    private CouponRepository couponRepository;
    
    /** Mock repository برای احراز هویت */
    @Mock
    private AuthRepository authRepository;
    
    /** Mock repository برای رستوران‌ها */
    @Mock
    private RestaurantRepository restaurantRepository;
    
    /** Mock repository برای ردیابی استفاده از کوپن‌ها */
    @Mock
    private CouponUsageRepository couponUsageRepository;
    
    /** سرویس تحت تست */
    private CouponService couponService;
    
    // داده‌های تست
    /** کاربر ادمین برای تست‌های مجوز */
    private User adminUser;
    /** مالک رستوران برای تست‌های مجوز */
    private User restaurantOwner;
    /** مشتری برای تست‌های اعمال کوپن */
    private User customer;
    /** رستوران نمونه برای تست‌ها */
    private Restaurant restaurant;
    /** کوپن درصدی نمونه */
    private Coupon percentageCoupon;
    /** کوپن مبلغ ثابت نمونه */
    private Coupon fixedAmountCoupon;
    /** تاریخ شروع اعتبار */
    private LocalDateTime validFrom;
    /** تاریخ پایان اعتبار */
    private LocalDateTime validUntil;
    
    /**
     * راه‌اندازی قبل از هر تست
     * 
     * عملیات انجام شده:
     * - initialize کردن mock objects
     * - ایجاد instance سرویس با dependency injection
     * - راه‌اندازی داده‌های تست
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        couponService = new CouponService(couponRepository, authRepository, restaurantRepository, couponUsageRepository);
        
        // راه‌اندازی داده‌های تست
        setupTestData();
    }
    
    /**
     * راه‌اندازی داده‌های تست
     * 
     * این متد تمام objects مورد نیاز برای تست‌ها را ایجاد می‌کند:
     * - کاربران با نقش‌های مختلف
     * - رستوران نمونه
     * - کوپن‌های نمونه
     * - تاریخ‌های معتبر
     */
    private void setupTestData() {
        // ایجاد کاربران
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setEmail("admin@test.com");
        
        restaurantOwner = new User();
        restaurantOwner.setId(2L);
        restaurantOwner.setRole(User.Role.SELLER);
        restaurantOwner.setEmail("owner@test.com");
        
        customer = new User();
        customer.setId(3L);
        customer.setRole(User.Role.BUYER);
        customer.setEmail("customer@test.com");
        
        // ایجاد رستوران
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setOwnerId(2L);
        restaurant.setStatus(RestaurantStatus.APPROVED);
        
        // تنظیم تاریخ‌ها
        validFrom = LocalDateTime.now().minusDays(1);
        validUntil = LocalDateTime.now().plusDays(30);
        
        // ایجاد کوپن‌های نمونه
        percentageCoupon = Coupon.createPercentageCoupon("SAVE20", "20% off your order", 20.0, validFrom, validUntil);
        percentageCoupon.setId(1L);
        percentageCoupon.setCreatedBy(1L);
        percentageCoupon.setMinOrderAmount(50.0);
        percentageCoupon.setMaxDiscountAmount(100.0);
        percentageCoupon.setUsageLimit(1000);
        percentageCoupon.setUsedCount(0);
        
        fixedAmountCoupon = Coupon.createFixedAmountCoupon("SAVE10", "10 dollars off", 10.0, validFrom, validUntil);
        fixedAmountCoupon.setId(2L);
        fixedAmountCoupon.setCreatedBy(1L);
        fixedAmountCoupon.setMinOrderAmount(25.0);
        fixedAmountCoupon.setUsageLimit(500);
        fixedAmountCoupon.setUsedCount(0);
    }
    
    /**
     * تست‌های ایجاد کوپن
     * 
     * این دسته تمام سناریوهای ایجاد کوپن را آزمایش می‌کند:
     * - ایجاد موفق کوپن‌های درصدی و مبلغ ثابت
     * - ایجاد کوپن‌های اختصاصی رستوران
     * - اعتبارسنجی ورودی‌ها
     * - مدیریت مجوزهای دسترسی
     * - خطاهای validation
     */
    @Nested
    @DisplayName("Coupon Creation Tests")
    class CouponCreationTests {
        
        /**
         * تست ایجاد موفق کوپن درصدی
         * 
         * Scenario: ادمین کوپن درصدی جدید ایجاد می‌کند
         * Expected:
         * - کوپن با موفقیت ایجاد شود
         * - تمام فیلدها صحیح تنظیم شوند
         * - repository methods صحیح فراخوانی شوند
         */
        @Test
        @DisplayName("✅ ایجاد موفق کوپن درصدی")
        void shouldCreatePercentageCouponSuccessfully() {
            // Arrange - آماده‌سازی mock ها
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.existsByCode("SAVE20")).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenReturn(percentageCoupon);
            
            // Act - فراخوانی متد تحت تست
            Coupon result = couponService.createPercentageCoupon(
                "SAVE20", "20% off your order", 20.0, validFrom, validUntil, 1L, null);
            
            // Assert - بررسی نتایج
            assertNotNull(result, "کوپن ایجاد شده نباید null باشد");
            assertEquals("SAVE20", result.getCode(), "کد کوپن باید صحیح باشد");
            assertEquals(Coupon.CouponType.PERCENTAGE, result.getType(), "نوع کوپن باید درصدی باشد");
            assertEquals(20.0, result.getValue(), "مقدار درصد باید صحیح باشد");
            assertEquals(1L, result.getCreatedBy(), "شناسه ایجادکننده باید صحیح باشد");
            
            // بررسی فراخوانی متدهای repository
            verify(couponRepository).existsByCode("SAVE20");
            verify(couponRepository).save(any(Coupon.class));
        }
        
        /**
         * تست ایجاد موفق کوپن مبلغ ثابت
         * 
         * Scenario: ادمین کوپن مبلغ ثابت جدید ایجاد می‌کند
         * Expected:
         * - کوپن با موفقیت ایجاد شود
         * - نوع کوپن FIXED_AMOUNT باشد
         */
        @Test
        @DisplayName("✅ ایجاد موفق کوپن مبلغ ثابت")
        void shouldCreateFixedAmountCouponSuccessfully() {
            // Arrange
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.existsByCode("SAVE10")).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenReturn(fixedAmountCoupon);
            
            // Act
            Coupon result = couponService.createFixedAmountCoupon(
                "SAVE10", "10 dollars off", 10.0, validFrom, validUntil, 1L, null);
            
            // Assert
            assertNotNull(result, "کوپن ایجاد شده نباید null باشد");
            assertEquals("SAVE10", result.getCode(), "کد کوپن باید صحیح باشد");
            assertEquals(Coupon.CouponType.FIXED_AMOUNT, result.getType(), "نوع کوپن باید مبلغ ثابت باشد");
            assertEquals(10.0, result.getValue(), "مقدار تخفیف باید صحیح باشد");
            assertEquals(1L, result.getCreatedBy(), "شناسه ایجادکننده باید صحیح باشد");
            
            verify(couponRepository).existsByCode("SAVE10");
            verify(couponRepository).save(any(Coupon.class));
        }
        
        /**
         * تست ایجاد کوپن اختصاصی رستوران
         * 
         * Scenario: مالک رستوران کوپن اختصاصی برای رستوران خود ایجاد می‌کند
         * Expected:
         * - کوپن با موفقیت ایجاد شود
         * - رستوران به کوپن اختصاص یابد
         */
        @Test
        @DisplayName("✅ ایجاد کوپن اختصاصی رستوران")
        void shouldCreateRestaurantSpecificCoupon() {
            // Arrange
            when(authRepository.findById(2L)).thenReturn(Optional.of(restaurantOwner));
            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
            when(couponRepository.existsByCode("RESTAURANT20")).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenReturn(percentageCoupon);
            
            // Act
            Coupon result = couponService.createPercentageCoupon(
                "RESTAURANT20", "20% off at our restaurant", 20.0, validFrom, validUntil, 2L, 1L);
            
            // Assert
            assertNotNull(result, "کوپن رستوران باید ایجاد شود");
            // بررسی فراخوانی دو بار findById: یکبار برای validation، یکبار برای setting restaurant
            verify(restaurantRepository, times(2)).findById(1L);
            verify(couponRepository).save(any(Coupon.class));
        }
        
        /**
         * تست خطا برای کد تکراری
         * 
         * Scenario: تلاش برای ایجاد کوپن با کد موجود
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای کد کوپن تکراری")
        void shouldThrowExceptionWhenCodeExists() {
            // Arrange
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.existsByCode("SAVE20")).thenReturn(true);
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                couponService.createPercentageCoupon("SAVE20", "Description", 20.0, validFrom, validUntil, 1L, null));
            
            assertEquals("Coupon code already exists: SAVE20", exception.getMessage());
            verify(couponRepository, never()).save(any(Coupon.class));
        }
        
        /**
         * تست خطا برای درصد نامعتبر
         * 
         * Scenario: تلاش برای ایجاد کوپن با درصد بیش از 100
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای درصد نامعتبر")
        void shouldThrowExceptionForInvalidPercentage() {
            // Arrange
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.existsByCode("INVALID")).thenReturn(false);
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                couponService.createPercentageCoupon("INVALID", "Description", 150.0, validFrom, validUntil, 1L, null));
            
            assertEquals("Percentage must be between 0 and 100", exception.getMessage());
        }
        
        /**
         * تست خطا برای مبلغ منفی
         * 
         * Scenario: تلاش برای ایجاد کوپن با مبلغ منفی
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای مبلغ ثابت منفی")
        void shouldThrowExceptionForInvalidFixedAmount() {
            // Arrange
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.existsByCode("INVALID")).thenReturn(false);
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                couponService.createFixedAmountCoupon("INVALID", "Description", -10.0, validFrom, validUntil, 1L, null));
            
            assertEquals("Fixed amount must be positive", exception.getMessage());
        }
        
        /**
         * تست خطا برای مجوز نامعتبر - کوپن سراسری
         * 
         * Scenario: مالک رستوران تلاش می‌کند کوپن سراسری ایجاد کند
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای ایجاد کوپن سراسری توسط مالک رستوران")
        void shouldThrowExceptionForRestaurantOwnerCreatingGlobalCoupon() {
            // Arrange
            when(authRepository.findById(2L)).thenReturn(Optional.of(restaurantOwner));
            when(couponRepository.existsByCode("GLOBAL")).thenReturn(false);
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                couponService.createPercentageCoupon("GLOBAL", "Description", 20.0, validFrom, validUntil, 2L, null));
            
            assertEquals("Restaurant owners cannot create global coupons", exception.getMessage());
        }
        
        /**
         * تست خطا برای مجوز نامعتبر - رستوران غیرمتعلق
         * 
         * Scenario: مالک رستوران تلاش می‌کند برای رستوران دیگری کوپن ایجاد کند
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای ایجاد کوپن برای رستوران غیرمتعلق")
        void shouldThrowExceptionForUnauthorizedRestaurantCoupon() {
            // Arrange
            Restaurant otherRestaurant = new Restaurant();
            otherRestaurant.setId(2L);
            otherRestaurant.setOwnerId(99L); // مالک متفاوت
            
            when(authRepository.findById(2L)).thenReturn(Optional.of(restaurantOwner));
            when(restaurantRepository.findById(2L)).thenReturn(Optional.of(otherRestaurant));
            when(couponRepository.existsByCode("UNAUTHORIZED")).thenReturn(false);
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                couponService.createPercentageCoupon("UNAUTHORIZED", "Description", 20.0, validFrom, validUntil, 2L, 2L));
            
            assertEquals("Restaurant owners can only create coupons for their own restaurants", exception.getMessage());
        }
        
        /**
         * تست خطا برای مجوز نامعتبر - مشتری
         * 
         * Scenario: مشتری تلاش می‌کند کوپن ایجاد کند
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای ایجاد کوپن توسط مشتری")
        void shouldThrowExceptionForCustomerCreatingCoupon() {
            // Arrange
            when(authRepository.findById(3L)).thenReturn(Optional.of(customer));
            when(couponRepository.existsByCode("CUSTOMER")).thenReturn(false);
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                couponService.createPercentageCoupon("CUSTOMER", "Description", 20.0, validFrom, validUntil, 3L, null));
            
            assertEquals("Only admins and restaurant owners can create coupons", exception.getMessage());
        }
        
        /**
         * تست خطا برای بازه تاریخ نامعتبر
         * 
         * Scenario: تاریخ پایان قبل از تاریخ شروع است
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای بازه تاریخ نامعتبر")
        void shouldThrowExceptionForInvalidDateRange() {
            // Arrange
            LocalDateTime invalidValidUntil = validFrom.minusDays(1);
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                couponService.createPercentageCoupon("INVALID", "Description", 20.0, validFrom, invalidValidUntil, 1L, null));
            
            assertEquals("Valid until date must be after valid from date", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Coupon Application Tests")
    class CouponApplicationTests {
        
        @Test
        @DisplayName("Should apply percentage coupon successfully")
        void shouldApplyPercentageCouponSuccessfully() {
            // Arrange
            double orderAmount = 100.0;
            when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("SAVE20", orderAmount, 1L, 3L);
            
            // Assert
            assertTrue(result.isSuccess());
            assertEquals(percentageCoupon, result.getCoupon());
            assertEquals(20.0, result.getDiscountAmount()); // 20% of 100
        }
        
        @Test
        @DisplayName("Should apply fixed amount coupon successfully")
        void shouldApplyFixedAmountCouponSuccessfully() {
            // Arrange
            double orderAmount = 50.0;
            when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(fixedAmountCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("SAVE10", orderAmount, 1L, 3L);
            
            // Assert
            assertTrue(result.isSuccess());
            assertEquals(fixedAmountCoupon, result.getCoupon());
            assertEquals(10.0, result.getDiscountAmount());
        }
        
        @Test
        @DisplayName("Should respect maximum discount amount for percentage coupon")
        void shouldRespectMaximumDiscountAmount() {
            // Arrange
            double orderAmount = 1000.0; // Would give 200 discount, but max is 100
            when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("SAVE20", orderAmount, 1L, 3L);
            
            // Assert
            assertTrue(result.isSuccess());
            assertEquals(100.0, result.getDiscountAmount()); // Capped at max discount
        }
        
        @Test
        @DisplayName("Should fail when coupon code not found")
        void shouldFailWhenCouponCodeNotFound() {
            // Arrange
            when(couponRepository.findByCode("NOTFOUND")).thenReturn(Optional.empty());
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("NOTFOUND", 100.0, 1L, 3L);
            
            // Assert
            assertFalse(result.isSuccess());
            assertEquals("Coupon code not found", result.getErrorMessage());
        }
        
        @Test
        @DisplayName("Should fail when order amount is below minimum")
        void shouldFailWhenOrderAmountBelowMinimum() {
            // Arrange
            double orderAmount = 30.0; // Below minimum of 50
            when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("SAVE20", orderAmount, 1L, 3L);
            
            // Assert
            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("Minimum order amount not met"));
        }
        
        @Test
        @DisplayName("Should fail when coupon is expired")
        void shouldFailWhenCouponIsExpired() {
            // Arrange
            Coupon expiredCoupon = Coupon.createPercentageCoupon("EXPIRED", "Expired coupon", 20.0, 
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));
            when(couponRepository.findByCode("EXPIRED")).thenReturn(Optional.of(expiredCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("EXPIRED", 100.0, 1L, 3L);
            
            // Assert
            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("expired"));
        }
        
        @Test
        @DisplayName("Should fail when coupon is not active")
        void shouldFailWhenCouponIsNotActive() {
            // Arrange
            percentageCoupon.setIsActive(false);
            when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("SAVE20", 100.0, 1L, 3L);
            
            // Assert
            assertFalse(result.isSuccess());
            assertEquals("Coupon is not active", result.getErrorMessage());
        }
        
        @Test
        @DisplayName("Should fail when usage limit is exceeded")
        void shouldFailWhenUsageLimitExceeded() {
            // Arrange
            percentageCoupon.setUsageLimit(10);
            percentageCoupon.setUsedCount(10);
            when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("SAVE20", 100.0, 1L, 3L);
            
            // Assert
            assertFalse(result.isSuccess());
            assertEquals("Coupon usage limit exceeded", result.getErrorMessage());
        }
        
        @Test
        @DisplayName("Should fail when coupon is restaurant-specific and doesn't match")
        void shouldFailWhenRestaurantMismatch() {
            // Arrange
            percentageCoupon.setRestaurant(restaurant);
            when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("SAVE20", 100.0, 999L, 3L);
            
            // Assert
            assertFalse(result.isSuccess());
            assertEquals("Coupon is not valid for this restaurant", result.getErrorMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for invalid inputs")
        void shouldThrowExceptionForInvalidInputs() {
            // Test null coupon code
            assertThrows(IllegalArgumentException.class, () ->
                couponService.applyCoupon(null, 100.0, 1L, 3L));
            
            // Test empty coupon code
            assertThrows(IllegalArgumentException.class, () ->
                couponService.applyCoupon("", 100.0, 1L, 3L));
            
            // Test null order amount
            assertThrows(IllegalArgumentException.class, () ->
                couponService.applyCoupon("SAVE20", null, 1L, 3L));
            
            // Test negative order amount
            assertThrows(IllegalArgumentException.class, () ->
                couponService.applyCoupon("SAVE20", -10.0, 1L, 3L));
            
            // Test null user ID
            assertThrows(IllegalArgumentException.class, () ->
                couponService.applyCoupon("SAVE20", 100.0, 1L, null));
        }
    }
    
    @Nested
    @DisplayName("Coupon Usage Tests")
    class CouponUsageTests {
        
        @Test
        @DisplayName("Should use coupon successfully")
        void shouldUseCouponSuccessfully() {
            // Arrange
            when(couponRepository.findById(1L)).thenReturn(Optional.of(percentageCoupon));
            when(couponRepository.update(any(Coupon.class))).thenReturn(percentageCoupon);
            
            // Act
            couponService.useCoupon(1L);
            
            // Assert
            assertEquals(1, percentageCoupon.getUsedCount());
            verify(couponRepository).update(percentageCoupon);
        }
        
        @Test
        @DisplayName("Should revert coupon usage successfully")
        void shouldRevertCouponUsageSuccessfully() {
            // Arrange
            percentageCoupon.setUsedCount(5);
            when(couponRepository.findById(1L)).thenReturn(Optional.of(percentageCoupon));
            when(couponRepository.update(any(Coupon.class))).thenReturn(percentageCoupon);
            
            // Act
            couponService.revertCouponUsage(1L);
            
            // Assert
            assertEquals(4, percentageCoupon.getUsedCount());
            verify(couponRepository).update(percentageCoupon);
        }
        
        @Test
        @DisplayName("Should throw exception when using non-existent coupon")
        void shouldThrowExceptionWhenUsingNonExistentCoupon() {
            // Arrange
            when(couponRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> couponService.useCoupon(999L));
        }
        
        @Test
        @DisplayName("Should throw exception when reverting usage of non-existent coupon")
        void shouldThrowExceptionWhenRevertingNonExistentCoupon() {
            // Arrange
            when(couponRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> couponService.revertCouponUsage(999L));
        }
    }
    
    @Nested
    @DisplayName("Coupon Management Tests")
    class CouponManagementTests {
        
        @Test
        @DisplayName("Should get coupon by ID successfully")
        void shouldGetCouponByIdSuccessfully() {
            // Arrange
            when(couponRepository.findById(1L)).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            Coupon result = couponService.getCoupon(1L);
            
            // Assert
            assertEquals(percentageCoupon, result);
        }
        
        @Test
        @DisplayName("Should throw exception when getting non-existent coupon")
        void shouldThrowExceptionWhenGettingNonExistentCoupon() {
            // Arrange
            when(couponRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> couponService.getCoupon(999L));
        }
        
        @Test
        @DisplayName("Should get coupon by code successfully")
        void shouldGetCouponByCodeSuccessfully() {
            // Arrange
            when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            Coupon result = couponService.getCouponByCode("SAVE20");
            
            // Assert
            assertEquals(percentageCoupon, result);
        }
        
        @Test
        @DisplayName("Should get applicable coupons successfully")
        void shouldGetApplicableCouponsSuccessfully() {
            // Arrange
            List<Coupon> expectedCoupons = Arrays.asList(percentageCoupon, fixedAmountCoupon);
            when(couponRepository.findApplicableCoupons(100.0, 1L)).thenReturn(expectedCoupons);
            
            // Act
            List<Coupon> result = couponService.getApplicableCoupons(100.0, 1L);
            
            // Assert
            assertEquals(expectedCoupons, result);
            verify(couponRepository).findApplicableCoupons(100.0, 1L);
        }
        
        @Test
        @DisplayName("Should activate coupon successfully")
        void shouldActivateCouponSuccessfully() {
            // Arrange
            percentageCoupon.setIsActive(false);
            when(couponRepository.findById(1L)).thenReturn(Optional.of(percentageCoupon));
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.update(any(Coupon.class))).thenReturn(percentageCoupon);
            
            // Act
            couponService.activateCoupon(1L, 1L);
            
            // Assert
            assertTrue(percentageCoupon.getIsActive());
            verify(couponRepository).update(percentageCoupon);
        }
        
        @Test
        @DisplayName("Should deactivate coupon successfully")
        void shouldDeactivateCouponSuccessfully() {
            // Arrange
            when(couponRepository.findById(1L)).thenReturn(Optional.of(percentageCoupon));
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.update(any(Coupon.class))).thenReturn(percentageCoupon);
            
            // Act
            couponService.deactivateCoupon(1L, 1L);
            
            // Assert
            assertFalse(percentageCoupon.getIsActive());
            verify(couponRepository).update(percentageCoupon);
        }
        
        @Test
        @DisplayName("Should delete unused coupon successfully")
        void shouldDeleteUnusedCouponSuccessfully() {
            // Arrange
            when(couponRepository.findById(1L)).thenReturn(Optional.of(percentageCoupon));
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.delete(1L)).thenReturn(true);
            
            // Act
            couponService.deleteCoupon(1L, 1L);
            
            // Assert
            verify(couponRepository).delete(1L);
        }
        
        @Test
        @DisplayName("Should throw exception when deleting used coupon")
        void shouldThrowExceptionWhenDeletingUsedCoupon() {
            // Arrange
            percentageCoupon.setUsedCount(5);
            when(couponRepository.findById(1L)).thenReturn(Optional.of(percentageCoupon));
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            
            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                couponService.deleteCoupon(1L, 1L));
            
            assertEquals("Cannot delete coupon that has been used", exception.getMessage());
            verify(couponRepository, never()).delete(1L);
        }
        
        @Test
        @DisplayName("Should throw exception when non-owner tries to update coupon")
        void shouldThrowExceptionWhenNonOwnerTriesToUpdateCoupon() {
            // Arrange
            when(couponRepository.findById(1L)).thenReturn(Optional.of(percentageCoupon));
            when(authRepository.findById(3L)).thenReturn(Optional.of(customer));
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                couponService.activateCoupon(1L, 3L));
            
            assertEquals("Users can only update their own coupons", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should get coupon statistics successfully")
        void shouldGetCouponStatisticsSuccessfully() {
            // Arrange
            when(couponRepository.countAll()).thenReturn(100L);
            when(couponRepository.countActive()).thenReturn(80L);
            when(couponRepository.findCouponsExpiringSoon(7)).thenReturn(Arrays.asList(percentageCoupon));
            when(couponRepository.findExpiredCoupons()).thenReturn(Arrays.asList(fixedAmountCoupon));
            
            // Act
            CouponService.CouponStatistics stats = couponService.getCouponStatistics();
            
            // Assert
            assertEquals(100L, stats.getTotalCoupons());
            assertEquals(80L, stats.getActiveCoupons());
            assertEquals(1L, stats.getExpiringSoon());
            assertEquals(1L, stats.getExpired());
            assertEquals(20L, stats.getInactiveCoupons());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Should handle null inputs gracefully")
        void shouldHandleNullInputsGracefully() {
            // Test various null input scenarios
            assertThrows(IllegalArgumentException.class, () ->
                couponService.getCoupon(null));
            
            assertThrows(IllegalArgumentException.class, () ->
                couponService.getCouponByCode(null));
            
            assertThrows(IllegalArgumentException.class, () ->
                couponService.getApplicableCoupons(null, 1L));
            
            assertThrows(IllegalArgumentException.class, () ->
                couponService.getCouponsByCreator(null));
        }
        
        @Test
        @DisplayName("Should handle boundary values correctly")
        void shouldHandleBoundaryValuesCorrectly() {
            // Test 0% discount
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.existsByCode("ZERO")).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            Coupon zeroCoupon = couponService.createPercentageCoupon(
                "ZERO", "0% discount", 0.0, validFrom, validUntil, 1L, null);
            
            assertEquals(0.0, zeroCoupon.getValue());
            
            // Test 100% discount
            Coupon fullCoupon = couponService.createPercentageCoupon(
                "FULL", "100% discount", 100.0, validFrom, validUntil, 1L, null);
            
            assertEquals(100.0, fullCoupon.getValue());
        }
        
        @Test
        @DisplayName("Should handle large order amounts correctly")
        void shouldHandleLargeOrderAmountsCorrectly() {
            // Arrange
            double largeOrderAmount = 999999.99;
            percentageCoupon.setMinOrderAmount(null); // No minimum
            percentageCoupon.setMaxDiscountAmount(null); // No maximum
            when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("SAVE20", largeOrderAmount, 1L, 3L);
            
            // Assert
            assertTrue(result.isSuccess());
            assertEquals(largeOrderAmount * 0.2, result.getDiscountAmount(), 0.01);
        }
        
        @Test
        @DisplayName("Should handle coupon with zero usage limit")
        void shouldHandleCouponWithZeroUsageLimit() {
            // Arrange
            percentageCoupon.setUsageLimit(0);
            when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("SAVE20", 100.0, 1L, 3L);
            
            // Assert
            assertFalse(result.isSuccess());
            assertEquals("Coupon usage limit exceeded", result.getErrorMessage());
        }
        
        @Test
        @DisplayName("Should handle very small discount amounts")
        void shouldHandleVerySmallDiscountAmounts() {
            // Arrange
            Coupon smallCoupon = Coupon.createFixedAmountCoupon("SMALL", "1 cent off", 0.01, validFrom, validUntil);
            when(couponRepository.findByCode("SMALL")).thenReturn(Optional.of(smallCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("SMALL", 100.0, 1L, 3L);
            
            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0.01, result.getDiscountAmount(), 0.001);
        }
    }
} 