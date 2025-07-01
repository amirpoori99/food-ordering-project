package com.myapp.review;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Rating;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.User;
import com.myapp.restaurant.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service لایه منطق کسب‌وکار برای مدیریت نظرات و امتیازدهی
 * 
 * این کلاس تمام عملیات مربوط به منطق کسب‌وکار نظرات و امتیازات را پیاده‌سازی می‌کند:
 * 
 * === عملیات اصلی CRUD ===
 * - createRating(): ایجاد نظر و امتیاز جدید
 * - updateRating(): به‌روزرسانی نظر موجود
 * - getRating(): دریافت نظر بر اساس شناسه
 * - deleteRating(): حذف نظر (فقط توسط صاحب نظر)
 * 
 * === جستجو و دریافت اطلاعات ===
 * - getRestaurantRatings(): تمام نظرات رستوران
 * - getUserRatings(): تمام نظرات کاربر
 * - getRestaurantRatingStats(): آمار کامل امتیازات رستوران
 * - hasUserRatedRestaurant(): بررسی نظردهی کاربر به رستوران
 * - getUserRatingForRestaurant(): نظر کاربر برای رستوران خاص
 * 
 * === عملیات تعامل کاربران ===
 * - markAsHelpful(): علامت‌گذاری نظر به عنوان مفید
 * - removeHelpfulMark(): حذف علامت مفید از نظر
 * 
 * === عملیات مدیریتی ===
 * - verifyRating(): تایید نظر (توسط ادمین)
 * - unverifyRating(): لغو تایید نظر (توسط ادمین)
 * - getAllRatings(): دریافت تمام نظرات (ادمین)
 * 
 * === فیلترها و جستجوهای پیشرفته ===
 * - getRatingsByScore(): نظرات در بازه امتیاز
 * - getVerifiedRatings(): نظرات تایید شده
 * - getRatingsWithReviews(): نظرات دارای متن
 * - getRecentRatings(): نظرات اخیر
 * 
 * === صفحه‌بندی و آمار ===
 * - getRatingsWithPagination(): دریافت با صفحه‌بندی
 * - getTotalRatingCount(): تعداد کل نظرات
 * 
 * === ویژگی‌های کلیدی ===
 * - Business Rules Enforcement: اجرای قوانین کسب‌وکار
 * - Data Validation: اعتبارسنجی کامل داده‌ها
 * - User Permission Checks: بررسی مجوزهای کاربران
 * - Duplicate Prevention: جلوگیری از نظر تکراری هر کاربر
 * - Owner Restriction: منع نظردهی مالک به رستوران خود
 * - Statistical Analysis: تحلیل آماری امتیازات
 * - Error Handling: مدیریت خطاها و validation
 * - Dependency Injection: پشتیبانی از تزریق وابستگی‌ها
 * 
 * === Inner Classes ===
 * - RatingStats: کلاس آمار امتیازات رستوران
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class RatingService {
    
    /** Logger برای ثبت عملیات و خطاها */
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
    
    /** Repository لایه دسترسی داده نظرات */
    private final RatingRepository ratingRepository;
    /** Repository لایه دسترسی داده کاربران */
    private final AuthRepository authRepository;
    /** Repository لایه دسترسی داده رستوران‌ها */
    private final RestaurantRepository restaurantRepository;
    
    /**
     * سازنده پیش‌فرض - Repository های مورد نیاز را ایجاد می‌کند
     */
    public RatingService() {
        this.ratingRepository = new RatingRepository();
        this.authRepository = new AuthRepository();
        this.restaurantRepository = new RestaurantRepository();
    }
    
    /**
     * سازنده برای تزریق وابستگی‌ها (برای تست‌ها)
     * 
     * @param ratingRepository repository نظرات
     * @param authRepository repository کاربران
     * @param restaurantRepository repository رستوران‌ها
     */
    public RatingService(RatingRepository ratingRepository, AuthRepository authRepository, RestaurantRepository restaurantRepository) {
        this.ratingRepository = ratingRepository;
        this.authRepository = authRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * ایجاد نظر و امتیاز جدید برای رستوران
     * 
     * قوانین کسب‌وکار:
     * - هر کاربر فقط یک نظر برای هر رستوران
     * - مالک رستوران نمی‌تواند به رستوران خودش نظر دهد
     * - امتیاز باید بین 1 تا 5 باشد
     * 
     * @param userId شناسه کاربر نظردهنده
     * @param restaurantId شناسه رستوران
     * @param score امتیاز (1-5)
     * @param reviewText متن نظر (اختیاری)
     * @return نظر ایجاد شده
     * @throws IllegalArgumentException در صورت ورودی‌های نامعتبر یا تکراری بودن
     * @throws NotFoundException در صورت عدم وجود کاربر یا رستوران
     */
    public Rating createRating(Long userId, Long restaurantId, Integer score, String reviewText) {
        logger.info("Creating rating: userId={}, restaurantId={}, score={}", userId, restaurantId, score);
        
        // اعتبارسنجی ورودی‌ها
        validateRatingInputs(userId, restaurantId, score);
        
        // دریافت کاربر و رستوران
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        // بررسی عدم وجود نظر قبلی از همین کاربر
        Optional<Rating> existingRating = ratingRepository.findByUserAndRestaurant(user, restaurant);
        if (existingRating.isPresent()) {
            throw new IllegalArgumentException("User has already rated this restaurant. Use update instead.");
        }
        
        // بررسی عدم نظردهی مالک به رستوران خودش
        if (restaurant.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Restaurant owners cannot rate their own restaurants");
        }
        
        // ایجاد و ذخیره نظر
        Rating rating = new Rating(user, restaurant, score, reviewText);
        Rating savedRating = ratingRepository.save(rating);
        
        logger.info("Created rating with ID: {}", savedRating.getId());
        return savedRating;
    }
    
    /**
     * به‌روزرسانی نظر موجود
     * 
     * کاربران می‌توانند امتیاز و/یا متن نظر خود را ویرایش کنند
     * 
     * @param ratingId شناسه نظر
     * @param newScore امتیاز جدید (اختیاری)
     * @param newReviewText متن نظر جدید (اختیاری)
     * @return نظر به‌روزرسانی شده
     * @throws IllegalArgumentException در صورت ورودی‌های نامعتبر
     * @throws NotFoundException در صورت عدم وجود نظر
     */
    public Rating updateRating(Long ratingId, Integer newScore, String newReviewText) {
        logger.info("Updating rating: ratingId={}, newScore={}", ratingId, newScore);
        
        // اعتبارسنجی ورودی‌ها
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        if (newScore != null && (newScore < 1 || newScore > 5)) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5");
        }
        
        // دریافت نظر موجود
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        // به‌روزرسانی نظر
        if (newScore != null) {
            rating.updateRating(newScore, newReviewText);
        } else if (newReviewText != null) {
            rating.setReviewText(newReviewText);
        }
        
        Rating updatedRating = ratingRepository.save(rating);
        logger.info("Updated rating with ID: {}", updatedRating.getId());
        return updatedRating;
    }
    
    /**
     * دریافت نظر بر اساس شناسه
     * 
     * @param ratingId شناسه نظر
     * @return نظر یافت شده
     * @throws IllegalArgumentException در صورت null بودن ID
     * @throws NotFoundException در صورت عدم وجود نظر
     */
    public Rating getRating(Long ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        
        return ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
    }
    
    /**
     * دریافت تمام نظرات رستوران خاص
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست نظرات رستوران
     * @throws IllegalArgumentException در صورت null بودن ID
     * @throws NotFoundException در صورت عدم وجود رستوران
     */
    public List<Rating> getRestaurantRatings(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return ratingRepository.findByRestaurant(restaurant);
    }
    
    /**
     * دریافت تمام نظرات ثبت شده توسط کاربر
     * 
     * @param userId شناسه کاربر
     * @return لیست نظرات کاربر
     * @throws IllegalArgumentException در صورت null بودن ID
     * @throws NotFoundException در صورت عدم وجود کاربر
     */
    public List<Rating> getUserRatings(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        
        return ratingRepository.findByUser(user);
    }
    
    /**
     * دریافت آمار کامل امتیازات رستوران
     * 
     * شامل میانگین امتیاز، تعداد کل نظرات و توزیع امتیازات
     * 
     * @param restaurantId شناسه رستوران
     * @return آمار امتیازات رستوران
     * @throws IllegalArgumentException در صورت null بودن ID
     * @throws NotFoundException در صورت عدم وجود رستوران
     */
    public RatingStats getRestaurantRatingStats(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        Double averageRating = ratingRepository.getAverageRating(restaurant);
        Long totalRatings = ratingRepository.getRatingCount(restaurant);
        Map<Integer, Long> distribution = ratingRepository.getRatingDistribution(restaurant);
        
        return new RatingStats(averageRating, totalRatings, distribution);
    }
    
    /**
     * بررسی اینکه آیا کاربر به رستوران نظر داده است
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @return true اگر کاربر نظر داده باشد، false در غیر این صورت
     */
    public boolean hasUserRatedRestaurant(Long userId, Long restaurantId) {
        if (userId == null || restaurantId == null) {
            return false;
        }
        
        try {
            User user = authRepository.findById(userId).orElse(null);
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
            
            if (user == null || restaurant == null) {
                return false;
            }
            
            return ratingRepository.findByUserAndRestaurant(user, restaurant).isPresent();
        } catch (Exception e) {
            logger.error("Error checking if user {} rated restaurant {}: {}", userId, restaurantId, e.getMessage());
            return false;
        }
    }
    
    /**
     * دریافت نظر کاربر برای رستوران خاص
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @return Optional حاوی نظر کاربر یا empty
     */
    public Optional<Rating> getUserRatingForRestaurant(Long userId, Long restaurantId) {
        if (userId == null || restaurantId == null) {
            return Optional.empty();
        }
        
        try {
            User user = authRepository.findById(userId).orElse(null);
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
            
            if (user == null || restaurant == null) {
                return Optional.empty();
            }
            
            return ratingRepository.findByUserAndRestaurant(user, restaurant);
        } catch (Exception e) {
            logger.error("Error getting user {} rating for restaurant {}: {}", userId, restaurantId, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * حذف نظر (فقط توسط صاحب نظر)
     * 
     * کاربران فقط می‌توانند نظرات خودشان را حذف کنند
     * 
     * @param ratingId شناسه نظر
     * @param userId شناسه کاربر درخواست‌کننده حذف
     * @return true در صورت حذف موفق، false در غیر این صورت
     * @throws IllegalArgumentException در صورت null بودن IDs یا عدم مجوز
     * @throws NotFoundException در صورت عدم وجود نظر
     */
    public boolean deleteRating(Long ratingId, Long userId) {
        logger.info("Deleting rating: ratingId={}, userId={}", ratingId, userId);
        
        if (ratingId == null || userId == null) {
            throw new IllegalArgumentException("Rating ID and User ID cannot be null");
        }
        
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        // بررسی مالکیت نظر
        if (!rating.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Users can only delete their own ratings");
        }
        
        boolean deleted = ratingRepository.delete(ratingId);
        if (deleted) {
            logger.info("Deleted rating with ID: {}", ratingId);
        }
        return deleted;
    }
    
    /**
     * علامت‌گذاری نظر به عنوان مفید
     * 
     * افزایش شمارنده "مفید بودن" نظر توسط سایر کاربران
     * 
     * @param ratingId شناسه نظر
     * @return نظر به‌روزرسانی شده
     * @throws IllegalArgumentException در صورت null بودن ID
     * @throws NotFoundException در صورت عدم وجود نظر
     */
    public Rating markAsHelpful(Long ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        rating.incrementHelpfulCount();
        return ratingRepository.save(rating);
    }
    
    /**
     * حذف علامت مفید از نظر
     * 
     * کاهش شمارنده "مفید بودن" نظر
     * 
     * @param ratingId شناسه نظر
     * @return نظر به‌روزرسانی شده
     * @throws IllegalArgumentException در صورت null بودن ID
     * @throws NotFoundException در صورت عدم وجود نظر
     */
    public Rating removeHelpfulMark(Long ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        rating.decrementHelpfulCount();
        return ratingRepository.save(rating);
    }
    
    /**
     * تایید نظر (فقط توسط ادمین)
     * 
     * نظرات تایید شده اعتبار بیشتری دارند
     * 
     * @param ratingId شناسه نظر
     * @return نظر تایید شده
     * @throws IllegalArgumentException در صورت null بودن ID
     * @throws NotFoundException در صورت عدم وجود نظر
     */
    public Rating verifyRating(Long ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        rating.verify();
        Rating verifiedRating = ratingRepository.save(rating);
        logger.info("Verified rating with ID: {}", ratingId);
        return verifiedRating;
    }
    
    /**
     * لغو تایید نظر (فقط توسط ادمین)
     * 
     * @param ratingId شناسه نظر
     * @return نظر با تایید لغو شده
     * @throws IllegalArgumentException در صورت null بودن ID
     * @throws NotFoundException در صورت عدم وجود نظر
     */
    public Rating unverifyRating(Long ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        rating.unverify();
        Rating unverifiedRating = ratingRepository.save(rating);
        logger.info("Unverified rating with ID: {}", ratingId);
        return unverifiedRating;
    }
    
    /**
     * دریافت نظرات در بازه امتیاز مشخص
     * 
     * @param minScore حداقل امتیاز (1-5)
     * @param maxScore حداکثر امتیاز (1-5)
     * @return لیست نظرات در بازه امتیاز
     * @throws IllegalArgumentException در صورت بازه نامعتبر
     */
    public List<Rating> getRatingsByScore(int minScore, int maxScore) {
        if (minScore < 1 || maxScore > 5 || minScore > maxScore) {
            throw new IllegalArgumentException("Invalid score range. Must be between 1-5 and minScore <= maxScore");
        }
        
        return ratingRepository.findByScoreRange(minScore, maxScore);
    }
    
    /**
     * دریافت نظرات تایید شده
     * 
     * @return لیست نظرات تایید شده توسط ادمین
     */
    public List<Rating> getVerifiedRatings() {
        return ratingRepository.findVerifiedRatings();
    }
    
    /**
     * دریافت نظراتی که متن نظر دارند
     * 
     * نظراتی که علاوه بر امتیاز، توضیحات متنی نیز ارائه داده‌اند
     * 
     * @return لیست نظرات دارای متن
     */
    public List<Rating> getRatingsWithReviews() {
        return ratingRepository.findRatingsWithReviews();
    }
    
    /**
     * دریافت نظرات اخیر (در چند روز گذشته)
     * 
     * @param days تعداد روزهای گذشته
     * @return لیست نظرات اخیر
     * @throws IllegalArgumentException در صورت تعداد روز منفی یا صفر
     */
    public List<Rating> getRecentRatings(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Days must be positive");
        }
        
        return ratingRepository.findRecentRatings(days);
    }
    
    /**
     * دریافت تمام نظرات (فقط برای ادمین)
     * 
     * @return لیست تمام نظرات سیستم
     */
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }
    
    /**
     * دریافت نظرات با صفحه‌بندی
     * 
     * برای بهبود عملکرد در صفحات مدیریت
     * 
     * @param page شماره صفحه (شروع از 0)
     * @param size تعداد رکورد در هر صفحه
     * @return لیست نظرات با صفحه‌بندی
     * @throws IllegalArgumentException در صورت پارامترهای نامعتبر
     */
    public List<Rating> getRatingsWithPagination(int page, int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page must be non-negative and size must be positive");
        }
        
        int offset = page * size;
        return ratingRepository.findWithPagination(offset, size);
    }
    
    /**
     * دریافت تعداد کل نظرات سیستم
     * 
     * @return تعداد کل نظرات
     */
    public Long getTotalRatingCount() {
        return ratingRepository.countAll();
    }
    
    // Private helper methods
    
    /**
     * اعتبارسنجی ورودی‌های ایجاد نظر
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @param score امتیاز
     * @throws IllegalArgumentException در صورت ورودی‌های نامعتبر
     */
    private void validateRatingInputs(Long userId, Long restaurantId, Integer score) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (score == null || score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5");
        }
    }
    
    /**
     * کلاس آمار امتیازات رستوران
     * 
     * این کلاس اطلاعات آماری کاملی از امتیازات رستوران ارائه می‌دهد:
     * - میانگین امتیاز
     * - تعداد کل نظرات
     * - توزیع امتیازات (تعداد هر امتیاز از 1 تا 5)
     * - متدهای کمکی برای نمایش و بررسی
     */
    public static class RatingStats {
        /** میانگین امتیاز رستوران */
        private final Double averageRating;
        /** تعداد کل نظرات */
        private final Long totalRatings;
        /** توزیع امتیازات (امتیاز -> تعداد) */
        private final Map<Integer, Long> distribution;
        
        /**
         * سازنده کلاس آمار
         * 
         * @param averageRating میانگین امتیاز
         * @param totalRatings تعداد کل نظرات
         * @param distribution توزیع امتیازات
         */
        public RatingStats(Double averageRating, Long totalRatings, Map<Integer, Long> distribution) {
            this.averageRating = averageRating != null ? averageRating : 0.0;
            this.totalRatings = totalRatings != null ? totalRatings : 0L;
            this.distribution = distribution != null ? distribution : Map.of();
        }
        
        /** دریافت میانگین امتیاز */
        public Double getAverageRating() {
            return averageRating;
        }
        
        /** دریافت تعداد کل نظرات */
        public Long getTotalRatings() {
            return totalRatings;
        }
        
        /** دریافت توزیع امتیازات */
        public Map<Integer, Long> getDistribution() {
            return distribution;
        }
        
        /** دریافت میانگین امتیاز با فرمت یک رقم اعشار */
        public String getAverageRatingFormatted() {
            return String.format("%.1f", averageRating);
        }
        
        /** بررسی وجود نظر برای رستوران */
        public boolean hasRatings() {
            return totalRatings > 0;
        }
        
        @Override
        public String toString() {
            return "RatingStats{" +
                    "averageRating=" + averageRating +
                    ", totalRatings=" + totalRatings +
                    ", distribution=" + distribution +
                    '}';
        }
    }
} 
