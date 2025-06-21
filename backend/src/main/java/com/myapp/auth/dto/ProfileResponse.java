package com.myapp.auth.dto;

import com.myapp.common.models.User;

public record ProfileResponse(long id, String fullName, String phone, String email, User.Role role, String address) {
    public static ProfileResponse from(User u) {
        return new ProfileResponse(u.getId(), u.getFullName(), u.getPhone(), u.getEmail(), u.getRole(), u.getAddress());
    }
} 