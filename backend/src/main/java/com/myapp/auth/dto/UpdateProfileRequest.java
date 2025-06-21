package com.myapp.auth.dto;

public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String address;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String fullName, String email, String address) {
        this.fullName = fullName;
        this.email = email;
        this.address = address;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
} 