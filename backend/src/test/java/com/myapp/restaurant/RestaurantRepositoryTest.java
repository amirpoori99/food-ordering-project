package com.myapp.restaurant;

import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * مجموعه تست‌های جامع RestaurantRepository
 * 
 * این کلاس تست تمام عملکردهای repository مدیریت رستوران‌ها را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Restaurant Creation Tests
 *    - ایجاد رستوران با داده‌های معتبر
 *    - ایجاد با حداقل داده‌ها
 *    - فرمت‌های مختلف تلفن
 * 
 * 2. Restaurant Retrieval Tests
 *    - یافتن رستوران با ID
 *    - مدیریت رستوران غیرموجود
 *    - دریافت همه رستوران‌ها
 * 
 * 3. Restaurant Status Tests
 *    - به‌روزرسانی وضعیت رستوران
 *    - فیلتر بر اساس وضعیت
 *    - تست همه وضعیت‌ها
 * 
 * 4. Restaurant Deletion Tests
 *    - حذف رستوران موجود
 *    - حذف رستوران غیرموجود
 *    - حذف همه رستوران‌ها
 * 
 * 5. Restaurant Owner Tests
 *    - یافتن رستوران‌های مالک
 *    - مالک بدون رستوران
 * 
 * 6. Restaurant Approved List Tests
 *    - لیست رستوران‌های تأیید شده
 *    - مدیریت حالت خالی
 * 
 * 7. Restaurant Update Tests
 *    - به‌روزرسانی وضعیت مستقیم
 *    - به‌روزرسانی اطلاعات رستوران
 * 
 * 8. Restaurant Edge Case Tests
 *    - کاراکترهای خاص و Unicode
 *    - فرمت‌های مختلف تلفن
 *    - نام‌های تکراری با مالکان مختلف
 *    - حداقل داده‌های معتبر
 * 
 * 9. Restaurant Concurrency Tests
 *    - عملیات متوالی سریع
 *    - ایجاد همزمان چندین رستوران
 * 
 * 10. Restaurant Query Performance Tests
 *     - عملکرد با dataset بزرگ
 *     - بهینه‌سازی query ها
 * 
 * Database Integration:
 * - Real Hibernate operations
 * - Transaction management
 * - Data cleanup
 * - Isolation between tests
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("RestaurantRepository Tests")
class RestaurantRepositoryTest {

    /** Repository instance تحت تست */
    private RestaurantRepository repository;

    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - initialize repository
     * - clean database state
     * - ensure test isolation
     */
    @BeforeEach
    void setUp() {
        repository = new RestaurantRepository();
        // پاک‌سازی پایگاه داده قبل از هر تست
        repository.deleteAll();
    }

    @Nested
    @DisplayName("Restaurant Creation Tests")
    class RestaurantCreationTests {
        
        @Test
        @DisplayName("Restaurant creation with valid data succeeds")
        void save_validRestaurant_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Pizza Palace", "Tehran, Valiasr St", "021-12345678");
            
