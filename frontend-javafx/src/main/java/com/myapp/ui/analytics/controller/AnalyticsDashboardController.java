package com.myapp.ui.analytics.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.myapp.ui.analytics.service.AnalyticsUIService;
import com.myapp.ui.analytics.service.ChartDataService;
import com.myapp.ui.analytics.service.DashboardUpdateService;
import com.myapp.ui.analytics.util.ChartUtil;
import com.myapp.ui.analytics.util.DataFormatter;
import com.myapp.ui.analytics.util.DashboardUtil;

/**
 * کنترلر اصلی داشبورد Analytics
 * مسئول مدیریت نمایش آمار و گزارش‌های تحلیلی
 * 
 * @author Food Ordering System
 * @version 1.0
 * @since 2024
 */
public class AnalyticsDashboardController implements Initializable {

    // ==================== FXML Components ====================
    
    @FXML private VBox mainContainer;
    @FXML private HBox headerContainer;
    @FXML private VBox sidebarContainer;
    @FXML private VBox contentContainer;
    @FXML private HBox statsContainer;
    @FXML private VBox chartsContainer;
    @FXML private VBox tablesContainer;
    
    // Header Components
    @FXML private Label titleLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Button settingsButton;
    
    // Sidebar Components
    @FXML private VBox navigationContainer;
    @FXML private Button overviewButton;
    @FXML private Button salesButton;
    @FXML private Button usersButton;
    @FXML private Button restaurantsButton;
    @FXML private Button performanceButton;
    @FXML private Button realtimeButton;
    @FXML private Button reportsButton;
    
    // Stats Cards
    @FXML private VBox totalUsersCard;
    @FXML private VBox totalRestaurantsCard;
    @FXML private VBox totalOrdersCard;
    @FXML private VBox totalRevenueCard;
    @FXML private VBox activeUsersCard;
    @FXML private VBox pendingOrdersCard;
    
    // Charts
    @FXML private LineChart<String, Number> salesTrendChart;
    @FXML private PieChart userDistributionChart;
    @FXML private BarChart<String, Number> restaurantPerformanceChart;
    @FXML private AreaChart<String, Number> revenueChart;
    
    // Tables
    @FXML private TableView<Map<String, Object>> topRestaurantsTable;
    @FXML private TableView<Map<String, Object>> recentOrdersTable;
    @FXML private TableView<Map<String, Object>> userActivityTable;
    
    // ==================== Services ====================
    
    private AnalyticsUIService analyticsUIService;
    private ChartDataService chartDataService;
    private DashboardUpdateService dashboardUpdateService;
    
    // ==================== Data ====================
    
    private ObservableList<Map<String, Object>> topRestaurantsData;
    private ObservableList<Map<String, Object>> recentOrdersData;
    private ObservableList<Map<String, Object>> userActivityData;
    
    private Timeline updateTimeline;
    private boolean isRealTimeMode = false;
    
    // ==================== Constants ====================
    
    private static final String CSS_STYLE = """
        .analytics-dashboard {
            -fx-background-color: #f8f9fa;
        }
        .header {
            -fx-background-color: #ffffff;
            -fx-border-color: #e9ecef;
            -fx-border-width: 0 0 1 0;
        }
        .sidebar {
            -fx-background-color: #343a40;
            -fx-pref-width: 250;
        }
        .stats-card {
            -fx-background-color: #ffffff;
            -fx-background-radius: 8;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);
        }
        .chart-container {
            -fx-background-color: #ffffff;
            -fx-background-radius: 8;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);
        }
        """;

