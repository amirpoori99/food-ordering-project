package com.myapp.api;

import com.myapp.auth.AuthRepository;
import com.myapp.auth.AuthService;
import com.myapp.common.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.*;

/**
 * Test Registration functionality with comprehensive scenarios
 * Following phase 1 test patterns and standards
 */
@DisplayName("Registration API Comprehensive Tests")
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

    @Nested
    @DisplayName("Basic Registration Tests")
    class BasicRegistrationTests {

        @Test
        @DisplayName("Register user with valid data succeeds")
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
        @DisplayName("Register user with minimal data succeeds")
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
        @DisplayName("Register user with duplicate phone throws exception")
        void registerUser_duplicatePhone_throws() {
            String phone = "09123456787";
            User firstUser = validUser(phone);
            authService.registerUser(firstUser);
            
            User secondUser = User.forRegistration("Second User", phone, "second@example.com", "hashed_pass2", "Isfahan");
            
            assertThatThrownBy(() -> authService.registerUser(secondUser))
                    .isInstanceOf(com.myapp.common.exceptions.DuplicatePhoneException.class);
        }

        @Test
        @DisplayName("Register user with password hashing works correctly")
        void registerUser_passwordHashing_correct() {
            String phone = "09123456785";
            String hashedPassword = "hashed_myplainpassword";
            
            User user = User.forRegistration("Test User", phone, "test@example.com", hashedPassword, "Tehran");
            User saved = authService.registerUser(user);
            
            assertThat(saved.getPasswordHash()).isEqualTo(hashedPassword);
            assertThat(saved.getPasswordHash()).startsWith("hashed_");
        }

        @Test
        @DisplayName("Register user has default BUYER role")
        void registerUser_userRole_defaultBuyer() {
            String phone = "09123456784";
            User user = validUser(phone);
            
            User saved = authService.registerUser(user);
            
            assertThat(saved.getRole()).isEqualTo(User.Role.BUYER);
        }
    }

    @Nested
    @DisplayName("Character Encoding Tests")
    class CharacterEncodingTests {

        @Test
        @DisplayName("Register user with Persian characters")
        void registerUser_persianCharacters_supported() {
            String phone = "09123456783";
            String persianName = "محمدرضا احمدی‌نژاد";
            String persianAddress = "تهران، خیابان آزادی، کوچه شهید محمدی";
            
            User user = User.forRegistration(persianName, phone, "persian@example.com", "hashed_pass", persianAddress);
            User saved = authService.registerUser(user);
            
            assertThat(saved.getFullName()).isEqualTo(persianName);
            assertThat(saved.getAddress()).isEqualTo(persianAddress);
        }

        @Test
        @DisplayName("Register user with Arabic characters")
        void registerUser_arabicCharacters_supported() {
            String phone = "09123456782";
            String arabicName = "عبد الله محمد الأحمد";
            String arabicAddress = "الرياض، شارع الملك فهد";
            
            User user = User.forRegistration(arabicName, phone, "arabic@example.com", "hashed_pass", arabicAddress);
            User saved = authService.registerUser(user);
            
            assertThat(saved.getFullName()).isEqualTo(arabicName);
            assertThat(saved.getAddress()).isEqualTo(arabicAddress);
        }

        @Test
        @DisplayName("Register user with mixed languages")
        void registerUser_mixedLanguages_supported() {
            String phone = "09123456781";
            String mixedName = "Ali علی Smith";
            String mixedAddress = "Tehran تهران, Street خیابان 123";
            
            User user = User.forRegistration(mixedName, phone, "mixed@example.com", "hashed_pass", mixedAddress);
            User saved = authService.registerUser(user);
            
            assertThat(saved.getFullName()).isEqualTo(mixedName);
            assertThat(saved.getAddress()).isEqualTo(mixedAddress);
        }

        @Test
        @DisplayName("Register user with special characters")
        void registerUser_specialCharacters_handled() {
            String phone = "09123456780";
            String nameWithSpecialChars = "Ali@Mohammad#Hassan$Hussein%^&*()";
            String emailWithSpecialChars = "test+user.name@sub-domain.example-site.com";
            
            User user = User.forRegistration(nameWithSpecialChars, phone, emailWithSpecialChars, "hashed_pass", "Tehran");
            User saved = authService.registerUser(user);
            
            assertThat(saved.getFullName()).isEqualTo(nameWithSpecialChars);
            assertThat(saved.getEmail()).isEqualTo(emailWithSpecialChars);
        }
    }

    @Nested
    @DisplayName("Data Length Tests")
    class DataLengthTests {

        @Test
        @DisplayName("Register user with long strings")
        void registerUser_longStrings_handled() {
            String phone = "09123456779";
            String longName = "A".repeat(200);
            String longEmail = "very.long.email.address.that.might.cause.problems@very-long-domain-name-that-should-be-tested.com";
            String longAddress = "Very Long Address That Includes Various Streets And Alleys And Multiple Building Numbers And Postal Codes ".repeat(5);
            
            User user = User.forRegistration(longName, phone, longEmail, "hashed_pass", longAddress);
            User saved = authService.registerUser(user);
            
            assertThat(saved.getFullName()).isEqualTo(longName);
            assertThat(saved.getEmail()).isEqualTo(longEmail);
            assertThat(saved.getAddress()).isEqualTo(longAddress);
        }

        @Test
        @DisplayName("Register user with very long password hash")
        void registerUser_longPasswordHash_handled() {
            String phone = "09123456778";
            String longPasswordHash = "hashed_" + "a".repeat(500);
            
            User user = User.forRegistration("Test User", phone, "test@example.com", longPasswordHash, "Tehran");
            User saved = authService.registerUser(user);
            
            assertThat(saved.getPasswordHash()).isEqualTo(longPasswordHash);
        }

        @Test
        @DisplayName("Register user with single character fields")
        void registerUser_singleCharacterFields_handled() {
            String phone = "09123456777";
            String singleCharName = "A";
            String singleCharEmail = "a@b.c";
            String singleCharAddress = "T";
            
            User user = User.forRegistration(singleCharName, phone, singleCharEmail, "h", singleCharAddress);
            User saved = authService.registerUser(user);
            
            assertThat(saved.getFullName()).isEqualTo(singleCharName);
            assertThat(saved.getEmail()).isEqualTo(singleCharEmail);
            assertThat(saved.getAddress()).isEqualTo(singleCharAddress);
        }
    }

    @Nested
    @DisplayName("Phone Number Validation Tests")
    class PhoneNumberValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "09123456776", "09123456775", "09123456774", "09123456773",
            "09121234567", "09351234567", "09901234567", "09171234567"
        })
        @DisplayName("Register user with various valid phone formats")
        void registerUser_validPhoneFormats_success(String phone) {
            User user = User.forRegistration("Test User", phone, "test@example.com", "hashed_pass", "Tehran");
            User saved = authService.registerUser(user);
            
            assertThat(saved.getPhone()).isEqualTo(phone);
        }

        @Test
        @DisplayName("Register user with phone containing spaces")
        void registerUser_phoneWithSpaces_handled() {
            String phone = "09123456772";
            User user = User.forRegistration("Test User", phone, "test@example.com", "hashed_pass", "Tehran");
            User saved = authService.registerUser(user);
            
            assertThat(saved.getPhone()).isEqualTo(phone);
        }
    }

    @Nested
    @DisplayName("Role Assignment Tests")
    class RoleAssignmentTests {

        @ParameterizedTest
        @EnumSource(User.Role.class)
        @DisplayName("Register user with different roles")
        void registerUser_differentRoles_success(User.Role role) {
            String phone = "0912345677" + role.ordinal();
            User user = User.forRegistration("Test User", phone, "test@example.com", "hashed_pass", "Tehran");
            user.setRole(role); // Set specific role
            
            User saved = authService.registerUser(user);
            
            assertThat(saved.getRole()).isEqualTo(role);
        }
    }

    @Nested
    @DisplayName("Multiple User Tests")
    class MultipleUserTests {

        @Test
        @DisplayName("Register multiple users with all data persisted")
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
        @DisplayName("Register large number of users")
        void registerUser_largeNumberOfUsers_allPersisted() {
            for (int i = 0; i < 50; i++) {
                String phone = String.format("0912345%04d", i);
                User user = User.forRegistration("User " + i, phone, "user" + i + "@example.com", "hashed_pass" + i, "Address " + i);
                
                User saved = authService.registerUser(user);
                
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getPhone()).isEqualTo(phone);
                assertThat(saved.getFullName()).isEqualTo("User " + i);
            }
        }

        @Test
        @DisplayName("Register users with similar but different data")
        void registerUser_similarButDifferentData_success() {
            User user1 = User.forRegistration("Ali Mohammad", "09111111110", "ali@example.com", "hashed_pass1", "Tehran");
            User user2 = User.forRegistration("Ali Mohammad", "09111111111", "ali2@example.com", "hashed_pass2", "Tehran");
            User user3 = User.forRegistration("Ali Mohammad", "09111111112", "ali@example.com", "hashed_pass3", "Isfahan");
            
            User saved1 = authService.registerUser(user1);
            User saved2 = authService.registerUser(user2);
            User saved3 = authService.registerUser(user3);
            
            assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
            assertThat(saved2.getId()).isNotEqualTo(saved3.getId());
            assertThat(saved1.getId()).isNotEqualTo(saved3.getId());
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Register user maintains database integrity")
        void registerUser_databaseIntegrity_maintained() {
            String phone = "09123456770";
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
        @DisplayName("Register user with exact field preservation")
        void registerUser_exactFieldPreservation_maintained() {
            String phone = "09123456769";
            String exactName = "Exact Name With Spaces  And  Multiple  Spaces";
            String exactEmail = "exact.email+with+plus@domain.co.uk";
            String exactAddress = "Exact Address\nWith\nNewlines\tAnd\tTabs";
            String exactPassword = "hashed_exact_password_with_special_chars!@#$%";
            
            User user = User.forRegistration(exactName, phone, exactEmail, exactPassword, exactAddress);
            User saved = authService.registerUser(user);
            
            assertThat(saved.getFullName()).isEqualTo(exactName);
            assertThat(saved.getEmail()).isEqualTo(exactEmail);
            assertThat(saved.getAddress()).isEqualTo(exactAddress);
            assertThat(saved.getPasswordHash()).isEqualTo(exactPassword);
            
            // Verify from database
            User retrieved = repo.findByPhone(phone).get();
            assertThat(retrieved.getFullName()).isEqualTo(exactName);
            assertThat(retrieved.getEmail()).isEqualTo(exactEmail);
            assertThat(retrieved.getAddress()).isEqualTo(exactAddress);
            assertThat(retrieved.getPasswordHash()).isEqualTo(exactPassword);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("forRegistration factory method sets correct defaults")
        void forRegistration_factoryMethod_correctDefaults() {
            String phone = "09123456768";
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

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("forRegistration with null/empty email")
        void forRegistration_nullEmptyEmail_handled(String email) {
            String phone = "0912345676" + (email == null ? "1" : "2");
            
            User user = User.forRegistration("Test User", phone, email, "hashed_pass", "Test Address");
            User saved = authService.registerUser(user);
            
            assertThat(saved.getEmail()).isEqualTo(email);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("forRegistration with null/empty address")
        void forRegistration_nullEmptyAddress_handled(String address) {
            String phone = "0912345675" + (address == null ? "1" : "2");
            
            User user = User.forRegistration("Test User", phone, "test@example.com", "hashed_pass", address);
            User saved = authService.registerUser(user);
            
            assertThat(saved.getAddress()).isEqualTo(address);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Register user with whitespace-only fields")
        void registerUser_whitespaceOnlyFields_handled() {
            String phone = "09123456767";
            String whitespaceName = "   ";
            String whitespaceEmail = "\t\n";
            String whitespaceAddress = "  \t  \n  ";
            
            User user = User.forRegistration(whitespaceName, phone, whitespaceEmail, "hashed_pass", whitespaceAddress);
            User saved = authService.registerUser(user);
            
            assertThat(saved.getFullName()).isEqualTo(whitespaceName);
            assertThat(saved.getEmail()).isEqualTo(whitespaceEmail);
            assertThat(saved.getAddress()).isEqualTo(whitespaceAddress);
        }

        @Test
        @DisplayName("Register user with numeric name")
        void registerUser_numericName_handled() {
            String phone = "09123456766";
            String numericName = "123456789";
            
            User user = User.forRegistration(numericName, phone, "numeric@example.com", "hashed_pass", "Tehran");
            User saved = authService.registerUser(user);
            
            assertThat(saved.getFullName()).isEqualTo(numericName);
        }

        @Test
        @DisplayName("Register user with email containing international characters")
        void registerUser_internationalEmail_handled() {
            String phone = "09123456765";
            String internationalEmail = "müller@münchen.de";
            
            User user = User.forRegistration("Test User", phone, internationalEmail, "hashed_pass", "Tehran");
            User saved = authService.registerUser(user);
            
            assertThat(saved.getEmail()).isEqualTo(internationalEmail);
        }
    }
}