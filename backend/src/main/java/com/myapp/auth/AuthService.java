package com.myapp.auth;

import com.myapp.auth.dto.RegisterRequest;
import com.myapp.auth.dto.UpdateProfileRequest;
import com.myapp.common.exceptions.InvalidCredentialsException;
import com.myapp.common.models.User;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.exceptions.DuplicatePhoneException;

import java.util.Objects;

public class AuthService {

    private final AuthRepository repository;

    public AuthService(AuthRepository repository) {
        this.repository = repository;
    }

    /**
     * Registers a new user and returns persisted copy.
     */
    public User register(RegisterRequest req) {
        Objects.requireNonNull(req, "request must not be null");
        User user = new User(null,
        req.getFullName(), req.getPhone(), req.getEmail(),
        req.getPasswordHash(), req.getRole(), req.getAddress());
        return repository.saveNew(user);
    }

    /**
     * Returns the user if phone & passwordHash match, otherwise throws InvalidCredentialsException.
     */
    public User login(String phone, String passwordHash) {
        return repository.findByPhone(phone)
                .filter(u -> u.getPasswordHash().equals(passwordHash))
                .orElseThrow(InvalidCredentialsException::new);
    }

    /**
     * Updates user profile (fullName, address, email). Will propagate DuplicatePhoneException on phone change.
     */
    public User updateProfile(User updated) throws DuplicatePhoneException {
        return repository.update(updated);
    }

    // ---------------------------------------------------------------------
    // Profile operations

    public User getProfile(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    public User updateProfile(long id, com.myapp.auth.dto.UpdateProfileRequest req) {
        User existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        if (req.getFullName() != null) existing.setFullName(req.getFullName());
        if (req.getEmail() != null) existing.setEmail(req.getEmail());
        if (req.getAddress() != null) existing.setAddress(req.getAddress());

        return repository.update(existing);
    }
    
    /**
     * Register a user directly (for HTTP API usage)
     */
    public User registerUser(User user) {
        return repository.saveNew(user);
    }
}
