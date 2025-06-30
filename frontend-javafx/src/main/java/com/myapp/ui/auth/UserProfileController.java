package com.myapp.ui.auth;

import com.myapp.ui.common.HttpClientUtil;
import com.myapp.ui.common.NavigationController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * کنترلر جامع مدیریت پروفایل و تاریخچه کاربر - فاز 28
 * 
 * این کلاس مسئول مدیریت کامل پروفایل کاربر شامل:
 * - مدیریت اطلاعات شخصی و آدرس‌ها
 * - نمایش و مدیریت تاریخچه سفارشات
 * - نمایش تاریخچه تراکنش‌های مالی
 * - مدیریت علاقه‌مندی‌ها (رستوران‌ها و آیتم‌ها)
 * - تنظیمات و ترجیحات کاربری
 * - آمار و تحلیل‌های کاربری
 * 
 * ویژگی‌های کلیدی:
 * - Tab-based navigation برای بخش‌های مختلف
 * - فیلترینگ و جستجوی پیشرفته
 * - نمودارهای آماری تعاملی
 * - Export گزارشات و رسیدها
 * - Real-time updates
 * - Responsive design
 * 
 * @author تیم توسعه سیستم سفارش غذا
 * @version 2.0
 * @since 1403/09/29
 */
public class UserProfileController implements Initializable {

    // ========== Tab Container ==========
    /** کانتینر اصلی Tab ها برای بخش‌های مختلف */
    @FXML TabPane mainTabPane;
    
    /** تب اطلاعات شخصی */
    @FXML Tab profileTab;
    
    /** تب تاریخچه سفارشات */
    @FXML Tab ordersTab;
    
    /** تب تاریخچه مالی */
    @FXML Tab transactionsTab;
    
    /** تب علاقه‌مندی‌ها */
    @FXML Tab favoritesTab;
    
    /** تب تنظیمات */
    @FXML Tab settingsTab;
    
    /** تب آمار و تحلیل */
    @FXML Tab analyticsTab;

    // ========== Profile Tab Components ==========
    /** فیلد نام کامل کاربر */
    @FXML TextField fullNameField;
    
    /** فیلد شماره تلفن (غیرقابل ویرایش) */
    @FXML TextField phoneField;
    
    /** فیلد ایمیل */
    @FXML TextField emailField;
    
    /** لیست آدرس‌های کاربر */
    @FXML ListView<AddressItem> addressListView;
    
    /** دکمه افزودن آدرس جدید */
    @FXML Button addAddressButton;
    
    /** دکمه ویرایش آدرس */
    @FXML Button editAddressButton;
    
    /** دکمه حذف آدرس */
    @FXML Button deleteAddressButton;
    
    /** برچسب نقش کاربری */
    @FXML Label roleLabel;
    
    /** برچسب وضعیت حساب */
    @FXML Label accountStatusLabel;
    
    /** برچسب تاریخ عضویت */
    @FXML Label memberSinceLabel;
    
    /** برچسب موجودی کیف پول */
    @FXML Label walletBalanceLabel;
    
    /** دکمه ذخیره تغییرات پروفایل */
    @FXML Button saveProfileButton;
    
    /** بخش تغییر رمز عبور */
    @FXML VBox passwordChangeSection;
    
    /** فیلد رمز عبور فعلی */
    @FXML PasswordField currentPasswordField;
    
    /** فیلد رمز عبور جدید */
    @FXML PasswordField newPasswordField;
    
    /** فیلد تأیید رمز عبور جدید */
    @FXML PasswordField confirmPasswordField;
    
    /** دکمه تغییر رمز عبور */
    @FXML Button changePasswordButton;

    // ========== Orders Tab Components ==========
    /** جدول نمایش سفارشات */
    @FXML TableView<OrderHistoryItem> ordersTable;
    
    /** ستون شماره سفارش */
    @FXML TableColumn<OrderHistoryItem, String> orderIdColumn;
    
    /** ستون تاریخ سفارش */
    @FXML TableColumn<OrderHistoryItem, String> orderDateColumn;
    
    /** ستون نام رستوران */
    @FXML TableColumn<OrderHistoryItem, String> restaurantColumn;
    
    /** ستون مبلغ کل */
    @FXML TableColumn<OrderHistoryItem, String> totalAmountColumn;
    
    /** ستون وضعیت سفارش */
    @FXML TableColumn<OrderHistoryItem, String> orderStatusColumn;
    
    /** ستون عملیات */
    @FXML TableColumn<OrderHistoryItem, Void> orderActionsColumn;
    
    /** فیلد جستجوی سفارشات */
    @FXML TextField orderSearchField;
    
    /** فیلتر وضعیت سفارش */
    @FXML ComboBox<String> orderStatusFilter;
    
    /** انتخاب بازه زمانی */
    @FXML DatePicker orderStartDate;
    @FXML DatePicker orderEndDate;
    
    /** دکمه اعمال فیلتر */
    @FXML Button applyOrderFilterButton;
    
    /** دکمه پاک کردن فیلتر */
    @FXML Button clearOrderFilterButton;
    
    /** برچسب تعداد کل سفارشات */
    @FXML Label totalOrdersLabel;
    
    /** برچسب مجموع خرید */
    @FXML Label totalSpentLabel;

    // ========== Transactions Tab Components ==========
    /** جدول تراکنش‌های مالی */
    @FXML TableView<TransactionItem> transactionsTable;
    
    /** ستون شماره تراکنش */
    @FXML TableColumn<TransactionItem, String> transactionIdColumn;
    
    /** ستون تاریخ تراکنش */
    @FXML TableColumn<TransactionItem, String> transactionDateColumn;
    
    /** ستون نوع تراکنش */
    @FXML TableColumn<TransactionItem, String> transactionTypeColumn;
    
    /** ستون مبلغ */
    @FXML TableColumn<TransactionItem, String> amountColumn;
    
    /** ستون توضیحات */
    @FXML TableColumn<TransactionItem, String> descriptionColumn;
    
    /** ستون موجودی پس از تراکنش */
    @FXML TableColumn<TransactionItem, String> balanceAfterColumn;
    
    /** فیلتر نوع تراکنش */
    @FXML ComboBox<String> transactionTypeFilter;
    
    /** انتخاب بازه زمانی تراکنش‌ها */
    @FXML DatePicker transactionStartDate;
    @FXML DatePicker transactionEndDate;
    
    /** دکمه دانلود گزارش تراکنش‌ها */
    @FXML Button downloadTransactionReportButton;

    // ========== Favorites Tab Components ==========
    /** لیست رستوران‌های مورد علاقه */
    @FXML ListView<FavoriteRestaurant> favoriteRestaurantsList;
    
