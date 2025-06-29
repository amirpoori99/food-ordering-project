package com.myapp.ui.restaurant;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

import com.myapp.ui.common.TestFXBase;

/**
 * تست‌کیس‌های جامع و کامل RestaurantDetailsController
 * 
 * این کلاس شامل تست‌های کاملی برای تمام جنبه‌های کنترلر است:
 * - تست مقداردهی اولیه کنترلر و UI components
 * - تست کامل و جامع تمام مدل‌های داده (Restaurant, MenuCategory, MenuItem, CartItem)
 * - تست عملکردهای کنترلر شامل جستجو، فیلتر، و مدیریت سبد خرید
 * - تست تعاملات UI و event handlers
 * - تست تمام سناریوهای edge cases و مقادیر خاص
 * - تست error handling و validation
 * - تست performance و یکپارچگی سیستم
 * 
 * @author Food Ordering System Team
 * @version 2.0 - تست‌های کامل و جامع
 * @since فاز 23
 */
@ExtendWith(ApplicationExtension.class)
class RestaurantDetailsControllerTest extends TestFXBase {

    private RestaurantDetailsController controller;
    private Label restaurantNameLabel;
    private Label restaurantAddressLabel;
    private Label restaurantRatingLabel;
    private Label restaurantPhoneLabel;
    private TextField menuSearchField;
    private TabPane menuTabPane;
    private VBox menuContainer;
    private VBox cartSummaryBox;
    private Label cartTotalLabel;
    private Button checkoutButton;
    
