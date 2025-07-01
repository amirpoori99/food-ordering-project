package com.myapp.common.models;

import jakarta.persistence.*;

/**
 * مدل آیتم غذایی - نماینده یک غذا یا نوشیدنی در منوی رستوران
 * این کلاس اطلاعات کاملی از آیتم‌های غذایی شامل قیمت، موجودی، تصویر و دسته‌بندی را نگهداری می‌کند
 * با استفاده از JPA برای ذخیره‌سازی در دیتابیس تنظیم شده است
 */
@Entity                         // نشان‌دهنده entity در JPA
@Table(name = "food_items")    // نام جدول در دیتابیس
public class FoodItem {
    
    @Id                                                    // کلید اصلی
    @GeneratedValue(strategy = GenerationType.AUTO)   // Auto-increment برای شناسه
    private Long id;                                       // شناسه یکتای آیتم غذایی
    
    @Column(nullable = false, length = 100)  // نام آیتم - اجباری، حداکثر 100 کاراکتر
    private String name;                     // نام آیتم غذایی
    
    @Column(length = 500)                    // توضیحات - اختیاری، حداکثر 500 کاراکتر
    private String description;              // توضیحات و معرفی آیتم غذایی
    
    @Column(nullable = false)                // قیمت - اجباری
    private Double price;                    // قیمت آیتم غذایی (به تومان)
    
    @Column(length = 50)                     // دسته‌بندی - اختیاری، حداکثر 50 کاراکتر
    private String category;                 // دسته‌بندی آیتم (مانند: پیتزا، برگر، نوشیدنی)
    
    @Column(name = "image_url", length = 255)  // URL تصویر - اختیاری، حداکثر 255 کاراکتر
    private String imageUrl;                   // آدرس تصویر آیتم غذایی
    
    @Column(nullable = false)                // موجودی - اجباری
    private Integer quantity = 0;            // موجودی فعلی آیتم (پیش‌فرض: 0)
    
    @Column(nullable = false)                // وضعیت در دسترس بودن - اجباری
    private Boolean available = true;        // آیا آیتم در دسترس است؟ (پیش‌فرض: بله)
    
    @Column(name = "keywords", length = 255)  // کلمات کلیدی - اختیاری، حداکثر 255 کاراکتر
    private String keywords;                  // کلمات کلیدی برای جستجو
    
    // رابطه many-to-one: چندین آیتم غذایی متعلق به یک رستوران
    @ManyToOne(fetch = FetchType.LAZY)                      // lazy loading برای بهینه‌سازی
    @JoinColumn(name = "restaurant_id", nullable = false)  // کلید خارجی - اجباری
    private Restaurant restaurant;                          // رستوران صاحب این آیتم
    
    /* ---------- سازنده‌های کلاس ---------- */
    
    /**
     * سازنده پیش‌فرض - برای JPA و فرمورک‌ها
     * JPA نیاز به سازنده بدون پارامتر دارد
     */
    public FoodItem() {}
    
    /**
     * سازنده اصلی بدون id - برای کد اصلی برنامه
     * Hibernate خودش id را تولید می‌کند
     * 
     * @param name نام آیتم غذایی
     * @param description توضیحات آیتم
     * @param price قیمت آیتم
     * @param category دسته‌بندی آیتم
     * @param imageUrl آدرس تصویر آیتم
     * @param quantity موجودی اولیه
     * @param keywords کلمات کلیدی
     * @param restaurant رستوران صاحب آیتم
     */
    public FoodItem(String name, String description, Double price, String category, 
                   String imageUrl, Integer quantity, String keywords, Restaurant restaurant) {
        this.name = name;               // تنظیم نام آیتم
        this.description = description; // تنظیم توضیحات
        this.price = price;             // تنظیم قیمت
        this.category = category;       // تنظیم دسته‌بندی
        this.imageUrl = imageUrl;       // تنظیم آدرس تصویر
        this.quantity = quantity;       // تنظیم موجودی
        this.keywords = keywords;       // تنظیم کلمات کلیدی
        this.restaurant = restaurant;   // تنظیم رستوران
        this.available = true;          // پیش‌فرض: در دسترس
    }
    
    /**
     * سازنده با Long id - برای تست‌هایی که id اولیه را تنظیم می‌کنند
     * معمولاً در تست‌های واحد استفاده می‌شود
     */
    public FoodItem(Long id, String name, String description, Double price, String category, 
                   String imageUrl, Integer quantity, String keywords, Restaurant restaurant) {
        this.id = id;                   // تنظیم دستی شناسه
        this.name = name;               // تنظیم نام آیتم
        this.description = description; // تنظیم توضیحات
        this.price = price;             // تنظیم قیمت
        this.category = category;       // تنظیم دسته‌بندی
        this.imageUrl = imageUrl;       // تنظیم آدرس تصویر
        this.quantity = quantity;       // تنظیم موجودی
        this.keywords = keywords;       // تنظیم کلمات کلیدی
        this.restaurant = restaurant;   // تنظیم رستوران
        this.available = true;          // پیش‌فرض: در دسترس
    }
    
    /* ---------- متدهای Factory Static ---------- */
    
