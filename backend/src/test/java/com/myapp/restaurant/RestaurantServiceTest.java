package com.myapp.restaurant;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestaurantServiceTest {

    private RestaurantService restaurantService;
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private RestaurantRepository mockRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Clean up database before each test
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from Restaurant").executeUpdate();
            tx.commit();
        }
        
        restaurantRepository = new RestaurantRepository();
        restaurantService = new RestaurantService(restaurantRepository);
    }

    @AfterEach
    void tearDown() {
        // Clean up database after each test
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from Restaurant").executeUpdate();
            tx.commit();
        }
    }

    // ==================== REGISTRATION TESTS ====================

    @Nested
    @DisplayName("Restaurant Registration Tests")
    class RestaurantRegistrationTests {

        @Test
        @Order(1)
        @DisplayName("Should register restaurant with valid parameters")
        void shouldRegisterRestaurantWithValidParameters() {
            // Given
            Long ownerId = 1L;
            String name = "Test Restaurant";
            String address = "123 Test Street";
            String phone = "123-456-7890";

            // When
            Restaurant result = restaurantService.registerRestaurant(ownerId, name, address, phone);

            // Then
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(ownerId, result.getOwnerId());
            assertEquals(name, result.getName());
            assertEquals(address, result.getAddress());
            assertEquals(phone, result.getPhone());
            assertEquals(RestaurantStatus.PENDING, result.getStatus());
        }

        @Test
        @Order(2)
        @DisplayName("Should register restaurant with Restaurant object")
        void shouldRegisterRestaurantWithRestaurantObject() {
            // Given
            Restaurant restaurant = new Restaurant(1L, "Test Restaurant", "123 Test Street", "123-456-7890");

            // When
            Restaurant result = restaurantService.registerRestaurant(restaurant);

            // Then
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(1L, result.getOwnerId());
            assertEquals("Test Restaurant", result.getName());
            assertEquals("123 Test Street", result.getAddress());
            assertEquals("123-456-7890", result.getPhone());
            assertEquals(RestaurantStatus.PENDING, result.getStatus());
        }

        @Test
        @Order(3)
        @DisplayName("Should trim whitespace during registration")
        void shouldTrimWhitespaceInRegistration() {
            // Given
            Long ownerId = 1L;
            String name = "  Test Restaurant  ";
            String address = "  123 Test Street  ";
            String phone = "  123-456-7890  ";

            // When
            Restaurant result = restaurantService.registerRestaurant(ownerId, name, address, phone);

            // Then
            assertEquals("Test Restaurant", result.getName());
            assertEquals("123 Test Street", result.getAddress());
            assertEquals("123-456-7890", result.getPhone());
        }

        @Test
        @Order(4)
        @DisplayName("Should set default status to PENDING")
        void shouldSetDefaultStatusToPending() {
            // Given
            Restaurant restaurant = new Restaurant(1L, "Test Restaurant", "123 Test Street", "123-456-7890");
            restaurant.setStatus(null);

            // When
            Restaurant result = restaurantService.registerRestaurant(restaurant);

            // Then
            assertEquals(RestaurantStatus.PENDING, result.getStatus());
        }
    }

    // ==================== REGISTRATION VALIDATION TESTS ====================

    @Nested
    @DisplayName("Registration Input Validation Tests")
    class RegistrationValidationTests {

        @Test
        @DisplayName("Should throw exception for null owner ID")
        void shouldThrowExceptionForNullOwnerId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(null, "Test", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for zero owner ID")
        void shouldThrowExceptionForZeroOwnerId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(0L, "Test", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for negative owner ID")
        void shouldThrowExceptionForNegativeOwnerId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(-1L, "Test", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for null restaurant name")
        void shouldThrowExceptionForNullRestaurantName() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, null, "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for empty restaurant name")
        void shouldThrowExceptionForEmptyRestaurantName() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only restaurant name")
        void shouldThrowExceptionForWhitespaceOnlyRestaurantName() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "   ", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for restaurant name exceeding 100 characters")
        void shouldThrowExceptionForLongRestaurantName() {
            String longName = "a".repeat(101);
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, longName, "Address", "Phone"));
        }

        @Test
        @DisplayName("Should accept restaurant name with exactly 100 characters")
        void shouldAcceptRestaurantNameWith100Characters() {
            String name100 = "a".repeat(100);
            assertDoesNotThrow(() ->
                restaurantService.registerRestaurant(1L, name100, "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for null address")
        void shouldThrowExceptionForNullAddress() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", null, "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for empty address")
        void shouldThrowExceptionForEmptyAddress() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", "", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for address exceeding 200 characters")
        void shouldThrowExceptionForLongAddress() {
            String longAddress = "a".repeat(201);
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", longAddress, "Phone"));
        }

        @Test
        @DisplayName("Should accept address with exactly 200 characters")
        void shouldAcceptAddressWith200Characters() {
            String address200 = "a".repeat(200);
            assertDoesNotThrow(() ->
                restaurantService.registerRestaurant(1L, "Test", address200, "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for null phone")
        void shouldThrowExceptionForNullPhone() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", "Address", null));
        }

        @Test
        @DisplayName("Should throw exception for empty phone")
        void shouldThrowExceptionForEmptyPhone() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", "Address", ""));
        }

        @Test
        @DisplayName("Should throw exception for phone exceeding 20 characters")
        void shouldThrowExceptionForLongPhone() {
            String longPhone = "1".repeat(21);
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant(1L, "Test", "Address", longPhone));
        }

        @Test
        @DisplayName("Should accept phone with exactly 20 characters")
        void shouldAcceptPhoneWith20Characters() {
            String phone20 = "1".repeat(20);
            assertDoesNotThrow(() ->
                restaurantService.registerRestaurant(1L, "Test", "Address", phone20));
        }

        @Test
        @DisplayName("Should throw exception for null restaurant object")
        void shouldThrowExceptionForNullRestaurantObject() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.registerRestaurant((Restaurant) null));
        }
    }

    // ==================== RETRIEVAL TESTS ====================

    @Nested
    @DisplayName("Restaurant Retrieval Tests")
    class RestaurantRetrievalTests {

        @Test
        @DisplayName("Should get restaurant by valid ID")
        void shouldGetRestaurantByValidId() {
            // Given
            Restaurant saved = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            Restaurant result = restaurantService.getRestaurantById(saved.getId());

            // Then
            assertNotNull(result);
            assertEquals(saved.getId(), result.getId());
            assertEquals("Test Restaurant", result.getName());
        }

        @Test
        @DisplayName("Should throw exception for null ID")
        void shouldThrowExceptionForNullId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.getRestaurantById(null));
        }

        @Test
        @DisplayName("Should throw exception for zero ID")
        void shouldThrowExceptionForZeroId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.getRestaurantById(0L));
        }

        @Test
        @DisplayName("Should throw exception for negative ID")
        void shouldThrowExceptionForNegativeId() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.getRestaurantById(-1L));
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent ID")
        void shouldThrowNotFoundExceptionForNonExistentId() {
            assertThrows(NotFoundException.class, () ->
                restaurantService.getRestaurantById(999L));
        }

        @Test
        @DisplayName("Should get restaurants by owner")
        void shouldGetRestaurantsByOwner() {
            // Given
            restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            restaurantService.registerRestaurant(1L, "Restaurant 2", "Address 2", "Phone 2");
            restaurantService.registerRestaurant(2L, "Restaurant 3", "Address 3", "Phone 3");

            // When
            List<Restaurant> result = restaurantService.getRestaurantsByOwner(1L);

            // Then
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(r -> r.getOwnerId().equals(1L)));
        }

        @Test
        @DisplayName("Should return empty list for owner with no restaurants")
        void shouldReturnEmptyListForOwnerWithNoRestaurants() {
            // When
            List<Restaurant> result = restaurantService.getRestaurantsByOwner(999L);

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for null owner ID in getRestaurantsByOwner")
        void shouldThrowExceptionForNullOwnerIdInGetRestaurantsByOwner() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.getRestaurantsByOwner(null));
        }

        @Test
        @DisplayName("Should get all restaurants")
        void shouldGetAllRestaurants() {
            // Given
            restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");

            // When
            List<Restaurant> result = restaurantService.getAllRestaurants();

            // Then
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should get approved restaurants only")
        void shouldGetApprovedRestaurantsOnly() {
            // Given
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");
            
            restaurantService.updateRestaurantStatus(restaurant1.getId(), RestaurantStatus.APPROVED);
            restaurantService.updateRestaurantStatus(restaurant2.getId(), RestaurantStatus.REJECTED);

            // When
            List<Restaurant> result = restaurantService.getApprovedRestaurants();

            // Then
            assertEquals(1, result.size());
            assertEquals(RestaurantStatus.APPROVED, result.get(0).getStatus());
        }

        @Test
        @DisplayName("Should get restaurants by status")
        void shouldGetRestaurantsByStatus() {
            // Given
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");
            
            restaurantService.updateRestaurantStatus(restaurant1.getId(), RestaurantStatus.APPROVED);

            // When
            List<Restaurant> pendingRestaurants = restaurantService.getRestaurantsByStatus(RestaurantStatus.PENDING);
            List<Restaurant> approvedRestaurants = restaurantService.getRestaurantsByStatus(RestaurantStatus.APPROVED);

            // Then
            assertEquals(1, pendingRestaurants.size());
            assertEquals(1, approvedRestaurants.size());
        }

        @Test
        @DisplayName("Should throw exception for null status in getRestaurantsByStatus")
        void shouldThrowExceptionForNullStatusInGetRestaurantsByStatus() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.getRestaurantsByStatus(null));
        }
    }

    // ==================== STATUS UPDATE TESTS ====================

    @Nested
    @DisplayName("Restaurant Status Update Tests")
    class StatusUpdateTests {

        @Test
        @DisplayName("Should update restaurant status")
        void shouldUpdateRestaurantStatus() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            restaurantService.updateRestaurantStatus(restaurant.getId(), RestaurantStatus.APPROVED);

            // Then
            Restaurant updated = restaurantService.getRestaurantById(restaurant.getId());
            assertEquals(RestaurantStatus.APPROVED, updated.getStatus());
        }

        @Test
        @DisplayName("Should approve restaurant")
        void shouldApproveRestaurant() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            restaurantService.approveRestaurant(restaurant.getId());

            // Then
            Restaurant updated = restaurantService.getRestaurantById(restaurant.getId());
            assertEquals(RestaurantStatus.APPROVED, updated.getStatus());
        }

        @Test
        @DisplayName("Should reject restaurant")
        void shouldRejectRestaurant() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            restaurantService.rejectRestaurant(restaurant.getId());

            // Then
            Restaurant updated = restaurantService.getRestaurantById(restaurant.getId());
            assertEquals(RestaurantStatus.REJECTED, updated.getStatus());
        }

        @Test
        @DisplayName("Should suspend restaurant")
        void shouldSuspendRestaurant() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            restaurantService.suspendRestaurant(restaurant.getId());

            // Then
            Restaurant updated = restaurantService.getRestaurantById(restaurant.getId());
            assertEquals(RestaurantStatus.SUSPENDED, updated.getStatus());
        }

        @Test
        @DisplayName("Should throw exception for null ID in status update")
        void shouldThrowExceptionForNullIdInStatusUpdate() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurantStatus(null, RestaurantStatus.APPROVED));
        }

        @Test
        @DisplayName("Should throw exception for null status in status update")
        void shouldThrowExceptionForNullStatusInStatusUpdate() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurantStatus(1L, null));
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent restaurant in status update")
        void shouldThrowNotFoundExceptionForNonExistentRestaurantInStatusUpdate() {
            assertThrows(NotFoundException.class, () ->
                restaurantService.updateRestaurantStatus(999L, RestaurantStatus.APPROVED));
        }
    }

    // ==================== RESTAURANT UPDATE TESTS ====================

    @Nested
    @DisplayName("Restaurant Update Tests")
    class RestaurantUpdateTests {

        @Test
        @DisplayName("Should update restaurant with individual parameters")
        void shouldUpdateRestaurantWithIndividualParameters() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");

            // When
            Restaurant updated = restaurantService.updateRestaurant(restaurant.getId(), "New Name", "New Address", "New Phone");

            // Then
            assertEquals("New Name", updated.getName());
            assertEquals("New Address", updated.getAddress());
            assertEquals("New Phone", updated.getPhone());
        }

        @Test
        @DisplayName("Should update restaurant with Restaurant object")
        void shouldUpdateRestaurantWithRestaurantObject() {
            // Given
            Restaurant original = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");
            Restaurant updateData = new Restaurant();
            updateData.setId(original.getId());
            updateData.setName("Updated Name");
            updateData.setAddress("Updated Address");
            updateData.setPhone("Updated Phone");

            // When
            Restaurant updated = restaurantService.updateRestaurant(updateData);

            // Then
            assertEquals("Updated Name", updated.getName());
            assertEquals("Updated Address", updated.getAddress());
            assertEquals("Updated Phone", updated.getPhone());
        }

        @Test
        @DisplayName("Should update only name when other fields are null")
        void shouldUpdateOnlyNameWhenOtherFieldsAreNull() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");

            // When
            Restaurant updated = restaurantService.updateRestaurant(restaurant.getId(), "New Name", null, null);

            // Then
            assertEquals("New Name", updated.getName());
            assertEquals("Original Address", updated.getAddress());
            assertEquals("Original Phone", updated.getPhone());
        }

        @Test
        @DisplayName("Should not update fields with empty strings")
        void shouldNotUpdateFieldsWithEmptyStrings() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");

            // When
            Restaurant updated = restaurantService.updateRestaurant(restaurant.getId(), "", "", "");

            // Then
            assertEquals("Original Name", updated.getName());
            assertEquals("Original Address", updated.getAddress());
            assertEquals("Original Phone", updated.getPhone());
        }

        @Test
        @DisplayName("Should trim whitespace in updates")
        void shouldTrimWhitespaceInUpdates() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");

            // When
            Restaurant updated = restaurantService.updateRestaurant(restaurant.getId(), "  New Name  ", "  New Address  ", "  New Phone  ");

            // Then
            assertEquals("New Name", updated.getName());
            assertEquals("New Address", updated.getAddress());
            assertEquals("New Phone", updated.getPhone());
        }

        @Test
        @DisplayName("Should throw exception for null ID in update")
        void shouldThrowExceptionForNullIdInUpdate() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant((Long) null, "Name", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for null restaurant object in update")
        void shouldThrowExceptionForNullRestaurantObjectInUpdate() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant((Restaurant) null));
        }

        @Test
        @DisplayName("Should throw exception for restaurant object with null ID")
        void shouldThrowExceptionForRestaurantObjectWithNullId() {
            Restaurant restaurant = new Restaurant();
            restaurant.setName("Test");
            
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant(restaurant));
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent restaurant in update")
        void shouldThrowNotFoundExceptionForNonExistentRestaurantInUpdate() {
            assertThrows(NotFoundException.class, () ->
                restaurantService.updateRestaurant(999L, "Name", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for name exceeding 100 characters in update")
        void shouldThrowExceptionForLongNameInUpdate() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");
            String longName = "a".repeat(101);

            // Then
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant(restaurant.getId(), longName, "Address", "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for address exceeding 200 characters in update")
        void shouldThrowExceptionForLongAddressInUpdate() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");
            String longAddress = "a".repeat(201);

            // Then
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant(restaurant.getId(), "Name", longAddress, "Phone"));
        }

        @Test
        @DisplayName("Should throw exception for phone exceeding 20 characters in update")
        void shouldThrowExceptionForLongPhoneInUpdate() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Original Name", "Original Address", "Original Phone");
            String longPhone = "1".repeat(21);

            // Then
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.updateRestaurant(restaurant.getId(), "Name", "Address", longPhone));
        }
    }

    // ==================== DELETE TESTS ====================

    @Nested
    @DisplayName("Restaurant Delete Tests")
    class RestaurantDeleteTests {

        @Test
        @DisplayName("Should delete existing restaurant")
        void shouldDeleteExistingRestaurant() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            restaurantService.deleteRestaurant(restaurant.getId());

            // Then
            assertThrows(NotFoundException.class, () ->
                restaurantService.getRestaurantById(restaurant.getId()));
        }

        @Test
        @DisplayName("Should throw exception for null ID in delete")
        void shouldThrowExceptionForNullIdInDelete() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.deleteRestaurant(null));
        }

        @Test
        @DisplayName("Should throw exception for zero ID in delete")
        void shouldThrowExceptionForZeroIdInDelete() {
            assertThrows(IllegalArgumentException.class, () ->
                restaurantService.deleteRestaurant(0L));
        }

        @Test
        @DisplayName("Should throw NotFoundException for non-existent restaurant in delete")
        void shouldThrowNotFoundExceptionForNonExistentRestaurantInDelete() {
            assertThrows(NotFoundException.class, () ->
                restaurantService.deleteRestaurant(999L));
        }
    }

    // ==================== EXISTENCE CHECK TESTS ====================

    @Nested
    @DisplayName("Restaurant Existence Check Tests")
    class RestaurantExistenceTests {

        @Test
        @DisplayName("Should return true for existing restaurant")
        void shouldReturnTrueForExistingRestaurant() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");

            // When
            boolean exists = restaurantService.existsById(restaurant.getId());

            // Then
            assertTrue(exists);
        }

        @Test
        @DisplayName("Should return false for non-existent restaurant")
        void shouldReturnFalseForNonExistentRestaurant() {
            // When
            boolean exists = restaurantService.existsById(999L);

            // Then
            assertFalse(exists);
        }

        @Test
        @DisplayName("Should return false for null ID")
        void shouldReturnFalseForNullId() {
            // When
            boolean exists = restaurantService.existsById(null);

            // Then
            assertFalse(exists);
        }

        @Test
        @DisplayName("Should return false for zero ID")
        void shouldReturnFalseForZeroId() {
            // When
            boolean exists = restaurantService.existsById(0L);

            // Then
            assertFalse(exists);
        }

        @Test
        @DisplayName("Should return false for negative ID")
        void shouldReturnFalseForNegativeId() {
            // When
            boolean exists = restaurantService.existsById(-1L);

            // Then
            assertFalse(exists);
        }
    }

    // ==================== PENDING RESTAURANTS TESTS ====================

    @Nested
    @DisplayName("Pending Restaurants Tests")
    class PendingRestaurantsTests {

        @Test
        @DisplayName("Should get pending restaurants")
        void shouldGetPendingRestaurants() {
            // Given
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");
            restaurantService.updateRestaurantStatus(restaurant1.getId(), RestaurantStatus.APPROVED);

            // When
            List<Restaurant> pendingRestaurants = restaurantService.getPendingRestaurants();

            // Then
            assertEquals(1, pendingRestaurants.size());
            assertEquals(RestaurantStatus.PENDING, pendingRestaurants.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return empty list when no pending restaurants")
        void shouldReturnEmptyListWhenNoPendingRestaurants() {
            // Given
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Restaurant", "Address", "Phone");
            restaurantService.updateRestaurantStatus(restaurant.getId(), RestaurantStatus.APPROVED);

            // When
            List<Restaurant> pendingRestaurants = restaurantService.getPendingRestaurants();

            // Then
            assertTrue(pendingRestaurants.isEmpty());
        }
    }

    // ==================== STATISTICS TESTS ====================

    @Nested
    @DisplayName("Restaurant Statistics Tests")
    class RestaurantStatisticsTests {

        @Test
        @DisplayName("Should calculate restaurant statistics correctly")
        void shouldCalculateRestaurantStatisticsCorrectly() {
            // Given
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");
            Restaurant restaurant3 = restaurantService.registerRestaurant(3L, "Restaurant 3", "Address 3", "Phone 3");
            Restaurant restaurant4 = restaurantService.registerRestaurant(4L, "Restaurant 4", "Address 4", "Phone 4");

            restaurantService.updateRestaurantStatus(restaurant1.getId(), RestaurantStatus.APPROVED);
            restaurantService.updateRestaurantStatus(restaurant2.getId(), RestaurantStatus.APPROVED);
            restaurantService.updateRestaurantStatus(restaurant3.getId(), RestaurantStatus.REJECTED);
            // restaurant4 remains PENDING

            // When
            RestaurantService.RestaurantStatistics stats = restaurantService.getRestaurantStatistics();

            // Then
            assertEquals(4, stats.getTotalCount());
            assertEquals(2, stats.getApprovedCount());
            assertEquals(1, stats.getPendingCount());
            assertEquals(1, stats.getRejectedCount());
            assertEquals(0, stats.getSuspendedCount());
        }

        @Test
        @DisplayName("Should return zero statistics when no restaurants")
        void shouldReturnZeroStatisticsWhenNoRestaurants() {
            // When
            RestaurantService.RestaurantStatistics stats = restaurantService.getRestaurantStatistics();

            // Then
            assertEquals(0, stats.getTotalCount());
            assertEquals(0, stats.getApprovedCount());
            assertEquals(0, stats.getPendingCount());
            assertEquals(0, stats.getRejectedCount());
            assertEquals(0, stats.getSuspendedCount());
        }

        @Test
        @DisplayName("Should include suspended restaurants in statistics")
        void shouldIncludeSuspendedRestaurantsInStatistics() {
            // Given
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, "Restaurant 1", "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, "Restaurant 2", "Address 2", "Phone 2");

            restaurantService.updateRestaurantStatus(restaurant1.getId(), RestaurantStatus.APPROVED);
            restaurantService.updateRestaurantStatus(restaurant2.getId(), RestaurantStatus.SUSPENDED);

            // When
            RestaurantService.RestaurantStatistics stats = restaurantService.getRestaurantStatistics();

            // Then
            assertEquals(2, stats.getTotalCount());
            assertEquals(1, stats.getApprovedCount());
            assertEquals(0, stats.getPendingCount());
            assertEquals(0, stats.getRejectedCount());
            assertEquals(1, stats.getSuspendedCount());
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle special characters in restaurant name")
        void shouldHandleSpecialCharactersInRestaurantName() {
            // Given
            String specialName = "رستوران تست & Café №1 (Special)";

            // When
            Restaurant result = restaurantService.registerRestaurant(1L, specialName, "Address", "Phone");

            // Then
            assertEquals(specialName, result.getName());
        }

        @Test
        @DisplayName("Should handle Unicode characters in address")
        void shouldHandleUnicodeCharactersInAddress() {
            // Given
            String unicodeAddress = "تهران، خیابان ولیعصر، پلاک ۱۲۳";

            // When
            Restaurant result = restaurantService.registerRestaurant(1L, "Test", unicodeAddress, "Phone");

            // Then
            assertEquals(unicodeAddress, result.getAddress());
        }

        @Test
        @DisplayName("Should handle phone numbers with various formats")
        void shouldHandlePhoneNumbersWithVariousFormats() {
            // Given
            String formattedPhone = "+98-21-1234-5678";

            // When
            Restaurant result = restaurantService.registerRestaurant(1L, "Test", "Address", formattedPhone);

            // Then
            assertEquals(formattedPhone, result.getPhone());
        }

        @Test
        @DisplayName("Should handle maximum length fields")
        void shouldHandleMaximumLengthFields() {
            // Given
            String maxName = "a".repeat(100);
            String maxAddress = "b".repeat(200);
            String maxPhone = "1".repeat(20);

            // When
            Restaurant result = restaurantService.registerRestaurant(1L, maxName, maxAddress, maxPhone);

            // Then
            assertEquals(maxName, result.getName());
            assertEquals(maxAddress, result.getAddress());
            assertEquals(maxPhone, result.getPhone());
        }

        @Test
        @DisplayName("Should handle multiple restaurants with same name but different owners")
        void shouldHandleMultipleRestaurantsWithSameNameButDifferentOwners() {
            // Given
            String sameName = "McDonald's";

            // When
            Restaurant restaurant1 = restaurantService.registerRestaurant(1L, sameName, "Address 1", "Phone 1");
            Restaurant restaurant2 = restaurantService.registerRestaurant(2L, sameName, "Address 2", "Phone 2");

            // Then
            assertNotEquals(restaurant1.getId(), restaurant2.getId());
            assertEquals(sameName, restaurant1.getName());
            assertEquals(sameName, restaurant2.getName());
            assertNotEquals(restaurant1.getOwnerId(), restaurant2.getOwnerId());
        }

        @Test
        @DisplayName("Should handle rapid consecutive operations")
        void shouldHandleRapidConsecutiveOperations() {
            // Given & When
            Restaurant restaurant = restaurantService.registerRestaurant(1L, "Test Restaurant", "Address", "Phone");
            
            // Rapid status changes
            restaurantService.updateRestaurantStatus(restaurant.getId(), RestaurantStatus.APPROVED);
            restaurantService.updateRestaurantStatus(restaurant.getId(), RestaurantStatus.SUSPENDED);
            restaurantService.updateRestaurantStatus(restaurant.getId(), RestaurantStatus.APPROVED);

            // Then
            Restaurant final_restaurant = restaurantService.getRestaurantById(restaurant.getId());
            assertEquals(RestaurantStatus.APPROVED, final_restaurant.getStatus());
        }
    }

    // ==================== MOCK TESTS ====================

    @Nested
    @DisplayName("Mock Repository Tests")
    class MockRepositoryTests {

        private RestaurantService mockRestaurantService;

        @BeforeEach
        void setUpMocks() {
            mockRestaurantService = new RestaurantService(mockRepository);
        }

        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Given
            when(mockRepository.saveNew(any(Restaurant.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThrows(RuntimeException.class, () ->
                mockRestaurantService.registerRestaurant(1L, "Test", "Address", "Phone"));
        }

        @Test
        @DisplayName("Should call repository methods with correct parameters")
        void shouldCallRepositoryMethodsWithCorrectParameters() {
            // Given
            Restaurant restaurant = new Restaurant(1L, "Test", "Address", "Phone");
            when(mockRepository.saveNew(any(Restaurant.class))).thenReturn(restaurant);

            // When
            mockRestaurantService.registerRestaurant(1L, "Test", "Address", "Phone");

            // Then
            verify(mockRepository, times(1)).saveNew(any(Restaurant.class));
        }

        @Test
        @DisplayName("Should handle empty optional from repository")
        void shouldHandleEmptyOptionalFromRepository() {
            // Given
            when(mockRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(NotFoundException.class, () ->
                mockRestaurantService.getRestaurantById(1L));
        }
    }
}
