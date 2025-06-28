package com.myapp.review;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Rating;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * کلاس تست جامع برای RatingService
 * 
 * این کلاس تمام سناریوهای مختلف عملیات RatingService را تست می‌کند:
 * 
 * === گروه‌های تست اصلی ===
 * - ConstructorTests: تست سازنده‌ها
 * - CreateRatingTests: تست ایجاد نظر جدید
 * - UpdateRatingTests: تست به‌روزرسانی نظر
 * - GetRatingTests: تست دریافت نظر بر اساس شناسه
 * - GetRestaurantRatingsTests: تست دریافت نظرات رستوران
 * - GetUserRatingsTests: تست دریافت نظرات کاربر
 * - GetRatingStatsTests: تست آمار امتیازات
 * - CheckUserRatingTests: تست بررسی نظردهی کاربر
 * - GetUserRatingForRestaurantTests: تست نظر کاربر برای رستوران
 * - DeleteRatingTests: تست حذف نظر
 * - HelpfulMarkTests: تست علامت‌گذاری مفید
 * - AdminOperationsTests: تست عملیات مدیریتی
 * - QueryOperationsTests: تست جستجوهای پیشرفته
 * - RatingStatsTests: تست کلاس آمار
 * - PerformanceTests: تست عملکرد
 * - EdgeCasesTests: تست موارد خاص
 * 
 * === استراتژی تست ===
 * - Unit Testing: تست واحد با Mock objects
 * - Business Logic Testing: تست منطق کسب‌وکار
 * - Exception Testing: تست مدیریت خطاها
 * - Edge Case Testing: تست موارد مرزی
 * - Performance Testing: تست عملکرد
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("RatingService Tests")
class RatingServiceTest {

    /** Mock repository برای نظرات */
    @Mock
    private RatingRepository mockRatingRepository;
    
    /** Mock repository برای کاربران */
    @Mock
    private AuthRepository mockAuthRepository;
    
    /** Mock repository برای رستوران‌ها */
    @Mock
    private RestaurantRepository mockRestaurantRepository;

    /** سرویس تحت تست */
    private RatingService ratingService;
    /** کاربر تست */
    private User testUser;
    /** رستوران تست */
    private Restaurant testRestaurant;
    /** نظر تست */
    private Rating testRating;

    /**
     * راه‌اندازی اولیه قبل از هر تست
     * 
     * ایجاد mock objects و داده‌های تست
     */
    @BeforeEach
    void setUp() {
        // راه‌اندازی Mockito
        MockitoAnnotations.openMocks(this);
        ratingService = new RatingService(mockRatingRepository, mockAuthRepository, mockRestaurantRepository);
        
        // ایجاد داده‌های تست
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setPhone("1234567890");
        testUser.setEmail("test@example.com");
        
        testRestaurant = new Restaurant();
        testRestaurant.setId(2L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setStatus(RestaurantStatus.APPROVED);
        testRestaurant.setOwnerId(3L);
        
        testRating = new Rating(testUser, testRestaurant, 4, "Great food!");
        testRating.setId(1L);
        testRating.setCreatedAt(LocalDateTime.now());
    }

    /**
     * گروه تست‌های سازنده
     * 
     * تست صحت عملکرد سازنده‌های مختلف RatingService
     */
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        /**
         * تست سازنده پیش‌فرض
         * 
         * بررسی ایجاد موفق سرویس با سازنده پیش‌فرض
         */
        @Test
        @DisplayName("Should create service with default constructor")
        void shouldCreateServiceWithDefaultConstructor() {
            // عمل: ایجاد سرویس با سازنده پیش‌فرض
            RatingService service = new RatingService();
            
            // بررسی: سرویس نباید null باشد
            assertNotNull(service);
        }
        
        /**
         * تست سازنده با تزریق وابستگی
         * 
         * بررسی ایجاد موفق سرویس با تزریق dependencies
         */
        @Test
        @DisplayName("Should create service with dependency injection")
        void shouldCreateServiceWithDependencyInjection() {
            // عمل: ایجاد سرویس با dependency injection
            RatingService service = new RatingService(mockRatingRepository, mockAuthRepository, mockRestaurantRepository);
            
            // بررسی: سرویس نباید null باشد
            assertNotNull(service);
        }
    }

    /**
     * گروه تست‌های ایجاد نظر
     * 
     * تست تمام سناریوهای ایجاد نظر جدید
     */
    @Nested
    @DisplayName("Create Rating Tests")
    class CreateRatingTests {

