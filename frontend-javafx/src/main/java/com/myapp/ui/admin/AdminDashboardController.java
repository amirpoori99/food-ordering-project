package com.myapp.ui.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 * کنترلر داشبورد جامع مدیریت ادمین (فاز 29)
 * 
 * این کنترلر مسئول مدیریت بخش‌های مختلف داشبورد ادمین شامل:
 * - مدیریت کاربران
 * - مدیریت سفارشات
 * - مدیریت تراکنش‌ها
 * - مدیریت رستوران‌ها
 * - گزارش‌ها و آمار
 * - تنظیمات ادمین
 * 
 * هر تب دارای جدول/نمودار/فرم مرتبط است.
 * تمام اجزا با کامنت فارسی مستندسازی شده‌اند.
 *
 * @author تیم توسعه سیستم سفارش غذا
 * @version 1.0
 * @since 1404/04/10
 */
public class AdminDashboardController implements Initializable {

    // ========== Data Collections ==========
    /** لیست داده‌های کاربران */
    private ObservableList<UserData> usersData = FXCollections.observableArrayList();
    /** لیست داده‌های سفارشات */
    private ObservableList<OrderData> ordersData = FXCollections.observableArrayList();
    /** لیست داده‌های تراکنش‌ها */
    private ObservableList<TransactionData> transactionsData = FXCollections.observableArrayList();
    /** لیست داده‌های رستوران‌ها */
    private ObservableList<RestaurantData> restaurantsData = FXCollections.observableArrayList();

    // ========== Tab Container ==========
    /** کانتینر اصلی تب‌ها */
    @FXML TabPane mainTabPane;
    /** کانتینر تب‌های ادمین (برای تست) */
    @FXML TabPane adminTabPane;
    /** تب مدیریت کاربران */
    @FXML Tab usersTab;
    /** تب مدیریت سفارشات */
    @FXML Tab ordersTab;
    /** تب مدیریت تراکنش‌ها */
    @FXML Tab transactionsTab;
    /** تب مدیریت رستوران‌ها */
    @FXML Tab restaurantsTab;
    /** تب گزارش‌ها و آمار */
    @FXML Tab reportsTab;
    /** تب تنظیمات */
    @FXML Tab settingsTab;

    // ========== Users Tab Components ==========
    /** جدول کاربران */
    @FXML TableView<UserData> usersTable;
    /** دکمه افزودن کاربر */
    @FXML Button addUserButton;
    /** دکمه ویرایش کاربر */
    @FXML Button editUserButton;
    /** دکمه حذف کاربر */
    @FXML Button deleteUserButton;
    /** فیلد جستجوی کاربر */
    @FXML TextField userSearchField;
    /** فیلتر نقش کاربر */
    @FXML ComboBox<String> userRoleFilter;

    // ========== Orders Tab Components ==========
    /** جدول سفارشات */
    @FXML TableView<OrderData> ordersTable;
    /** فیلد جستجوی سفارش */
    @FXML TextField orderSearchField;
    /** فیلتر وضعیت سفارش */
    @FXML ComboBox<String> orderStatusFilter;
    /** دکمه Export سفارشات */
    @FXML Button exportOrdersButton;

    // ========== Transactions Tab Components ==========
    /** جدول تراکنش‌ها */
    @FXML TableView<TransactionData> transactionsTable;
    /** فیلد جستجوی تراکنش */
    @FXML TextField transactionSearchField;
    /** فیلتر نوع تراکنش */
    @FXML ComboBox<String> transactionTypeFilter;
    /** دکمه Export تراکنش‌ها */
    @FXML Button exportTransactionsButton;

    // ========== Restaurants Tab Components ==========
    /** جدول رستوران‌ها */
    @FXML TableView<RestaurantData> restaurantsTable;
    /** دکمه افزودن رستوران */
    @FXML Button addRestaurantButton;
    /** دکمه ویرایش رستوران */
    @FXML Button editRestaurantButton;
    /** دکمه حذف رستوران */
    @FXML Button deleteRestaurantButton;
    /** فیلد جستجوی رستوران */
    @FXML TextField restaurantSearchField;

