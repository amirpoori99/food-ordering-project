package com.myapp.ui.analytics.util;

import javafx.scene.chart.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.*;

/**
 * ابزارهای نمودار برای Analytics
 * مسئول استایل‌دهی و فرمت‌بندی نمودارها
 * 
 * @author Food Ordering System
 * @version 1.0
 * @since 2024
 */
public class ChartUtil {

    // رنگ‌های پیش‌فرض برای نمودارها
    private static final Color[] CHART_COLORS = {
        Color.web("#007bff"), // آبی
        Color.web("#28a745"), // سبز
        Color.web("#ffc107"), // زرد
        Color.web("#dc3545"), // قرمز
        Color.web("#17a2b8"), // آبی روشن
        Color.web("#6f42c1"), // بنفش
        Color.web("#fd7e14"), // نارنجی
        Color.web("#20c997"), // سبز روشن
        Color.web("#e83e8c"), // صورتی
        Color.web("#6c757d")  // خاکستری
    };

    // استایل‌های پیش‌فرض
    private static final String CHART_STYLE = """
        .chart {
            -fx-background-color: white;
            -fx-background-radius: 8;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);
        }
        .chart-title {
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-text-fill: #333333;
        }
        .axis-label {
            -fx-font-size: 12px;
            -fx-text-fill: #666666;
        }
        .chart-legend {
            -fx-background-color: transparent;
        }
        .chart-legend-item {
            -fx-text-fill: #333333;
        }
        """;

    /**
     * اعمال استایل به نمودار
     */
    @SuppressWarnings("unchecked")
    public void applyChartStyle(Chart chart, String styleType) {
        if (chart == null || styleType == null) return;

        // اعمال استایل پایه
        chart.getStyleClass().add("chart");
        
        // اعمال استایل خاص بر اساس نوع
        switch (styleType) {
            case "sales-trend":
                applySalesTrendStyle((LineChart<String, Number>) chart);
                break;
            case "user-distribution":
                applyUserDistributionStyle((PieChart) chart);
                break;
            case "restaurant-performance":
                applyRestaurantPerformanceStyle((BarChart<String, Number>) chart);
                break;
            case "revenue":
                applyRevenueStyle((AreaChart<String, Number>) chart);
                break;
            case "order-status":
                applyOrderStatusStyle((PieChart) chart);
                break;
            case "user-activity":
                applyUserActivityStyle((LineChart<String, Number>) chart);
                break;
            case "geographic-distribution":
                applyGeographicDistributionStyle((BarChart<String, Number>) chart);
                break;
            case "performance-metrics":
                applyPerformanceMetricsStyle((LineChart<String, Number>) chart);
                break;
            default:
                applyDefaultStyle(chart);
        }
    }

