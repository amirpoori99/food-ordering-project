package com.myapp.auth;

import com.myapp.auth.dto.RegisterRequest;
import com.myapp.auth.dto.UpdateProfileRequest;
import com.myapp.common.exceptions.DuplicatePhoneException;
import com.myapp.common.exceptions.InvalidCredentialsException;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthService Comprehensive Tests")
class AuthServiceTest {

    private AuthService service;
    private AuthRepository repo;

    @BeforeEach
    void setUp() {
        repo = new AuthRepository();
        service = new AuthService(repo);
        // Clean database before each test
        repo.deleteAll();
    }

    private RegisterRequest req(String phone) {
        return new RegisterRequest("Test User", phone, "t@test.com", "hash", User.Role.BUYER, "Address");
    }

    private RegisterRequest req(String fullName, String phone, String email, String passwordHash, User.Role role, String address) {
        return new RegisterRequest(fullName, phone, email, passwordHash, role, address);
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Valid registration succeeds")
        void register_validData_success() {
            RegisterRequest request = req("Ahmad Mohammadi", "09123456789", "ahmad@example.com", "secureHash123", User.Role.BUYER, "Tehran, Iran");
            
            User saved = service.register(request);
            
            assertThat(saved.getId()).isNotNull().isPositive();
            assertThat(saved.getFullName()).isEqualTo("Ahmad Mohammadi");
            assertThat(saved.getPhone()).isEqualTo("09123456789");
            assertThat(saved.getEmail()).isEqualTo("ahmad@example.com");
            assertThat(saved.getPasswordHash()).isEqualTo("secureHash123");
            assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
            assertThat(saved.getAddress()).isEqualTo("Tehran, Iran");
        }

        @Test
        @DisplayName("Registration with minimal data succeeds")
        void register_minimalData_success() {
            RegisterRequest request = req("Simple User", "09123456788", "", "hash", User.Role.BUYER, "");
            
            User saved = service.register(request);
            
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getFullName()).isEqualTo("Simple User");
            assertThat(saved.getPhone()).isEqualTo("09123456788");
            assertThat(saved.getEmail()).isEqualTo("");
            assertThat(saved.getAddress()).isEqualTo("");
        }

        @EnumSource(User.Role.class)
        @ParameterizedTest
        @DisplayName("Registration with different roles succeeds")
        void register_differentRoles_success(User.Role role) {
            String phone = "0912345678" + role.ordinal();
            RegisterRequest request = req("Test User", phone, "test@example.com", "hash", role, "Address");
            
            User saved = service.register(request);
            
            assertThat(saved.getRole()).isEqualTo(role);
        }

        @Test
        @DisplayName("Registration with duplicate phone throws exception")
        void register_duplicatePhone_throwsException() {
            String phone = "09123456787";
            service.register(req(phone));
            
            assertThatThrownBy(() -> service.register(req(phone)))
                    .isInstanceOf(DuplicatePhoneException.class);
        }

