package com.myapp.review;

import com.myapp.common.TestDatabaseManager;
import com.myapp.common.models.Rating;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.models.User;
import com.myapp.common.TestDatabaseManager;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RatingRepository Integration Tests")
public class RatingRepositoryTest {

    private RatingRepository ratingRepository;
    private User testUser;
    private Restaurant testRestaurant;
    private Rating testRating;

    @BeforeEach
    void setUp() {
        // Clean database before each test to ensure isolation
        TestDatabaseManager.cleanRatingData();
        
        ratingRepository = new RatingRepository();
        
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
        
        testRating = new Rating(testUser, testRestaurant, 4, "Great food and service!");
        testRating.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Save Rating Tests")
    class SaveRatingTests {

        @Test
        @DisplayName("Should save new rating successfully")
        void shouldSaveNewRatingSuccessfully() {
            Rating savedRating = ratingRepository.save(testRating);

            assertNotNull(savedRating);
            assertNotNull(savedRating.getId());
            assertEquals(4, savedRating.getRatingScore());
            assertEquals("Great food and service!", savedRating.getReviewText());
            assertEquals(testUser.getId(), savedRating.getUser().getId());
            assertEquals(testRestaurant.getId(), savedRating.getRestaurant().getId());
        }

        @Test
        @DisplayName("Should update existing rating")
        void shouldUpdateExistingRating() {
            // First save the rating
            Rating savedRating = ratingRepository.save(testRating);
            
            // Update the rating
            savedRating.setRatingScore(5);
            savedRating.setReviewText("Updated review text");
            
            Rating updatedRating = ratingRepository.save(savedRating);

            assertNotNull(updatedRating);
            assertEquals(savedRating.getId(), updatedRating.getId());
            assertEquals(5, updatedRating.getRatingScore());
            assertEquals("Updated review text", updatedRating.getReviewText());
        }

        @Test
        @DisplayName("Should handle rating with null review text")
        void shouldHandleRatingWithNullReviewText() {
            testRating.setReviewText(null);
            
            Rating savedRating = ratingRepository.save(testRating);

            assertNotNull(savedRating);
            assertNull(savedRating.getReviewText());
        }

        @Test
        @DisplayName("Should handle save exception gracefully")
        void shouldHandleSaveExceptionGracefully() {
            // Create invalid rating (null user)
            Rating invalidRating = new Rating();
            invalidRating.setRatingScore(4);
            invalidRating.setReviewText("Test");

            assertThrows(RuntimeException.class, () -> 
                ratingRepository.save(invalidRating));
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find rating by ID")
        void shouldFindRatingById() {
            Rating savedRating = ratingRepository.save(testRating);

            Optional<Rating> found = ratingRepository.findById(savedRating.getId());

            assertTrue(found.isPresent());
            assertEquals(savedRating.getId(), found.get().getId());
            assertEquals(4, found.get().getRatingScore());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void shouldReturnEmptyForNonExistentId() {
            Optional<Rating> found = ratingRepository.findById(999L);

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should return empty for null ID")
        void shouldReturnEmptyForNullId() {
            Optional<Rating> found = ratingRepository.findById(null);

            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Find By User And Restaurant Tests")
    class FindByUserAndRestaurantTests {

        @Test
        @DisplayName("Should find rating by user and restaurant")
        void shouldFindRatingByUserAndRestaurant() {
            ratingRepository.save(testRating);

            Optional<Rating> found = ratingRepository.findByUserAndRestaurant(testUser, testRestaurant);

            assertTrue(found.isPresent());
            assertEquals(testUser.getId(), found.get().getUser().getId());
            assertEquals(testRestaurant.getId(), found.get().getRestaurant().getId());
        }

        @Test
        @DisplayName("Should return empty when no rating exists")
        void shouldReturnEmptyWhenNoRatingExists() {
            Optional<Rating> found = ratingRepository.findByUserAndRestaurant(testUser, testRestaurant);

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should handle null parameters gracefully")
        void shouldHandleNullParametersGracefully() {
            Optional<Rating> found1 = ratingRepository.findByUserAndRestaurant(null, testRestaurant);
            Optional<Rating> found2 = ratingRepository.findByUserAndRestaurant(testUser, null);

            assertFalse(found1.isPresent());
            assertFalse(found2.isPresent());
        }
    }

    @Nested
    @DisplayName("Find By Restaurant Tests")
    class FindByRestaurantTests {

        @Test
        @DisplayName("Should find ratings by restaurant")
        void shouldFindRatingsByRestaurant() {
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findByRestaurant(testRestaurant);

            assertNotNull(ratings);
            assertEquals(1, ratings.size());
            assertEquals(testRestaurant.getId(), ratings.get(0).getRestaurant().getId());
        }

        @Test
        @DisplayName("Should return empty list when no ratings exist")
        void shouldReturnEmptyListWhenNoRatingsExist() {
            List<Rating> ratings = ratingRepository.findByRestaurant(testRestaurant);

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }

        @Test
        @DisplayName("Should handle null restaurant gracefully")
        void shouldHandleNullRestaurantGracefully() {
            List<Rating> ratings = ratingRepository.findByRestaurant(null);

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }

        @Test
        @DisplayName("Should return ratings ordered by creation date")
        void shouldReturnRatingsOrderedByCreationDate() {
            // Create multiple ratings
            Rating rating1 = new Rating(testUser, testRestaurant, 4, "First rating");
            rating1.setCreatedAt(LocalDateTime.now().minusDays(2));
            
            Rating rating2 = new Rating(testUser, testRestaurant, 5, "Second rating");
            rating2.setCreatedAt(LocalDateTime.now().minusDays(1));

            ratingRepository.save(rating1);
            ratingRepository.save(rating2);

            List<Rating> ratings = ratingRepository.findByRestaurant(testRestaurant);

            assertNotNull(ratings);
            assertEquals(2, ratings.size());
            // Should be ordered by creation date DESC (newest first)
            assertTrue(ratings.get(0).getCreatedAt().isAfter(ratings.get(1).getCreatedAt()));
        }
    }

    @Nested
    @DisplayName("Find By User Tests")
    class FindByUserTests {

        @Test
        @DisplayName("Should find ratings by user")
        void shouldFindRatingsByUser() {
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findByUser(testUser);

            assertNotNull(ratings);
            assertEquals(1, ratings.size());
            assertEquals(testUser.getId(), ratings.get(0).getUser().getId());
        }

        @Test
        @DisplayName("Should return empty list when no ratings exist")
        void shouldReturnEmptyListWhenNoRatingsExist() {
            List<Rating> ratings = ratingRepository.findByUser(testUser);

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }

        @Test
        @DisplayName("Should handle null user gracefully")
        void shouldHandleNullUserGracefully() {
            List<Rating> ratings = ratingRepository.findByUser(null);

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find By Score Range Tests")
    class FindByScoreRangeTests {

        @Test
        @DisplayName("Should find ratings by score range")
        void shouldFindRatingsByScoreRange() {
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findByScoreRange(3, 5);

            assertNotNull(ratings);
            assertEquals(1, ratings.size());
            assertEquals(4, ratings.get(0).getRatingScore());
        }

        @Test
        @DisplayName("Should return empty list when no ratings in range")
        void shouldReturnEmptyListWhenNoRatingsInRange() {
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findByScoreRange(1, 2);

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }

        @Test
        @DisplayName("Should handle exact score match")
        void shouldHandleExactScoreMatch() {
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findByScoreRange(4, 4);

            assertNotNull(ratings);
            assertEquals(1, ratings.size());
        }
    }

    @Nested
    @DisplayName("Find Verified Ratings Tests")
    class FindVerifiedRatingsTests {

        @Test
        @DisplayName("Should find verified ratings")
        void shouldFindVerifiedRatings() {
            testRating.setIsVerified(true);
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findVerifiedRatings();

            assertNotNull(ratings);
            assertEquals(1, ratings.size());
            assertTrue(ratings.get(0).getIsVerified());
        }

        @Test
        @DisplayName("Should not return unverified ratings")
        void shouldNotReturnUnverifiedRatings() {
            testRating.setIsVerified(false);
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findVerifiedRatings();

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find Ratings With Reviews Tests")
    class FindRatingsWithReviewsTests {

        @Test
        @DisplayName("Should find ratings with review text")
        void shouldFindRatingsWithReviewText() {
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findRatingsWithReviews();

            assertNotNull(ratings);
            assertEquals(1, ratings.size());
            assertNotNull(ratings.get(0).getReviewText());
            assertFalse(ratings.get(0).getReviewText().trim().isEmpty());
        }

        @Test
        @DisplayName("Should not return ratings without review text")
        void shouldNotReturnRatingsWithoutReviewText() {
            testRating.setReviewText(null);
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findRatingsWithReviews();

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }

        @Test
        @DisplayName("Should not return ratings with empty review text")
        void shouldNotReturnRatingsWithEmptyReviewText() {
            testRating.setReviewText("");
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findRatingsWithReviews();

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find Recent Ratings Tests")
    class FindRecentRatingsTests {

        @Test
        @DisplayName("Should find recent ratings")
        void shouldFindRecentRatings() {
            testRating.setCreatedAt(LocalDateTime.now());
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findRecentRatings(7);

            assertNotNull(ratings);
            assertEquals(1, ratings.size());
        }

        @Test
        @DisplayName("Should not return old ratings")
        void shouldNotReturnOldRatings() {
            testRating.setCreatedAt(LocalDateTime.now().minusDays(10));
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findRecentRatings(7);

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Average Rating Tests")
    class GetAverageRatingTests {

        @Test
        @DisplayName("Should calculate average rating")
        void shouldCalculateAverageRating() {
            // Create multiple ratings
            Rating rating1 = new Rating(testUser, testRestaurant, 4, "Good");
            Rating rating2 = new Rating(testUser, testRestaurant, 5, "Excellent");
            
            ratingRepository.save(rating1);
            ratingRepository.save(rating2);

            Double average = ratingRepository.getAverageRating(testRestaurant);

            assertNotNull(average);
            assertEquals(4.5, average, 0.01);
        }

        @Test
        @DisplayName("Should return 0.0 when no ratings exist")
        void shouldReturnZeroWhenNoRatingsExist() {
            Double average = ratingRepository.getAverageRating(testRestaurant);

            assertEquals(0.0, average);
        }

        @Test
        @DisplayName("Should handle null restaurant gracefully")
        void shouldHandleNullRestaurantGracefully() {
            Double average = ratingRepository.getAverageRating(null);

            assertEquals(0.0, average);
        }
    }

    @Nested
    @DisplayName("Get Rating Count Tests")
    class GetRatingCountTests {

        @Test
        @DisplayName("Should count ratings for restaurant")
        void shouldCountRatingsForRestaurant() {
            ratingRepository.save(testRating);

            Long count = ratingRepository.getRatingCount(testRestaurant);

            assertEquals(1L, count);
        }

        @Test
        @DisplayName("Should return 0 when no ratings exist")
        void shouldReturnZeroWhenNoRatingsExist() {
            Long count = ratingRepository.getRatingCount(testRestaurant);

            assertEquals(0L, count);
        }

        @Test
        @DisplayName("Should handle null restaurant gracefully")
        void shouldHandleNullRestaurantGracefully() {
            Long count = ratingRepository.getRatingCount(null);

            assertEquals(0L, count);
        }
    }

    @Nested
    @DisplayName("Get Rating Distribution Tests")
    class GetRatingDistributionTests {

        @Test
        @DisplayName("Should get rating distribution")
        void shouldGetRatingDistribution() {
            // Create ratings with different scores
            Rating rating1 = new Rating(testUser, testRestaurant, 4, "Good");
            Rating rating2 = new Rating(testUser, testRestaurant, 5, "Excellent");
            Rating rating3 = new Rating(testUser, testRestaurant, 4, "Good again");
            
            ratingRepository.save(rating1);
            ratingRepository.save(rating2);
            ratingRepository.save(rating3);

            Map<Integer, Long> distribution = ratingRepository.getRatingDistribution(testRestaurant);

            assertNotNull(distribution);
            assertEquals(5, distribution.size()); // Should have all 5 score levels
            assertEquals(0L, distribution.get(1));
            assertEquals(0L, distribution.get(2));
            assertEquals(0L, distribution.get(3));
            assertEquals(2L, distribution.get(4));
            assertEquals(1L, distribution.get(5));
        }

        @Test
        @DisplayName("Should return empty distribution when no ratings exist")
        void shouldReturnEmptyDistributionWhenNoRatingsExist() {
            Map<Integer, Long> distribution = ratingRepository.getRatingDistribution(testRestaurant);

            assertNotNull(distribution);
            assertEquals(5, distribution.size());
            for (int i = 1; i <= 5; i++) {
                assertEquals(0L, distribution.get(i));
            }
        }

        @Test
        @DisplayName("Should handle null restaurant gracefully")
        void shouldHandleNullRestaurantGracefully() {
            Map<Integer, Long> distribution = ratingRepository.getRatingDistribution(null);

            assertNotNull(distribution);
            assertEquals(5, distribution.size());
            for (int i = 1; i <= 5; i++) {
                assertEquals(0L, distribution.get(i));
            }
        }
    }

    @Nested
    @DisplayName("Get Top Rated Restaurants Tests")
    class GetTopRatedRestaurantsTests {

        @Test
        @DisplayName("Should get top rated restaurants")
        void shouldGetTopRatedRestaurants() {
            // Ensure restaurant exists in database
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                session.merge(testRestaurant);
                transaction.commit();
            }
            
            // Create multiple ratings to meet minimum threshold
            for (int i = 0; i < 6; i++) {
                Rating rating = new Rating(testUser, testRestaurant, 5, "Excellent " + i);
                ratingRepository.save(rating);
            }

            List<Object[]> topRestaurants = ratingRepository.getTopRatedRestaurants(10);

            assertNotNull(topRestaurants);
            assertFalse(topRestaurants.isEmpty());
            
            // Check first result structure
            Object[] first = topRestaurants.get(0);
            assertEquals(4, first.length); // id, name, avg, count
            assertEquals(testRestaurant.getId(), first[0]);
            assertEquals(testRestaurant.getName(), first[1]);
        }

        @Test
        @DisplayName("Should return empty list when no restaurants meet threshold")
        void shouldReturnEmptyListWhenNoRestaurantsMeetThreshold() {
            // Clean up any existing data
            TestDatabaseManager.cleanRatingData();
            
            // Create only 2 ratings (below threshold of 5)
            Rating rating1 = new Rating(testUser, testRestaurant, 5, "Good");
            Rating rating2 = new Rating(testUser, testRestaurant, 4, "OK");
            
            ratingRepository.save(rating1);
            ratingRepository.save(rating2);

            List<Object[]> topRestaurants = ratingRepository.getTopRatedRestaurants(10);

            assertNotNull(topRestaurants);
            assertTrue(topRestaurants.isEmpty());
        }

        @Test
        @DisplayName("Should respect limit parameter")
        void shouldRespectLimitParameter() {
            // Clean up any existing data
            TestDatabaseManager.cleanRatingData();
            
            // Ensure restaurant exists in database
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                session.merge(testRestaurant);
                transaction.commit();
            }
            
            // Create multiple restaurants with enough ratings
            for (int i = 0; i < 6; i++) {
                Rating rating = new Rating(testUser, testRestaurant, 5, "Excellent " + i);
                ratingRepository.save(rating);
            }

            List<Object[]> topRestaurants = ratingRepository.getTopRatedRestaurants(1);

            assertNotNull(topRestaurants);
            assertTrue(topRestaurants.size() <= 1);
        }
    }

    @Nested
    @DisplayName("Delete Rating Tests")
    class DeleteRatingTests {

        @Test
        @DisplayName("Should delete rating successfully")
        void shouldDeleteRatingSuccessfully() {
            Rating savedRating = ratingRepository.save(testRating);

            boolean deleted = ratingRepository.delete(savedRating.getId());

            assertTrue(deleted);
            
            // Verify rating is deleted
            Optional<Rating> found = ratingRepository.findById(savedRating.getId());
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should return false when rating not found")
        void shouldReturnFalseWhenRatingNotFound() {
            boolean deleted = ratingRepository.delete(999L);

            assertFalse(deleted);
        }

        @Test
        @DisplayName("Should handle null ID gracefully")
        void shouldHandleNullIdGracefully() {
            boolean deleted = ratingRepository.delete(null);

            assertFalse(deleted);
        }
    }

    @Nested
    @DisplayName("Find All Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all ratings")
        void shouldFindAllRatings() {
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findAll();

            assertNotNull(ratings);
            assertEquals(1, ratings.size());
        }

        @Test
        @DisplayName("Should return empty list when no ratings exist")
        void shouldReturnEmptyListWhenNoRatingsExist() {
            List<Rating> ratings = ratingRepository.findAll();

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }

        @Test
        @DisplayName("Should return ratings ordered by creation date DESC")
        void shouldReturnRatingsOrderedByCreationDateDesc() {
            Rating rating1 = new Rating(testUser, testRestaurant, 4, "First");
            rating1.setCreatedAt(LocalDateTime.now().minusDays(1));
            
            Rating rating2 = new Rating(testUser, testRestaurant, 5, "Second");
            rating2.setCreatedAt(LocalDateTime.now());

            ratingRepository.save(rating1);
            ratingRepository.save(rating2);

            List<Rating> ratings = ratingRepository.findAll();

            assertNotNull(ratings);
            assertEquals(2, ratings.size());
            assertTrue(ratings.get(0).getCreatedAt().isAfter(ratings.get(1).getCreatedAt()));
        }
    }

    @Nested
    @DisplayName("Find With Pagination Tests")
    class FindWithPaginationTests {

        @Test
        @DisplayName("Should find ratings with pagination")
        void shouldFindRatingsWithPagination() {
            // Create multiple ratings
            for (int i = 0; i < 5; i++) {
                Rating rating = new Rating(testUser, testRestaurant, 4, "Rating " + i);
                ratingRepository.save(rating);
            }

            List<Rating> page1 = ratingRepository.findWithPagination(0, 2);
            List<Rating> page2 = ratingRepository.findWithPagination(2, 2);

            assertNotNull(page1);
            assertNotNull(page2);
            assertEquals(2, page1.size());
            assertEquals(2, page2.size());
        }

        @Test
        @DisplayName("Should return empty list for offset beyond data")
        void shouldReturnEmptyListForOffsetBeyondData() {
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findWithPagination(10, 5);

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }

        @Test
        @DisplayName("Should handle zero limit gracefully")
        void shouldHandleZeroLimitGracefully() {
            ratingRepository.save(testRating);

            List<Rating> ratings = ratingRepository.findWithPagination(0, 0);

            assertNotNull(ratings);
            assertTrue(ratings.isEmpty());
        }
    }

    @Nested
    @DisplayName("Count All Tests")
    class CountAllTests {

        @Test
        @DisplayName("Should count all ratings")
        void shouldCountAllRatings() {
            ratingRepository.save(testRating);

            Long count = ratingRepository.countAll();

            assertEquals(1L, count);
        }

        @Test
        @DisplayName("Should return 0 when no ratings exist")
        void shouldReturnZeroWhenNoRatingsExist() {
            Long count = ratingRepository.countAll();

            assertEquals(0L, count);
        }

        @Test
        @DisplayName("Should count multiple ratings correctly")
        void shouldCountMultipleRatingsCorrectly() {
            // Create multiple ratings
            for (int i = 0; i < 3; i++) {
                Rating rating = new Rating(testUser, testRestaurant, 4, "Rating " + i);
                ratingRepository.save(rating);
            }

            Long count = ratingRepository.countAll();

            assertEquals(3L, count);
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle bulk operations efficiently")
        void shouldHandleBulkOperationsEfficiently() {
            long startTime = System.currentTimeMillis();

            // Create multiple ratings
            for (int i = 0; i < 50; i++) {
                Rating rating = new Rating(testUser, testRestaurant, 4, "Rating " + i);
                ratingRepository.save(rating);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Should complete within reasonable time
            assertTrue(duration < 5000, "Bulk operations took too long: " + duration + "ms");
        }

        @Test
        @DisplayName("Should handle complex queries efficiently")
        void shouldHandleComplexQueriesEfficiently() {
            // Create test data
            for (int i = 0; i < 20; i++) {
                Rating rating = new Rating(testUser, testRestaurant, (i % 5) + 1, "Rating " + i);
                ratingRepository.save(rating);
            }

            long startTime = System.currentTimeMillis();

            // Execute multiple complex queries
            ratingRepository.getAverageRating(testRestaurant);
            ratingRepository.getRatingCount(testRestaurant);
            ratingRepository.getRatingDistribution(testRestaurant);
            ratingRepository.findByScoreRange(3, 5);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Should complete within reasonable time
            assertTrue(duration < 2000, "Complex queries took too long: " + duration + "ms");
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should maintain referential integrity")
        void shouldMaintainReferentialIntegrity() {
            Rating savedRating = ratingRepository.save(testRating);

            assertNotNull(savedRating.getUser());
            assertNotNull(savedRating.getRestaurant());
            assertEquals(testUser.getId(), savedRating.getUser().getId());
            assertEquals(testRestaurant.getId(), savedRating.getRestaurant().getId());
        }

        @Test
        @DisplayName("Should handle concurrent access gracefully")
        void shouldHandleConcurrentAccessGracefully() {
            // This test simulates concurrent access
            Rating rating1 = new Rating(testUser, testRestaurant, 4, "Concurrent 1");
            Rating rating2 = new Rating(testUser, testRestaurant, 5, "Concurrent 2");

            assertDoesNotThrow(() -> {
                ratingRepository.save(rating1);
                ratingRepository.save(rating2);
            });
        }

        @Test
        @DisplayName("Should validate rating constraints")
        void shouldValidateRatingConstraints() {
            // Test that rating score constraints are enforced at entity level
            assertThrows(IllegalArgumentException.class, () -> {
                Rating invalidRating = new Rating(testUser, testRestaurant, 0, "Invalid");
                ratingRepository.save(invalidRating);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                Rating invalidRating = new Rating(testUser, testRestaurant, 6, "Invalid");
                ratingRepository.save(invalidRating);
            });
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long review text")
        void shouldHandleVeryLongReviewText() {
            String longReview = "A".repeat(999); // Just under 1000 char limit
            testRating.setReviewText(longReview);

            assertDoesNotThrow(() -> {
                Rating savedRating = ratingRepository.save(testRating);
                assertEquals(longReview, savedRating.getReviewText());
            });
        }

        @Test
        @DisplayName("Should handle special characters in review text")
        void shouldHandleSpecialCharactersInReviewText() {
            String specialText = "Great food! 🍕 Rating: ★★★★★ Price: $$$";
            testRating.setReviewText(specialText);

            assertDoesNotThrow(() -> {
                Rating savedRating = ratingRepository.save(testRating);
                assertEquals(specialText, savedRating.getReviewText());
            });
        }

        @Test
        @DisplayName("Should handle boundary dates correctly")
        void shouldHandleBoundaryDatesCorrectly() {
            // Test with very old date
            Rating oldRating = new Rating(testUser, testRestaurant, 4, "Old rating");
            oldRating.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));

            // Test with future date
            Rating futureRating = new Rating(testUser, testRestaurant, 5, "Future rating");
            futureRating.setCreatedAt(LocalDateTime.now().plusDays(1));

            assertDoesNotThrow(() -> {
                ratingRepository.save(oldRating);
                ratingRepository.save(futureRating);
            });
        }
    }
}