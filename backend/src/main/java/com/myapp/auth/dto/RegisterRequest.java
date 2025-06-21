package com.myapp.auth.dto;

import com.myapp.common.models.User;

public class RegisterRequest {
    private String fullName;
    private String phone;
    private String email;
    private String passwordHash;
    private User.Role role;
    private String address;

    public RegisterRequest(String fullName, String phone, String email, String passwordHash, User.Role role, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.address = address;
    }

    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public User.Role getRole() { return role; }
    public String getAddress() { return address; }
} 