package com.myapp.menu;

import com.myapp.common.models.FoodItem;
import com.myapp.item.ItemRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository مدیریت منوی رستوران‌ها
 * 
 * این کلاس یک wrapper حول ItemRepository است که عملیات مخصوص منو را فراهم می‌کند:
 * - مدیریت آیتم‌های منوی رستوران
 * - دسته‌بندی آیتم‌های غذایی
 * - مدیریت موجودی و در دسترس بودن
 * - جستجو و فیلتر آیتم‌های منو
 * - آمار و گزارش‌گیری از منو
 * 
 * این کلاس به جای اتصال مستقیم به دیتابیس، از ItemRepository استفاده می‌کند
 * تا عملیات مربوط به منو را به شکل مفهومی و منطقی ارائه دهد.
 * 
 * Pattern های استفاده شده:
 * - Repository Pattern: انتزاع لایه دسترسی داده
 * - Facade Pattern: ساده‌سازی رابط با ItemRepository
 * - Delegation Pattern: واگذاری عملیات به ItemRepository
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class MenuRepository {
    
    /** Repository آیتم‌های غذایی */
    private final ItemRepository itemRepository;
    
    /**
     * سازنده پیش‌فرض
     * ItemRepository را به صورت خودکار ایجاد می‌کند
     */
    public MenuRepository() {
        this.itemRepository = new ItemRepository();
    }
    
    /**
     * سازنده برای dependency injection
     * 
     * @param itemRepository repository آیتم‌های غذایی تزریق شده
     */
    public MenuRepository(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
    
    /**
     * افزودن آیتم جدید به منوی رستوران
     * 
     * @param foodItem آیتم غذایی جدید
     * @return آیتم ذخیره شده با ID تولید شده
     */
    public FoodItem addMenuItem(FoodItem foodItem) {
        return itemRepository.saveNew(foodItem);
    }
    
    /**
     * به‌روزرسانی آیتم موجود در منو
     * 
     * @param foodItem آیتم غذایی برای به‌روزرسانی
     * @return آیتم به‌روزرسانی شده
     */
    public FoodItem updateMenuItem(FoodItem foodItem) {
        return itemRepository.save(foodItem);
    }
    
    /**
     * دریافت منوی کامل یک رستوران
     * 
     * شامل تمام آیتم‌ها، در دسترس و غیر قابل دسترس
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست کامل آیتم‌های منو
     */
    public List<FoodItem> getMenuByRestaurant(Long restaurantId) {
        return itemRepository.findByRestaurant(restaurantId);
    }
    
    /**
     * دریافت آیتم‌های در دسترس منوی رستوران
     * 
     * فقط آیتم‌هایی که available=true و quantity>0 هستند
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست آیتم‌های قابل سفارش
     */
    public List<FoodItem> getAvailableMenuByRestaurant(Long restaurantId) {
        return itemRepository.findAvailableByRestaurant(restaurantId);
    }
    
    /**
     * دریافت آیتم‌های منو بر اساس دسته‌بندی
     * 
     * @param restaurantId شناسه رستوران
     * @param category دسته‌بندی (مثل پیتزا، برگر، نوشیدنی)
     * @return لیست آیتم‌های یک دسته خاص
     */
    public List<FoodItem> getMenuByCategory(Long restaurantId, String category) {
        return itemRepository.findByRestaurantAndCategory(restaurantId, category);
    }
    
    /**
     * جستجوی آیتم‌های منو بر اساس کلیدواژه
     * 
     * در نام و توضیحات آیتم‌ها جستجو می‌کند
     * 
     * @param keyword کلیدواژه جستجو
     * @return لیست آیتم‌های مطابق
     */
    public List<FoodItem> searchMenu(String keyword) {
        return itemRepository.searchByKeyword(keyword);
    }
    
    /**
     * دریافت آیتم منو بر اساس شناسه
     * 
     * @param itemId شناسه آیتم
     * @return Optional حاوی آیتم یا خالی
     */
    public Optional<FoodItem> getMenuItem(Long itemId) {
        return itemRepository.findById(itemId);
    }
    
    /**
     * حذف آیتم از منو
     * 
     * این عمل آیتم را کاملاً از دیتابیس حذف می‌کند
     * 
     * @param itemId شناسه آیتم برای حذف
     */
    public void removeMenuItem(Long itemId) {
        itemRepository.delete(itemId);
    }
    
    /**
     * به‌روزرسانی وضعیت در دسترس بودن آیتم
     * 
     * @param itemId شناسه آیتم
     * @param available وضعیت جدید در دسترس بودن
     */
    public void updateItemAvailability(Long itemId, boolean available) {
        itemRepository.updateAvailability(itemId, available);
    }
    
    /**
     * به‌روزرسانی موجودی آیتم
     * 
     * @param itemId شناسه آیتم
     * @param quantity موجودی جدید
     */
    public void updateItemQuantity(Long itemId, Integer quantity) {
        itemRepository.updateQuantity(itemId, quantity);
    }
    
    /**
     * بررسی داشتن آیتم در منوی رستوران
     * 
     * @param restaurantId شناسه رستوران
     * @return true اگر رستوران آیتم منو داشته باشد
     */
    public boolean hasMenuItems(Long restaurantId) {
        return !getMenuByRestaurant(restaurantId).isEmpty();
    }
    
    /**
     * شمارش آیتم‌های در دسترس رستوران
     * 
     * @param restaurantId شناسه رستوران
     * @return تعداد آیتم‌های قابل سفارش
     */
    public long getAvailableItemCount(Long restaurantId) {
        return getAvailableMenuByRestaurant(restaurantId).size();
    }
    
    /**
     * دریافت تمام دسته‌بندی‌های منوی رستوران
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست دسته‌بندی‌های منحصر به فرد
     */
    public List<String> getCategories(Long restaurantId) {
        return itemRepository.getCategoriesByRestaurant(restaurantId);
    }
    
    /**
     * دریافت آیتم‌های کم موجودی رستوران
     * 
     * @param restaurantId شناسه رستوران
     * @param threshold آستانه کم موجودی
     * @return لیست آیتم‌های با موجودی پایین‌تر از آستانه
     */
    public List<FoodItem> getLowStockItems(Long restaurantId, int threshold) {
        return itemRepository.findLowStockByRestaurant(restaurantId, threshold);
    }
}
