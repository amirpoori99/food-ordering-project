package com.myapp.favorites;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Favorite;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.User;
import com.myapp.common.utils.JsonUtil;
import com.sun.net.httpserver.Headers;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * تست‌های جامع برای کلاس FavoritesController
 * 
 * پوشش کامل تمام HTTP endpoints مربوط به مدیریت علاقه‌مندی‌ها:
 * - GET endpoints: دریافت، بررسی، آمارگیری
 * - POST endpoints: اضافه کردن علاقه‌مندی
 * - PUT endpoints: به‌روزرسانی یادداشت
 * - DELETE endpoints: حذف علاقه‌مندی
 * - Error handling: مدیریت خطاها و validation
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("FavoritesController Tests")
public class FavoritesControllerTest {

    /** Mock سرویس علاقه‌مندی‌ها */
    @Mock
    private FavoritesService mockFavoritesService;
    
    /** Mock HTTP exchange object */
    @Mock
    private HttpExchange mockExchange;
    
    /** Mock HTTP headers */
    @Mock
    private Headers mockHeaders;
    
    /** کنترلر تحت تست */
    private FavoritesController favoritesController;
    
    /** Stream برای capture کردن response */
    private ByteArrayOutputStream responseStream;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        favoritesController = new FavoritesController(mockFavoritesService);
        responseStream = new ByteArrayOutputStream();
        
        // Setup common mocks
        when(mockExchange.getResponseBody()).thenReturn(responseStream);
        when(mockExchange.getResponseHeaders()).thenReturn(mockHeaders);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create controller with default constructor")
        void shouldCreateControllerWithDefaultConstructor() {
            FavoritesController controller = new FavoritesController();
            assertNotNull(controller);
        }
        
