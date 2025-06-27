package com.myapp.item;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import com.myapp.item.ItemRepository;
import com.myapp.item.ItemService;
import com.myapp.restaurant.RestaurantRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * کلاس تست جامع برای ItemService
 * 
 * این کلاس تمام عملکردهای ItemService را در سناریوهای مختلف تست می‌کند:
 * 
 * === دسته‌های تست ===
 * 1. AddItemTests - تست‌های افزودن آیتم
 * 2. UpdateItemTests - تست‌های به‌روزرسانی آیتم
 * 3. GetItemTests - تست‌های دریافت آیتم
 * 4. RestaurantItemsTests - تست‌های آیتم‌های رستوران
 * 5. SearchTests - تست‌های جستجو
 * 6. InventoryManagementTests - تست‌های مدیریت موجودی
 * 7. MenuStatisticsTests - تست‌های آمار منو
 * 8. InputValidationTests - تست‌های اعتبارسنجی ورودی
 * 9. BoundaryValueTests - تست‌های مقادیر مرزی
 * 10. BusinessLogicTests - تست‌های منطق کسب‌وکار
 * 11. DataConsistencyTests - تست‌های سازگاری داده
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class ItemServiceTest {
    
    /** Factory برای ایجاد session های دیتابیس */
    private static SessionFactory sessionFactory;
    
    /** Session فعال برای هر تست */
    private Session session;
    
    /** سرویس اصلی تحت تست */
    private ItemService itemService;
    
    /** Repository آیتم‌ها */
    private ItemRepository itemRepository;
    
    /** Repository رستوران‌ها */
    private RestaurantRepository restaurantRepository;
    
    /**
     * راه‌اندازی کلاس تست
     * این متد فقط یک بار قبل از همه تست‌ها اجرا می‌شود
     */
    @BeforeAll
    static void setUpClass() {
        sessionFactory = DatabaseUtil.getSessionFactory();
    }
    
    /**
     * نکته: SessionFactory بین کلاس‌های تست مشترک است
     * بنابراین آن را اینجا نمی‌بندیم و با خاموش شدن JVM بسته می‌شود
     */
    
    /**
     * راه‌اندازی هر تست
     * این متد قبل از هر تست اجرا می‌شود و محیط پاک برای تست فراهم می‌کند
     */
    @BeforeEach
    void setUp() {
        // ایجاد session جدید برای هر تست
        session = sessionFactory.openSession();
        
        // ایجاد repository ها و service
        itemRepository = new ItemRepository();
        restaurantRepository = new RestaurantRepository();
        itemService = new ItemService(itemRepository, restaurantRepository);
        
        // پاک‌سازی دیتابیس قبل از هر تست
        session.beginTransaction();
        session.createQuery("DELETE FROM OrderItem").executeUpdate();
        session.createQuery("DELETE FROM Order").executeUpdate();
        session.createQuery("DELETE FROM FoodItem").executeUpdate();
        session.createQuery("DELETE FROM Restaurant").executeUpdate();
        session.getTransaction().commit();
    }
    
    /**
     * پاک‌سازی بعد از هر تست
     * Session را می‌بندد تا resource leak نداشته باشیم
     */
    @AfterEach
    void tearDown() {
        if (session != null) {
            session.close();
        }
    }
    
    /**
     * دسته تست‌های افزودن آیتم
     * 
     * این کلاس تمام سناریوهای مربوط به افزودن آیتم جدید را تست می‌کند:
     * - افزودن آیتم با داده‌های معتبر
     * - افزودن آیتم بدون URL تصویر
     * - خطاها برای رستوران ناموجود
     * - validation نام خالی
     * - validation قیمت منفی
     * - validation قیمت بیش از حد
     */
    @Nested
    @DisplayName("Add Item Tests")
    class AddItemTests {
        
        /**
         * تست افزودن موفق آیتم با داده‌های کامل و معتبر
         * 
         * سناریو: کاربر آیتم جدید با تمام فیلدها اضافه می‌کند
         * انتظار: آیتم با موفقیت ایجاد شود و تمام فیلدها صحیح باشند
         */
        @Test
        @DisplayName("Should add item successfully with valid data")
        void addItem_ValidData_Success() {
            // Given - آماده‌سازی: ایجاد رستوران تست
            Restaurant restaurant = createTestRestaurant();
            
            // When - عمل: افزودن آیتم با داده‌های کامل
            FoodItem item = itemService.addItem(
                restaurant.getId(),
                "Cheeseburger",
                "Delicious beef burger with cheese",
                12.99,
                "Burgers",
                "https://example.com/burger.jpg",
                50
            );
            
            // Then - بررسی: تأیید صحت تمام فیلدهای آیتم ایجاد شده
            assertNotNull(item);
            assertNotNull(item.getId());
            assertEquals("Cheeseburger", item.getName());
            assertEquals("Delicious beef burger with cheese", item.getDescription());
            assertEquals(12.99, item.getPrice(), 0.01);
            assertEquals("Burgers", item.getCategory());
            assertEquals("https://example.com/burger.jpg", item.getImageUrl());
            assertEquals(50, item.getQuantity());
            assertEquals(restaurant.getId(), item.getRestaurant().getId());
            assertTrue(item.getAvailable());
        }
        
        /**
         * تست افزودن آیتم بدون URL تصویر
         * 
         * سناریو: کاربر آیتم بدون تصویر اضافه می‌کند
         * انتظار: آیتم ایجاد شود و فیلد imageUrl خالی باشد
         */
        @Test
        @DisplayName("Should add item without image URL")
        void addItem_NoImageUrl_Success() {
            // Given - آماده‌سازی: ایجاد رستوران تست
            Restaurant restaurant = createTestRestaurant();
            
            // When - عمل: افزودن آیتم بدون URL تصویر
            FoodItem item = itemService.addItem(
                restaurant.getId(),
                "Pizza",
                "Cheese pizza",
                15.99,
                "Pizza",
                null,
                30
            );
            
            // Then - بررسی: تأیید ایجاد آیتم و خالی بودن URL تصویر
            assertNotNull(item);
            assertEquals("Pizza", item.getName());
            assertNull(item.getImageUrl());
        }
        
        /**
         * تست خطا برای رستوران ناموجود
         * 
         * سناریو: تلاش برای افزودن آیتم به رستورانی که وجود ندارد
         * انتظار: پرتاب NotFoundException با پیام مناسب
         */
        @Test
        @DisplayName("Should throw exception for non-existent restaurant")
        void addItem_NonExistentRestaurant_ThrowsException() {
            // When & Then - عمل و بررسی: تلاش افزودن آیتم به رستوران ناموجود
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.addItem(999L, "Pizza", "Description", 10.0, "Category", null, 10)
            );
            assertEquals("Restaurant not found with id=999", exception.getMessage());
        }
        
        /**
         * تست validation نام خالی
         * 
         * سناریو: تلاش برای ایجاد آیتم با نام خالی
         * انتظار: پرتاب IllegalArgumentException
         */
        @Test
        @DisplayName("Should throw exception for empty name")
        void addItem_EmptyName_ThrowsException() {
            // Given - آماده‌سازی: ایجاد رستوران معتبر
            Restaurant restaurant = createTestRestaurant();
            
            // When & Then - عمل و بررسی: تلاش ایجاد آیتم با نام خالی
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "", "Description", 10.0, "Category", null, 10)
            );
            assertEquals("Item name cannot be empty", exception.getMessage());
        }
        
        /**
         * تست validation قیمت منفی
         * 
         * سناریو: تلاش برای ایجاد آیتم با قیمت منفی
         * انتظار: پرتاب IllegalArgumentException
         */
        @Test
        @DisplayName("Should throw exception for negative price")
        void addItem_NegativePrice_ThrowsException() {
            // Given - آماده‌سازی: ایجاد رستوران معتبر
            Restaurant restaurant = createTestRestaurant();
            
            // When & Then - عمل و بررسی: تلاش ایجاد آیتم با قیمت منفی
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Pizza", "Description", -5.0, "Category", null, 10)
            );
            assertEquals("Item price must be positive", exception.getMessage());
        }
        
        /**
         * تست validation قیمت بیش از حد مجاز
         * 
         * سناریو: تلاش برای ایجاد آیتم با قیمت بالاتر از حد مجاز
         * انتظار: پرتاب IllegalArgumentException
         */
        @Test
        @DisplayName("Should throw exception for excessive price")
        void addItem_ExcessivePrice_ThrowsException() {
            // Given - آماده‌سازی: ایجاد رستوران معتبر
            Restaurant restaurant = createTestRestaurant();
            
            // When & Then - عمل و بررسی: تلاش ایجاد آیتم با قیمت بیش از حد
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Pizza", "Description", 15000.0, "Category", null, 10)
            );
            assertEquals("Item price cannot exceed 10,000", exception.getMessage());
        }
    }
    
    /**
     * دسته تست‌های به‌روزرسانی آیتم
     * 
     * این کلاس سناریوهای مختلف به‌روزرسانی آیتم‌ها را تست می‌کند:
     * - به‌روزرسانی کامل آیتم
     * - به‌روزرسانی جزئی (فقط برخی فیلدها)
     * - خطا برای آیتم ناموجود
     */
    @Nested
    @DisplayName("Update Item Tests")
    class UpdateItemTests {
        
        /**
         * تست به‌روزرسانی موفق آیتم با داده‌های معتبر
         * 
         * سناریو: به‌روزرسانی تمام فیلدهای یک آیتم موجود
         * انتظار: آیتم با داده‌های جدید به‌روزرسانی شود
         */
        @Test
        @DisplayName("Should update item successfully")
        void updateItem_ValidData_Success() {
            // Given - آماده‌سازی: ایجاد رستوران و آیتم تست
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            
            // When - عمل: به‌روزرسانی آیتم با داده‌های جدید
            FoodItem updatedItem = itemService.updateItem(
                item.getId(),
                "Updated Burger",
                "Updated description",
                15.99,
                "Updated Category",
                "https://new-image.com/burger.jpg",
                75
            );
            
            // Then - بررسی: تأیید به‌روزرسانی تمام فیلدها
            assertNotNull(updatedItem);
            assertEquals("Updated Burger", updatedItem.getName());
            assertEquals("Updated description", updatedItem.getDescription());
            assertEquals(15.99, updatedItem.getPrice(), 0.01);
            assertEquals("Updated Category", updatedItem.getCategory());
            assertEquals("https://new-image.com/burger.jpg", updatedItem.getImageUrl());
            assertEquals(75, updatedItem.getQuantity());
        }
        
        /**
         * تست به‌روزرسانی جزئی آیتم (فقط برخی فیلدها)
         * 
         * سناریو: کاربر فقط برخی فیلدهای آیتم را به‌روزرسانی می‌کند
         * انتظار: فقط فیلدهای ارائه شده تغییر کنند، بقیه بدون تغییر بمانند
         */
        @Test
        @DisplayName("Should update only provided fields")
        void updateItem_PartialUpdate_Success() {
            // Given - آماده‌سازی: ایجاد آیتم با مقادیر اولیه
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            String originalName = item.getName();
            double originalPrice = item.getPrice();
            
            // When - عمل: به‌روزرسانی جزئی (فقط توضیحات)
            FoodItem updatedItem = itemService.updateItem(
                item.getId(),
                null, // عدم تغییر نام
                "New description only",
                -1, // عدم تغییر قیمت (مقدار منفی نادیده گرفته می‌شود)
                null, // عدم تغییر دسته‌بندی
                null, // عدم تغییر تصویر
                null  // عدم تغییر موجودی
            );
            
            // Then - بررسی: فقط توضیحات تغییر کرده، بقیه ثابت مانده‌اند
            assertEquals(originalName, updatedItem.getName()); // بدون تغییر
            assertEquals("New description only", updatedItem.getDescription()); // تغییر کرده
            assertEquals(originalPrice, updatedItem.getPrice(), 0.01); // بدون تغییر
        }
        
        /**
         * تست خطا برای آیتم ناموجود در به‌روزرسانی
         * 
         * سناریو: تلاش برای به‌روزرسانی آیتمی که وجود ندارد
         * انتظار: پرتاب NotFoundException با پیام مناسب
         */
        @Test
        @DisplayName("Should throw exception for non-existent item")
        void updateItem_NonExistentItem_ThrowsException() {
            // When & Then - عمل و بررسی: تلاش به‌روزرسانی آیتم ناموجود
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.updateItem(999L, "Name", "Description", 10.0, "Category", null, 10)
            );
            assertEquals("Food item not found with id=999", exception.getMessage());
        }
    }
    
    /**
     * دسته تست‌های دریافت آیتم
     * 
     * این کلاس سناریوهای مختلف دریافت آیتم‌ها را تست می‌کند:
     * - دریافت موفق آیتم با شناسه معتبر
     * - خطا برای شناسه ناموجود
     * - اعتبارسنجی داده‌های برگردانده شده
     */
    @Nested
    @DisplayName("Get Item Tests")
    class GetItemTests {
        
        /**
         * تست دریافت موفق آیتم با شناسه معتبر
         * 
         * سناریو: کاربر شناسه آیتم موجود را وارد می‌کند
         * انتظار: آیتم کامل با تمام اطلاعات برگردانده شود
         */
        @Test
        @DisplayName("Should get item by ID successfully")
        void getItem_ValidId_Success() {
            // Given - آماده‌سازی: ایجاد آیتم در دیتابیس
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            
            // When - عمل: دریافت آیتم با شناسه
            FoodItem foundItem = itemService.getItem(item.getId());
            
            // Then - بررسی: تأیید صحت آیتم برگردانده شده
            assertNotNull(foundItem);
            assertEquals(item.getId(), foundItem.getId());
            assertEquals(item.getName(), foundItem.getName());
        }
        
        /**
         * تست خطا برای شناسه آیتم ناموجود
         * 
         * سناریو: کاربر شناسه آیتمی که وجود ندارد را وارد می‌کند
         * انتظار: پرتاب NotFoundException با پیام واضح
         */
        @Test
        @DisplayName("Should throw exception for non-existent item")
        void getItem_NonExistentId_ThrowsException() {
            // When & Then - عمل و بررسی: تلاش دریافت آیتم ناموجود
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.getItem(999L)
            );
            assertEquals("Food item not found with id=999", exception.getMessage());
        }
    }
    
    /**
     * دسته تست‌های آیتم‌های رستوران
     * 
     * این کلاس سناریوهای مختلف مربوط به آیتم‌های رستوران را تست می‌کند:
     * - دریافت تمام آیتم‌های رستوران
     * - دریافت فقط آیتم‌های در دسترس
     * - خطا برای رستوران ناموجود
     * - فیلترینگ بر اساس وضعیت availability
     */
    @Nested
    @DisplayName("Restaurant Items Tests")
    class RestaurantItemsTests {
        
        /**
         * تست دریافت تمام آیتم‌های یک رستوران
         * 
         * سناریو: دریافت کل منوی رستوران شامل آیتم‌های فعال و غیرفعال
         * انتظار: برگرداندن تمام آیتم‌های رستوران بدون فیلتر
         */
        @Test
        @DisplayName("Should get all restaurant items")
        void getRestaurantItems_ValidRestaurant_Success() {
            // Given - آماده‌سازی: ایجاد رستوران با آیتم‌های مختلف
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Burger", true);
            createTestFoodItem(restaurant, "Pizza", false);
            createTestFoodItem(restaurant, "Pasta", true);
            
            // When - عمل: دریافت تمام آیتم‌های رستوران
            List<FoodItem> items = itemService.getRestaurantItems(restaurant.getId());
            
            // Then - بررسی: تأیید دریافت تمام آیتم‌ها (3 عدد)
            assertEquals(3, items.size());
        }
        
        /**
         * تست دریافت فقط آیتم‌های در دسترس رستوران
         * 
         * سناریو: دریافت آیتم‌هایی که برای سفارش قابل انتخاب هستند
         * انتظار: فقط آیتم‌های available=true و quantity>0 برگردانده شوند
         */
        @Test
        @DisplayName("Should get only available restaurant items")
        void getAvailableItems_ValidRestaurant_Success() {
            // Given - آماده‌سازی: ایجاد آیتم‌های متنوع
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Burger", true);   // در دسترس
            createTestFoodItem(restaurant, "Pizza", false);   // غیر فعال
            createTestFoodItem(restaurant, "Pasta", true);    // در دسترس
            
            // When - عمل: دریافت فقط آیتم‌های در دسترس
            List<FoodItem> items = itemService.getAvailableItems(restaurant.getId());
            
            // Then - بررسی: فقط 2 آیتم در دسترس برگردانده شده
            assertEquals(2, items.size());
            assertTrue(items.stream().allMatch(item -> item.getAvailable()));
        }
        
        /**
         * تست خطا برای رستوران ناموجود
         * 
         * سناریو: تلاش دریافت آیتم‌های رستورانی که وجود ندارد
         * انتظار: پرتاب NotFoundException با پیام مناسب
         */
        @Test
        @DisplayName("Should throw exception for non-existent restaurant")
        void getRestaurantItems_NonExistentRestaurant_ThrowsException() {
            // When & Then - عمل و بررسی: تلاش دریافت آیتم‌های رستوران ناموجود
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.getRestaurantItems(999L)
            );
            assertEquals("Restaurant not found with id=999", exception.getMessage());
        }
    }
    
    /**
     * دسته تست‌های جستجو
     * 
     * این کلاس عملکردهای جستجو و فیلترینگ آیتم‌ها را تست می‌کند:
     * - جستجو با کلیدواژه
     * - جستجو با کلیدواژه خالی (خطا)
     * - جستجو بر اساس دسته‌بندی
     * - case sensitivity در جستجو
     */
    @Nested
    @DisplayName("Search Tests")
    class SearchTests {
        
        /**
         * تست جستجوی آیتم‌ها با کلیدواژه
         * 
         * سناریو: کاربر کلیدواژه‌ای برای جستجو وارد می‌کند
         * انتظار: آیتم‌هایی که در نام یا keywords شامل کلیدواژه هستند برگردانده شوند
         */
        @Test
        @DisplayName("Should search items by keyword")
        void searchItems_ValidKeyword_Success() {
            // Given - آماده‌سازی: ایجاد آیتم‌های متنوع برای جستجو
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Cheeseburger", true);      // شامل burger
            createTestFoodItem(restaurant, "Chicken Burger", true);    // شامل burger
            createTestFoodItem(restaurant, "Pizza Margherita", true);  // فاقد burger
            
            // When - عمل: جستجو با کلیدواژه "burger"
            List<FoodItem> items = itemService.searchItems("burger");
            
            // Then - بررسی: فقط آیتم‌های مربوط به burger یافت شوند
            assertEquals(2, items.size());
            assertTrue(items.stream().allMatch(item -> 
                item.getName().toLowerCase().contains("burger") ||
                item.getDescription().toLowerCase().contains("burger") ||
                item.getKeywords().toLowerCase().contains("burger")
            ));
        }
        
        /**
         * تست خطا برای کلیدواژه خالی
         * 
         * سناریو: کاربر کلیدواژه خالی برای جستجو وارد می‌کند
         * انتظار: پرتاب IllegalArgumentException
         */
        @Test
        @DisplayName("Should throw exception for empty keyword")
        void searchItems_EmptyKeyword_ThrowsException() {
            // When & Then - عمل و بررسی: تلاش جستجو با کلیدواژه خالی
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                itemService.searchItems("")
            );
            assertEquals("Search keyword cannot be empty", exception.getMessage());
        }
        
        /**
         * تست جستجوی آیتم‌ها بر اساس دسته‌بندی
         * 
         * سناریو: فیلتر آیتم‌ها بر اساس یک دسته‌بندی خاص
         * انتظار: فقط آیتم‌های آن دسته‌بندی برگردانده شوند
         */
        @Test
        @DisplayName("Should get items by category")
        void getItemsByCategory_ValidCategory_Success() {
            // Given - آماده‌سازی: ایجاد آیتم‌ها در دسته‌بندی‌های مختلف
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Burger", "Burgers", true);
            createTestFoodItem(restaurant, "Cheeseburger", "Burgers", true);
            createTestFoodItem(restaurant, "Pizza", "Pizza", true);
            
            // When - عمل: جستجو آیتم‌های دسته "Burgers"
            List<FoodItem> items = itemService.getItemsByCategory("Burgers");
            
            // Then - بررسی: فقط آیتم‌های دسته Burgers یافت شوند
            assertEquals(2, items.size());
            assertTrue(items.stream().allMatch(item -> "Burgers".equals(item.getCategory())));
        }
    }
    
    /**
     * دسته تست‌های مدیریت موجودی
     * 
     * این کلاس عملکردهای مربوط به مدیریت موجودی آیتم‌ها را تست می‌کند:
     * - کاهش موجودی (هنگام سفارش)
     * - افزایش موجودی (هنگام تأمین مجدد)
     * - بررسی وضعیت موجودی
     * - شناسایی آیتم‌های کم موجودی
     * - مدیریت حالات مرزی موجودی
     */
    @Nested
    @DisplayName("Inventory Management Tests")
    class InventoryManagementTests {
        
        /**
         * تست کاهش موفق موجودی با موجودی کافی
         * 
         * سناریو: کاهش موجودی آیتم هنگام ثبت سفارش
         * انتظار: موجودی کاهش یابد و true برگردانده شود
         */
        @Test
        @DisplayName("Should decrease quantity successfully")
        void decreaseQuantity_SufficientStock_Success() {
            // Given - آماده‌سازی: ایجاد آیتم با موجودی کافی
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            item.setQuantity(10);
            itemRepository.save(item);
            
            // When - عمل: کاهش 3 واحد از موجودی
            boolean result = itemService.decreaseQuantity(item.getId(), 3);
            
            // Then - بررسی: موفقیت عملیات و کاهش موجودی
            assertTrue(result);
            FoodItem updatedItem = itemService.getItem(item.getId());
            assertEquals(7, updatedItem.getQuantity());
        }
        
        /**
         * تست شکست در کاهش موجودی با موجودی ناکافی
         * 
         * سناریو: تلاش کاهش موجودی بیشتر از موجودی فعلی
         * انتظار: false برگردانده شود و موجودی تغییر نکند
         */
        @Test
        @DisplayName("Should fail to decrease quantity with insufficient stock")
        void decreaseQuantity_InsufficientStock_ReturnsFalse() {
            // Given - آماده‌سازی: ایجاد آیتم با موجودی کم
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            item.setQuantity(2);
            itemRepository.save(item);
            
            // When - عمل: تلاش کاهش موجودی بیشتر از موجود
            boolean result = itemService.decreaseQuantity(item.getId(), 5);
            
            // Then - بررسی: شکست عملیات و عدم تغییر موجودی
            assertFalse(result);
            FoodItem unchangedItem = itemService.getItem(item.getId());
            assertEquals(2, unchangedItem.getQuantity()); // بدون تغییر
        }
        
        /**
         * تست افزایش موفق موجودی
         * 
         * سناریو: افزایش موجودی آیتم هنگام تأمین مجدد
         * انتظار: موجودی افزایش یابد
         */
        @Test
        @DisplayName("Should increase quantity successfully")
        void increaseQuantity_ValidAmount_Success() {
            // Given - آماده‌سازی: ایجاد آیتم با موجودی اولیه
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            item.setQuantity(5);
            itemRepository.save(item);
            
            // When - عمل: افزایش 10 واحد به موجودی
            itemService.increaseQuantity(item.getId(), 10);
            
            // Then - بررسی: افزایش موجودی
            FoodItem updatedItem = itemService.getItem(item.getId());
            assertEquals(15, updatedItem.getQuantity());
        }
        
        /**
         * تست بررسی صحیح وضعیت موجودی
         * 
         * سناریو: بررسی آیتم‌ها با موجودی‌های مختلف
         * انتظار: گزارش صحیح وضعیت موجودی (موجود/ناموجود)
         */
        @Test
        @DisplayName("Should check stock status correctly")
        void isInStock_VariousQuantities_CorrectStatus() {
            // Given - آماده‌سازی: ایجاد آیتم‌ها با موجودی‌های مختلف
            Restaurant restaurant = createTestRestaurant();
            FoodItem inStockItem = createTestFoodItem(restaurant);
            inStockItem.setQuantity(5);
            itemRepository.save(inStockItem);
            
            FoodItem outOfStockItem = createTestFoodItem(restaurant);
            outOfStockItem.setQuantity(0);
            itemRepository.save(outOfStockItem);
            
            // When & Then - عمل و بررسی: تأیید وضعیت موجودی
            assertTrue(itemService.isInStock(inStockItem.getId()));
            assertFalse(itemService.isInStock(outOfStockItem.getId()));
        }
        
        /**
         * تست شناسایی آیتم‌های کم موجودی
         * 
         * سناریو: یافتن آیتم‌هایی که موجودی آن‌ها کمتر از حد آستانه است
         * انتظار: فقط آیتم‌های کم موجودی (quantity < 5) برگردانده شوند
         */
        @Test
        @DisplayName("Should get low stock items")
        void getLowStockItems_VariousQuantities_ReturnsLowStockOnly() {
            // Given - آماده‌سازی: ایجاد آیتم‌ها با موجودی‌های مختلف
            Restaurant restaurant = createTestRestaurant();
            FoodItem lowStock1 = createTestFoodItem(restaurant, "Item1", true);
            lowStock1.setQuantity(2);  // کم موجودی
            itemRepository.save(lowStock1);
            
            FoodItem lowStock2 = createTestFoodItem(restaurant, "Item2", true);
            lowStock2.setQuantity(4);  // کم موجودی
            itemRepository.save(lowStock2);
            
            FoodItem normalStock = createTestFoodItem(restaurant, "Item3", true);
            normalStock.setQuantity(10); // موجودی عادی
            itemRepository.save(normalStock);
            
            // When - عمل: دریافت آیتم‌های کم موجودی
            List<FoodItem> lowStockItems = itemService.getLowStockItems(restaurant.getId());
            
            // Then - بررسی: فقط آیتم‌های کم موجودی یافت شوند
            assertEquals(2, lowStockItems.size());
            assertTrue(lowStockItems.stream().allMatch(item -> item.getQuantity() < 5));
        }
    }
    
    /**
     * دسته تست‌های آمار منو
     * 
     * این کلاس عملکردهای آماری و گزارش‌گیری از منوی رستوران را تست می‌کند:
     * - محاسبه آمار کلی منو
     * - آمار فروش آیتم‌ها
     * - محبوب‌ترین آیتم‌ها
     * - آمار دسته‌بندی‌ها
     * - تحلیل عملکرد منو
     */
    @Nested
    @DisplayName("Menu Statistics Tests")
    class MenuStatisticsTests {
        
        /**
         * تست محاسبه صحیح آمار منو با آیتم‌های متنوع
         * 
         * سناریو: محاسبه آمار کامل منوی رستوران با وضعیت‌های مختلف آیتم‌ها
         * انتظار: محاسبه دقیق تمام شاخص‌های آماری
         */
        @Test
        @DisplayName("Should calculate menu statistics correctly")
        void getMenuStatistics_VariousItems_CorrectStatistics() {
            // Given - آماده‌سازی: ایجاد آیتم‌ها با وضعیت‌های مختلف
            Restaurant restaurant = createTestRestaurant();
            
            // در دسترس و دارای موجودی
            FoodItem item1 = FoodItem.forMenu("Item1", "Test description", 15.0, "Test Category", restaurant);
            item1.setQuantity(10);
            item1.setAvailable(true);
            item1 = itemRepository.saveNew(item1);
            
            // در دسترس اما کم موجودی
            FoodItem item2 = FoodItem.forMenu("Item2", "Test description", 20.0, "Test Category", restaurant);
            item2.setQuantity(2);
            item2.setAvailable(true);
            item2 = itemRepository.saveNew(item2);
            
            // غیر فعال
            FoodItem item3 = FoodItem.forMenu("Item3", "Test description", 10.0, "Test Category", restaurant);
            item3.setQuantity(5);
            item3.setAvailable(false);
            item3 = itemRepository.saveNew(item3);
            
            // When - عمل: محاسبه آمار منو
            ItemService.MenuStatistics stats = itemService.getMenuStatistics(restaurant.getId());
            
            // Then - بررسی: صحت تمام شاخص‌های آماری
            assertEquals(3, stats.getTotalItems());
            assertEquals(2, stats.getAvailableItems());
            assertEquals(2, stats.getInStockItems()); // فقط Item1 و Item2 هم فعال و هم موجود
            assertEquals(1, stats.getLowStockItems());
            assertEquals(15.0, stats.getAveragePrice(), 0.01); // (15+20+10)/3
        }
    }

    // =============== NEW COMPREHENSIVE EDGE CASE TESTS ===============

    /**
     * دسته تست‌های اعتبارسنجی ورودی
     * 
     * این کلاس سناریوهای مختلف مربوط به اعتبارسنجی داده‌های ورودی را تست می‌کند:
     * - ورودی‌های null
     * - رشته‌های خالی و فقط فاصله
     * - شناسه‌های نامعتبر
     * - فرمت‌های اشتباه داده
     */
    @Nested
    @DisplayName("Input Validation Edge Cases")
    class InputValidationTests {
        
        /**
         * تست خطا برای پارامترهای null در افزودن آیتم
         * 
         * سناریو: تلاش افزودن آیتم با مقادیر null برای فیلدهای اجباری
         * انتظار: پرتاب IllegalArgumentException برای هر فیلد null
         */
        @Test
        @DisplayName("Add item with null parameters throws exceptions")
        void addItem_nullParameters_throwsExceptions() {
            Restaurant restaurant = createTestRestaurant();
            
            // نام null
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), null, "Description", 10.0, "Category", null, 10)
            );
            
            // توضیحات null - سیستم این را به عنوان فیلد اجباری اعتبارسنجی می‌کند
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Valid Name", null, 10.0, "Category", null, 10)
            );
            
            // دسته‌بندی null
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Name", "Description", 10.0, null, null, 10)
            );
        }
        
        /**
         * تست خطا برای فیلدهای فقط شامل فاصله
         * 
         * سناریو: تلاش افزودن آیتم با رشته‌های فقط شامل فاصله
         * انتظار: پرتاب IllegalArgumentException برای رشته‌های خالی
         */
        @Test
        @DisplayName("Add item with whitespace-only fields throws exceptions")
        void addItem_whitespaceOnlyFields_throwsExceptions() {
            Restaurant restaurant = createTestRestaurant();
            
            // نام فقط فاصله
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "   ", "Description", 10.0, "Category", null, 10)
            );
            
            // دسته‌بندی فقط فاصله
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Name", "Description", 10.0, "   ", null, 10)
            );
        }

        /**
         * تست خطا برای شناسه‌های نامعتبر در به‌روزرسانی
         * 
         * سناریو: تلاش به‌روزرسانی آیتم با شناسه‌های غیرمعتبر
         * انتظار: پرتاب خطای مناسب برای هر نوع شناسه نامعتبر
         */
        @Test
        @DisplayName("Update item with invalid IDs throws exceptions")
        void updateItem_invalidIds_throwsExceptions() {
            // شناسه null
            assertThrows(IllegalArgumentException.class, () ->
                itemService.updateItem(null, "Name", "Description", 10.0, "Category", null, 10)
            );
            
            // شناسه صفر
            assertThrows(NotFoundException.class, () ->
                itemService.updateItem(0L, "Name", "Description", 10.0, "Category", null, 10)
            );
            
            // شناسه منفی
            assertThrows(NotFoundException.class, () ->
                itemService.updateItem(-1L, "Name", "Description", 10.0, "Category", null, 10)
            );
        }

        /**
         * تست خطا برای شناسه‌های نامعتبر در دریافت آیتم
         * 
         * سناریو: تلاش دریافت آیتم با شناسه‌های غیرمعتبر
         * انتظار: پرتاب خطای مناسب برای هر نوع شناسه نامعتبر
         */
        @Test
        @DisplayName("Get item with invalid IDs throws exceptions")
        void getItem_invalidIds_throwsExceptions() {
            // شناسه null
            assertThrows(IllegalArgumentException.class, () ->
                itemService.getItem(null)
            );
            
            // شناسه صفر
            assertThrows(NotFoundException.class, () ->
                itemService.getItem(0L)
            );
            
            // شناسه منفی
            assertThrows(NotFoundException.class, () ->
                itemService.getItem(-1L)
            );
        }

        /**
         * تست خطا برای کلیدواژه null در جستجو
         * 
         * سناریو: تلاش جستجو با کلیدواژه null
         * انتظار: پرتاب IllegalArgumentException
         */
        @Test
        @DisplayName("Search with null keyword throws exception")
        void searchItems_nullKeyword_throwsException() {
            assertThrows(IllegalArgumentException.class, () ->
                itemService.searchItems(null)
            );
        }

        /**
         * تست خطا برای کلیدواژه فقط شامل فاصله در جستجو
         * 
         * سناریو: تلاش جستجو با کلیدواژه فقط شامل فاصله
         * انتظار: پرتاب IllegalArgumentException
         */
        @Test
        @DisplayName("Search with whitespace-only keyword throws exception")
        void searchItems_whitespaceKeyword_throwsException() {
            assertThrows(IllegalArgumentException.class, () ->
                itemService.searchItems("   ")
            );
        }
    }

    /**
     * دسته تست‌های مقادیر مرزی
     * 
     * این کلاس سناریوهای مربوط به مقادیر حدی و مرزی را تست می‌کند:
     * - حداقل و حداکثر قیمت‌ها
     * - حداقل و حداکثر موجودی
     * - محدودیت‌های طول رشته
     * - عملیات موجودی در حالات مرزی
     */
    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {
        
        /**
         * تست افزودن آیتم با قیمت‌های مرزی
         * 
         * سناریو: آزمایش محدودیت‌های قیمت در حالات مختلف
         * انتظار: رد قیمت‌های غیرمعتبر و پذیرش قیمت‌های معتبر
         */
        @Test
        @DisplayName("Add item with boundary prices")
        void addItem_boundaryPrices_handledCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            
            // قیمت صفر باید رد شود
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Free Item", "Description", 0.0, "Category", null, 10)
            );
            
            // کمترین قیمت مثبت باید پذیرفته شود
            assertDoesNotThrow(() -> {
                FoodItem item = itemService.addItem(restaurant.getId(), "Cheap Item", "Description", 0.01, "Category", null, 10);
                assertEquals(0.01, item.getPrice(), 0.001);
            });
            
            // حداکثر قیمت باید پذیرفته شود
            assertDoesNotThrow(() -> {
                FoodItem item = itemService.addItem(restaurant.getId(), "Expensive Item", "Description", 9999.99, "Category", null, 10);
                assertEquals(9999.99, item.getPrice(), 0.01);
            });
            
            // بیشتر از حداکثر باید رد شود
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Too Expensive", "Description", 10000.01, "Category", null, 10)
            );
        }

        /**
         * تست افزودن آیتم با موجودی‌های مرزی
         * 
         * سناریو: آزمایش محدودیت‌های موجودی در حالات مختلف
         * انتظار: رد موجودی‌های منفی و پذیرش موجودی‌های معتبر
         */
        @Test
        @DisplayName("Add item with boundary quantities")
        void addItem_boundaryQuantities_handledCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            
            // موجودی صفر باید پذیرفته شود (ناموجود)
            assertDoesNotThrow(() -> {
                FoodItem item = itemService.addItem(restaurant.getId(), "Out of Stock", "Description", 10.0, "Category", null, 0);
                assertEquals(0, item.getQuantity());
            });
            
            // موجودی منفی باید رد شود
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Invalid Quantity", "Description", 10.0, "Category", null, -1)
            );
            
            // موجودی بسیار زیاد باید پذیرفته شود
            assertDoesNotThrow(() -> {
                FoodItem item = itemService.addItem(restaurant.getId(), "Bulk Item", "Description", 10.0, "Category", null, 999999);
                assertEquals(999999, item.getQuantity());
            });
        }

        /**
         * تست افزودن آیتم با فیلدهای متنی بسیار طولانی
         * 
         * سناریو: آزمایش محدودیت‌های طول رشته در فیلدهای مختلف
         * انتظار: رد رشته‌های طولانی‌تر از حد مجاز و پذیرش طول‌های معتبر
         */
        @Test
        @DisplayName("Add item with very long text fields validates length constraints")
        void addItem_veryLongTextFields_validatesLengthConstraints() {
            Restaurant restaurant = createTestRestaurant();
            
            // نام بسیار طولانی (بیش از 100 کاراکتر)
            String longName = "A".repeat(101);
            assertThrows(IllegalArgumentException.class, () -> {
                itemService.addItem(restaurant.getId(), longName, "Description", 10.0, "Category", null, 10);
            });
            
            // توضیحات بسیار طولانی (بیش از 500 کاراکتر)
            String longDescription = "A".repeat(501);
            assertThrows(IllegalArgumentException.class, () -> {
                itemService.addItem(restaurant.getId(), "Normal Name", longDescription, 10.0, "Category", null, 10);
            });
            
            // دسته‌بندی بسیار طولانی (بیش از 50 کاراکتر)
            String longCategory = "A".repeat(51);
            assertThrows(IllegalArgumentException.class, () -> {
                itemService.addItem(restaurant.getId(), "Normal Name", "Description", 10.0, longCategory, null, 10);
            });
            
            // طول‌های معتبر باید کار کنند
            String validName = "A".repeat(100); // دقیقاً حد مجاز
            String validDescription = "B".repeat(500); // دقیقاً حد مجاز  
            String validCategory = "C".repeat(50); // دقیقاً حد مجاز
            assertDoesNotThrow(() -> {
                itemService.addItem(restaurant.getId(), validName, validDescription, 10.0, validCategory, null, 10);
            });
        }

        /**
         * تست عملیات موجودی با مقادیر مرزی
         * 
         * سناریو: آزمایش کاهش و افزایش موجودی در حالات حدی
         * انتظار: مدیریت صحیح حالات مرزی مانند صفر شدن موجودی
         */
        @Test
        @DisplayName("Inventory operations with boundary values")
        void inventoryOperations_boundaryValues_handledCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            item.setQuantity(1);
            itemRepository.save(item);
            
            // کاهش دقیقاً تا صفر
            assertTrue(itemService.decreaseQuantity(item.getId(), 1));
            assertEquals(0, itemService.getItem(item.getId()).getQuantity());
            
            // تلاش کاهش وقتی که صفر است
            assertFalse(itemService.decreaseQuantity(item.getId(), 1));
            assertEquals(0, itemService.getItem(item.getId()).getQuantity());
            
            // افزایش از صفر
            itemService.increaseQuantity(item.getId(), 5);
            assertEquals(5, itemService.getItem(item.getId()).getQuantity());
            
            // افزایش بسیار زیاد
            itemService.increaseQuantity(item.getId(), 999999);
            assertEquals(1000004, itemService.getItem(item.getId()).getQuantity());
        }
    }

    /**
     * دسته تست‌های منطق تجاری حالات خاص
     * 
     * این کلاس سناریوهای پیچیده منطق تجاری را تست می‌کند:
     * - جستجو با کاراکترهای خاص
     * - حساسیت به حروف بزرگ و کوچک
     * - فیلترینگ پیشرفته آیتم‌ها
     * - آمار در حالات خاص
     * - مدیریت همزمان موجودی
     */
    @Nested
    @DisplayName("Business Logic Edge Cases")
    class BusinessLogicTests {
        
        /**
         * تست جستجو با کاراکترهای خاص
         * 
         * سناریو: جستجو آیتم‌هایی که شامل کاراکترهای خاص هستند
         * انتظار: یافتن صحیح آیتم‌ها با کاراکترهای خاص مختلف
         */
        @Test
        @DisplayName("Search items with special characters works correctly")
        void searchItems_specialCharacters_worksCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Café Américain", "Beverages", true);
            createTestFoodItem(restaurant, "Nacho's Special", "Mexican", true);
            createTestFoodItem(restaurant, "Fish & Chips", "British", true);
            
            // جستجو با کاراکترهای دارای accent
            List<FoodItem> results1 = itemService.searchItems("café");
            assertEquals(1, results1.size());
            assertEquals("Café Américain", results1.get(0).getName());
            
            // جستجو با آپستروف
            List<FoodItem> results2 = itemService.searchItems("nacho");
            assertEquals(1, results2.size());
            
            // جستجو با ampersand
            List<FoodItem> results3 = itemService.searchItems("fish");
            assertEquals(1, results3.size());
        }

        /**
         * تست عدم حساسیت به حروف بزرگ و کوچک در جستجو
         * 
         * سناریو: جستجو با ترکیبات مختلف حروف بزرگ و کوچک
         * انتظار: یافتن آیتم‌ها بدون توجه به حالت حروف
         */
        @Test
        @DisplayName("Case insensitive search works correctly")
        void searchItems_caseInsensitive_worksCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "HAMBURGER Deluxe", "Burgers", true);
            createTestFoodItem(restaurant, "chicken WINGS", "Appetizers", true);
            
            // جستجو با حالت‌های مختلف حروف
            assertEquals(1, itemService.searchItems("hamburger").size());
            assertEquals(1, itemService.searchItems("HAMBURGER").size());
            assertEquals(1, itemService.searchItems("HaMbUrGeR").size());
            
            assertEquals(1, itemService.searchItems("wings").size());
            assertEquals(1, itemService.searchItems("WINGS").size());
            assertEquals(1, itemService.searchItems("WiNgS").size());
        }

        /**
         * تست حساسیت به حروف بزرگ و کوچک در دسته‌بندی
         * 
         * سناریو: جستجو بر اساس دسته‌بندی با حالت‌های مختلف حروف
         * انتظار: جستجوی حساس به حالت حروف (تصمیم تجاری)
         */
        @Test
        @DisplayName("Get items by category with case sensitivity")
        void getItemsByCategory_caseSensitive_worksCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Item1", "Burgers", true);
            createTestFoodItem(restaurant, "Item2", "burgers", true);
            createTestFoodItem(restaurant, "Item3", "BURGERS", true);
            
            // جستجوی دسته‌بندی باید حساس به حالت حروف باشد (تصمیم تجاری)
            assertEquals(1, itemService.getItemsByCategory("Burgers").size());
            assertEquals(1, itemService.getItemsByCategory("burgers").size());
            assertEquals(1, itemService.getItemsByCategory("BURGERS").size());
        }

        /**
         * تست فیلترینگ آیتم‌های در دسترس
         * 
         * سناریو: فیلترینگ آیتم‌ها بر اساس وضعیت‌های مختلف availability و موجودی
         * انتظار: فیلترینگ صحیح بر اساس قوانین تجاری
         */
        @Test
        @DisplayName("Available items filtering works correctly")
        void availableItemsFiltering_variousStates_worksCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            
            // فعال و موجود
            FoodItem item1 = createTestFoodItem(restaurant, "Available In Stock", true);
            item1.setQuantity(10);
            itemRepository.save(item1);
            
            // فعال اما بدون موجودی
            FoodItem item2 = createTestFoodItem(restaurant, "Available No Stock", true);
            item2.setQuantity(0);
            itemRepository.save(item2);
            
            // غیرفعال اما دارای موجودی
            FoodItem item3 = createTestFoodItem(restaurant, "Not Available Has Stock", false);
            item3.setQuantity(5);
            itemRepository.save(item3);
            
            // غیرفعال و بدون موجودی
            FoodItem item4 = createTestFoodItem(restaurant, "Not Available No Stock", false);
            item4.setQuantity(0);
            itemRepository.save(item4);
            
            // دریافت تمام آیتم‌ها
            List<FoodItem> allItems = itemService.getRestaurantItems(restaurant.getId());
            assertEquals(4, allItems.size());
            
            // دریافت فقط آیتم‌های در دسترس - سیستم بر اساس available=true و quantity>0 فیلتر می‌کند
            List<FoodItem> availableItems = itemService.getAvailableItems(restaurant.getId());
            assertEquals(1, availableItems.size()); // فقط item1 هر دو شرط را دارد
            assertTrue(availableItems.stream().allMatch(FoodItem::getAvailable));
            assertTrue(availableItems.stream().allMatch(item -> item.getQuantity() > 0));
        }

        /**
         * تست آمار منو در حالات خاص
         * 
         * سناریو: محاسبه آمار برای رستوران خالی (بدون آیتم)
         * انتظار: برگرداندن آمار صفر برای رستوران خالی
         */
        @Test
        @DisplayName("Menu statistics with edge cases")
        void menuStatistics_edgeCases_calculatedCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            
            // When - عمل: دریافت آمار رستوران خالی
            ItemService.MenuStatistics emptyStats = itemService.getMenuStatistics(restaurant.getId());
            
            // Then - بررسی: رستوران خالی
            assertEquals(0, emptyStats.getTotalItems());
            assertEquals(0, emptyStats.getAvailableItems());
            assertEquals(0, emptyStats.getInStockItems());
            assertEquals(0, emptyStats.getLowStockItems());
            assertEquals(0.0, emptyStats.getAveragePrice(), 0.01);
        }

        /**
         * تست مدیریت همزمان تغییرات موجودی
         * 
         * سناریو: شبیه‌سازی تغییرات همزمان موجودی
         * انتظار: مدیریت ایمن تغییرات متوالی موجودی
         */
        @Test
        @DisplayName("Concurrent quantity modifications handled safely")
        void concurrentQuantityModifications_handledSafely() throws InterruptedException {
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            item.setQuantity(100);
            itemRepository.save(item);
            
            // این تست دسترسی همزمان را در یک thread شبیه‌سازی می‌کند
            // در سناریوی واقعی همزمان، synchronization مناسب نیاز است
            
            // کاهش‌های متعدد
            for (int i = 0; i < 10; i++) {
                assertTrue(itemService.decreaseQuantity(item.getId(), 5));
            }
            
            FoodItem updatedItem = itemService.getItem(item.getId());
            assertEquals(50, updatedItem.getQuantity());
            
            // تلاش کاهش بیشتر از موجود
            assertFalse(itemService.decreaseQuantity(item.getId(), 100));
            assertEquals(50, itemService.getItem(item.getId()).getQuantity());
        }
    }

    /**
     * دسته تست‌های یکپارچگی و سازگاری داده
     * 
     * این کلاس سناریوهای مربوط به یکپارچگی داده‌ها را تست می‌کند:
     * - حفظ روابط بین جداول
     * - جستجو در چندین رستوران
     * - تشخیص کم موجودی در سطح رستوران
     * - سازگاری داده‌ها پس از عملیات مختلف
     */
    @Nested
    @DisplayName("Integration and Data Consistency Tests")
    class DataConsistencyTests {
        
        /**
         * تست حفظ یکپارچگی روابط رستوران در عملیات آیتم
         * 
         * سناریو: اطمینان از حفظ ارتباط صحیح آیتم با رستوران پس از عملیات مختلف
         * انتظار: حفظ ارتباط رستوران در تمام عملیات
         */
        @Test
        @DisplayName("Item operations maintain restaurant relationship integrity")
        void itemOperations_maintainRestaurantRelationship() {
            Restaurant restaurant1 = createTestRestaurant();
            Restaurant restaurant2 = createTestRestaurant();
            
            // افزودن آیتم به رستوران1
            FoodItem item = itemService.addItem(restaurant1.getId(), "Test Item", "Description", 10.0, "Category", null, 10);
            assertEquals(restaurant1.getId(), item.getRestaurant().getId());
            
            // به‌روزرسانی آیتم - ارتباط رستوران باید باقی بماند
            FoodItem updatedItem = itemService.updateItem(item.getId(), "Updated Name", null, -1, null, null, null);
            assertEquals(restaurant1.getId(), updatedItem.getRestaurant().getId());
            
            // آیتم باید در لیست رستوران1 باشد، نه رستوران2
            List<FoodItem> restaurant1Items = itemService.getRestaurantItems(restaurant1.getId());
            List<FoodItem> restaurant2Items = itemService.getRestaurantItems(restaurant2.getId());
            
            assertEquals(1, restaurant1Items.size());
            assertEquals(0, restaurant2Items.size());
            assertEquals(item.getId(), restaurant1Items.get(0).getId());
        }

        /**
         * تست جستجو در چندین رستوران
         * 
         * سناریو: جستجوی آیتم‌ها در تمام رستوران‌ها همزمان
         * انتظار: یافتن آیتم‌ها از تمام رستوران‌های مختلف
         */
        @Test
        @DisplayName("Search across multiple restaurants works correctly")
        void searchAcrossRestaurants_worksCorrectly() {
            Restaurant restaurant1 = createTestRestaurant();
            Restaurant restaurant2 = createTestRestaurant();
            
            createTestFoodItem(restaurant1, "Pizza Margherita", "Pizza", true);
            createTestFoodItem(restaurant2, "Pizza Pepperoni", "Pizza", true);
            createTestFoodItem(restaurant1, "Burger Classic", "Burgers", true);
            
            // جستجو باید آیتم‌ها را از تمام رستوران‌ها برگرداند
            List<FoodItem> pizzaResults = itemService.searchItems("pizza");
            assertEquals(2, pizzaResults.size());
            
            List<FoodItem> burgerResults = itemService.searchItems("burger");
            assertEquals(1, burgerResults.size());
        }

        /**
         * تست تشخیص کم موجودی در سطح رستوران
         * 
         * سناریو: شناسایی آیتم‌های کم موجودی در یک رستوران
         * انتظار: گزارش صحیح آیتم‌های کم موجودی بدون توجه به وضعیت availability
         */
        @Test
        @DisplayName("Low stock detection across restaurant works correctly")
        void lowStockDetection_acrossRestaurant_worksCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            
            // ایجاد آیتم‌ها با سطوح موجودی مختلف
            FoodItem lowStock1 = createTestFoodItem(restaurant, "Low Stock 1", true);
            lowStock1.setQuantity(1);
            itemRepository.save(lowStock1);
            
            FoodItem lowStock2 = createTestFoodItem(restaurant, "Low Stock 2", true);
            lowStock2.setQuantity(4);
            itemRepository.save(lowStock2);
            
            FoodItem normalStock = createTestFoodItem(restaurant, "Normal Stock", true);
            normalStock.setQuantity(10);
            itemRepository.save(normalStock);
            
            FoodItem outOfStock = createTestFoodItem(restaurant, "Out of Stock", true);
            outOfStock.setQuantity(0);
            itemRepository.save(outOfStock);
            
            // Unavailable item (will be included in low stock detection as system doesn't filter by availability)
            FoodItem unavailableLowStock = createTestFoodItem(restaurant, "Unavailable Low", false);
            unavailableLowStock.setQuantity(2);
            itemRepository.save(unavailableLowStock);
            
            List<FoodItem> lowStockItems = itemService.getLowStockItems(restaurant.getId());
            
            // System includes all items with quantity < 5, regardless of availability
            assertEquals(4, lowStockItems.size()); // lowStock1, lowStock2, outOfStock, unavailableLowStock
            assertTrue(lowStockItems.stream().allMatch(item -> item.getQuantity() < 5));
        }
    }

    // Helper methods
    private Restaurant createTestRestaurant() {
        Restaurant restaurant = new Restaurant(
            1L, // ownerId
            "Test Restaurant",
            "123 Test St", 
            "1234567890"
        );
        restaurant.setStatus(RestaurantStatus.APPROVED);
        return restaurantRepository.saveNew(restaurant);
    }
    
    private FoodItem createTestFoodItem(Restaurant restaurant) {
        return createTestFoodItem(restaurant, "Test Burger", true);
    }
    
    private FoodItem createTestFoodItem(Restaurant restaurant, String name, boolean available) {
        FoodItem item = FoodItem.forMenu(name, "Test description", 12.99, "Test Category", restaurant);
        item.setQuantity(50);
        item.setAvailable(available);
        return itemRepository.saveNew(item);
    }
    
    private FoodItem createTestFoodItem(Restaurant restaurant, String name, String category, boolean available) {
        FoodItem item = FoodItem.forMenu(name, "Test description", 12.99, category, restaurant);
        item.setQuantity(50);
        item.setAvailable(available);
        return itemRepository.saveNew(item);
    }
}
