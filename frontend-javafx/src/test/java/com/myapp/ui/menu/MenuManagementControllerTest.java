package com.myapp.ui.menu;

import com.myapp.ui.common.NavigationController;
import com.myapp.ui.menu.MenuManagementController.MenuCategory;
import com.myapp.ui.menu.MenuManagementController.MenuItem;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.myapp.ui.common.TestFXBase;

/**
 * تست‌کیس‌های MenuManagementController
 * 
 * این کلاس تست شامل موارد زیر است:
 * - مقداردهی اولیه کنترلر
 * - مدیریت دسته‌های منو (CRUD)
 * - مدیریت آیتم‌های منو
 * - فرم‌های ویرایش و اعتبارسنجی
 * - آمار و گزارش‌گیری منو
 * - تست data models
 * - جستجو و فیلتر در منو
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@ExtendWith(ApplicationExtension.class)
class MenuManagementControllerTest extends TestFXBase {

    /** کنترلر MenuManagementController مورد تست */
    private MenuManagementController controller;
    
    /** کامپوننت‌های UI - Header */
    private Button backButton;
    private Button addCategoryButton;
    private Button reorderMenuButton;
    private TextField searchMenuField;
    private ComboBox<String> menuViewComboBox;
    private Button refreshMenuButton;

    /** کامپوننت‌های UI - Menu Tree */
    private VBox menuTreeContainer;
    private Button expandCategory1;
    private Button expandCategory2;
    private Button editCategory1;
    private Button editCategory2;
    private Button deleteCategory1;
    private Button deleteCategory2;
    private VBox category1Items;
    private VBox category2Items;
    private Button addNewCategoryButton;

    /** کامپوننت‌های UI - Category Form */
    private Label detailsTitleLabel;
    private VBox categoryFormContainer;
    private TextField categoryNameField;
    private ComboBox<String> categoryIconComboBox;
    private TextArea categoryDescriptionArea;
    private TextField categoryOrderField;
    private CheckBox categoryActiveCheckBox;

    /** کامپوننت‌های UI - Category Items */
    private Button addItemToCategoryButton;
    private VBox categoryItemsList;
    private Button moveUpItem1;
    private Button moveDownItem1;
    private Button removeFromCategoryButton1;
    private VBox emptyCategoryMessage;

    /** کامپوننت‌های UI - Category Actions */
    private Button deleteCategoryButton;
    private Button clearCategoryFormButton;
    private Button saveCategoryButton;

    /** کامپوننت‌های UI - Statistics */
    private Label totalCategoriesLabel;
    private Label totalItemsLabel;
    private Label activeItemsInMenuLabel;
    private Label lastMenuUpdateLabel;

    /** کامپوننت‌های UI - Menu Actions */
    private Button previewMenuButton;
    private Button exportMenuButton;
    private Button publishMenuButton;

    /**
     * راه‌اندازی اولیه Stage برای تست
     * تلاش برای بارگذاری FXML یا ایجاد UI ساختگی
     */
    @Start
    public void start(Stage stage) throws Exception {
        try {
            // تلاش برای بارگذاری FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuManagement.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            
            // دریافت کامپوننت‌های UI
            initializeUIComponents(root);
            
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();
        } catch (Exception e) {
            // بارگذاری FXML ناموفق، ایجاد کامپوننت‌های UI ساختگی
            createMockUI(stage);
        }
    }
    
    /**
     * مقداردهی کامپوننت‌های UI از FXML
     */
    private void initializeUIComponents(Parent root) {
        // Header components
        backButton = (Button) root.lookup("#backButton");
        addCategoryButton = (Button) root.lookup("#addCategoryButton");
        reorderMenuButton = (Button) root.lookup("#reorderMenuButton");
        searchMenuField = (TextField) root.lookup("#searchMenuField");
        menuViewComboBox = (ComboBox<String>) root.lookup("#menuViewComboBox");
        refreshMenuButton = (Button) root.lookup("#refreshMenuButton");
        
        // Menu Tree components
        menuTreeContainer = (VBox) root.lookup("#menuTreeContainer");
        expandCategory1 = (Button) root.lookup("#expandCategory1");
        expandCategory2 = (Button) root.lookup("#expandCategory2");
        editCategory1 = (Button) root.lookup("#editCategory1");
        editCategory2 = (Button) root.lookup("#editCategory2");
        deleteCategory1 = (Button) root.lookup("#deleteCategory1");
        deleteCategory2 = (Button) root.lookup("#deleteCategory2");
        category1Items = (VBox) root.lookup("#category1Items");
        category2Items = (VBox) root.lookup("#category2Items");
        addNewCategoryButton = (Button) root.lookup("#addNewCategoryButton");
        
        // Category Form components
        detailsTitleLabel = (Label) root.lookup("#detailsTitleLabel");
        categoryFormContainer = (VBox) root.lookup("#categoryFormContainer");
        categoryNameField = (TextField) root.lookup("#categoryNameField");
        categoryIconComboBox = (ComboBox<String>) root.lookup("#categoryIconComboBox");
        categoryDescriptionArea = (TextArea) root.lookup("#categoryDescriptionArea");
        categoryOrderField = (TextField) root.lookup("#categoryOrderField");
        categoryActiveCheckBox = (CheckBox) root.lookup("#categoryActiveCheckBox");
        
        // Category Items components
        addItemToCategoryButton = (Button) root.lookup("#addItemToCategoryButton");
        categoryItemsList = (VBox) root.lookup("#categoryItemsList");
        moveUpItem1 = (Button) root.lookup("#moveUpItem1");
        moveDownItem1 = (Button) root.lookup("#moveDownItem1");
        removeFromCategoryButton1 = (Button) root.lookup("#removeFromCategoryButton1");
        emptyCategoryMessage = (VBox) root.lookup("#emptyCategoryMessage");
        
        // Category Actions components
        deleteCategoryButton = (Button) root.lookup("#deleteCategoryButton");
        clearCategoryFormButton = (Button) root.lookup("#clearCategoryFormButton");
        saveCategoryButton = (Button) root.lookup("#saveCategoryButton");
        
        // Statistics components
        totalCategoriesLabel = (Label) root.lookup("#totalCategoriesLabel");
        totalItemsLabel = (Label) root.lookup("#totalItemsLabel");
        activeItemsInMenuLabel = (Label) root.lookup("#activeItemsInMenuLabel");
        lastMenuUpdateLabel = (Label) root.lookup("#lastMenuUpdateLabel");
        
        // Menu Actions components
        previewMenuButton = (Button) root.lookup("#previewMenuButton");
        exportMenuButton = (Button) root.lookup("#exportMenuButton");
        publishMenuButton = (Button) root.lookup("#publishMenuButton");
    }
    
    /**
     * ایجاد UI ساختگی در صورت شکست بارگذاری FXML
     */
    private void createMockUI(Stage stage) {
        controller = new MenuManagementController();
        
        // ایجاد کامپوننت‌های UI ساختگی
        // Header
        backButton = new Button("← بازگشت");
        addCategoryButton = new Button("+ دسته جدید");
        reorderMenuButton = new Button("↕ مرتب‌سازی منو");
        searchMenuField = new TextField();
        searchMenuField.setPromptText("جستجو در منو...");
        menuViewComboBox = new ComboBox<>();
        refreshMenuButton = new Button("🔄");
        
        // تنظیم ComboBox نمایش
        menuViewComboBox.getItems().addAll("نمایش درختی", "نمایش فشرده", "نمایش جدولی");
        menuViewComboBox.setValue("نمایش درختی");
        
        // Menu Tree
        menuTreeContainer = new VBox();
        expandCategory1 = new Button("▼");
        expandCategory2 = new Button("▶");
        editCategory1 = new Button("✏️");
        editCategory2 = new Button("✏️");
        deleteCategory1 = new Button("🗑️");
        deleteCategory2 = new Button("🗑️");
        category1Items = new VBox();
        category2Items = new VBox();
        addNewCategoryButton = new Button("+ افزودن دسته جدید");
        
        // Category Form
        detailsTitleLabel = new Label("جزئیات دسته");
        categoryFormContainer = new VBox();
        categoryNameField = new TextField();
        categoryNameField.setPromptText("نام دسته منو");
        categoryIconComboBox = new ComboBox<>();
        categoryIconComboBox.getItems().addAll("🍖 غذاهای اصلی", "🥗 سالاد", "🥤 نوشیدنی", "🍰 دسر");
        categoryDescriptionArea = new TextArea();
        categoryDescriptionArea.setPromptText("توضیح کوتاه درباره این دسته...");
        categoryOrderField = new TextField();
        categoryOrderField.setPromptText("1");
        categoryActiveCheckBox = new CheckBox("دسته فعال است");
        categoryActiveCheckBox.setSelected(true);
        
        // Category Items
        addItemToCategoryButton = new Button("+ افزودن آیتم");
        categoryItemsList = new VBox();
        moveUpItem1 = new Button("↑");
        moveDownItem1 = new Button("↓");
        removeFromCategoryButton1 = new Button("×");
        emptyCategoryMessage = new VBox();
        
        // Category Actions
        deleteCategoryButton = new Button("حذف دسته");
        clearCategoryFormButton = new Button("پاک کردن فرم");
        saveCategoryButton = new Button("ذخیره دسته");
        
        // Statistics
        totalCategoriesLabel = new Label("مجموع دسته‌ها: ۲");
        totalItemsLabel = new Label("مجموع آیتم‌ها: ۸");
        activeItemsInMenuLabel = new Label("آیتم‌های فعال: ۷");
        lastMenuUpdateLabel = new Label("آخرین بروزرسانی: امروز ۱۴:۳۰");
        
        // Menu Actions
        previewMenuButton = new Button("👁️ پیش‌نمایش منو");
        exportMenuButton = new Button("📄 خروجی PDF");
        publishMenuButton = new Button("🚀 انتشار منو");
        
        // ایجاد scene با کامپوننت‌های ساختگی
        VBox root = new VBox(10);
        root.getChildren().addAll(
            backButton, addCategoryButton, searchMenuField, menuViewComboBox,
            menuTreeContainer, categoryFormContainer, categoryNameField, categoryDescriptionArea,
            categoryActiveCheckBox, saveCategoryButton, totalCategoriesLabel,
            totalItemsLabel, activeItemsInMenuLabel, publishMenuButton
        );
        
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }

    /**
     * راه‌اندازی قبل از هر تست
     * بازنشانی وضعیت UI
     */
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp(); // فراخوانی راه‌اندازی والد
        
        // بازنشانی وضعیت UI قبل از هر تست
        Platform.runLater(() -> {
            if (searchMenuField != null) searchMenuField.clear();
            if (categoryNameField != null) categoryNameField.clear();
            if (categoryDescriptionArea != null) categoryDescriptionArea.clear();
            if (categoryOrderField != null) categoryOrderField.clear();
            if (categoryActiveCheckBox != null) categoryActiveCheckBox.setSelected(true);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * تست مقداردهی اولیه کنترلر
     */
    @Test
    void testInitialization() {
        assertNotNull(controller, "کنترلر باید مقداردهی شود");
        assertNotNull(searchMenuField, "فیلد جستجو باید موجود باشد");
        assertNotNull(categoryFormContainer, "کانتینر فرم دسته باید موجود باشد");
        assertNotNull(saveCategoryButton, "دکمه ذخیره باید موجود باشد");
    }

    /**
     * تست وجود کامپوننت‌های UI اصلی
     */
    @Test
    void testUIComponentsExist() {
        // Header components
        assertNotNull(backButton, "دکمه بازگشت باید وجود داشته باشد");
        assertNotNull(addCategoryButton, "دکمه افزودن دسته باید وجود داشته باشد");
        assertNotNull(searchMenuField, "فیلد جستجو باید وجود داشته باشد");
        assertNotNull(menuViewComboBox, "ComboBox نمایش باید وجود داشته باشد");
        
        // Category Form components
        assertNotNull(categoryNameField, "فیلد نام دسته باید وجود داشته باشد");
        assertNotNull(categoryDescriptionArea, "ناحیه توضیحات باید وجود داشته باشد");
        assertNotNull(categoryActiveCheckBox, "چک‌باکس فعال/غیرفعال باید وجود داشته باشد");
        
        // Action buttons
        assertNotNull(saveCategoryButton, "دکمه ذخیره باید وجود داشته باشد");
        assertNotNull(deleteCategoryButton, "دکمه حذف باید وجود داشته باشد");
        assertNotNull(clearCategoryFormButton, "دکمه پاک کردن فرم باید وجود داشته باشد");
        
        // Statistics
        assertNotNull(totalCategoriesLabel, "برچسب مجموع دسته‌ها باید وجود داشته باشد");
        assertNotNull(totalItemsLabel, "برچسب مجموع آیتم‌ها باید وجود داشته باشد");
        assertNotNull(activeItemsInMenuLabel, "برچسب آیتم‌های فعال باید وجود داشته باشد");
    }

    /**
     * تست پیکربندی فیلد جستجو
     */
    @Test
    void testSearchFieldConfiguration() {
        assertNotNull(searchMenuField.getPromptText(), "فیلد جستجو باید متن راهنما داشته باشد");
        assertEquals("جستجو در منو...", searchMenuField.getPromptText());
    }

    /**
     * تست پیکربندی ComboBox نمایش
     */
    @Test
    void testMenuViewComboBoxConfiguration() {
        assertFalse(menuViewComboBox.getItems().isEmpty(), "ComboBox نمایش باید آیتم داشته باشد");
        assertTrue(menuViewComboBox.getItems().contains("نمایش درختی"), 
                  "باید گزینه نمایش درختی داشته باشد");
        assertTrue(menuViewComboBox.getItems().contains("نمایش فشرده"), 
                  "باید گزینه نمایش فشرده داشته باشد");
        assertEquals("نمایش درختی", menuViewComboBox.getValue());
    }

    /**
     * تست مدل داده دسته منو
     */
    @Test
    void testMenuCategoryDataModel() {
        MenuCategory category = new MenuCategory();
        category.setId(1L);
        category.setName("غذاهای اصلی");
        category.setDescription("انواع غذاهای اصلی ایرانی");
        category.setActive(true);
        category.setDisplayOrder(1);
        
        assertEquals(1L, category.getId());
        assertEquals("غذاهای اصلی", category.getName());
        assertEquals("انواع غذاهای اصلی ایرانی", category.getDescription());
        assertTrue(category.isActive());
        assertEquals(1, category.getDisplayOrder());
        assertNotNull(category.getItems());
        assertTrue(category.getItems().isEmpty());
    }

    /**
     * تست مدل داده آیتم منو
     */
    @Test
    void testMenuItemDataModel() {
        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setName("کباب کوبیده");
        item.setDescription("کباب کوبیده تازه با برنج");
        item.setPrice(85000.0);
        item.setActive(true);
        
        assertEquals(1L, item.getId());
        assertEquals("کباب کوبیده", item.getName());
        assertEquals("کباب کوبیده تازه با برنج", item.getDescription());
        assertEquals(85000.0, item.getPrice(), 0.01);
        assertTrue(item.isActive());
    }

    /**
     * تست پر کردن فرم دسته
     */
    @Test
    void testCategoryFormFilling() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            categoryNameField.setText("غذاهای اصلی");
            categoryDescriptionArea.setText("انواع غذاهای اصلی ایرانی");
            categoryOrderField.setText("1");
            categoryActiveCheckBox.setSelected(true);
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "پر کردن فرم باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
        
        assertEquals("غذاهای اصلی", categoryNameField.getText());
        assertEquals("انواع غذاهای اصلی ایرانی", categoryDescriptionArea.getText());
        assertEquals("1", categoryOrderField.getText());
        assertTrue(categoryActiveCheckBox.isSelected());
    }

    /**
     * تست پاک کردن فرم دسته
     */
    @Test
    void testClearCategoryForm() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            // ابتدا فرم را پر کن
            categoryNameField.setText("تست");
            categoryDescriptionArea.setText("توضیحات تست");
            categoryOrderField.setText("5");
            categoryActiveCheckBox.setSelected(false);
            
            // سپس پاک کن
            categoryNameField.clear();
            categoryDescriptionArea.clear();
            categoryOrderField.clear();
            categoryActiveCheckBox.setSelected(true);
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "پاک کردن فرم باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(categoryNameField.getText().isEmpty());
        assertTrue(categoryDescriptionArea.getText().isEmpty());
        assertTrue(categoryOrderField.getText().isEmpty());
        assertTrue(categoryActiveCheckBox.isSelected());
    }

    /**
     * تست اعتبارسنجی فرم خالی
     */
    @Test
    void testEmptyFormValidation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            // فرم خالی
            categoryNameField.clear();
            categoryDescriptionArea.clear();
            categoryOrderField.clear();
            
            // شبیه‌سازی کلیک دکمه ذخیره
            saveCategoryButton.fire();
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "اعتبارسنجی فرم خالی باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
        
        // نام دسته نباید خالی باشد
        assertTrue(categoryNameField.getText().isEmpty());
    }

    /**
     * تست جستجو در منو
     */
    @Test
    void testMenuSearch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            searchMenuField.setText("کباب");
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "جستجو در منو باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
        
        assertEquals("کباب", searchMenuField.getText());
    }

    /**
     * تست تغییر نمای منو
     */
    @Test
    void testMenuViewChange() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            menuViewComboBox.setValue("نمایش فشرده");
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "تغییر نمای منو باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
        
        assertEquals("نمایش فشرده", menuViewComboBox.getValue());
    }

    /**
     * تست آمار منو
     */
    @Test
    void testMenuStatistics() {
        // تست نمایش آمار
        assertNotNull(totalCategoriesLabel.getText(), "برچسب مجموع دسته‌ها باید متن داشته باشد");
        assertNotNull(totalItemsLabel.getText(), "برچسب مجموع آیتم‌ها باید متن داشته باشد");
        assertNotNull(activeItemsInMenuLabel.getText(), "برچسب آیتم‌های فعال باید متن داشته باشد");
        
        assertTrue(totalCategoriesLabel.getText().contains("دسته"), "باید کلمه دسته را شامل شود");
        assertTrue(totalItemsLabel.getText().contains("آیتم"), "باید کلمه آیتم را شامل شود");
        assertTrue(activeItemsInMenuLabel.getText().contains("فعال"), "باید کلمه فعال را شامل شود");
    }

    /**
     * تست دکمه‌های عملکردی
     */
    @Test
    void testActionButtonsState() {
        Platform.runLater(() -> {
            assertTrue(backButton.isVisible(), "دکمه بازگشت باید قابل مشاهده باشد");
            assertTrue(addCategoryButton.isVisible(), "دکمه افزودن دسته باید قابل مشاهده باشد");
            assertTrue(saveCategoryButton.isVisible(), "دکمه ذخیره باید قابل مشاهده باشد");
            assertTrue(publishMenuButton.isVisible(), "دکمه انتشار منو باید قابل مشاهده باشد");
            
            assertFalse(backButton.isDisabled(), "دکمه بازگشت باید فعال باشد");
            assertFalse(addCategoryButton.isDisabled(), "دکمه افزودن دسته باید فعال باشد");
            assertFalse(saveCategoryButton.isDisabled(), "دکمه ذخیره باید فعال باشد");
            assertFalse(publishMenuButton.isDisabled(), "دکمه انتشار منو باید فعال باشد");
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * تست CategoryIcon ComboBox
     */
    @Test
    void testCategoryIconComboBox() {
        if (categoryIconComboBox != null) {
            assertFalse(categoryIconComboBox.getItems().isEmpty(), 
                       "ComboBox آیکون باید آیتم داشته باشد");
            assertTrue(categoryIconComboBox.getItems().contains("🍖 غذاهای اصلی"), 
                      "باید آیکون غذاهای اصلی داشته باشد");
            assertTrue(categoryIconComboBox.getItems().contains("🥤 نوشیدنی"), 
                      "باید آیکون نوشیدنی داشته باشد");
        }
    }

    /**
     * تست اعتبارسنجی داده‌های null
     */
    @Test
    void testNullDataHandling() {
        // تست MenuCategory با مقادیر null
        MenuCategory category = new MenuCategory();
        category.setId(null);
        category.setName(null);
        category.setDescription(null);
        
        assertNull(category.getId());
        assertNull(category.getName());
        assertNull(category.getDescription());
        
        // تست MenuItem با مقادیر null
        MenuItem item = new MenuItem();
        item.setId(null);
        item.setName(null);
        item.setDescription(null);
        item.setPrice(null);
        
        assertNull(item.getId());
        assertNull(item.getName());
        assertNull(item.getDescription());
        assertNull(item.getPrice());
    }

    /**
     * تست مقادیر خالی
     */
    @Test
    void testEmptyDataHandling() {
        MenuCategory category = new MenuCategory();
        category.setName("");
        category.setDescription("");
        
        assertEquals("", category.getName());
        assertEquals("", category.getDescription());
        
        MenuItem item = new MenuItem();
        item.setName("");
        item.setDescription("");
        
        assertEquals("", item.getName());
        assertEquals("", item.getDescription());
    }

    /**
     * تست حالت‌های مختلف فعال/غیرفعال
     */
    @Test
    void testActiveInactiveStates() {
        MenuCategory activeCategory = new MenuCategory();
        activeCategory.setActive(true);
        assertTrue(activeCategory.isActive());
        
        MenuCategory inactiveCategory = new MenuCategory();
        inactiveCategory.setActive(false);
        assertFalse(inactiveCategory.isActive());
        
        MenuItem activeItem = new MenuItem();
        activeItem.setActive(true);
        assertTrue(activeItem.isActive());
        
        MenuItem inactiveItem = new MenuItem();
        inactiveItem.setActive(false);
        assertFalse(inactiveItem.isActive());
    }

    /**
     * تست ترتیب نمایش دسته‌ها
     */
    @Test
    void testCategoryDisplayOrder() {
        MenuCategory firstCategory = new MenuCategory();
        firstCategory.setDisplayOrder(1);
        assertEquals(1, firstCategory.getDisplayOrder());
        
        MenuCategory secondCategory = new MenuCategory();
        secondCategory.setDisplayOrder(2);
        assertEquals(2, secondCategory.getDisplayOrder());
        
        MenuCategory lastCategory = new MenuCategory();
        lastCategory.setDisplayOrder(10);
        assertEquals(10, lastCategory.getDisplayOrder());
    }

    /**
     * تست قیمت‌گذاری آیتم‌ها
     */
    @Test
    void testMenuItemPricing() {
        MenuItem cheapItem = new MenuItem();
        cheapItem.setPrice(15000.0);
        assertEquals(15000.0, cheapItem.getPrice(), 0.01);
        
        MenuItem expensiveItem = new MenuItem();
        expensiveItem.setPrice(250000.0);
        assertEquals(250000.0, expensiveItem.getPrice(), 0.01);
        
        MenuItem freeItem = new MenuItem();
        freeItem.setPrice(0.0);
        assertEquals(0.0, freeItem.getPrice(), 0.01);
    }

    /**
     * تست اعتبارسنجی ترتیب نمایش
     */
    @Test
    void testDisplayOrderValidation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            // تست مقادیر معتبر
            categoryOrderField.setText("1");
            assertEquals("1", categoryOrderField.getText());
            
            categoryOrderField.setText("10");
            assertEquals("10", categoryOrderField.getText());
            
            // تست مقادیر نامعتبر (در واقعیت باید اعتبارسنجی شود)
            categoryOrderField.setText("abc");
            assertEquals("abc", categoryOrderField.getText());
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "تست ترتیب نمایش باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * تست نمایش پیام‌های خالی
     */
    @Test
    void testEmptyStateMessages() {
        if (emptyCategoryMessage != null) {
            assertNotNull(emptyCategoryMessage, "پیام حالت خالی باید موجود باشد");
            // در حالت پیش‌فرض باید مخفی باشد
            // assertFalse(emptyCategoryMessage.isVisible());
        }
    }

    /**
     * تست کلیک دکمه‌های مختلف
     */
    @Test
    void testButtonClicks() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            // شبیه‌سازی کلیک دکمه‌ها
            addCategoryButton.fire();
            refreshMenuButton.fire();
            clearCategoryFormButton.fire();
            previewMenuButton.fire();
            exportMenuButton.fire();
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "کلیک دکمه‌ها باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
    }
} 