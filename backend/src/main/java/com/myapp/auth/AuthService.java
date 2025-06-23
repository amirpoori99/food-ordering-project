package com.myapp.auth;

import com.myapp.auth.dto.RegisterRequest;
import com.myapp.auth.dto.UpdateProfileRequest;
import com.myapp.common.exceptions.InvalidCredentialsException;
import com.myapp.common.models.User;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.exceptions.DuplicatePhoneException;
import com.myapp.common.utils.JWTUtil;

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
     * Login user and generate JWT tokens
     * 
     * @param phone User phone
     * @param passwordHash Password hash
     * @return AuthResult with JWT tokens
     */
    public AuthResult loginWithTokens(String phone, String passwordHash) {
        try {
            User user = login(phone, passwordHash);
            String[] tokens = JWTUtil.generateTokenPair(user.getId(), user.getPhone(), user.getRole().toString());
            
            // Return AuthResult with both tokens
            return AuthResult.refreshed(user.getId(), user.getPhone(), user.getRole().toString(), tokens[0], tokens[1]);
        } catch (InvalidCredentialsException e) {
            return AuthResult.unauthenticated("Invalid phone or password");
        } catch (Exception e) {
            return AuthResult.unauthenticated("Login failed: " + e.getMessage());
        }
    }
    
    /**
     * Refresh access token using refresh token
     * 
     * @param refreshToken Refresh token
     * @return AuthResult with new tokens
     */
    public AuthResult refreshToken(String refreshToken) {
        return AuthMiddleware.refreshAccessToken(refreshToken, repository);
    }
    
    /**
     * Validate access token
     * 
     * @param accessToken Access token
     * @return AuthResult with user information
     */
    public AuthResult validateToken(String accessToken) {
        return AuthMiddleware.authenticateToken(accessToken);
    }
    
    /**
     * Logout user (invalidate token on client side)
     * Note: In a production system, you might want to maintain a blacklist of tokens
     * 
     * @param userId User ID
     * @return Success message
     */
    public String logout(Long userId) {
        // In a stateless JWT system, logout is typically handled client-side
        // by removing the token from storage
        return "Logged out successfully";
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
