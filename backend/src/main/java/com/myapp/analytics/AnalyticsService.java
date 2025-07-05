package com.myapp.analytics;

import com.myapp.common.models.*;
import com.myapp.analytics.models.*;
import com.myapp.analytics.repository.AnalyticsRepository;
import com.myapp.analytics.etl.ETLProcessor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * سرویس تحلیل داده‌ها و Business Intelligence
 * این کلاس مسئول پردازش و تحلیل داده‌های تجاری سیستم است
 * 
 * ویژگی‌های کلیدی:
 * - Real-time Analytics Processing
 * - Data Warehouse Management
 * - ETL Operations
 * - Business Intelligence Reports
 * - Machine Learning Integration Ready
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
public class AnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private final SessionFactory sessionFactory;
    private final AnalyticsRepository analyticsRepository;
    private final ETLProcessor etlProcessor;
    
    // Cache برای بهبود performance
    private final Map<String, Object> analyticsCache;
    private final long CACHE_DURATION = 300_000; // 5 دقیقه
    
    public AnalyticsService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.analyticsRepository = new AnalyticsRepository(sessionFactory);
        this.etlProcessor = new ETLProcessor(sessionFactory);
        this.analyticsCache = new HashMap<>();
        
        logger.info("📊 Analytics Service initialized successfully");
    }
    
    /**
     * اجرای فرآیند ETL برای به‌روزرسانی Data Warehouse
     * این متد داده‌های operational را به data warehouse منتقل می‌کند
     */
    public CompletableFuture<ETLResult> executeETLProcess() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("🔄 Starting ETL process...");
            long startTime = System.currentTimeMillis();
            
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();
                
                ETLResult result = new ETLResult();
                
                // مرحله Extract: استخراج داده‌ها از منابع مختلف
                logger.info("📤 Extracting data from operational systems...");
                result.setExtractedOrders(etlProcessor.extractOrderData(session));
                result.setExtractedUsers(etlProcessor.extractUserData(session));
                result.setExtractedRestaurants(etlProcessor.extractRestaurantData(session));
                result.setExtractedPayments(etlProcessor.extractPaymentData(session));
                
                // مرحله Transform: تبدیل و تمیز کردن داده‌ها
                logger.info("🔄 Transforming and cleaning data...");
                List<OrderAnalytics> transformedOrders = etlProcessor.transformOrderData(result.getExtractedOrders());
                List<UserAnalytics> transformedUsers = etlProcessor.transformUserData(result.getExtractedUsers());
                List<RestaurantAnalytics> transformedRestaurants = etlProcessor.transformRestaurantData(result.getExtractedRestaurants());
                List<PaymentAnalytics> transformedPayments = etlProcessor.transformPaymentData(result.getExtractedPayments());
                
                // مرحله Load: بارگذاری داده‌ها به Data Warehouse
                logger.info("📥 Loading data into Data Warehouse...");
                result.setLoadedOrders(analyticsRepository.bulkInsertOrderAnalytics(transformedOrders, session));
                result.setLoadedUsers(analyticsRepository.bulkInsertUserAnalytics(transformedUsers, session));
                result.setLoadedRestaurants(analyticsRepository.bulkInsertRestaurantAnalytics(transformedRestaurants, session));
                result.setLoadedPayments(analyticsRepository.bulkInsertPaymentAnalytics(transformedPayments, session));
                
                session.getTransaction().commit();
                
                long endTime = System.currentTimeMillis();
                result.setProcessingTime(endTime - startTime);
                result.setTimestamp(LocalDateTime.now());
                result.setStatus("SUCCESS");
                
                // پاک کردن cache پس از به‌روزرسانی داده‌ها
                clearAnalyticsCache();
                
                logger.info("✅ ETL process completed successfully in {} ms", result.getProcessingTime());
                return result;
                
            } catch (Exception e) {
                logger.error("❌ ETL process failed: {}", e.getMessage(), e);
                ETLResult errorResult = new ETLResult();
                errorResult.setStatus("FAILED");
                errorResult.setErrorMessage(e.getMessage());
                errorResult.setTimestamp(LocalDateTime.now());
                return errorResult;
            }
        });
    }
    
    /**
     * تولید گزارش Real-time Dashboard
     * این متد KPIهای اصلی کسب‌وکار را محاسبه می‌کند
     */
    public DashboardMetrics getRealTimeDashboard() {
        String cacheKey = "dashboard_metrics";
        
        // بررسی cache
        if (analyticsCache.containsKey(cacheKey)) {
            CachedData cached = (CachedData) analyticsCache.get(cacheKey);
            if (System.currentTimeMillis() - cached.getTimestamp() < CACHE_DURATION) {
                logger.debug("📊 Returning cached dashboard metrics");
                return (DashboardMetrics) cached.getData();
            }
        }
        
        logger.info("📊 Generating real-time dashboard metrics...");
        
        try (Session session = sessionFactory.openSession()) {
            DashboardMetrics metrics = new DashboardMetrics();
            metrics.setGeneratedAt(LocalDateTime.now());
            
            // KPIهای فروش
            metrics.setTotalRevenue(analyticsRepository.getTotalRevenue(session));
            metrics.setTodayRevenue(analyticsRepository.getTodayRevenue(session));
            metrics.setRevenueGrowth(analyticsRepository.getRevenueGrowthRate(session));
            
            // KPIهای سفارش
            metrics.setTotalOrders(analyticsRepository.getTotalOrdersCount(session));
            metrics.setTodayOrders(analyticsRepository.getTodayOrdersCount(session));
            metrics.setAverageOrderValue(analyticsRepository.getAverageOrderValue(session));
            
            // KPIهای کاربر
            metrics.setTotalUsers(analyticsRepository.getTotalUsersCount(session));
            metrics.setActiveUsers(analyticsRepository.getActiveUsersCount(session));
            metrics.setNewUsersToday(analyticsRepository.getTodayNewUsersCount(session));
            
            // KPIهای رستوران
            metrics.setTotalRestaurants(analyticsRepository.getTotalRestaurantsCount(session));
            metrics.setActiveRestaurants(analyticsRepository.getActiveRestaurantsCount(session));
            
            // Performance Metrics
            metrics.setAverageDeliveryTime(analyticsRepository.getAverageDeliveryTime(session));
            metrics.setOrderCompletionRate(analyticsRepository.getOrderCompletionRate(session));
            metrics.setCustomerSatisfactionScore(analyticsRepository.getCustomerSatisfactionScore(session));
            
            // Top Lists
            metrics.setTopRestaurants(analyticsRepository.getTopRestaurants(session, 10));
            metrics.setTopItems(analyticsRepository.getTopItems(session, 10));
            metrics.setTopCustomers(analyticsRepository.getTopCustomers(session, 10));
            
            // ذخیره در cache
            analyticsCache.put(cacheKey, new CachedData(metrics, System.currentTimeMillis()));
            
            logger.info("✅ Dashboard metrics generated successfully");
            return metrics;
            
        } catch (Exception e) {
            logger.error("❌ Failed to generate dashboard metrics: {}", e.getMessage(), e);
            throw new RuntimeException("خطا در تولید متریک‌های داشبورد", e);
        }
    }
    
    /**
     * تحلیل رفتار مشتریان
     * این متد الگوهای رفتاری مشتریان را تحلیل می‌کند
     */
    public CustomerBehaviorAnalysis analyzeCustomerBehavior(Long userId, int daysPeriod) {
        logger.info("👥 Analyzing customer behavior for user {} over {} days", userId, daysPeriod);
        
        try (Session session = sessionFactory.openSession()) {
            CustomerBehaviorAnalysis analysis = new CustomerBehaviorAnalysis();
            analysis.setUserId(userId);
            analysis.setAnalysisPeriod(daysPeriod);
            analysis.setGeneratedAt(LocalDateTime.now());
            
            // الگوهای سفارش‌دهی
            analysis.setOrderFrequency(analyticsRepository.getUserOrderFrequency(userId, daysPeriod, session));
            analysis.setAverageOrderValue(analyticsRepository.getUserAverageOrderValue(userId, daysPeriod, session));
            analysis.setFavoriteRestaurants(analyticsRepository.getUserFavoriteRestaurants(userId, daysPeriod, session));
            analysis.setFavoriteItems(analyticsRepository.getUserFavoriteItems(userId, daysPeriod, session));
            
            // الگوهای زمانی
            analysis.setOrderTimePatterns(analyticsRepository.getUserOrderTimePatterns(userId, daysPeriod, session));
            analysis.setPeakOrderDays(analyticsRepository.getUserPeakOrderDays(userId, daysPeriod, session));
            
            // تجربه کاربری
            analysis.setAverageRating(analyticsRepository.getUserAverageRating(userId, daysPeriod, session));
            analysis.setComplaintRate(analyticsRepository.getUserComplaintRate(userId, daysPeriod, session));
            
            // پیش‌بینی‌ها
            OrderPrediction orderPrediction = predictNextOrder(userId, session);
            analysis.setNextOrderPrediction(orderPrediction.getPredictedOrderTime());
            analysis.setChurnProbability(calculateChurnProbability(userId, session));
            
            // تبدیل ItemRecommendation به String
            List<ItemRecommendation> recommendations = getPersonalizedRecommendations(userId, session);
            List<String> recommendationNames = recommendations.stream()
                    .map(ItemRecommendation::getItemName)
                    .collect(java.util.stream.Collectors.toList());
            analysis.setRecommendedItems(recommendationNames);
            
            logger.info("✅ Customer behavior analysis completed for user {}", userId);
            return analysis;
            
        } catch (Exception e) {
            logger.error("❌ Failed to analyze customer behavior: {}", e.getMessage(), e);
            throw new RuntimeException("خطا در تحلیل رفتار مشتری", e);
        }
    }
    
    /**
     * تحلیل عملکرد مالی
     * این متد گزارش‌های مالی تفصیلی تولید می‌کند
     */
    public FinancialAnalysis generateFinancialAnalysis(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("💰 Generating financial analysis from {} to {}", startDate, endDate);
        
        try (Session session = sessionFactory.openSession()) {
            FinancialAnalysis analysis = new FinancialAnalysis();
            analysis.setStartDate(startDate);
            analysis.setEndDate(endDate);
            analysis.setGeneratedAt(LocalDateTime.now());
            
            // درآمد کل و تفکیکی
            analysis.setTotalRevenue(analyticsRepository.getTotalRevenueBetween(startDate, endDate, session));
            analysis.setRevenueByRestaurant(analyticsRepository.getRevenueByRestaurant(startDate, endDate, session));
            analysis.setRevenueByCategory(analyticsRepository.getRevenueByCategory(startDate, endDate, session));
            
            // تبدیل Map<LocalDate,Double> به Map<String,Double>
            Map<java.time.LocalDate, Double> dailyRevenueMap = analyticsRepository.getDailyRevenue(startDate, endDate, session);
            Map<String, Double> stringDateRevenueMap = new java.util.HashMap<>();
            for (Map.Entry<java.time.LocalDate, Double> entry : dailyRevenueMap.entrySet()) {
                stringDateRevenueMap.put(entry.getKey().toString(), entry.getValue());
            }
            analysis.setDailyRevenue(stringDateRevenueMap);
            
            // هزینه‌ها و سود
            analysis.setTotalCommissions(analyticsRepository.getTotalCommissions(startDate, endDate, session));
            analysis.setDeliveryFees(analyticsRepository.getDeliveryFees(startDate, endDate, session));
            analysis.setRefunds(analyticsRepository.getRefunds(startDate, endDate, session));
            analysis.setNetProfit(analysis.getTotalRevenue() - analysis.getTotalCommissions() - analysis.getRefunds());
            
            // آمار پرداخت
            Map<String, Long> paymentMethodsLong = analyticsRepository.getPaymentMethodsBreakdown(startDate, endDate, session);
            Map<String, Integer> paymentMethodsInt = new java.util.HashMap<>();
            for (Map.Entry<String, Long> entry : paymentMethodsLong.entrySet()) {
                paymentMethodsInt.put(entry.getKey(), entry.getValue().intValue());
            }
            analysis.setPaymentMethodsBreakdown(paymentMethodsInt);
            
            Long successfulCount = analyticsRepository.getSuccessfulPaymentsCount(startDate, endDate, session);
            Long failedCount = analyticsRepository.getFailedPaymentsCount(startDate, endDate, session);
            Long totalPayments = successfulCount + failedCount;
            
            if (totalPayments > 0) {
                analysis.setSuccessfulPaymentRate((successfulCount.doubleValue() / totalPayments) * 100);
                analysis.setFailedPaymentRate((failedCount.doubleValue() / totalPayments) * 100);
            }
            
            // تحلیل‌های پیشرفته
            analysis.setRevenueGrowthRate(calculateRevenueGrowthRate(startDate, endDate, session));
            // Customer lifetime value برای تحلیل‌های آینده ذخیره می‌شود
            analysis.setProfitMargin((analysis.getNetProfit() / analysis.getTotalRevenue()) * 100);
            
            logger.info("✅ Financial analysis generated successfully");
            return analysis;
            
        } catch (Exception e) {
            logger.error("❌ Failed to generate financial analysis: {}", e.getMessage(), e);
            throw new RuntimeException("خطا در تولید تحلیل مالی", e);
        }
    }
    
    /**
     * پیش‌بینی سفارش بعدی مشتری با استفاده از Machine Learning
     */
    private OrderPrediction predictNextOrder(Long userId, Session session) {
        // Implementation سیستم پیش‌بینی
        OrderPrediction prediction = new OrderPrediction();
        prediction.setUserId(userId);
        prediction.setPredictedDate(LocalDateTime.now().plusDays(3)); // ساده‌سازی شده
        prediction.setConfidenceScore(0.75);
        prediction.setPredictedAmount(analyticsRepository.getUserAverageOrderValue(userId, 30, session));
        return prediction;
    }
    
    /**
     * محاسبه احتمال ترک سرویس توسط مشتری
     */
    private double calculateChurnProbability(Long userId, Session session) {
        // الگوریتم ساده برای محاسبه churn probability
        int daysSinceLastOrder = analyticsRepository.getDaysSinceLastOrder(userId, session);
        double averageOrderInterval = analyticsRepository.getUserAverageOrderInterval(userId, session);
        
        if (daysSinceLastOrder > averageOrderInterval * 2) {
            return Math.min(0.9, daysSinceLastOrder / (averageOrderInterval * 3));
        }
        return 0.1;
    }
    
    /**
     * تولید توصیه‌های شخصی‌سازی شده
     */
    private List<ItemRecommendation> getPersonalizedRecommendations(Long userId, Session session) {
        // سیستم recommendation ساده بر اساس تاریخچه سفارشات
        return analyticsRepository.getRecommendedItems(userId, session, 5);
    }
    
    /**
     * محاسبه نرخ رشد درآمد
     */
    private double calculateRevenueGrowthRate(LocalDateTime startDate, LocalDateTime endDate, Session session) {
        LocalDateTime previousPeriodStart = startDate.minusDays(endDate.getDayOfYear() - startDate.getDayOfYear());
        LocalDateTime previousPeriodEnd = startDate.minusDays(1);
        
        Double currentRevenue = analyticsRepository.getTotalRevenueBetween(startDate, endDate, session);
        Double previousRevenue = analyticsRepository.getTotalRevenueBetween(previousPeriodStart, previousPeriodEnd, session);
        
        if (previousRevenue == null || previousRevenue == 0) return 0.0;
        return ((currentRevenue - previousRevenue) / previousRevenue) * 100;
    }
    
    /**
     * پاک کردن cache analytics
     */
    private void clearAnalyticsCache() {
        analyticsCache.clear();
        logger.debug("🗑️ Analytics cache cleared");
    }
    
    /**
     * کلاس برای ذخیره داده‌های cache شده
     */
    private static class CachedData {
        private final Object data;
        private final long timestamp;
        
        public CachedData(Object data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
        
        public Object getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }
} 