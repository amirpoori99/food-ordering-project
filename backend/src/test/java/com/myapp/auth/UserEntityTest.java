package com.myapp.auth;

import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * تست‌های مدل User Entity
 * این کلاس entity خود User را تست می‌کند، نه business logic آن
 */
@DisplayName("User Entity Tests")
class UserEntityTest {

    @BeforeEach
    void setup() {
        // تمیزکاری پایگاه داده قبل از هر تست
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from User").executeUpdate();
            tx.commit();
        }
    }

    @Nested
    @DisplayName("Entity Creation Tests")
    class EntityCreationTests {
        
        @Test
        @DisplayName("User creation with valid data succeeds")
        void user_entityCreation_success() {
            // Given & When
            User user = new User("علی احمدی", "09123456789", "ali@example.com", 
                               "hashedpassword", User.Role.BUYER, "تهران، خیابان ولیعصر");
            
            // Then
            assertThat(user.getFullName()).isEqualTo("علی احمدی");
            assertThat(user.getPhone()).isEqualTo("09123456789");
            assertThat(user.getEmail()).isEqualTo("ali@example.com");
            assertThat(user.getPasswordHash()).isEqualTo("hashedpassword");
            assertThat(user.getRole()).isEqualTo(User.Role.BUYER);
            assertThat(user.getAddress()).isEqualTo("تهران، خیابان ولیعصر");
            assertThat(user.getIsActive()).isTrue(); // پیش‌فرض باید true باشد
            assertThat(user.getId()).isNull(); // هنوز persist نشده
        }

        @Test
        @DisplayName("User creation with Long id succeeds")
        void user_entityCreationWithLongId_success() {
            // Given & When
            User user = new User(100L, "سارا محمدی", "09987654321", "sara@example.com", 
                               "hashedpass", User.Role.SELLER, "شیراز");
            
            // Then
            assertThat(user.getId()).isEqualTo(100L);
            assertThat(user.getFullName()).isEqualTo("سارا محمدی");
            assertThat(user.getRole()).isEqualTo(User.Role.SELLER);
        }

        @Test
        @DisplayName("User creation with int id (compatibility) succeeds")
        void user_entityCreationWithIntId_success() {
            // Given & When
            User user = new User(50, "حسن رضایی", "09111111111", "hasan@example.com", 
                               "pass123", User.Role.COURIER, "اصفهان");
            
            // Then
            assertThat(user.getId()).isEqualTo(50L); // تبدیل int به Long
            assertThat(user.getFullName()).isEqualTo("حسن رضایی");
            assertThat(user.getRole()).isEqualTo(User.Role.COURIER);
        }

        @Test
        @DisplayName("User default constructor succeeds")
        void user_defaultConstructor_success() {
            // Given & When
            User user = new User();
            
            // Then
            assertThat(user.getIsActive()).isTrue(); // پیش‌فرض باید true باشد
            assertThat(user.getId()).isNull();
            assertThat(user.getFullName()).isNull();
            assertThat(user.getPhone()).isNull();
            assertThat(user.getEmail()).isNull();
            assertThat(user.getPasswordHash()).isNull();
            assertThat(user.getRole()).isNull();
            assertThat(user.getAddress()).isNull();
        }

        @Test
        @DisplayName("User forRegistration factory method succeeds")
        void user_forRegistration_success() {
            // Given & When
            User user = User.forRegistration("محمد کریمی", "09333333333", "mohammad@example.com", 
                                          "securepassword", "مشهد، خیابان امام رضا");
            
            // Then
            assertThat(user.getFullName()).isEqualTo("محمد کریمی");
            assertThat(user.getPhone()).isEqualTo("09333333333");
            assertThat(user.getEmail()).isEqualTo("mohammad@example.com");
            assertThat(user.getPasswordHash()).isEqualTo("securepassword");
            assertThat(user.getRole()).isEqualTo(User.Role.BUYER); // factory method پیش‌فرض BUYER است
            assertThat(user.getAddress()).isEqualTo("مشهد، خیابان امام رضا");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("User creation with empty email succeeds")
        void user_creation_emptyEmail_success() {
            // Given & When
            User user = new User("تست کاربر", "09999999999", "", 
                               "password", User.Role.BUYER, "آدرس تست");
            
            // Then
            assertThat(user.getEmail()).isEqualTo("");
            assertThat(user.getFullName()).isEqualTo("تست کاربر");
        }

        @Test
        @DisplayName("User creation with empty address succeeds")
        void user_creation_emptyAddress_success() {
            // Given & When
            User user = new User("تست کاربر", "09888888888", "test@example.com", 
                               "password", User.Role.ADMIN, "");
            
            // Then
            assertThat(user.getAddress()).isEqualTo("");
            assertThat(user.getRole()).isEqualTo(User.Role.ADMIN);
        }

        @Test
        @DisplayName("User creation with Persian characters succeeds")
        void user_creation_persianCharacters_success() {
            // Given & When
            User user = new User("فاطمه زهرایی", "09555555555", "fateme@نمونه.ایران", 
                               "رمزعبور۱۲۳", User.Role.SELLER, "تهران، میدان آزادی، خیابان شهید بهشتی");
            
            // Then
            assertThat(user.getFullName()).isEqualTo("فاطمه زهرایی");
            assertThat(user.getEmail()).isEqualTo("fateme@نمونه.ایران");
            assertThat(user.getPasswordHash()).isEqualTo("رمزعبور۱۲۳");
            assertThat(user.getAddress()).isEqualTo("تهران، میدان آزادی، خیابان شهید بهشتی");
        }

        @Test
        @DisplayName("User creation with special characters succeeds")
        void user_creation_specialCharacters_success() {
            // Given & When
            String nameWithSpecial = "John@Doe#2024";
            String emailWithSpecial = "user+test@example-site.co.uk";
            String addressWithSpecial = "123 Main St., Apt #4B, City @2024!";
            
            User user = new User(nameWithSpecial, "09123123123", emailWithSpecial, 
                               "pass@#$%", User.Role.COURIER, addressWithSpecial);
            
            // Then
            assertThat(user.getFullName()).isEqualTo(nameWithSpecial);
            assertThat(user.getEmail()).isEqualTo(emailWithSpecial);
            assertThat(user.getAddress()).isEqualTo(addressWithSpecial);
        }
    }

    @Nested
    @DisplayName("Role Enum Tests")
    class RoleEnumTests {
        
        @ParameterizedTest
        @EnumSource(User.Role.class)
        @DisplayName("All User roles should be assignable")
        void user_allRoles_assignable(User.Role role) {
            // Given & When
            User user = new User("تست کاربر", "09111222333", "test@example.com", 
                               "password", role, "آدرس تست");
            
            // Then
            assertThat(user.getRole()).isEqualTo(role);
        }

        @Test
        @DisplayName("Role enum values are correct")
        void userRole_enumValues_correct() {
            // Given & When & Then
            assertThat(User.Role.values()).hasSize(4);
            assertThat(User.Role.values()).contains(
                User.Role.BUYER, 
                User.Role.SELLER, 
                User.Role.COURIER, 
                User.Role.ADMIN
            );
        }

        @Test
        @DisplayName("Role enum valueOf works correctly")
        void userRole_valueOf_correct() {
            // Given & When & Then
            assertThat(User.Role.valueOf("BUYER")).isEqualTo(User.Role.BUYER);
            assertThat(User.Role.valueOf("SELLER")).isEqualTo(User.Role.SELLER);
            assertThat(User.Role.valueOf("COURIER")).isEqualTo(User.Role.COURIER);
            assertThat(User.Role.valueOf("ADMIN")).isEqualTo(User.Role.ADMIN);
        }

        @Test
        @DisplayName("Role enum toString works correctly")
        void userRole_toString_correct() {
            // Given & When & Then
            assertThat(User.Role.BUYER.toString()).isEqualTo("BUYER");
            assertThat(User.Role.SELLER.toString()).isEqualTo("SELLER");
            assertThat(User.Role.COURIER.toString()).isEqualTo("COURIER");
            assertThat(User.Role.ADMIN.toString()).isEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {
        
        @Test
        @DisplayName("All getters and setters work correctly")
        void user_gettersSetters_workCorrectly() {
            // Given
            User user = new User();
            
            // When & Then - ID
            user.setId(123L);
            assertThat(user.getId()).isEqualTo(123L);
            
            // When & Then - Full Name
            user.setFullName("نام جدید");
            assertThat(user.getFullName()).isEqualTo("نام جدید");
            
            // When & Then - Phone
            user.setPhone("09999999999");
            assertThat(user.getPhone()).isEqualTo("09999999999");
            
            // When & Then - Email
            user.setEmail("new@example.com");
            assertThat(user.getEmail()).isEqualTo("new@example.com");
            
            // When & Then - Password Hash
            user.setPasswordHash("newhashedpassword");
            assertThat(user.getPasswordHash()).isEqualTo("newhashedpassword");
            
            // When & Then - Role
            user.setRole(User.Role.ADMIN);
            assertThat(user.getRole()).isEqualTo(User.Role.ADMIN);
            
            // When & Then - Address
            user.setAddress("آدرس جدید");
            assertThat(user.getAddress()).isEqualTo("آدرس جدید");
            
            // When & Then - Is Active
            user.setIsActive(false);
            assertThat(user.getIsActive()).isFalse();
            
            user.setIsActive(true);
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Setters with null values work correctly")
        void user_settersWithNull_workCorrectly() {
            // Given
            User user = new User("تست", "09123456789", "test@example.com", 
                               "pass", User.Role.BUYER, "آدرس");
            
            // When & Then
            user.setFullName(null);
            assertThat(user.getFullName()).isNull();
            
            user.setEmail(null);
            assertThat(user.getEmail()).isNull();
            
            user.setAddress(null);
            assertThat(user.getAddress()).isNull();
            
            user.setRole(null);
            assertThat(user.getRole()).isNull();
            
            // isActive null test
            user.setIsActive(null);
            assertThat(user.getIsActive()).isNull();
        }
    }

    @Nested
    @DisplayName("Persistence Tests")
    class PersistenceTests {
        
        @Test
        @DisplayName("User persistence succeeds")
        void user_persistence_success() {
            // Given
            User user = new User("کاربر تست", "09111111111", "test@example.com", 
                               "hashed_password", User.Role.BUYER, "تهران");
            
            // When
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.persist(user);
                tx.commit();
            }
            
            // Then
            assertThat(user.getId()).isNotNull();
            assertThat(user.getId()).isPositive();
            
            // Verify persistence
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                User retrieved = session.get(User.class, user.getId());
                assertThat(retrieved).isNotNull();
                assertThat(retrieved.getFullName()).isEqualTo("کاربر تست");
                assertThat(retrieved.getPhone()).isEqualTo("09111111111");
                assertThat(retrieved.getRole()).isEqualTo(User.Role.BUYER);
                assertThat(retrieved.getIsActive()).isTrue();
            }
        }

        @Test
        @DisplayName("User with all roles persistence")
        void user_allRoles_persistence_success() {
            // Given
            User buyer = new User("خریدار", "09111111111", "buyer@example.com", "pass", User.Role.BUYER, "تهران");
            User seller = new User("فروشنده", "09222222222", "seller@example.com", "pass", User.Role.SELLER, "شیراز");
            User courier = new User("پیک", "09333333333", "courier@example.com", "pass", User.Role.COURIER, "اصفهان");
            User admin = new User("مدیر", "09444444444", "admin@example.com", "pass", User.Role.ADMIN, "مشهد");
            
            // When
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.persist(buyer);
                session.persist(seller);
                session.persist(courier);
                session.persist(admin);
                tx.commit();
            }
            
            // Then
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                User retrievedBuyer = session.get(User.class, buyer.getId());
                User retrievedSeller = session.get(User.class, seller.getId());
                User retrievedCourier = session.get(User.class, courier.getId());
                User retrievedAdmin = session.get(User.class, admin.getId());
                
                assertThat(retrievedBuyer.getRole()).isEqualTo(User.Role.BUYER);
                assertThat(retrievedSeller.getRole()).isEqualTo(User.Role.SELLER);
                assertThat(retrievedCourier.getRole()).isEqualTo(User.Role.COURIER);
                assertThat(retrievedAdmin.getRole()).isEqualTo(User.Role.ADMIN);
            }
        }

        @Test
        @DisplayName("User with Persian data persistence")
        void user_persianData_persistence_success() {
            // Given
            User user = new User("علیرضا محمدزاده", "09555555555", "alireza@نمونه.ایران", 
                               "رمزعبور۱۲۳۴", User.Role.SELLER, "تهران، خیابان انقلاب، پلاک ۱۲۳");
            
            // When
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.persist(user);
                tx.commit();
            }
            
            // Then
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                User retrieved = session.get(User.class, user.getId());
                assertThat(retrieved.getFullName()).isEqualTo("علیرضا محمدزاده");
                assertThat(retrieved.getEmail()).isEqualTo("alireza@نمونه.ایران");
                assertThat(retrieved.getPasswordHash()).isEqualTo("رمزعبور۱۲۳۴");
                assertThat(retrieved.getAddress()).isEqualTo("تهران، خیابان انقلاب، پلاک ۱۲۳");
            }
        }

        @Test
        @DisplayName("User isActive flag persistence")
        void user_isActiveFlag_persistence_success() {
            // Given
            User activeUser = new User("کاربر فعال", "09111111111", "active@example.com", 
                                     "pass", User.Role.BUYER, "تهران");
            activeUser.setIsActive(true);
            
            User inactiveUser = new User("کاربر غیرفعال", "09222222222", "inactive@example.com", 
                                       "pass", User.Role.BUYER, "تهران");
            inactiveUser.setIsActive(false);
            
            // When
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.persist(activeUser);
                session.persist(inactiveUser);
                tx.commit();
            }
            
            // Then
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                User retrievedActive = session.get(User.class, activeUser.getId());
                User retrievedInactive = session.get(User.class, inactiveUser.getId());
                
                assertThat(retrievedActive.getIsActive()).isTrue();
                assertThat(retrievedInactive.getIsActive()).isFalse();
            }
        }

        @Test
        @DisplayName("User phone uniqueness constraint")
        void user_phoneUniqueness_constraintEnforced() {
            // Given
            User user1 = new User("کاربر اول", "09123456789", "user1@example.com", 
                                "pass1", User.Role.BUYER, "تهران");
            User user2 = new User("کاربر دوم", "09123456789", "user2@example.com", 
                                "pass2", User.Role.SELLER, "شیراز");
            
            // When & Then
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.persist(user1);
                tx.commit();
                
                // تلاش برای ذخیره کاربر دوم با همان شماره تلفن
                Transaction tx2 = session.beginTransaction();
                assertThatThrownBy(() -> {
                    session.persist(user2);
                    session.flush(); // اجبار به بررسی constraint
                }).isInstanceOf(Exception.class); // انتظار constraint violation
                
                tx2.rollback();
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesAndBoundaryTests {
        
        @Test
        @DisplayName("User with very long data")
        void user_veryLongData_handled() {
            // Given
            String longName = "نام بسیار طولانی ".repeat(20);
            String longEmail = "very" + "long".repeat(50) + "@example.com";
            String longAddress = "آدرس بسیار طولانی ".repeat(50);
            
            // When
            User user = new User(longName, "09999999999", longEmail, 
                               "password", User.Role.BUYER, longAddress);
            
            // Then
            assertThat(user.getFullName()).isEqualTo(longName);
            assertThat(user.getEmail()).isEqualTo(longEmail);
            assertThat(user.getAddress()).isEqualTo(longAddress);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", " \t \n "})
        @DisplayName("User with whitespace-only fields")
        void user_whitespaceOnlyFields_handled(String whitespace) {
            // Given & When
            User user = new User(whitespace, "09999999999", whitespace, 
                               "password", User.Role.BUYER, whitespace);
            
            // Then
            assertThat(user.getFullName()).isEqualTo(whitespace);
            assertThat(user.getEmail()).isEqualTo(whitespace);
            assertThat(user.getAddress()).isEqualTo(whitespace);
        }

        @Test
        @DisplayName("User with extreme ID values")
        void user_extremeIdValues_handled() {
            // Given & When & Then
            User userMaxLong = new User(Long.MAX_VALUE, "تست", "09999999999", "test@example.com", 
                                      "pass", User.Role.BUYER, "آدرس");
            assertThat(userMaxLong.getId()).isEqualTo(Long.MAX_VALUE);
            
            User userMinLong = new User(Long.MIN_VALUE, "تست", "09999999998", "test2@example.com", 
                                      "pass", User.Role.BUYER, "آدرس");
            assertThat(userMinLong.getId()).isEqualTo(Long.MIN_VALUE);
            
            User userZero = new User(0L, "تست", "09999999997", "test3@example.com", 
                                   "pass", User.Role.BUYER, "آدرس");
            assertThat(userZero.getId()).isEqualTo(0L);
        }
    }
} 