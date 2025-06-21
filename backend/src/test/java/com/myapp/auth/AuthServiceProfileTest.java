package com.myapp.auth;

import com.myapp.auth.dto.UpdateProfileRequest;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthService Profile Management Tests")
class AuthServiceProfileTest {
    private AuthService service;
    private AuthRepository repo;

    @BeforeEach
    void setUp() {
        repo = new AuthRepository();
        service = new AuthService(repo);
        // Clean database before each test
        repo.deleteAll();
    }

    @Nested
    @DisplayName("Profile Retrieval Tests")
    class ProfileRetrievalTests {

        @Test
        @DisplayName("Get profile for non-existent user throws NotFoundException")
        void getProfile_notFound_throwsNotFoundException() {
            assertThatThrownBy(() -> service.getProfile(999))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Get profile for existing user returns correct data")
        void getProfile_existingUser_returnsCorrectData() {
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Test User", "09123456789", "test@example.com", "hash", User.Role.BUYER, "Test Address"));
            
            User profile = service.getProfile(saved.getId());
            
            assertThat(profile.getId()).isEqualTo(saved.getId());
            assertThat(profile.getFullName()).isEqualTo("Test User");
            assertThat(profile.getPhone()).isEqualTo("09123456789");
            assertThat(profile.getEmail()).isEqualTo("test@example.com");
            assertThat(profile.getAddress()).isEqualTo("Test Address");
            assertThat(profile.getRole()).isEqualTo(User.Role.BUYER);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -999L, Long.MIN_VALUE})
        @DisplayName("Get profile with invalid user IDs throws NotFoundException")
        void getProfile_invalidUserIds_throwsNotFoundException(long invalidId) {
            assertThatThrownBy(() -> service.getProfile(invalidId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    @Nested
    @DisplayName("Profile Update Tests")
    class ProfileUpdateTests {

        @Test
        @DisplayName("Update profile with valid data succeeds")
        void updateProfile_validData_success() {
            // create user
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Ali", "09123330000", "ali@example.com", "hash", User.Role.BUYER, "Original Address"));
            long id = saved.getId();

            UpdateProfileRequest req = new UpdateProfileRequest("Ali Edited", "ali-edited@example.com", "New Address");
            User updated = service.updateProfile(id, req);

            assertThat(updated.getFullName()).isEqualTo("Ali Edited");
            assertThat(updated.getEmail()).isEqualTo("ali-edited@example.com");
            assertThat(updated.getAddress()).isEqualTo("New Address");
            assertThat(updated.getPhone()).isEqualTo("09123330000"); // Phone should remain unchanged
            assertThat(updated.getId()).isEqualTo(id); // ID should remain unchanged

            // fetch again to verify persistence
            User fetched = service.getProfile(id);
            assertThat(fetched.getFullName()).isEqualTo("Ali Edited");
            assertThat(fetched.getEmail()).isEqualTo("ali-edited@example.com");
            assertThat(fetched.getAddress()).isEqualTo("New Address");
        }

        @Test
        @DisplayName("Update profile for non-existent user throws NotFoundException")
        void updateProfile_nonExistentUser_throwsNotFoundException() {
            UpdateProfileRequest req = new UpdateProfileRequest("Non Existent", "nonexistent@example.com", "Non Existent Address");
            
            assertThatThrownBy(() -> service.updateProfile(999999L, req))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User");
        }

        @Test
        @DisplayName("Update profile with minimal data succeeds")
        void updateProfile_minimalData_success() {
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Minimal User", "09123330001", "minimal@example.com", "hash", User.Role.BUYER, "Original Address"));
            
            UpdateProfileRequest req = new UpdateProfileRequest("Updated Minimal", "", "");
            User updated = service.updateProfile(saved.getId(), req);
            
            assertThat(updated.getFullName()).isEqualTo("Updated Minimal");
            assertThat(updated.getEmail()).isEqualTo("");
            assertThat(updated.getAddress()).isEqualTo("");
        }

        @Test
        @DisplayName("Update profile preserves sensitive data")
        void updateProfile_preservesSensitiveData_success() {
            String originalPassword = "originalPassword123";
            User.Role originalRole = User.Role.SELLER;
            String originalPhone = "09123330003";
            
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Preserve Test", originalPhone, "preserve@example.com", originalPassword, originalRole, "Original Address"));
            
            UpdateProfileRequest req = new UpdateProfileRequest("Updated Preserve Test", "updated-preserve@example.com", "Updated Address");
            User updated = service.updateProfile(saved.getId(), req);
            
            // Verify sensitive data is preserved
            assertThat(updated.getPasswordHash()).isEqualTo(originalPassword);
            assertThat(updated.getRole()).isEqualTo(originalRole);
            assertThat(updated.getPhone()).isEqualTo(originalPhone);
            assertThat(updated.getId()).isEqualTo(saved.getId());
            
            // Verify updated data
            assertThat(updated.getFullName()).isEqualTo("Updated Preserve Test");
            assertThat(updated.getEmail()).isEqualTo("updated-preserve@example.com");
            assertThat(updated.getAddress()).isEqualTo("Updated Address");
        }
    }

    @Nested
    @DisplayName("Profile Integration Tests")
    class ProfileIntegrationTests {

        @Test
        @DisplayName("Profile operations work correctly after login")
        void profileOperations_afterLogin_workCorrectly() {
            // Register user
            User registered = service.register(new com.myapp.auth.dto.RegisterRequest("Integration User", "09123330007", "integration@example.com", "password123", User.Role.BUYER, "Integration Address"));
            
            // Login
            User loggedIn = service.login("09123330007", "password123");
            assertThat(loggedIn.getId()).isEqualTo(registered.getId());
            
            // Get profile
            User profile = service.getProfile(loggedIn.getId());
            assertThat(profile.getFullName()).isEqualTo("Integration User");
            
            // Update profile
            UpdateProfileRequest updateReq = new UpdateProfileRequest("Updated Integration User", "updated-integration@example.com", "Updated Integration Address");
            User updated = service.updateProfile(profile.getId(), updateReq);
            
            assertThat(updated.getFullName()).isEqualTo("Updated Integration User");
            assertThat(updated.getEmail()).isEqualTo("updated-integration@example.com");
            
            // Login still works after profile update
            User loginAfterUpdate = service.login("09123330007", "password123");
            assertThat(loginAfterUpdate.getFullName()).isEqualTo("Updated Integration User");
        }
    }

    // Legacy tests for backward compatibility
    @Test
    void getProfile_notFound() {
        assertThatThrownBy(() -> service.getProfile(999)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateProfile_flow() {
        // create user
        User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Ali", "09123330000", null, "hash", User.Role.BUYER, "Addr"));
        long id = saved.getId();

        UpdateProfileRequest req = new UpdateProfileRequest("Ali Edited", "ali@ex.com", "New Addr");
        User updated = service.updateProfile(id, req);

        assertThat(updated.getFullName()).isEqualTo("Ali Edited");
        assertThat(updated.getEmail()).isEqualTo("ali@ex.com");
        assertThat(updated.getAddress()).isEqualTo("New Addr");

        // fetch again
        User fetched = service.getProfile(id);
        assertThat(fetched.getFullName()).isEqualTo("Ali Edited");
    }
} 