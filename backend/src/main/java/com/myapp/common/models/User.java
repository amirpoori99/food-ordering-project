package com.myapp.common.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    /* ---------- FIELDS ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String phone;

    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String address;

    public enum Role { BUYER, SELLER, COURIER, ADMIN }

    /* ---------- CONSTRUCTORS ---------- */
    /** Without id - for main code (Hibernate generates id itself) */
    public User(String fullName,
                String phone,
                String email,
                String passwordHash,
                Role role,
                String address) {
        this.fullName     = fullName;
        this.phone        = phone;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.address      = address;
    }

    /** با Long id - برای تست‌هایی که id را صراحتاً می‌دهند */
    public User(Long id,
                String fullName,
                String phone,
                String email,
                String passwordHash,
                Role role,
                String address) {
        this(fullName, phone, email, passwordHash, role, address);
        this.id = id;
    }

    /** سازندهٔ سازگاری قدیمی (int id) تا تست‌های قبلی بدون تغییر سبز شوند */
    public User(int id,
                String fullName,
                String phone,
                String email,
                String passwordHash,
                Role role,
                String address) {
        this((long) id, fullName, phone, email, passwordHash, role, address);
    }

    public User() { }

    /* ---------- GETTERS / SETTERS ---------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    /* ---------- FACTORY METHODS ---------- */
    /**
     * Factory method for user registration - creates User with BUYER role
     */
    public static User forRegistration(String fullName, String phone, String email, String passwordHash, String address) {
        return new User(fullName, phone, email, passwordHash, Role.BUYER, address);
    }
}