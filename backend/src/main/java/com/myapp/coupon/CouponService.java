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
 * Service class for Coupon business logic
 * Handles coupon creation, validation, application, and management
 */
public class CouponService {
    
    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);
    
    private final CouponRepository couponRepository;
    private final AuthRepository authRepository;
    private final RestaurantRepository restaurantRepository;
    private final CouponUsageRepository couponUsageRepository;
    
    public CouponService() {
        this.couponRepository = new CouponRepository();
        this.authRepository = new AuthRepository();
        this.restaurantRepository = new RestaurantRepository();
        this.couponUsageRepository = new CouponUsageRepository();
    }
    
    // Constructor for dependency injection (testing)
    public CouponService(CouponRepository couponRepository, AuthRepository authRepository, 
                        RestaurantRepository restaurantRepository, CouponUsageRepository couponUsageRepository) {
        this.couponRepository = couponRepository;
        this.authRepository = authRepository;
        this.restaurantRepository = restaurantRepository;
        this.couponUsageRepository = couponUsageRepository;
    }
    
    // ==================== COUPON CREATION ====================
    
    /**
     * Creates a new percentage-based coupon
     */
    public Coupon createPercentageCoupon(String code, String description, Double percentage,
                                        LocalDateTime validFrom, LocalDateTime validUntil,
                                        Long createdBy, Long restaurantId) {
        logger.info("Creating percentage coupon: code={}, percentage={}", code, percentage);
        
        // Validate inputs
        validateCouponCreationInputs(code, description, validFrom, validUntil, createdBy);
        validatePercentage(percentage);
        
        // Check if code already exists
        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Coupon code already exists: " + code);
        }
        
        // Validate creator permissions
        validateCreatorPermissions(createdBy, restaurantId);
        
        // Create coupon
        Coupon coupon = Coupon.createPercentageCoupon(code, description, percentage, validFrom, validUntil);
        coupon.setCreatedBy(createdBy);
        
        // Set restaurant if specified
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
     * Creates a new fixed amount coupon
     */
    public Coupon createFixedAmountCoupon(String code, String description, Double amount,
                                         LocalDateTime validFrom, LocalDateTime validUntil,
                                         Long createdBy, Long restaurantId) {
        logger.info("Creating fixed amount coupon: code={}, amount={}", code, amount);
        
        // Validate inputs
        validateCouponCreationInputs(code, description, validFrom, validUntil, createdBy);
        validateFixedAmount(amount);
        
        // Check if code already exists
        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Coupon code already exists: " + code);
        }
        
        // Validate creator permissions
        validateCreatorPermissions(createdBy, restaurantId);
        
        // Create coupon
        Coupon coupon = Coupon.createFixedAmountCoupon(code, description, amount, validFrom, validUntil);
        coupon.setCreatedBy(createdBy);
        
        // Set restaurant if specified
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
     * Creates a coupon with additional settings
     */
    public Coupon createCouponWithSettings(String code, String description, Coupon.CouponType type, Double value,
                                          LocalDateTime validFrom, LocalDateTime validUntil,
                                          Double minOrderAmount, Double maxDiscountAmount,
                                          Integer usageLimit, Integer perUserLimit,
                                          Long createdBy, Long restaurantId) {
        logger.info("Creating coupon with settings: code={}, type={}", code, type);
        
        // Create basic coupon
        Coupon coupon;
        if (type == Coupon.CouponType.PERCENTAGE) {
            coupon = createPercentageCoupon(code, description, value, validFrom, validUntil, createdBy, restaurantId);
        } else {
            coupon = createFixedAmountCoupon(code, description, value, validFrom, validUntil, createdBy, restaurantId);
        }
        
        // Apply additional settings
        coupon.updateInfo(description, minOrderAmount, maxDiscountAmount, usageLimit, perUserLimit, validUntil);
        
        return couponRepository.update(coupon);
    }
    
    // ==================== COUPON VALIDATION AND APPLICATION ====================
    
    /**
     * Validates and applies a coupon to an order
     */
    public CouponApplicationResult applyCoupon(String couponCode, Double orderAmount, Long restaurantId, Long userId) {
        logger.info("Applying coupon: code={}, orderAmount={}, restaurantId={}, userId={}", 
                   couponCode, orderAmount, restaurantId, userId);
        
        // Validate inputs
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Coupon code cannot be empty");
        }
        if (orderAmount == null || orderAmount <= 0) {
            throw new IllegalArgumentException("Order amount must be positive");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        // Find coupon
        Optional<Coupon> couponOpt = couponRepository.findByCode(couponCode.trim().toUpperCase());
        if (couponOpt.isEmpty()) {
            return CouponApplicationResult.failed("Coupon code not found");
        }
        
        Coupon coupon = couponOpt.get();
        
        // Validate coupon
        CouponValidationResult validation = validateCouponForOrder(coupon, orderAmount, restaurantId, userId);
        if (!validation.isValid()) {
            return CouponApplicationResult.failed(validation.getErrorMessage());
        }
        
        // Calculate discount
        Double discountAmount = coupon.calculateDiscount(orderAmount);
        
        return CouponApplicationResult.success(coupon, discountAmount);
    }
    
    /**
     * Validates a coupon for a specific order without applying it
     */
    public CouponValidationResult validateCouponForOrder(Coupon coupon, Double orderAmount, Long restaurantId, Long userId) {
        // Basic validation
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
        
        // Order amount validation
        if (!coupon.canApplyToOrder(orderAmount)) {
            if (coupon.getMinOrderAmount() != null && orderAmount < coupon.getMinOrderAmount()) {
                return CouponValidationResult.invalid("Minimum order amount not met. Required: " + coupon.getMinOrderAmount());
            }
        }
        
        // Restaurant validation
        if (coupon.getRestaurant() != null && !coupon.getRestaurant().getId().equals(restaurantId)) {
            return CouponValidationResult.invalid("Coupon is not valid for this restaurant");
        }
        
        // Per-user limit validation
        if (coupon.getPerUserLimit() != null) {
            Long userUsageCount = couponUsageRepository.countActiveByCouponIdAndUserId(coupon.getId(), userId);
            if (userUsageCount >= coupon.getPerUserLimit()) {
                return CouponValidationResult.invalid("User has exceeded the per-user limit for this coupon");
            }
        }
        
        return CouponValidationResult.valid();
    }
    
    /**
     * Uses a coupon (increments usage count)
     */
    public void useCoupon(Long couponId) {
        logger.info("Using coupon with ID: {}", couponId);
        
        Optional<Coupon> couponOpt = couponRepository.findById(couponId);
        if (couponOpt.isEmpty()) {
            throw new NotFoundException("Coupon", couponId);
        }
        
        Coupon coupon = couponOpt.get();
        coupon.use();
        couponRepository.update(coupon);
        
        logger.info("Used coupon {}, new usage count: {}", couponId, coupon.getUsedCount());
    }
    
    /**
     * Uses a coupon with full tracking (recommended method)
     */
    public CouponUsage useCouponWithTracking(Long couponId, Long userId, Long orderId, Double discountAmount, Double orderAmount) {
        logger.info("Using coupon with tracking: couponId={}, userId={}, orderId={}", couponId, userId, orderId);
        
        // Use the coupon
        useCoupon(couponId);
        
        // Create usage record
        Coupon coupon = getCoupon(couponId);
        CouponUsage usage = new CouponUsage(coupon, userId, orderId, discountAmount, orderAmount);
        
        return couponUsageRepository.save(usage);
    }
    
    /**
     * Reverts coupon usage (for order cancellations/refunds)
     */
    public void revertCouponUsage(Long couponId) {
        logger.info("Reverting coupon usage for ID: {}", couponId);
        
        Optional<Coupon> couponOpt = couponRepository.findById(couponId);
        if (couponOpt.isEmpty()) {
            throw new NotFoundException("Coupon", couponId);
        }
        
        Coupon coupon = couponOpt.get();
        coupon.revertUsage();
        couponRepository.update(coupon);
        
        logger.info("Reverted coupon usage {}, new usage count: {}", couponId, coupon.getUsedCount());
    }
    
    // ==================== COUPON MANAGEMENT ====================
    
    /**
     * Gets coupon by ID
     */
    public Coupon getCoupon(Long couponId) {
        if (couponId == null) {
            throw new IllegalArgumentException("Coupon ID cannot be null");
        }
        
        return couponRepository.findById(couponId)
            .orElseThrow(() -> new NotFoundException("Coupon", couponId));
    }
    
    /**
     * Gets coupon by code
     */
    public Coupon getCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Coupon code cannot be empty");
        }
        
        return couponRepository.findByCode(code.trim().toUpperCase())
            .orElseThrow(() -> new NotFoundException("Coupon with code", code));
    }
    
    /**
     * Gets all valid coupons
     */
    public List<Coupon> getValidCoupons() {
        return couponRepository.findValidCoupons();
    }
    
    /**
     * Gets coupons applicable for a specific order
     */
    public List<Coupon> getApplicableCoupons(Double orderAmount, Long restaurantId) {
        if (orderAmount == null || orderAmount <= 0) {
            throw new IllegalArgumentException("Order amount must be positive");
        }
        
        return couponRepository.findApplicableCoupons(orderAmount, restaurantId);
    }
    
    /**
     * Gets coupons by restaurant (null for global coupons)
     */
    public List<Coupon> getRestaurantCoupons(Long restaurantId) {
        return couponRepository.findByRestaurantId(restaurantId);
    }
    
    /**
     * Gets global coupons (applicable to all restaurants)
     */
    public List<Coupon> getGlobalCoupons() {
        return couponRepository.findGlobalCoupons();
    }
    
    /**
     * Gets coupons created by a user
     */
    public List<Coupon> getCouponsByCreator(Long createdBy) {
        if (createdBy == null) {
            throw new IllegalArgumentException("Creator ID cannot be null");
        }
        
        return couponRepository.findByCreatedBy(createdBy);
    }
    
    /**
     * Gets coupons expiring soon
     */
    public List<Coupon> getCouponsExpiringSoon(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Days must be positive");
        }
        
        return couponRepository.findCouponsExpiringSoon(days);
    }
    
    /**
     * Updates coupon information
     */
    public Coupon updateCoupon(Long couponId, String description, Double minOrderAmount, 
                              Double maxDiscountAmount, Integer usageLimit, Integer perUserLimit, 
                              LocalDateTime validUntil, Long updatedBy) {
        logger.info("Updating coupon with ID: {}", couponId);
        
        Coupon coupon = getCoupon(couponId);
        
        // Validate permissions
        validateUpdatePermissions(coupon, updatedBy);
        
        // Update information
        coupon.updateInfo(description, minOrderAmount, maxDiscountAmount, usageLimit, perUserLimit, validUntil);
        
        return couponRepository.update(coupon);
    }
    
    /**
     * Activates a coupon
     */
    public void activateCoupon(Long couponId, Long activatedBy) {
        logger.info("Activating coupon with ID: {}", couponId);
        
        Coupon coupon = getCoupon(couponId);
        validateUpdatePermissions(coupon, activatedBy);
        
        coupon.activate();
        couponRepository.update(coupon);
    }
    
    /**
     * Deactivates a coupon
     */
    public void deactivateCoupon(Long couponId, Long deactivatedBy) {
        logger.info("Deactivating coupon with ID: {}", couponId);
        
        Coupon coupon = getCoupon(couponId);
        validateUpdatePermissions(coupon, deactivatedBy);
        
        coupon.deactivate();
        couponRepository.update(coupon);
    }
    
    /**
     * Deletes a coupon
     */
    public void deleteCoupon(Long couponId, Long deletedBy) {
        logger.info("Deleting coupon with ID: {}", couponId);
        
        Coupon coupon = getCoupon(couponId);
        validateUpdatePermissions(coupon, deletedBy);
        
        // Check if coupon has been used
        if (coupon.getUsedCount() > 0) {
            throw new IllegalStateException("Cannot delete coupon that has been used");
        }
        
        couponRepository.delete(couponId);
    }
    
    /**
     * Gets coupon statistics
     */
    public CouponStatistics getCouponStatistics() {
        Long totalCoupons = couponRepository.countAll();
        Long activeCoupons = couponRepository.countActive();
        List<Coupon> expiringSoon = couponRepository.findCouponsExpiringSoon(7);
        List<Coupon> expired = couponRepository.findExpiredCoupons();
        
        return new CouponStatistics(totalCoupons, activeCoupons, (long)expiringSoon.size(), (long)expired.size());
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
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
    
    private void validatePercentage(Double percentage) {
        if (percentage == null || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
    }
    
    private void validateFixedAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Fixed amount must be positive");
        }
        if (amount > 10000) {
            throw new IllegalArgumentException("Fixed amount cannot exceed 10,000");
        }
    }
    
    private void validateCreatorPermissions(Long createdBy, Long restaurantId) {
        User creator = authRepository.findById(createdBy)
            .orElseThrow(() -> new NotFoundException("User", createdBy));
        
        // Admins can create any coupon
        if (creator.getRole() == User.Role.ADMIN) {
            return;
        }
        
        // Restaurant owners (SELLER role) can only create coupons for their own restaurants
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
    
    private void validateUpdatePermissions(Coupon coupon, Long updatedBy) {
        User updater = authRepository.findById(updatedBy)
            .orElseThrow(() -> new NotFoundException("User", updatedBy));
        
        // Admins can update any coupon
        if (updater.getRole() == User.Role.ADMIN) {
            return;
        }
        
        // Users can only update their own coupons
        if (!coupon.getCreatedBy().equals(updatedBy)) {
            throw new IllegalArgumentException("Users can only update their own coupons");
        }
        
        // Restaurant owners (SELLER role) can only update their restaurant coupons
        if (updater.getRole() == User.Role.SELLER && coupon.getRestaurant() != null) {
            if (!coupon.getRestaurant().getOwnerId().equals(updatedBy)) {
                throw new IllegalArgumentException("Restaurant owners can only update their own restaurant coupons");
            }
        }
    }
    
    // ==================== RESULT CLASSES ====================
    
    /**
     * Result of coupon application
     */
    public static class CouponApplicationResult {
        private final boolean success;
        private final String errorMessage;
        private final Coupon coupon;
        private final Double discountAmount;
        
        private CouponApplicationResult(boolean success, String errorMessage, Coupon coupon, Double discountAmount) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.coupon = coupon;
            this.discountAmount = discountAmount;
        }
        
        public static CouponApplicationResult success(Coupon coupon, Double discountAmount) {
            return new CouponApplicationResult(true, null, coupon, discountAmount);
        }
        
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
     * Result of coupon validation
     */
    public static class CouponValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private CouponValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static CouponValidationResult valid() {
            return new CouponValidationResult(true, null);
        }
        
        public static CouponValidationResult invalid(String errorMessage) {
            return new CouponValidationResult(false, errorMessage);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Coupon statistics
     */
    public static class CouponStatistics {
        private final Long totalCoupons;
        private final Long activeCoupons;
        private final Long expiringSoon;
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
        public Long getInactiveCoupons() { return totalCoupons - activeCoupons; }
    }
}