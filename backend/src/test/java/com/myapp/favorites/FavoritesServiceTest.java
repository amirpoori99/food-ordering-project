package com.myapp.favorites;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Favorite;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.models.User;
import com.myapp.restaurant.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("FavoritesService Tests")
public class FavoritesServiceTest {

    @Mock
    private FavoritesRepository mockFavoritesRepository;
    
    @Mock
    private AuthRepository mockAuthRepository;
    
    @Mock
    private RestaurantRepository mockRestaurantRepository;
    
    private FavoritesService favoritesService;
    private User testUser;
    private Restaurant testRestaurant;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        favoritesService = new FavoritesService(mockFavoritesRepository, mockAuthRepository, mockRestaurantRepository);
        
        // Test data setup
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setPhone("1234567890");
        testUser.setEmail("test@example.com");
        
        testRestaurant = new Restaurant();
        testRestaurant.setId(2L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setOwnerId(3L); // Different from testUser.getId()
        testRestaurant.setStatus(RestaurantStatus.APPROVED);
        
        testFavorite = new Favorite(testUser, testRestaurant);
        testFavorite.setId(1L);
        testFavorite.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create service with default constructor")
        void shouldCreateServiceWithDefaultConstructor() {
            FavoritesService service = new FavoritesService();
            assertNotNull(service);
        }
        
        @Test
        @DisplayName("Should create service with dependencies")
        void shouldCreateServiceWithDependencies() {
            FavoritesService service = new FavoritesService(mockFavoritesRepository, mockAuthRepository, mockRestaurantRepository);
            assertNotNull(service);
        }
    }

    @Nested
    @DisplayName("Add Favorite Tests")
    class AddFavoriteTests {
        
        @Test
        @DisplayName("Should add favorite successfully")
        void shouldAddFavoriteSuccessfully() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            when(mockFavoritesRepository.save(any(Favorite.class))).thenReturn(testFavorite);
            
            Favorite result = favoritesService.addFavorite(1L, 2L, "Great food!");
            
            assertNotNull(result);
            verify(mockFavoritesRepository).save(any(Favorite.class));
        }
        
        @Test
        @DisplayName("Should add favorite without notes")
        void shouldAddFavoriteWithoutNotes() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            when(mockFavoritesRepository.save(any(Favorite.class))).thenReturn(testFavorite);
            
            Favorite result = favoritesService.addFavorite(1L, 2L);
            
