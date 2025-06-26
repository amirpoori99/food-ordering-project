package com.myapp.restaurant;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.PerformanceUtil;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * سرویس مدیریت رستوران‌ها - لایه منطق کسب‌وکار
 * این کلاس مسئول پردازش منطق کسب‌وکار مربوط به رستوران‌هاست
 * شامل ثبت، به‌روزرسانی، مدیریت وضعیت و عملکرد بهینه با caching
 */
public class RestaurantService {
    
    // repository برای دسترسی به داده‌های رستوران
    private final RestaurantRepository restaurantRepository;
    
    /**
     * سازنده پیش‌فرض - ایجاد نمونه repository
     */
    public RestaurantService() {
        this.restaurantRepository = new RestaurantRepository();
    }
    
    /**
     * سازنده برای تزریق وابستگی (استفاده در تست)
     * 
     * @param restaurantRepository repository رستوران
     */
    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * ثبت رستوران جدید در سیستم
     * رستوران با وضعیت PENDING ثبت می‌شود و منتظر تأیید مدیر است
     * 
     * @param ownerId شناسه مالک رستوران
     * @param name نام رستوران
     * @param address آدرس رستوران
     * @param phone شماره تلفن رستوران
     * @return رستوران ثبت شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     */
    public Restaurant registerRestaurant(Long ownerId, String name, String address, String phone) {
        // اعتبارسنجی ورودی‌ها
        validateRegistrationInput(ownerId, name, address, phone);
        
        // ایجاد رستوران جدید با trim کردن رشته‌ها
        Restaurant restaurant = new Restaurant(ownerId, name.trim(), address.trim(), phone.trim());
        return restaurantRepository.saveNew(restaurant);
    }
    
    /**
     * ثبت رستوران با شیء Restaurant
     * این متد overload شده برای راحتی استفاده است
     * 
     * @param restaurant شیء رستوران
     * @return رستوران ثبت شده
     * @throws IllegalArgumentException در صورت null بودن رستوران یا نامعتبر بودن اطلاعات
     */
    public Restaurant registerRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
        
        // اعتبارسنجی اطلاعات رستوران
        validateRegistrationInput(restaurant.getOwnerId(), restaurant.getName(), 
                                restaurant.getAddress(), restaurant.getPhone());
        
        // تنظیم وضعیت پیش‌فرض برای رستوران‌های جدید
        if (restaurant.getStatus() == null) {
            restaurant.setStatus(RestaurantStatus.PENDING);
        }
        
