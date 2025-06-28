package com.myapp.favorites;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Favorite;
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

/**
 * تست‌های جامع برای کلاس FavoritesService
 * 
 * این کلاس تست تمام عملیات منطق کسب‌وکار مربوط به مدیریت علاقه‌مندی‌ها را پوشش می‌دهد:
 * 
 * === دسته‌بندی تست‌ها ===
 * - ConstructorTests: تست سازنده‌ها
 * - AddFavoriteTests: تست اضافه کردن علاقه‌مندی
 * - RemoveFavoriteTests: تست حذف علاقه‌مندی
 * - UpdateFavoriteNotesTests: تست به‌روزرسانی یادداشت
 * - GetFavoriteTests: تست دریافت علاقه‌مندی
 * - GetUserFavoritesTests: تست دریافت علاقه‌مندی‌های کاربر
 * - CheckFavoriteTests: تست بررسی وجود علاقه‌مندی
 * - StatisticsTests: تست آمارگیری
 * - RecentFavoritesTests: تست علاقه‌مندی‌های اخیر
 * - FavoritesWithNotesTests: تست علاقه‌مندی‌های دارای یادداشت
 * - AdminOperationsTests: تست عملیات مدیریتی
 * - FavoriteStatsTests: تست کلاس آمارهای داخلی
 * 
 * === ویژگی‌های تست ===
 * - Mock Dependencies: استفاده از mock برای dependencies
 * - Comprehensive Coverage: پوشش کامل تمام سناریوها
 * - Edge Case Testing: تست حالات مرزی
 * - Error Handling: تست مدیریت خطاها
 * - Business Logic Validation: تست قوانین کسب‌وکار
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("FavoritesService Tests")
public class FavoritesServiceTest {

    /** Mock repository برای مدیریت علاقه‌مندی‌ها */
    @Mock
    private FavoritesRepository mockFavoritesRepository;
    
    /** Mock repository برای احراز هویت و کاربران */
    @Mock
    private AuthRepository mockAuthRepository;
    
    /** Mock repository برای مدیریت رستوران‌ها */
    @Mock
    private RestaurantRepository mockRestaurantRepository;
    
    /** سرویس تحت تست با mock dependencies */
    private FavoritesService favoritesService;
    
    /** کاربر نمونه برای تست‌ها */
    private User testUser;
    
    /** رستوران نمونه برای تست‌ها */
    private Restaurant testRestaurant;
    
    /** علاقه‌مندی نمونه برای تست‌ها */
    private Favorite testFavorite;

    /**
     * راه‌اندازی اولیه برای هر تست
     * 
     * ایجاد mock objects و داده‌های نمونه
     */
    @BeforeEach
    void setUp() {
        // راه‌اندازی mock objects
        MockitoAnnotations.openMocks(this);
        favoritesService = new FavoritesService(mockFavoritesRepository, mockAuthRepository, mockRestaurantRepository);
        
        // ایجاد کاربر نمونه
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setPhone("1234567890");
        testUser.setEmail("test@example.com");
        
        // ایجاد رستوران نمونه
        testRestaurant = new Restaurant();
        testRestaurant.setId(2L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setOwnerId(3L); // متفاوت از testUser.getId()
        testRestaurant.setStatus(RestaurantStatus.APPROVED);
        
        // ایجاد علاقه‌مندی نمونه
        testFavorite = new Favorite(testUser, testRestaurant);
        testFavorite.setId(1L);
        testFavorite.setCreatedAt(LocalDateTime.now());
    }

    /**
     * تست‌های مربوط به سازنده‌های کلاس
     */
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        /**
         * تست ایجاد سرویس با سازنده پیش‌فرض
         * 
         * بررسی می‌کند که سرویس با dependencies خودکار به درستی ایجاد شود
         */
        @Test
        @DisplayName("Should create service with default constructor")
        void shouldCreateServiceWithDefaultConstructor() {
            FavoritesService service = new FavoritesService();
            assertNotNull(service);
        }
        
        /**
         * تست ایجاد سرویس با dependency injection دستی
         * 
         * بررسی می‌کند که سرویس با mock dependencies به درستی ایجاد شود
         */
        @Test
        @DisplayName("Should create service with dependencies")
        void shouldCreateServiceWithDependencies() {
            FavoritesService service = new FavoritesService(mockFavoritesRepository, mockAuthRepository, mockRestaurantRepository);
            assertNotNull(service);
        }
    }

    /**
     * تست‌های مربوط به اضافه کردن علاقه‌مندی
     */
    @Nested
    @DisplayName("Add Favorite Tests")
    class AddFavoriteTests {
        
        /**
         * تست اضافه کردن علاقه‌مندی با موفقیت
         * 
         * سناریو: کاربر و رستوران موجود، علاقه‌مندی تکراری نیست
         */
        @Test
        @DisplayName("Should add favorite successfully")
        void shouldAddFavoriteSuccessfully() {
            // آماده‌سازی mock behavior
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            when(mockFavoritesRepository.save(any(Favorite.class))).thenReturn(testFavorite);
            
            // اجرای متد تحت تست
            Favorite result = favoritesService.addFavorite(1L, 2L, "Great food!");
            
            // بررسی نتایج
            assertNotNull(result);
            verify(mockFavoritesRepository).save(any(Favorite.class));
        }
        
        /**
         * تست اضافه کردن علاقه‌مندی بدون یادداشت
         * 
         * بررسی overload method که یادداشت دریافت نمی‌کند
         */
        @Test
        @DisplayName("Should add favorite without notes")
        void shouldAddFavoriteWithoutNotes() {
            // آماده‌سازی mock behavior
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            when(mockFavoritesRepository.save(any(Favorite.class))).thenReturn(testFavorite);
            
            // اجرای متد تحت تست
            Favorite result = favoritesService.addFavorite(1L, 2L);
            
            // بررسی نتایج
            assertNotNull(result);
            verify(mockFavoritesRepository).save(any(Favorite.class));
        }
        
        /**
         * تست خطای کاربر یافت نشد
         * 
         * سناریو: userId نامعتبر ارسال شده
         */
        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // آماده‌سازی mock behavior - کاربر یافت نشود
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.empty());
            
            // بررسی پرتاب exception مناسب
            assertThrows(NotFoundException.class, () -> 
                favoritesService.addFavorite(1L, 2L, "Notes"));
        }
        
        /**
         * تست خطای رستوران یافت نشد
         * 
         * سناریو: restaurantId نامعتبر ارسال شده
         */
        @Test
        @DisplayName("Should throw exception when restaurant not found")
        void shouldThrowExceptionWhenRestaurantNotFound() {
            // آماده‌سازی mock behavior - کاربر موجود اما رستوران نه
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.empty());
            
            // بررسی پرتاب exception مناسب
            assertThrows(NotFoundException.class, () -> 
                favoritesService.addFavorite(1L, 2L, "Notes"));
        }
        
        /**
         * تست خطای علاقه‌مندی تکراری
         * 
         * سناریو: کاربر قبلاً این رستوران را علاقه‌مندی کرده
         */
        @Test
        @DisplayName("Should throw exception when already favorited")
        void shouldThrowExceptionWhenAlreadyFavorited() {
            // آماده‌سازی mock behavior - علاقه‌مندی موجود است
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.of(testFavorite));
            
            // بررسی پرتاب exception مناسب
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.addFavorite(1L, 2L, "Notes"));
        }
        
        /**
         * تست قانون کسب‌وکار: منع علاقه‌مندی مالک به رستوران خودش
         * 
         * سناریو: کاربر مالک رستوران است
         */
        @Test
        @DisplayName("Should throw exception when user tries to favorite own restaurant")
        void shouldThrowExceptionWhenUserTriesToFavoriteOwnRestaurant() {
            // تنظیم رستوران به عنوان متعلق به کاربر
            testRestaurant.setOwnerId(1L); // همان testUser.getId()
            
            // آماده‌سازی mock behavior
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            
            // بررسی پرتاب exception مناسب
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.addFavorite(1L, 2L, "Notes"));
        }
        
        /**
         * تست validation ورودی null userId
         */
        @Test
        @DisplayName("Should throw exception for null userId")
        void shouldThrowExceptionForNullUserId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.addFavorite(null, 2L, "Notes"));
        }
        
        /**
         * تست validation ورودی null restaurantId
         */
        @Test
        @DisplayName("Should throw exception for null restaurantId")
        void shouldThrowExceptionForNullRestaurantId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.addFavorite(1L, null, "Notes"));
        }
    }

    /**
     * تست‌های مربوط به حذف علاقه‌مندی
     */
    @Nested
    @DisplayName("Remove Favorite Tests")
    class RemoveFavoriteTests {
        
        /**
         * تست حذف علاقه‌مندی با موفقیت
         * 
         * سناریو: کاربر، رستوران و علاقه‌مندی موجود است
         */
        @Test
        @DisplayName("Should remove favorite successfully")
        void shouldRemoveFavoriteSuccessfully() {
            // آماده‌سازی mock behavior
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.of(testFavorite));
            when(mockFavoritesRepository.delete(1L)).thenReturn(true);
            
            // اجرای متد تحت تست
            boolean result = favoritesService.removeFavorite(1L, 2L);
            
            // بررسی نتایج
            assertTrue(result);
            verify(mockFavoritesRepository).delete(1L);
        }
        
        /**
         * تست خطای علاقه‌مندی یافت نشد
         * 
         * سناریو: کاربر این رستوران را علاقه‌مندی نکرده
         */
        @Test
        @DisplayName("Should throw exception when favorite not found")
        void shouldThrowExceptionWhenFavoriteNotFound() {
            // آماده‌سازی mock behavior - علاقه‌مندی موجود نیست
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            
            // بررسی پرتاب exception مناسب
            assertThrows(NotFoundException.class, () -> 
                favoritesService.removeFavorite(1L, 2L));
        }
        
        /**
         * تست validation ورودی null userId در حذف
         */
        @Test
        @DisplayName("Should throw exception for null userId")
        void shouldThrowExceptionForNullUserId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.removeFavorite(null, 2L));
        }
        
        /**
         * تست validation ورودی null restaurantId در حذف
         */
        @Test
        @DisplayName("Should throw exception for null restaurantId")
        void shouldThrowExceptionForNullRestaurantId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.removeFavorite(1L, null));
        }
    }

    /**
     * تست‌های مربوط به به‌روزرسانی یادداشت علاقه‌مندی
     */
    @Nested
    @DisplayName("Update Favorite Notes Tests")
    class UpdateFavoriteNotesTests {
        
        /**
         * تست به‌روزرسانی یادداشت با موفقیت
         * 
         * سناریو: علاقه‌مندی موجود و یادداشت جدید معتبر
         */
        @Test
        @DisplayName("Should update favorite notes successfully")
        void shouldUpdateFavoriteNotesSuccessfully() {
            // آماده‌سازی mock behavior
            when(mockFavoritesRepository.findById(1L)).thenReturn(Optional.of(testFavorite));
            when(mockFavoritesRepository.save(testFavorite)).thenReturn(testFavorite);
            
            // اجرای متد تحت تست
            Favorite result = favoritesService.updateFavoriteNotes(1L, "Updated notes");
            
            // بررسی نتایج
            assertNotNull(result);
            verify(mockFavoritesRepository).save(testFavorite);
        }
        
        /**
         * تست خطای علاقه‌مندی یافت نشد در به‌روزرسانی
         */
        @Test
        @DisplayName("Should throw exception when favorite not found")
        void shouldThrowExceptionWhenFavoriteNotFound() {
            // آماده‌سازی mock behavior - علاقه‌مندی موجود نیست
            when(mockFavoritesRepository.findById(1L)).thenReturn(Optional.empty());
            
            // بررسی پرتاب exception مناسب
            assertThrows(NotFoundException.class, () -> 
                favoritesService.updateFavoriteNotes(1L, "Notes"));
        }
        
        /**
         * تست validation ورودی null favoriteId
         */
        @Test
        @DisplayName("Should throw exception for null favoriteId")
        void shouldThrowExceptionForNullFavoriteId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.updateFavoriteNotes(null, "Notes"));
        }
    }

    /**
     * تست‌های مربوط به دریافت علاقه‌مندی خاص
     */
    @Nested
    @DisplayName("Get Favorite Tests")
    class GetFavoriteTests {
        
        /**
         * تست دریافت علاقه‌مندی بر اساس ID با موفقیت
         */
        @Test
        @DisplayName("Should get favorite by ID successfully")
        void shouldGetFavoriteByIdSuccessfully() {
            // آماده‌سازی mock behavior
            when(mockFavoritesRepository.findById(1L)).thenReturn(Optional.of(testFavorite));
            
            // اجرای متد تحت تست
            Favorite result = favoritesService.getFavorite(1L);
            
            // بررسی نتایج
            assertNotNull(result);
            assertEquals(testFavorite, result);
        }
        
        /**
         * تست خطای علاقه‌مندی یافت نشد
         */
        @Test
        @DisplayName("Should throw exception when favorite not found")
        void shouldThrowExceptionWhenFavoriteNotFound() {
            // آماده‌سازی mock behavior - علاقه‌مندی موجود نیست
            when(mockFavoritesRepository.findById(1L)).thenReturn(Optional.empty());
            
            // بررسی پرتاب exception مناسب
            assertThrows(NotFoundException.class, () -> 
                favoritesService.getFavorite(1L));
        }
        
        /**
         * تست validation ورودی null favoriteId
         */
        @Test
        @DisplayName("Should throw exception for null favoriteId")
        void shouldThrowExceptionForNullFavoriteId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getFavorite(null));
        }
    }

    /**
     * تست‌های مربوط به دریافت علاقه‌مندی‌های کاربر
     */
    @Nested
    @DisplayName("Get User Favorites Tests")
    class GetUserFavoritesTests {
        
        /**
         * تست دریافت علاقه‌مندی‌های کاربر با موفقیت
         */
        @Test
        @DisplayName("Should get user favorites successfully")
        void shouldGetUserFavoritesSuccessfully() {
            // آماده‌سازی داده‌های نمونه
            List<Favorite> favorites = Arrays.asList(testFavorite);
            
            // آماده‌سازی mock behavior
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockFavoritesRepository.findByUser(testUser)).thenReturn(favorites);
            
            // اجرای متد تحت تست
            List<Favorite> result = favoritesService.getUserFavorites(1L);
            
            // بررسی نتایج
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testFavorite, result.get(0));
        }
        
        /**
         * تست خطای کاربر یافت نشد
         */
        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // آماده‌سازی mock behavior - کاربر موجود نیست
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.empty());
            
            // بررسی پرتاب exception مناسب
            assertThrows(NotFoundException.class, () -> 
                favoritesService.getUserFavorites(1L));
        }
        
        /**
         * تست validation ورودی null userId
         */
        @Test
        @DisplayName("Should throw exception for null userId")
        void shouldThrowExceptionForNullUserId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getUserFavorites(null));
        }
    }

    /**
     * تست‌های مربوط به بررسی وجود علاقه‌مندی
     */
    @Nested
    @DisplayName("Check Favorite Tests")
    class CheckFavoriteTests {
        
        /**
         * تست بازگشت true زمانی که علاقه‌مندی موجود است
         */
        @Test
        @DisplayName("Should return true when favorite exists")
        void shouldReturnTrueWhenFavoriteExists() {
            // آماده‌سازی mock behavior - علاقه‌مندی موجود است
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.of(testFavorite));
            
            // اجرای متد تحت تست
            boolean result = favoritesService.isFavorite(1L, 2L);
            
            // بررسی نتایج
            assertTrue(result);
        }
        
        /**
         * تست بازگشت false زمانی که علاقه‌مندی موجود نیست
         */
        @Test
        @DisplayName("Should return false when favorite does not exist")
        void shouldReturnFalseWhenFavoriteDoesNotExist() {
            // آماده‌سازی mock behavior - علاقه‌مندی موجود نیست
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            
            // اجرای متد تحت تست
            boolean result = favoritesService.isFavorite(1L, 2L);
            
            // بررسی نتایج
            assertFalse(result);
        }
        
        /**
         * تست بازگشت false برای پارامترهای null
         * 
         * این متد برخلاف سایرین exception نمی‌اندازد
         */
        @Test
        @DisplayName("Should return false for null parameters")
        void shouldReturnFalseForNullParameters() {
            assertFalse(favoritesService.isFavorite(null, 2L));
            assertFalse(favoritesService.isFavorite(1L, null));
            assertFalse(favoritesService.isFavorite(null, null));
        }
        
        /**
         * تست بازگشت false زمانی که کاربر یافت نشود
         */
        @Test
        @DisplayName("Should return false when user not found")
        void shouldReturnFalseWhenUserNotFound() {
            // آماده‌سازی mock behavior - کاربر موجود نیست
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.empty());
            
            // اجرای متد تحت تست
            boolean result = favoritesService.isFavorite(1L, 2L);
            
            // بررسی نتایج
            assertFalse(result);
        }
        
        /**
         * تست بازگشت false زمانی که رستوران یافت نشود
         */
        @Test
        @DisplayName("Should return false when restaurant not found")
        void shouldReturnFalseWhenRestaurantNotFound() {
            // آماده‌سازی mock behavior - کاربر موجود اما رستوران نه
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.empty());
            
            // اجرای متد تحت تست
            boolean result = favoritesService.isFavorite(1L, 2L);
            
            // بررسی نتایج
            assertFalse(result);
        }
    }

    /**
     * تست‌های مربوط به آمارگیری
     */
    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {
        
        /**
         * تست دریافت آمارهای کامل کاربر
         * 
         * شامل تعداد کل، دارای یادداشت و اخیر
         */
        @Test
        @DisplayName("Should get user favorite stats successfully")
        void shouldGetUserFavoriteStatsSuccessfully() {
            // ایجاد علاقه‌مندی اخیر با یادداشت
            Favorite recentFavorite = new Favorite(testUser, testRestaurant, "Recent notes");
            recentFavorite.setCreatedAt(LocalDateTime.now().minusDays(5));
            
            // ایجاد علاقه‌مندی قدیمی بدون یادداشت
            Favorite oldFavorite = new Favorite(testUser, testRestaurant);
            oldFavorite.setCreatedAt(LocalDateTime.now().minusDays(45));
            
            List<Favorite> favorites = Arrays.asList(recentFavorite, oldFavorite);
            
            // آماده‌سازی mock behavior
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockFavoritesRepository.findByUser(testUser)).thenReturn(favorites);
            
            // اجرای متد تحت تست
            FavoritesService.FavoriteStats stats = favoritesService.getUserFavoriteStats(1L);
            
            // بررسی نتایج
            assertNotNull(stats);
            assertEquals(2L, stats.getTotalFavorites());
            assertEquals(1L, stats.getFavoritesWithNotes());
            assertEquals(1L, stats.getRecentFavorites());
        }
        
        /**
         * تست دریافت تعداد علاقه‌مندان رستوران
         */
        @Test
        @DisplayName("Should get restaurant favorite count successfully")
        void shouldGetRestaurantFavoriteCountSuccessfully() {
            // آماده‌سازی mock behavior
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.countByRestaurant(testRestaurant)).thenReturn(5L);
            
            // اجرای متد تحت تست
            Long count = favoritesService.getRestaurantFavoriteCount(2L);
            
            // بررسی نتایج
            assertEquals(5L, count);
        }
        
        /**
         * تست دریافت تعداد علاقه‌مندی‌های کاربر
         */
        @Test
        @DisplayName("Should get user favorite count successfully")
        void shouldGetUserFavoriteCountSuccessfully() {
            // آماده‌سازی mock behavior
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockFavoritesRepository.countByUser(testUser)).thenReturn(3L);
            
            // اجرای متد تحت تست
            Long count = favoritesService.getUserFavoriteCount(1L);
            
            // بررسی نتایج
            assertEquals(3L, count);
        }
    }

    /**
     * تست‌های مربوط به علاقه‌مندی‌های اخیر
     */
    @Nested
    @DisplayName("Recent Favorites Tests")
    class RecentFavoritesTests {
        
        /**
         * تست دریافت علاقه‌مندی‌های اخیر با موفقیت
         */
        @Test
        @DisplayName("Should get recent favorites successfully")
        void shouldGetRecentFavoritesSuccessfully() {
            // آماده‌سازی داده‌های نمونه
            List<Favorite> recentFavorites = Arrays.asList(testFavorite);
            
            // آماده‌سازی mock behavior
            when(mockFavoritesRepository.findRecentFavorites(30)).thenReturn(recentFavorites);
            
            // اجرای متد تحت تست
            List<Favorite> result = favoritesService.getRecentFavorites(30);
            
            // بررسی نتایج
            assertNotNull(result);
            assertEquals(1, result.size());
        }
        
        /**
         * تست validation پارامتر نامعتبر days
         */
        @Test
        @DisplayName("Should throw exception for invalid days")
        void shouldThrowExceptionForInvalidDays() {
            // بررسی پرتاب exception برای days صفر و منفی
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getRecentFavorites(0));
            
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getRecentFavorites(-1));
        }
    }

    /**
     * تست‌های مربوط به علاقه‌مندی‌های دارای یادداشت
     */
    @Nested
    @DisplayName("Favorites With Notes Tests")
    class FavoritesWithNotesTests {
        
        /**
         * تست دریافت علاقه‌مندی‌های دارای یادداشت
         */
        @Test
        @DisplayName("Should get favorites with notes successfully")
        void shouldGetFavoritesWithNotesSuccessfully() {
            // آماده‌سازی داده‌های نمونه
            List<Favorite> favoritesWithNotes = Arrays.asList(testFavorite);
            
            // آماده‌سازی mock behavior
            when(mockFavoritesRepository.findFavoritesWithNotes()).thenReturn(favoritesWithNotes);
            
            // اجرای متد تحت تست
            List<Favorite> result = favoritesService.getFavoritesWithNotes();
            
            // بررسی نتایج
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    /**
     * تست‌های مربوط به دریافت علاقه‌مندی خاص کاربر برای رستوران
     */
    @Nested
    @DisplayName("Get User Favorite For Restaurant Tests")
    class GetUserFavoriteForRestaurantTests {
        
        /**
         * تست دریافت علاقه‌مندی خاص کاربر برای رستوران با موفقیت
         */
        @Test
        @DisplayName("Should get user favorite for restaurant successfully")
        void shouldGetUserFavoriteForRestaurantSuccessfully() {
            // آماده‌سازی mock behavior
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.of(testFavorite));
            
            // اجرای متد تحت تست
            Optional<Favorite> result = favoritesService.getUserFavoriteForRestaurant(1L, 2L);
            
            // بررسی نتایج
            assertTrue(result.isPresent());
            assertEquals(testFavorite, result.get());
        }
        
        /**
         * تست بازگشت empty زمانی که علاقه‌مندی موجود نیست
         */
        @Test
        @DisplayName("Should return empty when favorite does not exist")
        void shouldReturnEmptyWhenFavoriteDoesNotExist() {
            // آماده‌سازی mock behavior
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            
            // اجرای متد تحت تست
            Optional<Favorite> result = favoritesService.getUserFavoriteForRestaurant(1L, 2L);
            
            // بررسی نتایج
            assertTrue(result.isEmpty());
        }
        
        /**
         * تست بازگشت empty برای پارامترهای null
         */
        @Test
        @DisplayName("Should return empty for null parameters")
        void shouldReturnEmptyForNullParameters() {
            assertTrue(favoritesService.getUserFavoriteForRestaurant(null, 2L).isEmpty());
            assertTrue(favoritesService.getUserFavoriteForRestaurant(1L, null).isEmpty());
            assertTrue(favoritesService.getUserFavoriteForRestaurant(null, null).isEmpty());
        }
        
        /**
         * تست بازگشت empty زمانی که کاربر یافت نشود
         */
        @Test
        @DisplayName("Should return empty when user not found")
        void shouldReturnEmptyWhenUserNotFound() {
            // آماده‌سازی mock behavior - کاربر موجود نیست
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.empty());
            
            // اجرای متد تحت تست
            Optional<Favorite> result = favoritesService.getUserFavoriteForRestaurant(1L, 2L);
            
            // بررسی نتایج
            assertTrue(result.isEmpty());
        }
        
        /**
         * تست مدیریت exceptions
         */
        @Test
        @DisplayName("Should handle exceptions gracefully")
        void shouldHandleExceptionsGracefully() {
            // آماده‌سازی mock behavior - exception در repository
            when(mockAuthRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));
            
            // اجرای متد تحت تست
            Optional<Favorite> result = favoritesService.getUserFavoriteForRestaurant(1L, 2L);
            
            // بررسی نتایج - باید empty برگرداند بدون exception
            assertTrue(result.isEmpty());
        }
    }

    /**
     * تست‌های مربوط به عملیات مدیریتی (Admin)
     */
    @Nested
    @DisplayName("Admin Operations Tests")
    class AdminOperationsTests {
        
        /**
         * تست دریافت تمام علاقه‌مندی‌ها (برای ادمین)
         */
        @Test
        @DisplayName("Should get all favorites successfully")
        void shouldGetAllFavoritesSuccessfully() {
            // آماده‌سازی داده‌های نمونه
            List<Favorite> allFavorites = Arrays.asList(testFavorite);
            
            // آماده‌سازی mock behavior
            when(mockFavoritesRepository.findAll()).thenReturn(allFavorites);
            
            // اجرای متد تحت تست
            List<Favorite> result = favoritesService.getAllFavorites();
            
            // بررسی نتایج
            assertNotNull(result);
            assertEquals(1, result.size());
        }
        
        /**
         * تست دریافت علاقه‌مندی‌ها با صفحه‌بندی
         */
        @Test
        @DisplayName("Should get favorites with pagination successfully")
        void shouldGetFavoritesWithPaginationSuccessfully() {
            // آماده‌سازی داده‌های نمونه
            List<Favorite> pagedFavorites = Arrays.asList(testFavorite);
            
            // آماده‌سازی mock behavior
            when(mockFavoritesRepository.findWithPagination(0, 10)).thenReturn(pagedFavorites);
            
            // اجرای متد تحت تست
            List<Favorite> result = favoritesService.getFavoritesWithPagination(0, 10);
            
            // بررسی نتایج
            assertNotNull(result);
            assertEquals(1, result.size());
        }
        
        /**
         * تست validation پارامترهای نامعتبر pagination
         */
        @Test
        @DisplayName("Should throw exception for invalid pagination parameters")
        void shouldThrowExceptionForInvalidPaginationParameters() {
            // بررسی پرتاب exception برای page منفی
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getFavoritesWithPagination(-1, 10));
            
            // بررسی پرتاب exception برای size صفر یا منفی
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getFavoritesWithPagination(0, 0));
            
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getFavoritesWithPagination(0, -1));
        }
        
        /**
         * تست حذف علاقه‌مندی توسط ادمین
         */
        @Test
        @DisplayName("Should delete favorite successfully")
        void shouldDeleteFavoriteSuccessfully() {
            // آماده‌سازی mock behavior
            when(mockFavoritesRepository.delete(1L)).thenReturn(true);
            
            // اجرای متد تحت تست
            boolean result = favoritesService.deleteFavorite(1L);
            
            // بررسی نتایج
            assertTrue(result);
            verify(mockFavoritesRepository).delete(1L);
        }
        
        /**
         * تست validation ورودی null favoriteId در حذف ادمین
         */
        @Test
        @DisplayName("Should throw exception for null favoriteId in delete")
        void shouldThrowExceptionForNullFavoriteIdInDelete() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.deleteFavorite(null));
        }
        
        /**
         * تست دریافت کل تعداد علاقه‌مندی‌ها
         */
        @Test
        @DisplayName("Should get total favorite count successfully")
        void shouldGetTotalFavoriteCountSuccessfully() {
            // آماده‌سازی mock behavior
            when(mockFavoritesRepository.countAll()).thenReturn(100L);
            
            // اجرای متد تحت تست
            Long count = favoritesService.getTotalFavoriteCount();
            
            // بررسی نتایج
            assertEquals(100L, count);
        }
    }

    /**
     * تست‌های مربوط به کلاس داخلی FavoriteStats
     */
    @Nested
    @DisplayName("FavoriteStats Inner Class Tests")
    class FavoriteStatsTests {
        
        /**
         * تست ایجاد FavoriteStats با تمام مقادیر
         */
        @Test
        @DisplayName("Should create FavoriteStats with all values")
        void shouldCreateFavoriteStatsWithAllValues() {
            // ایجاد stats با مقادیر نمونه
            FavoritesService.FavoriteStats stats = new FavoritesService.FavoriteStats(10L, 5L, 3L);
            
            // بررسی مقادیر
            assertEquals(10L, stats.getTotalFavorites());
            assertEquals(5L, stats.getFavoritesWithNotes());
            assertEquals(3L, stats.getRecentFavorites());
            assertTrue(stats.hasFavorites());
            assertEquals(50.0, stats.getNotesPercentage(), 0.01);
            assertEquals(30.0, stats.getRecentPercentage(), 0.01);
        }
        
        /**
         * تست handling مقادیر null در FavoriteStats
         */
        @Test
        @DisplayName("Should handle null values in FavoriteStats")
        void shouldHandleNullValuesInFavoriteStats() {
            // ایجاد stats با مقادیر null
            FavoritesService.FavoriteStats stats = new FavoritesService.FavoriteStats(null, null, null);
            
            // بررسی handling صحیح null values
            assertEquals(0L, stats.getTotalFavorites());
            assertEquals(0L, stats.getFavoritesWithNotes());
            assertEquals(0L, stats.getRecentFavorites());
            assertFalse(stats.hasFavorites());
        }
        
        /**
         * تست محاسبه درصدها برای total صفر
         */
        @Test
        @DisplayName("Should calculate percentages correctly for zero total")
        void shouldCalculatePercentagesCorrectlyForZeroTotal() {
            // ایجاد stats با total صفر
            FavoritesService.FavoriteStats stats = new FavoritesService.FavoriteStats(0L, 0L, 0L);
            
            // بررسی عدم تقسیم بر صفر
            assertEquals(0.0, stats.getNotesPercentage(), 0.01);
            assertEquals(0.0, stats.getRecentPercentage(), 0.01);
        }
        
        /**
         * تست متد toString
         */
        @Test
        @DisplayName("Should have proper toString method")
        void shouldHaveProperToStringMethod() {
            FavoritesService.FavoriteStats stats = new FavoritesService.FavoriteStats(10L, 5L, 3L);
            String toString = stats.toString();
            
            // بررسی شامل بودن مقادیر کلیدی در toString
            assertNotNull(toString);
            assertTrue(toString.contains("10"));
            assertTrue(toString.contains("5"));
            assertTrue(toString.contains("3"));
        }
    }
} 