        /**
         * تست ایجاد موفق نظر
         * 
         * بررسی ایجاد نظر در شرایط عادی
         */
        @Test
        @DisplayName("Should create rating successfully")
        void shouldCreateRatingSuccessfully() {
            // آماده‌سازی: تنظیم رفتار mock objects
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            // عمل: ایجاد نظر
            Rating result = ratingService.createRating(1L, 2L, 4, "Great food!");

            // بررسی: نتیجه صحیح باشد
            assertNotNull(result);
            assertEquals(4, result.getRatingScore());
            assertEquals("Great food!", result.getReviewText());
            verify(mockRatingRepository).save(any(Rating.class));
        }

        /**
         * تست ایجاد نظر با متن null
         * 
         * بررسی ایجاد نظر بدون متن توضیحی
         */
        @Test
        @DisplayName("Should create rating with null review text")
        void shouldCreateRatingWithNullReviewText() {
            // آماده‌سازی
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            // عمل: ایجاد نظر بدون متن
            Rating result = ratingService.createRating(1L, 2L, 4, null);

            // بررسی: نظر ایجاد شود
            assertNotNull(result);
            verify(mockRatingRepository).save(any(Rating.class));
        }

        /**
         * تست خطای عدم وجود کاربر
         * 
         * بررسی پرتاب exception زمانی که کاربر وجود ندارد
         */
        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // آماده‌سازی: کاربر یافت نشود
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.empty());