        return restaurantRepository.saveNew(restaurant);
    }
    
    /**
     * دریافت رستوران با شناسه
     * 
     * @param id شناسه رستوران
     * @return رستوران یافت شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه
     * @throws NotFoundException در صورت یافت نشدن رستوران
     */
    public Restaurant getRestaurantById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        Optional<Restaurant> restaurant = restaurantRepository.findById(id);
        if (restaurant.isEmpty()) {
            throw new NotFoundException("Restaurant", id);
        }
        
        return restaurant.get();
    }
    
    /**
     * دریافت همه رستوران‌های متعلق به یک مالک
     * 
     * @param ownerId شناسه مالک
     * @return لیست رستوران‌های مالک
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه مالک
     */
    public List<Restaurant> getRestaurantsByOwner(Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID must be positive");
        }
        
        return restaurantRepository.listByOwner(ownerId);
    }
    
    /**
     * دریافت همه رستوران‌های تأیید شده (نمایش عمومی) - بهینه‌سازی شده با caching
     * این متد برای نمایش رستوران‌ها در صفحه اصلی استفاده می‌شود
     * 
     * @return لیست رستوران‌های تأیید شده
     */
    public List<Restaurant> getApprovedRestaurants() {
        String cacheKey = PerformanceUtil.createQueryCacheKey("approved_restaurants");
        
        return PerformanceUtil.executeWithCache(
            cacheKey,
            () -> {
                // اندازه‌گیری عملکرد عملیات دیتابیس
                PerformanceUtil.PerformanceResult<List<Restaurant>> result = 
                    PerformanceUtil.measurePerformance("getApprovedRestaurants", 
                        () -> restaurantRepository.listApproved());
                
                // نمایش نتیجه عملکرد با emoji
                System.out.println("🏪 " + result.toString());
                return result.getResult();
            },
            List.class,
            15 // Cache برای 15 دقیقه
        );
    }
    
    /**
     * دریافت همه رستوران‌ها (برای مدیران)
     * 
     * @return لیست همه رستوران‌ها
     */
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }
    
    /**
     * دریافت رستوران‌ها بر اساس وضعیت
     * 
     * @param status وضعیت مورد نظر (PENDING, APPROVED, REJECTED, SUSPENDED)
     * @return لیست رستوران‌ها با وضعیت مشخص
     * @throws IllegalArgumentException در صورت null بودن وضعیت
     */
    public List<Restaurant> getRestaurantsByStatus(RestaurantStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        return restaurantRepository.findByStatus(status);
    }
    
    /**
     * به‌روزرسانی وضعیت رستوران (عملکرد مدیریتی)
     * فقط مدیران اجازه تغییر وضعیت رستوران‌ها را دارند
     * 
     * @param id شناسه رستوران
     * @param status وضعیت جدید
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     * @throws NotFoundException در صورت یافت نشدن رستوران
     */
    public void updateRestaurantStatus(Long id, RestaurantStatus status) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        // بررسی وجود رستوران
        getRestaurantById(id);
        
        // به‌روزرسانی وضعیت در دیتابیس
        restaurantRepository.updateStatus(id, status);
    }
    
    /**
     * Update restaurant information
     */
    public Restaurant updateRestaurant(Long id, String name, String address, String phone) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        Restaurant existingRestaurant = getRestaurantById(id);
        
        // Update fields if provided
        if (name != null && !name.trim().isEmpty()) {
            if (name.trim().length() > 100) {
                throw new IllegalArgumentException("Restaurant name cannot exceed 100 characters");
            }
            existingRestaurant.setName(name.trim());
        }
        
        if (address != null && !address.trim().isEmpty()) {
            if (address.trim().length() > 200) {
                throw new IllegalArgumentException("Restaurant address cannot exceed 200 characters");
            }
            existingRestaurant.setAddress(address.trim());
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            if (phone.trim().length() > 20) {
                throw new IllegalArgumentException("Restaurant phone cannot exceed 20 characters");
            }
            existingRestaurant.setPhone(phone.trim());
        }
        
        return restaurantRepository.save(existingRestaurant);
    }
    
    /**
     * Update restaurant with Restaurant object
     */
    public Restaurant updateRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
        if (restaurant.getId() == null || restaurant.getId() <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // Get existing restaurant to preserve all fields
        Restaurant existingRestaurant = getRestaurantById(restaurant.getId());
        
        // Update only the provided fields
        if (restaurant.getName() != null && !restaurant.getName().trim().isEmpty()) {
            if (restaurant.getName().trim().length() > 100) {
                throw new IllegalArgumentException("Restaurant name cannot exceed 100 characters");
            }
            existingRestaurant.setName(restaurant.getName().trim());
        }
        
        if (restaurant.getAddress() != null && !restaurant.getAddress().trim().isEmpty()) {
            if (restaurant.getAddress().trim().length() > 200) {
                throw new IllegalArgumentException("Restaurant address cannot exceed 200 characters");
            }
            existingRestaurant.setAddress(restaurant.getAddress().trim());
        }
        
        if (restaurant.getPhone() != null && !restaurant.getPhone().trim().isEmpty()) {
            if (restaurant.getPhone().trim().length() > 20) {
                throw new IllegalArgumentException("Restaurant phone cannot exceed 20 characters");
            }
            existingRestaurant.setPhone(restaurant.getPhone().trim());
        }
        
        // Preserve ownerId and status from existing restaurant
        // (they should not be updated via this method)
        
        return restaurantRepository.save(existingRestaurant);
    }
    
    /**
     * Delete restaurant
     */
    public void deleteRestaurant(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // Verify restaurant exists
        getRestaurantById(id);
        
        restaurantRepository.delete(id);
    }
    
    /**
     * بررسی وجود رستوران با شناسه
     * 
     * @param id شناسه رستوران
     * @return true اگر رستوران وجود داشته باشد
     */
    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        
        return restaurantRepository.findById(id).isPresent();
    }
    
    /**
     * دریافت رستوران‌های در انتظار تأیید (برای مدیران)
     * 
     * @return لیست رستوران‌های PENDING
     */
    public List<Restaurant> getPendingRestaurants() {
        return restaurantRepository.findByStatus(RestaurantStatus.PENDING);
    }
    
    /**
     * تأیید رستوران - تغییر وضعیت به APPROVED
     * 
     * @param id شناسه رستوران
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه
     * @throws NotFoundException در صورت یافت نشدن رستوران
     */
    public void approveRestaurant(Long id) {
        updateRestaurantStatus(id, RestaurantStatus.APPROVED);
    }
    
    /**
     * رد رستوران - تغییر وضعیت به REJECTED
     * 
     * @param id شناسه رستوران
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه
     * @throws NotFoundException در صورت یافت نشدن رستوران
     */
    public void rejectRestaurant(Long id) {
        updateRestaurantStatus(id, RestaurantStatus.REJECTED);
    }
    
    /**
     * تعلیق رستوران - تغییر وضعیت به SUSPENDED
     * 
     * @param id شناسه رستوران
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه
     * @throws NotFoundException در صورت یافت نشدن رستوران
     */
    public void suspendRestaurant(Long id) {
        updateRestaurantStatus(id, RestaurantStatus.SUSPENDED);
    }
    
    /**
     * Bulk update restaurant statuses - Optimized with async processing
     */
    public Future<Void> bulkUpdateRestaurantStatus(List<Long> restaurantIds, RestaurantStatus status) {
        if (restaurantIds == null || restaurantIds.isEmpty()) {
            throw new IllegalArgumentException("Restaurant IDs cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        return PerformanceUtil.executeAsync(() -> {
            PerformanceUtil.measurePerformance("bulkUpdateRestaurantStatus", () -> {
                // Process in batches of 50
                PerformanceUtil.processBatch(restaurantIds, 50, batch -> {
                    for (Long id : batch) {
                        try {
                            updateRestaurantStatus(id, status);
                        } catch (Exception e) {
                            System.err.println("Failed to update restaurant " + id + ": " + e.getMessage());
                        }
                    }
                });
                
                // Clear relevant caches after bulk update
                clearRestaurantCaches();
                return null;
            });
        });
    }
    
    /**
     * Bulk approve restaurants - Optimized async operation
     */
    public Future<Void> bulkApproveRestaurants(List<Long> restaurantIds) {
        return bulkUpdateRestaurantStatus(restaurantIds, RestaurantStatus.APPROVED);
    }
    
    /**
     * Clear restaurant-related caches after data modifications
     */
    private void clearRestaurantCaches() {
        // Clear specific cache keys
        String approvedKey = PerformanceUtil.createQueryCacheKey("approved_restaurants");
        String statsKey = PerformanceUtil.createQueryCacheKey("restaurant_statistics");
        
        // Note: PerformanceUtil doesn't have specific key removal, so we'd need to add that
        // For now, we can force a cache cleanup
        PerformanceUtil.cleanExpiredEntries();
    }
    
    /**
     * Get restaurant statistics - Optimized with caching and performance monitoring
     */
    public RestaurantStatistics getRestaurantStatistics() {
        String cacheKey = PerformanceUtil.createQueryCacheKey("restaurant_statistics");
        
        return PerformanceUtil.executeWithCache(
            cacheKey,
            () -> {
                return PerformanceUtil.measurePerformance("calculateRestaurantStatistics", () -> {
                    List<Restaurant> allRestaurants = getAllRestaurants();
                    
                    // Check memory usage before processing large dataset
                    if (PerformanceUtil.isMemoryUsageCritical()) {
                        PerformanceUtil.forceGarbageCollection();
                    }
                    
                    long totalCount = allRestaurants.size();
                    long approvedCount = allRestaurants.parallelStream()
                            .filter(r -> r.getStatus() == RestaurantStatus.APPROVED)
                            .count();
                    long pendingCount = allRestaurants.parallelStream()
                            .filter(r -> r.getStatus() == RestaurantStatus.PENDING)
                            .count();
                    long rejectedCount = allRestaurants.parallelStream()
                            .filter(r -> r.getStatus() == RestaurantStatus.REJECTED)
                            .count();
                    long suspendedCount = allRestaurants.parallelStream()
                            .filter(r -> r.getStatus() == RestaurantStatus.SUSPENDED)
                            .count();
                    
                    return new RestaurantStatistics(totalCount, approvedCount, pendingCount, 
                                                  rejectedCount, suspendedCount);
                }).getResult();
            },
            RestaurantStatistics.class,
            30 // Cache for 30 minutes
        );
    }
    
    // متدهای خصوصی برای اعتبارسنجی
    
    /**
     * اعتبارسنجی اطلاعات ورودی برای ثبت رستوران
     * بررسی تمام محدودیت‌های کسب‌وکار برای اطلاعات رستوران
     * 
     * @param ownerId شناسه مالک رستوران
     * @param name نام رستوران
     * @param address آدرس رستوران
     * @param phone شماره تلفن رستوران
     * @throws IllegalArgumentException در صورت نامعتبر بودن هر کدام از ورودی‌ها
     */
    private void validateRegistrationInput(Long ownerId, String name, String address, String phone) {
        // بررسی شناسه مالک
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID must be positive");
        }
        
        // بررسی نام رستوران
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be empty");
        }
        
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Restaurant name cannot exceed 100 characters");
        }
        
        // بررسی آدرس رستوران
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant address cannot be empty");
        }
        
        if (address.trim().length() > 200) {
            throw new IllegalArgumentException("Restaurant address cannot exceed 200 characters");
        }
        
        // بررسی شماره تلفن
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant phone cannot be empty");
        }
        
        if (phone.trim().length() > 20) {
            throw new IllegalArgumentException("Restaurant phone cannot exceed 20 characters");
        }
    }
    
    // کلاس داخلی برای آمار رستوران‌ها
    
    /**
     * کلاس آمار رستوران‌ها
     * حاوی اطلاعات خلاصه از وضعیت رستوران‌ها در سیستم
     */
    public static class RestaurantStatistics {
        private final long totalCount;      // تعداد کل رستوران‌ها
        private final long approvedCount;   // تعداد رستوران‌های تأیید شده
        private final long pendingCount;    // تعداد رستوران‌های در انتظار
        private final long rejectedCount;   // تعداد رستوران‌های رد شده
        private final long suspendedCount;  // تعداد رستوران‌های تعلیق شده
        
        /**
         * سازنده آمار رستوران‌ها
         * 
         * @param totalCount تعداد کل
         * @param approvedCount تعداد تأیید شده
         * @param pendingCount تعداد در انتظار
         * @param rejectedCount تعداد رد شده
         * @param suspendedCount تعداد تعلیق شده
         */
        public RestaurantStatistics(long totalCount, long approvedCount, long pendingCount,
                                  long rejectedCount, long suspendedCount) {
            this.totalCount = totalCount;
            this.approvedCount = approvedCount;
            this.pendingCount = pendingCount;
            this.rejectedCount = rejectedCount;
            this.suspendedCount = suspendedCount;
        }
        
        // Getter methods برای دسترسی به آمار
        public long getTotalCount() { return totalCount; }
        public long getApprovedCount() { return approvedCount; }
        public long getPendingCount() { return pendingCount; }
        public long getRejectedCount() { return rejectedCount; }
        public long getSuspendedCount() { return suspendedCount; }
    }
}
