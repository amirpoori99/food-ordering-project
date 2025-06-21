package com.myapp.auth;

import com.myapp.common.exceptions.DuplicatePhoneException;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthRepository Tests")
class AuthRepositoryTest {

    private AuthRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AuthRepository();
        // Clean database before each test
        repository.deleteAll();
    }

    @Nested
    @DisplayName("User Creation Tests")
    class UserCreationTests {
        
        @Test
        @DisplayName("User creation with valid data succeeds")
        void saveNew_validUser_success() {
            // Given
            User user = User.forRegistration("John Doe", "09123456789", "john@example.com", "hashedPassword", "Tehran");
            
            // When
            User saved = repository.saveNew(user);
            
            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getId()).isPositive();
            assertThat(saved.getFullName()).isEqualTo("John Doe");
            assertThat(saved.getPhone()).isEqualTo("09123456789");
            assertThat(saved.getEmail()).isEqualTo("john@example.com");
            assertThat(saved.getPasswordHash()).isEqualTo("hashedPassword");
            assertThat(saved.getAddress()).isEqualTo("Tehran");
            assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
        }

        @Test
        @DisplayName("User creation with minimal data succeeds")
        void saveNew_minimalUser_success() {
            // Given
            User user = User.forRegistration("Simple User", "09123456788", "", "hashedPassword", "");
            
            // When
            User saved = repository.saveNew(user);
            
            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getFullName()).isEqualTo("Simple User");
            assertThat(saved.getPhone()).isEqualTo("09123456788");
            assertThat(saved.getEmail()).isEqualTo("");
            assertThat(saved.getAddress()).isEqualTo("");
            assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
        }

        @EnumSource(User.Role.class)
        @ParameterizedTest
        @DisplayName("User creation with different roles succeeds")
        void saveNew_differentRoles_success(User.Role role) {
            // Given
            User user = User.forRegistration("Test User", "0912345678" + role.ordinal(), "test@example.com", "hashedPassword", "Tehran");
            user.setRole(role);
            
            // When
            User saved = repository.saveNew(user);
            
            // Then
            assertThat(saved.getRole()).isEqualTo(role);
        }

        @Test
        @DisplayName("User creation with duplicate phone throws exception")
        void saveNew_duplicatePhone_throwsException() {
            // Given
            String phone = "09123456784";
            User firstUser = User.forRegistration("First User", phone, "first@example.com", "hashedPassword1", "Tehran");
            User secondUser = User.forRegistration("Second User", phone, "second@example.com", "hashedPassword2", "Isfahan");
            
            repository.saveNew(firstUser);
            
            // When & Then
            assertThatThrownBy(() -> repository.saveNew(secondUser))
                    .isInstanceOf(DuplicatePhoneException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "09111111111", "09222222222", "09333333333", "09444444444", "09555555555"
        })
        @DisplayName("User creation with various phone formats succeeds")
        void saveNew_variousPhoneFormats_success(String phone) {
            // Given
            User user = User.forRegistration("Test User", phone, "test@example.com", "hashedPassword", "Tehran");
            
            // When
            User saved = repository.saveNew(user);
            
            // Then
            assertThat(saved.getPhone()).isEqualTo(phone);
        }
    }

    @Nested
    @DisplayName("User Retrieval Tests")
    class UserRetrievalTests {
        
        @Test
        @DisplayName("Find user by ID succeeds")
        void findById_existingUser_success() {
            // Given
            User user = User.forRegistration("John Doe", "09123456783", "john@example.com", "hashedPassword", "Tehran");
            User saved = repository.saveNew(user);
            
            // When
            Optional<User> found = repository.findById(saved.getId());
            
            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
            assertThat(found.get().getFullName()).isEqualTo("John Doe");
            assertThat(found.get().getPhone()).isEqualTo("09123456783");
        }

        @Test
        @DisplayName("Find user by non-existent ID returns empty")
        void findById_nonExistentUser_returnsEmpty() {
            // When
            Optional<User> found = repository.findById(999999L);
            
            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find user by phone succeeds")
        void findByPhone_existingUser_success() {
            // Given
            String phone = "09123456782";
            User user = User.forRegistration("Jane Doe", phone, "jane@example.com", "hashedPassword", "Tehran");
            repository.saveNew(user);
            
            // When
            Optional<User> found = repository.findByPhone(phone);
            
            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getPhone()).isEqualTo(phone);
            assertThat(found.get().getFullName()).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("Find user by non-existent phone returns empty")
        void findByPhone_nonExistentUser_returnsEmpty() {
            // When
            Optional<User> found = repository.findByPhone("09999999999");
            
            // Then
            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("User Update Tests")
    class UserUpdateTests {
        
        @Test
        @DisplayName("User update with valid data succeeds")
        void update_validUser_success() {
            // Given
            User user = User.forRegistration("John Doe", "09123456781", "john@example.com", "hashedPassword", "Tehran");
            User saved = repository.saveNew(user);
            
            // Modify user data
            saved.setFullName("John Smith");
            saved.setEmail("johnsmith@example.com");
            saved.setAddress("Isfahan");
            
            // When
            User updated = repository.update(saved);
            
            // Then
            assertThat(updated.getFullName()).isEqualTo("John Smith");
            assertThat(updated.getEmail()).isEqualTo("johnsmith@example.com");
            assertThat(updated.getAddress()).isEqualTo("Isfahan");
            assertThat(updated.getPhone()).isEqualTo("09123456781"); // Phone unchanged
        }

        @Test
        @DisplayName("User update with phone change to existing phone throws exception")
        void update_duplicatePhone_throwsException() {
            // Given
            User user1 = User.forRegistration("User One", "09123456780", "user1@example.com", "hashedPassword", "Tehran");
            User user2 = User.forRegistration("User Two", "09123456779", "user2@example.com", "hashedPassword", "Tehran");
            
            User saved1 = repository.saveNew(user1);
            User saved2 = repository.saveNew(user2);
            
            // Try to update user2's phone to user1's phone
            saved2.setPhone("09123456780");
            
            // When & Then
            assertThatThrownBy(() -> repository.update(saved2))
                    .isInstanceOf(DuplicatePhoneException.class);
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    class UserDeletionTests {
        
        @Test
        @DisplayName("User deletion succeeds")
        void delete_existingUser_success() {
            // Given
            User user = User.forRegistration("John Doe", "09123456778", "john@example.com", "hashedPassword", "Tehran");
            User saved = repository.saveNew(user);
            
            // When
            repository.delete(saved.getId());
            
            // Then
            Optional<User> found = repository.findById(saved.getId());
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("User deletion allows phone reuse")
        void delete_allowsPhoneReuse_success() {
            // Given
            String phone = "09123456777";
            User user1 = User.forRegistration("First User", phone, "first@example.com", "hashedPassword", "Tehran");
            User saved1 = repository.saveNew(user1);
            
            // When
            repository.delete(saved1.getId());
            
            // Then - should be able to create new user with same phone
            User user2 = User.forRegistration("Second User", phone, "second@example.com", "hashedPassword", "Isfahan");
            User saved2 = repository.saveNew(user2);
            
            assertThat(saved2.getPhone()).isEqualTo(phone);
            assertThat(saved2.getFullName()).isEqualTo("Second User");
        }
    }
}