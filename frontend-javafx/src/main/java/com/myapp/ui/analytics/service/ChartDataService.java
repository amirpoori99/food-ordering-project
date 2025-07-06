package com.myapp.ui.analytics.service;

import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.myapp.ui.analytics.util.ChartUtil;

/**
 * سرویس مدیریت داده‌های نمودار برای Analytics
 * مسئول بارگذاری و مدیریت داده‌های نمودارها
 * 
 * @author Food Ordering System
 * @version 1.0
 * @since 2024
 */
public class ChartDataService {

    private final ExecutorService executorService;
    private final ChartUtil chartUtil;
    
    // Cache برای داده‌های نمودار
    private final Map<String, Object> chartDataCache;
    private final Map<String, Long> cacheTimestamps;
    private static final long CACHE_DURATION = 10 * 60 * 1000; // 10 دقیقه

    public ChartDataService() {
        this.executorService = Executors.newFixedThreadPool(2);
        this.chartUtil = new ChartUtil();
        this.chartDataCache = new HashMap<>();
        this.cacheTimestamps = new HashMap<>();
    }

    // ==================== Sales Trend Chart ====================

    /**
     * بارگذاری داده‌های نمودار روند فروش
     */
    public void loadSalesTrendData(LineChart<String, Number> chart) {
        if (chart == null) return;
        
        CompletableFuture.supplyAsync(() -> generateSalesTrendData())
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    chart.getData().clear();
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("فروش روزانه");
                    
                    for (Map.Entry<String, Number> entry : data.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    
                    chart.getData().add(series);
                    chartUtil.applyChartStyle(chart, "sales-trend");
                });
            }, executorService);
    }

    /**
     * بارگذاری داده‌های نمودار روند فروش برای بازه زمانی خاص
     */
    public void loadSalesTrendData(LineChart<String, Number> chart, String period) {
        if (chart == null) return;
        
        CompletableFuture.supplyAsync(() -> generateSalesTrendData(period))
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    chart.getData().clear();
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("فروش " + getPeriodDisplayName(period));
                    
                    for (Map.Entry<String, Number> entry : data.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    
                    chart.getData().add(series);
                    chartUtil.applyChartStyle(chart, "sales-trend");
                });
            }, executorService);
    }

    /**
     * تولید داده‌های روند فروش
     */
    private Map<String, Number> generateSalesTrendData() {
        return generateSalesTrendData("daily");
    }

    /**
     * تولید داده‌های روند فروش برای بازه زمانی خاص
     */
    private Map<String, Number> generateSalesTrendData(String period) {
        Map<String, Number> data = new LinkedHashMap<>();
        
        switch (period) {
            case "daily":
                data.put("شنبه", 1200);
                data.put("یکشنبه", 1500);
                data.put("دوشنبه", 1800);
                data.put("سه‌شنبه", 1600);
                data.put("چهارشنبه", 2000);
                data.put("پنج‌شنبه", 2200);
                data.put("جمعه", 2500);
                break;
                
            case "weekly":
                data.put("هفته 1", 8500);
                data.put("هفته 2", 9200);
                data.put("هفته 3", 8800);
                data.put("هفته 4", 9500);
                break;
                
            case "monthly":
                data.put("فروردین", 25000);
                data.put("اردیبهشت", 28000);
                data.put("خرداد", 32000);
                data.put("تیر", 35000);
                data.put("مرداد", 38000);
                data.put("شهریور", 36000);
                break;
                
            default:
                data.put("شنبه", 1200);
                data.put("یکشنبه", 1500);
                data.put("دوشنبه", 1800);
                data.put("سه‌شنبه", 1600);
                data.put("چهارشنبه", 2000);
                data.put("پنج‌شنبه", 2200);
                data.put("جمعه", 2500);
        }
        
        return data;
    }

    // ==================== User Distribution Chart ====================

    /**
     * بارگذاری داده‌های نمودار توزیع کاربران
     */
    public void loadUserDistributionData(PieChart chart) {
        if (chart == null) return;
        
        CompletableFuture.supplyAsync(() -> generateUserDistributionData())
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    chart.getData().clear();
                    
                    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                    for (Map.Entry<String, Number> entry : data.entrySet()) {
                        pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
                    }
                    
                    chart.setData(pieChartData);
                    chartUtil.applyChartStyle(chart, "user-distribution");
                });
            }, executorService);
    }

    /**
     * تولید داده‌های توزیع کاربران
     */
    private Map<String, Number> generateUserDistributionData() {
        Map<String, Number> data = new LinkedHashMap<>();
        data.put("کاربران عادی", 65);
        data.put("کاربران VIP", 20);
        data.put("کاربران جدید", 15);
        return data;
    }

    // ==================== Restaurant Performance Chart ====================

    /**
     * بارگذاری داده‌های نمودار عملکرد رستوران‌ها
     */
    public void loadRestaurantPerformanceData(BarChart<String, Number> chart) {
        if (chart == null) return;
        
        CompletableFuture.supplyAsync(() -> generateRestaurantPerformanceData())
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    chart.getData().clear();
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("امتیاز");
                    
                    for (Map.Entry<String, Number> entry : data.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    
                    chart.getData().add(series);
                    chartUtil.applyChartStyle(chart, "restaurant-performance");
                });
            }, executorService);
    }

    /**
     * بارگذاری داده‌های نمودار عملکرد رستوران‌ها با محدودیت
     */
    public void loadRestaurantPerformanceData(BarChart<String, Number> chart, int limit) {
        if (chart == null) return;
        
        CompletableFuture.supplyAsync(() -> generateRestaurantPerformanceData(limit))
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    chart.getData().clear();
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("امتیاز");
                    
                    for (Map.Entry<String, Number> entry : data.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    
                    chart.getData().add(series);
                    chartUtil.applyChartStyle(chart, "restaurant-performance");
                });
            }, executorService);
    }

    /**
     * تولید داده‌های عملکرد رستوران‌ها
     */
    private Map<String, Number> generateRestaurantPerformanceData() {
        return generateRestaurantPerformanceData(10);
    }

    /**
     * تولید داده‌های عملکرد رستوران‌ها با محدودیت
     */
    private Map<String, Number> generateRestaurantPerformanceData(int limit) {
        Map<String, Number> data = new LinkedHashMap<>();
        
        data.put("رستوران شاندیز", 4.8);
        data.put("رستوران تهران", 4.6);
        data.put("رستوران اصفهان", 4.7);
        data.put("رستوران شیراز", 4.5);
        data.put("رستوران مشهد", 4.9);
        data.put("رستوران تبریز", 4.4);
        data.put("رستوران کرج", 4.3);
        data.put("رستوران قم", 4.2);
        data.put("رستوران یزد", 4.6);
        data.put("رستوران کرمانشاه", 4.1);
        
        // محدود کردن تعداد نتایج
        return data.entrySet().stream()
            .limit(limit)
            .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
    }

    // ==================== Revenue Chart ====================

    /**
     * بارگذاری داده‌های نمودار درآمد
     */
    public void loadRevenueData(AreaChart<String, Number> chart) {
        loadRevenueData(chart, "monthly");
    }

    /**
     * بارگذاری داده‌های نمودار درآمد برای بازه زمانی خاص
     */
    public void loadRevenueData(AreaChart<String, Number> chart, String period) {
        if (chart == null) return;
        
        CompletableFuture.supplyAsync(() -> generateRevenueData(period))
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    chart.getData().clear();
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("درآمد " + getPeriodDisplayName(period));
                    
                    for (Map.Entry<String, Number> entry : data.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    
                    chart.getData().add(series);
                    chartUtil.applyChartStyle(chart, "revenue");
                });
            }, executorService);
    }

    /**
     * تولید داده‌های درآمد
     */
    private Map<String, Number> generateRevenueData(String period) {
        Map<String, Number> data = new LinkedHashMap<>();
        
        switch (period) {
            case "daily":
                data.put("شنبه", 50000);
                data.put("یکشنبه", 65000);
                data.put("دوشنبه", 75000);
                data.put("سه‌شنبه", 70000);
                data.put("چهارشنبه", 85000);
                data.put("پنج‌شنبه", 95000);
                data.put("جمعه", 110000);
                break;
                
            case "weekly":
                data.put("هفته 1", 350000);
                data.put("هفته 2", 380000);
                data.put("هفته 3", 360000);
                data.put("هفته 4", 400000);
                break;
                
            case "monthly":
                data.put("فروردین", 50000);
                data.put("اردیبهشت", 65000);
                data.put("خرداد", 75000);
                data.put("تیر", 80000);
                data.put("مرداد", 90000);
                data.put("شهریور", 85000);
                break;
                
            default:
                data.put("فروردین", 50000);
                data.put("اردیبهشت", 65000);
                data.put("خرداد", 75000);
                data.put("تیر", 80000);
                data.put("مرداد", 90000);
                data.put("شهریور", 85000);
        }
        
        return data;
    }

    // ==================== Order Status Chart ====================

    /**
     * بارگذاری داده‌های نمودار وضعیت سفارشات
     */
    public void loadOrderStatusData(PieChart chart) {
        if (chart == null) return;
        
        CompletableFuture.supplyAsync(() -> generateOrderStatusData())
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    chart.getData().clear();
                    
                    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                    for (Map.Entry<String, Number> entry : data.entrySet()) {
                        pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
                    }
                    
                    chart.setData(pieChartData);
                    chartUtil.applyChartStyle(chart, "order-status");
                });
            }, executorService);
    }

    /**
     * تولید داده‌های وضعیت سفارشات
     */
    private Map<String, Number> generateOrderStatusData() {
        Map<String, Number> data = new LinkedHashMap<>();
        data.put("تحویل شده", 65);
        data.put("در راه", 20);
        data.put("در حال آماده‌سازی", 10);
        data.put("لغو شده", 5);
        return data;
    }

    // ==================== User Activity Chart ====================

    /**
     * بارگذاری داده‌های نمودار فعالیت کاربران
     */
    public void loadUserActivityData(LineChart<String, Number> chart) {
        if (chart == null) return;
        
        CompletableFuture.supplyAsync(() -> generateUserActivityData())
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    chart.getData().clear();
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("کاربران فعال");
                    
                    for (Map.Entry<String, Number> entry : data.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    
                    chart.getData().add(series);
                    chartUtil.applyChartStyle(chart, "user-activity");
                });
            }, executorService);
    }

    /**
     * تولید داده‌های فعالیت کاربران
     */
    private Map<String, Number> generateUserActivityData() {
        Map<String, Number> data = new LinkedHashMap<>();
        data.put("00:00", 50);
        data.put("04:00", 30);
        data.put("08:00", 120);
        data.put("12:00", 280);
        data.put("16:00", 320);
        data.put("20:00", 250);
        data.put("24:00", 80);
        return data;
    }

    // ==================== Geographic Distribution Chart ====================

    /**
     * بارگذاری داده‌های نمودار توزیع جغرافیایی
     */
    public void loadGeographicDistributionData(BarChart<String, Number> chart) {
        if (chart == null) return;
        
        CompletableFuture.supplyAsync(() -> generateGeographicDistributionData())
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    chart.getData().clear();
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("تعداد سفارش");
                    
                    for (Map.Entry<String, Number> entry : data.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    
                    chart.getData().add(series);
                    chartUtil.applyChartStyle(chart, "geographic-distribution");
                });
            }, executorService);
    }

    /**
     * تولید داده‌های توزیع جغرافیایی
     */
    private Map<String, Number> generateGeographicDistributionData() {
        Map<String, Number> data = new LinkedHashMap<>();
        data.put("تهران", 1200);
        data.put("اصفهان", 850);
        data.put("شیراز", 720);
        data.put("مشهد", 680);
        data.put("تبریز", 550);
        data.put("کرج", 480);
        data.put("قم", 320);
        data.put("یزد", 280);
        return data;
    }

    // ==================== Performance Metrics Chart ====================

    /**
     * بارگذاری داده‌های نمودار معیارهای عملکرد
     */
    public void loadPerformanceMetricsData(LineChart<String, Number> chart) {
        if (chart == null) return;
        
        CompletableFuture.supplyAsync(() -> generatePerformanceMetricsData())
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    chart.getData().clear();
                    
                    // سری زمان پاسخ
                    XYChart.Series<String, Number> responseTimeSeries = new XYChart.Series<>();
                    responseTimeSeries.setName("زمان پاسخ (ms)");
                    
                    // سری نرخ خطا
                    XYChart.Series<String, Number> errorRateSeries = new XYChart.Series<>();
                    errorRateSeries.setName("نرخ خطا (%)");
                    
                    for (Map.Entry<String, Map<String, Number>> entry : data.entrySet()) {
                        String time = entry.getKey();
                        Map<String, Number> metrics = entry.getValue();
                        
                        responseTimeSeries.getData().add(new XYChart.Data<>(time, metrics.get("responseTime")));
                        errorRateSeries.getData().add(new XYChart.Data<>(time, metrics.get("errorRate")));
                    }
                    
                    chart.getData().add(responseTimeSeries);
                    chart.getData().add(errorRateSeries);
                    chartUtil.applyChartStyle(chart, "performance-metrics");
                });
            }, executorService);
    }

    /**
     * تولید داده‌های معیارهای عملکرد
     */
    private Map<String, Map<String, Number>> generatePerformanceMetricsData() {
        Map<String, Map<String, Number>> data = new LinkedHashMap<>();
        
        String[] times = {"00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "24:00"};
        int[] responseTimes = {120, 150, 180, 200, 220, 190, 140};
        double[] errorRates = {0.5, 0.8, 1.2, 1.5, 1.8, 1.3, 0.7};
        
        for (int i = 0; i < times.length; i++) {
            Map<String, Number> metrics = new HashMap<>();
            metrics.put("responseTime", responseTimes[i]);
            metrics.put("errorRate", errorRates[i]);
            data.put(times[i], metrics);
        }
        
        return data;
    }

    // ==================== Utility Methods ====================

    /**
     * دریافت نام نمایشی بازه زمانی
     */
    private String getPeriodDisplayName(String period) {
        switch (period) {
            case "daily": return "روزانه";
            case "weekly": return "هفتگی";
            case "monthly": return "ماهانه";
            case "yearly": return "سالانه";
            default: return "روزانه";
        }
    }

    /**
     * بررسی اعتبار کش
     */
    private boolean isCacheValid(String key) {
        if (!chartDataCache.containsKey(key) || !cacheTimestamps.containsKey(key)) {
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
        chartDataCache.put(key, data);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }

    /**
     * پاک کردن کش
     */
    public void clearCache() {
        chartDataCache.clear();
        cacheTimestamps.clear();
    }

    /**
     * پاک کردن کش برای کلید خاص
     */
    public void clearCache(String key) {
        chartDataCache.remove(key);
        cacheTimestamps.remove(key);
    }

    /**
     * دریافت داده‌های واقعی از API
     */
    public CompletableFuture<Map<String, Number>> getRealDataFromAPI(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            // در حالت واقعی، اینجا درخواست HTTP به API ارسال می‌شود
            // فعلاً داده‌های نمونه برمی‌گردانیم
            Map<String, Number> data = new HashMap<>();
            data.put("sample", 100);
            return data;
        }, executorService);
    }

    /**
     * به‌روزرسانی نمودار با داده‌های جدید
     */
    public void updateChartWithRealData(Chart chart, String chartType, String dataSource) {
        getRealDataFromAPI(dataSource)
            .thenAcceptAsync(data -> {
                Platform.runLater(() -> {
                    updateChartData(chart, chartType, data);
                });
            }, executorService);
    }

    /**
     * به‌روزرسانی داده‌های نمودار
     */
    @SuppressWarnings("unchecked")
    private void updateChartData(Chart chart, String chartType, Map<String, Number> data) {
        switch (chartType) {
            case "line":
                updateLineChart((LineChart<String, Number>) chart, data);
                break;
            case "bar":
                updateBarChart((BarChart<String, Number>) chart, data);
                break;
            case "pie":
                updatePieChart((PieChart) chart, data);
                break;
            case "area":
                updateAreaChart((AreaChart<String, Number>) chart, data);
                break;
        }
    }

    /**
     * به‌روزرسانی نمودار خطی
     */
    private void updateLineChart(LineChart<String, Number> chart, Map<String, Number> data) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("داده‌های جدید");
        
        for (Map.Entry<String, Number> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        chart.getData().add(series);
    }

    /**
     * به‌روزرسانی نمودار میله‌ای
     */
    private void updateBarChart(BarChart<String, Number> chart, Map<String, Number> data) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("داده‌های جدید");
        
        for (Map.Entry<String, Number> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        chart.getData().add(series);
    }

    /**
     * به‌روزرسانی نمودار دایره‌ای
     */
    private void updatePieChart(PieChart chart, Map<String, Number> data) {
        chart.getData().clear();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Number> entry : data.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
        }
        
        chart.setData(pieChartData);
    }

    /**
     * به‌روزرسانی نمودار ناحیه‌ای
     */
    private void updateAreaChart(AreaChart<String, Number> chart, Map<String, Number> data) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("داده‌های جدید");
        
        for (Map.Entry<String, Number> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        chart.getData().add(series);
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