    /**
     * متد Factory برای ایجاد آیتم منو ساده
     * این متد برای ساده‌سازی ایجاد آیتم‌های غذایی بدون تصویر استفاده می‌شود
     * 
     * @param name نام آیتم غذایی
     * @param description توضیحات آیتم
     * @param price قیمت آیتم
     * @param category دسته‌بندی آیتم
     * @param restaurant رستوران صاحب آیتم
     * @return آیتم غذایی جدید
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     */
    public static FoodItem forMenu(String name, String description, Double price, 
                                  String category, Restaurant restaurant) {
        // اعتبارسنجی ورودی‌های ضروری
        if (name == null || name.trim().isEmpty() || price == null || price <= 0 || 
            category == null || category.trim().isEmpty() || restaurant == null) {
            throw new IllegalArgumentException("Name, price, category and restaurant are required");
        }
        // ایجاد آیتم با مقادیر پیش‌فرض (بدون تصویر، موجودی 1، کلمات کلیدی همان نام)
        return new FoodItem(name, description, price, category, null, 1, name, restaurant);
    }
    
    /**
     * متد Factory برای ایجاد آیتم منو با تصویر
     * این متد برای ایجاد آیتم‌های غذایی همراه با تصویر استفاده می‌شود
     * 
     * @param name نام آیتم غذایی
     * @param description توضیحات آیتم
     * @param price قیمت آیتم
     * @param category دسته‌بندی آیتم
     * @param imageUrl آدرس تصویر آیتم
     * @param restaurant رستوران صاحب آیتم
     * @return آیتم غذایی جدید با تصویر
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     */
    public static FoodItem forMenuWithImage(String name, String description, Double price, 
                                           String category, String imageUrl, Restaurant restaurant) {
        // اعتبارسنجی ورودی‌های ضروری
        if (name == null || name.trim().isEmpty() || price == null || price <= 0 || 
            category == null || category.trim().isEmpty() || restaurant == null) {
            throw new IllegalArgumentException("Name, price, category and restaurant are required");
        }
        // ایجاد آیتم با تصویر
        return new FoodItem(name, description, price, category, imageUrl, 1, name, restaurant);
    }
    
    /* ---------- Getter و Setter ها ---------- */
    
    // دریافت و تنظیم شناسه آیتم غذایی
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    // دریافت و تنظیم نام آیتم غذایی
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    // دریافت و تنظیم توضیحات آیتم غذایی
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // دریافت و تنظیم قیمت آیتم غذایی
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    // دریافت و تنظیم دسته‌بندی آیتم غذایی
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    // دریافت و تنظیم آدرس تصویر آیتم غذایی
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    // دریافت و تنظیم موجودی آیتم غذایی
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    // دریافت و تنظیم وضعیت در دسترس بودن آیتم
    public Boolean getAvailable() {
        return available;
    }
    
    public void setAvailable(Boolean available) {
        this.available = available;
    }
    
    // دریافت و تنظیم کلمات کلیدی آیتم
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    // دریافت و تنظیم رستوران صاحب آیتم
    public Restaurant getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    
    /* ---------- متدهای منطق کسب‌وکار ---------- */
    
    /**
     * بررسی موجود بودن آیتم در انبار
     * آیتم در صورتی موجود است که هم فعال باشد و هم موجودی داشته باشد
     * 
     * @return true اگر آیتم موجود و قابل سفارش باشد
     */
    public boolean isInStock() {
        return available && quantity > 0;
    }
    
    /**
     * کاهش موجودی آیتم (هنگام ثبت سفارش)
     * 
     * @param amount مقدار کاهش موجودی
     * @throws IllegalArgumentException در صورت عدم موجودی کافی
     */
    public void decreaseQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;  // کاهش موجودی
        } else {
            throw new IllegalArgumentException("Not enough quantity available");
        }
    }
    
    /**
     * افزایش موجودی آیتم (هنگام لغو سفارش یا تأمین مجدد)
     * 
     * @param amount مقدار افزایش موجودی
     */
    public void increaseQuantity(int amount) {
        if (amount > 0) {
            quantity += amount;  // افزایش موجودی
        }
    }
    
    /* ---------- متدهای کمکی (Utility Methods) ---------- */
    
    /**
     * نمایش رشته‌ای از آیتم غذایی
     * برای debugging و logging استفاده می‌شود
     * 
     * @return نمایش رشته‌ای از آیتم
     */
    @Override
    public String toString() {
        return "FoodItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", available=" + available +
                ", quantity=" + quantity +
                '}';
    }
    
    /**
     * بررسی تساوی دو آیتم غذایی
     * دو آیتم در صورت داشتن همان id برابر هستند
     * 
     * @param o شیء دیگر برای مقایسه
     * @return true اگر دو آیتم برابر باشند
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                              // همان شیء
        if (o == null || getClass() != o.getClass()) return false; // نوع متفاوت
        FoodItem foodItem = (FoodItem) o;
        return id != null && id.equals(foodItem.id);           // مقایسه بر اساس id
    }
    
    /**
     * محاسبه hash code برای آیتم غذایی
     * برای استفاده در HashMap و HashSet
     * 
     * @return hash code آیتم
     */
    @Override
    public int hashCode() {
        return getClass().hashCode(); // استفاده از hash code کلاس
    }
}
