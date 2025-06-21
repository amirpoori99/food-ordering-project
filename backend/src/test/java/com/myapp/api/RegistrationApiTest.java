package com.myapp.api;

import com.myapp.auth.AuthRepository;
import com.myapp.auth.AuthService;
import com.myapp.common.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Test Registration functionality with comprehensive scenarios
 * Following phase 1 test patterns and standards
 */
class RegistrationApiTest {

    private AuthRepository repo;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        repo = new AuthRepository();
        repo.deleteAll(); // Clean database before each test
        authService = new AuthService(repo);
    }

    private User validUser(String phone) {
        return User.forRegistration("Ahmad Mohammadi", phone, "ahmad@example.com", "hashed_securepass123", "Tehran");
    }

    private User minimalUser(String phone) {
        return User.forRegistration("Simple User", phone, "", "hashed_simplepass", "");
    }

    @Test
    void registerUser_validData_success() {
        String phone = "09123456789";
        User user = validUser(phone);
        
        User saved = authService.registerUser(user);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isPositive();
        assertThat(saved.getFullName()).isEqualTo("Ahmad Mohammadi");
        assertThat(saved.getPhone()).isEqualTo(phone);
        assertThat(saved.getEmail()).isEqualTo("ahmad@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed_securepass123");
        assertThat(saved.getAddress()).isEqualTo("Tehran");
        assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
    }

    @Test
    void registerUser_minimalData_success() {
        String phone = "09123456788";
        User user = minimalUser(phone);
        
        User saved = authService.registerUser(user);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFullName()).isEqualTo("Simple User");
        assertThat(saved.getPhone()).isEqualTo(phone);
        assertThat(saved.getEmail()).isEqualTo("");
        assertThat(saved.getAddress()).isEqualTo("");
        assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
    }

    @Test
    void registerUser_duplicatePhone_throws() {
        String phone = "09123456787";
        User firstUser = validUser(phone);
        authService.registerUser(firstUser);
        
        User secondUser = User.forRegistration("Second User", phone, "second@example.com", "hashed_pass2", "Isfahan");
        
        assertThatThrownBy(() -> authService.registerUser(secondUser))
                .isInstanceOf(com.myapp.common.exceptions.DuplicatePhoneException.class);
    }

    @Test
    void registerUser_passwordHashing_correct() {
        String phone = "09123456785";
        String hashedPassword = "hashed_myplainpassword";
        
        User user = User.forRegistration("Test User", phone, "test@example.com", hashedPassword, "Tehran");
        User saved = authService.registerUser(user);
        
        assertThat(saved.getPasswordHash()).isEqualTo(hashedPassword);
        assertThat(saved.getPasswordHash()).startsWith("hashed_");
    }

    @Test
    void registerUser_userRole_defaultBuyer() {
        String phone = "09123456784";
        User user = validUser(phone);
        
        User saved = authService.registerUser(user);
        
        assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
    }

    @Test
    void registerUser_persianCharacters_supported() {
        String phone = "09123456783";
        String persianName = "MohammadReza Ahmadinezhad";
        String persianAddress = "Tehran, Azadi Street, Shahid Mohammadi Alley";
        
        User user = User.forRegistration(persianName, phone, "persian@example.com", "hashed_pass", persianAddress);
        User saved = authService.registerUser(user);
        
        assertThat(saved.getFullName()).isEqualTo(persianName);
        assertThat(saved.getAddress()).isEqualTo(persianAddress);
    }

    @Test
    void registerUser_longStrings_handled() {
        String phone = "09123456782";
        String longName = "Very Long Name That Might Cause Problems In The System And Should Be Tested";
        String longAddress = "Very Long Address That Includes Various Streets And Alleys And Multiple Building Numbers";
        
        User user = User.forRegistration(longName, phone, "long@example.com", "hashed_pass", longAddress);
        User saved = authService.registerUser(user);
        
        assertThat(saved.getFullName()).isEqualTo(longName);
        assertThat(saved.getAddress()).isEqualTo(longAddress);
    }

    @Test
    void registerUser_specialCharacters_handled() {
        String phone = "09123456781";
        String nameWithSpecialChars = "Ali@Mohammad#Hassan$Hussein%";
        String emailWithSpecialChars = "test+user@example-domain.com";
        
        User user = User.forRegistration(nameWithSpecialChars, phone, emailWithSpecialChars, "hashed_pass", "Tehran");
        User saved = authService.registerUser(user);
        
        assertThat(saved.getFullName()).isEqualTo(nameWithSpecialChars);
        assertThat(saved.getEmail()).isEqualTo(emailWithSpecialChars);
    }

    @Test
    void registerUser_multipleUsers_allPersisted() {
        User user1 = validUser("09111111111");
        User user2 = validUser("09222222222");
        User user3 = validUser("09333333333");
        
        User saved1 = authService.registerUser(user1);
        User saved2 = authService.registerUser(user2);
        User saved3 = authService.registerUser(user3);
        
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
        assertThat(saved2.getId()).isNotEqualTo(saved3.getId());
        assertThat(saved1.getId()).isNotEqualTo(saved3.getId());
        
        assertThat(repo.findByPhone("09111111111")).isPresent();
        assertThat(repo.findByPhone("09222222222")).isPresent();
        assertThat(repo.findByPhone("09333333333")).isPresent();
    }

    @Test
    void registerUser_databaseIntegrity_maintained() {
        String phone = "09123456777";
        User user = validUser(phone);
        
        // Verify database is clean
        assertThat(repo.findByPhone(phone)).isNotPresent();
        
        // Register user
        User saved = authService.registerUser(user);
        
        // Verify persistence
        assertThat(repo.findById(saved.getId())).isPresent();
        assertThat(repo.findByPhone(phone)).isPresent();
        
        // Verify data integrity
        User retrieved = repo.findByPhone(phone).get();
        assertThat(retrieved.getId()).isEqualTo(saved.getId());
        assertThat(retrieved.getFullName()).isEqualTo(saved.getFullName());
        assertThat(retrieved.getPhone()).isEqualTo(saved.getPhone());
        assertThat(retrieved.getEmail()).isEqualTo(saved.getEmail());
        assertThat(retrieved.getPasswordHash()).isEqualTo(saved.getPasswordHash());
        assertThat(retrieved.getAddress()).isEqualTo(saved.getAddress());
        assertThat(retrieved.getRole()).isEqualTo(saved.getRole());
    }

    @Test
    void forRegistration_factoryMethod_correctDefaults() {
        String phone = "09123456776";
        String fullName = "Test User";
        String email = "test@example.com";
        String passwordHash = "hashed_password";
        String address = "Test Address";
        
        User user = User.forRegistration(fullName, phone, email, passwordHash, address);
        
        assertThat(user.getFullName()).isEqualTo(fullName);
        assertThat(user.getPhone()).isEqualTo(phone);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(user.getAddress()).isEqualTo(address);
        assertThat(user.getRole()).isEqualTo(User.Role.BUYER);
        assertThat(user.getId()).isNull(); // Not set until saved
    }
}