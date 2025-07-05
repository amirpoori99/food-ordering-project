package com.myapp.analytics.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * مدل Metrics برای Real-time Dashboard
 * این کلاس تمام KPI های کلیدی سیستم را نگهداری می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
public class DashboardMetrics {
    
    private LocalDateTime generatedAt;
    
    // KPI های فروش و درآمد
    private Double totalRevenue;
    private Double todayRevenue;
    private Double revenueGrowth;
    private Double averageOrderValue;
    private Double monthlyRevenue;
    private Double weeklyRevenue;
    
    // KPI های سفارش
    private Long totalOrders;
    private Long todayOrders;
    private Long pendingOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Double orderCompletionRate;
    
    // KPI های کاربران
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    private Double userRetentionRate;
    
    // KPI های رستوران
    private Long totalRestaurants;
    private Long activeRestaurants;
    private Long pendingApprovalRestaurants;
    private Double averageRestaurantRating;
    
    // Performance Metrics
    private Double averageDeliveryTime;
    private Double customerSatisfactionScore;
    private Double systemUptime;
    private Long apiResponseTime;
    
    // Top Lists
    private List<TopRestaurant> topRestaurants;
    private List<TopItem> topItems;
    private List<TopCustomer> topCustomers;
    private List<PopularCategory> popularCategories;
    
    // Financial Metrics
    private Double totalCommissions;
    private Double deliveryFees;
    private Double refunds;
    private Double netProfit;
    private Double profitMargin;
    
    // Time-based Analytics
    private Map<String, Double> hourlyOrders;
    private Map<String, Double> dailyRevenue;
    private Map<String, Integer> weeklyOrders;
    private Map<String, Double> monthlyGrowth;
    
    // Geographic Analytics
    private Map<String, Integer> ordersByCity;
    private Map<String, Double> revenueByRegion;
    
    // Payment Analytics
    private Map<String, Integer> paymentMethods;
    private Double successfulPaymentRate;
    private Double failedPaymentRate;
    