            assertNotNull(result);
            verify(mockFavoritesRepository).save(any(Favorite.class));
        }
        
        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> 
                favoritesService.addFavorite(1L, 2L, "Notes"));
        }
        
        @Test
        @DisplayName("Should throw exception when restaurant not found")
        void shouldThrowExceptionWhenRestaurantNotFound() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> 
                favoritesService.addFavorite(1L, 2L, "Notes"));
        }
        
        @Test
        @DisplayName("Should throw exception when already favorited")
        void shouldThrowExceptionWhenAlreadyFavorited() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.of(testFavorite));
            
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.addFavorite(1L, 2L, "Notes"));
        }
        
        @Test
        @DisplayName("Should throw exception when user tries to favorite own restaurant")
        void shouldThrowExceptionWhenUserTriesToFavoriteOwnRestaurant() {
            testRestaurant.setOwnerId(1L); // Same as testUser.getId()
            
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.addFavorite(1L, 2L, "Notes"));
        }
        
        @Test
        @DisplayName("Should throw exception for null userId")
        void shouldThrowExceptionForNullUserId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.addFavorite(null, 2L, "Notes"));
        }
        
        @Test
        @DisplayName("Should throw exception for null restaurantId")
        void shouldThrowExceptionForNullRestaurantId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.addFavorite(1L, null, "Notes"));
        }
    }

    @Nested
    @DisplayName("Remove Favorite Tests")
    class RemoveFavoriteTests {
        
        @Test
        @DisplayName("Should remove favorite successfully")
        void shouldRemoveFavoriteSuccessfully() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.of(testFavorite));
            when(mockFavoritesRepository.delete(1L)).thenReturn(true);
            
            boolean result = favoritesService.removeFavorite(1L, 2L);
            
            assertTrue(result);
            verify(mockFavoritesRepository).delete(1L);
        }
        
        @Test
        @DisplayName("Should throw exception when favorite not found")
        void shouldThrowExceptionWhenFavoriteNotFound() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> 
                favoritesService.removeFavorite(1L, 2L));
        }
        
        @Test
        @DisplayName("Should throw exception for null userId")
        void shouldThrowExceptionForNullUserId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.removeFavorite(null, 2L));
        }
        
        @Test
        @DisplayName("Should throw exception for null restaurantId")
        void shouldThrowExceptionForNullRestaurantId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.removeFavorite(1L, null));
        }
    }

    @Nested
    @DisplayName("Update Favorite Notes Tests")
    class UpdateFavoriteNotesTests {
        
        @Test
        @DisplayName("Should update favorite notes successfully")
        void shouldUpdateFavoriteNotesSuccessfully() {
            when(mockFavoritesRepository.findById(1L)).thenReturn(Optional.of(testFavorite));
            when(mockFavoritesRepository.save(testFavorite)).thenReturn(testFavorite);
            
            Favorite result = favoritesService.updateFavoriteNotes(1L, "Updated notes");
            
            assertNotNull(result);
            verify(mockFavoritesRepository).save(testFavorite);
        }
        
        @Test
        @DisplayName("Should throw exception when favorite not found")
        void shouldThrowExceptionWhenFavoriteNotFound() {
            when(mockFavoritesRepository.findById(1L)).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> 
                favoritesService.updateFavoriteNotes(1L, "Notes"));
        }
        
        @Test
        @DisplayName("Should throw exception for null favoriteId")
        void shouldThrowExceptionForNullFavoriteId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.updateFavoriteNotes(null, "Notes"));
        }
    }

    @Nested
    @DisplayName("Get Favorite Tests")
    class GetFavoriteTests {
        
        @Test
        @DisplayName("Should get favorite by ID successfully")
        void shouldGetFavoriteByIdSuccessfully() {
            when(mockFavoritesRepository.findById(1L)).thenReturn(Optional.of(testFavorite));
            
            Favorite result = favoritesService.getFavorite(1L);
            
            assertNotNull(result);
            assertEquals(testFavorite, result);
        }
        
        @Test
        @DisplayName("Should throw exception when favorite not found")
        void shouldThrowExceptionWhenFavoriteNotFound() {
            when(mockFavoritesRepository.findById(1L)).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> 
                favoritesService.getFavorite(1L));
        }
        
        @Test
        @DisplayName("Should throw exception for null favoriteId")
        void shouldThrowExceptionForNullFavoriteId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getFavorite(null));
        }
    }

    @Nested
    @DisplayName("Get User Favorites Tests")
    class GetUserFavoritesTests {
        
        @Test
        @DisplayName("Should get user favorites successfully")
        void shouldGetUserFavoritesSuccessfully() {
            List<Favorite> favorites = Arrays.asList(testFavorite);
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockFavoritesRepository.findByUser(testUser)).thenReturn(favorites);
            
            List<Favorite> result = favoritesService.getUserFavorites(1L);
            
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testFavorite, result.get(0));
        }
        
        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> 
                favoritesService.getUserFavorites(1L));
        }
        
        @Test
        @DisplayName("Should throw exception for null userId")
        void shouldThrowExceptionForNullUserId() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getUserFavorites(null));
        }
    }

    @Nested
    @DisplayName("Check Favorite Tests")
    class CheckFavoriteTests {
        
        @Test
        @DisplayName("Should return true when favorite exists")
        void shouldReturnTrueWhenFavoriteExists() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.of(testFavorite));
            
            boolean result = favoritesService.isFavorite(1L, 2L);
            
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should return false when favorite does not exist")
        void shouldReturnFalseWhenFavoriteDoesNotExist() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.findByUserAndRestaurant(testUser, testRestaurant)).thenReturn(Optional.empty());
            
            boolean result = favoritesService.isFavorite(1L, 2L);
            
            assertFalse(result);
        }
        
        @Test
        @DisplayName("Should return false for null parameters")
        void shouldReturnFalseForNullParameters() {
            assertFalse(favoritesService.isFavorite(null, 2L));
            assertFalse(favoritesService.isFavorite(1L, null));
            assertFalse(favoritesService.isFavorite(null, null));
        }
        
        @Test
        @DisplayName("Should return false when user not found")
        void shouldReturnFalseWhenUserNotFound() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.empty());
            
            boolean result = favoritesService.isFavorite(1L, 2L);
            
            assertFalse(result);
        }
        
        @Test
        @DisplayName("Should return false when restaurant not found")
        void shouldReturnFalseWhenRestaurantNotFound() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.empty());
            
            boolean result = favoritesService.isFavorite(1L, 2L);
            
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {
        
        @Test
        @DisplayName("Should get user favorite stats successfully")
        void shouldGetUserFavoriteStatsSuccessfully() {
            Favorite recentFavorite = new Favorite(testUser, testRestaurant, "Recent notes");
            recentFavorite.setCreatedAt(LocalDateTime.now().minusDays(5));
            
            Favorite oldFavorite = new Favorite(testUser, testRestaurant);
            oldFavorite.setCreatedAt(LocalDateTime.now().minusDays(45));
            
            List<Favorite> favorites = Arrays.asList(recentFavorite, oldFavorite);
            
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockFavoritesRepository.findByUser(testUser)).thenReturn(favorites);
            
            FavoritesService.FavoriteStats stats = favoritesService.getUserFavoriteStats(1L);
            
            assertNotNull(stats);
            assertEquals(2L, stats.getTotalFavorites());
            assertEquals(1L, stats.getFavoritesWithNotes());
            assertEquals(1L, stats.getRecentFavorites());
        }
        
        @Test
        @DisplayName("Should get restaurant favorite count successfully")
        void shouldGetRestaurantFavoriteCountSuccessfully() {
            when(mockRestaurantRepository.findById(2L)).thenReturn(Optional.of(testRestaurant));
            when(mockFavoritesRepository.countByRestaurant(testRestaurant)).thenReturn(5L);
            
            Long count = favoritesService.getRestaurantFavoriteCount(2L);
            
            assertEquals(5L, count);
        }
        
        @Test
        @DisplayName("Should get user favorite count successfully")
        void shouldGetUserFavoriteCountSuccessfully() {
            when(mockAuthRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mockFavoritesRepository.countByUser(testUser)).thenReturn(3L);
            
            Long count = favoritesService.getUserFavoriteCount(1L);
            
            assertEquals(3L, count);
        }
    }

    @Nested
    @DisplayName("Recent Favorites Tests")
    class RecentFavoritesTests {
        
        @Test
        @DisplayName("Should get recent favorites successfully")
        void shouldGetRecentFavoritesSuccessfully() {
            List<Favorite> recentFavorites = Arrays.asList(testFavorite);
            when(mockFavoritesRepository.findRecentFavorites(30)).thenReturn(recentFavorites);
            
            List<Favorite> result = favoritesService.getRecentFavorites(30);
            
            assertNotNull(result);
            assertEquals(1, result.size());
        }
        
        @Test
        @DisplayName("Should throw exception for invalid days")
        void shouldThrowExceptionForInvalidDays() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getRecentFavorites(0));
            
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getRecentFavorites(-1));
        }
    }

    @Nested
    @DisplayName("Favorites With Notes Tests")
    class FavoritesWithNotesTests {
        
        @Test
        @DisplayName("Should get favorites with notes successfully")
        void shouldGetFavoritesWithNotesSuccessfully() {
            List<Favorite> favoritesWithNotes = Arrays.asList(testFavorite);
            when(mockFavoritesRepository.findFavoritesWithNotes()).thenReturn(favoritesWithNotes);
            
            List<Favorite> result = favoritesService.getFavoritesWithNotes();
            
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Admin Operations Tests")
    class AdminOperationsTests {
        
        @Test
        @DisplayName("Should get all favorites successfully")
        void shouldGetAllFavoritesSuccessfully() {
            List<Favorite> allFavorites = Arrays.asList(testFavorite);
            when(mockFavoritesRepository.findAll()).thenReturn(allFavorites);
            
            List<Favorite> result = favoritesService.getAllFavorites();
            
            assertNotNull(result);
            assertEquals(1, result.size());
        }
        
        @Test
        @DisplayName("Should get favorites with pagination successfully")
        void shouldGetFavoritesWithPaginationSuccessfully() {
            List<Favorite> pagedFavorites = Arrays.asList(testFavorite);
            when(mockFavoritesRepository.findWithPagination(0, 10)).thenReturn(pagedFavorites);
            
            List<Favorite> result = favoritesService.getFavoritesWithPagination(0, 10);
            
            assertNotNull(result);
            assertEquals(1, result.size());
        }
        
        @Test
        @DisplayName("Should throw exception for invalid pagination parameters")
        void shouldThrowExceptionForInvalidPaginationParameters() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getFavoritesWithPagination(-1, 10));
            
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.getFavoritesWithPagination(0, 0));
        }
        
        @Test
        @DisplayName("Should delete favorite successfully")
        void shouldDeleteFavoriteSuccessfully() {
            when(mockFavoritesRepository.delete(1L)).thenReturn(true);
            
            boolean result = favoritesService.deleteFavorite(1L);
            
            assertTrue(result);
            verify(mockFavoritesRepository).delete(1L);
        }
        
        @Test
        @DisplayName("Should throw exception for null favoriteId in delete")
        void shouldThrowExceptionForNullFavoriteIdInDelete() {
            assertThrows(IllegalArgumentException.class, () -> 
                favoritesService.deleteFavorite(null));
        }
        
        @Test
        @DisplayName("Should get total favorite count successfully")
        void shouldGetTotalFavoriteCountSuccessfully() {
            when(mockFavoritesRepository.countAll()).thenReturn(10L);
            
            Long count = favoritesService.getTotalFavoriteCount();
            
            assertEquals(10L, count);
        }
    }

    @Nested
    @DisplayName("FavoriteStats Inner Class Tests")
    class FavoriteStatsTests {
        
        @Test
        @DisplayName("Should create FavoriteStats with all values")
        void shouldCreateFavoriteStatsWithAllValues() {
            FavoritesService.FavoriteStats stats = new FavoritesService.FavoriteStats(10L, 5L, 3L);
            
            assertEquals(10L, stats.getTotalFavorites());
            assertEquals(5L, stats.getFavoritesWithNotes());
            assertEquals(3L, stats.getRecentFavorites());
            assertTrue(stats.hasFavorites());
            assertEquals(50.0, stats.getNotesPercentage(), 0.01);
            assertEquals(30.0, stats.getRecentPercentage(), 0.01);
        }
        
        @Test
        @DisplayName("Should handle null values in FavoriteStats")
        void shouldHandleNullValuesInFavoriteStats() {
            FavoritesService.FavoriteStats stats = new FavoritesService.FavoriteStats(null, null, null);
            
            assertEquals(0L, stats.getTotalFavorites());
            assertEquals(0L, stats.getFavoritesWithNotes());
            assertEquals(0L, stats.getRecentFavorites());
            assertFalse(stats.hasFavorites());
            assertEquals(0.0, stats.getNotesPercentage(), 0.01);
            assertEquals(0.0, stats.getRecentPercentage(), 0.01);
        }
        
        @Test
        @DisplayName("Should calculate percentages correctly for zero total")
        void shouldCalculatePercentagesCorrectlyForZeroTotal() {
            FavoritesService.FavoriteStats stats = new FavoritesService.FavoriteStats(0L, 0L, 0L);
            
            assertEquals(0.0, stats.getNotesPercentage(), 0.01);
            assertEquals(0.0, stats.getRecentPercentage(), 0.01);
        }
        
        @Test
        @DisplayName("Should have proper toString method")
        void shouldHaveProperToStringMethod() {
            FavoritesService.FavoriteStats stats = new FavoritesService.FavoriteStats(10L, 5L, 3L);
            
            String result = stats.toString();
            
            assertNotNull(result);
            assertTrue(result.contains("10"));
            assertTrue(result.contains("5"));
            assertTrue(result.contains("3"));
        }
    }
} 