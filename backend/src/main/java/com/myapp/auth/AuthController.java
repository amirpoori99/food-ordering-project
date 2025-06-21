package com.myapp.auth;

import com.myapp.auth.dto.RegisterRequest;
import com.myapp.auth.dto.ProfileResponse;
import com.myapp.auth.dto.UpdateProfileRequest;
import com.myapp.common.models.User;

/**
 * Thin façade over AuthService. In later phases this class will be annotated with
 * web-framework annotations (@RestController, @PostMapping, …). For now we keep
 * it framework-agnostic so that unit tests can focus on behaviour, not HTTP.
 */
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    public User register(RegisterRequest req) {
        return service.register(req);
    }

    public User login(String phone, String passwordHash) {
        return service.login(phone, passwordHash);
    }

    public ProfileResponse getProfile(long id) {
        return ProfileResponse.from(service.getProfile(id));
    }

    public ProfileResponse updateProfile(long id, UpdateProfileRequest req) {
        return ProfileResponse.from(service.updateProfile(id, req));
    }
}