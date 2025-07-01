package com.myapp.coupon;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Coupon;
import com.myapp.common.models.CouponUsage;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.User;
import com.myapp.restaurant.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service لایه منطق کسب‌وکار برای مدیریت کوپن‌های تخفیف
 * 
 * این کلاس تمام عملیات مربوط به منطق کسب‌وکار کوپن‌ها را پیاده‌سازی می‌کند:
 * 
 * === ایجاد کوپن ===
 * - createPercentageCoupon(): ایجاد کوپن درصدی
 * - createFixedAmountCoupon(): ایجاد کوپن مبلغ ثابت
 * - createCouponWithSettings(): ایجاد کوپن با تنظیمات پیشرفته
 * 
 * === اعتبارسنجی و اعمال کوپن ===
 * - applyCoupon(): اعمال کوپن به سفارش
 * - validateCouponForOrder(): اعتبارسنجی بدون اعمال
 * - useCoupon(): استفاده از کوپن (افزایش شمارنده)
 * - useCouponWithTracking(): استفاده با ردیابی کامل
 * - revertCouponUsage(): برگشت استفاده (برای لغو سفارش)
 * 
 * === مدیریت کوپن ===
 * - getCoupon(): دریافت کوپن با ID
 * - getCouponByCode(): دریافت کوپن با کد
 * - getValidCoupons(): کوپن‌های معتبر
 * - getApplicableCoupons(): کوپن‌های قابل اعمال
 * - getRestaurantCoupons(): کوپن‌های رستوران
 * - getGlobalCoupons(): کوپن‌های سراسری
 * - getCouponsByCreator(): کوپن‌های ایجاد شده توسط کاربر
 * - getCouponsExpiringSoon(): کوپن‌های نزدیک به انقضا
 * 
 * === مدیریت وضعیت ===
 * - updateCoupon(): به‌روزرسانی اطلاعات
 * - activateCoupon(): فعال‌سازی کوپن
 * - deactivateCoupon(): غیرفعال‌سازی کوپن
 * - deleteCoupon(): حذف کوپن
 * - getCouponStatistics(): آمار کوپن‌ها
 * 
 * === ویژگی‌های پیشرفته ===
 * - Complex Validation Logic: منطق اعتبارسنجی پیچیده
 * - Permission Management: مدیریت مجوزهای دسترسی
 * - Usage Tracking: ردیابی دقیق استفاده
 * - Business Rule Enforcement: اجرای قوانین کسب‌وکار
 * - Error Handling: مدیریت خطاها و validation
 * - Dependency Injection: پشتیبانی از تزریق وابستگی‌ها
 * 
 * === Inner Classes ===
 * - CouponApplicationResult: نتیجه اعمال کوپن
 * - CouponValidationResult: نتیجه اعتبارسنجی
 * - CouponStatistics: آمار کوپن‌ها
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class CouponService {
    
    /** Logger برای ثبت عملیات و خطاها */
    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);
    
    /** Repository لایه دسترسی داده کوپن‌ها */
    private final CouponRepository couponRepository;
    /** Repository لایه دسترسی داده کاربران برای اعتبارسنجی مجوزها */
    private final AuthRepository authRepository;
    /** Repository لایه دسترسی داده رستوران‌ها */
    private final RestaurantRepository restaurantRepository;
    /** Repository لایه دسترسی داده استفاده از کوپن‌ها */
    private final CouponUsageRepository couponUsageRepository;
    
    /**
     * سازنده پیش‌فرض - Repository های مورد نیاز را ایجاد می‌کند
     */
    public CouponService() {
        this.couponRepository = new CouponRepository();
        this.authRepository = new AuthRepository();
        this.restaurantRepository = new RestaurantRepository();
        this.couponUsageRepository = new CouponUsageRepository();
    }
    
    /**
     * سازنده برای تزریق وابستگی‌ها (برای تست‌ها)
     * 
     * @param couponRepository repository کوپن‌ها
     * @param authRepository repository احراز هویت
     * @param restaurantRepository repository رستوران‌ها
     * @param couponUsageRepository repository استفاده از کوپن‌ها
     */
    public CouponService(CouponRepository couponRepository, AuthRepository authRepository, 
                        RestaurantRepository restaurantRepository, CouponUsageRepository couponUsageRepository) {
        this.couponRepository = couponRepository;
        this.authRepository = authRepository;
        this.restaurantRepository = restaurantRepository;
        this.couponUsageRepository = couponUsageRepository;
    }
    
    // ==================== COUPON CREATION ====================
    
    /**
     * ایجاد کوپن درصدی جدید
     * 
     * این متد کوپنی ایجاد می‌کند که درصد مشخصی از مبلغ سفارش را تخفیف می‌دهد
     * 
     * @param code کد یکتای کوپن (حداکثر 50 کاراکتر)
     * @param description توضیحات کوپن (حداکثر 255 کاراکتر)
     * @param percentage درصد تخفیف (0 تا 100)
     * @param validFrom تاریخ شروع اعتبار
     * @param validUntil تاریخ پایان اعتبار
     * @param createdBy شناسه کاربر ایجادکننده
     * @param restaurantId شناسه رستوران (null برای کوپن سراسری)
     * @return کوپن ایجاد شده
     * @throws IllegalArgumentException در صورت ورودی‌های نامعتبر
     * @throws NotFoundException در صورت عدم وجود رستوران یا کاربر
     */
    public Coupon createPercentageCoupon(String code, String description, Double percentage,
                                        LocalDateTime validFrom, LocalDateTime validUntil,
                                        Long createdBy, Long restaurantId) {
        logger.info("Creating percentage coupon: code={}, percentage={}", code, percentage);
        
        // اعتبارسنجی ورودی‌ها
        validateCouponCreationInputs(code, description, validFrom, validUntil, createdBy);
        validatePercentage(percentage);
        
        // بررسی یکتا بودن کد
        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Coupon code already exists: " + code);
        }
        
        // اعتبارسنجی مجوزهای ایجادکننده
        validateCreatorPermissions(createdBy, restaurantId);
        
        // ایجاد کوپن درصدی
        Coupon coupon = Coupon.createPercentageCoupon(code, description, percentage, validFrom, validUntil);
        coupon.setCreatedBy(createdBy);
        
        // تنظیم رستوران در صورت مشخص بودن
        if (restaurantId != null) {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
            coupon.setRestaurant(restaurant);
        }
        
        Coupon savedCoupon = couponRepository.save(coupon);
        logger.info("Created percentage coupon with ID: {}", savedCoupon.getId());
        return savedCoupon;
    }
    
    /**
     * ایجاد کوپن مبلغ ثابت جدید
     * 
     * این متد کوپنی ایجاد می‌کند که مبلغ ثابتی از سفارش کم می‌کند
     * 
     * @param code کد یکتای کوپن
     * @param description توضیحات کوپن
     * @param amount مبلغ تخفیف ثابت (مثبت و حداکثر 10,000)
     * @param validFrom تاریخ شروع اعتبار
     * @param validUntil تاریخ پایان اعتبار
     * @param createdBy شناسه کاربر ایجادکننده
     * @param restaurantId شناسه رستوران (null برای کوپن سراسری)
     * @return کوپن ایجاد شده
     * @throws IllegalArgumentException در صورت ورودی‌های نامعتبر
     * @throws NotFoundException در صورت عدم وجود رستوران یا کاربر
     */
    public Coupon createFixedAmountCoupon(String code, String description, Double amount,
                                         LocalDateTime validFrom, LocalDateTime validUntil,
                                         Long createdBy, Long restaurantId) {
        logger.info("Creating fixed amount coupon: code={}, amount={}", code, amount);
        
        // اعتبارسنجی ورودی‌ها
        validateCouponCreationInputs(code, description, validFrom, validUntil, createdBy);
        validateFixedAmount(amount);
        
        // بررسی یکتا بودن کد
        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Coupon code already exists: " + code);
        }
        
        // اعتبارسنجی مجوزهای ایجادکننده
        validateCreatorPermissions(createdBy, restaurantId);
        
        // ایجاد کوپن مبلغ ثابت
        Coupon coupon = Coupon.createFixedAmountCoupon(code, description, amount, validFrom, validUntil);
        coupon.setCreatedBy(createdBy);
        
        // تنظیم رستوران در صورت مشخص بودن
        if (restaurantId != null) {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
            coupon.setRestaurant(restaurant);
        }
        
        Coupon savedCoupon = couponRepository.save(coupon);
        logger.info("Created fixed amount coupon with ID: {}", savedCoupon.getId());
        return savedCoupon;
    }
    
    /**
     * ایجاد کوپن با تنظیمات پیشرفته
     * 
     * این متد کوپنی با قابلیت‌های پیشرفته مانند:
     * - حداقل مبلغ سفارش
     * - حداکثر مبلغ تخفیف
     * - محدودیت کل استفاده
     * - محدودیت استفاده به ازای کاربر
     * 
     * @param code کد کوپن
     * @param description توضیحات
     * @param type نوع کوپن (درصدی یا مبلغ ثابت)
     * @param value مقدار تخفیف
     * @param validFrom تاریخ شروع
     * @param validUntil تاریخ پایان
     * @param minOrderAmount حداقل مبلغ سفارش (اختیاری)
     * @param maxDiscountAmount حداکثر مبلغ تخفیف (اختیاری)
     * @param usageLimit محدودیت کل استفاده (اختیاری)
     * @param perUserLimit محدودیت استفاده هر کاربر (اختیاری)
     * @param createdBy ایجادکننده
     * @param restaurantId رستوران (اختیاری)
     * @return کوپن ایجاد شده با تنظیمات
     */
    public Coupon createCouponWithSettings(String code, String description, Coupon.CouponType type, Double value,
                                          LocalDateTime validFrom, LocalDateTime validUntil,
                                          Double minOrderAmount, Double maxDiscountAmount,
                                          Integer usageLimit, Integer perUserLimit,
                                          Long createdBy, Long restaurantId) {
        logger.info("Creating coupon with settings: code={}, type={}", code, type);
        
        // ایجاد کوپن پایه بر اساس نوع
        Coupon coupon;
        if (type == Coupon.CouponType.PERCENTAGE) {
            coupon = createPercentageCoupon(code, description, value, validFrom, validUntil, createdBy, restaurantId);
        } else {
            coupon = createFixedAmountCoupon(code, description, value, validFrom, validUntil, createdBy, restaurantId);
        }
        
        // اعمال تنظیمات پیشرفته
        coupon.updateInfo(description, minOrderAmount, maxDiscountAmount, usageLimit, perUserLimit, validUntil);
        
        return couponRepository.update(coupon);
    }
    
    // ==================== COUPON VALIDATION AND APPLICATION ====================
    
    /**
     * اعتبارسنجی و اعمال کوپن به سفارش
     * 
     * این متد کامل‌ترین فرآیند استفاده از کوپن است که شامل:
     * - پیدا کردن کوپن با کد
     * - اعتبارسنجی کامل کوپن
     * - محاسبه مبلغ تخفیف
     * - بازگشت نتیجه اعمال
     * 
     * @param couponCode کد کوپن
     * @param orderAmount مبلغ سفارش
     * @param restaurantId شناسه رستوران
     * @param userId شناسه کاربر
     * @return نتیجه اعمال کوپن (موفق یا ناموفق با دلیل)
     * @throws IllegalArgumentException در صورت ورودی‌های نامعتبر
     */
    public CouponApplicationResult applyCoupon(String couponCode, Double orderAmount, Long restaurantId, Long userId) {
        logger.info("Applying coupon: code={}, orderAmount={}, restaurantId={}, userId={}", 
                   couponCode, orderAmount, restaurantId, userId);
        
        // اعتبارسنجی ورودی‌ها
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Coupon code cannot be empty");
        }
        if (orderAmount == null || orderAmount <= 0) {
            throw new IllegalArgumentException("Order amount must be positive");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        // جستجوی کوپن با کد (تبدیل به حروف بزرگ)
        Optional<Coupon> couponOpt = couponRepository.findByCode(couponCode.trim().toUpperCase());
        if (couponOpt.isEmpty()) {
            return CouponApplicationResult.failed("Coupon code not found");
        }
        
        Coupon coupon = couponOpt.get();
        
        // اعتبارسنجی کوپن برای سفارش
        CouponValidationResult validation = validateCouponForOrder(coupon, orderAmount, restaurantId, userId);
        if (!validation.isValid()) {
            return CouponApplicationResult.failed(validation.getErrorMessage());
        }
        
        // محاسبه مبلغ تخفیف
        Double discountAmount = coupon.calculateDiscount(orderAmount);
        
        return CouponApplicationResult.success(coupon, discountAmount);
    }
    
    /**
     * اعتبارسنجی کوپن برای سفارش خاص بدون اعمال آن
     * 
     * این متد برای بررسی صحت کوپن قبل از اعمال استفاده می‌شود
     * 
     * @param coupon شیء کوپن
     * @param orderAmount مبلغ سفارش
     * @param restaurantId شناسه رستوران
     * @param userId شناسه کاربر
     * @return نتیجه اعتبارسنجی
     */
    public CouponValidationResult validateCouponForOrder(Coupon coupon, Double orderAmount, Long restaurantId, Long userId) {
        // اعتبارسنجی پایه کوپن
        if (!coupon.isValid()) {
            if (!coupon.getIsActive()) {
                return CouponValidationResult.invalid("Coupon is not active");
            }
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(coupon.getValidFrom())) {
                return CouponValidationResult.invalid("Coupon is not yet valid");
            }
            if (now.isAfter(coupon.getValidUntil())) {
                return CouponValidationResult.invalid("Coupon has expired");
            }
            if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
                return CouponValidationResult.invalid("Coupon usage limit exceeded");
            }
        }
        
        // اعتبارسنجی مبلغ سفارش
        if (!coupon.canApplyToOrder(orderAmount)) {
            if (coupon.getMinOrderAmount() != null && orderAmount < coupon.getMinOrderAmount()) {
                return CouponValidationResult.invalid("Minimum order amount not met. Required: " + coupon.getMinOrderAmount());
            }
        }
        
        // اعتبارسنجی رستوران - کوپن باید برای این رستوران معتبر باشد
        if (coupon.getRestaurant() != null && !coupon.getRestaurant().getId().equals(restaurantId)) {
            return CouponValidationResult.invalid("Coupon is not valid for this restaurant");
        }
        
        // اعتبارسنجی محدودیت هر کاربر
        if (coupon.getPerUserLimit() != null) {
            Long userUsageCount = couponUsageRepository.countActiveByCouponIdAndUserId(coupon.getId(), userId);
            if (userUsageCount >= coupon.getPerUserLimit()) {
                return CouponValidationResult.invalid("User has exceeded the per-user limit for this coupon");
            }
        }
        
        return CouponValidationResult.valid();
    }
    
    /**
     * استفاده از کوپن (افزایش شمارنده استفاده)
     * 
     * این متد زمانی فراخوانی می‌شود که کوپن در سفارش اعمال شده است
     * 
     * @param couponId شناسه کوپن
     * @throws NotFoundException در صورت عدم وجود کوپن
     */
    public void useCoupon(Long couponId) {
        logger.info("Using coupon with ID: {}", couponId);
        
        Optional<Coupon> couponOpt = couponRepository.findById(couponId);
        if (couponOpt.isEmpty()) {
            throw new NotFoundException("Coupon", couponId);
        }
        
        Coupon coupon = couponOpt.get();
        coupon.use(); // افزایش شمارنده استفاده
        couponRepository.update(coupon);
        
        logger.info("Used coupon {}, new usage count: {}", couponId, coupon.getUsedCount());
    }
    
    /**
     * استفاده از کوپن با ردیابی کامل (روش توصیه شده)
     * 
     * این متد علاوه بر افزایش شمارنده، رکورد کاملی از استفاده ایجاد می‌کند
     * 
     * @param couponId شناسه کوپن
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param discountAmount مبلغ تخفیف اعمال شده
     * @param orderAmount مبلغ کل سفارش
     * @return رکورد استفاده از کوپن
     */
    public CouponUsage useCouponWithTracking(Long couponId, Long userId, Long orderId, Double discountAmount, Double orderAmount) {
        logger.info("Using coupon with tracking: couponId={}, userId={}, orderId={}", couponId, userId, orderId);
        
        // استفاده از کوپن
        useCoupon(couponId);
        
        // ایجاد رکورد استفاده
        Coupon coupon = getCoupon(couponId);
        CouponUsage usage = new CouponUsage(coupon, userId, orderId, discountAmount, orderAmount);
        
        return couponUsageRepository.save(usage);
    }
    
    /**
     * برگشت استفاده از کوپن (برای لغو سفارش یا بازگردانی)
     * 
     * @param couponId شناسه کوپن
     * @throws NotFoundException در صورت عدم وجود کوپن
     */
    public void revertCouponUsage(Long couponId) {
        logger.info("Reverting coupon usage for ID: {}", couponId);
        
        Optional<Coupon> couponOpt = couponRepository.findById(couponId);
        if (couponOpt.isEmpty()) {
            throw new NotFoundException("Coupon", couponId);
        }
        
        Coupon coupon = couponOpt.get();
        coupon.revertUsage(); // کاهش شمارنده استفاده
        couponRepository.update(coupon);
        
        logger.info("Reverted coupon usage {}, new usage count: {}", couponId, coupon.getUsedCount());
    }
    
    // ==================== COUPON MANAGEMENT ====================
    
    /**
     * دریافت کوپن بر اساس شناسه
     * 
     * @param couponId شناسه کوپن
     * @return کوپن یافت شده
     * @throws IllegalArgumentException در صورت null بودن ID
     * @throws NotFoundException در صورت عدم وجود کوپن
     */
    public Coupon getCoupon(Long couponId) {
        if (couponId == null) {
            throw new IllegalArgumentException("Coupon ID cannot be null");
        }
        
        return couponRepository.findById(couponId)
            .orElseThrow(() -> new NotFoundException("Coupon", couponId));
    }
    
    /**
     * دریافت کوپن بر اساس کد
     * 
     * کد به حروف بزرگ تبدیل می‌شود برای یکنواختی
     * 
     * @param code کد کوپن
     * @return کوپن یافت شده
     * @throws IllegalArgumentException در صورت خالی بودن کد
     * @throws NotFoundException در صورت عدم وجود کوپن
     */
    public Coupon getCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Coupon code cannot be empty");
        }
        
        return couponRepository.findByCode(code.trim().toUpperCase())
            .orElseThrow(() -> new NotFoundException("Coupon with code", code));
    }
    
    /**
     * دریافت تمام کوپن‌های معتبر
     * 
     * @return لیست کوپن‌های معتبر
     */
    public List<Coupon> getValidCoupons() {
        return couponRepository.findValidCoupons();
    }
    
    /**
     * دریافت کوپن‌های قابل اعمال برای سفارش خاص
     * 
     * @param orderAmount مبلغ سفارش
     * @param restaurantId شناسه رستوران
     * @return لیست کوپن‌های قابل اعمال
     * @throws IllegalArgumentException در صورت مبلغ منفی یا صفر
     */
    public List<Coupon> getApplicableCoupons(Double orderAmount, Long restaurantId) {
        if (orderAmount == null || orderAmount <= 0) {
            throw new IllegalArgumentException("Order amount must be positive");
        }
        
        return couponRepository.findApplicableCoupons(orderAmount, restaurantId);
    }
    
    /**
     * دریافت کوپن‌های رستوران خاص
     * 
     * @param restaurantId شناسه رستوران (null برای کوپن‌های سراسری)
     * @return لیست کوپن‌های رستوران یا سراسری
     */
    public List<Coupon> getRestaurantCoupons(Long restaurantId) {
        return couponRepository.findByRestaurantId(restaurantId);
    }
    
    /**
     * دریافت کوپن‌های سراسری
     * 
     * @return لیست کوپن‌های قابل استفاده در تمام رستوران‌ها
     */
    public List<Coupon> getGlobalCoupons() {
        return couponRepository.findGlobalCoupons();
    }
    
    /**
     * دریافت کوپن‌های ایجاد شده توسط کاربر
     * 
     * @param createdBy شناسه کاربر ایجادکننده
     * @return لیست کوپن‌های ایجاد شده توسط کاربر
     * @throws IllegalArgumentException در صورت null بودن شناسه
     */
    public List<Coupon> getCouponsByCreator(Long createdBy) {
        if (createdBy == null) {
            throw new IllegalArgumentException("Creator ID cannot be null");
        }
        
        return couponRepository.findByCreatedBy(createdBy);
    }
    
    /**
     * دریافت کوپن‌های نزدیک به انقضا
     * 
     * @param days تعداد روزهای آینده
     * @return لیست کوپن‌های نزدیک به انقضا
     * @throws IllegalArgumentException در صورت تعداد روز منفی
     */
    public List<Coupon> getCouponsExpiringSoon(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Days must be positive");
        }
        
        return couponRepository.findCouponsExpiringSoon(days);
    }
    
    /**
     * به‌روزرسانی اطلاعات کوپن
     * 
     * @param couponId شناسه کوپن
     * @param description توضیحات جدید
     * @param minOrderAmount حداقل مبلغ سفارش
     * @param maxDiscountAmount حداکثر مبلغ تخفیف
     * @param usageLimit محدودیت کل استفاده
     * @param perUserLimit محدودیت هر کاربر
     * @param validUntil تاریخ انقضا
     * @param updatedBy شناسه به‌روزرسانی کننده
     * @return کوپن به‌روزرسانی شده
     */
    public Coupon updateCoupon(Long couponId, String description, Double minOrderAmount, 
                              Double maxDiscountAmount, Integer usageLimit, Integer perUserLimit, 
                              LocalDateTime validUntil, Long updatedBy) {
        logger.info("Updating coupon with ID: {}", couponId);
        
        Coupon coupon = getCoupon(couponId);
        
        // اعتبارسنجی مجوزهای به‌روزرسانی
        validateUpdatePermissions(coupon, updatedBy);
        
        // به‌روزرسانی اطلاعات
        coupon.updateInfo(description, minOrderAmount, maxDiscountAmount, usageLimit, perUserLimit, validUntil);
        
        return couponRepository.update(coupon);
    }
    
    /**
     * فعال‌سازی کوپن
     * 
     * @param couponId شناسه کوپن
     * @param activatedBy شناسه فعال‌کننده
     */
    public void activateCoupon(Long couponId, Long activatedBy) {
        logger.info("Activating coupon with ID: {}", couponId);
        
        Coupon coupon = getCoupon(couponId);
        validateUpdatePermissions(coupon, activatedBy);
        
        coupon.activate();
        couponRepository.update(coupon);
    }
    
    /**
     * غیرفعال‌سازی کوپن
     * 
     * @param couponId شناسه کوپن
     * @param deactivatedBy شناسه غیرفعال‌کننده
     */
    public void deactivateCoupon(Long couponId, Long deactivatedBy) {
        logger.info("Deactivating coupon with ID: {}", couponId);
        
        Coupon coupon = getCoupon(couponId);
        validateUpdatePermissions(coupon, deactivatedBy);
        
        coupon.deactivate();
        couponRepository.update(coupon);
    }
    
    /**
     * حذف کوپن
     * 
     * کوپن‌هایی که استفاده شده‌اند قابل حذف نیستند
     * 
     * @param couponId شناسه کوپن
     * @param deletedBy شناسه حذف‌کننده
     * @throws IllegalStateException در صورت استفاده شدن کوپن
     */
    public void deleteCoupon(Long couponId, Long deletedBy) {
        logger.info("Deleting coupon with ID: {}", couponId);
        
        Coupon coupon = getCoupon(couponId);
        validateUpdatePermissions(coupon, deletedBy);
        
        // بررسی استفاده نشدن کوپن
        if (coupon.getUsedCount() > 0) {
            throw new IllegalStateException("Cannot delete coupon that has been used");
        }
        
        couponRepository.delete(couponId);
    }
    
    /**
     * دریافت آمار کوپن‌ها
     * 
     * @return آمار شامل کل، فعال، نزدیک به انقضا و منقضی
     */
    public CouponStatistics getCouponStatistics() {
        Long totalCoupons = couponRepository.countAll();
        Long activeCoupons = couponRepository.countActive();
        List<Coupon> expiringSoon = couponRepository.findCouponsExpiringSoon(7);
        List<Coupon> expired = couponRepository.findExpiredCoupons();
        
        return new CouponStatistics(totalCoupons, activeCoupons, (long)expiringSoon.size(), (long)expired.size());
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * اعتبارسنجی ورودی‌های ایجاد کوپن
     * 
     * بررسی تمام قوانین validation برای ایجاد کوپن
     */
    private void validateCouponCreationInputs(String code, String description, LocalDateTime validFrom, 
                                            LocalDateTime validUntil, Long createdBy) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Coupon code cannot be empty");
        }
        if (code.trim().length() > 50) {
            throw new IllegalArgumentException("Coupon code cannot exceed 50 characters");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Coupon description cannot be empty");
        }
        if (description.trim().length() > 255) {
            throw new IllegalArgumentException("Coupon description cannot exceed 255 characters");
        }
        if (validFrom == null) {
            throw new IllegalArgumentException("Valid from date cannot be null");
        }
        if (validUntil == null) {
            throw new IllegalArgumentException("Valid until date cannot be null");
        }
        if (validUntil.isBefore(validFrom)) {
            throw new IllegalArgumentException("Valid until date must be after valid from date");
        }
        if (validUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Valid until date must be in the future");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Creator ID cannot be null");
        }
    }
    
    /**
     * اعتبارسنجی درصد تخفیف
     * 
     * درصد باید بین 0 تا 100 باشد
     */
    private void validatePercentage(Double percentage) {
        if (percentage == null || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
    }
    
    /**
     * اعتبارسنجی مبلغ ثابت تخفیف
     * 
     * مبلغ باید مثبت و کمتر از 10,000 باشد
     */
    private void validateFixedAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Fixed amount must be positive");
        }
        if (amount > 10000) {
            throw new IllegalArgumentException("Fixed amount cannot exceed 10,000");
        }
    }
    
    /**
     * اعتبارسنجی مجوزهای ایجاد کوپن
     * 
     * قوانین:
     * - ادمین‌ها می‌توانند هر نوع کوپنی ایجاد کنند
     * - مالکان رستوران فقط برای رستوران خودشان
     * - سایر کاربران اجازه ایجاد ندارند
     */
    private void validateCreatorPermissions(Long createdBy, Long restaurantId) {
        User creator = authRepository.findById(createdBy)
            .orElseThrow(() -> new NotFoundException("User", createdBy));
        
        // ادمین‌ها می‌توانند هر کوپنی ایجاد کنند
        if (creator.getRole() == User.Role.ADMIN) {
            return;
        }
        
        // مالکان رستوران (نقش SELLER) فقط برای رستوران خودشان
        if (creator.getRole() == User.Role.SELLER) {
            if (restaurantId == null) {
                throw new IllegalArgumentException("Restaurant owners cannot create global coupons");
            }
            
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
            
            if (!restaurant.getOwnerId().equals(createdBy)) {
                throw new IllegalArgumentException("Restaurant owners can only create coupons for their own restaurants");
            }
            return;
        }
        
        throw new IllegalArgumentException("Only admins and restaurant owners can create coupons");
    }
    
    /**
     * اعتبارسنجی مجوزهای به‌روزرسانی کوپن
     * 
     * قوانین:
     * - ادمین‌ها می‌توانند همه کوپن‌ها را به‌روزرسانی کنند
     * - کاربران فقط کوپن‌های خودشان را
     * - مالکان رستوران فقط کوپن‌های رستوران خودشان را
     */
    private void validateUpdatePermissions(Coupon coupon, Long updatedBy) {
        User updater = authRepository.findById(updatedBy)
            .orElseThrow(() -> new NotFoundException("User", updatedBy));
        
        // ادمین‌ها می‌توانند هر کوپنی را به‌روزرسانی کنند
        if (updater.getRole() == User.Role.ADMIN) {
            return;
        }
        
        // کاربران فقط کوپن‌های خودشان را
        if (!coupon.getCreatedBy().equals(updatedBy)) {
            throw new IllegalArgumentException("Users can only update their own coupons");
        }
        
        // مالکان رستوران فقط کوپن‌های رستوران خودشان را
        if (updater.getRole() == User.Role.SELLER && coupon.getRestaurant() != null) {
            if (!coupon.getRestaurant().getOwnerId().equals(updatedBy)) {
                throw new IllegalArgumentException("Restaurant owners can only update their own restaurant coupons");
            }
        }
    }
    
    // ==================== RESULT CLASSES ====================
    
    /**
     * کلاس نتیجه اعمال کوپن
     * 
     * این کلاس نتیجه فرآیند اعمال کوپن را در بر می‌گیرد:
     * - موفقیت/ناموفقی بودن
     * - پیام خطا در صورت ناموفقی بودن
     * - شیء کوپن در صورت موفقیت
     * - مبلغ تخفیف محاسبه شده
     */
    public static class CouponApplicationResult {
        /** آیا اعمال کوپن موفق بوده است */
        private final boolean success;
        /** پیام خطا در صورت ناموفقی بودن */
        private final String errorMessage;
        /** شیء کوپن در صورت موفقیت */
        private final Coupon coupon;
        /** مبلغ تخفیف محاسبه شده */
        private final Double discountAmount;
        
        private CouponApplicationResult(boolean success, String errorMessage, Coupon coupon, Double discountAmount) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.coupon = coupon;
            this.discountAmount = discountAmount;
        }
        
        /**
         * ایجاد نتیجه موفق
         */
        public static CouponApplicationResult success(Coupon coupon, Double discountAmount) {
            return new CouponApplicationResult(true, null, coupon, discountAmount);
        }
        
        /**
         * ایجاد نتیجه ناموفق با پیام خطا
         */
        public static CouponApplicationResult failed(String errorMessage) {
            return new CouponApplicationResult(false, errorMessage, null, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public Coupon getCoupon() { return coupon; }
        public Double getDiscountAmount() { return discountAmount; }
    }
    
    /**
     * کلاس نتیجه اعتبارسنجی کوپن
     * 
     * برای بررسی معتبر بودن کوپن بدون اعمال آن
     */
    public static class CouponValidationResult {
        /** آیا کوپن معتبر است */
        private final boolean valid;
        /** پیام خطا در صورت نامعتبر بودن */
        private final String errorMessage;
        
        private CouponValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        /**
         * ایجاد نتیجه معتبر
         */
        public static CouponValidationResult valid() {
            return new CouponValidationResult(true, null);
        }
        
        /**
         * ایجاد نتیجه نامعتبر با پیام خطا
         */
        public static CouponValidationResult invalid(String errorMessage) {
            return new CouponValidationResult(false, errorMessage);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * کلاس آمار کوپن‌ها
     * 
     * شامل آمارهای کلی سیستم کوپن‌ها برای dashboard
     */
    public static class CouponStatistics {
        /** تعداد کل کوپن‌ها */
        private final Long totalCoupons;
        /** تعداد کوپن‌های فعال */
        private final Long activeCoupons;
        /** تعداد کوپن‌های نزدیک به انقضا */
        private final Long expiringSoon;
        /** تعداد کوپن‌های منقضی */
        private final Long expired;
        
        public CouponStatistics(Long totalCoupons, Long activeCoupons, Long expiringSoon, Long expired) {
            this.totalCoupons = totalCoupons;
            this.activeCoupons = activeCoupons;
            this.expiringSoon = expiringSoon;
            this.expired = expired;
        }
        
        // Getters
        public Long getTotalCoupons() { return totalCoupons; }
        public Long getActiveCoupons() { return activeCoupons; }
        public Long getExpiringSoon() { return expiringSoon; }
        public Long getExpired() { return expired; }
        /** محاسبه تعداد کوپن‌های غیرفعال */
        public Long getInactiveCoupons() { return totalCoupons - activeCoupons; }
    }
}
