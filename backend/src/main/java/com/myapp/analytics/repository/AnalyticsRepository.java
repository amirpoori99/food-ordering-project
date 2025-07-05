package com.myapp.analytics.repository;

import com.myapp.common.models.*;
import com.myapp.analytics.models.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository برای عملیات Analytics و Data Warehouse
 * این کلاس تمام عملیات پایگاه داده مربوط به Analytics را مدیریت می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
public class AnalyticsRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsRepository.class);
    private final SessionFactory sessionFactory;
    
    public AnalyticsRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        logger.info("📊 Analytics Repository initialized");
    }
    
    /**
     * دریافت کل درآمد سیستم
     */
    public Double getTotalRevenue(Session session) {
        try {
            String hql = "SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED'";
            Double revenue = session.createQuery(hql, Double.class).uniqueResult();
            return revenue != null ? revenue : 0.0;
        } catch (Exception e) {
            logger.error("خطا در دریافت کل درآمد: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * دریافت درآمد امروز
     */
    public Double getTodayRevenue(Session session) {
        try {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            String hql = "SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED' " +
                        "AND o.createdAt >= :start AND o.createdAt < :end";
            
            Double revenue = session.createQuery(hql, Double.class)
                    .setParameter("start", startOfDay)
                    .setParameter("end", endOfDay)
                    .uniqueResult();
                    
            return revenue != null ? revenue : 0.0;
        } catch (Exception e) {
            logger.error("خطا در دریافت درآمد امروز: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * محاسبه نرخ رشد درآمد
     */
    public Double getRevenueGrowthRate(Session session) {
        try {
            // درآمد این ماه
            LocalDateTime thisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            Double currentRevenue = getRevenueBetween(thisMonth, LocalDateTime.now(), session);
            
            // درآمد ماه گذشته
            LocalDateTime lastMonth = thisMonth.minusMonths(1);
            LocalDateTime endLastMonth = thisMonth.minusDays(1);
            Double previousRevenue = getRevenueBetween(lastMonth, endLastMonth, session);
            
            if (previousRevenue != null && previousRevenue > 0) {
                return ((currentRevenue - previousRevenue) / previousRevenue) * 100;
            }
            return 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه نرخ رشد: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * تعداد کل سفارشات
     */
    public Long getTotalOrdersCount(Session session) {
        try {
            String hql = "SELECT COUNT(o) FROM Order o";
            return session.createQuery(hql, Long.class).uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در دریافت تعداد سفارشات: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * تعداد سفارشات امروز
     */
    public Long getTodayOrdersCount(Session session) {
        try {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            String hql = "SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end";
            return session.createQuery(hql, Long.class)
                    .setParameter("start", startOfDay)
                    .setParameter("end", endOfDay)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در دریافت تعداد سفارشات امروز: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * میانگین مبلغ سفارش
     */
    public Double getAverageOrderValue(Session session) {
        try {
            String hql = "SELECT AVG(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED'";
            Double avg = session.createQuery(hql, Double.class).uniqueResult();
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه میانگین سفارش: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * تعداد کل کاربران
     */
    public Long getTotalUsersCount(Session session) {
        try {
            String hql = "SELECT COUNT(u) FROM User u";
            return session.createQuery(hql, Long.class).uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در دریافت تعداد کاربران: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * تعداد کاربران فعال (30 روز گذشته)
     */
    public Long getActiveUsersCount(Session session) {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            String hql = "SELECT COUNT(DISTINCT o.user.id) FROM Order o WHERE o.createdAt >= :date";
            return session.createQuery(hql, Long.class)
                    .setParameter("date", thirtyDaysAgo)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در دریافت کاربران فعال: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * تعداد کاربران جدید امروز
     */
    public Long getTodayNewUsersCount(Session session) {
        try {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            String hql = "SELECT COUNT(u) FROM User u WHERE u.createdAt >= :start AND u.createdAt < :end";
            return session.createQuery(hql, Long.class)
                    .setParameter("start", startOfDay)
                    .setParameter("end", endOfDay)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در دریافت کاربران جدید امروز: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * تعداد کل رستوران‌ها
     */
    public Long getTotalRestaurantsCount(Session session) {
        try {
            String hql = "SELECT COUNT(r) FROM Restaurant r";
            return session.createQuery(hql, Long.class).uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در دریافت تعداد رستوران‌ها: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * تعداد رستوران‌های فعال
     */
    public Long getActiveRestaurantsCount(Session session) {
        try {
            String hql = "SELECT COUNT(r) FROM Restaurant r WHERE r.isActive = true";
            return session.createQuery(hql, Long.class).uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در دریافت رستوران‌های فعال: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * میانگین زمان تحویل (دقیقه)
     */
    public Double getAverageDeliveryTime(Session session) {
        try {
            // استفاده از HQL به جای Native SQL برای امنیت بیشتر
            String hql = "SELECT AVG(TIMESTAMPDIFF(MINUTE, o.createdAt, o.deliveredAt)) " +
                        "FROM Order o WHERE o.status = :status AND o.deliveredAt IS NOT NULL";
            
            Double avgMinutes = session.createQuery(hql, Double.class)
                    .setParameter("status", "COMPLETED")
                    .uniqueResult();
            return avgMinutes != null ? avgMinutes : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه میانگین زمان تحویل: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * نرخ تکمیل سفارش
     */
    public Double getOrderCompletionRate(Session session) {
        try {
            String completedHql = "SELECT COUNT(o) FROM Order o WHERE o.status = 'COMPLETED'";
            String totalHql = "SELECT COUNT(o) FROM Order o";
            
            Long completed = session.createQuery(completedHql, Long.class).uniqueResult();
            Long total = session.createQuery(totalHql, Long.class).uniqueResult();
            
            if (total != null && total > 0) {
                return (completed.doubleValue() / total.doubleValue()) * 100;
            }
            return 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه نرخ تکمیل سفارش: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * امتیاز رضایت مشتری
     */
    public Double getCustomerSatisfactionScore(Session session) {
        try {
            String hql = "SELECT AVG(r.rating) FROM Rating r";
            Double avg = session.createQuery(hql, Double.class).uniqueResult();
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه رضایت مشتری: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * برترین رستوران‌ها
     */
    public List<DashboardMetrics.TopRestaurant> getTopRestaurants(Session session, int limit) {
        try {
            String hql = "SELECT r.id, r.name, SUM(o.totalAmount), COUNT(o), AVG(rt.rating) " +
                        "FROM Order o JOIN o.restaurant r LEFT JOIN Rating rt ON rt.restaurant.id = r.id " +
                        "WHERE o.status = 'COMPLETED' " +
                        "GROUP BY r.id, r.name " +
                        "ORDER BY SUM(o.totalAmount) DESC";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setMaxResults(limit)
                    .getResultList();
            
            return results.stream()
                    .map(row -> new DashboardMetrics.TopRestaurant(
                            (Long) row[0],
                            (String) row[1],
                            (Double) row[2],
                            ((Long) row[3]).intValue(),
                            (Double) row[4]
                    ))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("خطا در دریافت برترین رستوران‌ها: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * برترین آیتم‌ها
     */
    public List<DashboardMetrics.TopItem> getTopItems(Session session, int limit) {
        try {
            String hql = "SELECT fi.id, fi.name, COUNT(oi), SUM(oi.totalPrice), fi.category " +
                        "FROM OrderItem oi JOIN oi.foodItem fi " +
                        "GROUP BY fi.id, fi.name, fi.category " +
                        "ORDER BY COUNT(oi) DESC";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setMaxResults(limit)
                    .getResultList();
            
            return results.stream()
                    .map(row -> new DashboardMetrics.TopItem(
                            (Long) row[0],
                            (String) row[1],
                            ((Long) row[2]).intValue(),
                            (Double) row[3],
                            (String) row[4]
                    ))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("خطا در دریافت برترین آیتم‌ها: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * برترین مشتریان
     */
    public List<DashboardMetrics.TopCustomer> getTopCustomers(Session session, int limit) {
        try {
            String hql = "SELECT u.id, u.fullName, SUM(o.totalAmount), COUNT(o) " +
                        "FROM Order o JOIN o.user u " +
                        "WHERE o.status = 'COMPLETED' " +
                        "GROUP BY u.id, u.fullName " +
                        "ORDER BY SUM(o.totalAmount) DESC";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setMaxResults(limit)
                    .getResultList();
            
            return results.stream()
                    .map(row -> new DashboardMetrics.TopCustomer(
                            (Long) row[0],
                            (String) row[1],
                            (Double) row[2],
                            ((Long) row[3]).intValue()
                    ))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("خطا در دریافت برترین مشتریان: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * درآمد بین دو تاریخ
     */
    public Double getRevenueBetween(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT SUM(o.totalAmount) FROM Order o " +
                        "WHERE o.status = 'COMPLETED' AND o.createdAt >= :start AND o.createdAt <= :end";
            
            Double revenue = session.createQuery(hql, Double.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();
                    
            return revenue != null ? revenue : 0.0;
        } catch (Exception e) {
            logger.error("خطا در دریافت درآمد بین تاریخ‌ها: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Bulk insert برای OrderAnalytics
     */
    public Integer bulkInsertOrderAnalytics(List<OrderAnalytics> analytics, Session session) {
        try {
            int inserted = 0;
            for (int i = 0; i < analytics.size(); i++) {
                session.save(analytics.get(i));
                inserted++;
                
                if (i % 100 == 0) { // Batch commit every 100 records
                    session.flush();
                    session.clear();
                }
            }
            return inserted;
        } catch (Exception e) {
            logger.error("خطا در bulk insert سفارشات: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Bulk insert برای UserAnalytics
     */
    public Integer bulkInsertUserAnalytics(List<UserAnalytics> analytics, Session session) {
        try {
            int inserted = 0;
            for (int i = 0; i < analytics.size(); i++) {
                session.save(analytics.get(i));
                inserted++;
                
                if (i % 100 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            return inserted;
        } catch (Exception e) {
            logger.error("خطا در bulk insert کاربران: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Bulk insert برای RestaurantAnalytics
     */
    public Integer bulkInsertRestaurantAnalytics(List<RestaurantAnalytics> analytics, Session session) {
        try {
            int inserted = 0;
            for (int i = 0; i < analytics.size(); i++) {
                session.save(analytics.get(i));
                inserted++;
                
                if (i % 100 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            return inserted;
        } catch (Exception e) {
            logger.error("خطا در bulk insert رستوران‌ها: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Bulk insert برای PaymentAnalytics
     */
    public Integer bulkInsertPaymentAnalytics(List<PaymentAnalytics> analytics, Session session) {
        try {
            int inserted = 0;
            for (int i = 0; i < analytics.size(); i++) {
                session.save(analytics.get(i));
                inserted++;
                
                if (i % 100 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            return inserted;
        } catch (Exception e) {
            logger.error("خطا در bulk insert پرداخت‌ها: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * دریافت تعداد کل درآمد بین تاریخ‌ها
     */
    public Double getTotalRevenueBetween(LocalDateTime start, LocalDateTime end, Session session) {
        return getRevenueBetween(start, end, session);
    }
    
    /**
     * Helper methods برای Customer Analytics
     */
    public Double getUserOrderFrequency(Long userId, int daysPeriod, Session session) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(daysPeriod);
            String hql = "SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.createdAt >= :since";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("userId", userId)
                    .setParameter("since", since)
                    .uniqueResult();
            return count != null ? count.doubleValue() : 0.0;
        } catch (Exception e) {
            logger.error("خطا در دریافت تناوب سفارش کاربر: {}", e.getMessage());
            return 0.0;
        }
    }
    
    public Double getUserAverageOrderValue(Long userId, int daysPeriod, Session session) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(daysPeriod);
            String hql = "SELECT AVG(o.totalAmount) FROM Order o WHERE o.user.id = :userId AND o.createdAt >= :since AND o.status = 'COMPLETED'";
            Double avg = session.createQuery(hql, Double.class)
                    .setParameter("userId", userId)
                    .setParameter("since", since)
                    .uniqueResult();
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه میانگین سفارش کاربر: {}", e.getMessage());
            return 0.0;
        }
    }
    
    public List<String> getUserFavoriteRestaurants(Long userId, int daysPeriod, Session session) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(daysPeriod);
            String hql = "SELECT r.name FROM Order o JOIN o.restaurant r WHERE o.user.id = :userId AND o.createdAt >= :since GROUP BY r.id, r.name ORDER BY COUNT(o) DESC";
            return session.createQuery(hql, String.class)
                    .setParameter("userId", userId)
                    .setParameter("since", since)
                    .setMaxResults(5)
                    .getResultList();
        } catch (Exception e) {
            logger.error("خطا در دریافت رستوران‌های مورد علاقه: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<String> getUserFavoriteItems(Long userId, int daysPeriod, Session session) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(daysPeriod);
            String hql = "SELECT fi.name FROM OrderItem oi JOIN oi.order o JOIN oi.foodItem fi WHERE o.user.id = :userId AND o.createdAt >= :since GROUP BY fi.id, fi.name ORDER BY COUNT(oi) DESC";
            return session.createQuery(hql, String.class)
                    .setParameter("userId", userId)
                    .setParameter("since", since)
                    .setMaxResults(5)
                    .getResultList();
        } catch (Exception e) {
            logger.error("خطا در دریافت آیتم‌های مورد علاقه: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public Map<String, Integer> getUserOrderTimePatterns(Long userId, int daysPeriod, Session session) {
        // Implementation برای الگوهای زمانی سفارش
        return new HashMap<>();
    }
    
    public List<String> getUserPeakOrderDays(Long userId, int daysPeriod, Session session) {
        // Implementation برای روزهای پیک سفارش
        return new ArrayList<>();
    }
    
    public Double getUserAverageRating(Long userId, int daysPeriod, Session session) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(daysPeriod);
            String hql = "SELECT AVG(r.rating) FROM Rating r WHERE r.user.id = :userId AND r.createdAt >= :since";
            Double avg = session.createQuery(hql, Double.class)
                    .setParameter("userId", userId)
                    .setParameter("since", since)
                    .uniqueResult();
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه میانگین امتیاز کاربر: {}", e.getMessage());
            return 0.0;
        }
    }
    
    public Double getUserComplaintRate(Long userId, int daysPeriod, Session session) {
        // Implementation برای نرخ شکایت کاربر
        return 0.0;
    }
    
    public Integer getDaysSinceLastOrder(Long userId, Session session) {
        try {
            String hql = "SELECT MAX(o.createdAt) FROM Order o WHERE o.user.id = :userId";
            LocalDateTime lastOrder = session.createQuery(hql, LocalDateTime.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
            
            if (lastOrder != null) {
                return (int) java.time.Duration.between(lastOrder, LocalDateTime.now()).toDays();
            }
            return Integer.MAX_VALUE;
        } catch (Exception e) {
            logger.error("خطا در محاسبه روزهای از آخرین سفارش: {}", e.getMessage());
            return 0;
        }
    }
    
    public Double getUserAverageOrderInterval(Long userId, Session session) {
        // Implementation برای میانگین فاصله سفارشات
        return 7.0; // Default: 7 days
    }
    
    public List<ItemRecommendation> getRecommendedItems(Long userId, Session session, int limit) {
        // پیاده‌سازی ساده برای recommendations
        List<ItemRecommendation> recommendations = new ArrayList<>();
        try {
            // در اینجا منطق پیچیده ML می‌تواند قرار گیرد
            String hql = "SELECT DISTINCT fi FROM FoodItem fi JOIN OrderItem oi ON fi.id = oi.foodItem.id " +
                        "WHERE oi.order.customer.id != :userId ORDER BY fi.name";
            
            List<FoodItem> items = session.createQuery(hql, FoodItem.class)
                    .setParameter("userId", userId)
                    .setMaxResults(limit)
                    .getResultList();
                    
            for (FoodItem item : items) {
                ItemRecommendation rec = new ItemRecommendation();
                rec.setUserId(userId);
                rec.setItemId(item.getId());
                rec.setItemName(item.getName());
                rec.setScore(0.8); // نمره فرضی
                rec.setReason("Based on popular items");
                recommendations.add(rec);
            }
            
        } catch (Exception e) {
            logger.error("خطا در دریافت توصیه‌ها: {}", e.getMessage());
        }
        return recommendations;
    }

    /**
     * دریافت درآمد بر اساس رستوران
     */
    public Map<String, Double> getRevenueByRestaurant(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT r.name, SUM(o.totalAmount) FROM Order o " +
                        "JOIN o.restaurant r WHERE o.status = 'COMPLETED' " +
                        "AND o.orderDate >= :start AND o.orderDate <= :end " +
                        "GROUP BY r.name ORDER BY SUM(o.totalAmount) DESC";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
            
            Map<String, Double> revenueMap = new LinkedHashMap<>();
            for (Object[] row : results) {
                String restaurantName = (String) row[0];
                Double revenue = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                revenueMap.put(restaurantName, revenue);
            }
            
            return revenueMap;
        } catch (Exception e) {
            logger.error("خطا در دریافت درآمد رستوران‌ها: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * دریافت درآمد بر اساس دسته‌بندی
     */
    public Map<String, Double> getRevenueByCategory(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT fi.category, SUM(oi.price * oi.quantity) FROM OrderItem oi " +
                        "JOIN oi.foodItem fi JOIN oi.order o WHERE o.status = 'COMPLETED' " +
                        "AND o.orderDate >= :start AND o.orderDate <= :end " +
                        "GROUP BY fi.category ORDER BY SUM(oi.price * oi.quantity) DESC";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
            
            Map<String, Double> categoryMap = new LinkedHashMap<>();
            for (Object[] row : results) {
                String category = (String) row[0];
                Double revenue = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                categoryMap.put(category != null ? category : "Other", revenue);
            }
            
            return categoryMap;
        } catch (Exception e) {
            logger.error("خطا در دریافت درآمد دسته‌بندی‌ها: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * دریافت درآمد روزانه
     */
    public Map<LocalDate, Double> getDailyRevenue(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT DATE(o.orderDate), SUM(o.totalAmount) FROM Order o " +
                        "WHERE o.status = 'COMPLETED' AND o.orderDate >= :start AND o.orderDate <= :end " +
                        "GROUP BY DATE(o.orderDate) ORDER BY DATE(o.orderDate)";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
            
            Map<LocalDate, Double> dailyMap = new LinkedHashMap<>();
            for (Object[] row : results) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                Double revenue = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                dailyMap.put(date, revenue);
            }
            
            return dailyMap;
        } catch (Exception e) {
            logger.error("خطا در دریافت درآمد روزانه: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * دریافت کل کمیسیون‌ها
     */
    public Double getTotalCommissions(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            // فرض می‌کنیم کمیسیون 10% از هر سفارش است
            Double totalRevenue = getRevenueBetween(start, end, session);
            return totalRevenue * 0.10; // 10% commission rate
        } catch (Exception e) {
            logger.error("خطا در محاسبه کمیسیون‌ها: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * دریافت هزینه‌های تحویل
     */
    public Double getDeliveryFees(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            // فرض می‌کنیم هزینه تحویل ثابت 20000 تومان برای هر سفارش است
            String hql = "SELECT COUNT(o) FROM Order o WHERE o.status = 'COMPLETED' " +
                        "AND o.orderDate >= :start AND o.orderDate <= :end";
            
            Long orderCount = session.createQuery(hql, Long.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();
            
            return orderCount != null ? orderCount * 20000.0 : 0.0; // 20000 per order
        } catch (Exception e) {
            logger.error("خطا در محاسبه هزینه‌های تحویل: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * دریافت مبلغ استرداد
     */
    public Double getRefunds(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'CANCELLED' " +
                        "AND o.orderDate >= :start AND o.orderDate <= :end";
            
            Double refunds = session.createQuery(hql, Double.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();
            
            return refunds != null ? refunds : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه استردادها: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * تفکیک روش‌های پرداخت
     */
    public Map<String, Long> getPaymentMethodsBreakdown(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT p.paymentMethod, COUNT(p) FROM Payment p " +
                        "WHERE p.status = 'COMPLETED' AND p.createdAt >= :start AND p.createdAt <= :end " +
                        "GROUP BY p.paymentMethod";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
            
            Map<String, Long> methodsMap = new HashMap<>();
            for (Object[] row : results) {
                String method = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                methodsMap.put(method != null ? method : "Unknown", count);
            }
            
            return methodsMap;
        } catch (Exception e) {
            logger.error("خطا در تفکیک روش‌های پرداخت: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * تعداد پرداخت‌های موفق
     */
    public Long getSuccessfulPaymentsCount(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' " +
                        "AND p.createdAt >= :start AND p.createdAt <= :end";
            
            return session.createQuery(hql, Long.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در شمارش پرداخت‌های موفق: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * تعداد پرداخت‌های ناموفق
     */
    public Long getFailedPaymentsCount(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT COUNT(p) FROM Payment p WHERE p.status = 'FAILED' " +
                        "AND p.createdAt >= :start AND p.createdAt <= :end";
            
            return session.createQuery(hql, Long.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در شمارش پرداخت‌های ناموفق: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * میانگین ارزش طول عمر مشتری
     */
    public Double getAverageCustomerLifetimeValue(Session session) {
        try {
            String hql = "SELECT AVG(customerTotal) FROM (" +
                        "SELECT SUM(o.totalAmount) as customerTotal FROM Order o " +
                        "WHERE o.status = 'COMPLETED' GROUP BY o.customer.id)";
            
            Double ltv = session.createQuery(hql, Double.class).uniqueResult();
            return ltv != null ? ltv : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه میانگین ارزش مشتری: {}", e.getMessage());
            return 0.0;
        }
    }
} 