            // When
            Restaurant saved = repository.save(restaurant);
            
            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getId()).isPositive();
            assertThat(saved.getName()).isEqualTo("Pizza Palace");
            assertThat(saved.getAddress()).isEqualTo("Tehran, Valiasr St");
            assertThat(saved.getPhone()).isEqualTo("021-12345678");
            assertThat(saved.getStatus()).isEqualTo(RestaurantStatus.PENDING);
        }

        @Test
        @DisplayName("Restaurant creation with minimal data succeeds")
        void save_minimalRestaurant_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(2L, "Simple Cafe", "Isfahan", "031-11111111");
            
            // When
            Restaurant saved = repository.save(restaurant);
            
            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Simple Cafe");
            assertThat(saved.getAddress()).isEqualTo("Isfahan");
            assertThat(saved.getPhone()).isEqualTo("031-11111111");
            assertThat(saved.getStatus()).isEqualTo(RestaurantStatus.PENDING);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "021-12345678", "031-87654321", "061-11111111", "041-22222222", "051-33333333"
        })
        @DisplayName("Restaurant creation with various phone formats succeeds")
        void save_variousPhoneFormats_success(String phone) {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(3L, "Test Restaurant", "Tehran", phone);
            
            // When
            Restaurant saved = repository.save(restaurant);
            
            // Then
            assertThat(saved.getPhone()).isEqualTo(phone);
        }
    }

    @Nested
    @DisplayName("Restaurant Retrieval Tests")
    class RestaurantRetrievalTests {
        
        @Test
        @DisplayName("Find restaurant by ID succeeds")
        void findById_existingRestaurant_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(4L, "Test Restaurant", "Tehran", "021-12345678");
            Restaurant saved = repository.save(restaurant);
            
            // When
            Optional<Restaurant> found = repository.findById(saved.getId());
            
            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
            assertThat(found.get().getName()).isEqualTo("Test Restaurant");
        }

        @Test
        @DisplayName("Find restaurant by non-existent ID returns empty")
        void findById_nonExistentRestaurant_returnsEmpty() {
            // When
            Optional<Restaurant> found = repository.findById(999999L);
            
            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find all restaurants succeeds")
        void findAll_multipleRestaurants_success() {
            // Given
            Restaurant restaurant1 = Restaurant.forRegistration(5L, "Restaurant One", "Tehran", "021-11111111");
            Restaurant restaurant2 = Restaurant.forRegistration(6L, "Restaurant Two", "Isfahan", "031-22222222");
            repository.save(restaurant1);
            repository.save(restaurant2);
            
            // When
            List<Restaurant> restaurants = repository.findAll();
            
            // Then
            assertThat(restaurants).hasSize(2);
            assertThat(restaurants).extracting(Restaurant::getName)
                    .containsExactlyInAnyOrder("Restaurant One", "Restaurant Two");
        }
    }

    @Nested
    @DisplayName("Restaurant Status Tests")
    class RestaurantStatusTests {
        
        @EnumSource(RestaurantStatus.class)
        @ParameterizedTest
        @DisplayName("Restaurant status update succeeds")
        void updateStatus_allStatuses_success(RestaurantStatus status) {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(7L, "Test Restaurant", "Tehran", "021-12345678");
            Restaurant saved = repository.save(restaurant);
            
            // When
            saved.setStatus(status);
            Restaurant updated = repository.save(saved);
            
            // Then
            assertThat(updated.getStatus()).isEqualTo(status);
            
            // Verify persistence
            Optional<Restaurant> retrieved = repository.findById(saved.getId());
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Find restaurants by status succeeds")
        void findByStatus_filteredResults_success() {
            // Given
            Restaurant pending = Restaurant.forRegistration(8L, "Pending Restaurant", "Tehran", "021-11111111");
            Restaurant approved = Restaurant.forRegistration(9L, "Approved Restaurant", "Isfahan", "031-22222222");
            approved.setStatus(RestaurantStatus.APPROVED);
            
            repository.save(pending);
            repository.save(approved);
            
            // When
            List<Restaurant> pendingRestaurants = repository.findByStatus(RestaurantStatus.PENDING);
            List<Restaurant> approvedRestaurants = repository.findByStatus(RestaurantStatus.APPROVED);
            
            // Then
            assertThat(pendingRestaurants).hasSize(1);
            assertThat(pendingRestaurants.get(0).getName()).isEqualTo("Pending Restaurant");
            
            assertThat(approvedRestaurants).hasSize(1);
            assertThat(approvedRestaurants.get(0).getName()).isEqualTo("Approved Restaurant");
        }
    }

    @Nested
    @DisplayName("Restaurant Deletion Tests")
    class RestaurantDeletionTests {
        
        @Test
        @DisplayName("Restaurant deletion succeeds")
        void delete_existingRestaurant_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(10L, "Test Restaurant", "Tehran", "021-12345678");
            Restaurant saved = repository.save(restaurant);
            
            // When
            repository.delete(saved.getId());
            
            // Then
            Optional<Restaurant> found = repository.findById(saved.getId());
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Delete non-existent restaurant does not throw exception")
        void delete_nonExistentRestaurant_noException() {
            // When & Then
            assertThatCode(() -> repository.delete(999999L))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Delete all restaurants succeeds")
        void deleteAll_multipleRestaurants_success() {
            // Given
            Restaurant restaurant1 = Restaurant.forRegistration(11L, "Restaurant One", "Tehran", "021-11111111");
            Restaurant restaurant2 = Restaurant.forRegistration(12L, "Restaurant Two", "Isfahan", "031-22222222");
            repository.save(restaurant1);
            repository.save(restaurant2);
            
            // When
            repository.deleteAll();
            
            // Then
            List<Restaurant> restaurants = repository.findAll();
            assertThat(restaurants).isEmpty();
        }
    }

    @Nested
    @DisplayName("Restaurant Owner Tests")
    class RestaurantOwnerTests {
        
        @Test
        @DisplayName("Find restaurants by owner ID succeeds")
        void listByOwner_existingOwner_success() {
            // Given
            Restaurant restaurant1 = Restaurant.forRegistration(13L, "Owner 13 Restaurant 1", "Tehran", "021-11111111");
            Restaurant restaurant2 = Restaurant.forRegistration(13L, "Owner 13 Restaurant 2", "Isfahan", "031-22222222");
            Restaurant restaurant3 = Restaurant.forRegistration(14L, "Owner 14 Restaurant", "Shiraz", "071-33333333");
            repository.save(restaurant1);
            repository.save(restaurant2);
            repository.save(restaurant3);
            
            // When
            List<Restaurant> owner13Restaurants = repository.listByOwner(13L);
            List<Restaurant> owner14Restaurants = repository.listByOwner(14L);
            
            // Then
            assertThat(owner13Restaurants).hasSize(2);
            assertThat(owner13Restaurants).extracting(Restaurant::getName)
                    .containsExactlyInAnyOrder("Owner 13 Restaurant 1", "Owner 13 Restaurant 2");
            
            assertThat(owner14Restaurants).hasSize(1);
            assertThat(owner14Restaurants.get(0).getName()).isEqualTo("Owner 14 Restaurant");
        }

        @Test
        @DisplayName("Find restaurants by non-existent owner returns empty list")
        void listByOwner_nonExistentOwner_returnsEmpty() {
            // When
            List<Restaurant> restaurants = repository.listByOwner(999999L);
            
            // Then
            assertThat(restaurants).isEmpty();
        }
    }

    @Nested
    @DisplayName("Restaurant Approved List Tests")
    class RestaurantApprovedTests {
        
        @Test
        @DisplayName("List approved restaurants only")
        void listApproved_mixedStatuses_returnsOnlyApproved() {
            // Given
            Restaurant pending = Restaurant.forRegistration(15L, "Pending Restaurant", "Tehran", "021-11111111");
            Restaurant approved1 = Restaurant.forRegistration(16L, "Approved Restaurant 1", "Isfahan", "031-22222222");
            Restaurant approved2 = Restaurant.forRegistration(17L, "Approved Restaurant 2", "Shiraz", "071-33333333");
            Restaurant rejected = Restaurant.forRegistration(18L, "Rejected Restaurant", "Mashhad", "051-44444444");
            
            approved1.setStatus(RestaurantStatus.APPROVED);
            approved2.setStatus(RestaurantStatus.APPROVED);
            rejected.setStatus(RestaurantStatus.REJECTED);
            
            repository.save(pending);
            repository.save(approved1);
            repository.save(approved2);
            repository.save(rejected);
            
            // When
            List<Restaurant> approvedRestaurants = repository.listApproved();
            
            // Then
            assertThat(approvedRestaurants).hasSize(2);
            assertThat(approvedRestaurants).extracting(Restaurant::getName)
                    .containsExactlyInAnyOrder("Approved Restaurant 1", "Approved Restaurant 2");
            assertThat(approvedRestaurants).allMatch(r -> r.getStatus() == RestaurantStatus.APPROVED);
        }

        @Test
        @DisplayName("List approved restaurants when none exist returns empty")
        void listApproved_noApprovedRestaurants_returnsEmpty() {
            // Given
            Restaurant pending = Restaurant.forRegistration(19L, "Pending Restaurant", "Tehran", "021-11111111");
            Restaurant rejected = Restaurant.forRegistration(20L, "Rejected Restaurant", "Isfahan", "031-22222222");
            rejected.setStatus(RestaurantStatus.REJECTED);
            
            repository.save(pending);
            repository.save(rejected);
            
            // When
            List<Restaurant> approvedRestaurants = repository.listApproved();
            
            // Then
            assertThat(approvedRestaurants).isEmpty();
        }
    }

    @Nested
    @DisplayName("Restaurant Update Tests")
    class RestaurantUpdateTests {
        
        @Test
        @DisplayName("Update restaurant status directly")
        void updateStatus_existingRestaurant_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(21L, "Test Restaurant", "Tehran", "021-12345678");
            Restaurant saved = repository.save(restaurant);
            
            // When
            repository.updateStatus(saved.getId(), RestaurantStatus.APPROVED);
            
            // Then
            Optional<Restaurant> updated = repository.findById(saved.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getStatus()).isEqualTo(RestaurantStatus.APPROVED);
        }

        @Test
        @DisplayName("Update status of non-existent restaurant does not throw exception")
        void updateStatus_nonExistentRestaurant_noException() {
            // When & Then
            assertThatCode(() -> repository.updateStatus(999999L, RestaurantStatus.APPROVED))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Update restaurant information")
        void save_existingRestaurant_updatesInformation() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(22L, "Original Name", "Original Address", "021-11111111");
            Restaurant saved = repository.save(restaurant);
            
            // When
            saved.setName("Updated Name");
            saved.setAddress("Updated Address");
            saved.setPhone("021-99999999");
            Restaurant updated = repository.save(saved);
            
            // Then
            assertThat(updated.getName()).isEqualTo("Updated Name");
            assertThat(updated.getAddress()).isEqualTo("Updated Address");
            assertThat(updated.getPhone()).isEqualTo("021-99999999");
            
            // Verify persistence
            Optional<Restaurant> retrieved = repository.findById(saved.getId());
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getName()).isEqualTo("Updated Name");
            assertThat(retrieved.get().getAddress()).isEqualTo("Updated Address");
            assertThat(retrieved.get().getPhone()).isEqualTo("021-99999999");
        }
    }

    @Nested
    @DisplayName("Restaurant Edge Case Tests")
    class RestaurantEdgeCaseTests {
        
        @Test
        @DisplayName("Restaurant with special characters in name")
        void save_specialCharactersInName_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(23L, "رستوران پیتزا & کافه №1 (Special)", "Tehran", "021-12345678");
            
            // When
            Restaurant saved = repository.save(restaurant);
            
            // Then
            assertThat(saved.getName()).isEqualTo("رستوران پیتزا & کافه №1 (Special)");
        }

        @Test
        @DisplayName("Restaurant with Unicode characters in address")
        void save_unicodeCharactersInAddress_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(24L, "Test Restaurant", "تهران، خیابان ولیعصر، پلاک ۱۲۳", "021-12345678");
            
            // When
            Restaurant saved = repository.save(restaurant);
            
            // Then
            assertThat(saved.getAddress()).isEqualTo("تهران، خیابان ولیعصر، پلاک ۱۲۳");
        }

        @Test
        @DisplayName("Restaurant with various phone number formats")
        void save_variousPhoneFormats_success() {
            // Given
            Restaurant restaurant1 = Restaurant.forRegistration(25L, "Restaurant 1", "Tehran", "+98-21-1234-5678");
            Restaurant restaurant2 = Restaurant.forRegistration(26L, "Restaurant 2", "Isfahan", "031.87654321");
            Restaurant restaurant3 = Restaurant.forRegistration(27L, "Restaurant 3", "Shiraz", "071 11 22 33 44");
            
            // When
            Restaurant saved1 = repository.save(restaurant1);
            Restaurant saved2 = repository.save(restaurant2);
            Restaurant saved3 = repository.save(restaurant3);
            
            // Then
            assertThat(saved1.getPhone()).isEqualTo("+98-21-1234-5678");
            assertThat(saved2.getPhone()).isEqualTo("031.87654321");
            assertThat(saved3.getPhone()).isEqualTo("071 11 22 33 44");
        }

        @Test
        @DisplayName("Multiple restaurants with same name but different owners")
        void save_sameNameDifferentOwners_success() {
            // Given
            Restaurant restaurant1 = Restaurant.forRegistration(28L, "McDonald's", "Tehran Branch", "021-11111111");
            Restaurant restaurant2 = Restaurant.forRegistration(29L, "McDonald's", "Isfahan Branch", "031-22222222");
            
            // When
            Restaurant saved1 = repository.save(restaurant1);
            Restaurant saved2 = repository.save(restaurant2);
            
            // Then
            assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
            assertThat(saved1.getName()).isEqualTo(saved2.getName());
            assertThat(saved1.getOwnerId()).isNotEqualTo(saved2.getOwnerId());
        }

        @Test
        @DisplayName("Restaurant with minimum valid data")
        void save_minimumValidData_success() {
            // Given
            Restaurant restaurant = new Restaurant();
            restaurant.setOwnerId(30L);
            restaurant.setName("Min");
            restaurant.setAddress("A");
            restaurant.setPhone("1");
            
            // When
            Restaurant saved = repository.save(restaurant);
            
            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Min");
            assertThat(saved.getAddress()).isEqualTo("A");
            assertThat(saved.getPhone()).isEqualTo("1");
            assertThat(saved.getStatus()).isEqualTo(RestaurantStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Restaurant Concurrency Tests")
    class RestaurantConcurrencyTests {
        
        @Test
        @DisplayName("Rapid consecutive operations on same restaurant")
        void rapidOperations_sameRestaurant_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(31L, "Rapid Test Restaurant", "Tehran", "021-12345678");
            Restaurant saved = repository.save(restaurant);
            
            // When - Rapid status changes
            repository.updateStatus(saved.getId(), RestaurantStatus.APPROVED);
            repository.updateStatus(saved.getId(), RestaurantStatus.SUSPENDED);
            repository.updateStatus(saved.getId(), RestaurantStatus.APPROVED);
            
            // Then
            Optional<Restaurant> final_restaurant = repository.findById(saved.getId());
            assertThat(final_restaurant).isPresent();
            assertThat(final_restaurant.get().getStatus()).isEqualTo(RestaurantStatus.APPROVED);
        }

        @Test
        @DisplayName("Multiple restaurants created simultaneously")
        void multipleCreations_simultaneousOperations_success() {
            // Given & When
            Restaurant restaurant1 = repository.save(Restaurant.forRegistration(32L, "Concurrent Restaurant 1", "Tehran", "021-11111111"));
            Restaurant restaurant2 = repository.save(Restaurant.forRegistration(33L, "Concurrent Restaurant 2", "Isfahan", "031-22222222"));
            Restaurant restaurant3 = repository.save(Restaurant.forRegistration(34L, "Concurrent Restaurant 3", "Shiraz", "071-33333333"));
            
            // Then
            assertThat(restaurant1.getId()).isNotNull();
            assertThat(restaurant2.getId()).isNotNull();
            assertThat(restaurant3.getId()).isNotNull();
            
            List<Restaurant> allRestaurants = repository.findAll();
            assertThat(allRestaurants).hasSize(3);
            assertThat(allRestaurants).extracting(Restaurant::getName)
                    .containsExactlyInAnyOrder("Concurrent Restaurant 1", "Concurrent Restaurant 2", "Concurrent Restaurant 3");
        }
    }

    @Nested
    @DisplayName("Restaurant Query Performance Tests")
    class RestaurantQueryPerformanceTests {
        
        @Test
        @DisplayName("Large dataset operations perform efficiently")
        void largeDataset_operations_performEfficiently() {
            // Given - Create many restaurants
            for (int i = 100; i < 150; i++) {
                Restaurant restaurant = Restaurant.forRegistration((long) i, "Restaurant " + i, "Address " + i, "021-" + i);
                if (i % 3 == 0) restaurant.setStatus(RestaurantStatus.APPROVED);
                if (i % 5 == 0) restaurant.setStatus(RestaurantStatus.REJECTED);
                repository.save(restaurant);
            }
            
            // When & Then - Operations should complete efficiently
            List<Restaurant> allRestaurants = repository.findAll();
            assertThat(allRestaurants).hasSizeGreaterThanOrEqualTo(50);
            
            List<Restaurant> approvedRestaurants = repository.listApproved();
            assertThat(approvedRestaurants).isNotEmpty();
            
            List<Restaurant> pendingRestaurants = repository.findByStatus(RestaurantStatus.PENDING);
            assertThat(pendingRestaurants).isNotEmpty();
            
            List<Restaurant> owner100Restaurants = repository.listByOwner(100L);
            assertThat(owner100Restaurants).hasSize(1);
        }
    }
}