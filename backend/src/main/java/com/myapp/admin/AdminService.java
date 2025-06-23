package com.myapp.admin;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import com.myapp.courier.DeliveryRepository;
import com.myapp.order.OrderRepository;
import com.myapp.payment.PaymentRepository;
import com.myapp.restaurant.RestaurantRepository;

import java.util.List;
import java.util.Map;

/**
 * Service layer for Admin Dashboard Operations
 * Provides business logic and validation for admin functionality
 */
public class AdminService {
    
    private final AdminRepository adminRepository;
    private final AuthRepository authRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    
    public AdminService(AdminRepository adminRepository, AuthRepository authRepository,
                       RestaurantRepository restaurantRepository, OrderRepository orderRepository,
                       PaymentRepository paymentRepository, DeliveryRepository deliveryRepository) {
        this.adminRepository = adminRepository;
        this.authRepository = authRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.deliveryRepository = deliveryRepository;
    }

    // ==================== USER MANAGEMENT ====================
    
    /**
     * Get all users with pagination and filtering
     */
    public List<User> getAllUsers(String searchTerm, String role, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100; // Limit max page size
        
        User.Role roleEnum = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                roleEnum = User.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }
        
        int offset = page * size;
        return adminRepository.getAllUsers(searchTerm, roleEnum, size, offset);
    }
    
    /**
     * Count users with filtering
     */
    public Long countUsers(String searchTerm, String role) {
        User.Role roleEnum = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                roleEnum = User.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }
        
        return adminRepository.countUsers(searchTerm, roleEnum);
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        return authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
    }
    
    /**
     * Get user statistics by role
     */
    public Map<User.Role, Long> getUserStatsByRole() {
        return adminRepository.getUserStatsByRole();
    }
    
    /**
     * Block/Unblock user
     */
    public void updateUserStatus(Long userId, boolean isActive, Long adminId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive");
        }
        
        // Verify user exists
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        
        // Verify admin exists and has admin role
        User admin = authRepository.findById(adminId)
            .orElseThrow(() -> new NotFoundException("Admin", adminId));
        
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can update user status");
        }
        
        // Cannot block/unblock another admin
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalArgumentException("Cannot modify admin user status");
        }
        
        adminRepository.updateUserStatus(userId, isActive);
    }

    // ==================== RESTAURANT MANAGEMENT ====================
    
    /**
     * Get all restaurants with pagination and filtering
     */
    public List<Restaurant> getAllRestaurants(String searchTerm, String status, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        RestaurantStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = RestaurantStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid restaurant status: " + status);
            }
        }
        
        int offset = page * size;
        return adminRepository.getAllRestaurants(searchTerm, statusEnum, size, offset);
    }
    
    /**
     * Count restaurants with filtering
     */
    public Long countRestaurants(String searchTerm, String status) {
        RestaurantStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = RestaurantStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid restaurant status: " + status);
            }
        }
        
        return adminRepository.countRestaurants(searchTerm, statusEnum);
    }
    
    /**
     * Get restaurant by ID
     */
    public Restaurant getRestaurantById(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        return restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
    }
    
    /**
     * Get restaurant statistics by status
     */
    public Map<RestaurantStatus, Long> getRestaurantStatsByStatus() {
        return adminRepository.getRestaurantStatsByStatus();
    }
    
    /**
     * Update restaurant status (approve/reject/suspend)
     */
    public void updateRestaurantStatus(Long restaurantId, RestaurantStatus status, Long adminId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Restaurant status cannot be null");
        }
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive");
        }
        
        // Verify restaurant exists
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        // Verify admin exists and has admin role
        User admin = authRepository.findById(adminId)
            .orElseThrow(() -> new NotFoundException("Admin", adminId));
        
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can update restaurant status");
        }
        
        // Update restaurant status
        restaurant.setStatus(status);
        restaurantRepository.update(restaurant);
    }

    // ==================== ORDER MANAGEMENT ====================
    
    /**
     * Get all orders with pagination and filtering
     */
    public List<Order> getAllOrders(String searchTerm, String status, Long customerId, Long restaurantId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        OrderStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid order status: " + status);
            }
        }
        
        int offset = page * size;
        return adminRepository.getAllOrders(searchTerm, statusEnum, customerId, restaurantId, size, offset);
    }
    
    /**
     * Count orders with filtering
     */
    public Long countOrders(String searchTerm, String status, Long customerId, Long restaurantId) {
        OrderStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid order status: " + status);
            }
        }
        
        return adminRepository.countOrders(searchTerm, statusEnum, customerId, restaurantId);
    }
    
    /**
     * Get order by ID
     */
    public Order getOrderById(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order", orderId));
    }
    
    /**
     * Get order statistics by status
     */
    public Map<OrderStatus, Long> getOrderStatsByStatus() {
        return adminRepository.getOrderStatsByStatus();
    }
    
    /**
     * Update order status (admin override)
     */
    public void updateOrderStatus(Long orderId, OrderStatus status, Long adminId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive");
        }
        
        // Verify order exists
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order", orderId));
        
        // Verify admin exists and has admin role
        User admin = authRepository.findById(adminId)
            .orElseThrow(() -> new NotFoundException("Admin", adminId));
        
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can override order status");
        }
        
        // Update order status
        order.setStatus(status);
        orderRepository.update(order);
    }

    // ==================== TRANSACTION MANAGEMENT ====================
    
    /**
     * Get all transactions with pagination and filtering
     */
    public List<Transaction> getAllTransactions(String searchTerm, String status, String type, Long userId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        TransactionStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = TransactionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid transaction status: " + status);
            }
        }
        
        TransactionType typeEnum = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                typeEnum = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid transaction type: " + type);
            }
        }
        
        int offset = page * size;
        return adminRepository.getAllTransactions(searchTerm, statusEnum, typeEnum, userId, size, offset);
    }
    
    /**
     * Count transactions with filtering
     */
    public Long countTransactions(String searchTerm, String status, String type, Long userId) {
        TransactionStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = TransactionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid transaction status: " + status);
            }
        }
        
        TransactionType typeEnum = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                typeEnum = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid transaction type: " + type);
            }
        }
        
        return adminRepository.countTransactions(searchTerm, statusEnum, typeEnum, userId);
    }
    
    /**
     * Get transaction by ID
     */
    public Transaction getTransactionById(Long transactionId) {
        if (transactionId == null || transactionId <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive");
        }
        
        return paymentRepository.findById(transactionId)
            .orElseThrow(() -> new NotFoundException("Transaction", transactionId));
    }

    // ==================== DELIVERY MANAGEMENT ====================
    
    /**
     * Get all deliveries with pagination and filtering
     */
    public List<Delivery> getAllDeliveries(String searchTerm, String status, Long courierId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        DeliveryStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = DeliveryStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid delivery status: " + status);
            }
        }
        
        int offset = page * size;
        return adminRepository.getAllDeliveries(searchTerm, statusEnum, courierId, size, offset);
    }
    
    /**
     * Count deliveries with filtering
     */
    public Long countDeliveries(String searchTerm, String status, Long courierId) {
        DeliveryStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = DeliveryStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid delivery status: " + status);
            }
        }
        
        return adminRepository.countDeliveries(searchTerm, statusEnum, courierId);
    }
    
    /**
     * Get delivery by ID
     */
    public Delivery getDeliveryById(Long deliveryId) {
        if (deliveryId == null || deliveryId <= 0) {
            throw new IllegalArgumentException("Delivery ID must be positive");
        }
        
        return deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));
    }

    // ==================== SYSTEM STATISTICS ====================
    
    /**
     * Get comprehensive system statistics
     */
    public AdminRepository.SystemStatistics getSystemStatistics() {
        return adminRepository.getSystemStatistics();
    }
    
    /**
     * Get daily statistics for the last N days
     */
    public List<AdminRepository.DailyStatistics> getDailyStatistics(int days) {
        if (days <= 0) days = 7;
        if (days > 365) days = 365; // Limit to 1 year
        
        return adminRepository.getDailyStatistics(days);
    }
    
    /**
     * Verify admin permissions
     */
    public void verifyAdminPermissions(Long adminId) {
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive");
        }
        
        User admin = authRepository.findById(adminId)
            .orElseThrow(() -> new NotFoundException("Admin", adminId));
        
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Access denied: Admin privileges required");
        }
    }
}