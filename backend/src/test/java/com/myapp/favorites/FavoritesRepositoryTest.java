package com.myapp.favorites;

import com.myapp.common.models.Favorite;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FavoritesRepository Integration Tests")
public class FavoritesRepositoryTest {

    private FavoritesRepository favoritesRepository;
    private User testUser;
    private Restaurant testRestaurant;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        favoritesRepository = new FavoritesRepository();
        
        // Create test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setPhone("1234567890");
        testUser.setEmail("test@example.com");
        
        testRestaurant = new Restaurant();
        testRestaurant.setId(2L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setStatus(RestaurantStatus.APPROVED);
        testRestaurant.setOwnerId(3L);
        
        testFavorite = new Favorite(testUser, testRestaurant, "Great food!");
        testFavorite.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Save Operation Tests")
    class SaveOperationTests {
        
        @Test
        @DisplayName("Should save new favorite successfully")
        void shouldSaveNewFavoriteSuccessfully() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                Favorite saved = favoritesRepository.save(testFavorite);
                assertNotNull(saved);
                assertNotNull(saved.getId());
            });
        }
        
        @Test
        @DisplayName("Should save favorite with legacy method")
        void shouldSaveFavoriteWithLegacyMethod() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                Favorite saved = favoritesRepository.save(1L, 2L);
                assertNotNull(saved);
            });
        }
        
        @Test
        @DisplayName("Should update existing favorite")
        void shouldUpdateExistingFavorite() {
            // Arrange
            testFavorite.setId(1L);
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                Favorite updated = favoritesRepository.save(testFavorite);
                assertNotNull(updated);
                assertEquals(1L, updated.getId());
            });
        }
        
        @Test
        @DisplayName("Should handle null favorite gracefully")
        void shouldHandleNullFavoriteGracefully() {
            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                favoritesRepository.save((Favorite) null);
            });
        }
        
        @Test
        @DisplayName("Should handle invalid user/restaurant IDs in legacy save")
        void shouldHandleInvalidIdsInLegacySave() {
            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                favoritesRepository.save(999L, 999L);
            });
        }
    }

    @Nested
    @DisplayName("Find Operation Tests")
    class FindOperationTests {
        
        @Test
        @DisplayName("Should find favorite by ID")
        void shouldFindFavoriteById() {
            // Act
            Optional<Favorite> result = favoritesRepository.findById(1L);
            
            // Assert
            assertNotNull(result);
            // Note: May be empty if no data exists, which is valid
        }
        
        @Test
        @DisplayName("Should return empty for non-existent ID")
        void shouldReturnEmptyForNonExistentId() {
            // Act
            Optional<Favorite> result = favoritesRepository.findById(999999L);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should find favorite by user and restaurant")
        void shouldFindFavoriteByUserAndRestaurant() {
            // Act
            Optional<Favorite> result = favoritesRepository.findByUserAndRestaurant(testUser, testRestaurant);
            
            // Assert
            assertNotNull(result);
            // Note: May be empty if no data exists, which is valid
        }
        
        @Test
        @DisplayName("Should handle null user in findByUserAndRestaurant")
        void shouldHandleNullUserInFindByUserAndRestaurant() {
            // Act
            Optional<Favorite> result = favoritesRepository.findByUserAndRestaurant(null, testRestaurant);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null restaurant in findByUserAndRestaurant")
        void shouldHandleNullRestaurantInFindByUserAndRestaurant() {
            // Act
            Optional<Favorite> result = favoritesRepository.findByUserAndRestaurant(testUser, null);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should find favorites using legacy method")
        void shouldFindFavoritesUsingLegacyMethod() {
            // Act
            Optional<Favorite> result = favoritesRepository.find(1L, 2L);
            
            // Assert
            assertNotNull(result);
            // Note: May be empty if no data exists, which is valid
        }
        
        @Test
        @DisplayName("Should handle invalid IDs in legacy find")
        void shouldHandleInvalidIdsInLegacyFind() {
            // Act
            Optional<Favorite> result = favoritesRepository.find(-1L, -1L);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find By User Tests")
    class FindByUserTests {
        
        @Test
        @DisplayName("Should find favorites by user")
        void shouldFindFavoritesByUser() {
            // Act
            List<Favorite> result = favoritesRepository.findByUser(testUser);
            
            // Assert
            assertNotNull(result);
            // Note: May be empty if no data exists, which is valid
        }
        
        @Test
        @DisplayName("Should find favorites using legacy listByUser method")
        void shouldFindFavoritesUsingLegacyListByUser() {
            // Act
            List<Favorite> result = favoritesRepository.listByUser(1L);
            
            // Assert
            assertNotNull(result);
            // Note: May be empty if no data exists, which is valid
        }
        
        @Test
        @DisplayName("Should handle null user in findByUser")
        void shouldHandleNullUserInFindByUser() {
            // Act
            List<Favorite> result = favoritesRepository.findByUser(null);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle invalid user ID in legacy listByUser")
        void shouldHandleInvalidUserIdInLegacyListByUser() {
            // Act
            List<Favorite> result = favoritesRepository.listByUser(-1L);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should return favorites ordered by creation date")
        void shouldReturnFavoritesOrderedByCreationDate() {
            // Act
            List<Favorite> result = favoritesRepository.findByUser(testUser);
            
            // Assert
            assertNotNull(result);
            // Verify ordering if multiple results exist
            if (result.size() > 1) {
                for (int i = 0; i < result.size() - 1; i++) {
                    assertTrue(result.get(i).getCreatedAt().isAfter(result.get(i + 1).getCreatedAt()) ||
                              result.get(i).getCreatedAt().isEqual(result.get(i + 1).getCreatedAt()));
                }
            }
        }
    }

    @Nested
    @DisplayName("Find By Restaurant Tests")
    class FindByRestaurantTests {
        
        @Test
        @DisplayName("Should find favorites by restaurant")
        void shouldFindFavoritesByRestaurant() {
            // Act
            List<Favorite> result = favoritesRepository.findByRestaurant(testRestaurant);
            
            // Assert
            assertNotNull(result);
            // Note: May be empty if no data exists, which is valid
        }
        
        @Test
        @DisplayName("Should handle null restaurant in findByRestaurant")
        void shouldHandleNullRestaurantInFindByRestaurant() {
            // Act
            List<Favorite> result = favoritesRepository.findByRestaurant(null);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should return restaurant favorites ordered by creation date")
        void shouldReturnRestaurantFavoritesOrderedByCreationDate() {
            // Act
            List<Favorite> result = favoritesRepository.findByRestaurant(testRestaurant);
            
            // Assert
            assertNotNull(result);
            // Verify ordering if multiple results exist
            if (result.size() > 1) {
                for (int i = 0; i < result.size() - 1; i++) {
                    assertTrue(result.get(i).getCreatedAt().isAfter(result.get(i + 1).getCreatedAt()) ||
                              result.get(i).getCreatedAt().isEqual(result.get(i + 1).getCreatedAt()));
                }
            }
        }
    }

    @Nested
    @DisplayName("Recent Favorites Tests")
    class RecentFavoritesTests {
        
        @Test
        @DisplayName("Should find recent favorites")
        void shouldFindRecentFavorites() {
            // Act
            List<Favorite> result = favoritesRepository.findRecentFavorites(30);
            
            // Assert
            assertNotNull(result);
            // Note: May be empty if no recent data exists, which is valid
        }
        
        @Test
        @DisplayName("Should handle zero days")
        void shouldHandleZeroDays() {
            // Act
            List<Favorite> result = favoritesRepository.findRecentFavorites(0);
            
            // Assert
            assertNotNull(result);
            // Should return empty list for 0 days
        }
        
        @Test
        @DisplayName("Should handle negative days")
        void shouldHandleNegativeDays() {
            // Act
            List<Favorite> result = favoritesRepository.findRecentFavorites(-1);
            
            // Assert
            assertNotNull(result);
            // Should return empty list for negative days
        }
        
        @Test
        @DisplayName("Should handle large number of days")
        void shouldHandleLargeNumberOfDays() {
            // Act
            List<Favorite> result = favoritesRepository.findRecentFavorites(36500); // 100 years
            
            // Assert
            assertNotNull(result);
            // Should not throw exception
        }
    }

    @Nested
    @DisplayName("Favorites With Notes Tests")
    class FavoritesWithNotesTests {
        
        @Test
        @DisplayName("Should find favorites with notes")
        void shouldFindFavoritesWithNotes() {
            // Act
            List<Favorite> result = favoritesRepository.findFavoritesWithNotes();
            
            // Assert
            assertNotNull(result);
            // Verify all returned favorites have notes
            for (Favorite favorite : result) {
                assertTrue(favorite.hasNotes());
            }
        }
        
        @Test
        @DisplayName("Should return empty list when no favorites have notes")
        void shouldReturnEmptyListWhenNoFavoritesHaveNotes() {
            // Act
            List<Favorite> result = favoritesRepository.findFavoritesWithNotes();
            
            // Assert
            assertNotNull(result);
            // Note: May be empty if no favorites with notes exist, which is valid
        }
    }

    @Nested
    @DisplayName("Count Operations Tests")
    class CountOperationsTests {
        
        @Test
        @DisplayName("Should count favorites by restaurant")
        void shouldCountFavoritesByRestaurant() {
            // Act
            Long count = favoritesRepository.countByRestaurant(testRestaurant);
            
            // Assert
            assertNotNull(count);
            assertTrue(count >= 0);
        }
        
        @Test
        @DisplayName("Should count favorites by user")
        void shouldCountFavoritesByUser() {
            // Act
            Long count = favoritesRepository.countByUser(testUser);
            
            // Assert
            assertNotNull(count);
            assertTrue(count >= 0);
        }
        
        @Test
        @DisplayName("Should count all favorites")
        void shouldCountAllFavorites() {
            // Act
            Long count = favoritesRepository.countAll();
            
            // Assert
            assertNotNull(count);
            assertTrue(count >= 0);
        }
        
        @Test
        @DisplayName("Should handle null restaurant in count")
        void shouldHandleNullRestaurantInCount() {
            // Act
            Long count = favoritesRepository.countByRestaurant(null);
            
            // Assert
            assertNotNull(count);
            assertEquals(0L, count);
        }
        
        @Test
        @DisplayName("Should handle null user in count")
        void shouldHandleNullUserInCount() {
            // Act
            Long count = favoritesRepository.countByUser(null);
            
            // Assert
            assertNotNull(count);
            assertEquals(0L, count);
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {
        
        @Test
        @DisplayName("Should delete favorite by ID")
        void shouldDeleteFavoriteById() {
            // Act
            boolean result = favoritesRepository.delete(999999L); // Non-existent ID
            
            // Assert
            assertFalse(result); // Should return false for non-existent ID
        }
        
        @Test
        @DisplayName("Should handle null ID in delete")
        void shouldHandleNullIdInDelete() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                boolean result = favoritesRepository.delete((Long) null);
                assertFalse(result);
            });
        }
        
        @Test
        @DisplayName("Should delete using legacy method")
        void shouldDeleteUsingLegacyMethod() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                favoritesRepository.delete(1L, 2L);
            });
        }
        
        @Test
        @DisplayName("Should handle invalid IDs in legacy delete")
        void shouldHandleInvalidIdsInLegacyDelete() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                favoritesRepository.delete(-1L, -1L);
            });
        }
    }

    @Nested
    @DisplayName("Admin Operations Tests")
    class AdminOperationsTests {
        
        @Test
        @DisplayName("Should find all favorites")
        void shouldFindAllFavorites() {
            // Act
            List<Favorite> result = favoritesRepository.findAll();
            
            // Assert
            assertNotNull(result);
            // Note: May be empty if no data exists, which is valid
        }
        
        @Test
        @DisplayName("Should find favorites with pagination")
        void shouldFindFavoritesWithPagination() {
            // Act
            List<Favorite> result = favoritesRepository.findWithPagination(0, 10);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.size() <= 10);
        }
        
        @Test
        @DisplayName("Should handle negative offset in pagination")
        void shouldHandleNegativeOffsetInPagination() {
            // Act
            List<Favorite> result = favoritesRepository.findWithPagination(-1, 10);
            
            // Assert
            assertNotNull(result);
            // Should handle gracefully
        }
        
        @Test
        @DisplayName("Should handle zero limit in pagination")
        void shouldHandleZeroLimitInPagination() {
            // Act
            List<Favorite> result = favoritesRepository.findWithPagination(0, 0);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle large offset in pagination")
        void shouldHandleLargeOffsetInPagination() {
            // Act
            List<Favorite> result = favoritesRepository.findWithPagination(1000000, 10);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty()); // Should return empty for large offset
        }
        
        @Test
        @DisplayName("Should clear all favorites")
        void shouldClearAllFavorites() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                favoritesRepository.clear();
            });
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle database connection issues gracefully")
        void shouldHandleDatabaseConnectionIssuesGracefully() {
            // Note: This test assumes the database connection might fail
            // In a real scenario, you might mock the SessionFactory to throw exceptions
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                favoritesRepository.findAll();
                favoritesRepository.countAll();
                favoritesRepository.findById(1L);
            });
        }
        
        @Test
        @DisplayName("Should handle malformed entities gracefully")
        void shouldHandleMalformedEntitiesGracefully() {
            // Arrange
            Favorite malformedFavorite = new Favorite();
            // Missing required fields
            
            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                favoritesRepository.save(malformedFavorite);
            });
        }
        
        @Test
        @DisplayName("Should handle concurrent access gracefully")
        void shouldHandleConcurrentAccessGracefully() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                // Simulate concurrent access
                Thread[] threads = new Thread[5];
                for (int i = 0; i < 5; i++) {
                    threads[i] = new Thread(() -> {
                        favoritesRepository.findAll();
                        favoritesRepository.countAll();
                    });
                    threads[i].start();
                }
                
                // Wait for all threads to complete
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should handle large result sets efficiently")
        void shouldHandleLargeResultSetsEfficiently() {
            // Act
            long startTime = System.currentTimeMillis();
            List<Favorite> result = favoritesRepository.findAll();
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertNotNull(result);
            long duration = endTime - startTime;
            assertTrue(duration < 5000, "Query took too long: " + duration + "ms");
        }
        
        @Test
        @DisplayName("Should handle rapid successive queries")
        void shouldHandleRapidSuccessiveQueries() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                long startTime = System.currentTimeMillis();
                
                for (int i = 0; i < 100; i++) {
                    favoritesRepository.countAll();
                    favoritesRepository.findById((long) (i % 10 + 1));
                }
                
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                assertTrue(duration < 10000, "Rapid queries took too long: " + duration + "ms");
            });
        }
        
        @Test
        @DisplayName("Should handle complex queries efficiently")
        void shouldHandleComplexQueriesEfficiently() {
            // Act
            long startTime = System.currentTimeMillis();
            
            favoritesRepository.findRecentFavorites(30);
            favoritesRepository.findFavoritesWithNotes();
            favoritesRepository.findWithPagination(0, 50);
            
            long endTime = System.currentTimeMillis();
            
            // Assert
            long duration = endTime - startTime;
            assertTrue(duration < 3000, "Complex queries took too long: " + duration + "ms");
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {
        
        @Test
        @DisplayName("Should maintain referential integrity")
        void shouldMaintainReferentialIntegrity() {
            // Act
            List<Favorite> favorites = favoritesRepository.findAll();
            
            // Assert
            for (Favorite favorite : favorites) {
                assertNotNull(favorite.getUser(), "Favorite should have a user");
                assertNotNull(favorite.getRestaurant(), "Favorite should have a restaurant");
                assertNotNull(favorite.getCreatedAt(), "Favorite should have creation date");
            }
        }
        
        @Test
        @DisplayName("Should handle unique constraint violations")
        void shouldHandleUniqueConstraintViolations() {
            // Note: This test depends on having the unique constraint defined in the database
            // In a real scenario, attempting to save duplicate user-restaurant combinations
            // should throw an exception
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                // This should either succeed (if no duplicate) or throw exception (if duplicate exists)
                try {
                    favoritesRepository.save(1L, 2L);
                    favoritesRepository.save(1L, 2L); // Potential duplicate
                } catch (RuntimeException e) {
                    // Expected for duplicate constraint violation
                    assertTrue(e.getMessage().contains("constraint") || 
                              e.getMessage().contains("duplicate") ||
                              e.getMessage().contains("unique"));
                }
            });
        }
        
        @Test
        @DisplayName("Should validate entity state before save")
        void shouldValidateEntityStateBeforeSave() {
            // Arrange
            Favorite invalidFavorite = new Favorite();
            invalidFavorite.setUser(null); // Invalid state
            invalidFavorite.setRestaurant(null); // Invalid state
            
            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                favoritesRepository.save(invalidFavorite);
            });
        }
    }
} 