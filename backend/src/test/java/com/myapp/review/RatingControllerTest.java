package com.myapp.review;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Rating;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.models.User;
import com.myapp.common.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RatingController HTTP Tests")
class RatingControllerTest {

    @Mock
    private RatingService mockRatingService;

    @Mock
    private HttpExchange mockExchange;

    private RatingController ratingController;
    private User testUser;
    private Restaurant testRestaurant;
    private Rating testRating;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ratingController = new RatingController(mockRatingService);
        responseBody = new ByteArrayOutputStream();
        
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
        
        testRating = new Rating(testUser, testRestaurant, 4, "Great food!");
        testRating.setId(1L);
        testRating.setCreatedAt(LocalDateTime.now());
        
        // Setup common mock behavior
        try {
            when(mockExchange.getResponseBody()).thenReturn(responseBody);
        } catch (Exception e) {
            // Handle setup exception
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create controller with default constructor")
        void shouldCreateControllerWithDefaultConstructor() {
            RatingController controller = new RatingController();
            assertNotNull(controller);
        }

        @Test
        @DisplayName("Should create controller with service injection")
        void shouldCreateControllerWithServiceInjection() {
            RatingController controller = new RatingController(mockRatingService);
            assertNotNull(controller);
        }
    }

    @Nested
    @DisplayName("POST /api/ratings Tests")
    class CreateRatingTests {

        @Test
        @DisplayName("Should create rating successfully")
        void shouldCreateRatingSuccessfully() throws IOException {
            // Setup request
            String requestBody = JsonUtil.toJson(Map.of(
                "userId", 1L,
                "restaurantId", 2L,
                "score", 4,
                "reviewText", "Great food!"
            ));
            
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(mockRatingService.createRating(1L, 2L, 4, "Great food!")).thenReturn(testRating);

            ratingController.handle(mockExchange);

            verify(mockRatingService).createRating(1L, 2L, 4, "Great food!");
            verify(mockExchange).sendResponseHeaders(201, -1);
        }

        @Test
        @DisplayName("Should handle missing parameters")
        void shouldHandleMissingParameters() throws IOException {
            String requestBody = JsonUtil.toJson(Map.of(
                "userId", 1L
                // Missing restaurantId and score
            ));
            
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }

        @Test
        @DisplayName("Should handle service exceptions")
        void shouldHandleServiceExceptions() throws IOException {
            String requestBody = JsonUtil.toJson(Map.of(
                "userId", 1L,
                "restaurantId", 2L,
                "score", 4,
                "reviewText", "Great food!"
            ));
            
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(mockRatingService.createRating(anyLong(), anyLong(), anyInt(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid rating"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }

        @Test
        @DisplayName("Should handle not found exceptions")
        void shouldHandleNotFoundExceptions() throws IOException {
            String requestBody = JsonUtil.toJson(Map.of(
                "userId", 1L,
                "restaurantId", 999L,
                "score", 4,
                "reviewText", "Great food!"
            ));
            
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(mockRatingService.createRating(anyLong(), anyLong(), anyInt(), anyString()))
                .thenThrow(new NotFoundException("Restaurant", 999L));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(404, -1);
        }

        @Test
        @DisplayName("Should handle malformed JSON")
        void shouldHandleMalformedJson() throws IOException {
            String malformedJson = "{ invalid json }";
            
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(malformedJson.getBytes()));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }
    }

    @Nested
    @DisplayName("PUT /api/ratings/{id} Tests")
    class UpdateRatingTests {

        @Test
        @DisplayName("Should update rating successfully")
        void shouldUpdateRatingSuccessfully() throws IOException {
            String requestBody = JsonUtil.toJson(Map.of(
                "score", 5,
                "reviewText", "Updated review"
            ));
            
            when(mockExchange.getRequestMethod()).thenReturn("PUT");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/1"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(mockRatingService.updateRating(1L, 5, "Updated review")).thenReturn(testRating);

            ratingController.handle(mockExchange);

            verify(mockRatingService).updateRating(1L, 5, "Updated review");
            verify(mockExchange).sendResponseHeaders(200, -1);
        }

        @Test
        @DisplayName("Should update only score")
        void shouldUpdateOnlyScore() throws IOException {
            String requestBody = JsonUtil.toJson(Map.of(
                "score", 5
            ));
            
            when(mockExchange.getRequestMethod()).thenReturn("PUT");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/1"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(mockRatingService.updateRating(1L, 5, null)).thenReturn(testRating);

            ratingController.handle(mockExchange);

            verify(mockRatingService).updateRating(1L, 5, null);
            verify(mockExchange).sendResponseHeaders(200, -1);
        }

        @Test
        @DisplayName("Should update only review text")
        void shouldUpdateOnlyReviewText() throws IOException {
            String requestBody = JsonUtil.toJson(Map.of(
                "reviewText", "Updated review"
            ));
            
            when(mockExchange.getRequestMethod()).thenReturn("PUT");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/1"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(mockRatingService.updateRating(1L, null, "Updated review")).thenReturn(testRating);

            ratingController.handle(mockExchange);

            verify(mockRatingService).updateRating(1L, null, "Updated review");
            verify(mockExchange).sendResponseHeaders(200, -1);
        }

        @Test
        @DisplayName("Should handle invalid rating ID")
        void shouldHandleInvalidRatingId() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("PUT");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/invalid"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }

        @Test
        @DisplayName("Should handle rating not found")
        void shouldHandleRatingNotFound() throws IOException {
            String requestBody = JsonUtil.toJson(Map.of("score", 5));
            
            when(mockExchange.getRequestMethod()).thenReturn("PUT");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/999"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(mockRatingService.updateRating(anyLong(), anyInt(), any()))
                .thenThrow(new NotFoundException("Rating", 999L));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(404, -1);
        }
    }

    @Nested
    @DisplayName("GET /api/ratings/{id} Tests")
    class GetRatingTests {

        @Test
        @DisplayName("Should get rating by ID successfully")
        void shouldGetRatingByIdSuccessfully() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/1"));
            when(mockRatingService.getRating(1L)).thenReturn(testRating);

            ratingController.handle(mockExchange);

            verify(mockRatingService).getRating(1L);
            verify(mockExchange).sendResponseHeaders(200, -1);
        }

        @Test
        @DisplayName("Should handle rating not found")
        void shouldHandleRatingNotFound() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/999"));
            when(mockRatingService.getRating(999L)).thenThrow(new NotFoundException("Rating", 999L));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(404, -1);
        }

        @Test
        @DisplayName("Should handle invalid rating ID")
        void shouldHandleInvalidRatingId() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/invalid"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }
    }