            // عمل و بررسی: باید NotFoundException پرتاب شود
            assertThrows(NotFoundException.class, () -> 
                ratingService.createRating(1L, 2L, 4, "Great food!"));
        }

        /**
         * تست خطای عدم وجود رستوران
         * 
         * بررسی پرتاب exception زمانی که رستوران وجود ندارد
         */
        @Test
        @DisplayName("Should throw exception when restaurant not found")
        void shouldThrowExceptionWhenRestaurantNotFound() {
            // آماده‌سازی: کاربر یافت شود اما رستوران نه
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.empty());

            // عمل و بررسی: باید NotFoundException پرتاب شود
            assertThrows(NotFoundException.class, () -> 
                ratingService.createRating(1L, 2L, 4, "Great food!"));
        }

        /**
         * تست خطای نظر تکراری
         * 
         * بررسی منع ایجاد نظر دوم توسط همان کاربر
         */
        @Test
        @DisplayName("Should throw exception when user already rated restaurant")
        void shouldThrowExceptionWhenUserAlreadyRatedRestaurant() {
            // آماده‌سازی: نظر قبلی وجود دارد
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.of(testRating));

            // عمل و بررسی: باید IllegalArgumentException پرتاب شود
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.createRating(1L, 2L, 4, "Great food!"));
        }

        /**
         * تست منع نظردهی مالک به رستوران خود
         * 
         * بررسی منع نظردهی مالک رستوران به رستوران خودش
         */
        @Test
        @DisplayName("Should throw exception when owner rates own restaurant")
        void shouldThrowExceptionWhenOwnerRatesOwnRestaurant() {
            // آماده‌سازی: مالک رستوران همان کاربر باشد
            testRestaurant.setOwnerId(1L); // همان ID کاربر
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());

            // عمل و بررسی: باید IllegalArgumentException پرتاب شود
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.createRating(1L, 2L, 4, "Great food!"));
        }

        /**
         * تست اعتبارسنجی امتیاز نامعتبر
         * 
         * بررسی پرتاب exception برای امتیازهای خارج از بازه 1-5
         */
        @Test
        @DisplayName("Should throw exception for invalid rating values")
        void shouldThrowExceptionForInvalidRatingValues() {
            // بررسی امتیاز صفر
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.createRating(1L, 2L, 0, "Comment"));
            
            // بررسی امتیاز بیشتر از 5
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.createRating(1L, 2L, 6, "Comment"));
            
            // بررسی امتیاز منفی
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.createRating(1L, 2L, -1, "Comment"));
        }

        /**
         * تست اعتبارسنجی پارامترهای null
         * 
         * بررسی پرتاب exception برای پارامترهای اجباری null
         */
        @Test
        @DisplayName("Should throw exception for null parameters")
        void shouldThrowExceptionForNullParameters() {
            // بررسی userId null
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.createRating(null, 2L, 4, "Comment"));
            
            // بررسی restaurantId null
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.createRating(1L, null, 4, "Comment"));
            
            // بررسی score null
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.createRating(1L, 2L, null, "Comment"));
        }
    }

    /**
     * گروه تست‌های به‌روزرسانی نظر
     * 
     * تست تمام سناریوهای به‌روزرسانی نظر موجود
     */
    @Nested
    @DisplayName("Update Rating Tests")
    class UpdateRatingTests {

        /**
         * تست به‌روزرسانی موفق نظر
         * 
         * بررسی به‌روزرسانی امتیاز و متن نظر
         */
        @Test
        @DisplayName("Should update rating successfully")
        void shouldUpdateRatingSuccessfully() {
            // آماده‌سازی: نظر موجود باشد
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.of(testRating));
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            // عمل: به‌روزرسانی نظر
            Rating result = ratingService.updateRating(1L, 5, "Updated review");

            // بررسی: نظر به‌روزرسانی شود
            assertNotNull(result);
            verify(mockRatingRepository).save(any(Rating.class));
        }

        /**
         * تست به‌روزرسانی فقط امتیاز
         * 
         * بررسی به‌روزرسانی امتیاز بدون تغییر متن
         */
        @Test
        @DisplayName("Should update only score")
        void shouldUpdateOnlyScore() {
            // آماده‌سازی
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.of(testRating));
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            // عمل: به‌روزرسانی فقط امتیاز
            Rating result = ratingService.updateRating(1L, 5, null);

            // بررسی: نظر به‌روزرسانی شود
            assertNotNull(result);
            verify(mockRatingRepository).save(any(Rating.class));
        }

        /**
         * تست به‌روزرسانی فقط متن نظر
         * 
         * بررسی به‌روزرسانی متن بدون تغییر امتیاز
         */
        @Test
        @DisplayName("Should update only review text")
        void shouldUpdateOnlyReviewText() {
            // آماده‌سازی
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.of(testRating));
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            // عمل: به‌روزرسانی فقط متن
            Rating result = ratingService.updateRating(1L, null, "Updated review");

            // بررسی: نظر به‌روزرسانی شود
            assertNotNull(result);
            verify(mockRatingRepository).save(any(Rating.class));
        }

        /**
         * تست خطای عدم وجود نظر
         * 
         * بررسی پرتاب exception زمانی که نظر یافت نشود
         */
        @Test
        @DisplayName("Should throw exception when rating not found")
        void shouldThrowExceptionWhenRatingNotFound() {
            // آماده‌سازی: نظر یافت نشود
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.empty());

            // عمل و بررسی: باید NotFoundException پرتاب شود
            assertThrows(NotFoundException.class, () -> 
                ratingService.updateRating(1L, 5, "Updated review"));
        }

        /**
         * تست اعتبارسنجی امتیاز نامعتبر
         * 
         * بررسی پرتاب exception برای امتیازهای خارج از بازه
         */
        @Test
        @DisplayName("Should throw exception for invalid score")
        void shouldThrowExceptionForInvalidScore() {
            // بررسی امتیاز صفر
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.updateRating(1L, 0, "Review"));
            
            // بررسی امتیاز بیشتر از 5
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.updateRating(1L, 6, "Review"));
        }

        /**
         * تست اعتبارسنجی شناسه null
         * 
         * بررسی پرتاب exception برای شناسه نظر null
         */
        @Test
        @DisplayName("Should throw exception for null rating ID")
        void shouldThrowExceptionForNullRatingId() {
            // عمل و بررسی: باید IllegalArgumentException پرتاب شود
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.updateRating(null, 5, "Review"));
        }
    }

    @Nested
    @DisplayName("Get Rating Tests")
    class GetRatingTests {

        @Test
        @DisplayName("Should get rating by ID")
        void shouldGetRatingById() {
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.of(testRating));

            Rating result = ratingService.getRating(1L);

            assertNotNull(result);
            assertEquals(testRating.getId(), result.getId());
        }

        @Test
        @DisplayName("Should throw exception when rating not found")
        void shouldThrowExceptionWhenRatingNotFound() {
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> 
                ratingService.getRating(1L));
        }

        @Test
        @DisplayName("Should throw exception for null ID")
        void shouldThrowExceptionForNullId() {
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRating(null));
        }
    }

    @Nested
    @DisplayName("Get Restaurant Ratings Tests")
    class GetRestaurantRatingsTests {

        @Test
        @DisplayName("Should get restaurant ratings")
        void shouldGetRestaurantRatings() {
            List<Rating> ratings = Arrays.asList(testRating);
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByRestaurant(testRestaurant)).thenReturn(ratings);

            List<Rating> result = ratingService.getRestaurantRatings(2L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testRating.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Should throw exception when restaurant not found")
        void shouldThrowExceptionWhenRestaurantNotFound() {
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> 
                ratingService.getRestaurantRatings(2L));
        }

        @Test
        @DisplayName("Should throw exception for null restaurant ID")
        void shouldThrowExceptionForNullRestaurantId() {
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRestaurantRatings(null));
        }
    }

    @Nested
    @DisplayName("Get User Ratings Tests")
    class GetUserRatingsTests {

        @Test
        @DisplayName("Should get user ratings")
        void shouldGetUserRatings() {
            List<Rating> ratings = Arrays.asList(testRating);
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRatingRepository.findByUser(testUser)).thenReturn(ratings);

            List<Rating> result = ratingService.getUserRatings(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testRating.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> 
                ratingService.getUserRatings(1L));
        }

        @Test
        @DisplayName("Should throw exception for null user ID")
        void shouldThrowExceptionForNullUserId() {
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getUserRatings(null));
        }
    }

    @Nested
    @DisplayName("Get Rating Stats Tests")
    class GetRatingStatsTests {

        @Test
        @DisplayName("Should get restaurant rating stats")
        void shouldGetRestaurantRatingStats() {
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.getAverageRating(testRestaurant)).thenReturn(4.5);
            when(mockRatingRepository.getRatingCount(testRestaurant)).thenReturn(10L);
            when(mockRatingRepository.getRatingDistribution(testRestaurant)).thenReturn(Map.of(4, 5L, 5, 5L));

            RatingService.RatingStats stats = ratingService.getRestaurantRatingStats(2L);

            assertNotNull(stats);
            assertEquals(4.5, stats.getAverageRating());
            assertEquals(10L, stats.getTotalRatings());
            assertNotNull(stats.getDistribution());
        }

        @Test
        @DisplayName("Should throw exception when restaurant not found")
        void shouldThrowExceptionWhenRestaurantNotFound() {
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> 
                ratingService.getRestaurantRatingStats(2L));
        }

        @Test
        @DisplayName("Should throw exception for null restaurant ID")
        void shouldThrowExceptionForNullRestaurantId() {
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRestaurantRatingStats(null));
        }
    }

    @Nested
    @DisplayName("Check User Rating Tests")
    class CheckUserRatingTests {

        @Test
        @DisplayName("Should return true when user has rated restaurant")
        void shouldReturnTrueWhenUserHasRatedRestaurant() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.of(testRating));

            boolean result = ratingService.hasUserRatedRestaurant(1L, 2L);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when user has not rated restaurant")
        void shouldReturnFalseWhenUserHasNotRatedRestaurant() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());

            boolean result = ratingService.hasUserRatedRestaurant(1L, 2L);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for null parameters")
        void shouldReturnFalseForNullParameters() {
            assertFalse(ratingService.hasUserRatedRestaurant(null, 2L));
            assertFalse(ratingService.hasUserRatedRestaurant(1L, null));
            assertFalse(ratingService.hasUserRatedRestaurant(null, null));
        }

        @Test
        @DisplayName("Should return false when user not found")
        void shouldReturnFalseWhenUserNotFound() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.empty());

            boolean result = ratingService.hasUserRatedRestaurant(1L, 2L);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when restaurant not found")
        void shouldReturnFalseWhenRestaurantNotFound() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.empty());

            boolean result = ratingService.hasUserRatedRestaurant(1L, 2L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Get User Rating For Restaurant Tests")
    class GetUserRatingForRestaurantTests {

        @Test
        @DisplayName("Should get user rating for restaurant")
        void shouldGetUserRatingForRestaurant() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.of(testRating));

            Optional<Rating> result = ratingService.getUserRatingForRestaurant(1L, 2L);

            assertTrue(result.isPresent());
            assertEquals(testRating.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Should return empty when no rating found")
        void shouldReturnEmptyWhenNoRatingFound() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());

            Optional<Rating> result = ratingService.getUserRatingForRestaurant(1L, 2L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return empty for null parameters")
        void shouldReturnEmptyForNullParameters() {
            assertTrue(ratingService.getUserRatingForRestaurant(null, 2L).isEmpty());
            assertTrue(ratingService.getUserRatingForRestaurant(1L, null).isEmpty());
            assertTrue(ratingService.getUserRatingForRestaurant(null, null).isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete Rating Tests")
    class DeleteRatingTests {

        @Test
        @DisplayName("Should delete rating successfully")
        void shouldDeleteRatingSuccessfully() {
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.of(testRating));
            when(mockRatingRepository.delete(1L)).thenReturn(true);

            boolean result = ratingService.deleteRating(1L, 1L);

            assertTrue(result);
            verify(mockRatingRepository).delete(1L);
        }

        @Test
        @DisplayName("Should throw exception when rating not found")
        void shouldThrowExceptionWhenRatingNotFound() {
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> 
                ratingService.deleteRating(1L, 1L));
        }

        @Test
        @DisplayName("Should throw exception when user doesn't own rating")
        void shouldThrowExceptionWhenUserDoesntOwnRating() {
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.of(testRating));

            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.deleteRating(1L, 999L)); // Different user ID
        }

        @Test
        @DisplayName("Should throw exception for null parameters")
        void shouldThrowExceptionForNullParameters() {
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.deleteRating(null, 1L));
            
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.deleteRating(1L, null));
        }
    }

    @Nested
    @DisplayName("Helpful Mark Tests")
    class HelpfulMarkTests {

        @Test
        @DisplayName("Should mark rating as helpful")
        void shouldMarkRatingAsHelpful() {
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.of(testRating));
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            Rating result = ratingService.markAsHelpful(1L);

            assertNotNull(result);
            verify(mockRatingRepository).save(any(Rating.class));
        }

        @Test
        @DisplayName("Should remove helpful mark")
        void shouldRemoveHelpfulMark() {
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.of(testRating));
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            Rating result = ratingService.removeHelpfulMark(1L);

            assertNotNull(result);
            verify(mockRatingRepository).save(any(Rating.class));
        }

        @Test
        @DisplayName("Should throw exception for null rating ID")
        void shouldThrowExceptionForNullRatingId() {
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.markAsHelpful(null));
            
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.removeHelpfulMark(null));
        }
    }

    @Nested
    @DisplayName("Admin Operations Tests")
    class AdminOperationsTests {

        @Test
        @DisplayName("Should verify rating")
        void shouldVerifyRating() {
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.of(testRating));
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            Rating result = ratingService.verifyRating(1L);

            assertNotNull(result);
            verify(mockRatingRepository).save(any(Rating.class));
        }

        @Test
        @DisplayName("Should unverify rating")
        void shouldUnverifyRating() {
            when(mockRatingRepository.findById(1L)).thenReturn(Optional.of(testRating));
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            Rating result = ratingService.unverifyRating(1L);

            assertNotNull(result);
            verify(mockRatingRepository).save(any(Rating.class));
        }

        @Test
        @DisplayName("Should get all ratings")
        void shouldGetAllRatings() {
            List<Rating> ratings = Arrays.asList(testRating);
            when(mockRatingRepository.findAll()).thenReturn(ratings);

            List<Rating> result = ratingService.getAllRatings();

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should get total rating count")
        void shouldGetTotalRatingCount() {
            when(mockRatingRepository.countAll()).thenReturn(100L);

            Long result = ratingService.getTotalRatingCount();

            assertEquals(100L, result);
        }
    }

    @Nested
    @DisplayName("Query Operations Tests")
    class QueryOperationsTests {

        @Test
        @DisplayName("Should get ratings by score range")
        void shouldGetRatingsByScoreRange() {
            List<Rating> ratings = Arrays.asList(testRating);
            when(mockRatingRepository.findByScoreRange(4, 5)).thenReturn(ratings);

            List<Rating> result = ratingService.getRatingsByScore(4, 5);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should throw exception for invalid score range")
        void shouldThrowExceptionForInvalidScoreRange() {
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRatingsByScore(0, 5));
            
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRatingsByScore(1, 6));
            
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRatingsByScore(5, 1));
        }

        @Test
        @DisplayName("Should get verified ratings")
        void shouldGetVerifiedRatings() {
            List<Rating> ratings = Arrays.asList(testRating);
            when(mockRatingRepository.findVerifiedRatings()).thenReturn(ratings);

            List<Rating> result = ratingService.getVerifiedRatings();

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should get ratings with reviews")
        void shouldGetRatingsWithReviews() {
            List<Rating> ratings = Arrays.asList(testRating);
            when(mockRatingRepository.findRatingsWithReviews()).thenReturn(ratings);

            List<Rating> result = ratingService.getRatingsWithReviews();

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should get recent ratings")
        void shouldGetRecentRatings() {
            List<Rating> ratings = Arrays.asList(testRating);
            when(mockRatingRepository.findRecentRatings(7)).thenReturn(ratings);

            List<Rating> result = ratingService.getRecentRatings(7);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should throw exception for invalid days")
        void shouldThrowExceptionForInvalidDays() {
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRecentRatings(0));
            
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRecentRatings(-1));
        }

        @Test
        @DisplayName("Should get ratings with pagination")
        void shouldGetRatingsWithPagination() {
            List<Rating> ratings = Arrays.asList(testRating);
            when(mockRatingRepository.findWithPagination(0, 10)).thenReturn(ratings);

            List<Rating> result = ratingService.getRatingsWithPagination(0, 10);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should throw exception for invalid pagination parameters")
        void shouldThrowExceptionForInvalidPaginationParameters() {
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRatingsWithPagination(-1, 10));
            
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRatingsWithPagination(0, 0));
            
            assertThrows(IllegalArgumentException.class, () -> 
                ratingService.getRatingsWithPagination(0, -1));
        }
    }

    @Nested
    @DisplayName("RatingStats Inner Class Tests")
    class RatingStatsTests {

        @Test
        @DisplayName("Should create RatingStats with valid data")
        void shouldCreateRatingStatsWithValidData() {
            Map<Integer, Long> distribution = Map.of(4, 5L, 5, 5L);
            RatingService.RatingStats stats = new RatingService.RatingStats(4.5, 10L, distribution);

            assertEquals(4.5, stats.getAverageRating());
            assertEquals(10L, stats.getTotalRatings());
            assertEquals(distribution, stats.getDistribution());
            assertEquals("4.5", stats.getAverageRatingFormatted());
            assertTrue(stats.hasRatings());
        }

        @Test
        @DisplayName("Should handle null values in RatingStats")
        void shouldHandleNullValuesInRatingStats() {
            RatingService.RatingStats stats = new RatingService.RatingStats(null, null, null);

            assertEquals(0.0, stats.getAverageRating());
            assertEquals(0L, stats.getTotalRatings());
            assertNotNull(stats.getDistribution());
            assertTrue(stats.getDistribution().isEmpty());
            assertEquals("0.0", stats.getAverageRatingFormatted());
            assertFalse(stats.hasRatings());
        }

        @Test
        @DisplayName("Should format average rating correctly")
        void shouldFormatAverageRatingCorrectly() {
            RatingService.RatingStats stats = new RatingService.RatingStats(4.567, 10L, Map.of());

            assertEquals("4.6", stats.getAverageRatingFormatted());
        }

        @Test
        @DisplayName("Should have proper toString method")
        void shouldHaveProperToStringMethod() {
            Map<Integer, Long> distribution = Map.of(4, 5L, 5, 5L);
            RatingService.RatingStats stats = new RatingService.RatingStats(4.5, 10L, distribution);

            String result = stats.toString();

            assertNotNull(result);
            assertTrue(result.contains("RatingStats"));
            assertTrue(result.contains("averageRating=4.5"));
            assertTrue(result.contains("totalRatings=10"));
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle multiple rating operations efficiently")
        void shouldHandleMultipleRatingOperationsEfficiently() {
            when(mockAuthRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(any(), any())).thenReturn(Optional.empty());
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 100; i++) {
                ratingService.createRating(1L, 2L, 4, "Test review " + i);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Should complete within reasonable time (less than 1 second for 100 operations)
            assertTrue(duration < 1000, "Operations took too long: " + duration + "ms");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty rating lists")
        void shouldHandleEmptyRatingLists() {
            when(mockRatingRepository.findAll()).thenReturn(List.of());

            List<Rating> result = ratingService.getAllRatings();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            when(mockRatingRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

            assertThrows(RuntimeException.class, () -> 
                ratingService.getRating(1L));
        }

        @Test
        @DisplayName("Should handle boundary values for ratings")
        void shouldHandleBoundaryValuesForRatings() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            // Test minimum rating
            assertDoesNotThrow(() -> ratingService.createRating(1L, 2L, 1, "Min rating"));
            
            // Test maximum rating  
            assertDoesNotThrow(() -> ratingService.createRating(1L, 2L, 5, "Max rating"));
        }

        @Test
        @DisplayName("Should handle very long review text")
        void shouldHandleVeryLongReviewText() {
            String longReview = "A".repeat(999); // Just under 1000 char limit
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockRatingRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            when(mockRatingRepository.save(any(Rating.class))).thenReturn(testRating);

            assertDoesNotThrow(() -> ratingService.createRating(1L, 2L, 4, longReview));
        }
    }
}