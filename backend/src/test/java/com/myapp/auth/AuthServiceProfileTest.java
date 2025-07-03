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
            assertThat(updated.getPasswordHash()).contains(":");
            assertThat(updated.getRole()).isEqualTo(originalRole);
            assertThat(updated.getPhone()).isEqualTo(originalPhone);
            assertThat(updated.getId()).isEqualTo(saved.getId());
            
            // Verify updated data
            assertThat(updated.getFullName()).isEqualTo("Updated Preserve Test");
            assertThat(updated.getEmail()).isEqualTo("updated-preserve@example.com");
            assertThat(updated.getAddress()).isEqualTo("Updated Address");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Update profile with null/empty fullName")
        void updateProfile_nullEmptyFullName_handledCorrectly(String fullName) {
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Original Name", "09123330010", "test@example.com", "hash", User.Role.BUYER, "Address"));
            
            UpdateProfileRequest req = new UpdateProfileRequest(fullName, "updated@example.com", "Updated Address");
            User updated = service.updateProfile(saved.getId(), req);
            
            // System behavior: null values don't update the field, empty strings do
            if (fullName == null) {
                // Null values don't change existing field
                assertThat(updated.getFullName()).isEqualTo("Original Name");
            } else {
                // Empty strings update the field
                assertThat(updated.getFullName()).isEqualTo(fullName);
            }
            assertThat(updated.getEmail()).isEqualTo("updated@example.com");
            assertThat(updated.getAddress()).isEqualTo("Updated Address");
        }

        @Test
        @DisplayName("Update profile with null request throws exception")
        void updateProfile_nullRequest_throwsException() {
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Test User", "09123330011", "test@example.com", "hash", User.Role.BUYER, "Address"));
            
            assertThatThrownBy(() -> service.updateProfile(saved.getId(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Update profile with Persian characters")
        void updateProfile_persianCharacters_success() {
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("علی محمدی", "09123330012", "ali@example.com", "hash", User.Role.BUYER, "تهران"));
            
            UpdateProfileRequest req = new UpdateProfileRequest("علی احمدی", "ali.ahmadi@example.com", "اصفهان، خیابان چهارباغ");
            User updated = service.updateProfile(saved.getId(), req);
            
            assertThat(updated.getFullName()).isEqualTo("علی احمدی");
            assertThat(updated.getEmail()).isEqualTo("ali.ahmadi@example.com");
            assertThat(updated.getAddress()).isEqualTo("اصفهان، خیابان چهارباغ");
        }

        @Test
        @DisplayName("Update profile with very long data")
        void updateProfile_veryLongData_success() {
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Short Name", "09123330013", "short@example.com", "hash", User.Role.BUYER, "Short Address"));
            
            String longName = "A".repeat(200);
            String longEmail = "very.long.email.address.that.might.cause.problems@very-long-domain-name-that-should-be-tested.com";
            String longAddress = "Very Long Address ".repeat(20);
            
            UpdateProfileRequest req = new UpdateProfileRequest(longName, longEmail, longAddress);
            User updated = service.updateProfile(saved.getId(), req);
            
            assertThat(updated.getFullName()).isEqualTo(longName);
            assertThat(updated.getEmail()).isEqualTo(longEmail);
            assertThat(updated.getAddress()).isEqualTo(longAddress);
        }

        @Test
        @DisplayName("Update profile with special characters")
        void updateProfile_specialCharacters_success() {
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Normal Name", "09123330014", "normal@example.com", "hash", User.Role.BUYER, "Normal Address"));
            
            String nameWithSpecialChars = "Ali@Mohammad#Hassan$Hussein%^&*()";
            String emailWithSpecialChars = "test+user.name@sub-domain.example-site.com";
            String addressWithSpecialChars = "Tehran, St. 1st, Alley 2nd, No. 3/4-5";
            
            UpdateProfileRequest req = new UpdateProfileRequest(nameWithSpecialChars, emailWithSpecialChars, addressWithSpecialChars);
            User updated = service.updateProfile(saved.getId(), req);
            
            assertThat(updated.getFullName()).isEqualTo(nameWithSpecialChars);
            assertThat(updated.getEmail()).isEqualTo(emailWithSpecialChars);
            assertThat(updated.getAddress()).isEqualTo(addressWithSpecialChars);
        }

        @Test
        @DisplayName("Update profile multiple times")
        void updateProfile_multipleTimes_success() {
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Original User", "09123330015", "original@example.com", "hash", User.Role.BUYER, "Original Address"));
            long userId = saved.getId();
            
            // First update
            UpdateProfileRequest req1 = new UpdateProfileRequest("First Update", "first@example.com", "First Address");
            User updated1 = service.updateProfile(userId, req1);
            assertThat(updated1.getFullName()).isEqualTo("First Update");
            
            // Second update
            UpdateProfileRequest req2 = new UpdateProfileRequest("Second Update", "second@example.com", "Second Address");
            User updated2 = service.updateProfile(userId, req2);
            assertThat(updated2.getFullName()).isEqualTo("Second Update");
            assertThat(updated2.getEmail()).isEqualTo("second@example.com");
            assertThat(updated2.getAddress()).isEqualTo("Second Address");
            
            // Third update - partial
            UpdateProfileRequest req3 = new UpdateProfileRequest("Final Update", "", "");
            User updated3 = service.updateProfile(userId, req3);
            assertThat(updated3.getFullName()).isEqualTo("Final Update");
            assertThat(updated3.getEmail()).isEqualTo("");
            assertThat(updated3.getAddress()).isEqualTo("");
            
            // Verify final state
            User fetched = service.getProfile(userId);
            assertThat(fetched.getFullName()).isEqualTo("Final Update");
            assertThat(fetched.getEmail()).isEqualTo("");
            assertThat(fetched.getAddress()).isEqualTo("");
            assertThat(fetched.getPhone()).isEqualTo("09123330015"); // Should remain unchanged
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -999L, Long.MIN_VALUE})
        @DisplayName("Update profile with invalid user IDs throws NotFoundException")
        void updateProfile_invalidUserIds_throwsNotFoundException(long invalidId) {
            UpdateProfileRequest req = new UpdateProfileRequest("Test Name", "test@example.com", "Test Address");
            
            assertThatThrownBy(() -> service.updateProfile(invalidId, req))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    @Nested
    @DisplayName("Profile Edge Cases")
    class ProfileEdgeCases {

        @Test
        @DisplayName("Get profile after multiple updates")
        void getProfile_afterMultipleUpdates_returnsLatestData() {
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Original", "09123330020", "original@example.com", "hash", User.Role.BUYER, "Original"));
            long userId = saved.getId();
            
            // Multiple updates
            service.updateProfile(userId, new UpdateProfileRequest("Update 1", "update1@example.com", "Address 1"));
            service.updateProfile(userId, new UpdateProfileRequest("Update 2", "update2@example.com", "Address 2"));
            service.updateProfile(userId, new UpdateProfileRequest("Final Update", "final@example.com", "Final Address"));
            
            User profile = service.getProfile(userId);
            assertThat(profile.getFullName()).isEqualTo("Final Update");
            assertThat(profile.getEmail()).isEqualTo("final@example.com");
            assertThat(profile.getAddress()).isEqualTo("Final Address");
        }

        @Test
        @DisplayName("Profile operations with concurrent updates")
        void profileOperations_concurrentUpdates_handledCorrectly() {
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Concurrent User", "09123330021", "concurrent@example.com", "hash", User.Role.BUYER, "Concurrent Address"));
            long userId = saved.getId();
            
            // Simulate concurrent updates (in real scenario, these would be in different threads)
            UpdateProfileRequest req1 = new UpdateProfileRequest("Concurrent Update 1", "concurrent1@example.com", "Address 1");
            UpdateProfileRequest req2 = new UpdateProfileRequest("Concurrent Update 2", "concurrent2@example.com", "Address 2");
            
            User updated1 = service.updateProfile(userId, req1);
            User updated2 = service.updateProfile(userId, req2);
            
            // Last update should win
            assertThat(updated2.getFullName()).isEqualTo("Concurrent Update 2");
            assertThat(updated2.getEmail()).isEqualTo("concurrent2@example.com");
            assertThat(updated2.getAddress()).isEqualTo("Address 2");
            
            // Verify persistence
            User fetched = service.getProfile(userId);
            assertThat(fetched.getFullName()).isEqualTo("Concurrent Update 2");
        }

        @Test
        @DisplayName("Profile data consistency across operations")
        void profileData_consistencyAcrossOperations_maintained() {
            String originalPhone = "09123330022";
            String originalPassword = "securePassword123";
            User.Role originalRole = User.Role.SELLER;
            
            User saved = service.register(new com.myapp.auth.dto.RegisterRequest("Consistency Test", originalPhone, "consistency@example.com", originalPassword, originalRole, "Original Address"));
            long userId = saved.getId();
            
            // Multiple profile operations
            User profile1 = service.getProfile(userId);
            service.updateProfile(userId, new UpdateProfileRequest("Updated Name", "updated@example.com", "Updated Address"));
            User profile2 = service.getProfile(userId);
            service.updateProfile(userId, new UpdateProfileRequest("Final Name", "final@example.com", "Final Address"));
            User profile3 = service.getProfile(userId);
            
            // Verify consistency of immutable fields across all operations
            assertThat(profile1.getId()).isEqualTo(profile2.getId()).isEqualTo(profile3.getId());
            assertThat(profile1.getPhone()).isEqualTo(profile2.getPhone()).isEqualTo(profile3.getPhone()).isEqualTo(originalPhone);
            assertThat(profile1.getPasswordHash()).contains(":").isEqualTo(profile2.getPasswordHash()).isEqualTo(profile3.getPasswordHash());
            assertThat(profile1.getRole()).isEqualTo(profile2.getRole()).isEqualTo(profile3.getRole()).isEqualTo(originalRole);
            
            // Verify mutable fields are updated correctly
            assertThat(profile3.getFullName()).isEqualTo("Final Name");
            assertThat(profile3.getEmail()).isEqualTo("final@example.com");
            assertThat(profile3.getAddress()).isEqualTo("Final Address");
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