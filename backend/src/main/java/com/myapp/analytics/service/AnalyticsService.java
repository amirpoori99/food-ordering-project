package com.myapp.analytics.service;

import com.myapp.analytics.repository.AnalyticsRepository;
import com.myapp.analytics.dto.*;
import com.myapp.common.utils.DateUtil;
import com.myapp.common.utils.MathUtil;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * سرویس Analytics و Business Intelligence
 * ارائه منطق تجاری برای تحلیل‌های پیشرفته
 */
public class AnalyticsService {
    
    private final AnalyticsRepository analyticsRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public AnalyticsService() {
        this.analyticsRepository = new AnalyticsRepository();
    }
    
    /**
     * Constructor for dependency injection (mainly for testing)
     */
    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }
    
    /**
     * دریافت آمار کلی سیستم
     */
    public SystemOverviewDTO getSystemOverview() {
        try {
            SystemOverviewDTO overview = analyticsRepository.getSystemOverview();
            return overview;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار کلی سیستم: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار فروش و درآمد
     */
    public SalesAnalyticsDTO getSalesAnalytics(String period) {
        try {
            SalesAnalyticsDTO sales = analyticsRepository.getSalesAnalytics(period);
            return sales;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار فروش: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار کاربران
     */
    public UserAnalyticsDTO getUserAnalytics(String period) {
        try {
            UserAnalyticsDTO users = analyticsRepository.getUserAnalytics(period);
            return users;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار کاربران: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار رستوران‌ها
     */
    public RestaurantAnalyticsDTO getRestaurantAnalytics(String period) {
        try {
            RestaurantAnalyticsDTO restaurants = analyticsRepository.getRestaurantAnalytics(period);
            return restaurants;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار رستوران‌ها: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار آیتم‌های محبوب
     */
    public List<PopularItemDTO> getPopularItemsAnalytics(String period, int limit) {
        try {
            return analyticsRepository.getPopularItems(period, limit);
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار آیتم‌های محبوب: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار جغرافیایی
     */
    public GeographicAnalyticsDTO getGeographicAnalytics(String period) {
        try {
            GeographicAnalyticsDTO geo = analyticsRepository.getGeographicAnalytics(period);
            return geo;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار جغرافیایی: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار عملکرد سیستم
     */
    public PerformanceAnalyticsDTO getPerformanceAnalytics(String period) {
        try {
            PerformanceAnalyticsDTO performance = analyticsRepository.getPerformanceAnalytics(period);
            return performance;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار عملکرد: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار کوپن‌ها
     */
    public CouponAnalyticsDTO getCouponAnalytics(String period) {
        try {
            CouponAnalyticsDTO coupons = analyticsRepository.getCouponAnalytics(period);
            return coupons;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار کوپن: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار پیک‌ها
     */
    public DeliveryAnalyticsDTO getDeliveryAnalytics(String period) {
        try {
            DeliveryAnalyticsDTO delivery = analyticsRepository.getDeliveryAnalytics(period);
            return delivery;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار پیک‌ها: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار روندها
     */
    public TrendAnalyticsDTO getTrendAnalytics(String period, String metric) {
        try {
            TrendAnalyticsDTO trends = analyticsRepository.getTrendAnalytics(period);
            return trends;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار روندها: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار پیش‌بینی
     */
    public PredictiveAnalyticsDTO getPredictiveAnalytics(String period) {
        try {
            PredictiveAnalyticsDTO predictions = analyticsRepository.getPredictiveAnalytics(period);
            return predictions;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار پیش‌بینی: " + e.getMessage());
        }
    }
    
    /**
     * دریافت گزارش سفارشی
     */
    public CustomReportDTO getCustomReport(String reportType, Map<String, Object> parameters) {
        try {
            CustomReportDTO report = analyticsRepository.getCustomReport(reportType, parameters);
            return report;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت گزارش سفارشی: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار real-time
     */
    public RealTimeAnalyticsDTO getRealTimeAnalytics() {
        try {
            RealTimeAnalyticsDTO realTime = analyticsRepository.getRealTimeAnalytics();
            return realTime;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار real-time: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار مقایسه‌ای
     */
    public ComparativeAnalyticsDTO getComparativeAnalytics(String period1, String period2, String metric) {
        try {
            ComparativeAnalyticsDTO comparison = analyticsRepository.getComparativeAnalytics(period1);
            return comparison;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار مقایسه‌ای: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار فصلی
     */
    public SeasonalAnalyticsDTO getSeasonalAnalytics(String year) {
        try {
            SeasonalAnalyticsDTO seasonal = analyticsRepository.getSeasonalAnalytics(year);
            return seasonal;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار فصلی: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار ROI
     */
    public ROIAnalyticsDTO getROIAnalytics(String period) {
        try {
            ROIAnalyticsDTO roi = analyticsRepository.getROIAnalytics(period);
            return roi;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار ROI: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار مشتریان وفادار
     */
    public LoyaltyAnalyticsDTO getLoyaltyAnalytics(String period) {
        try {
            LoyaltyAnalyticsDTO loyalty = analyticsRepository.getLoyaltyAnalytics(period);
            return loyalty;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار وفاداری: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار کیفیت خدمات
     */
    public QualityAnalyticsDTO getQualityAnalytics(String period) {
        try {
            QualityAnalyticsDTO quality = analyticsRepository.getQualityAnalytics(period);
            return quality;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار کیفیت: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار امنیت
     */
    public SecurityAnalyticsDTO getSecurityAnalytics(String period) {
        try {
            SecurityAnalyticsDTO security = analyticsRepository.getSecurityAnalytics(period);
            return security;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار امنیت: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار مالی
     */
    public FinancialAnalyticsDTO getFinancialAnalytics(String period) {
        try {
            FinancialAnalyticsDTO financial = analyticsRepository.getFinancialAnalytics(period);
            return financial;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار مالی: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار عملیاتی
     */
    public OperationalAnalyticsDTO getOperationalAnalytics(String period) {
        try {
            OperationalAnalyticsDTO operational = analyticsRepository.getOperationalAnalytics(period);
            return operational;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار عملیاتی: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار رقابتی
     */
    public CompetitiveAnalyticsDTO getCompetitiveAnalytics(String period) {
        try {
            CompetitiveAnalyticsDTO competitive = analyticsRepository.getCompetitiveAnalytics(period);
            return competitive;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار رقابتی: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار بازار
     */
    public MarketAnalyticsDTO getMarketAnalytics(String period) {
        try {
            MarketAnalyticsDTO market = analyticsRepository.getMarketAnalytics(period);
            return market;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار بازار: " + e.getMessage());
        }
    }
    
    /**
     * دریافت آمار نوآوری
     */
    public InnovationAnalyticsDTO getInnovationAnalytics(String period) {
        try {
            InnovationAnalyticsDTO innovation = analyticsRepository.getInnovationAnalytics(period);
            return innovation;
        } catch (Exception e) {
            throw new RuntimeException("خطا در دریافت آمار نوآوری: " + e.getMessage());
        }
    }
    
    /**
     * محاسبه نرخ رشد
     */
    private double calculateGrowth(double previous, double current) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }
} 