        @Test
        @DisplayName("Registration with null request throws exception")
        void register_nullRequest_throwsException() {
            assertThatThrownBy(() -> service.register(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("request must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "09111111111", "09222222222", "09333333333", "09444444444", "09555555555"
        })
        @DisplayName("Registration with various valid phone numbers succeeds")
        void register_variousPhoneNumbers_success(String phone) {
            RegisterRequest request = req("User " + phone, phone, "user@example.com", "hash", User.Role.BUYER, "Address");
            
            User saved = service.register(request);
            
            assertThat(saved.getPhone()).isEqualTo(phone);
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Login with correct credentials succeeds")
        void login_correctCredentials_success() {
            String phone = "09123456786";
            String password = "securePassword123";
            service.register(req("Test User", phone, "test@example.com", password, User.Role.BUYER, "Address"));
            
            User loggedIn = service.login(phone, password);
            
            assertThat(loggedIn.getPhone()).isEqualTo(phone);
            assertThat(loggedIn.getPasswordHash()).isEqualTo(password);
            assertThat(loggedIn.getFullName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("Login with wrong password throws exception")
        void login_wrongPassword_throwsException() {
            String phone = "09123456785";
            String correctPassword = "correctPassword";
            String wrongPassword = "wrongPassword";
            
            service.register(req("Test User", phone, "test@example.com", correctPassword, User.Role.BUYER, "Address"));
            
            assertThatThrownBy(() -> service.login(phone, wrongPassword))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("Login with non-existent phone throws exception")
        void login_nonExistentPhone_throwsException() {
            assertThatThrownBy(() -> service.login("09999999999", "anyPassword"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("Login is case sensitive for password")
        void login_caseSensitivePassword_throwsException() {
            String phone = "09123456783";
            String password = "CaseSensitivePassword";
            service.register(req("Test User", phone, "test@example.com", password, User.Role.BUYER, "Address"));
            
            assertThatThrownBy(() -> service.login(phone, "casesensitivepassword"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("Profile Management Tests")
    class ProfileManagementTests {

        @Test
        @DisplayName("Get profile for existing user succeeds")
        void getProfile_existingUser_success() {
            User registered = service.register(req("Test User", "09123456781", "test@example.com", "password", User.Role.BUYER, "Address"));
            
            User profile = service.getProfile(registered.getId());
            
            assertThat(profile.getId()).isEqualTo(registered.getId());
            assertThat(profile.getFullName()).isEqualTo("Test User");
            assertThat(profile.getPhone()).isEqualTo("09123456781");
            assertThat(profile.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Get profile for non-existent user throws exception")
        void getProfile_nonExistentUser_throwsException() {
            assertThatThrownBy(() -> service.getProfile(999999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User");
        }

        @Test
        @DisplayName("Update profile with valid data succeeds")
        void updateProfile_validData_success() {
            User registered = service.register(req("Original Name", "09123456780", "original@example.com", "password", User.Role.BUYER, "Original Address"));
            
            UpdateProfileRequest updateRequest = new UpdateProfileRequest("Updated Name", "updated@example.com", "Updated Address");
            User updated = service.updateProfile(registered.getId(), updateRequest);
            
            assertThat(updated.getFullName()).isEqualTo("Updated Name");
            assertThat(updated.getEmail()).isEqualTo("updated@example.com");
            assertThat(updated.getAddress()).isEqualTo("Updated Address");
            assertThat(updated.getPhone()).isEqualTo("09123456780"); // Phone should remain unchanged
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("User registration and login flow works end-to-end")
        void registrationLoginFlow_endToEnd_success() {
            // Registration
            RegisterRequest registerRequest = req("End-to-End User", "09123456777", "e2e@example.com", "e2ePassword", User.Role.BUYER, "E2E Address");
            User registered = service.register(registerRequest);
            
            assertThat(registered.getId()).isNotNull();
            
            // Login
            User loggedIn = service.login("09123456777", "e2ePassword");
            
            assertThat(loggedIn.getId()).isEqualTo(registered.getId());
            assertThat(loggedIn.getFullName()).isEqualTo("End-to-End User");
            
            // Profile retrieval
            User profile = service.getProfile(registered.getId());
            
            assertThat(profile.getId()).isEqualTo(registered.getId());
            assertThat(profile.getFullName()).isEqualTo("End-to-End User");
        }

        @Test
        @DisplayName("Service handles concurrent registrations correctly")
        void concurrentRegistrations_handledCorrectly() {
            RegisterRequest req1 = req("User 1", "09111111111", "user1@example.com", "password1", User.Role.BUYER, "Address1");
            RegisterRequest req2 = req("User 2", "09222222222", "user2@example.com", "password2", User.Role.SELLER, "Address2");
            RegisterRequest req3 = req("User 3", "09333333333", "user3@example.com", "password3", User.Role.COURIER, "Address3");
            
            User user1 = service.register(req1);
            User user2 = service.register(req2);
            User user3 = service.register(req3);
            
            assertThat(user1.getId()).isNotEqualTo(user2.getId());
            assertThat(user2.getId()).isNotEqualTo(user3.getId());
            assertThat(user1.getId()).isNotEqualTo(user3.getId());
            
            assertThat(service.login("09111111111", "password1").getId()).isEqualTo(user1.getId());
            assertThat(service.login("09222222222", "password2").getId()).isEqualTo(user2.getId());
            assertThat(service.login("09333333333", "password3").getId()).isEqualTo(user3.getId());
        }
    }

    // Legacy tests for backward compatibility
    @Test
    void register_ok() {
        User saved = service.register(req("09120000001"));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isPositive();
    }

    @Test
    void register_duplicatePhone() {
        service.register(req("09120000002"));
        assertThatThrownBy(() -> service.register(req("09120000002")))
                .isInstanceOf(DuplicatePhoneException.class);
    }

    @Test
    void login_success() {
        service.register(req("09120000003"));
        User logged = service.login("09120000003", "hash");
        assertThat(logged.getPhone()).isEqualTo("09120000003");
    }

    @Test
    void login_badPassword() {
        service.register(req("09120000004"));
        assertThatThrownBy(() -> service.login("09120000004", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}