package com.myapp.ui.analytics.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.myapp.ui.analytics.util.DataFormatter;
import com.myapp.ui.analytics.util.DashboardUtil;

/**
 * سرویس مدیریت UI برای Analytics
 * مسئول مدیریت داده‌ها و عملیات مربوط به رابط کاربری
 * 
 * @author Food Ordering System
 * @version 1.0
 * @since 2024
 */
public class AnalyticsUIService {

    private final ExecutorService executorService;
    private final DataFormatter dataFormatter;
    private final DashboardUtil dashboardUtil;
    
    // Cache برای داده‌ها
    private final Map<String, Object> dataCache;
    private final Map<String, Long> cacheTimestamps;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 دقیقه

    public AnalyticsUIService() {
        this.executorService = Executors.newFixedThreadPool(4);
        this.dataFormatter = new DataFormatter();
        this.dashboardUtil = new DashboardUtil();
        this.dataCache = new HashMap<>();
        this.cacheTimestamps = new HashMap<>();
    }

    // ==================== Stats Management ====================

    /**
     * دریافت آمار کلی سیستم
     */
    @SuppressWarnings("unchecked")
    public CompletableFuture<Map<String, Object>> getSystemStats() {
        return CompletableFuture.supplyAsync(() -> {
            String cacheKey = "system_stats";
            
            // بررسی کش
            if (isCacheValid(cacheKey)) {
                return (Map<String, Object>) dataCache.get(cacheKey);
            }
            
            // دریافت داده‌های جدید
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", getTotalUsers());
            stats.put("totalRestaurants", getTotalRestaurants());
            stats.put("totalOrders", getTotalOrders());
            stats.put("totalRevenue", getTotalRevenue());
            stats.put("activeUsers", getActiveUsers());
            stats.put("pendingOrders", getPendingOrders());
            
            // ذخیره در کش
            updateCache(cacheKey, stats);
            
            return stats;
        }, executorService);
    }

    /**
     * به‌روزرسانی کارت‌های آمار
     */
    public void updateStatsCards(Map<String, VBox> statsCards, Map<String, Object> stats) {
        if (statsCards.containsKey("totalUsers")) {
            updateStatsCard(statsCards.get("totalUsers"), "👥 کل کاربران", 
                dataFormatter.formatNumber((Long) stats.get("totalUsers")), "#007bff");
        }
        
        if (statsCards.containsKey("totalRestaurants")) {
            updateStatsCard(statsCards.get("totalRestaurants"), "🍽️ کل رستوران‌ها", 
                dataFormatter.formatNumber((Long) stats.get("totalRestaurants")), "#28a745");
        }
        
        if (statsCards.containsKey("totalOrders")) {
            updateStatsCard(statsCards.get("totalOrders"), "📦 کل سفارشات", 
                dataFormatter.formatNumber((Long) stats.get("totalOrders")), "#ffc107");
        }
        
        if (statsCards.containsKey("totalRevenue")) {
            updateStatsCard(statsCards.get("totalRevenue"), "💰 کل درآمد", 
                dataFormatter.formatCurrency((Double) stats.get("totalRevenue")), "#dc3545");
        }
        
        if (statsCards.containsKey("activeUsers")) {
            updateStatsCard(statsCards.get("activeUsers"), "🟢 کاربران فعال", 
                dataFormatter.formatNumber((Long) stats.get("activeUsers")), "#17a2b8");
        }
        
        if (statsCards.containsKey("pendingOrders")) {
            updateStatsCard(statsCards.get("pendingOrders"), "⏳ سفارشات در انتظار", 
                dataFormatter.formatNumber((Long) stats.get("pendingOrders")), "#6f42c1");
        }
    }

