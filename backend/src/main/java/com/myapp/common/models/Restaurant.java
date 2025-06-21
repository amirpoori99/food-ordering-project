package com.myapp.common.models;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    /* ---------- FIELDS ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;        // FK → users.id

    @Column(nullable = false)
    private String name;

    private String address;
    private String phone;

    @Enumerated(EnumType.STRING)
    private RestaurantStatus status = RestaurantStatus.PENDING;

    /* ---------- CONSTRUCTORS ---------- */
    public Restaurant() { }

    /** Without id - for creating new objects in business logic */
    public Restaurant(Long ownerId,
                      String name,
                      String address,
                      String phone) {
        this.ownerId = ownerId;
        this.name    = name;
        this.address = address;
        this.phone   = phone;
    }

    /** With Long id - for tests that set initial id */
    public Restaurant(Long id,
                      Long ownerId,
                      String name,
                      String address,
                      String phone,
                      RestaurantStatus status) {
        this(ownerId, name, address, phone);
        this.id     = id;
        this.status = status;
    }

    /** Legacy compatibility constructor (int id / ownerId) */
    public Restaurant(int id,
                      long ownerId,
                      String name,
                      String address,
                      String phone,
                      RestaurantStatus status) {
        this((long) id, ownerId, name, address, phone, status);
    }

    /* ---------- STATIC FACTORY for legacy tests ---------- */
    public static Restaurant forRegistration(Long ownerId,
                                             String name,
                                             String address,
                                             String phone) {
        return new Restaurant(ownerId, name, address, phone);
    }

    /* ---------- GETTERS / SETTERS ---------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public RestaurantStatus getStatus() { return status; }
    public void setStatus(RestaurantStatus status) { this.status = status; }
}