    /** لیست آیتم‌های مورد علاقه */
    @FXML ListView<FavoriteItem> favoriteItemsList;
    
    /** دکمه حذف از علاقه‌مندی‌ها */
    @FXML Button removeFavoriteButton;
    
    /** دکمه سفارش مجدد */
    @FXML Button reorderFavoriteButton;
    
    /** برچسب تعداد رستوران‌های مورد علاقه */
    @FXML Label favoriteRestaurantsCountLabel;
    
    /** برچسب تعداد آیتم‌های مورد علاقه */
    @FXML Label favoriteItemsCountLabel;

    // ========== Settings Tab Components ==========
    /** تنظیمات اطلاع‌رسانی ایمیل */
    @FXML CheckBox emailNotificationCheckBox;
    
    /** تنظیمات اطلاع‌رسانی پیامک */
    @FXML CheckBox smsNotificationCheckBox;
    
    /** تنظیمات اطلاع‌رسانی push */
    @FXML CheckBox pushNotificationCheckBox;
    
    /** انتخاب زبان */
    @FXML ComboBox<String> languageComboBox;
    
    /** انتخاب تم */
    @FXML ComboBox<String> themeComboBox;
    
    /** تنظیمات اشتراک‌گذاری داده */
    @FXML CheckBox shareDataCheckBox;
    @FXML CheckBox showProfileCheckBox;
    
    /** دکمه ذخیره تنظیمات */
    @FXML Button saveSettingsButton;
    
    /** دکمه بازنشانی تنظیمات */
    @FXML Button resetSettingsButton;

    // ========== Analytics Tab Components ==========
    /** نمودار توزیع سفارشات */
    @FXML PieChart orderDistributionChart;
    
    /** نمودار روند هزینه‌ها */
    @FXML LineChart<String, Number> spendingTrendChart;
    
    /** برچسب میانگین هزینه ماهانه */
    @FXML Label averageMonthlySpendLabel;
    
    /** برچسب رستوران محبوب */
    @FXML Label topRestaurantLabel;
    
    /** برچسب آیتم محبوب */
    @FXML Label topFoodItemLabel;
    
    /** برچسب تعداد کل سفارشات */
    @FXML Label totalOrdersAnalyticsLabel;
    
    /** برچسب مجموع کل خرید */
    @FXML Label totalSpentAnalyticsLabel;
    
    /** انتخاب بازه زمانی آمار */
    @FXML ComboBox<String> analyticsTimeRangeComboBox;

    // ========== Common UI Components ==========
    /** نوار وضعیت */
    @FXML Label statusLabel;
    
    /** نشانگر بارگذاری */
    @FXML ProgressIndicator loadingIndicator;
    
    /** نوار پیشرفت */
    @FXML ProgressBar progressBar;

    // ========== Instance Variables ==========
    /** کنترلر ناوبری */
    NavigationController navigationController;
    
    /** ObjectMapper برای پردازش JSON */
    final ObjectMapper objectMapper = new ObjectMapper();
    
    /** لیست سفارشات */
    ObservableList<OrderHistoryItem> ordersList = FXCollections.observableArrayList();
    
    /** لیست تراکنش‌ها */
    ObservableList<TransactionItem> transactionsList = FXCollections.observableArrayList();
    
    /** لیست رستوران‌های مورد علاقه */
    ObservableList<FavoriteRestaurant> favoriteRestaurants = FXCollections.observableArrayList();
    
    /** لیست آیتم‌های مورد علاقه */
    ObservableList<FavoriteItem> favoriteItems = FXCollections.observableArrayList();
    
    /** لیست آدرس‌ها */
    ObservableList<AddressItem> addressList = FXCollections.observableArrayList();
    
    /** اطلاعات پروفایل کاربر */
    UserProfile currentUserProfile;
    
    /** تنظیمات کاربر */
    UserSettings userSettings;
    
    /** آمار کاربر */
    UserAnalytics userAnalytics;

    /**
     * متد مقداردهی اولیه کنترلر
     * این متد پس از بارگذاری FXML فراخوانی می‌شود
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // مقداردهی navigation controller
        this.navigationController = NavigationController.getInstance();
        
        // راه‌اندازی UI
        setupUI();
        
        // راه‌اندازی جداول
        setupTables();
        
        // راه‌اندازی listeners
        setupListeners();
        
        // بارگذاری داده‌های اولیه
        loadInitialData();
    }

    /**
     * راه‌اندازی کامپوننت‌های UI
     * تنظیمات اولیه برای همه بخش‌ها
     */
    private void setupUI() {
        // تنظیم فیلد تلفن به حالت فقط خواندنی
        phoneField.setEditable(false);
        phoneField.setStyle("-fx-background-color: #e9ecef;");
        
        // مقداردهی فیلترها
        orderStatusFilter.setItems(FXCollections.observableArrayList(
            "همه", "در انتظار", "در حال آماده‌سازی", "آماده ارسال", 
            "در حال ارسال", "تحویل داده شده", "لغو شده"
        ));
        orderStatusFilter.setValue("همه");
        
        transactionTypeFilter.setItems(FXCollections.observableArrayList(
            "همه", "واریز", "برداشت", "پرداخت سفارش", "بازگشت وجه"
        ));
        transactionTypeFilter.setValue("همه");
        
        languageComboBox.setItems(FXCollections.observableArrayList(
            "فارسی", "English", "العربية"
        ));
        languageComboBox.setValue("فارسی");
        
        themeComboBox.setItems(FXCollections.observableArrayList(
            "روشن", "تیره", "خودکار"
        ));
        themeComboBox.setValue("روشن");
        
        analyticsTimeRangeComboBox.setItems(FXCollections.observableArrayList(
            "هفته اخیر", "ماه اخیر", "3 ماه اخیر", "6 ماه اخیر", "سال اخیر", "کل دوره"
        ));
        analyticsTimeRangeComboBox.setValue("ماه اخیر");
        
        // تنظیم تاریخ‌های پیش‌فرض
        orderEndDate.setValue(LocalDate.now());
        orderStartDate.setValue(LocalDate.now().minusMonths(1));
        transactionEndDate.setValue(LocalDate.now());
        transactionStartDate.setValue(LocalDate.now().minusMonths(1));
        
        // مخفی کردن loading در ابتدا
        setLoading(false);
    }

