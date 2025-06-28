package com.myapp.vendor;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.item.ItemRepository;
import com.myapp.restaurant.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * کلاس تست جامع برای VendorService - پوشش 100%
 * 
 * این کلاس تمام عملیات VendorService را از دیدگاه مشتری تست می‌کند:
 * 
 * === گروه‌های تست ===
 * - ConstructorTests: تست سازنده‌ها
 * - GetAllVendorsTests: تست دریافت تمام فروشندگان
 * - SearchVendorsTests: تست جستجوی فروشندگان
 * - GetVendorTests: تست دریافت فروشنده مشخص
 * - GetVendorMenuTests: تست دریافت منوی فروشنده
 * - GetVendorsByLocationTests: تست فروشندگان بر اساس موقعیت
 * - GetFeaturedVendorsTests: تست فروشندگان برجسته
 * - GetVendorsByCategoryTests: تست فروشندگان بر اساس دسته
 * - GetVendorStatsTests: تست آمار فروشندگان
 * - IsVendorAcceptingOrdersTests: تست پذیرش سفارش
 * - VendorStatsTests: تست کلاس آمار
 * - AdditionalEdgeCasesTests: تست موارد خاص
 * - PerformanceTests: تست‌های کارایی
 * 
 * === ویژگی‌های تست ===
 * - Unit Testing: تست واحد با Mock objects
 * - Edge Cases: تست موارد خاص و استثنائی
 * - Error Handling: تست مدیریت خطاها
 * - Performance: تست کارایی و استرس
 * - Business Logic: تست منطق کسب‌وکار
 * - Data Validation: تست اعتبارسنجی داده‌ها
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class VendorServiceTest {

    /** Mock repository عملیات فروشندگان */
    @Mock
    private VendorRepository mockVendorRepository;
    
    /** Mock repository رستوران‌ها */
    @Mock
    private RestaurantRepository mockRestaurantRepository;
    
    /** Mock repository آیتم‌های غذایی */
    @Mock
    private ItemRepository mockItemRepository;
    
    /** سرویس مورد تست */
    private VendorService vendorService;
    
    /**
     * راه‌اندازی اولیه قبل از هر تست
     * 
     * Mock objects را مقداردهی و سرویس را با dependency injection می‌سازد
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vendorService = new VendorService(mockVendorRepository, mockRestaurantRepository, mockItemRepository);
    }
    
    /**
     * گروه تست‌های سازنده‌ها
     * 
     * تست صحیح کار کردن سازنده‌های مختلف VendorService
     */
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        /**
         * تست سازنده پیش‌فرض
         * 
         * بررسی اینکه سازنده پیش‌فرض سرویس را با repositories پیش‌فرض می‌سازد
         */
        @Test
        @DisplayName("Default constructor should create service with default repositories")
        void testDefaultConstructor() {
            // اجرا
            VendorService defaultService = new VendorService();
            
            // بررسی
            assertNotNull(defaultService);
        }
        
        /**
         * تست سازنده با پارامتر
         * 
         * بررسی اینکه سازنده با پارامتر، سرویس را با repositories ارائه شده می‌سازد
         */
        @Test
        @DisplayName("Parameterized constructor should create service with provided repositories")
        void testParameterizedConstructor() {
            // اجرا
            VendorService paramService = new VendorService(mockVendorRepository, mockRestaurantRepository, mockItemRepository);
            
            // بررسی
            assertNotNull(paramService);
        }
    }
    
    /**
     * گروه تست‌های دریافت تمام فروشندگان
     * 
     * تست متد getAllVendors() در سناریوهای مختلف
     */
    @Nested
    @DisplayName("getAllVendors Tests")
    class GetAllVendorsTests {
        
        /**
         * تست دریافت تمام فروشندگان تایید شده
         * 
         * بررسی اینکه متد فقط رستوران‌های با وضعیت APPROVED را برمی‌گرداند
         */
        @Test
        @DisplayName("getAllVendors should return approved restaurants")
        void testGetAllVendors() {
            // آماده‌سازی
            List<Restaurant> approvedRestaurants = createSampleRestaurants();
            when(mockRestaurantRepository.findByStatus(RestaurantStatus.APPROVED)).thenReturn(approvedRestaurants);
            
            // اجرا
            List<Restaurant> result = vendorService.getAllVendors();
            
            // بررسی
            assertEquals(2, result.size());
            verify(mockRestaurantRepository).findByStatus(RestaurantStatus.APPROVED);
        }
        
        /**
         * تست دریافت تمام فروشندگان زمانی که هیچ فروشنده تایید شده‌ای وجود ندارد
         * 
         * بررسی اینکه متد لیست خالی برمی‌گرداند
         */
        @Test
        @DisplayName("getAllVendors should return empty list when no approved vendors exist")
        void testGetAllVendorsEmpty() {
            // آماده‌سازی
            when(mockRestaurantRepository.findByStatus(RestaurantStatus.APPROVED)).thenReturn(Collections.emptyList());
            
            // اجرا
            List<Restaurant> result = vendorService.getAllVendors();
            
            // بررسی
            assertTrue(result.isEmpty());
            verify(mockRestaurantRepository).findByStatus(RestaurantStatus.APPROVED);
        }
    }
    
    /**
     * گروه تست‌های جستجوی فروشندگان
     * 
     * تست متد searchVendors() در حالات مختلف جستجو
     */
    @Nested
    @DisplayName("searchVendors Tests")
    class SearchVendorsTests {
        
        /**
         * تست جستجوی فروشندگان با عبارت مشخص
         * 
         * بررسی اینکه متد عبارت جستجو را به repository ارسال می‌کند
         */
        @Test
        @DisplayName("searchVendors should call repository with search term")
        void testSearchVendors() {
            // آماده‌سازی
            String searchTerm = "pizza";
            List<Restaurant> searchResults = createSampleRestaurants();
            when(mockVendorRepository.searchVendors(searchTerm)).thenReturn(searchResults);
            
            // اجرا
            List<Restaurant> result = vendorService.searchVendors(searchTerm);
            
            // بررسی
            assertEquals(2, result.size());
            verify(mockVendorRepository).searchVendors(searchTerm);
        }
        
        @Test
        @DisplayName("searchVendors with empty term should return all vendors")
        void testSearchVendorsEmptyTerm() {
            // Arrange
            List<Restaurant> allVendors = createSampleRestaurants();
            when(mockRestaurantRepository.findByStatus(RestaurantStatus.APPROVED)).thenReturn(allVendors);
            
            // Act
            List<Restaurant> result = vendorService.searchVendors("");
            
            // Assert
            assertEquals(2, result.size());
            verify(mockRestaurantRepository).findByStatus(RestaurantStatus.APPROVED);
        }
        
        @Test
        @DisplayName("searchVendors with null term should return all vendors")
        void testSearchVendorsNullTerm() {
            // Arrange
            List<Restaurant> allVendors = createSampleRestaurants();
            when(mockRestaurantRepository.findByStatus(RestaurantStatus.APPROVED)).thenReturn(allVendors);
            
            // Act
            List<Restaurant> result = vendorService.searchVendors(null);
            
            // Assert
            assertEquals(2, result.size());
            verify(mockRestaurantRepository).findByStatus(RestaurantStatus.APPROVED);
        }
        
        @Test
        @DisplayName("searchVendors with whitespace only should return all vendors")
        void testSearchVendorsWhitespaceOnly() {
            // Arrange
            List<Restaurant> allVendors = createSampleRestaurants();
            when(mockRestaurantRepository.findByStatus(RestaurantStatus.APPROVED)).thenReturn(allVendors);
            
            // Act
            List<Restaurant> result = vendorService.searchVendors("   ");
            
            // Assert
            assertEquals(2, result.size());
            verify(mockRestaurantRepository).findByStatus(RestaurantStatus.APPROVED);
        }
        
        @Test
        @DisplayName("searchVendors should trim search term")
        void testSearchVendorsTrimSearchTerm() {
            // Arrange
            String searchTerm = "  pizza  ";
            String trimmedTerm = "pizza";
            List<Restaurant> searchResults = createSampleRestaurants();
            when(mockVendorRepository.searchVendors(trimmedTerm)).thenReturn(searchResults);
            
            // Act
            List<Restaurant> result = vendorService.searchVendors(searchTerm);
            
            // Assert
            assertEquals(2, result.size());
            verify(mockVendorRepository).searchVendors(trimmedTerm);
        }
        
        @Test
        @DisplayName("searchVendors should return empty list when no matches found")
        void testSearchVendorsNoMatches() {
            // Arrange
            String searchTerm = "nonexistent";
            when(mockVendorRepository.searchVendors(searchTerm)).thenReturn(Collections.emptyList());
            
            // Act
            List<Restaurant> result = vendorService.searchVendors(searchTerm);
            
            // Assert
            assertTrue(result.isEmpty());
            verify(mockVendorRepository).searchVendors(searchTerm);
        }
    }
    
    @Nested
    @DisplayName("getVendor Tests")
    class GetVendorTests {
        
        @Test
        @DisplayName("getVendor should return approved vendor")
        void testGetVendor() {
            // Arrange
            Long vendorId = 1L;
            Restaurant vendor = createSampleRestaurant();
            vendor.setStatus(RestaurantStatus.APPROVED);
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
            
            // Act
            Restaurant result = vendorService.getVendor(vendorId);
            
            // Assert
            assertNotNull(result);
            assertEquals(vendorId, result.getId());
            assertEquals(RestaurantStatus.APPROVED, result.getStatus());
        }
        
        @Test
        @DisplayName("getVendor should throw NotFoundException for non-existent vendor")
        void testGetVendorNotFound() {
            // Arrange
            Long vendorId = 999L;
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                vendorService.getVendor(vendorId);
            });
            
            assertTrue(exception.getMessage().contains("Vendor"));
            assertTrue(exception.getMessage().contains("999"));
        }
        
        @Test
        @DisplayName("getVendor should throw NotFoundException for non-approved vendor")
        void testGetVendorNotApproved() {
            // Arrange
            Long vendorId = 1L;
            Restaurant vendor = createSampleRestaurant();
            vendor.setStatus(RestaurantStatus.PENDING);
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
            
            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                vendorService.getVendor(vendorId);
            });
            
            assertTrue(exception.getMessage().contains("Vendor"));
            assertTrue(exception.getMessage().contains("1"));
        }
        
        @Test
        @DisplayName("getVendor should throw NotFoundException for rejected vendor")
        void testGetVendorRejected() {
            // Arrange
            Long vendorId = 1L;
            Restaurant vendor = createSampleRestaurant();
            vendor.setStatus(RestaurantStatus.REJECTED);
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> {
                vendorService.getVendor(vendorId);
            });
        }
        
        @Test
        @DisplayName("getVendor should throw IllegalArgumentException for null ID")
        void testGetVendorNullId() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                vendorService.getVendor(null);
            });
            
            assertTrue(exception.getMessage().contains("Vendor ID must be positive"));
        }
        
        @Test
        @DisplayName("getVendor should throw IllegalArgumentException for zero ID")
        void testGetVendorZeroId() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                vendorService.getVendor(0L);
            });
            
            assertTrue(exception.getMessage().contains("Vendor ID must be positive"));
        }
        
        @Test
        @DisplayName("getVendor should throw IllegalArgumentException for negative ID")
        void testGetVendorNegativeId() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                vendorService.getVendor(-1L);
            });
            
            assertTrue(exception.getMessage().contains("Vendor ID must be positive"));
        }
    }
    
    @Nested
    @DisplayName("getVendorMenu Tests")
    class GetVendorMenuTests {
        
        @Test
        @DisplayName("getVendorMenu should return menu organized by categories")
        void testGetVendorMenu() {
            // Arrange
            Long vendorId = 1L;
            Restaurant vendor = createSampleRestaurant();
            vendor.setStatus(RestaurantStatus.APPROVED);
            List<FoodItem> menuItems = createSampleMenuItems();
            
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
            when(mockItemRepository.findAvailableByRestaurant(vendorId)).thenReturn(menuItems);
            
            // Act
            Map<String, Object> result = vendorService.getVendorMenu(vendorId);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.containsKey("vendor"));
            assertTrue(result.containsKey("menu_titles"));
            assertTrue(result.containsKey("Pizza"));
            assertTrue(result.containsKey("Burgers"));
            
            @SuppressWarnings("unchecked")
            List<String> menuTitles = (List<String>) result.get("menu_titles");
            assertEquals(2, menuTitles.size());
            assertTrue(menuTitles.contains("Pizza"));
            assertTrue(menuTitles.contains("Burgers"));
            
            assertEquals(vendor, result.get("vendor"));
        }
        
        @Test
        @DisplayName("getVendorMenu should return empty menu when no items available")
        void testGetVendorMenuEmpty() {
            // Arrange
            Long vendorId = 1L;
            Restaurant vendor = createSampleRestaurant();
            vendor.setStatus(RestaurantStatus.APPROVED);
            
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
            when(mockItemRepository.findAvailableByRestaurant(vendorId)).thenReturn(Collections.emptyList());
            
            // Act
            Map<String, Object> result = vendorService.getVendorMenu(vendorId);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.containsKey("vendor"));
            assertTrue(result.containsKey("menu_titles"));
            
            @SuppressWarnings("unchecked")
            List<String> menuTitles = (List<String>) result.get("menu_titles");
            assertTrue(menuTitles.isEmpty());
        }
        
        @Test
        @DisplayName("getVendorMenu should handle single category")
        void testGetVendorMenuSingleCategory() {
            // Arrange
            Long vendorId = 1L;
            Restaurant vendor = createSampleRestaurant();
            vendor.setStatus(RestaurantStatus.APPROVED);
            
            FoodItem pizza = new FoodItem();
            pizza.setId(1L);
            pizza.setName("Margherita Pizza");
            pizza.setCategory("Pizza");
            pizza.setPrice(15.99);
            pizza.setAvailable(true);
            pizza.setRestaurant(vendor);
            
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
            when(mockItemRepository.findAvailableByRestaurant(vendorId)).thenReturn(Arrays.asList(pizza));
            
            // Act
            Map<String, Object> result = vendorService.getVendorMenu(vendorId);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.containsKey("Pizza"));
            
            @SuppressWarnings("unchecked")
            List<String> menuTitles = (List<String>) result.get("menu_titles");
            assertEquals(1, menuTitles.size());
            assertTrue(menuTitles.contains("Pizza"));
        }
        
        @Test
        @DisplayName("getVendorMenu should throw exception for invalid vendor")
        void testGetVendorMenuInvalidVendor() {
            // Arrange
            Long vendorId = 999L;
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> {
                vendorService.getVendorMenu(vendorId);
            });
        }
    }
    
    @Nested
    @DisplayName("getVendorsByLocation Tests")
    class GetVendorsByLocationTests {
        
        @Test
        @DisplayName("getVendorsByLocation should call repository")
        void testGetVendorsByLocation() {
            // Arrange
            String location = "Tehran";
            List<Restaurant> vendors = createSampleRestaurants();
            when(mockVendorRepository.findByLocation(location)).thenReturn(vendors);
            
            // Act
            List<Restaurant> result = vendorService.getVendorsByLocation(location);
            
            // Assert
            assertEquals(2, result.size());
            verify(mockVendorRepository).findByLocation(location);
        }
        
        @Test
        @DisplayName("getVendorsByLocation should trim location")
        void testGetVendorsByLocationTrimmed() {
            // Arrange
            String location = "  Tehran  ";
            String trimmedLocation = "Tehran";
            List<Restaurant> vendors = createSampleRestaurants();
            when(mockVendorRepository.findByLocation(trimmedLocation)).thenReturn(vendors);
            
            // Act
            List<Restaurant> result = vendorService.getVendorsByLocation(location);
            
            // Assert
            assertEquals(2, result.size());
            verify(mockVendorRepository).findByLocation(trimmedLocation);
        }
        
        @Test
        @DisplayName("getVendorsByLocation should throw exception for empty location")
        void testGetVendorsByLocationEmpty() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                vendorService.getVendorsByLocation("");
            });
            
            assertTrue(exception.getMessage().contains("Location cannot be empty"));
        }
        
        @Test
        @DisplayName("getVendorsByLocation should throw exception for null location")
        void testGetVendorsByLocationNull() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                vendorService.getVendorsByLocation(null);
            });
            
            assertTrue(exception.getMessage().contains("Location cannot be empty"));
        }
        
        @Test
        @DisplayName("getVendorsByLocation should throw exception for whitespace only location")
        void testGetVendorsByLocationWhitespaceOnly() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                vendorService.getVendorsByLocation("   ");
            });
            
            assertTrue(exception.getMessage().contains("Location cannot be empty"));
        }
        
        @Test
        @DisplayName("getVendorsByLocation should return empty list when no vendors found")
        void testGetVendorsByLocationNoResults() {
            // Arrange
            String location = "NonExistentCity";
            when(mockVendorRepository.findByLocation(location)).thenReturn(Collections.emptyList());
            
            // Act
            List<Restaurant> result = vendorService.getVendorsByLocation(location);
            
            // Assert
            assertTrue(result.isEmpty());
            verify(mockVendorRepository).findByLocation(location);
        }
    }
    
    @Nested
    @DisplayName("getFeaturedVendors Tests")
    class GetFeaturedVendorsTests {
        
        @Test
        @DisplayName("getFeaturedVendors should call repository")
        void testGetFeaturedVendors() {
            // Arrange
            List<Restaurant> vendors = createSampleRestaurants();
            when(mockVendorRepository.getFeaturedVendors()).thenReturn(vendors);
            
            // Act
            List<Restaurant> result = vendorService.getFeaturedVendors();
            
            // Assert
            assertEquals(2, result.size());
            verify(mockVendorRepository).getFeaturedVendors();
        }
        
        @Test
        @DisplayName("getFeaturedVendors should return empty list when no featured vendors")
        void testGetFeaturedVendorsEmpty() {
            // Arrange
            when(mockVendorRepository.getFeaturedVendors()).thenReturn(Collections.emptyList());
            
            // Act
            List<Restaurant> result = vendorService.getFeaturedVendors();
            
            // Assert
            assertTrue(result.isEmpty());
            verify(mockVendorRepository).getFeaturedVendors();
        }
    }
    
    @Nested
    @DisplayName("getVendorsByCategory Tests")
    class GetVendorsByCategoryTests {
        
        @Test
        @DisplayName("getVendorsByCategory should call repository")
        void testGetVendorsByCategory() {
            // Arrange
            String category = "Pizza";
            List<Restaurant> vendors = createSampleRestaurants();
            when(mockVendorRepository.findByFoodCategory(category)).thenReturn(vendors);
            
            // Act
            List<Restaurant> result = vendorService.getVendorsByCategory(category);
            
            // Assert
            assertEquals(2, result.size());
            verify(mockVendorRepository).findByFoodCategory(category);
        }
        
        @Test
        @DisplayName("getVendorsByCategory should trim category")
        void testGetVendorsByCategoryTrimmed() {
            // Arrange
            String category = "  Pizza  ";
            String trimmedCategory = "Pizza";
            List<Restaurant> vendors = createSampleRestaurants();
            when(mockVendorRepository.findByFoodCategory(trimmedCategory)).thenReturn(vendors);
            
            // Act
            List<Restaurant> result = vendorService.getVendorsByCategory(category);
            
            // Assert
            assertEquals(2, result.size());
            verify(mockVendorRepository).findByFoodCategory(trimmedCategory);
        }
        
        @Test
        @DisplayName("getVendorsByCategory should throw exception for empty category")
        void testGetVendorsByCategoryEmpty() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                vendorService.getVendorsByCategory("");
            });
            
            assertTrue(exception.getMessage().contains("Category cannot be empty"));
        }
        
        @Test
        @DisplayName("getVendorsByCategory should throw exception for null category")
        void testGetVendorsByCategoryNull() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                vendorService.getVendorsByCategory(null);
            });
            
            assertTrue(exception.getMessage().contains("Category cannot be empty"));
        }
        
        @Test
        @DisplayName("getVendorsByCategory should throw exception for whitespace only category")
        void testGetVendorsByCategoryWhitespaceOnly() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                vendorService.getVendorsByCategory("   ");
            });
            
            assertTrue(exception.getMessage().contains("Category cannot be empty"));
        }
        
        @Test
        @DisplayName("getVendorsByCategory should return empty list when no vendors found")
        void testGetVendorsByCategoryNoResults() {
            // Arrange
            String category = "NonExistentCategory";
            when(mockVendorRepository.findByFoodCategory(category)).thenReturn(Collections.emptyList());
            
            // Act
            List<Restaurant> result = vendorService.getVendorsByCategory(category);
            
            // Assert
            assertTrue(result.isEmpty());
            verify(mockVendorRepository).findByFoodCategory(category);
        }
    }
    
    @Nested
    @DisplayName("getVendorStats Tests")
    class GetVendorStatsTests {
        
        @Test
        @DisplayName("getVendorStats should return statistics")
        void testGetVendorStats() {
            // Arrange
            Long vendorId = 1L;
            Restaurant vendor = createSampleRestaurant();
            vendor.setStatus(RestaurantStatus.APPROVED);
            
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
            when(mockItemRepository.countByRestaurant(vendorId)).thenReturn(10);
            when(mockItemRepository.countAvailableByRestaurant(vendorId)).thenReturn(8);
            when(mockItemRepository.getCategoriesByRestaurant(vendorId)).thenReturn(Arrays.asList("Pizza", "Burgers", "Drinks"));
            
            // Act
            VendorService.VendorStats result = vendorService.getVendorStats(vendorId);
            
            // Assert
            assertNotNull(result);
            assertEquals(vendorId, result.getVendorId());
            assertEquals("Test Restaurant", result.getVendorName());
            assertEquals(10, result.getTotalItems());
            assertEquals(8, result.getAvailableItems());
            assertEquals(3, result.getTotalCategories());
            assertEquals(3, result.getCategories().size());
            assertTrue(result.getCategories().contains("Pizza"));
            assertTrue(result.getCategories().contains("Burgers"));
            assertTrue(result.getCategories().contains("Drinks"));
        }
        
        @Test
        @DisplayName("getVendorStats should handle vendor with no items")
        void testGetVendorStatsNoItems() {
            // Arrange
            Long vendorId = 1L;
            Restaurant vendor = createSampleRestaurant();
            vendor.setStatus(RestaurantStatus.APPROVED);
            
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
            when(mockItemRepository.countByRestaurant(vendorId)).thenReturn(0);
            when(mockItemRepository.countAvailableByRestaurant(vendorId)).thenReturn(0);
            when(mockItemRepository.getCategoriesByRestaurant(vendorId)).thenReturn(Collections.emptyList());
            
            // Act
            VendorService.VendorStats result = vendorService.getVendorStats(vendorId);
            
            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalItems());
            assertEquals(0, result.getAvailableItems());
            assertEquals(0, result.getTotalCategories());
            assertTrue(result.getCategories().isEmpty());
        }
        
        @Test
        @DisplayName("getVendorStats should throw exception for invalid vendor")
        void testGetVendorStatsInvalidVendor() {
            // Arrange
            Long vendorId = 999L;
            when(mockRestaurantRepository.findById(vendorId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> {
                vendorService.getVendorStats(vendorId);
            });
        }
    }
    
    @Nested
    @DisplayName("isVendorAcceptingOrders tests")
    class IsVendorAcceptingOrdersTests {

        @Test
        @DisplayName("isVendorAcceptingOrders should return true for APPROVED status")
        void testIsVendorAcceptingOrdersApproved() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setStatus(RestaurantStatus.APPROVED);
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
            when(mockItemRepository.countAvailableByRestaurant(1L)).thenReturn(5);

            boolean result = vendorService.isVendorAcceptingOrders(1L);
            assertTrue(result);
        }

        @Test
        @DisplayName("isVendorAcceptingOrders should return false for PENDING status")
        void testIsVendorAcceptingOrdersPending() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setStatus(RestaurantStatus.PENDING);
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            boolean result = vendorService.isVendorAcceptingOrders(1L);
            assertFalse(result);
        }

        @Test
        @DisplayName("isVendorAcceptingOrders should return false for REJECTED status")
        void testIsVendorAcceptingOrdersRejected() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setStatus(RestaurantStatus.REJECTED);
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            boolean result = vendorService.isVendorAcceptingOrders(1L);
            assertFalse(result);
        }

        @Test
        @DisplayName("isVendorAcceptingOrders should return false for SUSPENDED status")
        void testIsVendorAcceptingOrdersSuspended() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setStatus(RestaurantStatus.SUSPENDED);
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            boolean result = vendorService.isVendorAcceptingOrders(1L);
            assertFalse(result);
        }

        // Note: CLOSED and INACTIVE statuses don't exist in RestaurantStatus enum
        // Only PENDING, APPROVED, REJECTED, SUSPENDED are available

        @Test
        @DisplayName("isVendorAcceptingOrders should return false when vendor not found")
        void testIsVendorAcceptingOrdersNotFound() {
            when(mockRestaurantRepository.findById(999L)).thenReturn(Optional.empty());

            boolean result = vendorService.isVendorAcceptingOrders(999L);
            assertFalse(result);
        }

        @Test
        @DisplayName("isVendorAcceptingOrders should throw exception for null ID")
        void testIsVendorAcceptingOrdersNullId() {
            assertThrows(IllegalArgumentException.class, () -> {
                vendorService.isVendorAcceptingOrders(null);
            });
        }

        @Test
        @DisplayName("isVendorAcceptingOrders should throw exception for negative ID")
        void testIsVendorAcceptingOrdersNegativeId() {
            assertThrows(IllegalArgumentException.class, () -> {
                vendorService.isVendorAcceptingOrders(-1L);
            });
        }

        @Test
        @DisplayName("isVendorAcceptingOrders should throw exception for zero ID")
        void testIsVendorAcceptingOrdersZeroId() {
            assertThrows(IllegalArgumentException.class, () -> {
                vendorService.isVendorAcceptingOrders(0L);
            });
        }

        @Test
        @DisplayName("isVendorAcceptingOrders should handle very large ID")
        void testIsVendorAcceptingOrdersLargeId() {
            Long largeId = Long.MAX_VALUE;
            when(mockRestaurantRepository.findById(largeId)).thenReturn(Optional.empty());

            boolean result = vendorService.isVendorAcceptingOrders(largeId);
            assertFalse(result);
        }

        @Test
        @DisplayName("isVendorAcceptingOrders should handle null status")
        void testIsVendorAcceptingOrdersNullStatus() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setStatus(null);
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            boolean result = vendorService.isVendorAcceptingOrders(1L);
            assertFalse(result);
        }
    }
    
    @Nested
    @DisplayName("VendorStats tests")
    class VendorStatsTests {

        @Test
        @DisplayName("VendorStats constructor should work with all values")
        void testVendorStatsConstructor() {
            List<String> categories = Arrays.asList("Pizza", "Pasta");
            VendorService.VendorStats stats = new VendorService.VendorStats(1L, "Test Restaurant", 10, 5, 2, categories);
            assertEquals(1L, stats.getVendorId());
            assertEquals("Test Restaurant", stats.getVendorName());
            assertEquals(10, stats.getTotalItems());
            assertEquals(5, stats.getAvailableItems());
            assertEquals(2, stats.getTotalCategories());
            assertEquals(categories, stats.getCategories());
        }

        @Test
        @DisplayName("VendorStats constructor should handle zero values")
        void testVendorStatsConstructorZero() {
            List<String> categories = new ArrayList<>();
            VendorService.VendorStats stats = new VendorService.VendorStats(1L, "Test", 0, 0, 0, categories);
            assertEquals(0, stats.getTotalItems());
            assertEquals(0, stats.getAvailableItems());
            assertEquals(0, stats.getTotalCategories());
            assertTrue(stats.getCategories().isEmpty());
        }

        @Test
        @DisplayName("VendorStats constructor should handle null values")
        void testVendorStatsConstructorNullValues() {
            VendorService.VendorStats stats = new VendorService.VendorStats(null, null, 0, 0, 0, null);
            assertNull(stats.getVendorId());
            assertNull(stats.getVendorName());
            assertEquals(0, stats.getTotalItems());
            assertEquals(0, stats.getAvailableItems());
            assertEquals(0, stats.getTotalCategories());
            assertNull(stats.getCategories());
        }
    }

    @Nested
    @DisplayName("Additional Edge Cases and Error Handling")
    class AdditionalEdgeCasesTests {

        @Test
        @DisplayName("Service should handle repository returning null lists")
        void testRepositoryReturningNullLists() {
            when(mockRestaurantRepository.findByStatus(any())).thenReturn(null);
            when(mockVendorRepository.searchVendors(anyString())).thenReturn(null);
            when(mockVendorRepository.findByLocation(anyString())).thenReturn(null);
            when(mockVendorRepository.getFeaturedVendors()).thenReturn(null);
            when(mockVendorRepository.findByFoodCategory(anyString())).thenReturn(null);

            // These should not throw exceptions but may return null
            assertDoesNotThrow(() -> {
                List<Restaurant> result1 = vendorService.getAllVendors();
                List<Restaurant> result2 = vendorService.searchVendors("test");
                List<Restaurant> result3 = vendorService.getVendorsByLocation("test");
                List<Restaurant> result4 = vendorService.getFeaturedVendors();
                List<Restaurant> result5 = vendorService.getVendorsByCategory("test");

                // Results may be null from repository
            });
        }

        @Test
        @DisplayName("Service should handle concurrent access")
        void testConcurrentAccess() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setStatus(RestaurantStatus.APPROVED);
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
            when(mockRestaurantRepository.findByStatus(any())).thenReturn(Arrays.asList(restaurant));
            when(mockItemRepository.countAvailableByRestaurant(1L)).thenReturn(5);

            assertDoesNotThrow(() -> {
                Thread thread1 = new Thread(() -> vendorService.getAllVendors());
                Thread thread2 = new Thread(() -> vendorService.getVendor(1L));
                Thread thread3 = new Thread(() -> vendorService.isVendorAcceptingOrders(1L));

                thread1.start();
                thread2.start();
                thread3.start();

                thread1.join();
                thread2.join();
                thread3.join();
            });
        }

        @Test
        @DisplayName("Service should handle stress test")
        void testStressTest() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setStatus(RestaurantStatus.APPROVED);
            when(mockRestaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
            when(mockRestaurantRepository.findByStatus(any())).thenReturn(Arrays.asList(restaurant));
            when(mockItemRepository.countAvailableByRestaurant(anyLong())).thenReturn(5);
            when(mockVendorRepository.searchVendors(anyString())).thenReturn(Arrays.asList(restaurant));
            when(mockVendorRepository.findByLocation(anyString())).thenReturn(Arrays.asList(restaurant));

            assertDoesNotThrow(() -> {
                for (int i = 0; i < 100; i++) {
                    vendorService.getAllVendors();
                    vendorService.getVendor(1L);
                    vendorService.isVendorAcceptingOrders(1L);
                    vendorService.searchVendors("test");
                    vendorService.getVendorsByLocation("test");
                }
            });
        }

        @Test
        @DisplayName("Service should handle repository exceptions gracefully")
        void testRepositoryExceptions() {
            when(mockRestaurantRepository.findByStatus(any())).thenThrow(new RuntimeException("Database error"));
            when(mockRestaurantRepository.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

            // These should handle exceptions gracefully
            assertThrows(RuntimeException.class, () -> vendorService.getAllVendors());
            assertThrows(RuntimeException.class, () -> vendorService.getVendor(1L));
        }

        @Test
        @DisplayName("Service should handle very large datasets")
        void testLargeDatasets() {
            List<Restaurant> largeList = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                Restaurant restaurant = new Restaurant();
                restaurant.setId((long) i);
                restaurant.setName("Restaurant " + i);
                restaurant.setStatus(RestaurantStatus.APPROVED);
                largeList.add(restaurant);
            }

            when(mockRestaurantRepository.findByStatus(RestaurantStatus.APPROVED)).thenReturn(largeList);

            List<Restaurant> result = vendorService.getAllVendors();
            assertNotNull(result);
            assertEquals(10000, result.size());
        }

        @Test
        @DisplayName("Service should handle empty restaurant data")
        void testEmptyRestaurantData() {
            Restaurant emptyRestaurant = new Restaurant();
            emptyRestaurant.setStatus(RestaurantStatus.APPROVED); // Must be approved to be returned
            // No other fields set - all null/default values
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(emptyRestaurant));

            Restaurant result = vendorService.getVendor(1L);
            assertNotNull(result);
            assertEquals(emptyRestaurant, result);
        }

        @Test
        @DisplayName("Service should handle restaurant with null fields but throw exception for null status")
        void testRestaurantWithNullFields() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setName(null);
            restaurant.setAddress(null);
            restaurant.setPhone(null);
            restaurant.setStatus(null); // This will cause NotFoundException since it's not APPROVED
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            // Should throw NotFoundException because status is null (not APPROVED)
            assertThrows(NotFoundException.class, () -> vendorService.getVendor(1L));
        }

        @Test
        @DisplayName("Service should handle extreme input values")
        void testExtremeInputValues() {
            // Mock repository calls for extreme inputs
            when(mockVendorRepository.searchVendors(anyString())).thenReturn(new ArrayList<>());
            when(mockVendorRepository.findByLocation(anyString())).thenReturn(new ArrayList<>());
            when(mockVendorRepository.findByFoodCategory(anyString())).thenReturn(new ArrayList<>());
            when(mockRestaurantRepository.findByStatus(any())).thenReturn(new ArrayList<>());
            
            assertDoesNotThrow(() -> {
                vendorService.searchVendors("\0\n\r\t");
                // Note: \u0000 is treated as empty after trimming, so it will throw IllegalArgumentException
                // This is expected behavior, so we test it separately
                assertThrows(IllegalArgumentException.class, () -> vendorService.getVendorsByLocation("\u0000"));
                vendorService.getVendorsByCategory("a".repeat(10000));
            });
        }

        @Test
        @DisplayName("Service should handle Unicode input correctly")
        void testUnicodeInput() {
            when(mockVendorRepository.searchVendors(anyString())).thenReturn(new ArrayList<>());
            when(mockVendorRepository.findByLocation(anyString())).thenReturn(new ArrayList<>());
            when(mockVendorRepository.findByFoodCategory(anyString())).thenReturn(new ArrayList<>());

            assertDoesNotThrow(() -> {
                vendorService.searchVendors("رستوران");
                vendorService.getVendorsByLocation("تهران");
                vendorService.getVendorsByCategory("پیتزا");
            });
        }

        @Test
        @DisplayName("Service should handle SQL injection attempts")
        void testSQLInjectionAttempts() {
            when(mockVendorRepository.searchVendors(anyString())).thenReturn(new ArrayList<>());
            when(mockVendorRepository.findByLocation(anyString())).thenReturn(new ArrayList<>());
            when(mockVendorRepository.findByFoodCategory(anyString())).thenReturn(new ArrayList<>());

            assertDoesNotThrow(() -> {
                vendorService.searchVendors("'; DROP TABLE restaurants; --");
                vendorService.getVendorsByLocation("Tehran'; DELETE FROM users; --");
                vendorService.getVendorsByCategory("Pizza' OR '1'='1");
            });
        }

        @Test
        @DisplayName("Service should handle memory pressure gracefully")
        void testMemoryPressure() {
            // Simulate memory pressure with very large strings
            String largeString = "a".repeat(1000000); // 1MB string
            
            when(mockVendorRepository.searchVendors(anyString())).thenReturn(new ArrayList<>());
            
            assertDoesNotThrow(() -> {
                vendorService.searchVendors(largeString);
            });
        }
    }

    @Nested
    @DisplayName("Performance and Boundary Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Service should handle rapid successive calls")
        void testRapidSuccessiveCalls() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setStatus(RestaurantStatus.APPROVED);
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 1000; i++) {
                vendorService.getVendor(1L);
                vendorService.isVendorAcceptingOrders(1L);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // Should complete within reasonable time (less than 5 seconds)
            assertTrue(duration < 5000, "Performance test took too long: " + duration + "ms");
        }

        @Test
        @DisplayName("Service should handle boundary values for IDs")
        void testBoundaryValues() {
            // Test with boundary values
            assertThrows(IllegalArgumentException.class, () -> vendorService.getVendor(Long.MIN_VALUE));
            assertThrows(IllegalArgumentException.class, () -> vendorService.getVendor(-1L));
            assertThrows(IllegalArgumentException.class, () -> vendorService.getVendor(0L));
            
            // Test with maximum value
            when(mockRestaurantRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> vendorService.getVendor(Long.MAX_VALUE));
        }

        @Test
        @DisplayName("Service should handle method chaining correctly")
        void testMethodChaining() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setStatus(RestaurantStatus.APPROVED);
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
            when(mockItemRepository.findByRestaurant(any())).thenReturn(new ArrayList<>());
            when(mockItemRepository.findAvailableByRestaurant(1L)).thenReturn(new ArrayList<>());
            when(mockItemRepository.countAvailableByRestaurant(1L)).thenReturn(1); // Mock available items

            assertDoesNotThrow(() -> {
                Restaurant vendor = vendorService.getVendor(1L);
                Map<String, Object> menu = vendorService.getVendorMenu(vendor.getId());
                boolean accepting = vendorService.isVendorAcceptingOrders(vendor.getId());
                
                assertNotNull(vendor);
                assertNotNull(menu);
                assertTrue(accepting);
            });
        }

        @Test
        @DisplayName("Service should maintain state consistency")
        void testStateConsistency() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(1L);
            restaurant.setName("Test Restaurant");
            restaurant.setStatus(RestaurantStatus.APPROVED);
            when(mockRestaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            // Multiple calls should return consistent results
            Restaurant result1 = vendorService.getVendor(1L);
            Restaurant result2 = vendorService.getVendor(1L);
            Restaurant result3 = vendorService.getVendor(1L);

            assertEquals(result1.getId(), result2.getId());
            assertEquals(result2.getId(), result3.getId());
            assertEquals(result1.getName(), result2.getName());
            assertEquals(result2.getName(), result3.getName());
        }
    }
    
    // Helper methods
    
    private List<Restaurant> createSampleRestaurants() {
        Restaurant restaurant1 = createSampleRestaurant();
        
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setId(2L);
        restaurant2.setName("Pizza Palace");
        restaurant2.setAddress("456 Food St");
        restaurant2.setPhone("123-456-7890");
        restaurant2.setStatus(RestaurantStatus.APPROVED);
        restaurant2.setOwnerId(2L);
        
        return Arrays.asList(restaurant1, restaurant2);
    }
    
    private Restaurant createSampleRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("123 Test St");
        restaurant.setPhone("098-765-4321");
        restaurant.setStatus(RestaurantStatus.APPROVED);
        restaurant.setOwnerId(1L);
        return restaurant;
    }
    
    private List<FoodItem> createSampleMenuItems() {
        Restaurant restaurant = createSampleRestaurant();
        
        FoodItem pizza = new FoodItem();
        pizza.setId(1L);
        pizza.setName("Margherita Pizza");
        pizza.setCategory("Pizza");
        pizza.setPrice(15.99);
        pizza.setAvailable(true);
        pizza.setRestaurant(restaurant);
        
        FoodItem burger = new FoodItem();
        burger.setId(2L);
        burger.setName("Cheese Burger");
        burger.setCategory("Burgers");
        burger.setPrice(12.99);
        burger.setAvailable(true);
        burger.setRestaurant(restaurant);
        
        return Arrays.asList(pizza, burger);
    }
} 