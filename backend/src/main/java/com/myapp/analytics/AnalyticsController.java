package com.myapp.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.myapp.common.models.ApiResponse;
import com.myapp.common.utils.JsonUtil;
import com.myapp.analytics.AnalyticsService;
import com.myapp.analytics.dto.*;
import com.myapp.analytics.models.*;
import com.myapp.common.utils.DatabaseUtil;
import com.google.gson.Gson;
import org.hibernate.SessionFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * کنترلر Analytics و Business Intelligence
 * ارائه API های تحلیلی و گزارشی پیشرفته
 */
public class AnalyticsController implements HttpHandler {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    public AnalyticsController() {
        this.analyticsService = new AnalyticsService();
        this.objectMapper = new ObjectMapper();
        // اضافه کردن پشتیبانی از Java 8 Date/Time
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * دریافت آمار کلی سیستم
     */
    public ApiResponse<SystemOverviewDTO> getSystemOverview() {
        try {
            SystemOverviewDTO overview = analyticsService.getSystemOverview();
            return ApiResponse.success(overview, "آمار کلی سیستم با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار کلی سیستم: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار فروش
     */
    public ApiResponse<SalesAnalyticsDTO> getSalesAnalytics(String period) {
        try {
            SalesAnalyticsDTO salesData = analyticsService.getSalesAnalytics(period);
            return ApiResponse.success(salesData, "آمار فروش با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار فروش: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار کاربران
     */
    public ApiResponse<UserAnalyticsDTO> getUserAnalytics(String period) {
        try {
            UserAnalyticsDTO userData = analyticsService.getUserAnalytics(period);
            return ApiResponse.success(userData, "آمار کاربران با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار کاربران: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار رستوران‌ها
     */
    public ApiResponse<RestaurantAnalyticsDTO> getRestaurantAnalytics(String period) {
        try {
            RestaurantAnalyticsDTO restaurantData = analyticsService.getRestaurantAnalytics(period);
            return ApiResponse.success(restaurantData, "آمار رستوران‌ها با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار رستوران‌ها: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار مالی
     */
    public ApiResponse<FinancialAnalyticsDTO> getFinancialAnalytics(String period) {
        try {
            FinancialAnalyticsDTO financialData = analyticsService.getFinancialAnalytics(period);
            return ApiResponse.success(financialData, "آمار مالی با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار مالی: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار تحویل
     */
    public ApiResponse<DeliveryAnalyticsDTO> getDeliveryAnalytics(String period) {
        try {
            DeliveryAnalyticsDTO deliveryData = analyticsService.getDeliveryAnalytics(period);
            return ApiResponse.success(deliveryData, "آمار تحویل با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار تحویل: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار عملکرد
     */
    public ApiResponse<PerformanceAnalyticsDTO> getPerformanceAnalytics(String period) {
        try {
            PerformanceAnalyticsDTO performanceData = analyticsService.getPerformanceAnalytics(period);
            return ApiResponse.success(performanceData, "آمار عملکرد با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار عملکرد: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار کیفیت
     */
    public ApiResponse<QualityAnalyticsDTO> getQualityAnalytics(String period) {
        try {
            QualityAnalyticsDTO qualityData = analyticsService.getQualityAnalytics(period);
            return ApiResponse.success(qualityData, "آمار کیفیت با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار کیفیت: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار بازار
     */
    public ApiResponse<MarketAnalyticsDTO> getMarketAnalytics(String period) {
        try {
            MarketAnalyticsDTO marketData = analyticsService.getMarketAnalytics(period);
            return ApiResponse.success(marketData, "آمار بازار با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار بازار: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار روندها
     */
    public ApiResponse<TrendAnalyticsDTO> getTrendAnalytics(String period) {
        try {
            TrendAnalyticsDTO trendData = analyticsService.getTrendAnalytics(period);
            return ApiResponse.success(trendData, "آمار روندها با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار روندها: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار فصلی
     */
    public ApiResponse<SeasonalAnalyticsDTO> getSeasonalAnalytics(String period) {
        try {
            SeasonalAnalyticsDTO seasonalData = analyticsService.getSeasonalAnalytics(period);
            return ApiResponse.success(seasonalData, "آمار فصلی با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار فصلی: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار جغرافیایی
     */
    public ApiResponse<GeographicAnalyticsDTO> getGeographicAnalytics(String period) {
        try {
            GeographicAnalyticsDTO geographicData = analyticsService.getGeographicAnalytics(period);
            return ApiResponse.success(geographicData, "آمار جغرافیایی با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار جغرافیایی: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار عملیاتی
     */
    public ApiResponse<OperationalAnalyticsDTO> getOperationalAnalytics(String period) {
        try {
            OperationalAnalyticsDTO operationalData = analyticsService.getOperationalAnalytics(period);
            return ApiResponse.success(operationalData, "آمار عملیاتی با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار عملیاتی: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار وفاداری
     */
    public ApiResponse<LoyaltyAnalyticsDTO> getLoyaltyAnalytics(String period) {
        try {
            LoyaltyAnalyticsDTO loyaltyData = analyticsService.getLoyaltyAnalytics(period);
            return ApiResponse.success(loyaltyData, "آمار وفاداری با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار وفاداری: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار امنیت
     */
    public ApiResponse<SecurityAnalyticsDTO> getSecurityAnalytics(String period) {
        try {
            SecurityAnalyticsDTO securityData = analyticsService.getSecurityAnalytics(period);
            return ApiResponse.success(securityData, "آمار امنیت با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار امنیت: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار رقابتی
     */
    public ApiResponse<CompetitiveAnalyticsDTO> getCompetitiveAnalytics(String period) {
        try {
            CompetitiveAnalyticsDTO competitiveData = analyticsService.getCompetitiveAnalytics(period);
            return ApiResponse.success(competitiveData, "آمار رقابتی با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار رقابتی: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار مقایسه‌ای
     */
    public ApiResponse<ComparativeAnalyticsDTO> getComparativeAnalytics(String period) {
        try {
            ComparativeAnalyticsDTO comparativeData = analyticsService.getComparativeAnalytics(period);
            return ApiResponse.success(comparativeData, "آمار مقایسه‌ای با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار مقایسه‌ای: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار پیش‌بینی
     */
    public ApiResponse<PredictiveAnalyticsDTO> getPredictiveAnalytics(String period) {
        try {
            PredictiveAnalyticsDTO predictiveData = analyticsService.getPredictiveAnalytics(period);
            return ApiResponse.success(predictiveData, "آمار پیش‌بینی با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار پیش‌بینی: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار ROI
     */
    public ApiResponse<ROIAnalyticsDTO> getROIAnalytics(String period) {
        try {
            ROIAnalyticsDTO roiData = analyticsService.getROIAnalytics(period);
            return ApiResponse.success(roiData, "آمار ROI با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار ROI: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار نوآوری
     */
    public ApiResponse<InnovationAnalyticsDTO> getInnovationAnalytics(String period) {
        try {
            InnovationAnalyticsDTO innovationData = analyticsService.getInnovationAnalytics(period);
            return ApiResponse.success(innovationData, "آمار نوآوری با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار نوآوری: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار کوپن
     */
    public ApiResponse<CouponAnalyticsDTO> getCouponAnalytics(String period) {
        try {
            CouponAnalyticsDTO couponData = analyticsService.getCouponAnalytics(period);
            return ApiResponse.success(couponData, "آمار کوپن با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار کوپن: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار آنی
     */
    public ApiResponse<RealTimeAnalyticsDTO> getRealTimeAnalytics() {
        try {
            RealTimeAnalyticsDTO realTimeData = analyticsService.getRealTimeAnalytics();
            return ApiResponse.success(realTimeData, "آمار آنی با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آمار آنی: " + e.getMessage());
        }
    }

    /**
     * دریافت آیتم‌های محبوب
     */
    public ApiResponse<List<PopularItemDTO>> getPopularItems(String period, int limit) {
        try {
            List<PopularItemDTO> popularItems = analyticsService.getPopularItems(period, limit);
            return ApiResponse.success(popularItems, "آیتم‌های محبوب با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت آیتم‌های محبوب: " + e.getMessage());
        }
    }

    /**
     * دریافت گزارش سفارشی
     */
    public ApiResponse<CustomReportDTO> getCustomReport(String reportType, Map<String, Object> parameters) {
        try {
            CustomReportDTO customReport = analyticsService.getCustomReport(reportType, parameters);
            return ApiResponse.success(customReport, "گزارش سفارشی با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت گزارش سفارشی: " + e.getMessage());
        }
    }

    /**
     * دریافت آمار کلی سیستم به صورت JSON
     */
    public String getSystemOverviewJson() {
        try {
            SystemOverviewDTO overview = analyticsService.getSystemOverview();
            return JsonUtil.toJson(ApiResponse.success(overview, "آمار کلی سیستم با موفقیت دریافت شد"));
        } catch (Exception e) {
            return JsonUtil.toJson(ApiResponse.error("خطا در دریافت آمار کلی سیستم: " + e.getMessage()));
        }
    }

    /**
     * دریافت آمار فروش به صورت JSON
     */
    public String getSalesAnalyticsJson(String period) {
        try {
            SalesAnalyticsDTO salesData = analyticsService.getSalesAnalytics(period);
            return JsonUtil.toJson(ApiResponse.success(salesData, "آمار فروش با موفقیت دریافت شد"));
        } catch (Exception e) {
            return JsonUtil.toJson(ApiResponse.error("خطا در دریافت آمار فروش: " + e.getMessage()));
        }
    }

    /**
     * دریافت آمار کاربران به صورت JSON
     */
    public String getUserAnalyticsJson(String period) {
        try {
            UserAnalyticsDTO userData = analyticsService.getUserAnalytics(period);
            return JsonUtil.toJson(ApiResponse.success(userData, "آمار کاربران با موفقیت دریافت شد"));
        } catch (Exception e) {
            return JsonUtil.toJson(ApiResponse.error("خطا در دریافت آمار کاربران: " + e.getMessage()));
        }
    }

    /**
     * دریافت داشبورد آنی
     */
    public ApiResponse<DashboardMetrics> getRealTimeDashboard() {
        try {
            DashboardMetrics dashboardData = analyticsService.getRealTimeDashboard();
            return ApiResponse.success(dashboardData, "داشبورد آنی با موفقیت دریافت شد");
        } catch (Exception e) {
            return ApiResponse.error("خطا در دریافت داشبورد آنی: " + e.getMessage());
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        
        try {
            if (method.equals("GET")) {
                if (path.endsWith("/dashboard")) {
                    handleGetDashboard(exchange);
                } else if (path.endsWith("/system-overview")) {
                    handleGetSystemOverview(exchange);
                } else if (path.endsWith("/sales-analytics")) {
                    handleGetSalesAnalytics(exchange);
                } else if (path.endsWith("/user-analytics")) {
                    handleGetUserAnalytics(exchange);
                } else if (path.endsWith("/restaurant-analytics")) {
                    handleGetRestaurantAnalytics(exchange);
                } else if (path.endsWith("/financial-analytics")) {
                    handleGetFinancialAnalytics(exchange);
                } else if (path.endsWith("/delivery-analytics")) {
                    handleGetDeliveryAnalytics(exchange);
                } else if (path.endsWith("/performance-analytics")) {
                    handleGetPerformanceAnalytics(exchange);
                } else if (path.endsWith("/quality-analytics")) {
                    handleGetQualityAnalytics(exchange);
                } else if (path.endsWith("/market-analytics")) {
                    handleGetMarketAnalytics(exchange);
                } else if (path.endsWith("/trend-analytics")) {
                    handleGetTrendAnalytics(exchange);
                } else if (path.endsWith("/seasonal-analytics")) {
                    handleGetSeasonalAnalytics(exchange);
                } else if (path.endsWith("/geographic-analytics")) {
                    handleGetGeographicAnalytics(exchange);
                } else if (path.endsWith("/operational-analytics")) {
                    handleGetOperationalAnalytics(exchange);
                } else if (path.endsWith("/loyalty-analytics")) {
                    handleGetLoyaltyAnalytics(exchange);
                } else if (path.endsWith("/security-analytics")) {
                    handleGetSecurityAnalytics(exchange);
                } else if (path.endsWith("/competitive-analytics")) {
                    handleGetCompetitiveAnalytics(exchange);
                } else if (path.endsWith("/comparative-analytics")) {
                    handleGetComparativeAnalytics(exchange);
                } else if (path.endsWith("/predictive-analytics")) {
                    handleGetPredictiveAnalytics(exchange);
                } else if (path.endsWith("/roi-analytics")) {
                    handleGetROIAnalytics(exchange);
                } else if (path.endsWith("/innovation-analytics")) {
                    handleGetInnovationAnalytics(exchange);
                } else if (path.endsWith("/coupon-analytics")) {
                    handleGetCouponAnalytics(exchange);
                } else if (path.endsWith("/real-time-analytics")) {
                    handleGetRealTimeAnalytics(exchange);
                } else if (path.endsWith("/popular-items")) {
                    handleGetPopularItems(exchange);
                } else if (path.endsWith("/custom-report")) {
                    handleGetCustomReport(exchange);
                } else {
                    sendResponse(exchange, 404, "Endpoint not found");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = objectMapper.writeValueAsString(data);
        sendResponse(exchange, statusCode, jsonResponse);
    }
    
    // Handler methods for each endpoint
    private void handleGetDashboard(HttpExchange exchange) throws IOException {
        ApiResponse<DashboardMetrics> response = getRealTimeDashboard();
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetSystemOverview(HttpExchange exchange) throws IOException {
        ApiResponse<SystemOverviewDTO> response = getSystemOverview();
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetSalesAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<SalesAnalyticsDTO> response = getSalesAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetUserAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<UserAnalyticsDTO> response = getUserAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetRestaurantAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<RestaurantAnalyticsDTO> response = getRestaurantAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetFinancialAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<FinancialAnalyticsDTO> response = getFinancialAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetDeliveryAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<DeliveryAnalyticsDTO> response = getDeliveryAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetPerformanceAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<PerformanceAnalyticsDTO> response = getPerformanceAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetQualityAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<QualityAnalyticsDTO> response = getQualityAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetMarketAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<MarketAnalyticsDTO> response = getMarketAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetTrendAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<TrendAnalyticsDTO> response = getTrendAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetSeasonalAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<SeasonalAnalyticsDTO> response = getSeasonalAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetGeographicAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<GeographicAnalyticsDTO> response = getGeographicAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetOperationalAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<OperationalAnalyticsDTO> response = getOperationalAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetLoyaltyAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<LoyaltyAnalyticsDTO> response = getLoyaltyAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetSecurityAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<SecurityAnalyticsDTO> response = getSecurityAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetCompetitiveAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<CompetitiveAnalyticsDTO> response = getCompetitiveAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetComparativeAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<ComparativeAnalyticsDTO> response = getComparativeAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetPredictiveAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<PredictiveAnalyticsDTO> response = getPredictiveAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetROIAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<ROIAnalyticsDTO> response = getROIAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetInnovationAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<InnovationAnalyticsDTO> response = getInnovationAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetCouponAnalytics(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        ApiResponse<CouponAnalyticsDTO> response = getCouponAnalytics(period);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetRealTimeAnalytics(HttpExchange exchange) throws IOException {
        ApiResponse<RealTimeAnalyticsDTO> response = getRealTimeAnalytics();
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetPopularItems(HttpExchange exchange) throws IOException {
        String period = getQueryParameter(exchange, "period", "month");
        int limit = Integer.parseInt(getQueryParameter(exchange, "limit", "10"));
        ApiResponse<List<PopularItemDTO>> response = getPopularItems(period, limit);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void handleGetCustomReport(HttpExchange exchange) throws IOException {
        // This would need to parse the request body for parameters
        ApiResponse<CustomReportDTO> response = getCustomReport("default", Map.of());
        sendJsonResponse(exchange, 200, response);
    }
    
    private String getQueryParameter(HttpExchange exchange, String name, String defaultValue) {
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(name)) {
                    return keyValue[1];
                }
            }
        }
        return defaultValue;
    }
} 