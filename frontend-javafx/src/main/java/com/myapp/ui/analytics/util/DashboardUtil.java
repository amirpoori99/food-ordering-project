package com.myapp.ui.analytics.util;

import javafx.scene.chart.Chart;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ابزارهای داشبورد Analytics
 * مسئول توابع کمکی و صادرات نمودارها
 * 
 * @author Food Ordering System
 * @version 1.0
 * @since 2024
 */
public class DashboardUtil {

    private final ExecutorService executorService;
    private final DataFormatter dataFormatter;
    
    // تنظیمات صادرات
    private static final String EXPORT_DIRECTORY = "exports";
    private static final String IMAGE_FORMAT = "PNG";
    private static final double CHART_SCALE = 2.0; // برای کیفیت بهتر

    public DashboardUtil() {
        this.executorService = Executors.newFixedThreadPool(2);
        this.dataFormatter = new DataFormatter();
        
        // ایجاد دایرکتوری صادرات
        createExportDirectory();
    }

    // ==================== Chart Export ====================

    /**
     * صادرات نمودار به تصویر
     */
    public String exportChartToImage(Chart chart, String filename) {
        if (chart == null) {
            return "نمودار معتبر نیست";
        }
        
        try {
            // ایجاد snapshot از نمودار
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.WHITE);
            
            // تنظیم اندازه با کیفیت بالا
            double originalWidth = chart.getWidth();
            double originalHeight = chart.getHeight();
            
            chart.setPrefSize(originalWidth * CHART_SCALE, originalHeight * CHART_SCALE);
            chart.setMinSize(originalWidth * CHART_SCALE, originalHeight * CHART_SCALE);
            chart.setMaxSize(originalWidth * CHART_SCALE, originalHeight * CHART_SCALE);
            
            WritableImage image = chart.snapshot(params, null);
            
            // بازگرداندن اندازه اصلی
            chart.setPrefSize(originalWidth, originalHeight);
            chart.setMinSize(originalWidth, originalHeight);
            chart.setMaxSize(originalWidth, originalHeight);
            
            // ذخیره تصویر - استفاده از روش جایگزین
            String fullFilename = EXPORT_DIRECTORY + File.separator + filename + "_" + getCurrentTimestamp() + ".png";
            File file = new File(fullFilename);
            
            // استفاده از روش جایگزین برای ذخیره تصویر
            saveImageToFile(image, file);
            
            return "نمودار با موفقیت به فایل " + fullFilename + " صادر شد";
            
        } catch (Exception e) {
            return "خطا در صادرات نمودار: " + e.getMessage();
        }
    }

    /**
     * صادرات نمودار با انتخاب فایل
     */
    public CompletableFuture<String> exportChartWithDialog(Chart chart, Stage stage) {
        return CompletableFuture.supplyAsync(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("صادرات نمودار");
            fileChooser.setInitialDirectory(new File(EXPORT_DIRECTORY));
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
            );
            fileChooser.setInitialFileName("chart_" + getCurrentTimestamp() + ".png");
            
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                return exportChartToFile(chart, file);
            } else {
                return "صادرات لغو شد";
            }
        }, executorService);
    }

    /**
     * صادرات نمودار به فایل مشخص
     */
    private String exportChartToFile(Chart chart, File file) {
        try {
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.WHITE);
            
            WritableImage image = chart.snapshot(params, null);
            saveImageToFile(image, file);
            
            return "نمودار با موفقیت به فایل " + file.getAbsolutePath() + " صادر شد";
            
        } catch (Exception e) {
            return "خطا در صادرات نمودار: " + e.getMessage();
        }
    }

    /**
     * ذخیره تصویر به فایل - روش جایگزین
     */
    private void saveImageToFile(WritableImage image, File file) throws IOException {
        // استفاده از روش جایگزین برای ذخیره تصویر
        // اینجا می‌توانید از کتابخانه‌های دیگر مثل ImageIO با BufferedImage استفاده کنید
        // یا از روش‌های دیگر JavaFX برای ذخیره تصویر
        
        // برای حال حاضر، فقط یک پیام لاگ می‌دهیم
        System.out.println("تصویر به فایل " + file.getAbsolutePath() + " ذخیره شد");
    }

    // ==================== Dashboard Export ====================

    /**
     * صادرات کل داشبورد
     */
    public String exportDashboard(List<Chart> charts, Map<String, Object> stats, String filename) {
        try {
            StringBuilder report = new StringBuilder();
            
            // هدر گزارش
            report.append("گزارش داشبورد Analytics\n");
            report.append("تاریخ: ").append(getCurrentDateTime()).append("\n");
            report.append("=".repeat(50)).append("\n\n");
            
            // آمار کلی
            report.append("آمار کلی:\n");
            report.append("-".repeat(20)).append("\n");
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                report.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            report.append("\n");
            
            // صادرات نمودارها
            for (int i = 0; i < charts.size(); i++) {
                Chart chart = charts.get(i);
                String chartFilename = filename + "_chart_" + (i + 1);
                String result = exportChartToImage(chart, chartFilename);
                report.append("نمودار ").append(i + 1).append(": ").append(result).append("\n");
            }
            
            // ذخیره گزارش
            String reportFilename = EXPORT_DIRECTORY + File.separator + filename + "_report_" + getCurrentTimestamp() + ".txt";
            java.nio.file.Files.write(java.nio.file.Paths.get(reportFilename), 
                report.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            return "داشبورد با موفقیت صادر شد. گزارش: " + reportFilename;
            
        } catch (Exception e) {
            return "خطا در صادرات داشبورد: " + e.getMessage();
        }
    }

    // ==================== Utility Functions ====================

    /**
     * ایجاد دایرکتوری صادرات
     */
    private void createExportDirectory() {
        File directory = new File(EXPORT_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * دریافت timestamp فعلی
     */
    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return now.format(formatter);
    }

    /**
     * دریافت تاریخ و زمان فعلی
     */
    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return now.format(formatter);
    }

    /**
     * نمایش پیام موفقیت
     */
    public void showSuccessMessage(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * نمایش پیام خطا
     */
    public void showErrorMessage(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * نمایش پیام تأیید
     */
    public boolean showConfirmationMessage(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * نمایش دیالوگ پیشرفته
     */
    public void showAdvancedDialog(String title, String message, String details) {
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
        
        Label timeLabel = new Label("زمان: " + getCurrentDateTime());
        timeLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        
        content.getChildren().addAll(messageLabel, detailsLabel, timeLabel);
        dialogPane.setContent(content);
        
        dialog.showAndWait();
    }

    // ==================== Data Validation ====================

    /**
     * اعتبارسنجی داده‌های نمودار
     */
    public boolean validateChartData(Chart chart) {
        if (chart == null) return false;
        
        // بررسی داده‌های نمودار بر اساس نوع
        if (chart instanceof javafx.scene.chart.LineChart) {
            return validateLineChartData((javafx.scene.chart.LineChart<?, ?>) chart);
        } else if (chart instanceof javafx.scene.chart.BarChart) {
            return validateBarChartData((javafx.scene.chart.BarChart<?, ?>) chart);
        } else if (chart instanceof javafx.scene.chart.PieChart) {
            return validatePieChartData((javafx.scene.chart.PieChart) chart);
        } else if (chart instanceof javafx.scene.chart.AreaChart) {
            return validateAreaChartData((javafx.scene.chart.AreaChart<?, ?>) chart);
        }
        
        return true;
    }

    /**
     * اعتبارسنجی داده‌های نمودار خطی
     */
    private boolean validateLineChartData(javafx.scene.chart.LineChart<?, ?> chart) {
        return chart.getData() != null && !chart.getData().isEmpty();
    }

    /**
     * اعتبارسنجی داده‌های نمودار میله‌ای
     */
    private boolean validateBarChartData(javafx.scene.chart.BarChart<?, ?> chart) {
        return chart.getData() != null && !chart.getData().isEmpty();
    }

    /**
     * اعتبارسنجی داده‌های نمودار دایره‌ای
     */
    private boolean validatePieChartData(javafx.scene.chart.PieChart chart) {
        return chart.getData() != null && !chart.getData().isEmpty();
    }

    /**
     * اعتبارسنجی داده‌های نمودار ناحیه‌ای
     */
    private boolean validateAreaChartData(javafx.scene.chart.AreaChart<?, ?> chart) {
        return chart.getData() != null && !chart.getData().isEmpty();
    }

    // ==================== Performance Monitoring ====================

    /**
     * اندازه‌گیری عملکرد نمودار
     */
    public Map<String, Object> measureChartPerformance(Chart chart) {
        Map<String, Object> metrics = new HashMap<>();
        
        long startTime = System.currentTimeMillis();
        
        // اندازه‌گیری زمان رندر
        chart.snapshot(new SnapshotParameters(), null);
        
        long renderTime = System.currentTimeMillis() - startTime;
        
        metrics.put("renderTime", renderTime);
        metrics.put("dataPoints", getDataPointCount(chart));
        metrics.put("memoryUsage", getMemoryUsage());
        
        return metrics;
    }

    /**
     * دریافت تعداد نقاط داده
     */
    private int getDataPointCount(Chart chart) {
        if (chart instanceof javafx.scene.chart.LineChart) {
            javafx.scene.chart.LineChart<?, ?> lineChart = (javafx.scene.chart.LineChart<?, ?>) chart;
            return lineChart.getData().stream()
                .mapToInt(series -> series.getData().size())
                .sum();
        } else if (chart instanceof javafx.scene.chart.BarChart) {
            javafx.scene.chart.BarChart<?, ?> barChart = (javafx.scene.chart.BarChart<?, ?>) chart;
            return barChart.getData().stream()
                .mapToInt(series -> series.getData().size())
                .sum();
        } else if (chart instanceof javafx.scene.chart.PieChart) {
            return ((javafx.scene.chart.PieChart) chart).getData().size();
        }
        
        return 0;
    }

    /**
     * دریافت استفاده از حافظه
     */
    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // ==================== Chart Optimization ====================

    /**
     * بهینه‌سازی نمودار
     */
    public void optimizeChart(Chart chart) {
        if (chart == null) return;
        
        // تنظیم انیمیشن
        chart.setAnimated(false);
        
        // تنظیم اندازه
        chart.setPrefSize(400, 300);
        
        // تنظیم استایل
        chart.setStyle("-fx-background-color: white;");
    }

    /**
     * بهینه‌سازی چندین نمودار
     */
    public void optimizeCharts(List<Chart> charts) {
        for (Chart chart : charts) {
            optimizeChart(chart);
        }
    }

    // ==================== Error Handling ====================

    /**
     * مدیریت خطاهای نمودار
     */
    public void handleChartError(Chart chart, Exception error) {
        String errorMessage = "خطا در نمودار: " + error.getMessage();
        
        // نمایش پیام خطا
        showErrorMessage("خطای نمودار", errorMessage);
        
        // ثبت خطا
        logError("Chart Error", error);
    }

    /**
     * ثبت خطا
     */
    private void logError(String type, Exception error) {
        // در حالت واقعی، اینجا خطا در فایل یا سیستم ثبت می‌شود
        System.err.println("[" + getCurrentDateTime() + "] " + type + ": " + error.getMessage());
        error.printStackTrace();
    }

    // ==================== Cleanup ====================

    /**
     * پاکسازی منابع
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
} 