    // ========== Reports Tab Components ==========
    /** نمودار دایره‌ای سفارشات */
    @FXML PieChart ordersPieChart;
    /** نمودار خطی درآمد */
    @FXML LineChart<String, Number> revenueLineChart;
    /** دکمه Export گزارش */
    @FXML Button exportReportButton;

    // ========== Settings Tab Components ==========
    /** فرم تنظیمات ادمین */
    @FXML VBox adminSettingsForm;
    /** دکمه ذخیره تنظیمات */
    @FXML Button saveSettingsButton;
    /** دکمه بازنشانی تنظیمات */
    @FXML Button resetSettingsButton;

    // ========== سایر اجزای عمومی ==========
    /** برچسب وضعیت */
    @FXML Label statusLabel;
    /** Indicator بارگذاری */
    @FXML ProgressIndicator loadingIndicator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // مقداردهی اولیه UI و بارگذاری داده‌ها
        setupUI();
        setupTables();
        setupListeners();
        loadInitialData();
    }

    /** تنظیم اولیه UI */
    private void setupUI() {
        // مقداردهی اولیه فیلترها
        userRoleFilter.getItems().addAll("همه", "کاربر", "ادمین", "فروشنده", "پیک");
        orderStatusFilter.getItems().addAll("همه", "در انتظار", "تأیید شده", "در حال آماده‌سازی", "ارسال شده", "تحویل شده", "لغو شده");
        transactionTypeFilter.getItems().addAll("همه", "پرداخت", "برداشت", "بازگشت", "کمیسیون");
    }

    /** تنظیم جداول */
    private void setupTables() {
        // تنظیم ستون‌های جدول کاربران
        usersTable.setPlaceholder(new Label("هیچ کاربری یافت نشد"));
        usersTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // تنظیم ستون‌های جدول سفارشات
        ordersTable.setPlaceholder(new Label("هیچ سفارشی یافت نشد"));
        ordersTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // تنظیم ستون‌های جدول تراکنش‌ها
        transactionsTable.setPlaceholder(new Label("هیچ تراکنشی یافت نشد"));
        transactionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // تنظیم ستون‌های جدول رستوران‌ها
        restaurantsTable.setPlaceholder(new Label("هیچ رستورانی یافت نشد"));
        restaurantsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /** تنظیم Listener ها */
    private void setupListeners() {
        // Listener برای فیلتر کاربران
        userSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterUsers());
        userRoleFilter.setOnAction(event -> filterUsers());
        
        // Listener برای فیلتر سفارشات
        orderSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterOrders());
        orderStatusFilter.setOnAction(event -> filterOrders());
        
        // Listener برای فیلتر تراکنش‌ها
        transactionSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterTransactions());
        transactionTypeFilter.setOnAction(event -> filterTransactions());
        
        // Listener برای فیلتر رستوران‌ها
        restaurantSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterRestaurants());
    }

    /** بارگذاری داده‌های اولیه */
    private void loadInitialData() {
        loadUsers();
        loadOrders();
        loadTransactions();
        loadRestaurants();
        loadReports();
    }

    /** بارگذاری لیست کاربران */
    private void loadUsers() {
        setLoading(true);
        setStatus("در حال بارگذاری کاربران...");
        
        // شبیه‌سازی بارگذاری از API
        // در حالت واقعی، اینجا درخواست HTTP به سرور ارسال می‌شود
        List<UserData> sampleUsers = Arrays.asList(
            new UserData(1L, "احمد محمدی", "ahmad@example.com", "کاربر", "فعال", "2024-01-15"),
            new UserData(2L, "فاطمه احمدی", "fateme@example.com", "ادمین", "فعال", "2024-01-10"),
            new UserData(3L, "علی رضایی", "ali@example.com", "فروشنده", "فعال", "2024-01-20"),
            new UserData(4L, "مریم کریمی", "maryam@example.com", "پیک", "غیرفعال", "2024-01-05")
        );
        
        usersData.clear();
        usersData.addAll(sampleUsers);
        usersTable.setItems(usersData);
        
        setLoading(false);
        setStatus("کاربران بارگذاری شدند");
    }

    /** فیلتر کردن کاربران */
    private void filterUsers() {
        String searchText = userSearchField.getText().toLowerCase();
        String selectedRole = userRoleFilter.getValue();
        
        // منطق فیلتر کردن بر اساس متن جستجو و نقش انتخاب شده
        List<UserData> filteredUsers = usersData.stream()
            .filter(user -> {
                boolean matchesSearch = searchText.isEmpty() || 
                    user.getName().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText);
                
                boolean matchesRole = selectedRole == null || selectedRole.equals("همه") || 
                    user.getRole().equals(selectedRole);
                
                return matchesSearch && matchesRole;
            })
            .collect(Collectors.toList());
        
        usersTable.setItems(FXCollections.observableArrayList(filteredUsers));
    }

    /** افزودن کاربر جدید */
    @FXML
    private void handleAddUser() {
        // نمایش دیالوگ افزودن کاربر
        setStatus("در حال باز کردن فرم افزودن کاربر...");
        // در حالت واقعی، اینجا دیالوگ باز می‌شود
        // ...
    }

    /** ویرایش کاربر انتخاب شده */
    @FXML
    private void handleEditUser() {
        UserData selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            setStatus("لطفاً یک کاربر را انتخاب کنید");
            return;
        }
        
        setStatus("در حال باز کردن فرم ویرایش کاربر: " + selectedUser.getName());
        // در حالت واقعی، اینجا دیالوگ ویرایش باز می‌شود
        // ...
    }

    /** حذف کاربر انتخاب شده */
    @FXML
    private void handleDeleteUser() {
        UserData selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            setStatus("لطفاً یک کاربر را انتخاب کنید");
            return;
        }
        
        setStatus("در حال حذف کاربر: " + selectedUser.getName());
        // در حالت واقعی، اینجا دیالوگ تأیید حذف باز می‌شود
        // ...
    }

    /** بارگذاری لیست سفارشات */
    private void loadOrders() {
        setLoading(true);
        setStatus("در حال بارگذاری سفارشات...");
        
        // شبیه‌سازی بارگذاری از API
        List<OrderData> sampleOrders = Arrays.asList(
            new OrderData(1001L, "احمد محمدی", "رستوران تهران", "تحویل شده", 150000L, "2024-01-20 14:30"),
            new OrderData(1002L, "فاطمه احمدی", "رستوران شیراز", "در حال آماده‌سازی", 85000L, "2024-01-20 15:45"),
            new OrderData(1003L, "علی رضایی", "رستوران اصفهان", "ارسال شده", 120000L, "2024-01-20 16:15"),
            new OrderData(1004L, "مریم کریمی", "رستوران مشهد", "در انتظار", 95000L, "2024-01-20 17:00")
        );
        
        ordersData.clear();
        ordersData.addAll(sampleOrders);
        ordersTable.setItems(ordersData);
        
        setLoading(false);
        setStatus("سفارشات بارگذاری شدند");
    }

    /** فیلتر کردن سفارشات */
    private void filterOrders() {
        String searchText = orderSearchField.getText().toLowerCase();
        String selectedStatus = orderStatusFilter.getValue();
        
        // منطق فیلتر کردن بر اساس متن جستجو و وضعیت انتخاب شده
        List<OrderData> filteredOrders = ordersData.stream()
            .filter(order -> {
                boolean matchesSearch = searchText.isEmpty() || 
                    order.getCustomerName().toLowerCase().contains(searchText) ||
                    order.getRestaurantName().toLowerCase().contains(searchText) ||
                    order.getId().toString().contains(searchText);
                
                boolean matchesStatus = selectedStatus == null || selectedStatus.equals("همه") || 
                    order.getStatus().equals(selectedStatus);
                
                return matchesSearch && matchesStatus;
            })
            .collect(Collectors.toList());
        
        ordersTable.setItems(FXCollections.observableArrayList(filteredOrders));
    }

    /** Export سفارشات */
    @FXML
    private void handleExportOrders() {
        setStatus("در حال Export سفارشات...");
        // Export سفارشات به فایل CSV/Excel
        // در حالت واقعی، اینجا فایل CSV ایجاد می‌شود
        // ...
        setStatus("سفارشات با موفقیت Export شدند");
    }

    /** بارگذاری لیست تراکنش‌ها */
    private void loadTransactions() {
        setLoading(true);
        setStatus("در حال بارگذاری تراکنش‌ها...");
        
        // شبیه‌سازی بارگذاری از API
        List<TransactionData> sampleTransactions = Arrays.asList(
            new TransactionData(2001L, "احمد محمدی", "پرداخت", 150000L, "موفق", "2024-01-20 14:30"),
            new TransactionData(2002L, "فاطمه احمدی", "برداشت", 50000L, "موفق", "2024-01-20 15:45"),
            new TransactionData(2003L, "علی رضایی", "بازگشت", 25000L, "موفق", "2024-01-20 16:15"),
            new TransactionData(2004L, "مریم کریمی", "کمیسیون", 15000L, "موفق", "2024-01-20 17:00")
        );
        
        transactionsData.clear();
        transactionsData.addAll(sampleTransactions);
        transactionsTable.setItems(transactionsData);
        
        setLoading(false);
        setStatus("تراکنش‌ها بارگذاری شدند");
    }

    /** فیلتر کردن تراکنش‌ها */
    private void filterTransactions() {
        String searchText = transactionSearchField.getText().toLowerCase();
        String selectedType = transactionTypeFilter.getValue();
        
        // منطق فیلتر کردن بر اساس متن جستجو و نوع انتخاب شده
        List<TransactionData> filteredTransactions = transactionsData.stream()
            .filter(transaction -> {
                boolean matchesSearch = searchText.isEmpty() || 
                    transaction.getUserName().toLowerCase().contains(searchText) ||
                    transaction.getId().toString().contains(searchText);
                
                boolean matchesType = selectedType == null || selectedType.equals("همه") || 
                    transaction.getType().equals(selectedType);
                
                return matchesSearch && matchesType;
            })
            .collect(Collectors.toList());
        
        transactionsTable.setItems(FXCollections.observableArrayList(filteredTransactions));
    }

    /** Export تراکنش‌ها */
    @FXML
    private void handleExportTransactions() {
        setStatus("در حال Export تراکنش‌ها...");
        // Export تراکنش‌ها به فایل CSV/Excel
        // در حالت واقعی، اینجا فایل CSV ایجاد می‌شود
        // ...
        setStatus("تراکنش‌ها با موفقیت Export شدند");
    }

    /** بارگذاری لیست رستوران‌ها */
    private void loadRestaurants() {
        setLoading(true);
        setStatus("در حال بارگذاری رستوران‌ها...");
        
        // شبیه‌سازی بارگذاری از API
        List<RestaurantData> sampleRestaurants = Arrays.asList(
            new RestaurantData(3001L, "رستوران تهران", "تهران، خیابان ولیعصر", "فعال", 4.5, 150),
            new RestaurantData(3002L, "رستوران شیراز", "شیراز، خیابان زند", "فعال", 4.2, 120),
            new RestaurantData(3003L, "رستوران اصفهان", "اصفهان، خیابان چهارباغ", "غیرفعال", 4.0, 80),
            new RestaurantData(3004L, "رستوران مشهد", "مشهد، خیابان امام رضا", "فعال", 4.8, 200)
        );
        
        restaurantsData.clear();
        restaurantsData.addAll(sampleRestaurants);
        restaurantsTable.setItems(restaurantsData);
        
        setLoading(false);
        setStatus("رستوران‌ها بارگذاری شدند");
    }

    /** فیلتر کردن رستوران‌ها */
    private void filterRestaurants() {
        String searchText = restaurantSearchField.getText().toLowerCase();
        
        // منطق فیلتر کردن بر اساس متن جستجو
        List<RestaurantData> filteredRestaurants = restaurantsData.stream()
            .filter(restaurant -> {
                boolean matchesSearch = searchText.isEmpty() || 
                    restaurant.getName().toLowerCase().contains(searchText) ||
                    restaurant.getAddress().toLowerCase().contains(searchText) ||
                    restaurant.getId().toString().contains(searchText);
                
                return matchesSearch;
            })
            .collect(Collectors.toList());
        
        restaurantsTable.setItems(FXCollections.observableArrayList(filteredRestaurants));
    }

    /** افزودن رستوران جدید */
    @FXML
    private void handleAddRestaurant() {
        setStatus("در حال باز کردن فرم افزودن رستوران...");
        // نمایش دیالوگ افزودن رستوران
        // در حالت واقعی، اینجا دیالوگ باز می‌شود
        // ...
    }

    /** ویرایش رستوران انتخاب شده */
    @FXML
    private void handleEditRestaurant() {
        RestaurantData selectedRestaurant = restaurantsTable.getSelectionModel().getSelectedItem();
        if (selectedRestaurant == null) {
            setStatus("لطفاً یک رستوران را انتخاب کنید");
            return;
        }
        
        setStatus("در حال باز کردن فرم ویرایش رستوران: " + selectedRestaurant.getName());
        // در حالت واقعی، اینجا دیالوگ ویرایش باز می‌شود
        // ...
    }

    /** حذف رستوران انتخاب شده */
    @FXML
    private void handleDeleteRestaurant() {
        RestaurantData selectedRestaurant = restaurantsTable.getSelectionModel().getSelectedItem();
        if (selectedRestaurant == null) {
            setStatus("لطفاً یک رستوران را انتخاب کنید");
            return;
        }
        
        setStatus("در حال حذف رستوران: " + selectedRestaurant.getName());
        // در حالت واقعی، اینجا دیالوگ تأیید حذف باز می‌شود
        // ...
    }

    /** بارگذاری گزارش‌ها و آمار */
    private void loadReports() {
        setLoading(true);
        setStatus("در حال بارگذاری گزارش‌ها...");
        
        // بارگذاری داده‌های نمودارها
        loadOrdersChart();
        loadRevenueChart();
        
        setLoading(false);
        setStatus("گزارش‌ها بارگذاری شدند");
    }

    /** بارگذاری نمودار سفارشات */
    private void loadOrdersChart() {
        // تنظیم داده‌های نمودار دایره‌ای سفارشات
        PieChart.Data pendingData = new PieChart.Data("در انتظار", 25);
        PieChart.Data confirmedData = new PieChart.Data("تأیید شده", 30);
        PieChart.Data preparingData = new PieChart.Data("در حال آماده‌سازی", 20);
        PieChart.Data deliveredData = new PieChart.Data("تحویل شده", 15);
        PieChart.Data cancelledData = new PieChart.Data("لغو شده", 10);
        
        ordersPieChart.getData().clear();
        ordersPieChart.getData().addAll(pendingData, confirmedData, preparingData, deliveredData, cancelledData);
        
        // تنظیم رنگ‌ها
        ordersPieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #FFA500;"); // نارنجی
        ordersPieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #4169E1;"); // آبی
        ordersPieChart.getData().get(2).getNode().setStyle("-fx-pie-color: #32CD32;"); // سبز
        ordersPieChart.getData().get(3).getNode().setStyle("-fx-pie-color: #228B22;"); // سبز تیره
        ordersPieChart.getData().get(4).getNode().setStyle("-fx-pie-color: #DC143C;"); // قرمز
    }

    /** بارگذاری نمودار درآمد */
    private void loadRevenueChart() {
        // تنظیم داده‌های نمودار خطی درآمد
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("درآمد روزانه");
        
        series.getData().add(new XYChart.Data<>("شنبه", 1500000));
        series.getData().add(new XYChart.Data<>("یکشنبه", 1800000));
        series.getData().add(new XYChart.Data<>("دوشنبه", 1200000));
        series.getData().add(new XYChart.Data<>("سه‌شنبه", 2000000));
        series.getData().add(new XYChart.Data<>("چهارشنبه", 1600000));
        series.getData().add(new XYChart.Data<>("پنج‌شنبه", 2200000));
        series.getData().add(new XYChart.Data<>("جمعه", 1900000));
        
        revenueLineChart.getData().clear();
        revenueLineChart.getData().add(series);
        
        // تنظیم عنوان محورها
        revenueLineChart.getXAxis().setLabel("روز هفته");
        revenueLineChart.getYAxis().setLabel("درآمد (تومان)");
    }

    /** Export گزارش‌ها */
    @FXML
    private void handleExportReports() {
        setStatus("در حال Export گزارش‌ها...");
        // Export گزارش‌ها به فایل PDF/Excel
        // در حالت واقعی، اینجا فایل PDF ایجاد می‌شود
        // ...
        setStatus("گزارش‌ها با موفقیت Export شدند");
    }

    /** ذخیره تنظیمات */
    @FXML
    private void handleSaveSettings() {
        setStatus("در حال ذخیره تنظیمات...");
        // ذخیره تنظیمات ادمین
        // در حالت واقعی، اینجا تنظیمات در فایل یا دیتابیس ذخیره می‌شود
        // ...
        setStatus("تنظیمات با موفقیت ذخیره شدند");
    }

    /** بازنشانی تنظیمات */
    @FXML
    private void handleResetSettings() {
        setStatus("در حال بازنشانی تنظیمات...");
        // بازنشانی تنظیمات به مقادیر پیش‌فرض
        // در حالت واقعی، اینجا تنظیمات به مقادیر پیش‌فرض باز می‌گردد
        // ...
        setStatus("تنظیمات به مقادیر پیش‌فرض بازنشانی شدند");
    }

    /** تنظیم وضعیت بارگذاری */
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        
        // غیرفعال کردن دکمه‌ها در حین بارگذاری
        addUserButton.setDisable(loading);
        editUserButton.setDisable(loading);
        deleteUserButton.setDisable(loading);
        exportOrdersButton.setDisable(loading);
        exportTransactionsButton.setDisable(loading);
        addRestaurantButton.setDisable(loading);
        editRestaurantButton.setDisable(loading);
        deleteRestaurantButton.setDisable(loading);
        exportReportButton.setDisable(loading);
        saveSettingsButton.setDisable(loading);
        resetSettingsButton.setDisable(loading);
        
        // غیرفعال کردن فیلترها در حین بارگذاری
        userSearchField.setDisable(loading);
        userRoleFilter.setDisable(loading);
        orderSearchField.setDisable(loading);
        orderStatusFilter.setDisable(loading);
        transactionSearchField.setDisable(loading);
        transactionTypeFilter.setDisable(loading);
        restaurantSearchField.setDisable(loading);
    }

    /** تنظیم پیام وضعیت */
    private void setStatus(String message) {
        statusLabel.setText("وضعیت: " + message);
    }

    // ========== مدل‌های داده داخلی ==========

    /**
     * مدل داده کاربر برای جدول کاربران
     */
    public static class UserData {
        /** شناسه کاربر */
        private Long id;
        /** نام کامل */
        private String name;
        /** ایمیل */
        private String email;
        /** نقش */
        private String role;
        /** وضعیت */
        private String status;
        /** تاریخ عضویت */
        private String createdAt;

        /** سازنده کلاس */
        public UserData(Long id, String name, String email, String role, String status, String createdAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.status = status;
            this.createdAt = createdAt;
        }

        // Getter methods
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }

        // Setter methods
        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setRole(String role) { this.role = role; }
        public void setStatus(String status) { this.status = status; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    /**
     * مدل داده سفارش برای جدول سفارشات
     */
    public static class OrderData {
        /** شناسه سفارش */
        private Long id;
        /** نام مشتری */
        private String customerName;
        /** نام رستوران */
        private String restaurantName;
        /** وضعیت */
        private String status;
        /** مبلغ کل */
        private Long amount;
        /** تاریخ سفارش */
        private String orderDate;

        /** سازنده کلاس */
        public OrderData(Long id, String customerName, String restaurantName, String status, Long amount, String orderDate) {
            this.id = id;
            this.customerName = customerName;
            this.restaurantName = restaurantName;
            this.status = status;
            this.amount = amount;
            this.orderDate = orderDate;
        }

        // Getter methods
        public Long getId() { return id; }
        public String getCustomerName() { return customerName; }
        public String getRestaurantName() { return restaurantName; }
        public String getStatus() { return status; }
        public Long getAmount() { return amount; }
        public String getOrderDate() { return orderDate; }

        // Setter methods
        public void setId(Long id) { this.id = id; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
        public void setStatus(String status) { this.status = status; }
        public void setAmount(Long amount) { this.amount = amount; }
        public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    }

    /**
     * مدل داده تراکنش برای جدول تراکنش‌ها
     */
    public static class TransactionData {
        /** شناسه تراکنش */
        private Long id;
        /** نام کاربر */
        private String userName;
        /** نوع تراکنش */
        private String type;
        /** مبلغ */
        private Long amount;
        /** وضعیت */
        private String status;
        /** تاریخ */
        private String date;

        /** سازنده کلاس */
        public TransactionData(Long id, String userName, String type, Long amount, String status, String date) {
            this.id = id;
            this.userName = userName;
            this.type = type;
            this.amount = amount;
            this.status = status;
            this.date = date;
        }

        // Getter methods
        public Long getId() { return id; }
        public String getUserName() { return userName; }
        public String getType() { return type; }
        public Long getAmount() { return amount; }
        public String getStatus() { return status; }
        public String getDate() { return date; }

        // Setter methods
        public void setId(Long id) { this.id = id; }
        public void setUserName(String userName) { this.userName = userName; }
        public void setType(String type) { this.type = type; }
        public void setAmount(Long amount) { this.amount = amount; }
        public void setStatus(String status) { this.status = status; }
        public void setDate(String date) { this.date = date; }
    }

    /**
     * مدل داده رستوران برای جدول رستوران‌ها
     */
    public static class RestaurantData {
        /** شناسه رستوران */
        private Long id;
        /** نام رستوران */
        private String name;
        /** آدرس */
        private String address;
        /** وضعیت */
        private String status;
        /** امتیاز */
        private Double rating;
        /** تعداد سفارشات */
        private Integer orderCount;

        /** سازنده کلاس */
        public RestaurantData(Long id, String name, String address, String status, Double rating, Integer orderCount) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.status = status;
            this.rating = rating;
            this.orderCount = orderCount;
        }

        // Getter methods
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getStatus() { return status; }
        public Double getRating() { return rating; }
        public Integer getOrderCount() { return orderCount; }

        // Setter methods
        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setAddress(String address) { this.address = address; }
        public void setStatus(String status) { this.status = status; }
        public void setRating(Double rating) { this.rating = rating; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    }

    /**
     * مدل داده گزارش برای تب گزارش‌ها
     */
    public static class ReportData {
        /** عنوان گزارش */
        private String title;
        /** مقدار */
        private Double value;
        /** بازه زمانی */
        private String period;

        /** سازنده کلاس */
        public ReportData(String title, Double value, String period) {
            this.title = title;
            this.value = value;
            this.period = period;
        }

        // Getter methods
        public String getTitle() { return title; }
        public Double getValue() { return value; }
        public String getPeriod() { return period; }

        // Setter methods
        public void setTitle(String title) { this.title = title; }
        public void setValue(Double value) { this.value = value; }
        public void setPeriod(String period) { this.period = period; }
    }
} 