package com.myapp.vendor;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.restaurant.RestaurantRepository;
import com.myapp.item.ItemRepository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Service لایه منطق کسب‌وکار برای عملیات فروشندگان از دیدگاه مشتری
 * 
 * این کلاس تمام منطق کسب‌وکار مربوط به فروشندگان (رستوران‌ها) از دیدگاه مشتری را پیاده‌سازی می‌کند:
 * 
 * === مرور و جستجوی فروشندگان ===
 * - getAllVendors(): دریافت تمام فروشندگان فعال
 * - searchVendors(): جستجوی فروشندگان بر اساس نام یا موقعیت
 * - getVendor(): دریافت جزئیات کامل یک فروشنده
 * - getVendorsByLocation(): فروشندگان بر اساس موقعیت
 * - getFeaturedVendors(): فروشندگان برجسته
 * - getVendorsByCategory(): فروشندگان بر اساس دسته غذا
 * 
 * === مدیریت منوی فروشندگان ===
 * - getVendorMenu(): دریافت منوی کامل فروشنده با دسته‌بندی
 * - getVendorStats(): آمار فروشنده (تعداد آیتم‌ها، دسته‌ها)
 * - isVendorAcceptingOrders(): بررسی پذیرش سفارش
 * 
 * === ویژگی‌های کلیدی ===
 * - Customer-Perspective Logic: منطق مختص دیدگاه مشتری
 * - Status Filtering: فیلتر وضعیت (فقط تایید شده‌ها)
 * - Menu Organization: سازماندهی منو بر اساس دسته‌ها
 * - Availability Checking: بررسی در دسترس بودن
 * - Statistics Generation: تولید آمار فروشندگان
 * - Error Handling: مدیریت خطاها
 * - Data Validation: اعتبارسنجی ورودی‌ها
 * - Business Rules: اجرای قوانین کسب‌وکار
 * 
 * === Inner Classes ===
 * - VendorStats: کلاس آمار فروشندگان
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class VendorService {
    
    /** Repository عملیات فروشندگان */
    private final VendorRepository vendorRepository;
    /** Repository رستوران‌ها */
    private final RestaurantRepository restaurantRepository;
    /** Repository آیتم‌های غذایی */
    private final ItemRepository itemRepository;
    
    /**
     * سازنده پیش‌فرض با ایجاد repositories
     */
    public VendorService() {
        this.vendorRepository = new VendorRepository();
        this.restaurantRepository = new RestaurantRepository();
        this.itemRepository = new ItemRepository();
    }
    
    /**
     * سازنده با تزریق وابستگی‌ها (برای تست)
     * 
     * @param vendorRepository repository فروشندگان
     * @param restaurantRepository repository رستوران‌ها
     * @param itemRepository repository آیتم‌ها
     */
    public VendorService(VendorRepository vendorRepository, RestaurantRepository restaurantRepository, ItemRepository itemRepository) {
        this.vendorRepository = vendorRepository;
        this.restaurantRepository = restaurantRepository;
        this.itemRepository = itemRepository;
    }
    
    /**
     * دریافت تمام فروشندگان فعال (رستوران‌های تایید شده)
     * 
     * فقط رستوران‌هایی که وضعیت APPROVED دارند را برمی‌گرداند
     * 
     * @return لیست فروشندگان فعال
     */
    public List<Restaurant> getAllVendors() {
        return restaurantRepository.findByStatus(RestaurantStatus.APPROVED);
    }
    
    /**
     * جستجوی فروشندگان بر اساس نام یا موقعیت
     * 
     * اگر عبارت جستجو خالی باشد، تمام فروشندگان را برمی‌گرداند
     * 
     * @param searchTerm عبارت جستجو
     * @return لیست فروشندگان یافت شده
     */
    public List<Restaurant> searchVendors(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllVendors();
        }
        
        return vendorRepository.searchVendors(searchTerm.trim());
    }
    
    /**
     * دریافت فروشنده با شناسه به همراه جزئیات کامل
     * 
     * فقط فروشندگان تایید شده در دسترس مشتریان هستند
     * 
     * @param vendorId شناسه فروشنده
     * @return اطلاعات کامل فروشنده
     * @throws IllegalArgumentException در صورت شناسه نامعتبر
     * @throws NotFoundException در صورت عدم وجود یا عدم تایید فروشنده
     */
    public Restaurant getVendor(Long vendorId) {
        if (vendorId == null || vendorId <= 0) {
            throw new IllegalArgumentException("Vendor ID must be positive");
        }
        
        Restaurant vendor = restaurantRepository.findById(vendorId)
            .orElseThrow(() -> new NotFoundException("Vendor", vendorId));
        
        // فقط فروشندگان تایید شده را به مشتریان نشان می‌دهیم
        if (vendor.getStatus() != RestaurantStatus.APPROVED) {
            throw new NotFoundException("Vendor", vendorId);
        }
        
        return vendor;
    }
    
    /**
     * دریافت منوی فروشنده سازماندهی شده بر اساس دسته‌ها
     * 
     * منو را بر اساس دسته‌های غذایی گروه‌بندی می‌کند و
     * ساختار پاسخ را مطابق OpenAPI spec می‌سازد
     * 
     * @param vendorId شناسه فروشنده
     * @return Map حاوی اطلاعات فروشنده و منوی دسته‌بندی شده
     * @throws NotFoundException در صورت عدم وجود فروشنده
     */
    public Map<String, Object> getVendorMenu(Long vendorId) {
        Restaurant vendor = getVendor(vendorId);
        
        // دریافت آیتم‌های منوی موجود
        List<FoodItem> menuItems = itemRepository.findAvailableByRestaurant(vendorId);
        
        // گروه‌بندی آیتم‌ها بر اساس دسته
        Map<String, List<FoodItem>> itemsByCategory = menuItems.stream()
            .collect(Collectors.groupingBy(FoodItem::getCategory));
        
        // ساخت ساختار پاسخ مطابق OpenAPI spec
        Map<String, Object> response = new HashMap<>();
        response.put("vendor", vendor);
        response.put("menu_titles", itemsByCategory.keySet().stream().collect(Collectors.toList()));
        
        // اضافه کردن آیتم‌های هر دسته
        for (Map.Entry<String, List<FoodItem>> entry : itemsByCategory.entrySet()) {
            response.put(entry.getKey(), entry.getValue());
        }
        
        return response;
    }
    
    /**
     * دریافت فروشندگان بر اساس موقعیت/منطقه
     * 
     * @param location نام منطقه یا موقعیت
     * @return لیست فروشندگان در آن منطقه
     * @throws IllegalArgumentException در صورت موقعیت خالی
     */
    public List<Restaurant> getVendorsByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be empty");
        }
        
        return vendorRepository.findByLocation(location.trim());
    }
    
    /**
     * دریافت فروشندگان برجسته/محبوب
     * 
     * @return لیست فروشندگان برجسته
     */
    public List<Restaurant> getFeaturedVendors() {
        return vendorRepository.getFeaturedVendors();
    }
    
    /**
     * دریافت فروشندگان بر اساس دسته غذایی خاص
     * 
     * @param category نام دسته غذایی
     * @return لیست فروشندگان ارائه‌دهنده آن دسته
     * @throws IllegalArgumentException در صورت دسته خالی
     */
    public List<Restaurant> getVendorsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        
        return vendorRepository.findByFoodCategory(category.trim());
    }
    
    /**
     * دریافت آمار فروشنده برای نمایش
     * 
     * شامل تعداد آیتم‌ها، دسته‌ها و سایر آمار مفید
     * 
     * @param vendorId شناسه فروشنده
     * @return آمار کامل فروشنده
     * @throws NotFoundException در صورت عدم وجود فروشنده
     */
    public VendorStats getVendorStats(Long vendorId) {
        Restaurant vendor = getVendor(vendorId);
        
        // محاسبه آمار
        int totalItems = itemRepository.countByRestaurant(vendorId);
        int availableItems = itemRepository.countAvailableByRestaurant(vendorId);
        List<String> categories = itemRepository.getCategoriesByRestaurant(vendorId);
        
        return new VendorStats(
            vendorId,
            vendor.getName(),
            totalItems,
            availableItems,
            categories.size(),
            categories
        );
    }
    
    /**
     * بررسی اینکه آیا فروشنده در حال حاضر سفارش می‌پذیرد یا خیر
     * 
     * فروشنده در صورتی سفارش می‌پذیرد که:
     * - وجود داشته باشد
     * - وضعیت APPROVED داشته باشد  
     * - حداقل یک آیتم موجود داشته باشد
     * 
     * @param vendorId شناسه فروشنده
     * @return true اگر سفارش می‌پذیرد، false در غیر این صورت
     * @throws IllegalArgumentException در صورت شناسه نامعتبر
     */
    public boolean isVendorAcceptingOrders(Long vendorId) {
        if (vendorId == null || vendorId <= 0) {
            throw new IllegalArgumentException("Vendor ID must be positive");
        }

        Restaurant vendor = restaurantRepository.findById(vendorId).orElse(null);
        
        // false برگردان اگر فروشنده وجود ندارد یا تایید نشده
        if (vendor == null || vendor.getStatus() != RestaurantStatus.APPROVED) {
            return false;
        }
        
        // بررسی وجود حداقل یک آیتم موجود
        return itemRepository.countAvailableByRestaurant(vendorId) > 0;
    }
    
    /**
     * کلاس داده آمار فروشندگان
     * 
     * این کلاس اطلاعات آماری مفید در مورد فروشنده را نگه می‌دارد
     * شامل تعداد آیتم‌ها، دسته‌ها و سایر معیارهای عملکرد
     */
    public static class VendorStats {
        /** شناسه فروشنده */
        private final Long vendorId;
        /** نام فروشنده */
        private final String vendorName;
        /** تعداد کل آیتم‌های منو */
        private final int totalItems;
        /** تعداد آیتم‌های موجود */
        private final int availableItems;
        /** تعداد کل دسته‌های غذایی */
        private final int totalCategories;
        /** لیست نام دسته‌های غذایی */
        private final List<String> categories;
        
        /**
         * سازنده کلاس VendorStats
         * 
         * @param vendorId شناسه فروشنده
         * @param vendorName نام فروشنده
         * @param totalItems تعداد کل آیتم‌ها
         * @param availableItems تعداد آیتم‌های موجود
         * @param totalCategories تعداد دسته‌ها
         * @param categories لیست دسته‌ها
         */
        public VendorStats(Long vendorId, String vendorName, int totalItems, int availableItems, int totalCategories, List<String> categories) {
            this.vendorId = vendorId;
            this.vendorName = vendorName;
            this.totalItems = totalItems;
            this.availableItems = availableItems;
            this.totalCategories = totalCategories;
            this.categories = categories;
        }
        
        // Getters با کامنت فارسی
        /** @return شناسه فروشنده */
        public Long getVendorId() { return vendorId; }
        /** @return نام فروشنده */
        public String getVendorName() { return vendorName; }
        /** @return تعداد کل آیتم‌ها */
        public int getTotalItems() { return totalItems; }
        /** @return تعداد آیتم‌های موجود */
        public int getAvailableItems() { return availableItems; }
        /** @return تعداد دسته‌های غذایی */
        public int getTotalCategories() { return totalCategories; }
        /** @return لیست دسته‌های غذایی */
        public List<String> getCategories() { return categories; }
    }
}