    /**
     * اعمال استایل نمودار روند فروش
     */
    private void applySalesTrendStyle(LineChart<String, Number> chart) {
        chart.setTitle("روند فروش");
        chart.setAnimated(true);
        
        // تنظیم رنگ خط
        if (!chart.getData().isEmpty()) {
            XYChart.Series<String, Number> series = chart.getData().get(0);
            series.getNode().setStyle("-fx-stroke: #007bff; -fx-stroke-width: 3;");
        }
        
        // تنظیم رنگ نقاط
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-background-color: #007bff; -fx-background-radius: 3;");
                }
            }
        }
    }

    /**
     * اعمال استایل نمودار توزیع کاربران
     */
    private void applyUserDistributionStyle(PieChart chart) {
        chart.setTitle("توزیع کاربران");
        chart.setAnimated(true);
        
        // تنظیم رنگ‌های بخش‌ها
        for (int i = 0; i < chart.getData().size(); i++) {
            PieChart.Data data = chart.getData().get(i);
            Color color = CHART_COLORS[i % CHART_COLORS.length];
            data.getNode().setStyle("-fx-pie-color: " + colorToHex(color) + ";");
        }
    }

    /**
     * اعمال استایل نمودار عملکرد رستوران‌ها
     */
    private void applyRestaurantPerformanceStyle(BarChart<String, Number> chart) {
        chart.setTitle("عملکرد رستوران‌ها");
        chart.setAnimated(true);
        
        // تنظیم رنگ میله‌ها
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-bar-fill: #28a745;");
                }
            }
        }
    }

    /**
     * اعمال استایل نمودار درآمد
     */
    private void applyRevenueStyle(AreaChart<String, Number> chart) {
        chart.setTitle("درآمد");
        chart.setAnimated(true);
        
        // تنظیم رنگ ناحیه
        for (XYChart.Series<String, Number> series : chart.getData()) {
            series.getNode().setStyle("-fx-fill: #17a2b8; -fx-opacity: 0.7;");
        }
    }

    /**
     * اعمال استایل نمودار وضعیت سفارشات
     */
    private void applyOrderStatusStyle(PieChart chart) {
        chart.setTitle("وضعیت سفارشات");
        chart.setAnimated(true);
        
        // تنظیم رنگ‌های خاص برای وضعیت‌ها
        for (PieChart.Data data : chart.getData()) {
            String label = data.getName();
            Color color;
            
            switch (label) {
                case "تحویل شده":
                    color = Color.web("#28a745");
                    break;
                case "در راه":
                    color = Color.web("#17a2b8");
                    break;
                case "در حال آماده‌سازی":
                    color = Color.web("#ffc107");
                    break;
                case "لغو شده":
                    color = Color.web("#dc3545");
                    break;
                default:
                    color = Color.web("#6c757d");
            }
            
            data.getNode().setStyle("-fx-pie-color: " + colorToHex(color) + ";");
        }
    }

    /**
     * اعمال استایل نمودار فعالیت کاربران
     */
    private void applyUserActivityStyle(LineChart<String, Number> chart) {
        chart.setTitle("فعالیت کاربران");
        chart.setAnimated(true);
        
        // تنظیم رنگ خط
        if (!chart.getData().isEmpty()) {
            XYChart.Series<String, Number> series = chart.getData().get(0);
            series.getNode().setStyle("-fx-stroke: #6f42c1; -fx-stroke-width: 3;");
        }
    }

    /**
     * اعمال استایل نمودار توزیع جغرافیایی
     */
    private void applyGeographicDistributionStyle(BarChart<String, Number> chart) {
        chart.setTitle("توزیع جغرافیایی");
        chart.setAnimated(true);
        
        // تنظیم رنگ میله‌ها
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-bar-fill: #fd7e14;");
                }
            }
        }
    }

    /**
     * اعمال استایل نمودار معیارهای عملکرد
     */
    private void applyPerformanceMetricsStyle(LineChart<String, Number> chart) {
        chart.setTitle("معیارهای عملکرد");
        chart.setAnimated(true);
        
        // تنظیم رنگ‌های مختلف برای هر سری
        for (int i = 0; i < chart.getData().size(); i++) {
            XYChart.Series<String, Number> series = chart.getData().get(i);
            Color color = CHART_COLORS[i % CHART_COLORS.length];
            series.getNode().setStyle("-fx-stroke: " + colorToHex(color) + "; -fx-stroke-width: 2;");
        }
    }

    /**
     * اعمال استایل پیش‌فرض
     */
    private void applyDefaultStyle(Chart chart) {
        chart.setAnimated(true);
    }

    /**
     * تبدیل رنگ به فرمت hex
     */
    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    /**
     * ایجاد نمودار خالی با استایل
     */
    public Chart createEmptyChart(String chartType, String title) {
        Chart chart = null;
        
        switch (chartType.toLowerCase()) {
            case "line":
                chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
                break;
            case "bar":
                chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
                break;
            case "pie":
                chart = new PieChart();
                break;
            case "area":
                chart = new AreaChart<>(new CategoryAxis(), new NumberAxis());
                break;
            default:
                chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        }
        
        chart.setTitle(title);
        applyChartStyle(chart, "default");
        
        return chart;
    }

    /**
     * ایجاد کارت نمودار
     */
    public VBox createChartCard(Chart chart, String title, String description) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        // عنوان
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #333333;");
        
        // توضیحات
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        
        // نمودار
        chart.setPrefSize(400, 300);
        
        card.getChildren().addAll(titleLabel, descLabel, chart);
        
        return card;
    }

    /**
     * ایجاد کارت آمار
     */
    public VBox createStatsCard(String title, String value, String icon, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        // آیکون
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 24));
        
        // عنوان
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        titleLabel.setStyle("-fx-text-fill: #6c757d;");
        
        // مقدار
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        
        return card;
    }

    /**
     * ایجاد کارت مقایسه
     */
    public HBox createComparisonCard(String title, String currentValue, String previousValue, String changePercent) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        VBox leftContent = new VBox(5);
        VBox rightContent = new VBox(5);
        
        // عنوان
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #333333;");
        
        // مقدار فعلی
        Label currentLabel = new Label("فعلی: " + currentValue);
        currentLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        currentLabel.setStyle("-fx-text-fill: #007bff;");
        
        // مقدار قبلی
        Label previousLabel = new Label("قبلی: " + previousValue);
        previousLabel.setFont(Font.font("System", 12));
        previousLabel.setStyle("-fx-text-fill: #6c757d;");
        
        // درصد تغییر
        Label changeLabel = new Label(changePercent);
        changeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // تعیین رنگ بر اساس مثبت یا منفی بودن
        if (changePercent.contains("+")) {
            changeLabel.setStyle("-fx-text-fill: #28a745;");
        } else if (changePercent.contains("-")) {
            changeLabel.setStyle("-fx-text-fill: #dc3545;");
        } else {
            changeLabel.setStyle("-fx-text-fill: #6c757d;");
        }
        
        leftContent.getChildren().addAll(titleLabel, currentLabel, previousLabel);
        rightContent.getChildren().add(changeLabel);
        
        card.getChildren().addAll(leftContent, rightContent);
        
        return card;
    }

    /**
     * تنظیم رنگ‌های نمودار
     */
    public void setChartColors(Chart chart, Color... colors) {
        if (chart instanceof PieChart) {
            PieChart pieChart = (PieChart) chart;
            for (int i = 0; i < pieChart.getData().size() && i < colors.length; i++) {
                PieChart.Data data = pieChart.getData().get(i);
                data.getNode().setStyle("-fx-pie-color: " + colorToHex(colors[i]) + ";");
            }
        }
    }

    /**
     * تنظیم انیمیشن نمودار
     */
    public void setChartAnimation(Chart chart, boolean animated) {
        chart.setAnimated(animated);
    }

    /**
     * تنظیم اندازه نمودار
     */
    public void setChartSize(Chart chart, double width, double height) {
        chart.setPrefSize(width, height);
        chart.setMinSize(width, height);
        chart.setMaxSize(width, height);
    }

    /**
     * دریافت استایل CSS
     */
    public String getChartCSS() {
        return CHART_STYLE;
    }

    /**
     * اعمال استایل CSS به نمودار
     */
    public void applyCSS(Chart chart, String css) {
        chart.setStyle(css);
    }
} 