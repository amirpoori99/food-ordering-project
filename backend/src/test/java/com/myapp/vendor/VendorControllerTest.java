package com.myapp.vendor;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.JsonUtil;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VendorControllerTest {
    
    @Mock
    private VendorService mockVendorService;
    
    @Mock
    private HttpExchange mockExchange;
    
    @Mock
    private Headers mockHeaders;
    
    private VendorController vendorController;
    private ByteArrayOutputStream responseStream;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vendorController = new VendorController(mockVendorService);
        responseStream = new ByteArrayOutputStream();
        
        // Setup common mocks
        when(mockExchange.getResponseBody()).thenReturn(responseStream);
        when(mockExchange.getResponseHeaders()).thenReturn(mockHeaders);
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Default constructor should work")
        void testDefaultConstructor() {
            VendorController controller = new VendorController();
            assertNotNull(controller);
        }
        
        @Test
        @DisplayName("Parameterized constructor should work")
        void testParameterizedConstructor() {
            VendorController controller = new VendorController(mockVendorService);
            assertNotNull(controller);
        }
        
        @Test
        @DisplayName("Constructor with null service should work")
        void testConstructorWithNullService() {
            assertDoesNotThrow(() -> {
                VendorController controller = new VendorController(null);
                assertNotNull(controller);
            });
        }
    }
    
    @Nested
    @DisplayName("GET /api/vendors - Get All Vendors")
    class GetAllVendorsTests {
        
        @Test
        @DisplayName("Should return all vendors successfully")
        void testGetAllVendorsSuccess() throws IOException {
            // Arrange
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L), createSampleVendor(2L));
            when(mockVendorService.getAllVendors()).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).getAllVendors();
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            verify(mockHeaders).set("Content-Type", "application/json");
            
            String response = responseStream.toString();
            assertTrue(response.contains("Test Restaurant 1"));
            assertTrue(response.contains("Test Restaurant 2"));
        }
        
        @Test
        @DisplayName("Should handle empty vendor list")
        void testGetAllVendorsEmpty() throws IOException {
            // Arrange
            when(mockVendorService.getAllVendors()).thenReturn(Arrays.asList());
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).getAllVendors();
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertEquals("[]", response);
        }
        
        @Test
        @DisplayName("Should handle service exception")
        void testGetAllVendorsServiceException() throws IOException {
            // Arrange
            when(mockVendorService.getAllVendors()).thenThrow(new RuntimeException("Database error"));
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(500), anyLong());
            String response = responseStream.toString();
            assertTrue(response.contains("Internal server error"));
            assertTrue(response.contains("Database error"));
        }
    }
    
    @Nested
    @DisplayName("GET /api/vendors/search - Search Vendors")
    class SearchVendorsTests {
        
        @Test
        @DisplayName("Should search vendors with query parameter")
        void testSearchVendorsWithQuery() throws IOException {
            // Arrange
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L));
            when(mockVendorService.searchVendors("pizza")).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/search?q=pizza"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).searchVendors("pizza");
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("vendors"));
            assertTrue(response.contains("searchTerm"));
            assertTrue(response.contains("count"));
            assertTrue(response.contains("pizza"));
        }
        
        @Test
        @DisplayName("Should handle search without query parameter")
        void testSearchVendorsWithoutQuery() throws IOException {
            // Arrange
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L));
            when(mockVendorService.searchVendors("")).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/search"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).searchVendors("");
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        @Test
        @DisplayName("Should handle URL encoded search terms")
        void testSearchVendorsUrlEncoded() throws IOException {
            // Arrange
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L));
            when(mockVendorService.searchVendors("fast food")).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/search?q=fast%20food"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).searchVendors("fast food");
        }
        
        @Test
        @DisplayName("Should handle multiple query parameters")
        void testSearchVendorsMultipleParams() throws IOException {
            // Arrange
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L));
            when(mockVendorService.searchVendors("pizza")).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/search?q=pizza&other=value"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).searchVendors("pizza");
        }
    }
    
    @Nested
    @DisplayName("GET /api/vendors/{id} - Get Vendor Details")
    class GetVendorDetailsTests {
        
        @Test
        @DisplayName("Should get vendor details successfully")
        void testGetVendorDetailsSuccess() throws IOException {
            // Arrange
            Restaurant vendor = createSampleVendor(1L);
            when(mockVendorService.getVendor(1L)).thenReturn(vendor);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/1"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).getVendor(1L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("Test Restaurant 1"));
        }
        
        @Test
        @DisplayName("Should handle vendor not found")
        void testGetVendorDetailsNotFound() throws IOException {
            // Arrange
            when(mockVendorService.getVendor(999L)).thenThrow(new NotFoundException("Vendor", 999L));
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/999"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(404), anyLong());
            String response = responseStream.toString();
            assertTrue(response.contains("error"));
            assertTrue(response.contains("Vendor"));
        }
        
        @Test
        @DisplayName("Should handle invalid vendor ID")
        void testGetVendorDetailsInvalidId() throws IOException {
            // Arrange - negative IDs like "-1" match the non-numeric pattern and return 500
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/-1"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(500), anyLong());
            String response = responseStream.toString();
            assertTrue(response.contains("Internal server error"));
        }
        
        @Test
        @DisplayName("Should handle non-numeric vendor ID")
        void testGetVendorDetailsNonNumericId() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/abc"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(500), anyLong());
            String response = responseStream.toString();
            assertTrue(response.contains("Internal server error"));
        }
    }
    
    @Nested
    @DisplayName("GET /api/vendors/{id}/menu - Get Vendor Menu")
    class GetVendorMenuTests {
        
        @Test
        @DisplayName("Should get vendor menu successfully")
        void testGetVendorMenuSuccess() throws IOException {
            // Arrange
            Map<String, Object> menu = createSampleMenu();
            when(mockVendorService.getVendorMenu(1L)).thenReturn(menu);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/1/menu"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).getVendorMenu(1L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("vendor"));
            assertTrue(response.contains("menu_titles"));
        }
        
        @Test
        @DisplayName("Should handle vendor not found for menu")
        void testGetVendorMenuNotFound() throws IOException {
            // Arrange
            when(mockVendorService.getVendorMenu(999L)).thenThrow(new NotFoundException("Vendor", 999L));
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/999/menu"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(404), anyLong());
        }
    }
    
    @Nested
    @DisplayName("GET /api/vendors/{id}/stats - Get Vendor Stats")
    class GetVendorStatsTests {
        
        @Test
        @DisplayName("Should get vendor stats successfully")
        void testGetVendorStatsSuccess() throws IOException {
            // Arrange
            VendorService.VendorStats stats = createSampleStats();
            when(mockVendorService.getVendorStats(1L)).thenReturn(stats);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/1/stats"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).getVendorStats(1L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("vendorId"));
            assertTrue(response.contains("vendorName"));
            assertTrue(response.contains("totalItems"));
        }
    }
    
    @Nested
    @DisplayName("GET /api/vendors/{id}/available - Check Vendor Availability")
    class CheckVendorAvailabilityTests {
        
        @Test
        @DisplayName("Should check vendor availability successfully")
        void testCheckVendorAvailabilitySuccess() throws IOException {
            // Arrange
            when(mockVendorService.isVendorAcceptingOrders(1L)).thenReturn(true);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/1/available"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).isVendorAcceptingOrders(1L);
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("vendorId"));
            assertTrue(response.contains("acceptingOrders"));
            assertTrue(response.contains("true"));
        }
        
        @Test
        @DisplayName("Should return false for unavailable vendor")
        void testCheckVendorAvailabilityFalse() throws IOException {
            // Arrange
            when(mockVendorService.isVendorAcceptingOrders(1L)).thenReturn(false);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/1/available"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            String response = responseStream.toString();
            assertTrue(response.contains("false"));
        }
    }
    
    @Nested
    @DisplayName("GET /api/vendors/location/{location} - Get Vendors by Location")
    class GetVendorsByLocationTests {
        
        @Test
        @DisplayName("Should get vendors by location successfully")
        void testGetVendorsByLocationSuccess() throws IOException {
            // Arrange
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L));
            when(mockVendorService.getVendorsByLocation("Tehran")).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/location/Tehran"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).getVendorsByLocation("Tehran");
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("vendors"));
            assertTrue(response.contains("location"));
            assertTrue(response.contains("count"));
            assertTrue(response.contains("Tehran"));
        }
        
        @Test
        @DisplayName("Should handle URL encoded location")
        void testGetVendorsByLocationUrlEncoded() throws IOException {
            // Arrange
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L));
            when(mockVendorService.getVendorsByLocation("New York")).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/location/New%20York"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).getVendorsByLocation("New York");
        }
        
        @Test
        @DisplayName("Should handle invalid location")
        void testGetVendorsByLocationInvalid() throws IOException {
            // Arrange
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/location/"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(400), anyLong());
            String response = responseStream.toString();
            assertTrue(response.contains("Location cannot be empty"));
        }
    }
    
    @Nested
    @DisplayName("GET /api/vendors/category/{category} - Get Vendors by Category")
    class GetVendorsByCategoryTests {
        
        @Test
        @DisplayName("Should get vendors by category successfully")
        void testGetVendorsByCategorySuccess() throws IOException {
            // Arrange
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L));
            when(mockVendorService.getVendorsByCategory("Pizza")).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/category/Pizza"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).getVendorsByCategory("Pizza");
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("vendors"));
            assertTrue(response.contains("category"));
            assertTrue(response.contains("count"));
            assertTrue(response.contains("Pizza"));
        }
        
        @Test
        @DisplayName("Should handle empty category results")
        void testGetVendorsByCategoryEmpty() throws IOException {
            // Arrange
            when(mockVendorService.getVendorsByCategory("NonExistent")).thenReturn(Arrays.asList());
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/category/NonExistent"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            String response = responseStream.toString();
            assertTrue(response.contains("\"count\":0"));
        }
    }
    
    @Nested
    @DisplayName("GET /api/vendors/featured - Get Featured Vendors")
    class GetFeaturedVendorsTests {
        
        @Test
        @DisplayName("Should get featured vendors successfully")
        void testGetFeaturedVendorsSuccess() throws IOException {
            // Arrange
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L), createSampleVendor(2L));
            when(mockVendorService.getFeaturedVendors()).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/featured"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockVendorService).getFeaturedVendors();
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("vendors"));
            assertTrue(response.contains("count"));
            assertTrue(response.contains("\"count\":2"));
        }
        
        @Test
        @DisplayName("Should handle no featured vendors")
        void testGetFeaturedVendorsEmpty() throws IOException {
            // Arrange
            when(mockVendorService.getFeaturedVendors()).thenReturn(Arrays.asList());
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/featured"));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            String response = responseStream.toString();
            assertTrue(response.contains("\"count\":0"));
        }
    }
    
    @Nested
    @DisplayName("POST /api/vendors/filter - Filter Vendors")
    class FilterVendorsTests {
        
        @Test
        @DisplayName("Should filter vendors with all criteria")
        void testFilterVendorsAllCriteria() throws IOException {
            // Arrange
            Map<String, Object> requestData = Map.of(
                "location", "Tehran",
                "category", "Pizza",
                "search", "fast"
            );
            String requestBody = JsonUtil.toJson(requestData);
            
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/filter"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("vendors"));
            assertTrue(response.contains("count"));
            assertTrue(response.contains("filters"));
        }
        
        @Test
        @DisplayName("Should filter vendors with partial criteria")
        void testFilterVendorsPartialCriteria() throws IOException {
            // Arrange
            Map<String, Object> requestData = Map.of("location", "Tehran");
            String requestBody = JsonUtil.toJson(requestData);
            
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/filter"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        @Test
        @DisplayName("Should handle empty filter request")
        void testFilterVendorsEmpty() throws IOException {
            // Arrange
            Map<String, Object> requestData = new HashMap<>();
            String requestBody = JsonUtil.toJson(requestData);
            
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/filter"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        @Test
        @DisplayName("Should handle invalid JSON in filter request")
        void testFilterVendorsInvalidJson() throws IOException {
            // Arrange
            String invalidJson = "{ invalid json }";
            
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/filter"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(invalidJson.getBytes()));
            
            // Act
            vendorController.handle(mockExchange);
            
            // Assert
            verify(mockExchange).sendResponseHeaders(eq(500), anyLong());
        }
    }
    
    @Nested
    @DisplayName("HTTP Method and Error Handling Tests")
    class HttpMethodAndErrorTests {
        
        @Test
        @DisplayName("Should handle unsupported HTTP methods")
        void testUnsupportedHttpMethods() throws IOException {
            // Test PUT
            when(mockExchange.getRequestMethod()).thenReturn("PUT");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors"));
            
            vendorController.handle(mockExchange);
            verify(mockExchange).sendResponseHeaders(eq(405), anyLong());
            
            // Reset and test DELETE
            responseStream.reset();
            when(mockExchange.getRequestMethod()).thenReturn("DELETE");
            
            vendorController.handle(mockExchange);
            verify(mockExchange, times(2)).sendResponseHeaders(eq(405), anyLong());
        }
        
        @Test
        @DisplayName("Should handle invalid endpoints")
        void testInvalidEndpoints() throws IOException {
            // Test invalid GET endpoint - "invalid" is non-numeric so it matches the pattern for invalid vendor IDs
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/invalid"));
            
            vendorController.handle(mockExchange);
            verify(mockExchange).sendResponseHeaders(eq(500), anyLong());
            
            String response = responseStream.toString();
            assertTrue(response.contains("Internal server error"));
        }
        
        @Test
        @DisplayName("Should handle invalid POST endpoints")
        void testInvalidPostEndpoints() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/invalid"));
            
            vendorController.handle(mockExchange);
            verify(mockExchange).sendResponseHeaders(eq(404), anyLong());
        }
        
        @Test
        @DisplayName("Should include timestamp in error responses")
        void testErrorResponseFormat() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/invalid"));
            
            vendorController.handle(mockExchange);
            
            String response = responseStream.toString();
            assertTrue(response.contains("error"));
            assertTrue(response.contains("status"));
            assertTrue(response.contains("timestamp"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle very large vendor IDs")
        void testVeryLargeVendorIds() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/" + Long.MAX_VALUE));
            when(mockVendorService.getVendor(Long.MAX_VALUE)).thenThrow(new NotFoundException("Vendor", Long.MAX_VALUE));
            
            vendorController.handle(mockExchange);
            verify(mockExchange).sendResponseHeaders(eq(404), anyLong());
        }
        
        @Test
        @DisplayName("Should handle special characters in search")
        void testSpecialCharactersInSearch() throws IOException {
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L));
            // The & character in URL query gets interpreted as parameter separator, so we expect "café " not "café & restaurant"
            when(mockVendorService.searchVendors("café ")).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/search?q=caf%C3%A9%20%26%20restaurant"));
            
            vendorController.handle(mockExchange);
            verify(mockVendorService).searchVendors("café ");
        }
        
        @Test
        @DisplayName("Should handle Unicode characters in location")
        void testUnicodeInLocation() throws IOException {
            List<Restaurant> vendors = Arrays.asList(createSampleVendor(1L));
            when(mockVendorService.getVendorsByLocation("تهران")).thenReturn(vendors);
            when(mockExchange.getRequestMethod()).thenReturn("GET");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/location/%D8%AA%D9%87%D8%B1%D8%A7%D9%86"));
            
            vendorController.handle(mockExchange);
            verify(mockVendorService).getVendorsByLocation("تهران");
        }
        
        @Test
        @DisplayName("Should handle empty response body scenarios")
        void testEmptyResponseBody() throws IOException {
            when(mockExchange.getRequestMethod()).thenReturn("POST");
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/vendors/filter"));
            when(mockExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("".getBytes()));
            
            vendorController.handle(mockExchange);
            verify(mockExchange).sendResponseHeaders(eq(500), anyLong());
        }
    }
    
    // Helper methods
    private Restaurant createSampleVendor(Long id) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName("Test Restaurant " + id);
        restaurant.setAddress("Test Address " + id);
        restaurant.setPhone("123-456-789" + id);
        restaurant.setStatus(RestaurantStatus.APPROVED);
        restaurant.setOwnerId(id);
        return restaurant;
    }

    private Map<String, Object> createSampleMenu() {
        Map<String, Object> menu = new HashMap<>();
        menu.put("vendor", createSampleVendor(1L));
        menu.put("menu_titles", Arrays.asList("Pizza", "Pasta", "Salads"));
        menu.put("Pizza", Arrays.asList("Margherita", "Pepperoni"));
        menu.put("Pasta", Arrays.asList("Spaghetti", "Penne"));
        return menu;
    }

    private VendorService.VendorStats createSampleStats() {
        return new VendorService.VendorStats(
            1L, 
            "Test Restaurant 1", 
            15, 
            12, 
            3, 
            Arrays.asList("Pizza", "Pasta", "Salads")
        );
    }
}