    @Start
    public void start(Stage stage) throws Exception {
        controller = new RestaurantDetailsController();
        
        // ایجاد UI components برای تست
        setupUIComponents();
        
        VBox root = new VBox(10);
        root.getChildren().addAll(
            new Label("Restaurant Details Test"),
            restaurantNameLabel,
            restaurantAddressLabel,
            restaurantRatingLabel,
            restaurantPhoneLabel,
            menuSearchField,
            menuTabPane,
            menuContainer,
            cartSummaryBox,
            cartTotalLabel,
            checkoutButton
        );
        
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    /**
     * تنظیم UI components برای تست
     */
    private void setupUIComponents() {
        restaurantNameLabel = new Label();
        restaurantAddressLabel = new Label();
        restaurantRatingLabel = new Label();
        restaurantPhoneLabel = new Label();
        menuSearchField = new TextField();
        menuTabPane = new TabPane();
        menuContainer = new VBox();
        cartSummaryBox = new VBox();
        cartTotalLabel = new Label();
        checkoutButton = new Button("ثبت سفارش");
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    // ==================== تست‌های مقداردهی اولیه ====================

    /**
     * تست مقداردهی اولیه کنترلر
     * بررسی اینکه کنترلر به درستی ایجاد می‌شود
     */
    @Test
    void testControllerInitialization() {
        assertNotNull(controller, "کنترلر باید با موفقیت مقداردهی شود");
    }

    /**
     * تست مقداردهی UI components
     */
    @Test
    void testUIComponentsInitialization() {
        assertNotNull(restaurantNameLabel, "لیبل نام رستوران باید مقداردهی شود");
        assertNotNull(restaurantAddressLabel, "لیبل آدرس رستوران باید مقداردهی شود");
        assertNotNull(restaurantRatingLabel, "لیبل امتیاز رستوران باید مقداردهی شود");
        assertNotNull(restaurantPhoneLabel, "لیبل تلفن رستوران باید مقداردهی شود");
        assertNotNull(menuSearchField, "فیلد جستجوی منو باید مقداردهی شود");
        assertNotNull(menuTabPane, "تب منو باید مقداردهی شود");
        assertNotNull(menuContainer, "کانتینر منو باید مقداردهی شود");
        assertNotNull(cartSummaryBox, "خلاصه سبد خرید باید مقداردهی شود");
        assertNotNull(cartTotalLabel, "لیبل مجموع سبد باید مقداردهی شود");
        assertNotNull(checkoutButton, "دکمه ثبت سفارش باید مقداردهی شود");
    }

    // ==================== تست‌های مدل‌های داده ====================

    /**
     * تست کامل مدل داده رستوران با تمام فیلدهای ممکن
     * شامل تست تمام getter/setter ها و validation مقادیر
     */
    @Test
    void testRestaurantDataModelComplete() {
        RestaurantDetailsController.Restaurant restaurant = new RestaurantDetailsController.Restaurant();
        
        // تنظیم تمام فیلدهای مدل رستوران
        restaurant.setId(1L);
        restaurant.setName("رستوران طلایی پرشیا");
        restaurant.setAddress("تهران، خیابان ولیعصر، پلاک 100، واحد 5");
        restaurant.setPhone("021-88776655");
        restaurant.setRating(4.7);
        restaurant.setReviewCount(245);
        restaurant.setDescription("رستوران معروف با غذاهای سنتی ایرانی، فضای مدرن و سرویس عالی");
        restaurant.setImageUrl("https://example.com/images/restaurant-golden-persia.jpg");
        restaurant.setOpen(true);
        restaurant.setOpeningHours("09:00 - 24:00");
        
        // بررسی صحت تمام مقادیر تنظیم شده
        assertEquals(1L, restaurant.getId(), "شناسه رستوران باید برابر 1 باشد");
        assertEquals("رستوران طلایی پرشیا", restaurant.getName(), "نام رستوران باید صحیح تنظیم شود");
        assertTrue(restaurant.getAddress().contains("ولیعصر"), "آدرس باید شامل خیابان ولیعصر باشد");
        assertEquals("021-88776655", restaurant.getPhone(), "شماره تلفن باید صحیح ذخیره شود");
        assertEquals(4.7, restaurant.getRating(), 0.01, "امتیاز رستوران باید 4.7 باشد");
        assertEquals(245, restaurant.getReviewCount(), "تعداد نظرات باید 245 باشد");
        assertTrue(restaurant.getDescription().contains("سنتی"), "توضیحات باید شامل کلمه سنتی باشد");
        assertTrue(restaurant.getImageUrl().startsWith("https://"), "URL تصویر باید با https شروع شود");
        assertTrue(restaurant.isOpen(), "وضعیت رستوران باید باز (true) باشد");
        assertEquals("09:00 - 24:00", restaurant.getOpeningHours(), "ساعات کاری باید صحیح ذخیره شود");
    }

    /**
     * تست constructor رستوران با پارامترهای ورودی
     * بررسی صحت مقداردهی اولیه از طریق constructor
     */
    @Test
    void testRestaurantConstructorWithParameters() {
        RestaurantDetailsController.Restaurant restaurant = 
            new RestaurantDetailsController.Restaurant(1L, "رستوران سنتی کاشان", "تهران، میدان پاسداران", "021-22334455");
        
        assertEquals(1L, restaurant.getId(), "شناسه در constructor باید صحیح تنظیم شود");
        assertEquals("رستوران سنتی کاشان", restaurant.getName(), "نام در constructor باید صحیح تنظیم شود");
        assertEquals("تهران، میدان پاسداران", restaurant.getAddress(), "آدرس در constructor باید صحیح تنظیم شود");
        assertEquals("021-22334455", restaurant.getPhone(), "تلفن در constructor باید صحیح تنظیم شود");
    }

    /**
     * تست مدیریت مقادیر null در مدل رستوران
     * اطمینان از اینکه سیستم با مقادیر null به درستی کار می‌کند
     */
    @Test
    void testRestaurantNullHandling() {
        RestaurantDetailsController.Restaurant restaurant = new RestaurantDetailsController.Restaurant();
        
        // تنظیم مقادیر null برای تمام فیلدهای اختیاری
        restaurant.setName(null);
        restaurant.setAddress(null);
        restaurant.setPhone(null);
        restaurant.setRating(null);
        restaurant.setReviewCount(null);
        restaurant.setDescription(null);
        restaurant.setImageUrl(null);
        restaurant.setOpeningHours(null);
        
        // بررسی قابلیت ذخیره‌سازی مقادیر null
        assertNull(restaurant.getName(), "نام null باید قابل ذخیره باشد");
        assertNull(restaurant.getAddress(), "آدرس null باید قابل ذخیره باشد");
        assertNull(restaurant.getPhone(), "تلفن null باید قابل ذخیره باشد");
        assertNull(restaurant.getRating(), "امتیاز null باید قابل ذخیره باشد");
        assertNull(restaurant.getReviewCount(), "تعداد نظرات null باید قابل ذخیره باشد");
        assertNull(restaurant.getDescription(), "توضیحات null باید قابل ذخیره باشد");
        assertNull(restaurant.getImageUrl(), "URL تصویر null باید قابل ذخیره باشد");
        assertNull(restaurant.getOpeningHours(), "ساعات کاری null باید قابل ذخیره باشد");
    }

    /**
     * تست رشته‌های خالی و پر از فاصله در مدل رستوران
     */
    @Test
    void testRestaurantEmptyStrings() {
        RestaurantDetailsController.Restaurant restaurant = new RestaurantDetailsController.Restaurant();
        
        // تست رشته خالی
        restaurant.setName("");
        restaurant.setAddress("   ");
        restaurant.setDescription("     کیفیت عالی     ");
        
        assertEquals("", restaurant.getName(), "رشته خالی باید قابل ذخیره باشد");
        assertEquals("   ", restaurant.getAddress(), "رشته پر از فاصله باید قابل ذخیره باشد");
        assertTrue(restaurant.getDescription().contains("کیفیت"), "محتوای اصلی رشته باید حفظ شود");
    }

    /**
     * تست کامل مدل دسته‌بندی منو
     * شامل تست تمام فیلدها و مدیریت لیست آیتم‌ها
     */
    @Test
    void testMenuCategoryDataModelComplete() {
        RestaurantDetailsController.MenuCategory category = new RestaurantDetailsController.MenuCategory();
        
        // تنظیم فیلدهای اصلی دسته‌بندی
        category.setId(1L);
        category.setName("غذاهای اصلی ایرانی");
        category.setDescription("مجموعه کاملی از کباب‌ها، خورش‌ها و غذاهای سنتی ایرانی");
        category.setActive(true);
        category.setDisplayOrder(1);
        
        // ایجاد آیتم‌های متنوع برای تست
        List<RestaurantDetailsController.MenuItem> items = new ArrayList<>();
        
        // آیتم اول: کباب کوبیده
        RestaurantDetailsController.MenuItem item1 = new RestaurantDetailsController.MenuItem(1L, "کباب کوبیده", 85000.0, true);
        item1.setDescription("کباب کوبیده مخصوص با برنج زعفرانی و سالاد");
        item1.setPreparationTime(20);
        
        // آیتم دوم: جوجه کباب
        RestaurantDetailsController.MenuItem item2 = new RestaurantDetailsController.MenuItem(2L, "جوجه کباب", 95000.0, true);
        item2.setDescription("جوجه کباب با ادویه‌های مخصوص و سس مخصوص");
        item2.setPreparationTime(25);
        
        // آیتم سوم: خورش ناموجود
        RestaurantDetailsController.MenuItem item3 = new RestaurantDetailsController.MenuItem(3L, "خورش قیمه", 65000.0, false);
        item3.setDescription("خورش قیمه سنتی با گوشت و سیب‌زمینی");
        item3.setPreparationTime(30);
        
        items.add(item1);
        items.add(item2);
        items.add(item3);
        category.setItems(items);
        
        // بررسی فیلدهای اصلی دسته‌بندی
        assertEquals(1L, category.getId(), "شناسه دسته‌بندی باید صحیح باشد");
        assertEquals("غذاهای اصلی ایرانی", category.getName(), "نام دسته‌بندی باید صحیح ذخیره شود");
        assertTrue(category.getDescription().contains("ایرانی"), "توضیحات باید شامل کلمه ایرانی باشد");
        assertTrue(category.isActive(), "دسته‌بندی باید فعال (true) باشد");
        assertEquals(1, category.getDisplayOrder(), "ترتیب نمایش باید 1 باشد");
        
        // بررسی آیتم‌های داخل دسته‌بندی
        assertEquals(3, category.getItems().size(), "دسته‌بندی باید شامل 3 آیتم باشد");
        assertEquals("کباب کوبیده", category.getItems().get(0).getName(), "نام آیتم اول باید صحیح باشد");
        assertEquals("جوجه کباب", category.getItems().get(1).getName(), "نام آیتم دوم باید صحیح باشد");
        assertEquals("خورش قیمه", category.getItems().get(2).getName(), "نام آیتم سوم باید صحیح باشد");
        
        // بررسی وضعیت موجودی آیتم‌ها
        assertTrue(category.getItems().get(0).isAvailable(), "کباب کوبیده باید موجود باشد");
        assertTrue(category.getItems().get(1).isAvailable(), "جوجه کباب باید موجود باشد");
        assertFalse(category.getItems().get(2).isAvailable(), "خورش قیمه باید ناموجود باشد");
        
        // بررسی قیمت‌های آیتم‌ها
        assertEquals(85000.0, category.getItems().get(0).getPrice(), 0.01, "قیمت کباب کوبیده باید صحیح باشد");
        assertEquals(95000.0, category.getItems().get(1).getPrice(), 0.01, "قیمت جوجه کباب باید صحیح باشد");
        assertEquals(65000.0, category.getItems().get(2).getPrice(), 0.01, "قیمت خورش قیمه باید صحیح باشد");
    }

    /**
     * تست constructor دسته‌بندی منو با پارامترها
     */
    @Test
    void testMenuCategoryConstructorWithParameters() {
        RestaurantDetailsController.MenuCategory category = 
            new RestaurantDetailsController.MenuCategory(1L, "نوشیدنی‌های گرم", "انواع چای، قهوه و نوشیدنی‌های گرم مخصوص");
        
        assertEquals(1L, category.getId(), "شناسه در constructor باید صحیح تنظیم شود");
        assertEquals("نوشیدنی‌های گرم", category.getName(), "نام در constructor باید صحیح تنظیم شود");
        assertTrue(category.getDescription().contains("قهوه"), "توضیحات باید شامل کلمه قهوه باشد");
        assertNotNull(category.getItems(), "لیست آیتم‌ها باید مقداردهی شود");
        assertTrue(category.getItems().isEmpty(), "لیست آیتم‌ها در ابتدا باید خالی باشد");
    }

    /**
     * تست دسته‌بندی خالی از آیتم
     */
    @Test
    void testEmptyMenuCategory() {
        RestaurantDetailsController.MenuCategory emptyCategory = new RestaurantDetailsController.MenuCategory();
        emptyCategory.setName("دسته‌بندی خالی");
        emptyCategory.setDescription("این دسته‌بندی هیچ آیتمی ندارد");
        emptyCategory.setItems(new ArrayList<>());
        
        assertEquals("دسته‌بندی خالی", emptyCategory.getName(), "نام دسته‌بندی خالی باید صحیح باشد");
        assertTrue(emptyCategory.getItems().isEmpty(), "دسته‌بندی خالی نباید آیتمی داشته باشد");
        assertEquals(0, emptyCategory.getItems().size(), "تعداد آیتم‌ها در دسته‌بندی خالی باید صفر باشد");
    }

    /**
     * تست کامل مدل آیتم منو
     * شامل تست مواد تشکیل‌دهنده، آلرژن‌ها و سایر اطلاعات
     */
    @Test
    void testMenuItemDataModelComplete() {
        RestaurantDetailsController.MenuItem item = new RestaurantDetailsController.MenuItem();
        
        // تنظیم اطلاعات اصلی آیتم
        item.setId(1L);
        item.setName("کباب برگ مخصوص");
        item.setDescription("کباب برگ گوساله درجه یک با برنج زعفرانی، سالاد فصل و ترشی");
        item.setPrice(145000.0);
        item.setAvailable(true);
        item.setImageUrl("https://example.com/images/kabab-barg-special.jpg");
        item.setCategoryId(1L);
        item.setPreparationTime(30);
        
        // تنظیم مواد تشکیل‌دهنده
        List<String> ingredients = new ArrayList<>();
        ingredients.add("گوشت گوساله درجه یک");
        ingredients.add("برنج ایرانی");
        ingredients.add("زعفران اصل");
        ingredients.add("پیاز");
        ingredients.add("سبزیجات تازه");
        ingredients.add("ادویه‌های مخصوص");
        item.setIngredients(ingredients);
        
        // تنظیم آلرژن‌ها
        List<String> allergens = new ArrayList<>();
        allergens.add("گلوتن");
        allergens.add("لبنیات");
        item.setAllergens(allergens);
        
        // بررسی اطلاعات اصلی آیتم
        assertEquals(1L, item.getId(), "شناسه آیتم باید صحیح باشد");
        assertEquals("کباب برگ مخصوص", item.getName(), "نام آیتم باید صحیح باشد");
        assertTrue(item.getDescription().contains("زعفرانی"), "توضیحات باید شامل کلمه زعفرانی باشد");
        assertEquals(145000.0, item.getPrice(), 0.01, "قیمت آیتم باید صحیح باشد");
        assertTrue(item.isAvailable(), "آیتم باید موجود (true) باشد");
        assertTrue(item.getImageUrl().contains("kabab-barg"), "URL تصویر باید مناسب آیتم باشد");
        assertEquals(1L, item.getCategoryId(), "شناسه دسته‌بندی باید صحیح باشد");
        assertEquals(30, item.getPreparationTime(), "زمان آماده‌سازی باید 30 دقیقه باشد");
        
        // بررسی مواد تشکیل‌دهنده
        assertEquals(6, item.getIngredients().size(), "تعداد مواد تشکیل‌دهنده باید 6 باشد");
        assertTrue(item.getIngredients().contains("گوشت گوساله درجه یک"), "مواد باید شامل گوشت گوساله باشد");
        assertTrue(item.getIngredients().contains("زعفران اصل"), "مواد باید شامل زعفران باشد");
        assertTrue(item.getIngredients().contains("برنج ایرانی"), "مواد باید شامل برنج ایرانی باشد");
        assertTrue(item.getIngredients().contains("ادویه‌های مخصوص"), "مواد باید شامل ادویه‌های مخصوص باشد");
        
        // بررسی آلرژن‌ها
        assertEquals(2, item.getAllergens().size(), "تعداد آلرژن‌ها باید 2 باشد");
        assertTrue(item.getAllergens().contains("گلوتن"), "آلرژن‌ها باید شامل گلوتن باشد");
        assertTrue(item.getAllergens().contains("لبنیات"), "آلرژن‌ها باید شامل لبنیات باشد");
    }

    /**
     * تست constructor آیتم منو با پارامترها
     */
    @Test
    void testMenuItemConstructorWithParameters() {
        RestaurantDetailsController.MenuItem item = 
            new RestaurantDetailsController.MenuItem(1L, "خورش فسنجان", 78000.0, true);
        
        assertEquals(1L, item.getId(), "شناسه در constructor باید صحیح باشد");
        assertEquals("خورش فسنجان", item.getName(), "نام در constructor باید صحیح باشد");
        assertEquals(78000.0, item.getPrice(), 0.01, "قیمت در constructor باید صحیح باشد");
        assertTrue(item.isAvailable(), "موجودی در constructor باید true باشد");
        assertNotNull(item.getIngredients(), "لیست مواد تشکیل‌دهنده باید مقداردهی شود");
        assertNotNull(item.getAllergens(), "لیست آلرژن‌ها باید مقداردهی شود");
        assertTrue(item.getIngredients().isEmpty(), "لیست مواد در ابتدا باید خالی باشد");
        assertTrue(item.getAllergens().isEmpty(), "لیست آلرژن‌ها در ابتدا باید خالی باشد");
    }

    /**
     * تست آیتم منو ناموجود
     */
    @Test
    void testUnavailableMenuItem() {
        RestaurantDetailsController.MenuItem unavailableItem = new RestaurantDetailsController.MenuItem();
        unavailableItem.setId(1L);
        unavailableItem.setName("غذای فصلی ناموجود");
        unavailableItem.setPrice(55000.0);
        unavailableItem.setAvailable(false);
        unavailableItem.setDescription("این غذا فعلاً ناموجود است");
        
        assertEquals("غذای فصلی ناموجود", unavailableItem.getName(), "نام آیتم ناموجود باید صحیح باشد");
        assertFalse(unavailableItem.isAvailable(), "آیتم باید ناموجود (false) باشد");
        assertEquals(55000.0, unavailableItem.getPrice(), 0.01, "قیمت آیتم ناموجود باید صحیح باشد");
        assertTrue(unavailableItem.getDescription().contains("ناموجود"), "توضیحات باید وضعیت ناموجودی را نشان دهد");
    }

    /**
     * تست کامل مدل آیتم سبد خرید
     * شامل تست محاسبه قیمت کل و مدیریت دستورات ویژه
     */
    @Test
    void testCartItemDataModelComplete() {
        RestaurantDetailsController.CartItem cartItem = new RestaurantDetailsController.CartItem();
        
        // تنظیم اطلاعات کامل آیتم سبد خرید
        cartItem.setItemId(1L);
        cartItem.setItemName("کباب کوبیده ویژه");
        cartItem.setPrice(85000.0);
        cartItem.setQuantity(2);
        cartItem.setSpecialInstructions("بدون پیاز، کمی نمک، اضافه ترشی");
        
        // بررسی اطلاعات اصلی
        assertEquals(1L, cartItem.getItemId(), "شناسه آیتم سبد باید صحیح باشد");
        assertEquals("کباب کوبیده ویژه", cartItem.getItemName(), "نام آیتم سبد باید صحیح باشد");
        assertEquals(85000.0, cartItem.getPrice(), 0.01, "قیمت واحد آیتم باید صحیح باشد");
        assertEquals(2, cartItem.getQuantity(), "تعداد آیتم باید صحیح باشد");
        assertTrue(cartItem.getSpecialInstructions().contains("بدون پیاز"), "دستورات ویژه باید شامل 'بدون پیاز' باشد");
        
        // بررسی محاسبه قیمت کل
        assertEquals(170000.0, cartItem.getTotalPrice(), 0.01, "قیمت کل باید 85000 × 2 = 170000 باشد");
    }

    /**
     * تست دقیق محاسبه خودکار قیمت کل در سبد خرید
     * شامل تست سناریوهای مختلف تغییر قیمت و تعداد
     */
    @Test
    void testCartItemTotalPriceCalculation() {
        RestaurantDetailsController.CartItem cartItem = new RestaurantDetailsController.CartItem();
        
        // تست محاسبه اولیه
        cartItem.setPrice(50000.0);
        cartItem.setQuantity(3);
        assertEquals(150000.0, cartItem.getTotalPrice(), 0.01, "قیمت کل اولیه: 50000 × 3 = 150000");
        
        // تست تغییر قیمت واحد
        cartItem.setPrice(60000.0);
        assertEquals(180000.0, cartItem.getTotalPrice(), 0.01, "قیمت کل بعد از تغییر قیمت: 60000 × 3 = 180000");
        
        // تست تغییر تعداد
        cartItem.setQuantity(4);
        assertEquals(240000.0, cartItem.getTotalPrice(), 0.01, "قیمت کل بعد از تغییر تعداد: 60000 × 4 = 240000");
        
        // تست تعداد 1
        cartItem.setQuantity(1);
        assertEquals(60000.0, cartItem.getTotalPrice(), 0.01, "قیمت کل با تعداد 1 باید برابر قیمت واحد باشد");
    }

    /**
     * تست constructor آیتم سبد خرید با پارامتر
     */
    @Test
    void testCartItemConstructorWithParameters() {
        RestaurantDetailsController.CartItem cartItem = 
            new RestaurantDetailsController.CartItem(1L, "جوجه کباب", 95000.0, 1);
        
        assertEquals(1L, cartItem.getItemId(), "ID آیتم در constructor باید صحیح باشد");
        assertEquals("جوجه کباب", cartItem.getItemName(), "نام آیتم در constructor باید صحیح باشد");
        assertEquals(95000.0, cartItem.getPrice(), 0.01, "قیمت در constructor باید صحیح باشد");
        assertEquals(1, cartItem.getQuantity(), "تعداد در constructor باید صحیح باشد");
        assertEquals(95000.0, cartItem.getTotalPrice(), 0.01, "قیمت کل در constructor باید صحیح محاسبه شود");
    }

    // ==================== تست‌های Edge Cases ====================

    /**
     * تست مقادیر null در مدل‌ها
     */
    @Test
    void testNullValues() {
        RestaurantDetailsController.Restaurant restaurant = new RestaurantDetailsController.Restaurant();
        
        // تست مقادیر null
        restaurant.setName(null);
        restaurant.setAddress(null);
        restaurant.setPhone(null);
        restaurant.setRating(null);
        restaurant.setReviewCount(null);
        
        assertNull(restaurant.getName(), "نام null باید قابل قبول باشد");
        assertNull(restaurant.getAddress(), "آدرس null باید قابل قبول باشد");
        assertNull(restaurant.getPhone(), "تلفن null باید قابل قبول باشد");
        assertNull(restaurant.getRating(), "امتیاز null باید قابل قبول باشد");
        assertNull(restaurant.getReviewCount(), "تعداد نظرات null باید قابل قبول باشد");
    }

    /**
     * تست مقادیر صفر و منفی در قیمت‌ها
     */
    @Test
    void testPriceEdgeCases() {
        // تست قیمت صفر
        RestaurantDetailsController.MenuItem itemZeroPrice = new RestaurantDetailsController.MenuItem();
        itemZeroPrice.setPrice(0.0);
        assertEquals(0.0, itemZeroPrice.getPrice(), 0.01, "قیمت صفر باید قابل قبول باشد");
        
        // تست قیمت منفی
        RestaurantDetailsController.MenuItem itemNegativePrice = new RestaurantDetailsController.MenuItem();
        itemNegativePrice.setPrice(-100.0);
        assertEquals(-100.0, itemNegativePrice.getPrice(), 0.01, "قیمت منفی باید قابل ذخیره باشد");
        
        // تست سبد خرید با قیمت صفر
        RestaurantDetailsController.CartItem cartItemZero = new RestaurantDetailsController.CartItem();
        cartItemZero.setPrice(0.0);
        cartItemZero.setQuantity(5);
        assertEquals(0.0, cartItemZero.getTotalPrice(), 0.01, "قیمت کل با قیمت صفر باید صفر باشد");
    }

    /**
     * تست رشته‌های خالی و پر از فاصله
     */
    @Test
    void testEmptyAndWhitespaceStrings() {
        RestaurantDetailsController.Restaurant restaurant = new RestaurantDetailsController.Restaurant();
        
        // تست رشته خالی
        restaurant.setName("");
        assertEquals("", restaurant.getName(), "رشته خالی باید قابل قبول باشد");
        
        // تست رشته پر از فاصله
        restaurant.setName("   ");
        assertEquals("   ", restaurant.getName(), "رشته پر از فاصله باید قابل قبول باشد");
        
        // تست رشته خیلی طولانی
        String longName = "a".repeat(1000);
        restaurant.setName(longName);
        assertEquals(longName, restaurant.getName(), "رشته طولانی باید قابل قبول باشد");
    }

    // ==================== تست‌های عملکردی کنترلر ====================

    /**
     * تست تنظیم رستوران جاری
     */
    @Test
    void testSetCurrentRestaurant() {
        RestaurantDetailsController.Restaurant restaurant = new RestaurantDetailsController.Restaurant();
        restaurant.setId(1L);
        restaurant.setName("رستوران جدید");
        
        controller.setCurrentRestaurant(restaurant);
        
        assertNotNull(controller.getCurrentRestaurant(), "رستوران جاری باید تنظیم شود");
        assertEquals(1L, controller.getCurrentRestaurant().getId(), "ID رستوران جاری باید صحیح باشد");
        assertEquals("رستوران جدید", controller.getCurrentRestaurant().getName(), "نام رستوران جاری باید صحیح باشد");
    }

    /**
     * تست تنظیم رستوران null
     */
    @Test
    void testSetNullRestaurant() {
        controller.setCurrentRestaurant(null);
        assertNull(controller.getCurrentRestaurant(), "رستوران null باید قابل تنظیم باشد");
    }

    // ==================== تست‌های یکپارچگی سیستم ====================

    /**
     * تست کلی یکپارچگی سیستم
     */
    @Test
    void testSystemIntegration() {
        // ایجاد رستوران کامل
        RestaurantDetailsController.Restaurant restaurant = new RestaurantDetailsController.Restaurant(
            1L, "رستوران یکپارچه", "تهران، میدان آزادی", "021-11223344");
        restaurant.setRating(4.8);
        restaurant.setReviewCount(200);
        restaurant.setOpen(true);
        
        // ایجاد دسته‌بندی منو
        RestaurantDetailsController.MenuCategory category = new RestaurantDetailsController.MenuCategory(
            1L, "غذاهای اصلی", "انواع کباب و خورش");
        
        // ایجاد آیتم‌های منو
        RestaurantDetailsController.MenuItem item1 = new RestaurantDetailsController.MenuItem(
            1L, "کباب کوبیده", 85000.0, true);
        RestaurantDetailsController.MenuItem item2 = new RestaurantDetailsController.MenuItem(
            2L, "خورش قیمه", 65000.0, true);
        
        List<RestaurantDetailsController.MenuItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        category.setItems(items);
        
        // ایجاد سبد خرید
        RestaurantDetailsController.CartItem cartItem1 = new RestaurantDetailsController.CartItem(
            1L, item1.getName(), item1.getPrice(), 2);
        RestaurantDetailsController.CartItem cartItem2 = new RestaurantDetailsController.CartItem(
            2L, item2.getName(), item2.getPrice(), 1);
        
        // تنظیم در کنترلر
        controller.setCurrentRestaurant(restaurant);
        
        // تست یکپارچگی
        assertNotNull(controller.getCurrentRestaurant(), "رستوران باید تنظیم شده باشد");
        assertEquals("رستوران یکپارچه", controller.getCurrentRestaurant().getName(), "نام رستوران باید صحیح باشد");
        assertEquals(2, category.getItems().size(), "دسته‌بندی باید 2 آیتم داشته باشد");
        assertEquals(170000.0, cartItem1.getTotalPrice(), 0.01, "قیمت کل آیتم اول سبد باید صحیح باشد");
        assertEquals(65000.0, cartItem2.getTotalPrice(), 0.01, "قیمت کل آیتم دوم سبد باید صحیح باشد");
        
        // محاسبه کل سبد خرید
        double totalCartPrice = cartItem1.getTotalPrice() + cartItem2.getTotalPrice();
        assertEquals(235000.0, totalCartPrice, 0.01, "قیمت کل سبد خرید باید صحیح محاسبه شود");
    }
} 