    /**
     * به‌روزرسانی کارت آمار
     */
    private void updateStatsCard(VBox card, String title, String value, String color) {
        if (card.getChildren().isEmpty()) return;
        
        VBox content = (VBox) card.getChildren().get(0);
        if (content.getChildren().size() < 2) return;
        
        Label titleLabel = (Label) content.getChildren().get(0);
        Label valueLabel = (Label) content.getChildren().get(1);
        
        titleLabel.setText(title);
        valueLabel.setText(value);
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    // ==================== Chart Management ====================

    /**
     * به‌روزرسانی نمودار روند فروش
     */
    public void updateSalesTrendChart(LineChart<String, Number> chart, String period) {
        CompletableFuture.supplyAsync(() -> getSalesTrendData(period))
            .thenAcceptAsync(data -> {
                chart.getData().clear();
                
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("فروش " + period);
                
                for (Map.Entry<String, Number> entry : data.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
                
                chart.getData().add(series);
            }, executorService);
    }

    /**
     * به‌روزرسانی نمودار توزیع کاربران
     */
    public void updateUserDistributionChart(PieChart chart) {
        CompletableFuture.supplyAsync(this::getUserDistributionData)
            .thenAcceptAsync(data -> {
                chart.getData().clear();
                
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                for (Map.Entry<String, Number> entry : data.entrySet()) {
                    pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
                }
                
                chart.setData(pieChartData);
            }, executorService);
    }

    /**
     * به‌روزرسانی نمودار عملکرد رستوران‌ها
     */
    public void updateRestaurantPerformanceChart(BarChart<String, Number> chart) {
        CompletableFuture.supplyAsync(this::getRestaurantPerformanceData)
            .thenAcceptAsync(data -> {
                chart.getData().clear();
                
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("امتیاز");
                
                for (Map.Entry<String, Number> entry : data.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
                
                chart.getData().add(series);
            }, executorService);
    }

    /**
     * به‌روزرسانی نمودار درآمد
     */
    public void updateRevenueChart(AreaChart<String, Number> chart, String period) {
        CompletableFuture.supplyAsync(() -> getRevenueData(period))
            .thenAcceptAsync(data -> {
                chart.getData().clear();
                
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("درآمد " + period);
                
                for (Map.Entry<String, Number> entry : data.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
                
                chart.getData().add(series);
            }, executorService);
    }

    // ==================== Table Management ====================

    /**
     * به‌روزرسانی جدول رستوران‌های برتر
     */
    public void updateTopRestaurantsTable(TableView<Map<String, Object>> table, int limit) {
        CompletableFuture.supplyAsync(() -> getTopRestaurantsData(limit))
            .thenAcceptAsync(data -> {
                ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList(data);
                table.setItems(tableData);
            }, executorService);
    }

    /**
     * به‌روزرسانی جدول سفارشات اخیر
     */
    public void updateRecentOrdersTable(TableView<Map<String, Object>> table, int limit) {
        CompletableFuture.supplyAsync(() -> getRecentOrdersData(limit))
            .thenAcceptAsync(data -> {
                ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList(data);
                table.setItems(tableData);
            }, executorService);
    }

    /**
     * به‌روزرسانی جدول فعالیت کاربران
     */
    public void updateUserActivityTable(TableView<Map<String, Object>> table, int limit) {
        CompletableFuture.supplyAsync(() -> getUserActivityData(limit))
            .thenAcceptAsync(data -> {
                ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList(data);
                table.setItems(tableData);
            }, executorService);
    }

    // ==================== Data Retrieval ====================

    /**
     * دریافت تعداد کل کاربران
     */
    private Long getTotalUsers() {
        // در حالت واقعی، این داده از API دریافت می‌شود
        return 1250L;
    }

    /**
     * دریافت تعداد کل رستوران‌ها
     */
    private Long getTotalRestaurants() {
        return 85L;
    }

    /**
     * دریافت تعداد کل سفارشات
     */
    private Long getTotalOrders() {
        return 5420L;
    }

    /**
     * دریافت کل درآمد
     */
    private Double getTotalRevenue() {
        return 125000000.0;
    }

    /**
     * دریافت تعداد کاربران فعال
     */
    private Long getActiveUsers() {
        return 320L;
    }

    /**
     * دریافت تعداد سفارشات در انتظار
     */
    private Long getPendingOrders() {
        return 45L;
    }

    /**
     * دریافت داده‌های روند فروش
     */
    private Map<String, Number> getSalesTrendData(String period) {
        Map<String, Number> data = new LinkedHashMap<>();
        
        if ("daily".equals(period)) {
            data.put("شنبه", 1200);
            data.put("یکشنبه", 1500);
            data.put("دوشنبه", 1800);
            data.put("سه‌شنبه", 1600);
            data.put("چهارشنبه", 2000);
            data.put("پنج‌شنبه", 2200);
            data.put("جمعه", 2500);
        } else if ("weekly".equals(period)) {
            data.put("هفته 1", 8500);
            data.put("هفته 2", 9200);
            data.put("هفته 3", 8800);
            data.put("هفته 4", 9500);
        } else if ("monthly".equals(period)) {
            data.put("فروردین", 25000);
            data.put("اردیبهشت", 28000);
            data.put("خرداد", 32000);
            data.put("تیر", 35000);
            data.put("مرداد", 38000);
            data.put("شهریور", 36000);
        }
        
        return data;
    }

    /**
     * دریافت داده‌های توزیع کاربران
     */
    private Map<String, Number> getUserDistributionData() {
        Map<String, Number> data = new LinkedHashMap<>();
        data.put("کاربران عادی", 65);
        data.put("کاربران VIP", 20);
        data.put("کاربران جدید", 15);
        return data;
    }

    /**
     * دریافت داده‌های عملکرد رستوران‌ها
     */
    private Map<String, Number> getRestaurantPerformanceData() {
        Map<String, Number> data = new LinkedHashMap<>();
        data.put("رستوران A", 4.5);
        data.put("رستوران B", 4.2);
        data.put("رستوران C", 4.8);
        data.put("رستوران D", 4.0);
        data.put("رستوران E", 4.6);
        return data;
    }

    /**
     * دریافت داده‌های درآمد
     */
    private Map<String, Number> getRevenueData(String period) {
        Map<String, Number> data = new LinkedHashMap<>();
        
        if ("monthly".equals(period)) {
            data.put("فروردین", 50000);
            data.put("اردیبهشت", 65000);
            data.put("خرداد", 75000);
            data.put("تیر", 80000);
            data.put("مرداد", 90000);
            data.put("شهریور", 85000);
        }
        
        return data;
    }

    /**
     * دریافت داده‌های رستوران‌های برتر
     */
    private List<Map<String, Object>> getTopRestaurantsData(int limit) {
        List<Map<String, Object>> data = new ArrayList<>();
        
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
        
        Map<String, Object> restaurant3 = new HashMap<>();
        restaurant3.put("name", "رستوران اصفهان");
        restaurant3.put("rating", "4.7");
        restaurant3.put("orders", "138");
        restaurant3.put("revenue", "11,200,000 تومان");
        
        data.add(restaurant1);
        data.add(restaurant2);
        data.add(restaurant3);
        
        return data.subList(0, Math.min(limit, data.size()));
    }

    /**
     * دریافت داده‌های سفارشات اخیر
     */
    private List<Map<String, Object>> getRecentOrdersData(int limit) {
        List<Map<String, Object>> data = new ArrayList<>();
        
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
        
        Map<String, Object> order3 = new HashMap<>();
        order3.put("id", "#12343");
        order3.put("customer", "علی رضایی");
        order3.put("restaurant", "رستوران اصفهان");
        order3.put("amount", "95,000 تومان");
        order3.put("status", "در راه");
        
        data.add(order1);
        data.add(order2);
        data.add(order3);
        
        return data.subList(0, Math.min(limit, data.size()));
    }

    /**
     * دریافت داده‌های فعالیت کاربران
     */
    private List<Map<String, Object>> getUserActivityData(int limit) {
        List<Map<String, Object>> data = new ArrayList<>();
        
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
        
        Map<String, Object> activity3 = new HashMap<>();
        activity3.put("user", "علی رضایی");
        activity3.put("activity", "ثبت نظر");
        activity3.put("time", "14:20");
        activity3.put("duration", "2 دقیقه");
        
        data.add(activity1);
        data.add(activity2);
        data.add(activity3);
        
        return data.subList(0, Math.min(limit, data.size()));
    }

    // ==================== Cache Management ====================

    /**
     * بررسی اعتبار کش
     */
    private boolean isCacheValid(String key) {
        if (!dataCache.containsKey(key) || !cacheTimestamps.containsKey(key)) {
            return false;
        }
        
        long timestamp = cacheTimestamps.get(key);
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - timestamp) < CACHE_DURATION;
    }

    /**
     * به‌روزرسانی کش
     */
    private void updateCache(String key, Object data) {
        dataCache.put(key, data);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }

    /**
     * پاک کردن کش
     */
    public void clearCache() {
        dataCache.clear();
        cacheTimestamps.clear();
    }

    /**
     * پاک کردن کش برای کلید خاص
     */
    public void clearCache(String key) {
        dataCache.remove(key);
        cacheTimestamps.remove(key);
    }

    // ==================== Export Functions ====================

    /**
     * صادرات داده‌ها به CSV
     */
    public String exportToCSV(List<Map<String, Object>> data, String filename) {
        return dataFormatter.exportToCSV(data, filename);
    }

    /**
     * صادرات داده‌ها به JSON
     */
    public String exportToJSON(List<Map<String, Object>> data, String filename) {
        return dataFormatter.exportToJSON(data, filename);
    }

    /**
     * صادرات نمودار به تصویر
     */
    public String exportChartToImage(Chart chart, String filename) {
        return dashboardUtil.exportChartToImage(chart, filename);
    }

    // ==================== Utility Methods ====================

    /**
     * دریافت زمان فعلی
     */
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return now.format(formatter);
    }

    /**
     * بررسی اتصال به سرور
     */
    public boolean isServerConnected() {
        // پیاده‌سازی بررسی اتصال
        return true;
    }

    /**
     * دریافت وضعیت سیستم
     */
    public Map<String, String> getSystemStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("server", isServerConnected() ? "متصل" : "قطع");
        status.put("database", "متصل");
        status.put("cache", "فعال");
        status.put("lastUpdate", getCurrentDateTime());
        return status;
    }

    // ==================== Cleanup ====================

    /**
     * پاکسازی منابع
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        clearCache();
    }
} 