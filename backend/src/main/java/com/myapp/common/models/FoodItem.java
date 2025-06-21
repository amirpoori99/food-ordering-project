package com.myapp.common.models;

import jakarta.persistence.*;

@Entity
@Table(name = "food_items")
public class FoodItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(length = 50)
    private String category;
    
    @Column(name = "image_url", length = 255)
    private String imageUrl;
    
    @Column(nullable = false)
    private Integer quantity = 0;
    
    @Column(nullable = false)
    private Boolean available = true;
    
    @Column(name = "keywords", length = 255)
    private String keywords;
    
    // Many food items belong to one restaurant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    /* ---------- CONSTRUCTORS ---------- */
    
    public FoodItem() {}
    
    /** Without id - for main code (Hibernate generates id itself) */
    public FoodItem(String name, String description, Double price, String category, 
                   String imageUrl, Integer quantity, String keywords, Restaurant restaurant) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.keywords = keywords;
        this.restaurant = restaurant;
        this.available = true;
    }
    
    /** With Long id - for tests that set initial id */
    public FoodItem(Long id, String name, String description, Double price, String category, 
                   String imageUrl, Integer quantity, String keywords, Restaurant restaurant) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.keywords = keywords;
        this.restaurant = restaurant;
        this.available = true;
    }
    
    /* ---------- STATIC FACTORY for easy creation ---------- */
    
    public static FoodItem forMenu(String name, String description, Double price, 
                                  String category, Restaurant restaurant) {
        if (name == null || name.trim().isEmpty() || price == null || price <= 0 || 
            category == null || category.trim().isEmpty() || restaurant == null) {
            throw new IllegalArgumentException("Name, price, category and restaurant are required");
        }
        return new FoodItem(name, description, price, category, null, 1, name, restaurant);
    }
    
    public static FoodItem forMenuWithImage(String name, String description, Double price, 
                                           String category, String imageUrl, Restaurant restaurant) {
        if (name == null || name.trim().isEmpty() || price == null || price <= 0 || 
            category == null || category.trim().isEmpty() || restaurant == null) {
            throw new IllegalArgumentException("Name, price, category and restaurant are required");
        }
        return new FoodItem(name, description, price, category, imageUrl, 1, name, restaurant);
    }
    
    /* ---------- GETTERS AND SETTERS ---------- */
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Boolean getAvailable() {
        return available;
    }
    
    public void setAvailable(Boolean available) {
        this.available = available;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    public Restaurant getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    
    /* ---------- BUSINESS METHODS ---------- */
    
    public boolean isInStock() {
        return available && quantity > 0;
    }
    
    public void decreaseQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
        } else {
            throw new IllegalArgumentException("Not enough quantity available");
        }
    }
    
    public void increaseQuantity(int amount) {
        if (amount > 0) {
            quantity += amount;
        }
    }
    
    /* ---------- UTILITY METHODS ---------- */
    
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return id != null && id.equals(foodItem.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
