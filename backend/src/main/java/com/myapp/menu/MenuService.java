package com.myapp.menu;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.item.ItemRepository;
import com.myapp.restaurant.RestaurantRepository;

import java.util.List;
import java.util.Optional;

/**
 * سرویس مدیریت منوی رستوران‌ها
 * 
 * این کلاس منطق کسب‌وکار مربوط به مدیریت منوی رستوران‌ها را پیاده‌سازی می‌کند:
 * 
 * === عملیات CRUD منو ===
 * - افزودن آیتم جدید به منو
 * - به‌روزرسانی آیتم‌های موجود
 * - حذف آیتم از منو
 * - دریافت منوی کامل یا فیلتر شده
 * 
 * === مدیریت موجودی ===
 * - به‌روزرسانی وضعیت در دسترس بودن
 * - مدیریت موجودی آیتم‌ها
 * - شناسایی آیتم‌های کم موجودی
 * - مدیریت وضعیت out-of-stock
 * 
 * === دسته‌بندی و جستجو ===
 * - مدیریت دسته‌بندی آیتم‌ها
 * - فیلتر بر اساس دسته
 * - جستجوی آیتم‌های منو
 * 
 * === آمار و گزارش ===
 * - آمار کلی منو
 * - گزارش موجودی
 * - تحلیل در دسترس بودن آیتم‌ها
 * 
 * ویژگی‌های کلیدی:
 * - Input Validation: اعتبارسنجی کامل ورودی‌ها
 * - Business Rules: اعمال قوانین کسب‌وکار
 * - Error Handling: مدیریت خطاهای مختلف
 * - Data Integrity: حفظ یکپارچگی داده‌ها
 * - Permission Checking: بررسی مالکیت رستوران
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class MenuService {
    
    /** Repository مدیریت منو */
    private final MenuRepository menuRepository;
    
    /** Repository آیتم‌های غذایی */
    private final ItemRepository itemRepository;
    
    /** Repository رستوران‌ها */
    private final RestaurantRepository restaurantRepository;
    
    /**
     * سازنده پیش‌فرض
     * Dependencies را به صورت خودکار ایجاد می‌کند
     */
    public MenuService() {
        this.menuRepository = new MenuRepository();
        this.itemRepository = new ItemRepository();
        this.restaurantRepository = new RestaurantRepository();
    }
    
    /**
     * سازنده برای dependency injection (تست)
     * 
     * @param menuRepository repository منو
     * @param itemRepository repository آیتم‌ها
     * @param restaurantRepository repository رستوران‌ها
     */
    public MenuService(MenuRepository menuRepository, ItemRepository itemRepository, RestaurantRepository restaurantRepository) {
        this.menuRepository = menuRepository;
        this.itemRepository = itemRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * دریافت منوی کامل یک رستوران
     * 
     * شامل تمام آیتم‌ها بدون در نظر گیری وضعیت در دسترس بودن
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست کامل آیتم‌های منو
     * @throws IllegalArgumentException اگر ID نامعتبر باشد
     * @throws NotFoundException اگر رستوران وجود نداشته باشد
     */
    public List<FoodItem> getRestaurantMenu(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // بررسی وجود رستوران
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        return menuRepository.getMenuByRestaurant(restaurantId);
    }
    
    /**
     * دریافت آیتم‌های در دسترس منوی رستوران
     * 
     * فقط آیتم‌هایی که available=true و quantity>0 هستند
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست آیتم‌های قابل سفارش
     * @throws IllegalArgumentException اگر ID نامعتبر باشد
     * @throws NotFoundException اگر رستوران وجود نداشته باشد
     */
    public List<FoodItem> getAvailableMenu(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // بررسی وجود رستوران
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        return menuRepository.getAvailableMenuByRestaurant(restaurantId);
    }
    
    /**
     * افزودن آیتم جدید به منوی رستوران
     * 
     * آیتم با موجودی اولیه صفر ایجاد می‌شود
     * vendor باید موجودی را به صورت جداگانه تنظیم کند
     * 
     * @param restaurantId شناسه رستوران
     * @param name نام آیتم غذایی
     * @param description توضیحات آیتم
     * @param price قیمت آیتم
     * @param category دسته‌بندی آیتم
     * @return آیتم ایجاد شده
     * @throws IllegalArgumentException اگر ورودی‌ها نامعتبر باشند
     * @throws NotFoundException اگر رستوران وجود نداشته باشد
     */
    public FoodItem addItemToMenu(Long restaurantId, String name, String description, Double price, String category) {
        validateAddItemInput(restaurantId, name, description, price, category);
        
        // بررسی وجود رستوران و دریافت آن
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        Restaurant restaurant = restaurantOpt.get();
        
        // ایجاد آیتم غذایی جدید
        FoodItem foodItem = FoodItem.forMenu(name.trim(), description.trim(), price, category.trim(), restaurant);
        
        // تنظیم موجودی اولیه به صفر - vendor باید به صورت صریح موجودی تنظیم کند
        foodItem.setQuantity(0);
        
        return itemRepository.save(foodItem);
    }
    
    /**
     * افزودن آیتم به منو با استفاده از شیء FoodItem
     * 
     * @param foodItem آیتم غذایی برای افزودن
     * @return آیتم ایجاد شده
     * @throws IllegalArgumentException اگر آیتم یا رستوران نامعتبر باشد
     * @throws NotFoundException اگر رستوران وجود نداشته باشد
     */
    public FoodItem addItemToMenu(FoodItem foodItem) {
        if (foodItem == null) {
            throw new IllegalArgumentException("Food item cannot be null");
        }
        if (foodItem.getRestaurant() == null || foodItem.getRestaurant().getId() == null) {
            throw new IllegalArgumentException("Food item must have a valid restaurant");
        }
        
        validateAddItemInput(foodItem.getRestaurant().getId(), foodItem.getName(), 
                           foodItem.getDescription(), foodItem.getPrice(), foodItem.getCategory());
        
        // بررسی وجود رستوران
        if (!restaurantRepository.existsById(foodItem.getRestaurant().getId())) {
            throw new NotFoundException("Restaurant", foodItem.getRestaurant().getId());
        }
        
        return itemRepository.save(foodItem);
    }
    
    /**
     * به‌روزرسانی آیتم موجود در منو
     * 
     * تنها فیلدهای ارائه شده (غیر null) به‌روزرسانی می‌شوند
     * 
     * @param itemId شناسه آیتم
     * @param name نام جدید (اختیاری)
     * @param description توضیحات جدید (اختیاری)
     * @param price قیمت جدید (اختیاری)
     * @param category دسته‌بندی جدید (اختیاری)
     * @param quantity موجودی جدید (اختیاری)
     * @param available وضعیت در دسترس بودن (اختیاری)
     * @return آیتم به‌روزرسانی شده
     * @throws IllegalArgumentException اگر پارامترها نامعتبر باشند
     * @throws NotFoundException اگر آیتم وجود نداشته باشد
     */
    public FoodItem updateMenuItem(Long itemId, String name, String description, Double price, String category, Integer quantity, Boolean available) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        
        // دریافت آیتم موجود
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("Food item", itemId);
        }
        
        FoodItem existingItem = itemOpt.get();
        
        // به‌روزرسانی فیلدها در صورت ارائه
        if (name != null && !name.trim().isEmpty()) {
            if (name.trim().length() > 100) {
                throw new IllegalArgumentException("Food item name cannot exceed 100 characters");
            }
            existingItem.setName(name.trim());
        }
        
        if (description != null && !description.trim().isEmpty()) {
            if (description.trim().length() > 500) {
                throw new IllegalArgumentException("Food item description cannot exceed 500 characters");
            }
            existingItem.setDescription(description.trim());
        }
        
        if (price != null) {
            if (price < 0.01 || price > 9999.99) {
                throw new IllegalArgumentException("Price must be between 0.01 and 9999.99");
            }
            existingItem.setPrice(price);
        }
        
        if (category != null && !category.trim().isEmpty()) {
            if (category.trim().length() > 50) {
                throw new IllegalArgumentException("Category cannot exceed 50 characters");
            }
            existingItem.setCategory(category.trim());
        }
        
        if (quantity != null) {
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
            existingItem.setQuantity(quantity);
        }
        
        if (available != null) {
            existingItem.setAvailable(available);
        }
        
        return itemRepository.save(existingItem);
    }
    
    /**
     * به‌روزرسانی آیتم منو با استفاده از شیء FoodItem
     * 
     * فقط فیلدهای non-null به‌روزرسانی می‌شوند
     * رستوران آیتم تغییر نمی‌کند
     * 
     * @param foodItem آیتم با اطلاعات جدید
     * @return آیتم به‌روزرسانی شده
     * @throws IllegalArgumentException اگر آیتم یا ID نامعتبر باشد
     * @throws NotFoundException اگر آیتم وجود نداشته باشد
     */
    public FoodItem updateMenuItem(FoodItem foodItem) {
        if (foodItem == null) {
            throw new IllegalArgumentException("Food item cannot be null");
        }
        if (foodItem.getId() == null || foodItem.getId() <= 0) {
            throw new IllegalArgumentException("Food item ID must be positive");
        }
        
        // دریافت آیتم موجود برای حفظ تمام فیلدها
        Optional<FoodItem> existingOpt = itemRepository.findById(foodItem.getId());
        if (existingOpt.isEmpty()) {
            throw new NotFoundException("Food item", foodItem.getId());
        }
        
        FoodItem existingItem = existingOpt.get();
        
        // به‌روزرسانی تنها فیلدهای ارائه شده
        if (foodItem.getName() != null && !foodItem.getName().trim().isEmpty()) {
            if (foodItem.getName().trim().length() > 100) {
                throw new IllegalArgumentException("Food item name cannot exceed 100 characters");
            }
            existingItem.setName(foodItem.getName().trim());
        }
        
        if (foodItem.getDescription() != null && !foodItem.getDescription().trim().isEmpty()) {
            if (foodItem.getDescription().trim().length() > 500) {
                throw new IllegalArgumentException("Food item description cannot exceed 500 characters");
            }
            existingItem.setDescription(foodItem.getDescription().trim());
        }
        
        if (foodItem.getPrice() != null) {
            if (foodItem.getPrice() < 0.01 || foodItem.getPrice() > 9999.99) {
                throw new IllegalArgumentException("Price must be between 0.01 and 9999.99");
            }
            existingItem.setPrice(foodItem.getPrice());
        }
        
        if (foodItem.getCategory() != null && !foodItem.getCategory().trim().isEmpty()) {
            if (foodItem.getCategory().trim().length() > 50) {
                throw new IllegalArgumentException("Category cannot exceed 50 characters");
            }
            existingItem.setCategory(foodItem.getCategory().trim());
        }
        
        if (foodItem.getQuantity() != null) {
            if (foodItem.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
            existingItem.setQuantity(foodItem.getQuantity());
        }
        
        if (foodItem.getAvailable() != null) {
            existingItem.setAvailable(foodItem.getAvailable());
        }
        
        // حفظ اتصال رستوران
        // (رستوران نباید از طریق update تغییر کند)
        
        return itemRepository.save(existingItem);
    }
    
    /**
     * حذف آیتم از منو
     * 
     * این عمل آیتم را کاملاً از سیستم حذف می‌کند
     * 
     * @param itemId شناسه آیتم
     * @throws IllegalArgumentException اگر ID نامعتبر باشد
     * @throws NotFoundException اگر آیتم وجود نداشته باشد
     */
    public void removeItemFromMenu(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        
        // بررسی وجود آیتم
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Food item", itemId);
        }
        
        itemRepository.delete(itemId);
    }
    
    /**
     * تنظیم وضعیت در دسترس بودن آیتم
     * 
     * @param itemId شناسه آیتم
     * @param available وضعیت جدید
     * @return آیتم به‌روزرسانی شده
     * @throws IllegalArgumentException اگر ID نامعتبر باشد
     * @throws NotFoundException اگر آیتم وجود نداشته باشد
     */
    public FoodItem setItemAvailability(Long itemId, boolean available) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("Food item", itemId);
        }
        
        FoodItem item = itemOpt.get();
        item.setAvailable(available);
        
        return itemRepository.save(item);
    }
    
    /**
     * به‌روزرسانی موجودی آیتم
     * 
     * @param itemId شناسه آیتم
     * @param quantity موجودی جدید
     * @return آیتم به‌روزرسانی شده
     * @throws IllegalArgumentException اگر پارامترها نامعتبر باشند
     * @throws NotFoundException اگر آیتم وجود نداشته باشد
     */
    public FoodItem updateItemQuantity(Long itemId, int quantity) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("Food item", itemId);
        }
        
        FoodItem item = itemOpt.get();
        item.setQuantity(quantity);
        
        return itemRepository.save(item);
    }
    
    /**
     * دریافت آیتم‌های منو بر اساس دسته‌بندی
     * 
     * @param restaurantId شناسه رستوران
     * @param category دسته‌بندی
     * @return لیست آیتم‌های دسته مشخص
     * @throws IllegalArgumentException اگر پارامترها نامعتبر باشند
     * @throws NotFoundException اگر رستوران وجود نداشته باشد
     */
    public List<FoodItem> getMenuByCategory(Long restaurantId, String category) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        
        // بررسی وجود رستوران
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        return menuRepository.getMenuByCategory(restaurantId, category.trim());
    }
    
    /**
     * دریافت تمام دسته‌بندی‌های منوی رستوران
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست دسته‌بندی‌های منحصر به فرد
     * @throws IllegalArgumentException اگر ID نامعتبر باشد
     * @throws NotFoundException اگر رستوران وجود نداشته باشد
     */
    public List<String> getMenuCategories(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // بررسی وجود رستوران
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        return menuRepository.getCategories(restaurantId);
    }
    
    /**
     * دریافت آیتم‌های کم موجودی رستوران
     * 
     * @param restaurantId شناسه رستوران
     * @param threshold آستانه کم موجودی
     * @return لیست آیتم‌های با موجودی کمتر از آستانه
     * @throws IllegalArgumentException اگر پارامترها نامعتبر باشند
     * @throws NotFoundException اگر رستوران وجود نداشته باشد
     */
    public List<FoodItem> getLowStockItems(Long restaurantId, int threshold) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }
        
        // بررسی وجود رستوران
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        return menuRepository.getLowStockItems(restaurantId, threshold);
    }
    
    /**
     * دریافت آمار منوی رستوران
     * 
     * شامل تعداد کل آیتم‌ها، آیتم‌های در دسترس، ناموجود، کم موجودی
     * 
     * @param restaurantId شناسه رستوران
     * @return آمار کامل منو
     * @throws IllegalArgumentException اگر ID نامعتبر باشد
     * @throws NotFoundException اگر رستوران وجود نداشته باشد
     */
    public MenuStatistics getMenuStatistics(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // بررسی وجود رستوران
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        List<FoodItem> allItems = menuRepository.getMenuByRestaurant(restaurantId);
        
        int totalItems = allItems.size();
        int availableItems = (int) allItems.stream().filter(FoodItem::getAvailable).count();
        int unavailableItems = totalItems - availableItems;
        int outOfStockItems = (int) allItems.stream().filter(item -> item.getQuantity() == 0).count();
        int lowStockItems = (int) allItems.stream().filter(item -> item.getQuantity() > 0 && item.getQuantity() <= 5).count();
        
        return new MenuStatistics(totalItems, availableItems, unavailableItems, outOfStockItems, lowStockItems);
    }
    
    /**
     * بررسی مالکیت رستوران برای آیتم منو
     * 
     * @param restaurantId شناسه رستوران
     * @param itemId شناسه آیتم
     * @return true اگر آیتم متعلق به رستوران باشد
     * @throws IllegalArgumentException اگر IDها نامعتبر باشند
     */
    public boolean isRestaurantOwner(Long restaurantId, Long itemId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            return false;
        }
        
        FoodItem item = itemOpt.get();
        return item.getRestaurant().getId().equals(restaurantId);
    }
    
    /**
     * اعتبارسنجی ورودی‌های افزودن آیتم جدید
     * 
     * @param restaurantId شناسه رستوران
     * @param name نام آیتم
     * @param description توضیحات
     * @param price قیمت
     * @param category دسته‌بندی
     * @throws IllegalArgumentException اگر هر یک از ورودی‌ها نامعتبر باشد
     */
    private void validateAddItemInput(Long restaurantId, String name, String description, Double price, String category) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Food item name cannot be empty");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Food item name cannot exceed 100 characters");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Food item description cannot be empty");
        }
        if (description.trim().length() > 500) {
            throw new IllegalArgumentException("Food item description cannot exceed 500 characters");
        }
        if (price == null || price < 0.01 || price > 9999.99) {
            throw new IllegalArgumentException("Price must be between 0.01 and 9999.99");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        if (category.trim().length() > 50) {
            throw new IllegalArgumentException("Category cannot exceed 50 characters");
        }
    }
    
    /**
     * کلاس آمار منو
     * 
     * حاوی اطلاعات آماری مربوط به منوی رستوران
     */
    public static class MenuStatistics {
        /** تعداد کل آیتم‌ها */
        private final int totalItems;
        
        /** تعداد آیتم‌های در دسترس */
        private final int availableItems;
        
        /** تعداد آیتم‌های غیر قابل دسترس */
        private final int unavailableItems;
        
        /** تعداد آیتم‌های تمام شده */
        private final int outOfStockItems;
        
        /** تعداد آیتم‌های کم موجودی */
        private final int lowStockItems;
        
        /**
         * سازنده آمار منو
         */
        public MenuStatistics(int totalItems, int availableItems, int unavailableItems, int outOfStockItems, int lowStockItems) {
            this.totalItems = totalItems;
            this.availableItems = availableItems;
            this.unavailableItems = unavailableItems;
            this.outOfStockItems = outOfStockItems;
            this.lowStockItems = lowStockItems;
        }
        
        /** @return تعداد کل آیتم‌ها */
        public int getTotalItems() { return totalItems; }
        
        /** @return تعداد آیتم‌های در دسترس */
        public int getAvailableItems() { return availableItems; }
        
        /** @return تعداد آیتم‌های غیر قابل دسترس */
        public int getUnavailableItems() { return unavailableItems; }
        
        /** @return تعداد آیتم‌های تمام شده */
        public int getOutOfStockItems() { return outOfStockItems; }
        
        /** @return تعداد آیتم‌های کم موجودی */
        public int getLowStockItems() { return lowStockItems; }
        
        /** @return تعداد آیتم‌های موجود */
        public int getInStockItems() { return totalItems - outOfStockItems; }
        
        /**
         * محاسبه درصد در دسترس بودن
         * 
         * @return درصد آیتم‌های در دسترس (0-100)
         */
        public double getAvailabilityPercentage() { 
            return totalItems > 0 ? (double) availableItems / totalItems * 100 : 0.0; 
        }
    }
}
