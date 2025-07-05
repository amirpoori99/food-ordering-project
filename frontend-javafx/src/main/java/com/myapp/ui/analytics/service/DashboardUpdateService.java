package com.myapp.ui.analytics.service;

import javafx.application.Platform;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

import com.myapp.ui.analytics.controller.AnalyticsDashboardController;

/**
 * سرویس به‌روزرسانی داشبورد Analytics
 * مسئول به‌روزرسانی‌های real-time و اعلان‌ها
 * 
 * @author Food Ordering System
 * @version 1.0
 * @since 2024
 */
public class DashboardUpdateService {

    private final ScheduledExecutorService scheduler;
    private final AnalyticsUIService analyticsUIService;
    private final ChartDataService chartDataService;
    
    private AnalyticsDashboardController dashboardController;
    private Timeline updateTimeline;
    private ScheduledFuture<?> realTimeUpdateTask;
    
    private final AtomicBoolean isRealTimeActive;
    private final Map<String, Object> lastUpdateData;
    private final List<String> updateHistory;
    
    // تنظیمات به‌روزرسانی
    private static final int UPDATE_INTERVAL_SECONDS = 30;
    private static final int REAL_TIME_INTERVAL_SECONDS = 5;
    private static final int MAX_HISTORY_SIZE = 100;