    // Constructors
    public DashboardMetrics() {
        this.generatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public Double getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public Double getTodayRevenue() {
        return todayRevenue;
    }
    
    public void setTodayRevenue(Double todayRevenue) {
        this.todayRevenue = todayRevenue;
    }
    
    public Double getRevenueGrowth() {
        return revenueGrowth;
    }
    
    public void setRevenueGrowth(Double revenueGrowth) {
        this.revenueGrowth = revenueGrowth;
    }
    
    public Long getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public Long getTodayOrders() {
        return todayOrders;
    }
    
    public void setTodayOrders(Long todayOrders) {
        this.todayOrders = todayOrders;
    }
    
    public Double getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public void setAverageOrderValue(Double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    
    public Long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public Long getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public Long getNewUsersToday() {
        return newUsersToday;
    }
    
    public void setNewUsersToday(Long newUsersToday) {
        this.newUsersToday = newUsersToday;
    }
    
    public Long getTotalRestaurants() {
        return totalRestaurants;
    }
    
    public void setTotalRestaurants(Long totalRestaurants) {
        this.totalRestaurants = totalRestaurants;
    }
    
    public Long getActiveRestaurants() {
        return activeRestaurants;
    }
    
    public void setActiveRestaurants(Long activeRestaurants) {
        this.activeRestaurants = activeRestaurants;
    }
    
    public Double getAverageDeliveryTime() {
        return averageDeliveryTime;
    }
    
    public void setAverageDeliveryTime(Double averageDeliveryTime) {
        this.averageDeliveryTime = averageDeliveryTime;
    }
    
    public Double getOrderCompletionRate() {
        return orderCompletionRate;
    }
    
    public void setOrderCompletionRate(Double orderCompletionRate) {
        this.orderCompletionRate = orderCompletionRate;
    }
    
    public Double getCustomerSatisfactionScore() {
        return customerSatisfactionScore;
    }
    
    public void setCustomerSatisfactionScore(Double customerSatisfactionScore) {
        this.customerSatisfactionScore = customerSatisfactionScore;
    }
    
    public List<TopRestaurant> getTopRestaurants() {
        return topRestaurants;
    }
    
    public void setTopRestaurants(List<TopRestaurant> topRestaurants) {
        this.topRestaurants = topRestaurants;
    }
    
    public List<TopItem> getTopItems() {
        return topItems;
    }
    
    public void setTopItems(List<TopItem> topItems) {
        this.topItems = topItems;
    }
    
    public List<TopCustomer> getTopCustomers() {
        return topCustomers;
    }
    
    public void setTopCustomers(List<TopCustomer> topCustomers) {
        this.topCustomers = topCustomers;
    }
    
    // Additional getters and setters...
    public Double getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(Double monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    
    public Double getWeeklyRevenue() { return weeklyRevenue; }
    public void setWeeklyRevenue(Double weeklyRevenue) { this.weeklyRevenue = weeklyRevenue; }
    
    public Long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(Long pendingOrders) { this.pendingOrders = pendingOrders; }
    
    public Long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(Long completedOrders) { this.completedOrders = completedOrders; }
    
    public Long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(Long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    
    public Double getNetProfit() { return netProfit; }
    public void setNetProfit(Double netProfit) { this.netProfit = netProfit; }
    
    public Double getProfitMargin() { return profitMargin; }
    public void setProfitMargin(Double profitMargin) { this.profitMargin = profitMargin; }
    
    public Map<String, Double> getHourlyOrders() { return hourlyOrders; }
    public void setHourlyOrders(Map<String, Double> hourlyOrders) { this.hourlyOrders = hourlyOrders; }
    
    public Map<String, Double> getDailyRevenue() { return dailyRevenue; }
    public void setDailyRevenue(Map<String, Double> dailyRevenue) { this.dailyRevenue = dailyRevenue; }
    
    /**
     * محاسبه نرخ رشد درآمد روزانه
     */
    public Double calculateDailyGrowthRate() {
        if (todayRevenue != null && weeklyRevenue != null && weeklyRevenue > 0) {
            Double yesterdayRevenue = (weeklyRevenue - todayRevenue) / 6; // تقریبی
            if (yesterdayRevenue > 0) {
                return ((todayRevenue - yesterdayRevenue) / yesterdayRevenue) * 100;
            }
        }
        return 0.0;
    }
    
    /**
     * محاسبه میانگین سفارش در ساعت
     */
    public Double calculateAverageOrdersPerHour() {
        if (todayOrders != null) {
            return todayOrders / 24.0;
        }
        return 0.0;
    }
    
    /**
     * Inner classes برای Top lists
     */
    public static class TopRestaurant {
        private Long restaurantId;
        private String name;
        private Double revenue;
        private Integer orderCount;
        private Double rating;
        
        // Constructors, getters, setters...
        public TopRestaurant() {}
        
        public TopRestaurant(Long restaurantId, String name, Double revenue, Integer orderCount, Double rating) {
            this.restaurantId = restaurantId;
            this.name = name;
            this.revenue = revenue;
            this.orderCount = orderCount;
            this.rating = rating;
        }
        
        // Getters and Setters
        public Long getRestaurantId() { return restaurantId; }
        public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
        
        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
    }
    
    public static class TopItem {
        private Long itemId;
        private String name;
        private Integer orderCount;
        private Double revenue;
        private String category;
        
        public TopItem() {}
        
        public TopItem(Long itemId, String name, Integer orderCount, Double revenue, String category) {
            this.itemId = itemId;
            this.name = name;
            this.orderCount = orderCount;
            this.revenue = revenue;
            this.category = category;
        }
        
        // Getters and Setters
        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
        
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
    
    public static class TopCustomer {
        private Long userId;
        private String name;
        private Double totalSpent;
        private Integer orderCount;
        private Double averageOrderValue;
        
        public TopCustomer() {}
        
        public TopCustomer(Long userId, String name, Double totalSpent, Integer orderCount) {
            this.userId = userId;
            this.name = name;
            this.totalSpent = totalSpent;
            this.orderCount = orderCount;
            this.averageOrderValue = orderCount > 0 ? totalSpent / orderCount : 0.0;
        }
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
        
        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
        
        public Double getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    }
    
    public static class PopularCategory {
        private String categoryName;
        private Integer orderCount;
        private Double revenue;
        private Double percentage;
        
        public PopularCategory() {}
        
        public PopularCategory(String categoryName, Integer orderCount, Double revenue, Double percentage) {
            this.categoryName = categoryName;
            this.orderCount = orderCount;
            this.revenue = revenue;
            this.percentage = percentage;
        }
        
        // Getters and Setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        
        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
        
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
        
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }
} 