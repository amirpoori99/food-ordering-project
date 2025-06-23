package com.myapp.coupon;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Coupon;
import com.myapp.common.models.CouponUsage;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Complete test suite covering all missing scenarios for 100% coupon system coverage
 */
@DisplayName("Complete Coupon System Tests")
public class CouponCompleteTest {
    
    @Mock
    private CouponRepository couponRepository;
    
    @Mock
    private AuthRepository authRepository;
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private CouponUsageRepository couponUsageRepository;
    
    private CouponService couponService;
    
    // Test data
    private User adminUser;
    private User restaurantOwner;
    private User customer;
    private Restaurant restaurant;
    private Coupon percentageCoupon;
    private Coupon fixedAmountCoupon;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        couponService = new CouponService(couponRepository, authRepository, restaurantRepository, couponUsageRepository);
        
        setupTestData();
    }
    
    private void setupTestData() {
        // Users
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
        
        // Restaurant
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setOwnerId(2L);
        restaurant.setStatus(RestaurantStatus.APPROVED);
        
        // Dates
        validFrom = LocalDateTime.now().minusDays(1);
        validUntil = LocalDateTime.now().plusDays(30);
        
        // Coupons
        percentageCoupon = Coupon.createPercentageCoupon("COMPLETE20", "20% off your order", 20.0, validFrom, validUntil);
        percentageCoupon.setId(1L);
        percentageCoupon.setCreatedBy(1L);
        percentageCoupon.setMinOrderAmount(50.0);
        percentageCoupon.setMaxDiscountAmount(100.0);
        percentageCoupon.setUsageLimit(1000);
        percentageCoupon.setUsedCount(0);
        
        fixedAmountCoupon = Coupon.createFixedAmountCoupon("COMPLETE10", "10 dollars off", 10.0, validFrom, validUntil);
        fixedAmountCoupon.setId(2L);
        fixedAmountCoupon.setCreatedBy(1L);
        fixedAmountCoupon.setMinOrderAmount(25.0);
        fixedAmountCoupon.setUsageLimit(500);
        fixedAmountCoupon.setUsedCount(0);
    }
    
    @Nested
    @DisplayName("Per-User Limit Tests")
    class PerUserLimitTests {
        
        @Test
        @DisplayName("Should enforce per-user limit correctly")
        void shouldEnforcePerUserLimitCorrectly() {
            // Arrange
            percentageCoupon.setPerUserLimit(2);
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(1L, 3L)).thenReturn(2L);
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("COMPLETE20", 100.0, null, 3L);
            
            // Assert
            assertFalse(result.isSuccess());
            assertEquals("User has exceeded the per-user limit for this coupon", result.getErrorMessage());
        }
        
        @Test
        @DisplayName("Should allow usage when under per-user limit")
        void shouldAllowUsageWhenUnderPerUserLimit() {
            // Arrange
            percentageCoupon.setPerUserLimit(3);
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(1L, 3L)).thenReturn(1L);
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("COMPLETE20", 100.0, null, 3L);
            
            // Assert
            assertTrue(result.isSuccess());
            assertEquals(20.0, result.getDiscountAmount());
        }
        
        @Test
        @DisplayName("Should track coupon usage with full details")
        void shouldTrackCouponUsageWithFullDetails() {
            // Arrange
            when(couponRepository.findById(1L)).thenReturn(Optional.of(percentageCoupon));
            when(couponRepository.update(any(Coupon.class))).thenReturn(percentageCoupon);
            when(couponUsageRepository.save(any(CouponUsage.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            CouponUsage result = couponService.useCouponWithTracking(1L, 3L, 100L, 20.0, 100.0);
            
            // Assert
            assertNotNull(result);
            verify(couponUsageRepository).save(any(CouponUsage.class));
            verify(couponRepository).update(percentageCoupon);
            assertEquals(1, percentageCoupon.getUsedCount());
        }
        
        @Test
        @DisplayName("Should handle unlimited per-user usage")
        void shouldHandleUnlimitedPerUserUsage() {
            // Arrange
            percentageCoupon.setPerUserLimit(null); // Unlimited
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(1L, 3L)).thenReturn(100L); // High usage
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("COMPLETE20", 100.0, null, 3L);
            
            // Assert
            assertTrue(result.isSuccess());
            assertEquals(20.0, result.getDiscountAmount());
        }
        
        @Test
        @DisplayName("Should handle zero per-user limit")
        void shouldHandleZeroPerUserLimit() {
            // Arrange
            percentageCoupon.setPerUserLimit(0);
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(1L, 3L)).thenReturn(0L);
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("COMPLETE20", 100.0, null, 3L);
            
            // Assert - Zero limit means no one can use it
            // Since user usage is 0 and limit is 0, it should fail because 0 >= 0
            assertFalse(result.isSuccess());
            assertEquals("User has exceeded the per-user limit for this coupon", result.getErrorMessage());
        }
    }
    
    @Nested
    @DisplayName("Complex Business Scenario Tests")
    class ComplexBusinessScenarioTests {
        
        @Test
        @DisplayName("Should handle restaurant-specific coupon validation correctly")
        void shouldHandleRestaurantSpecificCouponValidation() {
            // Arrange
            percentageCoupon.setRestaurant(restaurant);
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(anyLong(), anyLong())).thenReturn(0L);
            
            // Act & Assert - Correct restaurant
            CouponService.CouponApplicationResult result1 = couponService.applyCoupon("COMPLETE20", 100.0, 1L, 3L);
            assertTrue(result1.isSuccess());
            
            // Act & Assert - Wrong restaurant
            CouponService.CouponApplicationResult result2 = couponService.applyCoupon("COMPLETE20", 100.0, 2L, 3L);
            assertFalse(result2.isSuccess());
            assertEquals("Coupon is not valid for this restaurant", result2.getErrorMessage());
        }
        
        @Test
        @DisplayName("Should handle multiple coupon constraints together")
        void shouldHandleMultipleCouponConstraintsTogether() {
            // Arrange - Coupon with multiple constraints
            percentageCoupon.setMinOrderAmount(50.0);
            percentageCoupon.setMaxDiscountAmount(15.0);
            percentageCoupon.setUsageLimit(100);
            percentageCoupon.setPerUserLimit(2);
            percentageCoupon.setUsedCount(0);
            
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(1L, 3L)).thenReturn(0L);
            
            // Act & Assert - Valid order meeting all constraints
            CouponService.CouponApplicationResult result = couponService.applyCoupon("COMPLETE20", 100.0, null, 3L);
            assertTrue(result.isSuccess());
            assertEquals(15.0, result.getDiscountAmount()); // Max discount applied
        }
        
        @Test
        @DisplayName("Should handle expired coupon edge case")
        void shouldHandleExpiredCouponEdgeCase() {
            // Arrange - Coupon that just expired
            LocalDateTime justExpired = LocalDateTime.now().minusSeconds(1);
            percentageCoupon.setValidUntil(justExpired);
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("COMPLETE20", 100.0, null, 3L);
            
            // Assert
            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("expired") || result.getErrorMessage().contains("not yet valid"));
        }
        
        @Test
        @DisplayName("Should handle future coupon edge case")
        void shouldHandleFutureCouponEdgeCase() {
            // Arrange - Coupon that's not valid yet
            LocalDateTime future = LocalDateTime.now().plusSeconds(1);
            percentageCoupon.setValidFrom(future);
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            
            // Act
            CouponService.CouponApplicationResult result = couponService.applyCoupon("COMPLETE20", 100.0, null, 3L);
            
            // Assert
            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("not yet valid"));
        }
        
        @Test
        @DisplayName("Should calculate exact discount for edge amounts")
        void shouldCalculateExactDiscountForEdgeAmounts() {
            // Arrange - Test with minimum order amount
            percentageCoupon.setMinOrderAmount(50.0);
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(anyLong(), anyLong())).thenReturn(0L);
            
            // Act - Exactly minimum amount
            CouponService.CouponApplicationResult result1 = couponService.applyCoupon("COMPLETE20", 50.0, null, 3L);
            assertTrue(result1.isSuccess());
            assertEquals(10.0, result1.getDiscountAmount()); // 20% of 50
            
            // Act - Just below minimum
            CouponService.CouponApplicationResult result2 = couponService.applyCoupon("COMPLETE20", 49.99, null, 3L);
            assertFalse(result2.isSuccess());
        }
        
        @Test
        @DisplayName("Should handle complex coupon chain scenarios")
        void shouldHandleComplexCouponChainScenarios() {
            // Arrange - Scenario: User applies coupon, order is cancelled, coupon is reverted
            when(couponRepository.findById(1L)).thenReturn(Optional.of(percentageCoupon));
            when(couponRepository.update(any(Coupon.class))).thenReturn(percentageCoupon);
            when(couponUsageRepository.save(any(CouponUsage.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            // 1. Use coupon
            CouponUsage usage = couponService.useCouponWithTracking(1L, 3L, 100L, 20.0, 100.0);
            assertEquals(1, percentageCoupon.getUsedCount());
            
            // 2. Revert coupon (order cancelled)
            couponService.revertCouponUsage(1L);
            assertEquals(0, percentageCoupon.getUsedCount());
            
            // 3. Use again (new order)
            couponService.useCouponWithTracking(1L, 3L, 101L, 15.0, 75.0);
            assertEquals(1, percentageCoupon.getUsedCount());
        }
    }
    
    @Nested
    @DisplayName("Advanced Error Handling Tests")
    class AdvancedErrorHandlingTests {
        
        @Test
        @DisplayName("Should provide specific error messages for different scenarios")
        void shouldProvideSpecificErrorMessagesForDifferentScenarios() {
            // Test different error scenarios and verify specific messages
            
            // Invalid code
            when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());
            CouponService.CouponApplicationResult result1 = couponService.applyCoupon("INVALID", 100.0, null, 3L);
            assertEquals("Coupon code not found", result1.getErrorMessage());
            
            // Minimum order not met
            percentageCoupon.setMinOrderAmount(100.0);
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(anyLong(), anyLong())).thenReturn(0L);
            CouponService.CouponApplicationResult result2 = couponService.applyCoupon("COMPLETE20", 50.0, null, 3L);
            assertTrue(result2.getErrorMessage().contains("Minimum order amount not met"));
            
            // Usage limit exceeded
            percentageCoupon.setUsageLimit(5);
            percentageCoupon.setUsedCount(5);
            percentageCoupon.setMinOrderAmount(null);
            CouponService.CouponApplicationResult result3 = couponService.applyCoupon("COMPLETE20", 100.0, null, 3L);
            assertTrue(result3.getErrorMessage().contains("usage limit exceeded") || 
                      result3.getErrorMessage().contains("not active"));
        }
        
        @Test
        @DisplayName("Should validate input parameters with descriptive messages")
        void shouldValidateInputParametersWithDescriptiveMessages() {
            // Test null coupon code
            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, 
                () -> couponService.applyCoupon(null, 100.0, null, 3L));
            assertTrue(ex1.getMessage().contains("Coupon code cannot be empty"));
            
            // Test empty coupon code
            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, 
                () -> couponService.applyCoupon("", 100.0, null, 3L));
            assertTrue(ex2.getMessage().contains("Coupon code cannot be empty"));
            
            // Test negative order amount
            IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, 
                () -> couponService.applyCoupon("COMPLETE20", -10.0, null, 3L));
            assertTrue(ex3.getMessage().contains("Order amount must be positive"));
            
            // Test null user ID
            IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class, 
                () -> couponService.applyCoupon("COMPLETE20", 100.0, null, null));
            assertTrue(ex4.getMessage().contains("User ID cannot be null"));
        }
        
        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Arrange
            when(couponRepository.findByCode("ERROR")).thenThrow(new RuntimeException("Database error"));
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                couponService.applyCoupon("ERROR", 100.0, null, 3L));
            
            assertEquals("Database error", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should handle edge case inputs correctly")
        void shouldHandleEdgeCaseInputsCorrectly() {
            // Very small order amount
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(anyLong(), anyLong())).thenReturn(0L);
            
            CouponService.CouponApplicationResult result1 = couponService.applyCoupon("COMPLETE20", 0.01, null, 3L);
            assertFalse(result1.isSuccess()); // Below minimum order amount
            
            // Very large order amount
            percentageCoupon.setMinOrderAmount(null);
            CouponService.CouponApplicationResult result2 = couponService.applyCoupon("COMPLETE20", 999999999.99, null, 3L);
            assertTrue(result2.isSuccess());
            
            // Zero order amount
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> couponService.applyCoupon("COMPLETE20", 0.0, null, 3L));
            assertTrue(ex.getMessage().contains("Order amount must be positive"));
        }
    }
    
    @Nested
    @DisplayName("Coupon Creation with Settings Tests")
    class CouponCreationWithSettingsTests {
        
        @Test
        @DisplayName("Should create coupon with all settings correctly")
        void shouldCreateCouponWithAllSettingsCorrectly() {
            // Arrange
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.existsByCode("SETTINGS")).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
                Coupon c = invocation.getArgument(0);
                c.setId(100L);
                return c;
            });
            when(couponRepository.update(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            Coupon result = couponService.createCouponWithSettings(
                "SETTINGS", "Complex coupon", Coupon.CouponType.PERCENTAGE, 25.0,
                validFrom, validUntil, 100.0, 50.0, 1000, 5,
                1L, null);
            
            // Assert
            assertNotNull(result);
            assertEquals("SETTINGS", result.getCode());
            assertEquals(25.0, result.getValue());
            assertEquals(100.0, result.getMinOrderAmount());
            assertEquals(50.0, result.getMaxDiscountAmount());
            assertEquals(1000, result.getUsageLimit());
            assertEquals(5, result.getPerUserLimit());
        }
        
        @Test
        @DisplayName("Should create fixed amount coupon with settings")
        void shouldCreateFixedAmountCouponWithSettings() {
            // Arrange
            when(authRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(couponRepository.existsByCode("FIXED_SET")).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
                Coupon c = invocation.getArgument(0);
                c.setId(101L);
                return c;
            });
            when(couponRepository.update(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            Coupon result = couponService.createCouponWithSettings(
                "FIXED_SET", "Fixed amount with settings", Coupon.CouponType.FIXED_AMOUNT, 15.0,
                validFrom, validUntil, 50.0, null, 500, 3,
                1L, null);
            
            // Assert
            assertNotNull(result);
            assertEquals("FIXED_SET", result.getCode());
            assertEquals(Coupon.CouponType.FIXED_AMOUNT, result.getType());
            assertEquals(15.0, result.getValue());
            assertEquals(50.0, result.getMinOrderAmount());
            assertNull(result.getMaxDiscountAmount()); // Not applicable for fixed amount
            assertEquals(500, result.getUsageLimit());
            assertEquals(3, result.getPerUserLimit());
        }
    }
    
    @Nested
    @DisplayName("Additional Coverage Tests")
    class AdditionalCoverageTests {
        
        @Test
        @DisplayName("Should handle whitespace in coupon codes")
        void shouldHandleWhitespaceInCouponCodes() {
            // Arrange
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(anyLong(), anyLong())).thenReturn(0L);
            
            // Act - Test with leading/trailing whitespace
            CouponService.CouponApplicationResult result1 = couponService.applyCoupon("  COMPLETE20  ", 100.0, null, 3L);
            assertTrue(result1.isSuccess());
            
            // Act - Test with tabs and spaces
            CouponService.CouponApplicationResult result2 = couponService.applyCoupon("\t COMPLETE20 \t", 100.0, null, 3L);
            assertTrue(result2.isSuccess());
            
            // Verify the code was trimmed and uppercased
            verify(couponRepository, atLeast(2)).findByCode("COMPLETE20");
        }
        
        @Test
        @DisplayName("Should handle case insensitive coupon codes")
        void shouldHandleCaseInsensitiveCouponCodes() {
            // Arrange
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(anyLong(), anyLong())).thenReturn(0L);
            
            // Act - Test with lowercase
            CouponService.CouponApplicationResult result1 = couponService.applyCoupon("complete20", 100.0, null, 3L);
            assertTrue(result1.isSuccess());
            
            // Act - Test with mixed case
            CouponService.CouponApplicationResult result2 = couponService.applyCoupon("Complete20", 100.0, null, 3L);
            assertTrue(result2.isSuccess());
            
            // Verify codes were converted to uppercase
            verify(couponRepository, atLeast(2)).findByCode("COMPLETE20");
        }
        
        @Test
        @DisplayName("Should handle very precise decimal calculations")
        void shouldHandleVeryPreciseDecimalCalculations() {
            // Arrange
            when(couponRepository.findByCode("COMPLETE20")).thenReturn(Optional.of(percentageCoupon));
            when(couponUsageRepository.countActiveByCouponIdAndUserId(anyLong(), anyLong())).thenReturn(0L);
            
            // Act - Test with precise decimal order amount
            CouponService.CouponApplicationResult result = couponService.applyCoupon("COMPLETE20", 123.456789, null, 3L);
            
            // Assert
            assertTrue(result.isSuccess());
            assertEquals(24.6913578, result.getDiscountAmount(), 0.0000001); // 20% of 123.456789
        }
    }
} 