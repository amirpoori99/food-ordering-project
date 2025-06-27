package com.myapp.common.models;

import jakarta.persistence.*;

/**
 * مدل آیتم سفارش - نماینده آیتم‌های فردی درون یک سفارش
 * 
 * این کلاس جدول اتصال (junction table) بین سفارش و آیتم غذایی است:
 * 
 * === ویژگی‌های کلیدی ===
 * - رابطه many-to-many بین Order و FoodItem را مدیریت می‌کند
 * - ذخیره تعداد هر آیتم در سفارش
 * - ذخیره قیمت در زمان سفارش (snapshot) برای تاریخچه دقیق
 * - محاسبه جمع جزء (قیمت × تعداد)
 * 
 * === قوانین کسب‌وکار ===
 * - قیمت در زمان سفارش ثبت می‌شود (تغییرات آینده قیمت تأثیری ندارد)
 * - تعداد باید مثبت باشد
 * - رابطه اجباری با سفارش و آیتم غذایی
 * 
 * === استفاده در سیستم ===
 * - محاسبه مبلغ کل سفارش
 * - نمایش جزئیات سفارش
 * - گزارش‌گیری فروش آیتم‌ها
 * - تاریخچه قیمت‌گذاری
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "order_items")
public class OrderItem {
    
    /** شناسه یکتای آیتم سفارش */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** سفارش مربوطه (رابطه چند‌به‌یک) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    /** آیتم غذایی (رابطه چند‌به‌یک) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;
    
    /** تعداد سفارش از این آیتم */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    /** قیمت در زمان سفارش (برای حفظ تاریخچه دقیق) */
    @Column(name = "price", nullable = false)
    private Double price;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * سازنده پیش‌فرض - برای JPA و فرمورک‌ها
     * JPA نیاز به سازنده بدون پارامتر دارد
     */
    public OrderItem() {}
    
    /**
     * سازنده اصلی - برای ایجاد آیتم سفارش جدید
     * 
     * @param order سفارش مربوطه
     * @param foodItem آیتم غذایی
     * @param quantity تعداد سفارش
     * @param price قیمت در زمان سفارش
     */
    public OrderItem(Order order, FoodItem foodItem, Integer quantity, Double price) {
        this.order = order;
        this.foodItem = foodItem;
        this.quantity = quantity;
        this.price = price;
    }
    
    // ==================== BUSINESS METHODS ====================
    
    /**
     * محاسبه جمع جزء (قیمت × تعداد)
     * 
     * @return مبلغ کل این آیتم در سفارش
     */
    public Double getSubtotal() {
        return price * quantity;
    }
    
    /**
     * بررسی اعتبار آیتم سفارش
     * 
     * @return true اگر تعداد و قیمت معتبر باشند
     */
    public boolean isValid() {
        return quantity != null && quantity > 0 && 
               price != null && price >= 0;
    }
    
    /**
     * محاسبه درصد این آیتم از کل سفارش
     * 
     * @param totalOrderAmount مبلغ کل سفارش
     * @return درصد این آیتم از کل
     */
    public Double getPercentageOfOrder(Double totalOrderAmount) {
        if (totalOrderAmount == null || totalOrderAmount == 0) {
            return 0.0;
        }
        return (getSubtotal() / totalOrderAmount) * 100.0;
    }
    
    // ==================== GETTERS AND SETTERS ====================
    
    /** @return شناسه آیتم سفارش */
    public Long getId() { return id; }
    
    /** @param id شناسه آیتم سفارش */
    public void setId(Long id) { this.id = id; }
    
    /** @return سفارش مربوطه */
    public Order getOrder() { return order; }
    
    /** @param order سفارش مربوطه */
    public void setOrder(Order order) { this.order = order; }
    
    /** @return آیتم غذایی */
    public FoodItem getFoodItem() { return foodItem; }
    
    /** @param foodItem آیتم غذایی */
    public void setFoodItem(FoodItem foodItem) { this.foodItem = foodItem; }
    
    /** @return تعداد سفارش */
    public Integer getQuantity() { return quantity; }
    
    /** @param quantity تعداد سفارش */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    /** @return قیمت در زمان سفارش */
    public Double getPrice() { return price; }
    
    /** @param price قیمت در زمان سفارش */
    public void setPrice(Double price) { this.price = price; }
    
    /**
     * نمایش رشته‌ای آیتم سفارش
     * 
     * @return اطلاعات کلیدی برای debugging
     */
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}