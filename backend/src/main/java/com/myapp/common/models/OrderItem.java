package com.myapp.common.models;

import jakarta.persistence.*;

/**
 * مدل آیتم سفارش - نماینده آیتم‌های فردی درون یک سفارش
 * جدول اتصال (junction table) بین سفارش و آیتم غذایی همراه با تعداد و قیمت
 * این کلاس رابطه many-to-many بین Order و FoodItem را مدیریت می‌کند
 */
@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "price", nullable = false)
    private Double price; // Price at time of order (for historical accuracy)
    
    // Constructors
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
    
    // Business methods
    /**
     * محاسبه جمع جزء (قیمت × تعداد)
     * 
     * @return مبلغ کل این آیتم در سفارش
     */
    public Double getSubtotal() {
        return price * quantity;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public FoodItem getFoodItem() { return foodItem; }
    public void setFoodItem(FoodItem foodItem) { this.foodItem = foodItem; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
} 