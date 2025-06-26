package com.myapp.favorites;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Favorite;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.User;
import com.myapp.restaurant.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * سرویس منطق کسب‌وکار برای مدیریت علاقه‌مندی‌ها
 * 
 * این کلاس تمام منطق کسب‌وکار مربوط به مدیریت علاقه‌مندی‌های کاربران را ارائه می‌دهد:
 * 
 * === عملیات اصلی ===
 * - addFavorite(): اضافه کردن رستوران به علاقه‌مندی‌ها
 * - removeFavorite(): حذف از علاقه‌مندی‌ها
 * - updateFavoriteNotes(): به‌روزرسانی یادداشت‌ها
 * - isFavorite(): بررسی وجود در علاقه‌مندی‌ها
 * 
 * === دریافت اطلاعات ===
 * - getUserFavorites(): علاقه‌مندی‌های کاربر
 * - getRestaurantFavorites(): کاربران علاقه‌مند به رستوران
 * - getFavorite(): دریافت علاقه‌مندی خاص
 * - getUserFavoriteForRestaurant(): علاقه‌مندی خاص کاربر به رستوران
 * 
 * === فیلترهای پیشرفته ===
 * - getRecentFavorites(): علاقه‌مندی‌های اخیر
 * - getFavoritesWithNotes(): دارای یادداشت
 * - getFavoritesWithPagination(): با صفحه‌بندی
 * 
 * === آمار و شمارش ===
 * - getRestaurantFavoriteCount(): تعداد علاقه‌مندان رستوران
 * - getUserFavoriteCount(): تعداد علاقه‌مندی‌های کاربر
 * - getTotalFavoriteCount(): کل تعداد علاقه‌مندی‌ها
 * - getUserFavoriteStats(): آمارهای کامل کاربر
 * 
 * === مدیریت (Admin) ===
 * - getAllFavorites(): تمام علاقه‌مندی‌ها
 * - deleteFavorite(): حذف توسط ادمین
 * 
 * === قوانین کسب‌وکار ===
 * 1. کاربران نمی‌توانند رستوران خودشان را علاقه‌مندی کنند
 * 2. یک رستوران فقط یک بار قابل علاقه‌مندی است
 * 3. تمام ورودی‌ها باید معتبر باشند
 * 4. یادداشت‌ها اختیاری هستند
 * 5. validation کامل برای تمام عملیات
 * 
 * === ویژگی‌های کلیدی ===
 * - Business Logic Validation: اعتبارسنجی کامل
 * - Cross-Repository Integration: تعامل با Auth و Restaurant
 * - Exception Handling: مدیریت خطاهای مناسب
 * - Statistics Generation: تولید آمارهای پیشرفته
 * - Logging: ثبت عملیات مهم
 * - Null Safety: بررسی null در تمام ورودی‌ها
 * - Performance: بهینه‌سازی queries
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class FavoritesService {
    
    /** Logger برای ثبت عملیات و خطاها */
    private static final Logger logger = LoggerFactory.getLogger(FavoritesService.class);
    
    /** Repository برای مدیریت علاقه‌مندی‌ها */
    private final FavoritesRepository favoritesRepository;
    
    /** Repository برای احراز هویت و مدیریت کاربران */
    private final AuthRepository authRepository;
    
    /** Repository برای مدیریت رستوران‌ها */
    private final RestaurantRepository restaurantRepository;
    
    /**
     * سازنده پیش‌فرض - Dependency Injection خودکار
     * 
     * برای استفاده عادی در production
     */
    public FavoritesService() {
        this.favoritesRepository = new FavoritesRepository();
        this.authRepository = new AuthRepository();
        this.restaurantRepository = new RestaurantRepository();
    }
    
    /**
     * سازنده برای تست - Manual Dependency Injection
     * 
     * امکان inject کردن mock repositories برای تست‌های واحد
     * 
     * @param favoritesRepository repository علاقه‌مندی‌ها
     * @param authRepository repository احراز هویت
     * @param restaurantRepository repository رستوران‌ها
     */
    public FavoritesService(FavoritesRepository favoritesRepository, 
                           AuthRepository authRepository, 
                           RestaurantRepository restaurantRepository) {
        this.favoritesRepository = favoritesRepository;
        this.authRepository = authRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * اضافه کردن رستوران به علاقه‌مندی‌های کاربر
     * 
     * این متد شامل validation های کاملی است:
     * - بررسی وجود کاربر و رستوران
     * - جلوگیری از علاقه‌مندی تکراری
     * - منع علاقه‌مندی مالک به رستوران خودش
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @param notes یادداشت اختیاری کاربر
     * @return علاقه‌مندی ایجاد شده
     * @throws NotFoundException اگر کاربر یا رستوران یافت نشود
     * @throws IllegalArgumentException در صورت نقض قوانین کسب‌وکار
     */
    public Favorite addFavorite(Long userId, Long restaurantId, String notes) {
        logger.info("Adding favorite: userId={}, restaurantId={}", userId, restaurantId);
        
        // اعتبارسنجی ورودی‌ها
        validateFavoriteInputs(userId, restaurantId);
        
        // دریافت کاربر و رستوران
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        // بررسی عدم وجود علاقه‌مندی تکراری
        Optional<Favorite> existingFavorite = favoritesRepository.findByUserAndRestaurant(user, restaurant);
        if (existingFavorite.isPresent()) {
            throw new IllegalArgumentException("Restaurant is already in user's favorites");
        }
        
        // قانون کسب‌وکار: مالک نمی‌تواند رستوران خودش را علاقه‌مندی کند
        if (restaurant.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Restaurant owners cannot favorite their own restaurants");
        }
        
        // ایجاد و ذخیره علاقه‌مندی
        Favorite favorite = new Favorite(user, restaurant, notes);
        Favorite savedFavorite = favoritesRepository.save(favorite);
        
        logger.info("Added favorite with ID: {}", savedFavorite.getId());
        return savedFavorite;
    }
    
    /**
     * متد Legacy برای سازگاری با نسخه قبل
     * 
     * اضافه کردن علاقه‌مندی بدون یادداشت
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @return علاقه‌مندی ایجاد شده
     */
    public Favorite addFavorite(Long userId, Long restaurantId) {
        return addFavorite(userId, restaurantId, null);
    }
    
    /**
     * حذف رستوران از علاقه‌مندی‌های کاربر
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @return true در صورت حذف موفق، false در غیر این صورت
     * @throws NotFoundException اگر کاربر، رستوران یا علاقه‌مندی یافت نشود
     */
    public boolean removeFavorite(Long userId, Long restaurantId) {
        logger.info("Removing favorite: userId={}, restaurantId={}", userId, restaurantId);
        
        // اعتبارسنجی ورودی‌ها
        validateFavoriteInputs(userId, restaurantId);
        
        // دریافت کاربر و رستوران
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        // یافتن علاقه‌مندی موجود
        Optional<Favorite> favorite = favoritesRepository.findByUserAndRestaurant(user, restaurant);
        if (favorite.isEmpty()) {
            throw new NotFoundException("Favorite", "user=" + userId + ", restaurant=" + restaurantId);
        }
        
        // حذف علاقه‌مندی
        boolean deleted = favoritesRepository.delete(favorite.get().getId());
        if (deleted) {
            logger.info("Removed favorite: userId={}, restaurantId={}", userId, restaurantId);
        }
        return deleted;
    }
    
    /**
     * به‌روزرسانی یادداشت علاقه‌مندی
     * 
     * @param favoriteId شناسه علاقه‌مندی
     * @param notes یادداشت جدید
     * @return علاقه‌مندی به‌روزرسانی شده
     * @throws NotFoundException اگر علاقه‌مندی یافت نشود
     */
    public Favorite updateFavoriteNotes(Long favoriteId, String notes) {
        logger.info("Updating favorite notes: favoriteId={}", favoriteId);
        
        if (favoriteId == null) {
            throw new IllegalArgumentException("Favorite ID cannot be null");
        }
        
        Favorite favorite = favoritesRepository.findById(favoriteId)
            .orElseThrow(() -> new NotFoundException("Favorite", favoriteId));
        
        favorite.updateNotes(notes);
        Favorite updatedFavorite = favoritesRepository.save(favorite);
        
        logger.info("Updated favorite notes for ID: {}", favoriteId);
        return updatedFavorite;
    }
    
    /**
     * دریافت علاقه‌مندی بر اساس شناسه
     * 
     * @param favoriteId شناسه علاقه‌مندی
     * @return علاقه‌مندی یافت شده
     * @throws NotFoundException اگر علاقه‌مندی یافت نشود
     */
    public Favorite getFavorite(Long favoriteId) {
        if (favoriteId == null) {
            throw new IllegalArgumentException("Favorite ID cannot be null");
        }
        
        return favoritesRepository.findById(favoriteId)
            .orElseThrow(() -> new NotFoundException("Favorite", favoriteId));
    }
    
    /**
     * دریافت تمام علاقه‌مندی‌های یک کاربر
     * 
     * @param userId شناسه کاربر
     * @return لیست علاقه‌مندی‌های کاربر (مرتب شده بر اساس تاریخ)
     * @throws NotFoundException اگر کاربر یافت نشود
     */
    public List<Favorite> getUserFavorites(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        
        return favoritesRepository.findByUser(user);
    }
    
    /**
     * دریافت لیست کاربرانی که رستوران خاص را علاقه‌مندی کرده‌اند
     * 
     * برای آمارگیری و تحلیل محبوبیت رستوران استفاده می‌شود
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست علاقه‌مندی‌های رستوران
     * @throws NotFoundException اگر رستوران یافت نشود
     */
    public List<Favorite> getRestaurantFavorites(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return favoritesRepository.findByRestaurant(restaurant);
    }
    
    /**
     * بررسی اینکه آیا کاربر خاص، رستوران خاص را علاقه‌مندی کرده است
     * 
     * این متد برخلاف سایر متدها exception نمی‌اندازد و false برمی‌گرداند
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @return true اگر علاقه‌مندی باشد، false در غیر این صورت
     */
    public boolean isFavorite(Long userId, Long restaurantId) {
        if (userId == null || restaurantId == null) {
            return false;
        }
        
        try {
            User user = authRepository.findById(userId).orElse(null);
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
            
            if (user == null || restaurant == null) {
                return false;
            }
            
            return favoritesRepository.findByUserAndRestaurant(user, restaurant).isPresent();
        } catch (Exception e) {
            logger.error("Error checking if user {} favorited restaurant {}: {}", 
                        userId, restaurantId, e.getMessage());
            return false;
        }
    }
    
    /**
     * دریافت علاقه‌مندی خاص کاربر برای رستوران مشخص
     * 
     * برای دریافت جزئیات کامل علاقه‌مندی شامل یادداشت و تاریخ
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @return Optional حاوی علاقه‌مندی یا empty
     */
    public Optional<Favorite> getUserFavoriteForRestaurant(Long userId, Long restaurantId) {
        if (userId == null || restaurantId == null) {
            return Optional.empty();
        }
        
        try {
            User user = authRepository.findById(userId).orElse(null);
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
            
            if (user == null || restaurant == null) {
                return Optional.empty();
            }
            
            return favoritesRepository.findByUserAndRestaurant(user, restaurant);
        } catch (Exception e) {
            logger.error("Error getting user {} favorite for restaurant {}: {}", 
                        userId, restaurantId, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * دریافت علاقه‌مندی‌های اخیر (در چند روز گذشته)
     * 
     * برای تحلیل تrendها و الگوهای رفتاری کاربران
     * 
     * @param days تعداد روزهای گذشته
     * @return لیست علاقه‌مندی‌های اخیر
     * @throws IllegalArgumentException اگر days منفی باشد
     */
    public List<Favorite> getRecentFavorites(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Days must be positive");
        }
        
        return favoritesRepository.findRecentFavorites(days);
    }
    
    /**
     * دریافت علاقه‌مندی‌های دارای یادداشت
     * 
     * برای تحلیل engagement کاربران و کیفیت علاقه‌مندی‌ها
     * 
     * @return لیست علاقه‌مندی‌های دارای یادداشت
     */
    public List<Favorite> getFavoritesWithNotes() {
        return favoritesRepository.findFavoritesWithNotes();
    }
    
    /**
     * دریافت تعداد علاقه‌مندان یک رستوران
     * 
     * برای محاسبه امتیاز محبوبیت و رتبه‌بندی
     * 
     * @param restaurantId شناسه رستوران
     * @return تعداد علاقه‌مندان
     * @throws NotFoundException اگر رستوران یافت نشود
     */
    public Long getRestaurantFavoriteCount(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return favoritesRepository.countByRestaurant(restaurant);
    }
    
    /**
     * دریافت تعداد علاقه‌مندی‌های یک کاربر
     * 
     * برای نمایش در پروفایل کاربر
     * 
     * @param userId شناسه کاربر
     * @return تعداد علاقه‌مندی‌ها
     * @throws NotFoundException اگر کاربر یافت نشود
     */
    public Long getUserFavoriteCount(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        
        return favoritesRepository.countByUser(user);
    }
    
    /**
     * دریافت تمام علاقه‌مندی‌ها (تنها برای ادمین)
     * 
     * برای مدیریت سیستم و آمارهای کلی
     * 
     * @return لیست تمام علاقه‌مندی‌ها
     */
    public List<Favorite> getAllFavorites() {
        return favoritesRepository.findAll();
    }
    
    /**
     * دریافت علاقه‌مندی‌ها با صفحه‌بندی
     * 
     * برای بهبود عملکرد در UI های با تعداد زیاد داده
     * 
     * @param page شماره صفحه (شروع از 0)
     * @param size تعداد آیتم در هر صفحه
     * @return لیست علاقه‌مندی‌ها با صفحه‌بندی
     * @throws IllegalArgumentException اگر پارامترها نامعتبر باشند
     */
    public List<Favorite> getFavoritesWithPagination(int page, int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page must be non-negative and size must be positive");
        }
        
        int offset = page * size;
        return favoritesRepository.findWithPagination(offset, size);
    }
    
    /**
     * دریافت کل تعداد علاقه‌مندی‌ها در سیستم
     * 
     * برای آمارهای کلی و محاسبه pagination
     * 
     * @return تعداد کل علاقه‌مندی‌ها
     */
    public Long getTotalFavoriteCount() {
        return favoritesRepository.countAll();
    }
    
    /**
     * حذف علاقه‌مندی توسط ادمین
     * 
     * برای مواقع اضطراری یا مدیریت محتوا
     * 
     * @param favoriteId شناسه علاقه‌مندی
     * @return true در صورت حذف موفق
     * @throws IllegalArgumentException اگر favoriteId null باشد
     */
    public boolean deleteFavorite(Long favoriteId) {
        if (favoriteId == null) {
            throw new IllegalArgumentException("Favorite ID cannot be null");
        }
        
        boolean deleted = favoritesRepository.delete(favoriteId);
        if (deleted) {
            logger.info("Deleted favorite with ID: {}", favoriteId);
        }
        return deleted;
    }
    
    /**
     * دریافت آمارهای کامل علاقه‌مندی‌های یک کاربر
     * 
     * شامل تعداد کل، دارای یادداشت، اخیر و درصدها
     * 
     * @param userId شناسه کاربر
     * @return آبجکت حاوی تمام آمارها
     * @throws NotFoundException اگر کاربر یافت نشود
     */
    public FavoriteStats getUserFavoriteStats(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        
        List<Favorite> favorites = favoritesRepository.findByUser(user);
        Long totalCount = (long) favorites.size();
        Long withNotesCount = favorites.stream()
            .mapToLong(f -> f.hasNotes() ? 1L : 0L)
            .sum();
        Long recentCount = favorites.stream()
            .mapToLong(f -> f.isRecent() ? 1L : 0L)
            .sum();
        
        return new FavoriteStats(totalCount, withNotesCount, recentCount);
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * اعتبارسنجی ورودی‌های عملیات علاقه‌مندی
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @throws IllegalArgumentException اگر هر کدام null باشند
     */
    private void validateFavoriteInputs(Long userId, Long restaurantId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
    }
    
    /**
     * کلاس داخلی برای آمارهای علاقه‌مندی
     * 
     * این کلاس اطلاعات آماری کاملی از علاقه‌مندی‌های کاربر ارائه می‌دهد
     * شامل محاسبات درصد و متدهای کمکی
     */
    public static class FavoriteStats {
        /** تعداد کل علاقه‌مندی‌ها */
        private final Long totalFavorites;
        
        /** تعداد علاقه‌مندی‌های دارای یادداشت */
        private final Long favoritesWithNotes;
        
        /** تعداد علاقه‌مندی‌های اخیر */
        private final Long recentFavorites;
        
        /**
         * سازنده کلاس آمارها
         * 
         * @param totalFavorites تعداد کل
         * @param favoritesWithNotes تعداد دارای یادداشت
         * @param recentFavorites تعداد اخیر
         */
        public FavoriteStats(Long totalFavorites, Long favoritesWithNotes, Long recentFavorites) {
            this.totalFavorites = totalFavorites != null ? totalFavorites : 0L;
            this.favoritesWithNotes = favoritesWithNotes != null ? favoritesWithNotes : 0L;
            this.recentFavorites = recentFavorites != null ? recentFavorites : 0L;
        }
        
        /**
         * @return تعداد کل علاقه‌مندی‌ها
         */
        public Long getTotalFavorites() {
            return totalFavorites;
        }
        
        /**
         * @return تعداد علاقه‌مندی‌های دارای یادداشت
         */
        public Long getFavoritesWithNotes() {
            return favoritesWithNotes;
        }
        
        /**
         * @return تعداد علاقه‌مندی‌های اخیر
         */
        public Long getRecentFavorites() {
            return recentFavorites;
        }
        
        /**
         * بررسی وجود علاقه‌مندی
         * 
         * @return true اگر حداقل یک علاقه‌مندی داشته باشد
         */
        public boolean hasFavorites() {
            return totalFavorites > 0;
        }
        
        /**
         * محاسبه درصد علاقه‌مندی‌های دارای یادداشت
         * 
         * @return درصد یادداشت‌دار (0-100)
         */
        public double getNotesPercentage() {
            return totalFavorites > 0 ? (favoritesWithNotes.doubleValue() / totalFavorites.doubleValue()) * 100 : 0.0;
        }
        
        /**
         * محاسبه درصد علاقه‌مندی‌های اخیر
         * 
         * @return درصد اخیر (0-100)
         */
        public double getRecentPercentage() {
            return totalFavorites > 0 ? (recentFavorites.doubleValue() / totalFavorites.doubleValue()) * 100 : 0.0;
        }
        
        /**
         * نمایش متنی آمارها
         */
        @Override
        public String toString() {
            return "FavoriteStats{" +
                    "totalFavorites=" + totalFavorites +
                    ", favoritesWithNotes=" + favoritesWithNotes +
                    ", recentFavorites=" + recentFavorites +
                    '}';
        }
    }
} 