    /**
     * راه‌اندازی جداول با ستون‌ها و cell factory ها
     */
    private void setupTables() {
        // تنظیم جدول سفارشات
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        restaurantColumn.setCellValueFactory(new PropertyValueFactory<>("restaurantName"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // ستون عملیات برای سفارشات
        setupOrderActionsColumn();
        
        // تنظیم جدول تراکنش‌ها
        transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        balanceAfterColumn.setCellValueFactory(new PropertyValueFactory<>("balanceAfter"));
        
        // Cell factory برای رنگ‌بندی مبالغ
        amountColumn.setCellFactory(column -> new TableCell<TransactionItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    TransactionItem transaction = getTableView().getItems().get(getIndex());
                    if (transaction.getType().equals("واریز") || transaction.getType().equals("بازگشت وجه")) {
                        setStyle("-fx-text-fill: #28a745;"); // سبز برای واریز
                    } else {
                        setStyle("-fx-text-fill: #dc3545;"); // قرمز برای برداشت
                    }
                }
            }
        });
        
        // Bind data to tables
        ordersTable.setItems(ordersList);
        transactionsTable.setItems(transactionsList);
    }

    /**
     * راه‌اندازی ستون عملیات در جدول سفارشات
     */
    private void setupOrderActionsColumn() {
        orderActionsColumn.setCellFactory(new Callback<TableColumn<OrderHistoryItem, Void>, TableCell<OrderHistoryItem, Void>>() {
            @Override
            public TableCell<OrderHistoryItem, Void> call(TableColumn<OrderHistoryItem, Void> param) {
                return new TableCell<OrderHistoryItem, Void>() {
                    private final Button viewButton = new Button("مشاهده");
                    private final Button reorderButton = new Button("سفارش مجدد");
                    private final Button receiptButton = new Button("رسید");
                    
                    {
                        // استایل دکمه‌ها
                        viewButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 12;");
                        reorderButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12;");
                        receiptButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12;");
                        
                        // Event handlers
                        viewButton.setOnAction(event -> {
                            OrderHistoryItem order = getTableView().getItems().get(getIndex());
                            handleViewOrderDetails(order);
                        });
                        
                        reorderButton.setOnAction(event -> {
                            OrderHistoryItem order = getTableView().getItems().get(getIndex());
                            handleReorder(order);
                        });
                        
                        receiptButton.setOnAction(event -> {
                            OrderHistoryItem order = getTableView().getItems().get(getIndex());
                            handleDownloadReceipt(order);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5);
                            buttons.getChildren().addAll(viewButton, reorderButton, receiptButton);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }

    /**
     * راه‌اندازی event listeners برای کامپوننت‌های مختلف
     */
    private void setupListeners() {
        // Tab change listener
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                handleTabChange(newTab);
            }
        });
        
        // Profile fields listeners
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> checkProfileChanges());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> checkProfileChanges());
        
        // Password fields listeners
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswordFields());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswordFields());
        
        // Address list selection listener
        addressListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editAddressButton.setDisable(!hasSelection);
            deleteAddressButton.setDisable(!hasSelection);
        });
        
        // Favorite lists selection listeners
        favoriteRestaurantsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateFavoriteButtons();
        });
        
        favoriteItemsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateFavoriteButtons();
        });
        
        // Search field listener
        orderSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterOrders();
        });
        
        // Filter listeners
        orderStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterOrders());
        transactionTypeFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTransactions());
        
        // Analytics time range listener
        analyticsTimeRangeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateAnalytics(newVal);
            }
        });
    }

    /**
     * بارگذاری داده‌های اولیه
     */
    private void loadInitialData() {
        // بارگذاری پروفایل کاربر
        loadUserProfile();
        
        // بارگذاری تنظیمات
        loadUserSettings();
        
        // سایر داده‌ها بر اساس تب فعال بارگذاری می‌شوند
    }

    // ========== Profile Management Methods ==========
    
    /**
     * بارگذاری اطلاعات پروفایل کاربر از سرور
     */
    private void loadUserProfile() {
        setLoading(true);
        setStatus("در حال بارگذاری پروفایل...");
        
        Task<UserProfile> loadTask = new Task<UserProfile>() {
            @Override
            protected UserProfile call() throws Exception {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/auth/profile");
                
                if (response.isSuccess() && response.getData() != null) {
                    return parseUserProfile(response.getData());
                } else {
                    throw new RuntimeException(response.getMessage() != null ? 
                        response.getMessage() : "خطا در بارگذاری پروفایل");
                }
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                currentUserProfile = loadTask.getValue();
                displayUserProfile();
                loadUserAddresses();
                setStatus("پروفایل بارگذاری شد");
            });
        });
        
        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                showError("خطا", "خطا در بارگذاری پروفایل: " + loadTask.getException().getMessage());
                setStatus("خطا در بارگذاری");
            });
        });
        
        new Thread(loadTask).start();
    }

    /**
     * پردازش اطلاعات پروفایل از JSON
     */
    private UserProfile parseUserProfile(JsonNode data) {
        UserProfile profile = new UserProfile();
        profile.setId(data.get("userId").asLong());
        profile.setFullName(data.get("fullName").asText());
        profile.setPhone(data.get("phone").asText());
        profile.setEmail(data.has("email") ? data.get("email").asText() : "");
        profile.setRole(data.get("role").asText());
        profile.setActive(data.has("isActive") ? data.get("isActive").asBoolean() : true);
        profile.setWalletBalance(data.has("walletBalance") ? data.get("walletBalance").asDouble() : 0.0);
        profile.setCreatedAt(data.has("createdAt") ? data.get("createdAt").asText() : "");
        return profile;
    }

    /**
     * نمایش اطلاعات پروفایل در UI
     */
    private void displayUserProfile() {
        if (currentUserProfile == null) return;
        
        fullNameField.setText(currentUserProfile.getFullName());
        phoneField.setText(currentUserProfile.getPhone());
        emailField.setText(currentUserProfile.getEmail());
        roleLabel.setText(getRoleText(currentUserProfile.getRole()));
        accountStatusLabel.setText(currentUserProfile.isActive() ? "فعال" : "غیرفعال");
        accountStatusLabel.setStyle("-fx-text-fill: " + (currentUserProfile.isActive() ? "#28a745" : "#dc3545"));
        walletBalanceLabel.setText(String.format("%,.0f تومان", currentUserProfile.getWalletBalance()));
        memberSinceLabel.setText(formatDate(currentUserProfile.getCreatedAt()));
    }

    /**
     * بارگذاری آدرس‌های کاربر
     */
    private void loadUserAddresses() {
        Task<List<AddressItem>> loadTask = new Task<List<AddressItem>>() {
            @Override
            protected List<AddressItem> call() throws Exception {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/user/addresses");
                
                if (response.isSuccess() && response.getData() != null) {
                    List<AddressItem> addresses = new ArrayList<>();
                    JsonNode addressArray = response.getData();
                    
                    for (JsonNode addressNode : addressArray) {
                        AddressItem address = new AddressItem();
                        address.setId(addressNode.get("id").asLong());
                        address.setTitle(addressNode.get("title").asText());
                        address.setAddress(addressNode.get("address").asText());
                        address.setDefault(addressNode.has("isDefault") && addressNode.get("isDefault").asBoolean());
                        addresses.add(address);
                    }
                    
                    return addresses;
                }
                return new ArrayList<>();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                addressList.clear();
                addressList.addAll(loadTask.getValue());
                addressListView.setItems(addressList);
            });
        });
        
        new Thread(loadTask).start();
    }

    /**
     * ذخیره تغییرات پروفایل
     */
    @FXML
    void handleSaveProfile() {
        if (!validateProfileData()) return;
        
        setLoading(true);
        setStatus("در حال ذخیره تغییرات...");
        
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("fullName", fullNameField.getText().trim());
                updateData.put("email", emailField.getText().trim());
                
                HttpClientUtil.ApiResponse response = HttpClientUtil.put("/auth/profile", updateData);
                
                if (!response.isSuccess()) {
                    throw new RuntimeException(response.getMessage());
                }
                return null;
            }
        };
        
        saveTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                setStatus("تغییرات با موفقیت ذخیره شد");
                showSuccess("موفقیت", "پروفایل با موفقیت به‌روزرسانی شد");
                loadUserProfile(); // بارگذاری مجدد
            });
        });
        
        saveTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                showError("خطا", "خطا در ذخیره تغییرات: " + saveTask.getException().getMessage());
                setStatus("خطا در ذخیره");
            });
        });
        
        new Thread(saveTask).start();
    }

    /**
     * تغییر رمز عبور
     */
    @FXML
    void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (!newPassword.equals(confirmPassword)) {
            showError("خطا", "رمز عبور جدید و تکرار آن یکسان نیستند");
            return;
        }
        
        if (newPassword.length() < 4) {
            showError("خطا", "رمز عبور باید حداقل 4 کاراکتر باشد");
            return;
        }
        
        setLoading(true);
        setStatus("در حال تغییر رمز عبور...");
        
        Task<Void> changePasswordTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Map<String, String> passwordData = new HashMap<>();
                passwordData.put("currentPassword", currentPassword);
                passwordData.put("newPassword", newPassword);
                
                HttpClientUtil.ApiResponse response = HttpClientUtil.post("/auth/change-password", passwordData);
                
                if (!response.isSuccess()) {
                    throw new RuntimeException(response.getMessage());
                }
                return null;
            }
        };
        
        changePasswordTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                setStatus("رمز عبور با موفقیت تغییر یافت");
                showSuccess("موفقیت", "رمز عبور با موفقیت تغییر یافت");
                clearPasswordFields();
            });
        });
        
        changePasswordTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                showError("خطا", "خطا در تغییر رمز عبور: " + changePasswordTask.getException().getMessage());
                setStatus("خطا در تغییر رمز");
            });
        });
        
        new Thread(changePasswordTask).start();
    }

    // ========== Orders Management Methods ==========
    
    /**
     * بارگذاری تاریخچه سفارشات
     */
    private void loadOrderHistory() {
        setLoading(true);
        setStatus("در حال بارگذاری سفارشات...");
        
        Task<List<OrderHistoryItem>> loadTask = new Task<List<OrderHistoryItem>>() {
            @Override
            protected List<OrderHistoryItem> call() throws Exception {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/orders/history");
                
                if (response.isSuccess() && response.getData() != null) {
                    List<OrderHistoryItem> orders = new ArrayList<>();
                    JsonNode ordersArray = response.getData();
                    
                    for (JsonNode orderNode : ordersArray) {
                        OrderHistoryItem order = new OrderHistoryItem();
                        order.setOrderId(orderNode.get("orderId").asText());
                        order.setOrderDate(formatDateTime(orderNode.get("createdAt").asText()));
                        order.setRestaurantName(orderNode.get("restaurantName").asText());
                        order.setTotalAmount(String.format("%,.0f تومان", orderNode.get("totalAmount").asDouble()));
                        order.setStatus(getOrderStatusText(orderNode.get("status").asText()));
                        order.setItems(parseOrderItems(orderNode.get("items")));
                        orders.add(order);
                    }
                    
                    return orders;
                }
                return new ArrayList<>();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                ordersList.clear();
                ordersList.addAll(loadTask.getValue());
                updateOrderStatistics();
                setStatus("سفارشات بارگذاری شد");
            });
        });
        
        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                showError("خطا", "خطا در بارگذاری سفارشات");
                setStatus("خطا در بارگذاری");
            });
        });
        
        new Thread(loadTask).start();
    }

    /**
     * فیلتر کردن سفارشات
     */
    private void filterOrders() {
        String searchText = orderSearchField.getText().toLowerCase();
        String statusFilter = orderStatusFilter.getValue();
        LocalDate startDate = orderStartDate.getValue();
        LocalDate endDate = orderEndDate.getValue();
        
        List<OrderHistoryItem> filteredList = ordersList.stream()
            .filter(order -> {
                // فیلتر متن جستجو
                boolean matchesSearch = searchText.isEmpty() || 
                    order.getRestaurantName().toLowerCase().contains(searchText) ||
                    order.getOrderId().toLowerCase().contains(searchText);
                
                // فیلتر وضعیت
                boolean matchesStatus = statusFilter.equals("همه") || 
                    order.getStatus().equals(statusFilter);
                
                // فیلتر تاریخ
                boolean matchesDate = true; // TODO: implement date filtering
                
                return matchesSearch && matchesStatus && matchesDate;
            })
            .collect(Collectors.toList());
        
        ordersTable.setItems(FXCollections.observableArrayList(filteredList));
    }

    /**
     * مشاهده جزئیات سفارش
     */
    void handleViewOrderDetails(OrderHistoryItem order) {
        // TODO: نمایش دیالوگ جزئیات سفارش
        showInfo("جزئیات سفارش", "شماره سفارش: " + order.getOrderId() + "\n" +
                                  "رستوران: " + order.getRestaurantName() + "\n" +
                                  "مبلغ: " + order.getTotalAmount() + "\n" +
                                  "وضعیت: " + order.getStatus());
    }

    /**
     * سفارش مجدد
     */
    void handleReorder(OrderHistoryItem order) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأیید سفارش مجدد");
        confirmAlert.setHeaderText("آیا می‌خواهید این سفارش را مجدداً ثبت کنید؟");
        confirmAlert.setContentText("آیتم‌های این سفارش به سبد خرید شما اضافه خواهند شد.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: افزودن آیتم‌ها به سبد خرید
                showSuccess("موفقیت", "آیتم‌ها به سبد خرید اضافه شدند");
                navigationController.navigateTo(NavigationController.CART_SCENE);
            }
        });
    }

    /**
     * دانلود رسید سفارش
     */
    void handleDownloadReceipt(OrderHistoryItem order) {
        try {
            String receipt = generateReceipt(order);
            File file = new File("receipt_" + order.getOrderId() + ".txt");
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(receipt);
            }
            
            showSuccess("موفقیت", "رسید در فایل " + file.getName() + " ذخیره شد");
        } catch (IOException e) {
            showError("خطا", "خطا در ذخیره رسید: " + e.getMessage());
        }
    }

    // ========== Transaction Management Methods ==========
    
    /**
     * بارگذاری تاریخچه تراکنش‌های مالی
     */
    private void loadTransactionHistory() {
        setLoading(true);
        setStatus("در حال بارگذاری تراکنش‌ها...");
        
        Task<List<TransactionItem>> loadTask = new Task<List<TransactionItem>>() {
            @Override
            protected List<TransactionItem> call() throws Exception {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/wallet/transactions");
                
                if (response.isSuccess() && response.getData() != null) {
                    List<TransactionItem> transactions = new ArrayList<>();
                    JsonNode transArray = response.getData();
                    
                    for (JsonNode transNode : transArray) {
                        TransactionItem trans = new TransactionItem();
                        trans.setTransactionId(transNode.get("transactionId").asText());
                        trans.setDate(formatDateTime(transNode.get("createdAt").asText()));
                        trans.setType(getTransactionTypeText(transNode.get("type").asText()));
                        trans.setAmount(String.format("%,.0f تومان", transNode.get("amount").asDouble()));
                        trans.setDescription(transNode.get("description").asText());
                        trans.setBalanceAfter(String.format("%,.0f تومان", transNode.get("balanceAfter").asDouble()));
                        transactions.add(trans);
                    }
                    
                    return transactions;
                }
                return new ArrayList<>();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                transactionsList.clear();
                transactionsList.addAll(loadTask.getValue());
                setStatus("تراکنش‌ها بارگذاری شد");
            });
        });
        
        new Thread(loadTask).start();
    }

    /**
     * فیلتر کردن تراکنش‌ها
     */
    private void filterTransactions() {
        String typeFilter = transactionTypeFilter.getValue();
        LocalDate startDate = transactionStartDate.getValue();
        LocalDate endDate = transactionEndDate.getValue();
        
        List<TransactionItem> filteredList = transactionsList.stream()
            .filter(trans -> {
                boolean matchesType = typeFilter.equals("همه") || trans.getType().equals(typeFilter);
                // TODO: implement date filtering
                return matchesType;
            })
            .collect(Collectors.toList());
        
        transactionsTable.setItems(FXCollections.observableArrayList(filteredList));
    }

    // ========== Favorites Management Methods ==========
    
    /**
     * بارگذاری علاقه‌مندی‌ها
     */
    private void loadFavorites() {
        loadFavoriteRestaurants();
        loadFavoriteItems();
    }

    /**
     * بارگذاری رستوران‌های مورد علاقه
     */
    private void loadFavoriteRestaurants() {
        Task<List<FavoriteRestaurant>> loadTask = new Task<List<FavoriteRestaurant>>() {
            @Override
            protected List<FavoriteRestaurant> call() throws Exception {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/favorites/restaurants");
                
                if (response.isSuccess() && response.getData() != null) {
                    List<FavoriteRestaurant> favorites = new ArrayList<>();
                    JsonNode favArray = response.getData();
                    
                    for (JsonNode favNode : favArray) {
                        FavoriteRestaurant fav = new FavoriteRestaurant();
                        fav.setId(favNode.get("id").asLong());
                        fav.setRestaurantId(favNode.get("restaurantId").asLong());
                        fav.setName(favNode.get("restaurantName").asText());
                        fav.setRating(favNode.get("rating").asDouble());
                        fav.setOrderCount(favNode.get("orderCount").asInt());
                        favorites.add(fav);
                    }
                    
                    return favorites;
                }
                return new ArrayList<>();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                favoriteRestaurants.clear();
                favoriteRestaurants.addAll(loadTask.getValue());
                favoriteRestaurantsList.setItems(favoriteRestaurants);
                favoriteRestaurantsCountLabel.setText(String.valueOf(favoriteRestaurants.size()));
            });
        });
        
        new Thread(loadTask).start();
    }

    /**
     * بارگذاری آیتم‌های مورد علاقه
     */
    private void loadFavoriteItems() {
        Task<List<FavoriteItem>> loadTask = new Task<List<FavoriteItem>>() {
            @Override
            protected List<FavoriteItem> call() throws Exception {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/favorites/items");
                
                if (response.isSuccess() && response.getData() != null) {
                    List<FavoriteItem> favorites = new ArrayList<>();
                    JsonNode favArray = response.getData();
                    
                    for (JsonNode favNode : favArray) {
                        FavoriteItem fav = new FavoriteItem();
                        fav.setId(favNode.get("id").asLong());
                        fav.setItemId(favNode.get("itemId").asLong());
                        fav.setName(favNode.get("itemName").asText());
                        fav.setRestaurantName(favNode.get("restaurantName").asText());
                        fav.setPrice(favNode.get("price").asDouble());
                        favorites.add(fav);
                    }
                    
                    return favorites;
                }
                return new ArrayList<>();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                favoriteItems.clear();
                favoriteItems.addAll(loadTask.getValue());
                favoriteItemsList.setItems(favoriteItems);
                favoriteItemsCountLabel.setText(String.valueOf(favoriteItems.size()));
            });
        });
        
        new Thread(loadTask).start();
    }

    // ========== Settings Management Methods ==========
    
    /**
     * بارگذاری تنظیمات کاربر
     */
    private void loadUserSettings() {
        Task<UserSettings> loadTask = new Task<UserSettings>() {
            @Override
            protected UserSettings call() throws Exception {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/user/settings");
                
                if (response.isSuccess() && response.getData() != null) {
                    JsonNode data = response.getData();
                    UserSettings settings = new UserSettings();
                    settings.setEmailNotifications(data.get("emailNotifications").asBoolean());
                    settings.setSmsNotifications(data.get("smsNotifications").asBoolean());
                    settings.setPushNotifications(data.get("pushNotifications").asBoolean());
                    settings.setLanguage(data.get("language").asText());
                    settings.setTheme(data.get("theme").asText());
                    settings.setShareData(data.get("shareData").asBoolean());
                    settings.setShowProfile(data.get("showProfile").asBoolean());
                    return settings;
                }
                return new UserSettings(); // Default settings
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                userSettings = loadTask.getValue();
                displayUserSettings();
            });
        });
        
        new Thread(loadTask).start();
    }

    /**
     * نمایش تنظیمات در UI
     */
    private void displayUserSettings() {
        if (userSettings == null) return;
        
        emailNotificationCheckBox.setSelected(userSettings.isEmailNotifications());
        smsNotificationCheckBox.setSelected(userSettings.isSmsNotifications());
        pushNotificationCheckBox.setSelected(userSettings.isPushNotifications());
        languageComboBox.setValue(userSettings.getLanguage());
        themeComboBox.setValue(userSettings.getTheme());
        shareDataCheckBox.setSelected(userSettings.isShareData());
        showProfileCheckBox.setSelected(userSettings.isShowProfile());
    }

    /**
     * ذخیره تنظیمات
     */
    @FXML
    void handleSaveSettings() {
        setLoading(true);
        setStatus("در حال ذخیره تنظیمات...");
        
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Map<String, Object> settings = new HashMap<>();
                settings.put("emailNotifications", emailNotificationCheckBox.isSelected());
                settings.put("smsNotifications", smsNotificationCheckBox.isSelected());
                settings.put("pushNotifications", pushNotificationCheckBox.isSelected());
                settings.put("language", languageComboBox.getValue());
                settings.put("theme", themeComboBox.getValue());
                settings.put("shareData", shareDataCheckBox.isSelected());
                settings.put("showProfile", showProfileCheckBox.isSelected());
                
                HttpClientUtil.ApiResponse response = HttpClientUtil.put("/user/settings", settings);
                
                if (!response.isSuccess()) {
                    throw new RuntimeException(response.getMessage());
                }
                return null;
            }
        };
        
        saveTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                setStatus("تنظیمات ذخیره شد");
                showSuccess("موفقیت", "تنظیمات با موفقیت ذخیره شد");
            });
        });
        
        new Thread(saveTask).start();
    }

    // ========== Analytics Methods ==========
    
    /**
     * بروزرسانی آمار و تحلیل‌ها
     */
    private void updateAnalytics(String timeRange) {
        loadOrderAnalytics(timeRange);
        loadSpendingAnalytics(timeRange);
    }

    /**
     * بارگذاری آمار سفارشات
     */
    private void loadOrderAnalytics(String timeRange) {
        // TODO: implement order analytics loading
        // نمونه داده‌های آزمایشی
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("پیتزا", 35),
            new PieChart.Data("برگر", 25),
            new PieChart.Data("کباب", 20),
            new PieChart.Data("سالاد", 10),
            new PieChart.Data("سایر", 10)
        );
        orderDistributionChart.setData(pieChartData);
    }

    /**
     * بارگذاری آمار مالی
     */
    private void loadSpendingAnalytics(String timeRange) {
        // TODO: implement spending analytics loading
        // نمونه داده‌های آزمایشی
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("مخارج ماهانه");
        
        series.getData().add(new XYChart.Data<>("فروردین", 850000));
        series.getData().add(new XYChart.Data<>("اردیبهشت", 920000));
        series.getData().add(new XYChart.Data<>("خرداد", 780000));
        series.getData().add(new XYChart.Data<>("تیر", 1100000));
        
        spendingTrendChart.getData().clear();
        spendingTrendChart.getData().add(series);
        
        // بروزرسانی برچسب‌ها
        averageMonthlySpendLabel.setText("890,000 تومان");
        topRestaurantLabel.setText("رستوران ایتالیایی");
        topFoodItemLabel.setText("پیتزا مخصوص");
        totalOrdersAnalyticsLabel.setText("45 سفارش");
        totalSpentAnalyticsLabel.setText("3,650,000 تومان");
    }

    // ========== Helper Methods ==========
    
    /**
     * مدیریت تغییر تب
     */
    private void handleTabChange(Tab newTab) {
        if (newTab == ordersTab && ordersList.isEmpty()) {
            loadOrderHistory();
        } else if (newTab == transactionsTab && transactionsList.isEmpty()) {
            loadTransactionHistory();
        } else if (newTab == favoritesTab && favoriteRestaurants.isEmpty()) {
            loadFavorites();
        } else if (newTab == analyticsTab) {
            updateAnalytics(analyticsTimeRangeComboBox.getValue());
        }
    }

    /**
     * بررسی تغییرات پروفایل
     */
    private void checkProfileChanges() {
        if (currentUserProfile == null) return;
        
        boolean hasChanges = !fullNameField.getText().equals(currentUserProfile.getFullName()) ||
                           !emailField.getText().equals(currentUserProfile.getEmail());
        
        saveProfileButton.setDisable(!hasChanges);
    }

    /**
     * اعتبارسنجی فیلدهای رمز عبور
     */
    private void validatePasswordFields() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String currentPassword = currentPasswordField.getText();
        
        boolean passwordsMatch = newPassword.equals(confirmPassword);
        boolean hasCurrentPassword = !currentPassword.trim().isEmpty();
        boolean hasNewPassword = !newPassword.trim().isEmpty();
        boolean passwordLengthValid = newPassword.length() >= 4;
        
        changePasswordButton.setDisable(!(hasCurrentPassword && hasNewPassword && 
                                        passwordsMatch && passwordLengthValid));
    }

    /**
     * اعتبارسنجی داده‌های پروفایل
     */
    private boolean validateProfileData() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        
        if (fullName.isEmpty()) {
            showError("خطا", "نام کامل نمی‌تواند خالی باشد");
            return false;
        }
        
        if (!email.isEmpty() && !isValidEmail(email)) {
            showError("خطا", "فرمت ایمیل صحیح نیست");
            return false;
        }
        
        return true;
    }

    /**
     * بررسی صحت ایمیل
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    /**
     * پاک کردن فیلدهای رمز عبور
     */
    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    /**
     * بروزرسانی دکمه‌های علاقه‌مندی
     */
    private void updateFavoriteButtons() {
        boolean hasRestaurantSelection = favoriteRestaurantsList.getSelectionModel().getSelectedItem() != null;
        boolean hasItemSelection = favoriteItemsList.getSelectionModel().getSelectedItem() != null;
        
        removeFavoriteButton.setDisable(!hasRestaurantSelection && !hasItemSelection);
        reorderFavoriteButton.setDisable(!hasItemSelection);
    }

    /**
     * بروزرسانی آمار سفارشات
     */
    void updateOrderStatistics() {
        int totalOrders = ordersList.size();
        double totalSpent = ordersList.stream()
            .mapToDouble(order -> {
                String amount = order.getTotalAmount().replaceAll("[^\\d]", "");
                return Double.parseDouble(amount);
            })
            .sum();
        
        totalOrdersLabel.setText(String.valueOf(totalOrders) + " سفارش");
        totalSpentLabel.setText(String.format("%,.0f تومان", totalSpent));
    }

    /**
     * تولید رسید
     */
    private String generateReceipt(OrderHistoryItem order) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=================================\n");
        receipt.append("          رسید سفارش\n");
        receipt.append("=================================\n");
        receipt.append("شماره سفارش: ").append(order.getOrderId()).append("\n");
        receipt.append("تاریخ: ").append(order.getOrderDate()).append("\n");
        receipt.append("رستوران: ").append(order.getRestaurantName()).append("\n");
        receipt.append("---------------------------------\n");
        
        if (order.getItems() != null) {
            for (String item : order.getItems()) {
                receipt.append(item).append("\n");
            }
        }
        
        receipt.append("---------------------------------\n");
        receipt.append("مبلغ کل: ").append(order.getTotalAmount()).append("\n");
        receipt.append("وضعیت: ").append(order.getStatus()).append("\n");
        receipt.append("=================================\n");
        
        return receipt.toString();
    }

    /**
     * پردازش آیتم‌های سفارش از JSON
     */
    private List<String> parseOrderItems(JsonNode itemsNode) {
        List<String> items = new ArrayList<>();
        if (itemsNode != null && itemsNode.isArray()) {
            for (JsonNode item : itemsNode) {
                String itemStr = item.get("name").asText() + " x" + 
                               item.get("quantity").asInt() + " - " +
                               String.format("%,.0f تومان", item.get("price").asDouble());
                items.add(itemStr);
            }
        }
        return items;
    }

    /**
     * دریافت متن فارسی نقش کاربری
     */
    private String getRoleText(String role) {
        switch (role.toUpperCase()) {
            case "BUYER": return "خریدار";
            case "SELLER": return "فروشنده";
            case "COURIER": return "پیک";
            case "ADMIN": return "مدیر";
            default: return role;
        }
    }

    /**
     * دریافت متن فارسی وضعیت سفارش
     */
    private String getOrderStatusText(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return "در انتظار";
            case "PREPARING": return "در حال آماده‌سازی";
            case "READY": return "آماده ارسال";
            case "DELIVERING": return "در حال ارسال";
            case "DELIVERED": return "تحویل داده شده";
            case "CANCELLED": return "لغو شده";
            default: return status;
        }
    }

    /**
     * دریافت متن فارسی نوع تراکنش
     */
    private String getTransactionTypeText(String type) {
        switch (type.toUpperCase()) {
            case "DEPOSIT": return "واریز";
            case "WITHDRAWAL": return "برداشت";
            case "PAYMENT": return "پرداخت سفارش";
            case "REFUND": return "بازگشت وجه";
            default: return type;
        }
    }

    /**
     * فرمت تاریخ
     */
    private String formatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr.substring(0, 10));
            return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (Exception e) {
            return dateStr;
        }
    }

    /**
     * فرمت تاریخ و زمان
     */
    private String formatDateTime(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr.substring(0, 19));
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    // ========== UI Helper Methods ==========
    
    /**
     * تنظیم حالت بارگذاری
     */
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        progressBar.setVisible(loading);
        
        // غیرفعال کردن دکمه‌ها در حالت بارگذاری
        saveProfileButton.setDisable(loading);
        changePasswordButton.setDisable(loading);
        saveSettingsButton.setDisable(loading);
    }

    /**
     * نمایش پیام وضعیت
     */
    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * نمایش پیام خطا
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * نمایش پیام موفقیت
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * نمایش پیام اطلاعاتی
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // ========== Event Handlers ==========
    
    /**
     * افزودن آدرس جدید
     */
    @FXML
    private void handleAddAddress() {
        // TODO: نمایش دیالوگ افزودن آدرس
        showInfo("افزودن آدرس", "این قابلیت در نسخه بعدی اضافه خواهد شد");
    }

    /**
     * ویرایش آدرس انتخاب شده
     */
    @FXML
    private void handleEditAddress() {
        AddressItem selected = addressListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: نمایش دیالوگ ویرایش آدرس
            showInfo("ویرایش آدرس", "ویرایش آدرس: " + selected.getTitle());
        }
    }

    /**
     * حذف آدرس انتخاب شده
     */
    @FXML
    private void handleDeleteAddress() {
        AddressItem selected = addressListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("تأیید حذف");
            confirmAlert.setHeaderText("آیا مطمئن هستید؟");
            confirmAlert.setContentText("آیا می‌خواهید آدرس \"" + selected.getTitle() + "\" را حذف کنید؟");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // TODO: حذف آدرس از سرور
                    addressList.remove(selected);
                    showSuccess("موفقیت", "آدرس با موفقیت حذف شد");
                }
            });
        }
    }

    /**
     * حذف از علاقه‌مندی‌ها
     */
    @FXML
    void handleRemoveFavorite() {
        FavoriteRestaurant selectedRestaurant = favoriteRestaurantsList.getSelectionModel().getSelectedItem();
        FavoriteItem selectedItem = favoriteItemsList.getSelectionModel().getSelectedItem();
        
        if (selectedRestaurant != null) {
            // TODO: حذف رستوران از علاقه‌مندی‌ها
            favoriteRestaurants.remove(selectedRestaurant);
            showSuccess("موفقیت", "رستوران از علاقه‌مندی‌ها حذف شد");
        } else if (selectedItem != null) {
            // TODO: حذف آیتم از علاقه‌مندی‌ها
            favoriteItems.remove(selectedItem);
            showSuccess("موفقیت", "آیتم از علاقه‌مندی‌ها حذف شد");
        }
    }

    /**
     * سفارش مجدد آیتم مورد علاقه
     */
    @FXML
    private void handleReorderFavorite() {
        FavoriteItem selected = favoriteItemsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: افزودن به سبد خرید
            showSuccess("موفقیت", "آیتم \"" + selected.getName() + "\" به سبد خرید اضافه شد");
            navigationController.navigateTo(NavigationController.CART_SCENE);
        }
    }

    /**
     * اعمال فیلتر سفارشات
     */
    @FXML
    private void handleApplyOrderFilter() {
        filterOrders();
        setStatus("فیلترها اعمال شد");
    }

    /**
     * پاک کردن فیلتر سفارشات
     */
    @FXML
    private void handleClearOrderFilter() {
        orderSearchField.clear();
        orderStatusFilter.setValue("همه");
        orderStartDate.setValue(LocalDate.now().minusMonths(1));
        orderEndDate.setValue(LocalDate.now());
        ordersTable.setItems(ordersList);
        setStatus("فیلترها پاک شد");
    }

    /**
     * دانلود گزارش تراکنش‌ها
     */
    @FXML
    void handleDownloadTransactionReport() {
        try {
            String report = generateTransactionReport();
            File file = new File("transaction_report_" + LocalDate.now() + ".txt");
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(report);
            }
            
            showSuccess("موفقیت", "گزارش در فایل " + file.getName() + " ذخیره شد");
        } catch (IOException e) {
            showError("خطا", "خطا در ذخیره گزارش: " + e.getMessage());
        }
    }

    /**
     * بازنشانی تنظیمات
     */
    @FXML
    void handleResetSettings() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأیید بازنشانی");
        confirmAlert.setHeaderText("آیا مطمئن هستید؟");
        confirmAlert.setContentText("تمام تنظیمات به حالت پیش‌فرض بازگردانده خواهند شد.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                emailNotificationCheckBox.setSelected(true);
                smsNotificationCheckBox.setSelected(false);
                pushNotificationCheckBox.setSelected(true);
                languageComboBox.setValue("فارسی");
                themeComboBox.setValue("روشن");
                shareDataCheckBox.setSelected(false);
                showProfileCheckBox.setSelected(true);
                showSuccess("موفقیت", "تنظیمات به حالت پیش‌فرض بازگردانده شد");
            }
        });
    }

    /**
     * تولید گزارش تراکنش‌ها
     */
    private String generateTransactionReport() {
        StringBuilder report = new StringBuilder();
        report.append("=====================================\n");
        report.append("        گزارش تراکنش‌های مالی\n");
        report.append("=====================================\n");
        report.append("تاریخ گزارش: ").append(LocalDate.now()).append("\n");
        report.append("تعداد تراکنش‌ها: ").append(transactionsList.size()).append("\n");
        report.append("-------------------------------------\n\n");
        
        for (TransactionItem trans : transactionsList) {
            report.append("شماره تراکنش: ").append(trans.getTransactionId()).append("\n");
            report.append("تاریخ: ").append(trans.getDate()).append("\n");
            report.append("نوع: ").append(trans.getType()).append("\n");
            report.append("مبلغ: ").append(trans.getAmount()).append("\n");
            report.append("توضیحات: ").append(trans.getDescription()).append("\n");
            report.append("موجودی پس از تراکنش: ").append(trans.getBalanceAfter()).append("\n");
            report.append("-------------------------------------\n");
        }
        
        return report.toString();
    }

    // ========== Inner Classes ==========
    
    /**
     * کلاس مدل پروفایل کاربر
     */
    public static class UserProfile {
        private Long id;
        private String fullName;
        private String phone;
        private String email;
        private String role;
        private boolean active;
        private double walletBalance;
        private String createdAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public double getWalletBalance() { return walletBalance; }
        public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    /**
     * کلاس مدل آیتم آدرس
     */
    public static class AddressItem {
        private Long id;
        private String title;
        private String address;
        private boolean isDefault;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public boolean isDefault() { return isDefault; }
        public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
        
        @Override
        public String toString() {
            return title + (isDefault ? " (پیش‌فرض)" : "") + "\n" + address;
        }
    }

    /**
     * کلاس مدل آیتم تاریخچه سفارش
     */
    public static class OrderHistoryItem {
        private String orderId;
        private String orderDate;
        private String restaurantName;
        private String totalAmount;
        private String status;
        private List<String> items;

        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getOrderDate() { return orderDate; }
        public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
        
        public String getRestaurantName() { return restaurantName; }
        public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
        
        public String getTotalAmount() { return totalAmount; }
        public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
    }

    /**
     * کلاس مدل آیتم تراکنش
     */
    public static class TransactionItem {
        private String transactionId;
        private String date;
        private String type;
        private String amount;
        private String description;
        private String balanceAfter;

        // Getters and Setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getBalanceAfter() { return balanceAfter; }
        public void setBalanceAfter(String balanceAfter) { this.balanceAfter = balanceAfter; }
    }

    /**
     * کلاس مدل رستوران مورد علاقه
     */
    public static class FavoriteRestaurant {
        private Long id;
        private Long restaurantId;
        private String name;
        private double rating;
        private int orderCount;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getRestaurantId() { return restaurantId; }
        public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }
        
        public int getOrderCount() { return orderCount; }
        public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
        
        @Override
        public String toString() {
            return name + " (" + rating + " ⭐) - " + orderCount + " سفارش";
        }
    }

    /**
     * کلاس مدل آیتم مورد علاقه
     */
    public static class FavoriteItem {
        private Long id;
        private Long itemId;
        private String name;
        private String restaurantName;
        private double price;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getRestaurantName() { return restaurantName; }
        public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        @Override
        public String toString() {
            return name + " - " + restaurantName + " (" + String.format("%,.0f تومان", price) + ")";
        }
    }

    /**
     * کلاس مدل تنظیمات کاربر
     */
    public static class UserSettings {
        private boolean emailNotifications = true;
        private boolean smsNotifications = false;
        private boolean pushNotifications = true;
        private String language = "فارسی";
        private String theme = "روشن";
        private boolean shareData = false;
        private boolean showProfile = true;

        // Getters and Setters
        public boolean isEmailNotifications() { return emailNotifications; }
        public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }
        
        public boolean isSmsNotifications() { return smsNotifications; }
        public void setSmsNotifications(boolean smsNotifications) { this.smsNotifications = smsNotifications; }
        
        public boolean isPushNotifications() { return pushNotifications; }
        public void setPushNotifications(boolean pushNotifications) { this.pushNotifications = pushNotifications; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public boolean isShareData() { return shareData; }
        public void setShareData(boolean shareData) { this.shareData = shareData; }
        
        public boolean isShowProfile() { return showProfile; }
        public void setShowProfile(boolean showProfile) { this.showProfile = showProfile; }
    }

    /**
     * کلاس مدل آمار کاربر
     */
    public static class UserAnalytics {
        private int totalOrders;
        private double totalSpent;
        private double averageOrderValue;
        private String topRestaurant;
        private String topFoodItem;
        private Map<String, Integer> orderDistribution;
        private Map<String, Double> monthlySpending;

        // Getters and Setters
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
        
        public double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
        
        public double getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
        
        public String getTopRestaurant() { return topRestaurant; }
        public void setTopRestaurant(String topRestaurant) { this.topRestaurant = topRestaurant; }
        
        public String getTopFoodItem() { return topFoodItem; }
        public void setTopFoodItem(String topFoodItem) { this.topFoodItem = topFoodItem; }
        
        public Map<String, Integer> getOrderDistribution() { return orderDistribution; }
        public void setOrderDistribution(Map<String, Integer> orderDistribution) { this.orderDistribution = orderDistribution; }
        
        public Map<String, Double> getMonthlySpending() { return monthlySpending; }
        public void setMonthlySpending(Map<String, Double> monthlySpending) { this.monthlySpending = monthlySpending; }
    }
} 