    @Nested
    @DisplayName("GET /api/ratings Tests")
    class GetAllRatingsTests {

        @Test
        @DisplayName("Should get all ratings successfully")
        void shouldGetAllRatingsSuccessfully() throws IOException {
            List<Rating> ratings = Arrays.asList(testRating);
            
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockRatingService.getAllRatings()).thenReturn(ratings);

            ratingController.handle(mockExchange);

            verify(mockRatingService).getAllRatings();
            verify(mockExchange).sendResponseHeaders(200, -1);
        }

        @Test
        @DisplayName("Should return empty list when no ratings exist")
        void shouldReturnEmptyListWhenNoRatingsExist() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockRatingService.getAllRatings()).thenReturn(List.of());

            ratingController.handle(mockExchange);

            verify(mockRatingService).getAllRatings();
            verify(mockExchange).sendResponseHeaders(200, -1);
        }
    }

    @Nested
    @DisplayName("GET /api/ratings/restaurant Tests")
    class GetRestaurantRatingsTests {

        @Test
        @DisplayName("Should get restaurant ratings successfully")
        void shouldGetRestaurantRatingsSuccessfully() throws IOException {
            List<Rating> ratings = Arrays.asList(testRating);
            
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/restaurant?restaurantId=2"));
            when(mockRatingService.getRestaurantRatings(2L)).thenReturn(ratings);

            ratingController.handle(mockExchange);

            verify(mockRatingService).getRestaurantRatings(2L);
            verify(mockExchange).sendResponseHeaders(200, -1);
        }

        @Test
        @DisplayName("Should handle missing restaurant ID parameter")
        void shouldHandleMissingRestaurantIdParameter() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/restaurant"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }

        @Test
        @DisplayName("Should handle invalid restaurant ID parameter")
        void shouldHandleInvalidRestaurantIdParameter() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/restaurant?restaurantId=invalid"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }

        @Test
        @DisplayName("Should handle restaurant not found")
        void shouldHandleRestaurantNotFound() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/restaurant?restaurantId=999"));
            when(mockRatingService.getRestaurantRatings(999L))
                .thenThrow(new NotFoundException("Restaurant", 999L));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(404, -1);
        }
    }

    @Nested
    @DisplayName("GET /api/ratings/stats Tests")
    class GetRatingStatsTests {

        @Test
        @DisplayName("Should get rating stats successfully")
        void shouldGetRatingStatsSuccessfully() throws IOException {
            RatingService.RatingStats stats = new RatingService.RatingStats(4.5, 10L, Map.of(4, 5L, 5, 5L));
            
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/stats?restaurantId=2"));
            when(mockRatingService.getRestaurantRatingStats(2L)).thenReturn(stats);

            ratingController.handle(mockExchange);

            verify(mockRatingService).getRestaurantRatingStats(2L);
            verify(mockExchange).sendResponseHeaders(200, -1);
        }

        @Test
        @DisplayName("Should handle missing restaurant ID parameter")
        void shouldHandleMissingRestaurantIdParameter() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/stats"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }

        @Test
        @DisplayName("Should handle restaurant not found")
        void shouldHandleRestaurantNotFound() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/stats?restaurantId=999"));
            when(mockRatingService.getRestaurantRatingStats(999L))
                .thenThrow(new NotFoundException("Restaurant", 999L));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(404, -1);
        }
    }

    @Nested
    @DisplayName("Method Not Allowed Tests")
    class MethodNotAllowedTests {

        @Test
        @DisplayName("Should return 405 for DELETE method")
        void shouldReturn405ForDeleteMethod() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("DELETE");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/1"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(405, -1);
        }

        @Test
        @DisplayName("Should return 405 for PATCH method")
        void shouldReturn405ForPatchMethod() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("PATCH");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/1"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(405, -1);
        }

        @Test
        @DisplayName("Should return 405 for HEAD method")
        void shouldReturn405ForHeadMethod() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("HEAD");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(405, -1);
        }
    }

    @Nested
    @DisplayName("Not Found Tests")
    class NotFoundTests {

        @Test
        @DisplayName("Should return 404 for invalid GET endpoint")
        void shouldReturn404ForInvalidGetEndpoint() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/invalid/endpoint"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(404, -1);
        }

        @Test
        @DisplayName("Should return 404 for invalid POST endpoint")
        void shouldReturn404ForInvalidPostEndpoint() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/invalid"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(404, -1);
        }

        @Test
        @DisplayName("Should return 404 for invalid PUT endpoint")
        void shouldReturn404ForInvalidPutEndpoint() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("PUT");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/invalid/path"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(404, -1);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle internal server errors")
        void shouldHandleInternalServerErrors() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/1"));
            when(mockRatingService.getRating(anyLong())).thenThrow(new RuntimeException("Database error"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(500, -1);
        }

        @Test
        @DisplayName("Should handle IO exceptions gracefully")
        void shouldHandleIoExceptionsGracefully() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockExchange.getRequestBody()).thenThrow(new IOException("IO error"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(500, -1);
        }

        @Test
        @DisplayName("Should handle null pointer exceptions")
        void shouldHandleNullPointerExceptions() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/1"));
            when(mockRatingService.getRating(anyLong())).thenThrow(new NullPointerException("Null error"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(500, -1);
        }
    }

    @Nested
    @DisplayName("URL Encoding Tests")
    class UrlEncodingTests {

        @Test
        @DisplayName("Should handle URL encoded parameters")
        void shouldHandleUrlEncodedParameters() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/restaurant?restaurantId=2%20"));
            when(mockRatingService.getRestaurantRatings(2L)).thenReturn(List.of(testRating));

            ratingController.handle(mockExchange);

            verify(mockRatingService).getRestaurantRatings(2L);
            verify(mockExchange).sendResponseHeaders(200, -1);
        }

        @Test
        @DisplayName("Should handle special characters in query parameters")
        void shouldHandleSpecialCharactersInQueryParameters() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/restaurant?restaurantId=2&extra=test%26value"));
            when(mockRatingService.getRestaurantRatings(2L)).thenReturn(List.of(testRating));

            ratingController.handle(mockExchange);

            verify(mockRatingService).getRestaurantRatings(2L);
            verify(mockExchange).sendResponseHeaders(200, -1);
        }

        @Test
        @DisplayName("Should handle empty query parameters")
        void shouldHandleEmptyQueryParameters() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/restaurant?restaurantId="));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle multiple concurrent requests efficiently")
        void shouldHandleMultipleConcurrentRequestsEfficiently() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/1"));
            when(mockRatingService.getRating(anyLong())).thenReturn(testRating);

            long startTime = System.currentTimeMillis();

            // Simulate multiple requests
            for (int i = 0; i < 100; i++) {
                ratingController.handle(mockExchange);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Should handle requests efficiently
            assertTrue(duration < 1000, "Request handling took too long: " + duration + "ms");
        }

        @Test
        @DisplayName("Should handle large request bodies efficiently")
        void shouldHandleLargeRequestBodiesEfficiently() throws IOException {
            // Create large request body
            String largeReviewText = "A".repeat(900); // Large but valid review
            String requestBody = JsonUtil.toJson(Map.of(
                "userId", 1L,
                "restaurantId", 2L,
                "score", 4,
                "reviewText", largeReviewText
            ));
            
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(mockRatingService.createRating(anyLong(), anyLong(), anyInt(), anyString())).thenReturn(testRating);

            long startTime = System.currentTimeMillis();
            ratingController.handle(mockExchange);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Should handle large requests efficiently
            assertTrue(duration < 500, "Large request handling took too long: " + duration + "ms");
            verify(mockExchange).sendResponseHeaders(201, -1);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large rating IDs")
        void shouldHandleVeryLargeRatingIds() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/" + Long.MAX_VALUE));
            when(mockRatingService.getRating(Long.MAX_VALUE))
                .thenThrow(new NotFoundException("Rating", Long.MAX_VALUE));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(404, -1);
        }

        @Test
        @DisplayName("Should handle negative rating IDs")
        void shouldHandleNegativeRatingIds() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/-1"));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }

        @Test
        @DisplayName("Should handle zero rating ID")
        void shouldHandleZeroRatingId() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings/0"));
            when(mockRatingService.getRating(0L)).thenThrow(new NotFoundException("Rating", 0L));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(404, -1);
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("".getBytes()));

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(400, -1);
        }

        @Test
        @DisplayName("Should handle null request body")
        void shouldHandleNullRequestBody() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/ratings"));
            when(mockExchange.getRequestBody()).thenReturn(null);

            ratingController.handle(mockExchange);

            verify(mockExchange).sendResponseHeaders(500, -1);
        }
    }
}