        @Test
        @DisplayName("Should create controller with service dependency")
        void shouldCreateControllerWithServiceDependency() {
            FavoritesController controller = new FavoritesController(mockFavoritesService);
            assertNotNull(controller);
        }
    }

    @Nested
    @DisplayName("GET /api/favorites - Get User Favorites")
    class GetUserFavoritesTests {
        
        @Test
        @DisplayName("Should get user favorites successfully")
        void shouldGetUserFavoritesSuccessfully() throws IOException {
            // Arrange
            List<Favorite> favorites = Arrays.asList(createSampleFavorite());
            when(mockFavoritesService.getUserFavorites(1L)).thenReturn(favorites);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites?userId=1"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).getUserFavorites(1L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            verify(mockHeaders).set("Content-Type", "application/json");
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("favorites"));
            assertTrue(response.contains("count"));
        }
        
        @Test
        @DisplayName("Should handle missing userId parameter")
        void shouldHandleMissingUserIdParameter() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":false"));
            assertTrue(response.contains("Missing required parameter: userId"));
        }
        
        @Test
        @DisplayName("Should handle invalid userId format")
        void shouldHandleInvalidUserIdFormat() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites?userId=invalid"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":false"));
            assertTrue(response.contains("Invalid userId format"));
        }
        
        @Test
        @DisplayName("Should handle user not found")
        void shouldHandleUserNotFound() throws IOException {
            // Arrange
            when(mockFavoritesService.getUserFavorites(999L)).thenThrow(new NotFoundException("User", 999L));
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites?userId=999"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(404), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":false"));
            assertTrue(response.contains("User"));
        }
    }

    @Nested
    @DisplayName("GET /api/favorites/check - Check Favorite Status")
    class CheckFavoriteStatusTests {
        
        @Test
        @DisplayName("Should check favorite status successfully")
        void shouldCheckFavoriteStatusSuccessfully() throws IOException {
            // Arrange
            when(mockFavoritesService.isFavorite(1L, 2L)).thenReturn(true);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/check?userId=1&restaurantId=2"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).isFavorite(1L, 2L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("\"isFavorite\":true"));
        }
        
        @Test
        @DisplayName("Should handle missing parameters in check")
        void shouldHandleMissingParametersInCheck() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/check?userId=1"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("Missing required parameters: userId, restaurantId"));
        }
    }

    @Nested
    @DisplayName("GET /api/favorites/recent - Get Recent Favorites")
    class GetRecentFavoritesTests {
        
        @Test
        @DisplayName("Should get recent favorites successfully")
        void shouldGetRecentFavoritesSuccessfully() throws IOException {
            // Arrange
            List<Favorite> recentFavorites = Arrays.asList(createSampleFavorite());
            when(mockFavoritesService.getRecentFavorites(30)).thenReturn(recentFavorites);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/recent?days=30"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).getRecentFavorites(30);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("favorites"));
            assertTrue(response.contains("\"days\":30"));
        }
        
        @Test
        @DisplayName("Should use default days when not specified")
        void shouldUseDefaultDaysWhenNotSpecified() throws IOException {
            // Arrange
            List<Favorite> recentFavorites = Arrays.asList(createSampleFavorite());
            when(mockFavoritesService.getRecentFavorites(30)).thenReturn(recentFavorites);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/recent"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).getRecentFavorites(30);
        }
        
        @Test
        @DisplayName("Should handle invalid days parameter")
        void shouldHandleInvalidDaysParameter() throws IOException {
            // Arrange
            List<Favorite> recentFavorites = Arrays.asList(createSampleFavorite());
            when(mockFavoritesService.getRecentFavorites(30)).thenReturn(recentFavorites);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/recent?days=invalid"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).getRecentFavorites(30); // Should fallback to default
        }
    }

    @Nested
    @DisplayName("GET /api/favorites/with-notes - Get Favorites With Notes")
    class GetFavoritesWithNotesTests {
        
        @Test
        @DisplayName("Should get favorites with notes successfully")
        void shouldGetFavoritesWithNotesSuccessfully() throws IOException {
            // Arrange
            List<Favorite> favoritesWithNotes = Arrays.asList(createSampleFavorite());
            when(mockFavoritesService.getFavoritesWithNotes()).thenReturn(favoritesWithNotes);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/with-notes"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).getFavoritesWithNotes();
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("favorites"));
        }
    }

    @Nested
    @DisplayName("GET /api/favorites/stats - Get User Stats")
    class GetUserStatsTests {
        
        @Test
        @DisplayName("Should get user stats successfully")
        void shouldGetUserStatsSuccessfully() throws IOException {
            // Arrange
            FavoritesService.FavoriteStats stats = new FavoritesService.FavoriteStats(10L, 5L, 3L);
            when(mockFavoritesService.getUserFavoriteStats(1L)).thenReturn(stats);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/stats?userId=1"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).getUserFavoriteStats(1L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("stats"));
        }
        
        @Test
        @DisplayName("Should handle missing userId in stats")
        void shouldHandleMissingUserIdInStats() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/stats"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("Missing required parameter: userId"));
        }
    }

    @Nested
    @DisplayName("GET /api/favorites/restaurant/{id} - Get Restaurant Favorites")
    class GetRestaurantFavoritesTests {
        
        @Test
        @DisplayName("Should get restaurant favorites successfully")
        void shouldGetRestaurantFavoritesSuccessfully() throws IOException {
            // Arrange
            List<Favorite> favorites = Arrays.asList(createSampleFavorite());
            when(mockFavoritesService.getRestaurantFavorites(2L)).thenReturn(favorites);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/restaurant/2"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).getRestaurantFavorites(2L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("favorites"));
        }
        
        @Test
        @DisplayName("Should get restaurant favorite count")
        void shouldGetRestaurantFavoriteCount() throws IOException {
            // Arrange
            when(mockFavoritesService.getRestaurantFavoriteCount(2L)).thenReturn(5L);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/restaurant/2?count=true"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).getRestaurantFavoriteCount(2L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("\"count\":5"));
        }
        
        @Test
        @DisplayName("Should handle invalid restaurant ID")
        void shouldHandleInvalidRestaurantId() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/restaurant/invalid"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("Invalid restaurantId format"));
        }
    }

    @Nested
    @DisplayName("GET /api/favorites/{id} - Get Favorite By ID")
    class GetFavoriteByIdTests {
        
        @Test
        @DisplayName("Should get favorite by ID successfully")
        void shouldGetFavoriteByIdSuccessfully() throws IOException {
            // Arrange
            Favorite favorite = createSampleFavorite();
            when(mockFavoritesService.getFavorite(1L)).thenReturn(favorite);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/1"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).getFavorite(1L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("favorite"));
        }
        
        @Test
        @DisplayName("Should handle favorite not found")
        void shouldHandleFavoriteNotFound() throws IOException {
            // Arrange
            when(mockFavoritesService.getFavorite(999L)).thenThrow(new NotFoundException("Favorite", 999L));
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/999"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(404), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":false"));
            assertTrue(response.contains("Favorite"));
        }
    }

    @Nested
    @DisplayName("POST /api/favorites/add - Add Favorite")
    class AddFavoriteTests {
        
        @Test
        @DisplayName("Should add favorite successfully")
        void shouldAddFavoriteSuccessfully() throws IOException {
            // Arrange
            Favorite favorite = createSampleFavorite();
            when(mockFavoritesService.addFavorite(1L, 2L, "Great food!")).thenReturn(favorite);
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("userId", 1L);
            requestData.put("restaurantId", 2L);
            requestData.put("notes", "Great food!");
            
            String requestBody = JsonUtil.toJson(requestData);
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/add"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).addFavorite(1L, 2L, "Great food!");
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("Added to favorites successfully"));
        }
        
        @Test
        @DisplayName("Should add favorite without notes")
        void shouldAddFavoriteWithoutNotes() throws IOException {
            // Arrange
            Favorite favorite = createSampleFavorite();
            when(mockFavoritesService.addFavorite(1L, 2L, null)).thenReturn(favorite);
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("userId", 1L);
            requestData.put("restaurantId", 2L);
            
            String requestBody = JsonUtil.toJson(requestData);
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/add"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).addFavorite(1L, 2L, null);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        @Test
        @DisplayName("Should handle missing required fields")
        void shouldHandleMissingRequiredFields() throws IOException {
            // Arrange
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("userId", 1L);
            // Missing restaurantId
            
            String requestBody = JsonUtil.toJson(requestData);
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/add"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("Missing required fields: userId, restaurantId"));
        }
        
        @Test
        @DisplayName("Should handle already favorited restaurant")
        void shouldHandleAlreadyFavoritedRestaurant() throws IOException {
            // Arrange
            when(mockFavoritesService.addFavorite(1L, 2L, null))
                .thenThrow(new IllegalArgumentException("Restaurant is already in user's favorites"));
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("userId", 1L);
            requestData.put("restaurantId", 2L);
            
            String requestBody = JsonUtil.toJson(requestData);
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/add"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":false"));
            assertTrue(response.contains("already in user's favorites"));
        }
    }

    @Nested
    @DisplayName("PUT /api/favorites/{id}/notes - Update Notes")
    class UpdateNotesTests {
        
        @Test
        @DisplayName("Should update notes successfully")
        void shouldUpdateNotesSuccessfully() throws IOException {
            // Arrange
            Favorite favorite = createSampleFavorite();
            when(mockFavoritesService.updateFavoriteNotes(1L, "Updated notes")).thenReturn(favorite);
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("notes", "Updated notes");
            
            String requestBody = JsonUtil.toJson(requestData);
            when(mockExchange.getRequestMethod()).thenReturn("PUT");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/1/notes"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).updateFavoriteNotes(1L, "Updated notes");
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("Favorite notes updated successfully"));
        }
        
        @Test
        @DisplayName("Should handle invalid favorite ID in notes update")
        void shouldHandleInvalidFavoriteIdInNotesUpdate() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("PUT");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/invalid/notes"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert - Should return 404 for invalid endpoint, not 400
            verify(mockExchange).sendResponseHeaders(eq(404), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("Endpoint not found") || response.contains("Invalid favoriteId format"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/favorites/remove - Remove Favorite")
    class RemoveFavoriteTests {
        
        @Test
        @DisplayName("Should remove favorite successfully")
        void shouldRemoveFavoriteSuccessfully() throws IOException {
            // Arrange
            when(mockFavoritesService.removeFavorite(1L, 2L)).thenReturn(true);
            when(mockExchange.getRequestMethod()).thenReturn("DELETE");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/remove?userId=1&restaurantId=2"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).removeFavorite(1L, 2L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("Removed from favorites successfully"));
            assertTrue(response.contains("\"removed\":true"));
        }
        
        @Test
        @DisplayName("Should handle missing parameters in remove")
        void shouldHandleMissingParametersInRemove() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("DELETE");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/remove?userId=1"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("Missing required parameters: userId, restaurantId"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/favorites/{id} - Delete By ID")
    class DeleteByIdTests {
        
        @Test
        @DisplayName("Should delete favorite by ID successfully")
        void shouldDeleteFavoriteByIdSuccessfully() throws IOException {
            // Arrange
            when(mockFavoritesService.deleteFavorite(1L)).thenReturn(true);
            when(mockExchange.getRequestMethod()).thenReturn("DELETE");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/1"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).deleteFavorite(1L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":true"));
            assertTrue(response.contains("Favorite deleted successfully"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle unsupported HTTP method")
        void shouldHandleUnsupportedHttpMethod() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("PATCH");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(405), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":false"));
            assertTrue(response.contains("Method not allowed: PATCH"));
        }
        
        @Test
        @DisplayName("Should handle invalid endpoint")
        void shouldHandleInvalidEndpoint() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/invalid-endpoint"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(404), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":false"));
            assertTrue(response.contains("Endpoint not found"));
        }
        
        @Test
        @DisplayName("Should handle service exceptions")
        void shouldHandleServiceExceptions() throws IOException {
            // Arrange
            when(mockFavoritesService.getUserFavorites(1L)).thenThrow(new RuntimeException("Database error"));
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites?userId=1"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(500), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":false"));
            assertTrue(response.contains("Internal server error"));
        }
        
        @Test
        @DisplayName("Should handle malformed JSON in POST")
        void shouldHandleMalformedJsonInPost() throws IOException {
            // Arrange
            String malformedJson = "{invalid json}";
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/add"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(malformedJson.getBytes()));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(500), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("\"success\":false"));
            assertTrue(response.contains("Internal server error"));
        }
    }

    @Nested
    @DisplayName("URL Encoding Tests")
    class UrlEncodingTests {
        
        @Test
        @DisplayName("Should handle URL encoded query parameters")
        void shouldHandleUrlEncodedQueryParameters() throws IOException {
            // Arrange
            when(mockFavoritesService.isFavorite(1L, 2L)).thenReturn(true);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/check?userId=1&restaurantId=2"));
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).isFavorite(1L, 2L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        @Test
        @DisplayName("Should handle special characters in URLs")
        void shouldHandleSpecialCharactersInUrls() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/favorites/check?userId=1&restaurantId=2&extra=%20test%20"));
            when(mockFavoritesService.isFavorite(1L, 2L)).thenReturn(false);
            
            // Act
            favoritesController.handle(mockExchange);
            
            // Assert
            verify(mockFavoritesService).isFavorite(1L, 2L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
        }
    }

    // Helper methods
    private Favorite createSampleFavorite() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Test User");
        
        Restaurant restaurant = new Restaurant();
        restaurant.setId(2L);
        restaurant.setName("Test Restaurant");
        
        Favorite favorite = new Favorite(user, restaurant, "Great food!");
        favorite.setId(1L);
        favorite.setCreatedAt(LocalDateTime.now());
        
        return favorite;
    }
}