    // ==================== Initialization ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupUI();
        setupEventHandlers();
        setupCharts();
        setupTables();
        loadInitialData();
        startAutoRefresh();
    }
    
    /**
     * راه‌اندازی سرویس‌های مورد نیاز
     */
    private void initializeServices() {
        analyticsUIService = new AnalyticsUIService();
        chartDataService = new ChartDataService();
        dashboardUpdateService = new DashboardUpdateService();
    }
    
    /**
     * تنظیم رابط کاربری
     */
    private void setupUI() {
        setupHeader();
        setupSidebar();
        setupStatsCards();
        setupCharts();
        setupTables();
        applyStyles();
    }
    
    /**
     * تنظیم هدر داشبورد
     */
    private void setupHeader() {
        titleLabel.setText("داشبورد Analytics");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        updateDateTime();
        
        refreshButton.setText("🔄 به‌روزرسانی");
        exportButton.setText("📊 صادرات");
        settingsButton.setText("⚙️ تنظیمات");
    }
    
    /**
     * تنظیم نوار کناری
     */
    private void setupSidebar() {
        overviewButton.setText("📊 نمای کلی");
        salesButton.setText("💰 فروش");
        usersButton.setText("👥 کاربران");
        restaurantsButton.setText("🍽️ رستوران‌ها");
        performanceButton.setText("⚡ عملکرد");
        realtimeButton.setText("🔄 زنده");
        reportsButton.setText("📋 گزارش‌ها");
        
        // انتخاب دکمه پیش‌فرض
        overviewButton.getStyleClass().add("selected");
    }
    
    /**
     * تنظیم کارت‌های آمار
     */
    private void setupStatsCards() {
        setupStatsCard(totalUsersCard, "👥 کل کاربران", "1,250", "#007bff");
        setupStatsCard(totalRestaurantsCard, "🍽️ کل رستوران‌ها", "85", "#28a745");
        setupStatsCard(totalOrdersCard, "📦 کل سفارشات", "5,420", "#ffc107");
        setupStatsCard(totalRevenueCard, "💰 کل درآمد", "125,000,000 تومان", "#dc3545");
        setupStatsCard(activeUsersCard, "🟢 کاربران فعال", "320", "#17a2b8");
        setupStatsCard(pendingOrdersCard, "⏳ سفارشات در انتظار", "45", "#6f42c1");
    }
    
    /**
     * تنظیم کارت آمار
     */
    private void setupStatsCard(VBox card, String title, String value, String color) {
        card.getStyleClass().add("stats-card");
        card.setPadding(new Insets(20));
        card.setSpacing(10);
        
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", 14));
        titleLabel.setTextFill(Color.web("#6c757d"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));
        
        content.getChildren().addAll(titleLabel, valueLabel);
        card.getChildren().add(content);
    }
    
    /**
     * تنظیم نمودارها
     */
    private void setupCharts() {
        setupSalesTrendChart();
        setupUserDistributionChart();
        setupRestaurantPerformanceChart();
        setupRevenueChart();
    }
    
    /**
     * تنظیم نمودار روند فروش
     */
    private void setupSalesTrendChart() {
        salesTrendChart.setTitle("روند فروش");
        salesTrendChart.setAnimated(true);
        salesTrendChart.setCreateSymbols(true);
        salesTrendChart.setLegendVisible(true);
        
        // تنظیم محور X
        CategoryAxis xAxis = (CategoryAxis) salesTrendChart.getXAxis();
        xAxis.setLabel("زمان");
        xAxis.setAnimated(true);
        
        // تنظیم محور Y
        NumberAxis yAxis = (NumberAxis) salesTrendChart.getYAxis();
        yAxis.setLabel("فروش (میلیون تومان)");
        yAxis.setAnimated(true);
    }
    
    /**
     * تنظیم نمودار توزیع کاربران
     */
    private void setupUserDistributionChart() {
        userDistributionChart.setTitle("توزیع کاربران");
        userDistributionChart.setAnimated(true);
        userDistributionChart.setLegendVisible(true);
        userDistributionChart.setLabelsVisible(true);
    }
    
    /**
     * تنظیم نمودار عملکرد رستوران‌ها
     */
    private void setupRestaurantPerformanceChart() {
        restaurantPerformanceChart.setTitle("عملکرد رستوران‌ها");
        restaurantPerformanceChart.setAnimated(true);
        restaurantPerformanceChart.setLegendVisible(true);
        
        // تنظیم محور X
        CategoryAxis xAxis = (CategoryAxis) restaurantPerformanceChart.getXAxis();
        xAxis.setLabel("رستوران");
        xAxis.setAnimated(true);
        
        // تنظیم محور Y
        NumberAxis yAxis = (NumberAxis) restaurantPerformanceChart.getYAxis();
        yAxis.setLabel("امتیاز");
        yAxis.setAnimated(true);
    }
    
    /**
     * تنظیم نمودار درآمد
     */
    private void setupRevenueChart() {
        revenueChart.setTitle("درآمد");
        revenueChart.setAnimated(true);
        revenueChart.setLegendVisible(true);
        
        // تنظیم محور X
        CategoryAxis xAxis = (CategoryAxis) revenueChart.getXAxis();
        xAxis.setLabel("زمان");
        xAxis.setAnimated(true);
        
        // تنظیم محور Y
        NumberAxis yAxis = (NumberAxis) revenueChart.getYAxis();
        yAxis.setLabel("درآمد (میلیون تومان)");
        yAxis.setAnimated(true);
    }
    
    /**
     * تنظیم جداول
     */
    private void setupTables() {
        setupTopRestaurantsTable();
        setupRecentOrdersTable();
        setupUserActivityTable();
    }
    
    /**
     * تنظیم جدول رستوران‌های برتر
     */
    @SuppressWarnings("unchecked")
    private void setupTopRestaurantsTable() {
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("نام رستوران");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Map<String, Object>, String> ratingCol = new TableColumn<>("امتیاز");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        
        TableColumn<Map<String, Object>, String> ordersCol = new TableColumn<>("تعداد سفارش");
        ordersCol.setCellValueFactory(new PropertyValueFactory<>("orders"));
        
        TableColumn<Map<String, Object>, String> revenueCol = new TableColumn<>("درآمد");
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        
        topRestaurantsTable.getColumns().addAll(nameCol, ratingCol, ordersCol, revenueCol);
        topRestaurantsTable.setPlaceholder(new Label("هیچ رستورانی یافت نشد"));
    }
    
    /**
     * تنظیم جدول سفارشات اخیر
     */
    @SuppressWarnings("unchecked")
    private void setupRecentOrdersTable() {
        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("شناسه");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Map<String, Object>, String> customerCol = new TableColumn<>("مشتری");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customer"));
        
        TableColumn<Map<String, Object>, String> restaurantCol = new TableColumn<>("رستوران");
        restaurantCol.setCellValueFactory(new PropertyValueFactory<>("restaurant"));
        
        TableColumn<Map<String, Object>, String> amountCol = new TableColumn<>("مبلغ");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("وضعیت");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        recentOrdersTable.getColumns().addAll(idCol, customerCol, restaurantCol, amountCol, statusCol);
        recentOrdersTable.setPlaceholder(new Label("هیچ سفارشی یافت نشد"));
    }
    
    /**
     * تنظیم جدول فعالیت کاربران
     */
    @SuppressWarnings("unchecked")
    private void setupUserActivityTable() {
        TableColumn<Map<String, Object>, String> userCol = new TableColumn<>("کاربر");
        userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        
        TableColumn<Map<String, Object>, String> activityCol = new TableColumn<>("فعالیت");
        activityCol.setCellValueFactory(new PropertyValueFactory<>("activity"));
        
        TableColumn<Map<String, Object>, String> timeCol = new TableColumn<>("زمان");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        
        TableColumn<Map<String, Object>, String> durationCol = new TableColumn<>("مدت");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        
        userActivityTable.getColumns().addAll(userCol, activityCol, timeCol, durationCol);
        userActivityTable.setPlaceholder(new Label("هیچ فعالیتی یافت نشد"));
    }
    
    /**
     * تنظیم event handlers
     */
    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> refreshData());
        exportButton.setOnAction(e -> exportData());
        settingsButton.setOnAction(e -> openSettings());
        
        overviewButton.setOnAction(e -> showOverview());
        salesButton.setOnAction(e -> showSales());
        usersButton.setOnAction(e -> showUsers());
        restaurantsButton.setOnAction(e -> showRestaurants());
        performanceButton.setOnAction(e -> showPerformance());
        realtimeButton.setOnAction(e -> toggleRealTime());
        reportsButton.setOnAction(e -> showReports());
    }
    
    /**
     * اعمال استایل‌ها
     */
    private void applyStyles() {
        mainContainer.getStyleClass().add("analytics-dashboard");
        headerContainer.getStyleClass().add("header");
        sidebarContainer.getStyleClass().add("sidebar");
    }
    
    // ==================== Data Loading ====================
    
    /**
     * بارگذاری داده‌های اولیه
     */
    private void loadInitialData() {
        loadStatsData();
        loadChartData();
        loadTableData();
    }
    
    /**
     * بارگذاری داده‌های آمار
     */
    private void loadStatsData() {
        // در حالت واقعی، این داده‌ها از API دریافت می‌شوند
        updateStatsCard(totalUsersCard, "👥 کل کاربران", "1,250", "#007bff");
        updateStatsCard(totalRestaurantsCard, "🍽️ کل رستوران‌ها", "85", "#28a745");
        updateStatsCard(totalOrdersCard, "📦 کل سفارشات", "5,420", "#ffc107");
        updateStatsCard(totalRevenueCard, "💰 کل درآمد", "125,000,000 تومان", "#dc3545");
        updateStatsCard(activeUsersCard, "🟢 کاربران فعال", "320", "#17a2b8");
        updateStatsCard(pendingOrdersCard, "⏳ سفارشات در انتظار", "45", "#6f42c1");
    }
    
    /**
     * بارگذاری داده‌های نمودار
     */
    private void loadChartData() {
        // داده‌های نمودار از سرویس دریافت می‌شوند
        chartDataService.loadSalesTrendData(salesTrendChart);
        chartDataService.loadUserDistributionData(userDistributionChart);
        chartDataService.loadRestaurantPerformanceData(restaurantPerformanceChart);
        chartDataService.loadRevenueData(revenueChart);
    }
    
    /**
     * بارگذاری داده‌های جداول
     */
    private void loadTableData() {
        loadTopRestaurantsData();
        loadRecentOrdersData();
        loadUserActivityData();
    }
    
    /**
     * بارگذاری داده‌های رستوران‌های برتر
     */
    @SuppressWarnings("unchecked")
    private void loadTopRestaurantsData() {
        topRestaurantsData = FXCollections.observableArrayList();
        
        // داده‌های نمونه
        Map<String, Object> restaurant1 = new HashMap<>();
        restaurant1.put("name", "رستوران شاندیز");
        restaurant1.put("rating", "4.8");
        restaurant1.put("orders", "156");
        restaurant1.put("revenue", "12,500,000 تومان");
        
        Map<String, Object> restaurant2 = new HashMap<>();
        restaurant2.put("name", "رستوران تهران");
        restaurant2.put("rating", "4.6");
        restaurant2.put("orders", "142");
        restaurant2.put("revenue", "11,800,000 تومان");
        
        topRestaurantsData.addAll(restaurant1, restaurant2);
        topRestaurantsTable.setItems(topRestaurantsData);
    }
    
    /**
     * بارگذاری داده‌های سفارشات اخیر
     */
    @SuppressWarnings("unchecked")
    private void loadRecentOrdersData() {
        recentOrdersData = FXCollections.observableArrayList();
        
        // داده‌های نمونه
        Map<String, Object> order1 = new HashMap<>();
        order1.put("id", "#12345");
        order1.put("customer", "احمد محمدی");
        order1.put("restaurant", "رستوران شاندیز");
        order1.put("amount", "85,000 تومان");
        order1.put("status", "تحویل شده");
        
        Map<String, Object> order2 = new HashMap<>();
        order2.put("id", "#12344");
        order2.put("customer", "فاطمه احمدی");
        order2.put("restaurant", "رستوران تهران");
        order2.put("amount", "120,000 تومان");
        order2.put("status", "در حال آماده‌سازی");
        
        recentOrdersData.addAll(order1, order2);
        recentOrdersTable.setItems(recentOrdersData);
    }
    
    /**
     * بارگذاری داده‌های فعالیت کاربران
     */
    @SuppressWarnings("unchecked")
    private void loadUserActivityData() {
        userActivityData = FXCollections.observableArrayList();
        
        // داده‌های نمونه
        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("user", "احمد محمدی");
        activity1.put("activity", "سفارش جدید");
        activity1.put("time", "14:30");
        activity1.put("duration", "5 دقیقه");
        
        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("user", "فاطمه احمدی");
        activity2.put("activity", "بازدید منو");
        activity2.put("time", "14:25");
        activity2.put("duration", "3 دقیقه");
        
        userActivityData.addAll(activity1, activity2);
        userActivityTable.setItems(userActivityData);
    }
    
    // ==================== Event Handlers ====================
    
    /**
     * به‌روزرسانی داده‌ها
     */
    @FXML
    private void refreshData() {
        loadInitialData();
        updateDateTime();
        showAlert("اطلاعات", "داده‌ها با موفقیت به‌روزرسانی شدند", Alert.AlertType.INFORMATION);
    }
    
    /**
     * صادرات داده‌ها
     */
    @FXML
    private void exportData() {
        // پیاده‌سازی صادرات داده‌ها
        showAlert("صادرات", "داده‌ها با موفقیت صادر شدند", Alert.AlertType.INFORMATION);
    }
    
    /**
     * باز کردن تنظیمات
     */
    @FXML
    private void openSettings() {
        // پیاده‌سازی باز کردن تنظیمات
        showAlert("تنظیمات", "پنجره تنظیمات باز شد", Alert.AlertType.INFORMATION);
    }
    
    /**
     * نمایش نمای کلی
     */
    @FXML
    private void showOverview() {
        selectNavigationButton(overviewButton);
        // نمایش محتوای نمای کلی
    }
    
    /**
     * نمایش آمار فروش
     */
    @FXML
    private void showSales() {
        selectNavigationButton(salesButton);
        // نمایش محتوای فروش
    }
    
    /**
     * نمایش آمار کاربران
     */
    @FXML
    private void showUsers() {
        selectNavigationButton(usersButton);
        // نمایش محتوای کاربران
    }
    
    /**
     * نمایش آمار رستوران‌ها
     */
    @FXML
    private void showRestaurants() {
        selectNavigationButton(restaurantsButton);
        // نمایش محتوای رستوران‌ها
    }
    
    /**
     * نمایش آمار عملکرد
     */
    @FXML
    private void showPerformance() {
        selectNavigationButton(performanceButton);
        // نمایش محتوای عملکرد
    }
    
    /**
     * تغییر حالت real-time
     */
    @FXML
    private void toggleRealTime() {
        isRealTimeMode = !isRealTimeMode;
        selectNavigationButton(realtimeButton);
        
        if (isRealTimeMode) {
            startRealTimeUpdates();
            realtimeButton.setText("🔄 زنده (فعال)");
        } else {
            stopRealTimeUpdates();
            realtimeButton.setText("🔄 زنده");
        }
    }
    
    /**
     * نمایش گزارش‌ها
     */
    @FXML
    private void showReports() {
        selectNavigationButton(reportsButton);
        // نمایش محتوای گزارش‌ها
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * انتخاب دکمه ناوبری
     */
    private void selectNavigationButton(Button selectedButton) {
        // حذف انتخاب از همه دکمه‌ها
        overviewButton.getStyleClass().remove("selected");
        salesButton.getStyleClass().remove("selected");
        usersButton.getStyleClass().remove("selected");
        restaurantsButton.getStyleClass().remove("selected");
        performanceButton.getStyleClass().remove("selected");
        realtimeButton.getStyleClass().remove("selected");
        reportsButton.getStyleClass().remove("selected");
        
        // انتخاب دکمه جدید
        selectedButton.getStyleClass().add("selected");
    }
    
    /**
     * به‌روزرسانی تاریخ و زمان
     */
    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        dateTimeLabel.setText(now.format(formatter));
    }
    
    /**
     * به‌روزرسانی کارت آمار
     */
    private void updateStatsCard(VBox card, String title, String value, String color) {
        VBox content = (VBox) card.getChildren().get(0);
        Label titleLabel = (Label) content.getChildren().get(0);
        Label valueLabel = (Label) content.getChildren().get(1);
        
        titleLabel.setText(title);
        valueLabel.setText(value);
        valueLabel.setTextFill(Color.web(color));
    }
    
    /**
     * نمایش پیام
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * شروع به‌روزرسانی خودکار
     */
    private void startAutoRefresh() {
        updateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(30), e -> updateDateTime())
        );
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();
    }
    
    /**
     * شروع به‌روزرسانی real-time
     */
    private void startRealTimeUpdates() {
        dashboardUpdateService.startRealTimeUpdates(this);
    }
    
    /**
     * توقف به‌روزرسانی real-time
     */
    private void stopRealTimeUpdates() {
        dashboardUpdateService.stopRealTimeUpdates();
    }
    
    /**
     * به‌روزرسانی داده‌های real-time
     */
    public void updateRealTimeData() {
        Platform.runLater(() -> {
            loadStatsData();
            loadChartData();
            loadTableData();
            updateDateTime();
        });
    }
    
    /**
     * پاک‌سازی منابع
     */
    public void cleanup() {
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
        if (dashboardUpdateService != null) {
            dashboardUpdateService.stopRealTimeUpdates();
        }
    }
} 