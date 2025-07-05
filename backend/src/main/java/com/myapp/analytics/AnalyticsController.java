package com.myapp.analytics;

import com.myapp.common.models.ApiResponse;
import com.myapp.common.utils.JsonUtil;
import com.myapp.analytics.service.AnalyticsService;
import com.myapp.analytics.dto.*;

import java.util.List;
import java.util.Map;

/**
 * کنترلر Analytics و Business Intelligence
 * ارائه API های تحلیلی و گزارش‌گیری پیشرفته
 */
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    public AnalyticsController() {
        this.analyticsService = new AnalyticsService();
    }
    
    /**
     * دریافت آمار کلی سیستم
     */
    public String getSystemOverview() {
        try {
            SystemOverviewDTO overview = analyticsService.getSystemOverview();
            return JsonUtil.toJson(new ApiResponse(true, "آمار کلی سیستم", overview));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار کلی: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار فروش و درآمد
     */
    public String getSalesAnalytics(String period) {
        try {
            SalesAnalyticsDTO sales = analyticsService.getSalesAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار فروش و درآمد", sales));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار فروش: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار کاربران
     */
    public String getUserAnalytics(String period) {
        try {
            UserAnalyticsDTO users = analyticsService.getUserAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار کاربران", users));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار کاربران: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار رستوران‌ها
     */
    public String getRestaurantAnalytics(String period) {
        try {
            RestaurantAnalyticsDTO restaurants = analyticsService.getRestaurantAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار رستوران‌ها", restaurants));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار رستوران‌ها: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار آیتم‌های محبوب
     */
    public String getPopularItemsAnalytics(String period, int limit) {
        try {
            List<PopularItemDTO> items = analyticsService.getPopularItemsAnalytics(period, limit);
            return JsonUtil.toJson(new ApiResponse(true, "آمار آیتم‌های محبوب", items));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار آیتم‌ها: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار جغرافیایی
     */
    public String getGeographicAnalytics(String period) {
        try {
            GeographicAnalyticsDTO geo = analyticsService.getGeographicAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار جغرافیایی", geo));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار جغرافیایی: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار عملکرد سیستم
     */
    public String getPerformanceAnalytics(String period) {
        try {
            PerformanceAnalyticsDTO performance = analyticsService.getPerformanceAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار عملکرد سیستم", performance));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار عملکرد: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار کوپن‌ها
     */
    public String getCouponAnalytics(String period) {
        try {
            CouponAnalyticsDTO coupons = analyticsService.getCouponAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار کوپن‌ها", coupons));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار کوپن‌ها: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار پیک‌ها
     */
    public String getDeliveryAnalytics(String period) {
        try {
            DeliveryAnalyticsDTO delivery = analyticsService.getDeliveryAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار پیک‌ها", delivery));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار پیک‌ها: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار روندها
     */
    public String getTrendAnalytics(String period, String metric) {
        try {
            TrendAnalyticsDTO trends = analyticsService.getTrendAnalytics(period, metric);
            return JsonUtil.toJson(new ApiResponse(true, "آمار روندها", trends));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار روندها: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار پیش‌بینی
     */
    public String getPredictiveAnalytics(String period) {
        try {
            PredictiveAnalyticsDTO predictions = analyticsService.getPredictiveAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار پیش‌بینی", predictions));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار پیش‌بینی: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت گزارش سفارشی
     */
    public String getCustomReport(String reportType, Map<String, Object> parameters) {
        try {
            CustomReportDTO report = analyticsService.getCustomReport(reportType, parameters);
            return JsonUtil.toJson(new ApiResponse(true, "گزارش سفارشی", report));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت گزارش سفارشی: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار real-time
     */
    public String getRealTimeAnalytics() {
        try {
            RealTimeAnalyticsDTO realTime = analyticsService.getRealTimeAnalytics();
            return JsonUtil.toJson(new ApiResponse(true, "آمار real-time", realTime));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار real-time: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار مقایسه‌ای
     */
    public String getComparativeAnalytics(String period1, String period2, String metric) {
        try {
            ComparativeAnalyticsDTO comparison = analyticsService.getComparativeAnalytics(period1, period2, metric);
            return JsonUtil.toJson(new ApiResponse(true, "آمار مقایسه‌ای", comparison));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار مقایسه‌ای: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار فصلی
     */
    public String getSeasonalAnalytics(String year) {
        try {
            SeasonalAnalyticsDTO seasonal = analyticsService.getSeasonalAnalytics(year);
            return JsonUtil.toJson(new ApiResponse(true, "آمار فصلی", seasonal));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار فصلی: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار ROI
     */
    public String getROIAnalytics(String period) {
        try {
            ROIAnalyticsDTO roi = analyticsService.getROIAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار ROI", roi));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار ROI: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار مشتریان وفادار
     */
    public String getLoyaltyAnalytics(String period) {
        try {
            LoyaltyAnalyticsDTO loyalty = analyticsService.getLoyaltyAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار مشتریان وفادار", loyalty));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار وفاداری: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار کیفیت خدمات
     */
    public String getQualityAnalytics(String period) {
        try {
            QualityAnalyticsDTO quality = analyticsService.getQualityAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار کیفیت خدمات", quality));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار کیفیت: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار امنیت
     */
    public String getSecurityAnalytics(String period) {
        try {
            SecurityAnalyticsDTO security = analyticsService.getSecurityAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار امنیت", security));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار امنیت: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار مالی
     */
    public String getFinancialAnalytics(String period) {
        try {
            FinancialAnalyticsDTO financial = analyticsService.getFinancialAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار مالی", financial));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار مالی: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار عملیاتی
     */
    public String getOperationalAnalytics(String period) {
        try {
            OperationalAnalyticsDTO operational = analyticsService.getOperationalAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار عملیاتی", operational));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار عملیاتی: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار رقابتی
     */
    public String getCompetitiveAnalytics(String period) {
        try {
            CompetitiveAnalyticsDTO competitive = analyticsService.getCompetitiveAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار رقابتی", competitive));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار رقابتی: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار بازار
     */
    public String getMarketAnalytics(String period) {
        try {
            MarketAnalyticsDTO market = analyticsService.getMarketAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار بازار", market));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار بازار: " + e.getMessage()));
        }
    }
    
    /**
     * دریافت آمار نوآوری
     */
    public String getInnovationAnalytics(String period) {
        try {
            InnovationAnalyticsDTO innovation = analyticsService.getInnovationAnalytics(period);
            return JsonUtil.toJson(new ApiResponse(true, "آمار نوآوری", innovation));
        } catch (Exception e) {
            return JsonUtil.toJson(new ApiResponse(false, "خطا در دریافت آمار نوآوری: " + e.getMessage()));
        }
    }
} 