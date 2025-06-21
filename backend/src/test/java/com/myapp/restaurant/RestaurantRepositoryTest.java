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

@DisplayName("RestaurantRepository Tests")
class RestaurantRepositoryTest {

    private RestaurantRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RestaurantRepository();
        // Clean database before each test
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
    }
}