    public DashboardUpdateService() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.analyticsUIService = new AnalyticsUIService();
        this.chartDataService = new ChartDataService();
        this.isRealTimeActive = new AtomicBoolean(false);
        this.lastUpdateData = new ConcurrentHashMap<>();
        this.updateHistory = new CopyOnWriteArrayList<>();
    }

    // ==================== Real-time Updates ====================

    /**
     * شروع به‌روزرسانی‌های real-time
     */
    public void startRealTimeUpdates(AnalyticsDashboardController controller) {
        this.dashboardController = controller;
        
        if (isRealTimeActive.compareAndSet(false, true)) {
            scheduleRealTimeUpdates();
            showRealTimeNotification("Real-time updates activated", "Dashboard will update every " + REAL_TIME_INTERVAL_SECONDS + " seconds");
        }
    }

    /**
     * توقف به‌روزرسانی‌های real-time
     */
    public void stopRealTimeUpdates() {
        if (isRealTimeActive.compareAndSet(true, false)) {
            if (realTimeUpdateTask != null && !realTimeUpdateTask.isCancelled()) {
                realTimeUpdateTask.cancel(false);
            }
            showRealTimeNotification("Real-time updates deactivated", "Dashboard updates stopped");
        }
    }

    /**
     * برنامه‌ریزی به‌روزرسانی‌های real-time
     */
    private void scheduleRealTimeUpdates() {
        realTimeUpdateTask = scheduler.scheduleAtFixedRate(
            this::performRealTimeUpdate,
            REAL_TIME_INTERVAL_SECONDS,
            REAL_TIME_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );
    }

    /**
     * انجام به‌روزرسانی real-time
     */
    private void performRealTimeUpdate() {
        try {
            // به‌روزرسانی کنترلر
            if (dashboardController != null) {
                Platform.runLater(() -> {
                    dashboardController.updateRealTimeData();
                    updateLastUpdateTime();
                });
            }
            
            // بررسی تغییرات مهم
            checkForSignificantChanges();
            
            // ثبت تاریخچه
            addToUpdateHistory("Real-time update completed at " + getCurrentTime());
            
        } catch (Exception e) {
            addToUpdateHistory("Error during real-time update: " + e.getMessage());
            showErrorNotification("Update Error", "Failed to update dashboard: " + e.getMessage());
        }
    }

    // ==================== Periodic Updates ====================

    /**
     * شروع به‌روزرسانی‌های دوره‌ای
     */
    public void startPeriodicUpdates(AnalyticsDashboardController controller) {
        this.dashboardController = controller;
        
        updateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(UPDATE_INTERVAL_SECONDS), e -> {
                performPeriodicUpdate();
            })
        );
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();
        
        addToUpdateHistory("Periodic updates started");
    }

    /**
     * توقف به‌روزرسانی‌های دوره‌ای
     */
    public void stopPeriodicUpdates() {
        if (updateTimeline != null) {
            updateTimeline.stop();
            addToUpdateHistory("Periodic updates stopped");
        }
    }

    /**
     * انجام به‌روزرسانی دوره‌ای
     */
    private void performPeriodicUpdate() {
        try {
            // به‌روزرسانی داده‌های آمار
            updateStatsData();
            
            // به‌روزرسانی نمودارها
            updateChartsData();
            
            // به‌روزرسانی جداول
            updateTablesData();
            
            addToUpdateHistory("Periodic update completed at " + getCurrentTime());
            
        } catch (Exception e) {
            addToUpdateHistory("Error during periodic update: " + e.getMessage());
            showErrorNotification("Update Error", "Failed to perform periodic update: " + e.getMessage());
        }
    }

    // ==================== Data Updates ====================

    /**
     * به‌روزرسانی داده‌های آمار
     */
    private void updateStatsData() {
        analyticsUIService.getSystemStats()
            .thenAcceptAsync(stats -> {
                Platform.runLater(() -> {
                    // مقایسه با داده‌های قبلی
                    checkForStatsChanges(stats);
                    
                    // به‌روزرسانی کنترلر
                    if (dashboardController != null) {
                        // اینجا می‌توانید متدهای به‌روزرسانی کنترلر را فراخوانی کنید
                    }
                });
            });
    }

    /**
     * به‌روزرسانی داده‌های نمودار
     */
    private void updateChartsData() {
        // به‌روزرسانی نمودارهای مختلف
        Platform.runLater(() -> {
            // اینجا می‌توانید نمودارها را به‌روزرسانی کنید
        });
    }

    /**
     * به‌روزرسانی داده‌های جداول
     */
    private void updateTablesData() {
        // به‌روزرسانی جداول
        Platform.runLater(() -> {
            // اینجا می‌توانید جداول را به‌روزرسانی کنید
        });
    }

    // ==================== Change Detection ====================

    /**
     * بررسی تغییرات مهم
     */
    private void checkForSignificantChanges() {
        // بررسی تغییرات در سفارشات جدید
        checkNewOrders();
        
        // بررسی تغییرات در کاربران فعال
        checkActiveUsers();
        
        // بررسی تغییرات در درآمد
        checkRevenueChanges();
        
        // بررسی خطاهای سیستم
        checkSystemErrors();
    }

    /**
     * بررسی سفارشات جدید
     */
    private void checkNewOrders() {
        // پیاده‌سازی بررسی سفارشات جدید
        int newOrders = getNewOrdersCount();
        if (newOrders > 0) {
            showNotification("سفارشات جدید", newOrders + " سفارش جدید دریافت شد", AlertType.INFORMATION);
        }
    }

    /**
     * بررسی کاربران فعال
     */
    private void checkActiveUsers() {
        // پیاده‌سازی بررسی کاربران فعال
        int activeUsers = getActiveUsersCount();
        int previousActiveUsers = getPreviousActiveUsersCount();
        
        if (Math.abs(activeUsers - previousActiveUsers) > 10) {
            showNotification("تغییر کاربران فعال", 
                "تعداد کاربران فعال از " + previousActiveUsers + " به " + activeUsers + " تغییر کرد", 
                AlertType.INFORMATION);
        }
    }

    /**
     * بررسی تغییرات درآمد
     */
    private void checkRevenueChanges() {
        // پیاده‌سازی بررسی تغییرات درآمد
        double currentRevenue = getCurrentRevenue();
        double previousRevenue = getPreviousRevenue();
        
        double changePercent = ((currentRevenue - previousRevenue) / previousRevenue) * 100;
        
        if (Math.abs(changePercent) > 5) {
            String message = changePercent > 0 ? "افزایش" : "کاهش";
            showNotification("تغییر درآمد", 
                "درآمد " + message + " " + Math.abs(changePercent) + "% نسبت به دوره قبل", 
                AlertType.INFORMATION);
        }
    }

    /**
     * بررسی خطاهای سیستم
     */
    private void checkSystemErrors() {
        // پیاده‌سازی بررسی خطاهای سیستم
        int errorCount = getSystemErrorCount();
        if (errorCount > 5) {
            showErrorNotification("خطای سیستم", errorCount + " خطا در سیستم شناسایی شد");
        }
    }

    /**
     * بررسی تغییرات آمار
     */
    private void checkForStatsChanges(Map<String, Object> newStats) {
        for (Map.Entry<String, Object> entry : newStats.entrySet()) {
            String key = entry.getKey();
            Object newValue = entry.getValue();
            Object oldValue = lastUpdateData.get(key);
            
            if (oldValue != null && !oldValue.equals(newValue)) {
                showNotification("تغییر آمار", 
                    key + " از " + oldValue + " به " + newValue + " تغییر کرد", 
                    AlertType.INFORMATION);
            }
            
            lastUpdateData.put(key, newValue);
        }
    }

    // ==================== Notifications ====================

    /**
     * نمایش اعلان real-time
     */
    private void showRealTimeNotification(String title, String message) {
        Platform.runLater(() -> {
            showNotification(title, message, AlertType.INFORMATION);
        });
    }

    /**
     * نمایش اعلان خطا
     */
    private void showErrorNotification(String title, String message) {
        Platform.runLater(() -> {
            showNotification(title, message, AlertType.ERROR);
        });
    }

    /**
     * نمایش اعلان
     */
    private void showNotification(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setResizable(false);
        
        // تنظیم زمان نمایش خودکار
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (alert.isShowing()) {
                        alert.close();
                    }
                });
            }
        }, 5000); // 5 ثانیه
        
        alert.show();
    }

    /**
     * نمایش اعلان پیشرفته
     */
    public void showAdvancedNotification(String title, String message, String details, AlertType type) {
        Platform.runLater(() -> {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.setHeaderText(null);
            
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getButtonTypes().add(ButtonType.OK);
            
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            
            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            Label detailsLabel = new Label(details);
            detailsLabel.setWrapText(true);
            
            Label timeLabel = new Label("زمان: " + getCurrentTime());
            timeLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
            
            content.getChildren().addAll(messageLabel, detailsLabel, timeLabel);
            dialogPane.setContent(content);
            
            dialog.showAndWait();
        });
    }

    // ==================== Utility Methods ====================

    /**
     * به‌روزرسانی زمان آخرین به‌روزرسانی
     */
    private void updateLastUpdateTime() {
        if (dashboardController != null) {
            // به‌روزرسانی برچسب زمان در کنترلر
        }
    }

    /**
     * دریافت زمان فعلی
     */
    private String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }

    /**
     * اضافه کردن به تاریخچه
     */
    private void addToUpdateHistory(String entry) {
        updateHistory.add(entry);
        
        // محدود کردن اندازه تاریخچه
        if (updateHistory.size() > MAX_HISTORY_SIZE) {
            updateHistory.remove(0);
        }
    }

    /**
     * دریافت تاریخچه به‌روزرسانی
     */
    public List<String> getUpdateHistory() {
        return new ArrayList<>(updateHistory);
    }

    /**
     * پاک کردن تاریخچه
     */
    public void clearUpdateHistory() {
        updateHistory.clear();
    }

    // ==================== Mock Data Methods ====================

    /**
     * دریافت تعداد سفارشات جدید (Mock)
     */
    private int getNewOrdersCount() {
        return new Random().nextInt(5);
    }

    /**
     * دریافت تعداد کاربران فعال (Mock)
     */
    private int getActiveUsersCount() {
        return 300 + new Random().nextInt(50);
    }

    /**
     * دریافت تعداد کاربران فعال قبلی (Mock)
     */
    private int getPreviousActiveUsersCount() {
        return 320;
    }

    /**
     * دریافت درآمد فعلی (Mock)
     */
    private double getCurrentRevenue() {
        return 125000000 + new Random().nextInt(1000000);
    }

    /**
     * دریافت درآمد قبلی (Mock)
     */
    private double getPreviousRevenue() {
        return 125000000;
    }

    /**
     * دریافت تعداد خطاهای سیستم (Mock)
     */
    private int getSystemErrorCount() {
        return new Random().nextInt(10);
    }

    // ==================== Status Methods ====================

    /**
     * بررسی وضعیت real-time
     */
    public boolean isRealTimeActive() {
        return isRealTimeActive.get();
    }

    /**
     * دریافت وضعیت سرویس
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("realTimeActive", isRealTimeActive.get());
        status.put("lastUpdate", getCurrentTime());
        status.put("updateHistorySize", updateHistory.size());
        status.put("schedulerActive", !scheduler.isShutdown());
        return status;
    }

    /**
     * دریافت آمار به‌روزرسانی
     */
    public Map<String, Object> getUpdateStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUpdates", updateHistory.size());
        stats.put("realTimeUpdates", updateHistory.stream()
            .filter(entry -> entry.contains("Real-time update"))
            .count());
        stats.put("periodicUpdates", updateHistory.stream()
            .filter(entry -> entry.contains("Periodic update"))
            .count());
        stats.put("errors", updateHistory.stream()
            .filter(entry -> entry.contains("Error"))
            .count());
        return stats;
    }

    // ==================== Cleanup ====================

    /**
     * پاکسازی منابع
     */
    public void shutdown() {
        stopRealTimeUpdates();
        stopPeriodicUpdates();
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        clearUpdateHistory();
        lastUpdateData.clear();
    }
} 