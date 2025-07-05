package com.myapp.analytics.repository;

import com.myapp.common.models.*;
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
                        "AND o.orderDate >= :start AND o.orderDate < :end";
            
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
            
            String hql = "SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :start AND o.orderDate < :end";
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
            String hql = "SELECT COUNT(DISTINCT o.customer.id) FROM Order o WHERE o.orderDate >= :date";
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
     * توجه: User مدل فیلد createdAt ندارد، بنابراین از orderDate اولین سفارش استفاده می‌کنیم
     */
    public Long getTodayNewUsersCount(Session session) {
        try {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            // کاربران جدید را بر اساس اولین سفارش آنها شناسایی می‌کنیم
            String hql = "SELECT COUNT(DISTINCT u.id) FROM User u " +
                        "WHERE u.id IN (SELECT DISTINCT o.customer.id FROM Order o " +
                        "WHERE o.orderDate >= :start AND o.orderDate < :end " +
                        "AND o.orderDate = (SELECT MIN(o2.orderDate) FROM Order o2 WHERE o2.customer.id = u.id))";
            
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
            String hql = "SELECT COUNT(r) FROM Restaurant r WHERE r.status = 'ACTIVE'";
            return session.createQuery(hql, Long.class).uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در دریافت رستوران‌های فعال: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * میانگین زمان تحویل
     */
    public Double getAverageDeliveryTime(Session session) {
        try {
            String hql = "SELECT AVG(TIMESTAMPDIFF(MINUTE, o.orderDate, o.actualDeliveryTime)) " +
                        "FROM Order o WHERE o.status = 'COMPLETED' AND o.actualDeliveryTime IS NOT NULL";
            Double avg = session.createQuery(hql, Double.class).uniqueResult();
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه میانگین زمان تحویل: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * نرخ تکمیل سفارشات
     */
    public Double getOrderCompletionRate(Session session) {
        try {
            String hql = "SELECT COUNT(o) FROM Order o WHERE o.status = 'COMPLETED'";
            Long completed = session.createQuery(hql, Long.class).uniqueResult();
            
            String hqlTotal = "SELECT COUNT(o) FROM Order o";
            Long total = session.createQuery(hqlTotal, Long.class).uniqueResult();
            
            if (total != null && total > 0) {
                return (completed.doubleValue() / total.doubleValue()) * 100;
            }
            return 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه نرخ تکمیل سفارشات: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * امتیاز رضایت مشتریان
     */
    public Double getCustomerSatisfactionScore(Session session) {
        try {
            String hql = "SELECT AVG(r.ratingScore) FROM Rating r WHERE r.ratingScore IS NOT NULL";
            Double avg = session.createQuery(hql, Double.class).uniqueResult();
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه امتیاز رضایت: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * دریافت درآمد بین دو تاریخ
     */
    public Double getRevenueBetween(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED' " +
                        "AND o.orderDate >= :start AND o.orderDate < :end";
            
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
     * دریافت کل درآمد بین دو تاریخ
     */
    public Double getTotalRevenueBetween(LocalDateTime start, LocalDateTime end, Session session) {
        return getRevenueBetween(start, end, session);
    }
    
    /**
     * محاسبه فراوانی سفارش کاربر
     */
    public Double getUserOrderFrequency(Long userId, int daysPeriod, Session session) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(daysPeriod);
            
            String hql = "SELECT COUNT(o) FROM Order o WHERE o.customer.id = :userId " +
                        "AND o.orderDate >= :startDate";
            
            Long orderCount = session.createQuery(hql, Long.class)
                    .setParameter("userId", userId)
                    .setParameter("startDate", startDate)
                    .uniqueResult();
                    
            return orderCount != null ? orderCount.doubleValue() / daysPeriod : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه فراوانی سفارش کاربر: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * محاسبه میانگین مبلغ سفارش کاربر
     */
    public Double getUserAverageOrderValue(Long userId, int daysPeriod, Session session) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(daysPeriod);
            
            String hql = "SELECT AVG(o.totalAmount) FROM Order o WHERE o.customer.id = :userId " +
                        "AND o.orderDate >= :startDate AND o.status = 'COMPLETED'";
            
            Double avg = session.createQuery(hql, Double.class)
                    .setParameter("userId", userId)
                    .setParameter("startDate", startDate)
                    .uniqueResult();
                    
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه میانگین مبلغ سفارش کاربر: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * دریافت رستوران‌های مورد علاقه کاربر
     */
    public List<String> getUserFavoriteRestaurants(Long userId, int daysPeriod, Session session) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(daysPeriod);
            
            String hql = "SELECT r.name, COUNT(o) as orderCount FROM Order o " +
                        "JOIN o.restaurant r WHERE o.customer.id = :userId " +
                        "AND o.orderDate >= :startDate GROUP BY r.name " +
                        "ORDER BY orderCount DESC";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("userId", userId)
                    .setParameter("startDate", startDate)
                    .setMaxResults(5)
                    .list();
                    
            return results.stream()
                    .map(row -> (String) row[0])
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("خطا در دریافت رستوران‌های مورد علاقه: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * دریافت آیتم‌های مورد علاقه کاربر
     */
    public List<String> getUserFavoriteItems(Long userId, int daysPeriod, Session session) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(daysPeriod);
            
            String hql = "SELECT i.name, COUNT(oi) as itemCount FROM OrderItem oi " +
                        "JOIN oi.foodItem i JOIN oi.order o WHERE o.customer.id = :userId " +
                        "AND o.orderDate >= :startDate GROUP BY i.name " +
                        "ORDER BY itemCount DESC";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("userId", userId)
                    .setParameter("startDate", startDate)
                    .setMaxResults(5)
                    .list();
                    
            return results.stream()
                    .map(row -> (String) row[0])
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("خطا در دریافت آیتم‌های مورد علاقه: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * دریافت الگوهای زمانی سفارش کاربر
     */
    public Map<String, Integer> getUserOrderTimePatterns(Long userId, int daysPeriod, Session session) {
        // موقتاً Map خالی برمی‌گردانیم
        return new HashMap<>();
    }
    
    /**
     * دریافت روزهای پیک سفارش کاربر
     */
    public List<String> getUserPeakOrderDays(Long userId, int daysPeriod, Session session) {
        // موقتاً لیست خالی برمی‌گردانیم
        return new ArrayList<>();
    }
    
    /**
     * محاسبه میانگین امتیاز کاربر
     */
    public Double getUserAverageRating(Long userId, int daysPeriod, Session session) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(daysPeriod);
            
            String hql = "SELECT AVG(r.ratingScore) FROM Rating r WHERE r.user.id = :userId " +
                        "AND r.createdAt >= :startDate AND r.ratingScore IS NOT NULL";
            
            Double avg = session.createQuery(hql, Double.class)
                    .setParameter("userId", userId)
                    .setParameter("startDate", startDate)
                    .uniqueResult();
                    
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه میانگین امتیاز کاربر: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * محاسبه نرخ شکایت کاربر
     */
    public Double getUserComplaintRate(Long userId, int daysPeriod, Session session) {
        // موقتاً 0.0 برمی‌گردانیم
        return 0.0;
    }
    
    /**
     * محاسبه روزهای گذشته از آخرین سفارش
     */
    public Integer getDaysSinceLastOrder(Long userId, Session session) {
        try {
            String hql = "SELECT MAX(o.orderDate) FROM Order o WHERE o.customer.id = :userId";
            LocalDateTime lastOrder = session.createQuery(hql, LocalDateTime.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
                    
            if (lastOrder != null) {
                return (int) java.time.Duration.between(lastOrder, LocalDateTime.now()).toDays();
            }
            return 0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه روزهای گذشته از آخرین سفارش: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * محاسبه میانگین فاصله زمانی سفارشات کاربر
     */
    public Double getUserAverageOrderInterval(Long userId, Session session) {
        try {
            String hql = "SELECT o.orderDate FROM Order o WHERE o.customer.id = :userId " +
                        "ORDER BY o.orderDate";
            
            List<LocalDateTime> orderDates = session.createQuery(hql, LocalDateTime.class)
                    .setParameter("userId", userId)
                    .list();
                    
            if (orderDates.size() < 2) {
                return 0.0;
            }
            
            double totalDays = 0;
            for (int i = 1; i < orderDates.size(); i++) {
                totalDays += java.time.Duration.between(orderDates.get(i-1), orderDates.get(i)).toDays();
            }
            
            return totalDays / (orderDates.size() - 1);
        } catch (Exception e) {
            logger.error("خطا در محاسبه میانگین فاصله زمانی سفارشات: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * دریافت درآمد بر اساس رستوران
     */
    public Map<String, Double> getRevenueByRestaurant(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT r.name, SUM(o.totalAmount) FROM Order o " +
                        "JOIN o.restaurant r WHERE o.status = 'COMPLETED' " +
                        "AND o.createdAt >= :start AND o.createdAt < :end " +
                        "GROUP BY r.name ORDER BY SUM(o.totalAmount) DESC";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .list();
                    
            Map<String, Double> revenueMap = new HashMap<>();
            for (Object[] row : results) {
                revenueMap.put((String) row[0], (Double) row[1]);
            }
            
            return revenueMap;
        } catch (Exception e) {
            logger.error("خطا در دریافت درآمد بر اساس رستوران: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * دریافت درآمد بر اساس دسته‌بندی
     */
    public Map<String, Double> getRevenueByCategory(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT c.name, SUM(oi.price * oi.quantity) FROM OrderItem oi " +
                        "JOIN oi.item i JOIN i.category c JOIN oi.order o " +
                        "WHERE o.status = 'COMPLETED' " +
                        "AND o.createdAt >= :start AND o.createdAt < :end " +
                        "GROUP BY c.name ORDER BY SUM(oi.price * oi.quantity) DESC";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .list();
                    
            Map<String, Double> revenueMap = new HashMap<>();
            for (Object[] row : results) {
                revenueMap.put((String) row[0], (Double) row[1]);
            }
            
            return revenueMap;
        } catch (Exception e) {
            logger.error("خطا در دریافت درآمد بر اساس دسته‌بندی: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * دریافت درآمد روزانه
     */
    public Map<LocalDate, Double> getDailyRevenue(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT DATE(o.createdAt), SUM(o.totalAmount) FROM Order o " +
                        "WHERE o.status = 'COMPLETED' " +
                        "AND o.createdAt >= :start AND o.createdAt < :end " +
                        "GROUP BY DATE(o.createdAt) ORDER BY DATE(o.createdAt)";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .list();
                    
            Map<LocalDate, Double> revenueMap = new HashMap<>();
            for (Object[] row : results) {
                revenueMap.put((LocalDate) row[0], (Double) row[1]);
            }
            
            return revenueMap;
        } catch (Exception e) {
            logger.error("خطا در دریافت درآمد روزانه: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * محاسبه کل کمیسیون‌ها
     */
    public Double getTotalCommissions(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT SUM(o.commission) FROM Order o WHERE o.status = 'COMPLETED' " +
                        "AND o.createdAt >= :start AND o.createdAt < :end";
            
            Double commission = session.createQuery(hql, Double.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();
                    
            return commission != null ? commission : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه کل کمیسیون‌ها: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * محاسبه هزینه‌های تحویل
     */
    public Double getDeliveryFees(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT SUM(o.deliveryFee) FROM Order o WHERE o.status = 'COMPLETED' " +
                        "AND o.createdAt >= :start AND o.createdAt < :end";
            
            Double fees = session.createQuery(hql, Double.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();
                    
            return fees != null ? fees : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه هزینه‌های تحویل: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * محاسبه کل بازپرداخت‌ها
     */
    public Double getRefunds(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT SUM(o.refundAmount) FROM Order o WHERE o.status = 'REFUNDED' " +
                        "AND o.createdAt >= :start AND o.createdAt < :end";
            
            Double refunds = session.createQuery(hql, Double.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();
                    
            return refunds != null ? refunds : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه کل بازپرداخت‌ها: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * دریافت آمار روش‌های پرداخت
     */
    public Map<String, Long> getPaymentMethodsBreakdown(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT p.paymentMethod, COUNT(p) FROM Payment p " +
                        "WHERE p.status = 'SUCCESS' " +
                        "AND p.createdAt >= :start AND p.createdAt < :end " +
                        "GROUP BY p.paymentMethod";
            
            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .list();
                    
            Map<String, Long> breakdown = new HashMap<>();
            for (Object[] row : results) {
                breakdown.put((String) row[0], (Long) row[1]);
            }
            
            return breakdown;
        } catch (Exception e) {
            logger.error("خطا در دریافت آمار روش‌های پرداخت: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * تعداد پرداخت‌های موفق
     */
    public Long getSuccessfulPaymentsCount(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS' " +
                        "AND p.createdAt >= :start AND p.createdAt < :end";
            
            return session.createQuery(hql, Long.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در دریافت تعداد پرداخت‌های موفق: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * تعداد پرداخت‌های ناموفق
     */
    public Long getFailedPaymentsCount(LocalDateTime start, LocalDateTime end, Session session) {
        try {
            String hql = "SELECT COUNT(p) FROM Payment p WHERE p.status = 'FAILED' " +
                        "AND p.createdAt >= :start AND p.createdAt < :end";
            
            return session.createQuery(hql, Long.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("خطا در دریافت تعداد پرداخت‌های ناموفق: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * محاسبه میانگین ارزش طول عمر مشتری
     */
    public Double getAverageCustomerLifetimeValue(Session session) {
        try {
            String hql = "SELECT AVG(totalSpent) FROM (" +
                        "SELECT u.id, SUM(o.totalAmount) as totalSpent FROM User u " +
                        "JOIN u.orders o WHERE o.status = 'COMPLETED' " +
                        "GROUP BY u.id)";
            
            Double avg = session.createQuery(hql, Double.class).uniqueResult();
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("خطا در محاسبه میانگین ارزش طول عمر مشتری: {}", e.getMessage());
            return 0.0;
        }
    }
} 