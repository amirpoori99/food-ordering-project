package com.myapp.ui.menu;

import com.myapp.ui.common.HttpClientUtil;
import com.myapp.ui.common.NavigationController;
import com.myapp.ui.common.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * کنترلر مدیریت منوی رستوران
 * 
 * این کلاس مسئول مدیریت منوی رستوران توسط صاحب رستوران شامل:
 * - مدیریت دسته‌های منو (ایجاد، ویرایش، حذف)
 * - مدیریت آیتم‌های منو در هر دسته
 * - مرتب‌سازی و تنظیم ترتیب نمایش
 * - فعال/غیرفعال کردن دسته‌ها و آیتم‌ها
 * - پیش‌نمایش و انتشار منو
 * 
 * ویژگی‌ها:
 * - رابط کاربری درختی برای نمایش ساختار منو
 * - فرم‌های ویرایش تعاملی
 * - جستجو و فیلتر در منو
 * - آمار و گزارش‌گیری از منو
 * - خروجی PDF از منو
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class MenuManagementController implements Initializable {

    // UI Components - Header
    @FXML private Button backButton;
    @FXML private Button addCategoryButton;
    @FXML private Button reorderMenuButton;
    @FXML private TextField searchMenuField;
    @FXML private ComboBox<String> menuViewComboBox;
    @FXML private Button refreshMenuButton;

    // UI Components - Menu Tree
    @FXML private VBox menuTreeContainer;
    @FXML private Button expandCategory1;
    @FXML private Button expandCategory2;
    @FXML private Button editCategory1;
    @FXML private Button editCategory2;
    @FXML private Button deleteCategory1;
    @FXML private Button deleteCategory2;
    @FXML private VBox category1Items;
    @FXML private VBox category2Items;
    @FXML private Button addNewCategoryButton;

    // UI Components - Category Form
    @FXML private Label detailsTitleLabel;
    @FXML private VBox categoryFormContainer;
    @FXML private TextField categoryNameField;
    @FXML private ComboBox<String> categoryIconComboBox;
    @FXML private TextArea categoryDescriptionArea;
    @FXML private TextField categoryOrderField;
    @FXML private CheckBox categoryActiveCheckBox;

    // UI Components - Category Items
    @FXML private Button addItemToCategoryButton;
    @FXML private VBox categoryItemsList;
    @FXML private Button moveUpItem1;
    @FXML private Button moveDownItem1;
    @FXML private Button removeFromCategoryButton1;
    @FXML private VBox emptyCategoryMessage;

    // UI Components - Category Actions
    @FXML private Button deleteCategoryButton;
    @FXML private Button clearCategoryFormButton;
    @FXML private Button saveCategoryButton;

    // UI Components - Statistics
    @FXML private Label totalCategoriesLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label activeItemsInMenuLabel;
    @FXML private Label lastMenuUpdateLabel;

    // UI Components - Menu Actions
    @FXML private Button previewMenuButton;
    @FXML private Button exportMenuButton;
    @FXML private Button publishMenuButton;

    // Services
    private NavigationController navigationController;
    private NotificationService notificationService;

    // Data
    private List<MenuCategory> menuCategories = new ArrayList<>();
    private MenuCategory selectedCategory;
    private Long restaurantId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.navigationController = NavigationController.getInstance();
        this.notificationService = NotificationService.getInstance();
        
        setupUI();
        loadMenuData();
    }

    /**
     * راه‌اندازی اولیه UI components
     */
    private void setupUI() {
        // راه‌اندازی فیلد جستجو
        searchMenuField.textProperty().addListener((obs, oldText, newText) -> {
            filterMenuCategories(newText.trim());
        });
        
        // راه‌اندازی ComboBox نمایش
        menuViewComboBox.setOnAction(e -> updateMenuView());
        
        // وضعیت اولیه فرم
        clearCategoryForm();
    }

    /**
     * بارگذاری داده‌های منو از backend
     */
    private void loadMenuData() {
        // دریافت restaurant ID از session یا navigation
        this.restaurantId = getCurrentRestaurantId();
        
        if (restaurantId == null) {
            notificationService.showError("شناسه رستوران مشخص نشده است");
            return;
        }
        
        Task<List<MenuCategory>> loadTask = new Task<List<MenuCategory>>() {
            @Override
            protected List<MenuCategory> call() throws Exception {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants/" + restaurantId + "/menu");
                
                if (response.isSuccess() && response.getData() != null) {
                    List<MenuCategory> categories = new ArrayList<>();
                    JsonNode dataArray = response.getData();
                    
                    if (dataArray.isArray()) {
                        for (JsonNode categoryNode : dataArray) {
                            MenuCategory category = parseMenuCategory(categoryNode);
                            categories.add(category);
                        }
                    }
                    
                    return categories;
                } else {
                    throw new RuntimeException(response.getMessage() != null ? 
                                               response.getMessage() : "خطا در بارگذاری منو");
                }
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                menuCategories = loadTask.getValue();
                updateMenuDisplay();
                updateStatistics();
            });
        });
        
        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = "خطا در بارگذاری منو";
                if (exception != null && exception.getMessage() != null) {
                    errorMessage = exception.getMessage();
                }
                notificationService.showError(errorMessage);
            });
        });
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    /**
     * به‌روزرسانی نمایش منو
     */
    private void updateMenuDisplay() {
        // پیاده‌سازی نمایش دسته‌ها در tree container
        // این بخش برای سادگی در فازهای بعدی تکمیل می‌شود
    }

    /**
     * به‌روزرسانی آمار منو
     */
    private void updateStatistics() {
        int totalCategories = menuCategories.size();
        int totalItems = menuCategories.stream()
            .mapToInt(cat -> cat.getItems().size())
            .sum();
        int activeItems = menuCategories.stream()
            .mapToInt(cat -> (int) cat.getItems().stream()
                .filter(item -> item.isActive())
                .count())
            .sum();
        
        Platform.runLater(() -> {
            totalCategoriesLabel.setText("مجموع دسته‌ها: " + totalCategories);
            totalItemsLabel.setText("مجموع آیتم‌ها: " + totalItems);
            activeItemsInMenuLabel.setText("آیتم‌های فعال: " + activeItems);
            lastMenuUpdateLabel.setText("آخرین بروزرسانی: اکنون");
        });
    }

    /**
     * فیلتر دسته‌های منو بر اساس متن جستجو
     */
    private void filterMenuCategories(String searchText) {
        // پیاده‌سازی فیلتر در فازهای بعدی
    }

    /**
     * تغییر نمای منو
     */
    private void updateMenuView() {
        String selectedView = menuViewComboBox.getValue();
        // پیاده‌سازی تغییر نما در فازهای بعدی
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * بازگشت به صفحه قبل
     */
    @FXML
    private void handleBack() {
        navigationController.navigateTo(NavigationController.RESTAURANT_LIST_SCENE);
    }

    /**
     * افزودن دسته جدید
     */
    @FXML
    private void handleAddCategory() {
        clearCategoryForm();
        detailsTitleLabel.setText("دسته جدید");
        categoryFormContainer.setVisible(true);
        categoryFormContainer.setManaged(true);
    }

    /**
     * مرتب‌سازی منو
     */
    @FXML
    private void handleReorderMenu() {
        notificationService.showInfo("قابلیت مرتب‌سازی در حال توسعه است");
    }

    /**
     * بروزرسانی منو
     */
    @FXML
    private void handleRefreshMenu() {
        loadMenuData();
    }

    /**
     * گسترش/جمع کردن دسته
     */
    @FXML
    private void handleExpandCategory() {
        // پیاده‌سازی expand/collapse در فازهای بعدی
    }

    /**
     * ویرایش دسته
     */
    @FXML
    private void handleEditCategory() {
        // پیاده‌سازی ویرایش دسته در فازهای بعدی
    }

    /**
     * حذف دسته
     */
    @FXML
    private void handleDeleteCategory() {
        // پیاده‌سازی حذف دسته در فازهای بعدی
    }

    /**
     * افزودن دسته جدید از دکمه پایینی
     */
    @FXML
    private void handleAddNewCategory() {
        handleAddCategory();
    }

    /**
     * افزودن آیتم به دسته
     */
    @FXML
    private void handleAddItemToCategory() {
        if (selectedCategory == null) {
            notificationService.showWarning("لطفاً ابتدا دسته‌ای را انتخاب کنید");
            return;
        }
        
        // پیاده‌سازی افزودن آیتم در فازهای بعدی
        notificationService.showInfo("قابلیت افزودن آیتم در حال توسعه است");
    }

    /**
     * انتقال آیتم به بالا
     */
    @FXML
    private void handleMoveItemUp() {
        // پیاده‌سازی انتقال آیتم در فازهای بعدی
    }

    /**
     * انتقال آیتم به پایین
     */
    @FXML
    private void handleMoveItemDown() {
        // پیاده‌سازی انتقال آیتم در فازهای بعدی
    }

    /**
     * حذف آیتم از دسته
     */
    @FXML
    private void handleRemoveFromCategory() {
        // پیاده‌سازی حذف آیتم در فازهای بعدی
    }

    /**
     * حذف دسته از فرم
     */
    @FXML
    private void handleDeleteCategoryForm() {
        if (selectedCategory == null) {
            notificationService.showWarning("هیچ دسته‌ای برای حذف انتخاب نشده است");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأیید حذف");
        confirmAlert.setHeaderText("حذف دسته");
        confirmAlert.setContentText("آیا مطمئن هستید که می‌خواهید این دسته را حذف کنید؟");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteCategoryFromServer();
            }
        });
    }

    /**
     * پاک کردن فرم دسته
     */
    @FXML
    private void handleClearCategoryForm() {
        clearCategoryForm();
    }

    /**
     * ذخیره دسته
     */
    @FXML
    private void handleSaveCategory() {
        if (!validateCategoryForm()) {
            return;
        }
        
        MenuCategory category = createCategoryFromForm();
        saveCategoryToServer(category);
    }

    /**
     * پیش‌نمایش منو
     */
    @FXML
    private void handlePreviewMenu() {
        notificationService.showInfo("قابلیت پیش‌نمایش در حال توسعه است");
    }

    /**
     * خروجی PDF منو
     */
    @FXML
    private void handleExportMenu() {
        notificationService.showInfo("قابلیت خروجی PDF در حال توسعه است");
    }

    /**
     * انتشار منو
     */
    @FXML
    private void handlePublishMenu() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأیید انتشار");
        confirmAlert.setHeaderText("انتشار منو");
        confirmAlert.setContentText("آیا مطمئن هستید که می‌خواهید منو را منتشر کنید؟");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                publishMenuToServer();
            }
        });
    }

    // ==================== HELPER METHODS ====================

    /**
     * پاک کردن فرم دسته
     */
    private void clearCategoryForm() {
        categoryNameField.clear();
        categoryIconComboBox.setValue(null);
        categoryDescriptionArea.clear();
        categoryOrderField.clear();
        categoryActiveCheckBox.setSelected(true);
        selectedCategory = null;
    }

    /**
     * اعتبارسنجی فرم دسته
     */
    private boolean validateCategoryForm() {
        if (categoryNameField.getText().trim().isEmpty()) {
            notificationService.showWarning("نام دسته الزامی است");
            categoryNameField.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * ایجاد category از اطلاعات فرم
     */
    private MenuCategory createCategoryFromForm() {
        MenuCategory category = new MenuCategory();
        category.setName(categoryNameField.getText().trim());
        category.setDescription(categoryDescriptionArea.getText().trim());
        category.setActive(categoryActiveCheckBox.isSelected());
        
        try {
            if (!categoryOrderField.getText().trim().isEmpty()) {
                category.setDisplayOrder(Integer.parseInt(categoryOrderField.getText().trim()));
            }
        } catch (NumberFormatException e) {
            category.setDisplayOrder(1);
        }
        
        return category;
    }

    /**
     * ذخیره دسته در server
     */
    private void saveCategoryToServer(MenuCategory category) {
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String endpoint = "/restaurants/" + restaurantId + "/menu/categories";
                HttpClientUtil.ApiResponse response;
                
                if (selectedCategory != null && selectedCategory.getId() != null) {
                    // ویرایش دسته موجود
                    endpoint += "/" + selectedCategory.getId();
                    response = HttpClientUtil.put(endpoint, category);
                } else {
                    // ایجاد دسته جدید
                    response = HttpClientUtil.post(endpoint, category);
                }
                
                if (!response.isSuccess()) {
                    throw new RuntimeException(response.getMessage() != null ? 
                                               response.getMessage() : "خطا در ذخیره دسته");
                }
                
                return null;
            }
        };
        
        saveTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                notificationService.showSuccess("دسته با موفقیت ذخیره شد");
                loadMenuData();
                clearCategoryForm();
            });
        });
        
        saveTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = saveTask.getException();
                String errorMessage = "خطا در ذخیره دسته";
                if (exception != null && exception.getMessage() != null) {
                    errorMessage = exception.getMessage();
                }
                notificationService.showError(errorMessage);
            });
        });
        
        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
    }

    /**
     * حذف دسته از server
     */
    private void deleteCategoryFromServer() {
        if (selectedCategory == null || selectedCategory.getId() == null) {
            return;
        }
        
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String endpoint = "/restaurants/" + restaurantId + "/menu/categories/" + selectedCategory.getId();
                HttpClientUtil.ApiResponse response = HttpClientUtil.delete(endpoint);
                
                if (!response.isSuccess()) {
                    throw new RuntimeException(response.getMessage() != null ? 
                                               response.getMessage() : "خطا در حذف دسته");
                }
                
                return null;
            }
        };
        
        deleteTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                notificationService.showSuccess("دسته با موفقیت حذف شد");
                loadMenuData();
                clearCategoryForm();
            });
        });
        
        deleteTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = deleteTask.getException();
                String errorMessage = "خطا در حذف دسته";
                if (exception != null && exception.getMessage() != null) {
                    errorMessage = exception.getMessage();
                }
                notificationService.showError(errorMessage);
            });
        });
        
        Thread deleteThread = new Thread(deleteTask);
        deleteThread.setDaemon(true);
        deleteThread.start();
    }

    /**
     * انتشار منو در server
     */
    private void publishMenuToServer() {
        Task<Void> publishTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String endpoint = "/restaurants/" + restaurantId + "/menu/publish";
                HttpClientUtil.ApiResponse response = HttpClientUtil.post(endpoint, null);
                
                if (!response.isSuccess()) {
                    throw new RuntimeException(response.getMessage() != null ? 
                                               response.getMessage() : "خطا در انتشار منو");
                }
                
                return null;
            }
        };
        
        publishTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                notificationService.showSuccess("منو با موفقیت منتشر شد");
                updateStatistics();
            });
        });
        
        publishTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = publishTask.getException();
                String errorMessage = "خطا در انتشار منو";
                if (exception != null && exception.getMessage() != null) {
                    errorMessage = exception.getMessage();
                }
                notificationService.showError(errorMessage);
            });
        });
        
        Thread publishThread = new Thread(publishTask);
        publishThread.setDaemon(true);
        publishThread.start();
    }

    /**
     * دریافت ID رستوران فعلی
     */
    private Long getCurrentRestaurantId() {
        // در عملی، این از session یا navigation parameter دریافت می‌شود
        return 1L; // مقدار موقت برای تست
    }

    /**
     * Parse menu category از JSON
     */
    private MenuCategory parseMenuCategory(JsonNode node) {
        MenuCategory category = new MenuCategory();
        category.setId(node.get("id").asLong());
        category.setName(node.get("name").asText());
        category.setDescription(node.get("description").asText(""));
        category.setActive(node.get("active").asBoolean(true));
        
        if (node.has("displayOrder")) {
            category.setDisplayOrder(node.get("displayOrder").asInt(1));
        }
        
        List<MenuItem> items = new ArrayList<>();
        if (node.has("items") && node.get("items").isArray()) {
            for (JsonNode itemNode : node.get("items")) {
                MenuItem item = parseMenuItem(itemNode);
                items.add(item);
            }
        }
        category.setItems(items);
        
        return category;
    }

    /**
     * Parse menu item از JSON
     */
    private MenuItem parseMenuItem(JsonNode node) {
        MenuItem item = new MenuItem();
        item.setId(node.get("id").asLong());
        item.setName(node.get("name").asText());
        item.setDescription(node.get("description").asText(""));
        item.setPrice(node.get("price").asDouble());
        item.setActive(node.get("active").asBoolean(true));
        
        return item;
    }

    // ==================== DATA MODELS ====================

    /**
     * مدل داده دسته منو
     */
    public static class MenuCategory {
        private Long id;
        private String name;
        private String description;
        private boolean active = true;
        private int displayOrder = 1;
        private List<MenuItem> items = new ArrayList<>();

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

        public List<MenuItem> getItems() { return items; }
        public void setItems(List<MenuItem> items) { this.items = items; }
    }

    /**
     * مدل داده آیتم منو
     */
    public static class MenuItem {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private boolean active = true;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}
