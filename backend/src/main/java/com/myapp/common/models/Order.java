package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * مدل سفارش - نماینده یک سفارش مشتری
 * این کلاس جزئیات سفارش، آیتم‌ها، وضعیت و روابط با کاربر و رستوران را شامل می‌شود
 * یکی از پیچیده‌ترین مدل‌های سیستم با منطق کسب‌وکار قوی
 */
@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;
    
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;
    
    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;
    
    @Column(name = "phone", nullable = false, length = 15)
    private String phone;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;
    
    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;
    
    // Constructors
    /**
     * سازنده پیش‌فرض - برای JPA و فرمورک‌ها
     * JPA نیاز به سازنده بدون پارامتر دارد
     */
    public Order() {}
    
    /**
     * سازنده خصوصی - برای ایجاد سفارش جدید با اعتبارسنجی
     * این سازنده توسط factory method استفاده می‌شود
     * 
     * @param customer مشتری سفارش‌دهنده
     * @param restaurant رستوران
     * @param deliveryAddress آدرس تحویل
     * @param phone شماره تلفن تماس
     */
    private Order(User customer, Restaurant restaurant, String deliveryAddress, String phone) {
        // اعتبارسنجی ورودی‌ها
        if (customer == null || restaurant == null) {
            throw new IllegalArgumentException("Customer and restaurant are required");
        }
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address is required");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        
        // تنظیم مقادیر اولیه
        this.customer = customer;
        this.restaurant = restaurant;
        this.deliveryAddress = deliveryAddress;
        this.phone = phone;
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.totalAmount = 0.0;
    }
    
    // Factory methods
    /**
     * متد Factory برای ایجاد سفارش جدید
     * این متد روش امن و کنترل شده‌ای برای ایجاد سفارش ارائه می‌دهد
     * 
     * @param customer مشتری سفارش‌دهنده
     * @param restaurant رستوران
     * @param deliveryAddress آدرس تحویل
     * @param phone شماره تلفن تماس
     * @return سفارش جدید ایجاد شده
     */
    public static Order createNew(User customer, Restaurant restaurant, String deliveryAddress, String phone) {
        return new Order(customer, restaurant, deliveryAddress, phone);
    }
    
    // Business methods
    /**
     * اضافه کردن آیتم به سفارش
     * اگر آیتم قبلاً موجود باشد، تعداد آن افزایش می‌یابد
     * 
     * @param foodItem آیتم غذایی
     * @param quantity تعداد درخواستی
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها یا عدم موجودی کافی
     */
    public void addItem(FoodItem foodItem, Integer quantity) {
        // اعتبارسنجی ورودی‌ها
        if (foodItem == null || quantity <= 0) {
            throw new IllegalArgumentException("Food item and quantity must be valid");
        }
        
        // بررسی موجودی
        if (!foodItem.isInStock() || foodItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough quantity available");
        }
        
        // بررسی وجود آیتم در سفارش
        OrderItem existingItem = findOrderItemByFoodItem(foodItem);
        if (existingItem != null) {
            // به‌روزرسانی تعداد آیتم موجود
            int newQuantity = existingItem.getQuantity() + quantity;
            if (foodItem.getQuantity() < newQuantity) {
                throw new IllegalArgumentException("Not enough quantity available");
            }
            existingItem.setQuantity(newQuantity);  // افزایش تعداد
        } else {
            // اضافه کردن آیتم جدید
            OrderItem orderItem = new OrderItem(this, foodItem, quantity, foodItem.getPrice());
            orderItems.add(orderItem);
        }
        recalculateTotal(); // محاسبه مجدد مبلغ کل
    }
    
    /**
     * حذف آیتم از سفارش
     * 
     * @param orderItem آیتم سفارش برای حذف
     */
    public void removeItem(OrderItem orderItem) {
        if (orderItems.remove(orderItem)) {  // حذف آیتم از لیست
            recalculateTotal(); // محاسبه مجدد مبلغ کل
        }
    }
    
    /**
     * حذف آیتم از سفارش بر اساس آیتم غذایی
     * 
     * @param foodItem آیتم غذایی برای حذف
     */
    public void removeItem(FoodItem foodItem) {
        OrderItem orderItem = findOrderItemByFoodItem(foodItem);
        if (orderItem != null) {
            removeItem(orderItem);  // فراخوانی متد حذف اصلی
        }
    }
    
    /**
     * تنظیم تعداد مشخص برای یک آیتم (بدون اضافه کردن)
     * 
     * @param foodItem آیتم غذایی
     * @param quantity تعداد جدید
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها یا عدم موجودی کافی
     */
    public void setItemQuantity(FoodItem foodItem, Integer quantity) {
        // اعتبارسنجی ورودی‌ها
        if (foodItem == null || quantity <= 0) {
            throw new IllegalArgumentException("Food item and quantity must be valid");
        }
        
        // بررسی موجودی
        if (!foodItem.isInStock() || foodItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough quantity available");
        }
        
        OrderItem existingItem = findOrderItemByFoodItem(foodItem);
        if (existingItem != null) {
            // تنظیم تعداد مستقیم (نه اضافه کردن)
            existingItem.setQuantity(quantity);
        } else {
            // اضافه کردن آیتم جدید
            OrderItem orderItem = new OrderItem(this, foodItem, quantity, foodItem.getPrice());
            orderItems.add(orderItem);
        }
        recalculateTotal(); // محاسبه مجدد مبلغ کل
    }
    
    /**
     * پیدا کردن آیتم سفارش بر اساس آیتم غذایی
     * متد کمکی برای جستجو در لیست آیتم‌ها
     * 
     * @param foodItem آیتم غذایی برای جستجو
     * @return آیتم سفارش یافت شده یا null
     */
    private OrderItem findOrderItemByFoodItem(FoodItem foodItem) {
        return orderItems.stream()
                .filter(item -> item.getFoodItem().equals(foodItem))  // فیلتر بر اساس آیتم غذایی
                .findFirst()                                          // یافتن اولین مورد
                .orElse(null);                                        // برگرداندن null در صورت عدم وجود
    }
    
    /**
     * محاسبه مجدد مبلغ کل سفارش
     * این متد بعد از هر تغییر در آیتم‌ها فراخوانی می‌شود
     */
    public void recalculateTotal() {
        this.totalAmount = orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())  // محاسبه قیمت هر آیتم
                .sum();                                                     // جمع کل
    }
    
    /**
     * تأیید سفارش
     * تغییر وضعیت به تأیید شده و کاهش موجودی آیتم‌ها
     * 
     * @throws IllegalStateException در صورت خالی بودن سفارش
     */
    public void confirm() {
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Cannot confirm empty order");
        }
        this.status = OrderStatus.CONFIRMED;                              // تغییر وضعیت به تأیید شده
        this.estimatedDeliveryTime = LocalDateTime.now().plusMinutes(30); // تنظیم زمان تحویل تخمینی (30 دقیقه)
        
        // کاهش موجودی آیتم‌های غذایی
        for (OrderItem item : orderItems) {
            item.getFoodItem().decreaseQuantity(item.getQuantity());
        }
    }
    
    /**
     * لغو سفارش
     * بازگرداندن موجودی آیتم‌ها در صورت لزوم
     * 
     * @throws IllegalStateException در صورت عدم امکان لغو سفارش
     */
    public void cancel() {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order in current status: " + status);
        }
        
        // بازگرداندن موجودی آیتم‌ها اگر سفارش تأیید شده بود
        if (status == OrderStatus.CONFIRMED || status == OrderStatus.PREPARING) {
            for (OrderItem item : orderItems) {
                item.getFoodItem().increaseQuantity(item.getQuantity());
            }
        }
        
        this.status = OrderStatus.CANCELLED; // تغییر وضعیت به لغو شده
    }
    
    /**
     * علامت‌گذاری سفارش به عنوان تحویل داده شده
     * 
     * @throws IllegalStateException در صورت عدم امکان تحویل سفارش
     */
    public void markAsDelivered() {
        if (status != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException("Order must be out for delivery to mark as delivered");
        }
        this.status = OrderStatus.DELIVERED;              // تغییر وضعیت به تحویل داده شده
        this.actualDeliveryTime = LocalDateTime.now();   // ثبت زمان واقعی تحویل
    }
    
    /**
     * بررسی امکان لغو سفارش
     * 
     * @return true اگر سفارش قابل لغو باشد
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || 
               status == OrderStatus.CONFIRMED || 
               status == OrderStatus.PREPARING;
    }
    
    /**
     * محاسبه تعداد کل آیتم‌های سفارش
     * 
     * @return مجموع تعداد تمام آیتم‌ها
     */
    public int getTotalItems() {
        return orderItems.stream().mapToInt(OrderItem::getQuantity).sum();
    }
    
    // Getters and setters
    /**
     * دریافت و تنظیم شناسه سفارش
     * 
     * @return شناسه یکتای سفارش
     */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    /**
     * دریافت و تنظیم مشتری سفارش
     * 
     * @return مشتری سفارش‌دهنده
     */
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    
    /**
     * دریافت و تنظیم رستوران سفارش
     * 
     * @return رستوران سفارش
     */
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    
    /**
     * دریافت و تنظیم لیست آیتم‌های سفارش
     * 
     * @return لیست آیتم‌های سفارش
     */
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    
    /**
     * دریافت و تنظیم مبلغ کل سفارش
     * 
     * @return مبلغ کل سفارش
     */
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    /**
     * دریافت و تنظیم وضعیت سفارش
     * 
     * @return وضعیت فعلی سفارش
     */
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    /**
     * دریافت و تنظیم تاریخ سفارش
     * 
     * @return تاریخ و زمان ثبت سفارش
     */
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    /**
     * دریافت و تنظیم آدرس تحویل
     * 
     * @return آدرس تحویل سفارش
     */
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    /**
     * دریافت و تنظیم شماره تلفن تماس
     * 
     * @return شماره تلفن تماس
     */
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    /**
     * دریافت و تنظیم یادداشت سفارش
     * 
     * @return یادداشت اضافی مشتری
     */
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    /**
     * دریافت و تنظیم زمان تخمینی تحویل
     * 
     * @return زمان تخمینی تحویل سفارش
     */
    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { 
        this.estimatedDeliveryTime = estimatedDeliveryTime; 
    }
    
    /**
     * دریافت و تنظیم زمان واقعی تحویل
     * 
     * @return زمان واقعی تحویل سفارش
     */
    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { 
        this.actualDeliveryTime = actualDeliveryTime; 
    }
}
