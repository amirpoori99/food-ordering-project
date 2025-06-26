# 📡 API Reference

REST API documentation for the Food Ordering System.

## Base URL

- **Development:** `http://localhost:8081`
- **Production:** `https://your-domain.com`

## Authentication

All authenticated endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

---

## Authentication Endpoints

### POST /auth/register
Register a new user account.

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe", 
  "phone": "09123456789",
  "password": "password123",
  "email": "john@example.com",
  "role": "CUSTOMER"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "phone": "09123456789",
    "role": "CUSTOMER"
  }
}
```

### POST /auth/login
Authenticate user and receive JWT token.

**Request:**
```json
{
  "phone": "09123456789",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "phone": "09123456789",
      "role": "CUSTOMER"
    }
  }
}
```

---

## Restaurant Endpoints

### GET /restaurants
Get list of all active restaurants.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Pizza Palace",
      "address": "123 Main St, Tehran",
      "phone": "02112345678",
      "status": "APPROVED",
      "cuisine": "Italian"
    }
  ]
}
```

### GET /restaurants/{id}
Get restaurant details by ID.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Pizza Palace",
    "address": "123 Main St, Tehran",
    "phone": "02112345678",
    "status": "APPROVED",
    "cuisine": "Italian",
    "description": "Authentic Italian cuisine"
  }
}
```

### GET /restaurants/{id}/menu
Get restaurant menu items.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Margherita Pizza",
      "description": "Fresh tomatoes, mozzarella, basil",
      "price": 25000,
      "available": true,
      "category": "Pizza"
    }
  ]
}
```

---

## Order Endpoints

### POST /orders
Create a new order. **Requires authentication.**

**Request:**
```json
{
  "restaurantId": 1,
  "deliveryAddress": "456 Oak St, Tehran",
  "deliveryPhone": "09123456789",
  "notes": "Ring doorbell twice",
  "items": [
    {
      "foodItemId": 1,
      "quantity": 2
    },
    {
      "foodItemId": 2,
      "quantity": 1
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 123,
    "trackingCode": "ORD-2024-123",
    "status": "PLACED",
    "totalAmount": 50000,
    "estimatedDeliveryTime": "2024-12-15T19:30:00Z"
  }
}
```

### GET /orders/{id}
Get order details by ID. **Requires authentication.**

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "trackingCode": "ORD-2024-123",
    "status": "CONFIRMED",
    "totalAmount": 50000,
    "deliveryAddress": "456 Oak St, Tehran",
    "createdAt": "2024-12-15T18:00:00Z",
    "items": [
      {
        "foodItem": {
          "name": "Margherita Pizza",
          "price": 25000
        },
        "quantity": 2,
        "subtotal": 50000
      }
    ]
  }
}
```

### GET /orders/user/{userId}
Get orders for a specific user. **Requires authentication.**

**Query Parameters:**
- `status` (optional): Filter by order status
- `page` (optional): Page number (default: 0)  
- `size` (optional): Page size (default: 10)

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 123,
        "trackingCode": "ORD-2024-123",
        "status": "DELIVERED",
        "totalAmount": 50000,
        "createdAt": "2024-12-15T18:00:00Z"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

### PUT /orders/{id}/status
Update order status. **Requires authentication (vendor/admin).**

**Request:**
```json
{
  "status": "CONFIRMED",
  "notes": "Order confirmed, estimated prep time 30 minutes"
}
```

---

## User Profile Endpoints

### GET /users/profile
Get current user profile. **Requires authentication.**

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "phone": "09123456789",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "createdAt": "2024-12-01T10:00:00Z"
  }
}
```

### PUT /users/profile
Update user profile. **Requires authentication.**

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "email": "johnsmith@example.com"
}
```

---

## Food Item Endpoints

### GET /food-items/search
Search food items across all restaurants.

**Query Parameters:**
- `q`: Search query
- `category` (optional): Filter by category
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Margherita Pizza",
      "description": "Fresh tomatoes, mozzarella, basil",
      "price": 25000,
      "restaurant": {
        "id": 1,
        "name": "Pizza Palace"
      }
    }
  ]
}
```

---

## Payment Endpoints

### POST /payments/process
Process payment for an order. **Requires authentication.**

**Request:**
```json
{
  "orderId": 123,
  "paymentMethod": "ONLINE",
  "cardDetails": {
    "cardNumber": "1234567890123456",
    "expiryMonth": 12,
    "expiryYear": 2025,
    "cvv": "123"
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "transactionId": "TXN-2024-456",
    "status": "COMPLETED",
    "amount": 50000
  }
}
```

---

## Admin Endpoints

### GET /admin/statistics
Get system statistics. **Requires admin authentication.**

**Response:**
```json
{
  "success": true,
  "data": {
    "totalUsers": 1250,
    "totalRestaurants": 45,
    "totalOrders": 3420,
    "totalRevenue": 125000000,
    "ordersToday": 87,
    "revenueToday": 2150000
  }
}
```

### GET /admin/orders
Get all orders with filtering. **Requires admin authentication.**

**Query Parameters:**
- `status` (optional): Filter by status
- `restaurantId` (optional): Filter by restaurant
- `startDate` (optional): Filter from date
- `endDate` (optional): Filter to date

---

## Health Check

### GET /health
Check application health status.

**Response:**
```json
{
  "status": "UP",
  "service": "food-ordering-backend",
  "timestamp": "2024-12-15T12:00:00Z",
  "version": "1.0.0"
}
```

---

## Error Responses

All endpoints return errors in this format:

```json
{
  "success": false,
  "message": "Error description",
  "error": "VALIDATION_ERROR",
  "details": {
    "field": "phone",
    "issue": "Phone number must be 11 digits"
  }
}
```

### Common HTTP Status Codes

- **200 OK** - Request successful
- **201 Created** - Resource created successfully  
- **400 Bad Request** - Invalid request data
- **401 Unauthorized** - Authentication required/failed
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **422 Unprocessable Entity** - Validation errors
- **500 Internal Server Error** - Server error

---

## Rate Limiting

API requests are limited to:
- **60 requests per minute** per IP address
- **10 auth requests per minute** per IP address
- **30 order requests per minute** per authenticated user

Rate limit headers included in responses:
```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1640123456
```

---

## Testing

### Using curl

**Login:**
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"09123456789","password":"password123"}'
```

**Get restaurants:**
```bash
curl http://localhost:8081/restaurants
```

**Create order (with auth):**
```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"restaurantId":1,"deliveryAddress":"Test Address","items":[{"foodItemId":1,"quantity":2}]}'
```

---

**Version:** 1.0  
**Last Updated:** December 2024 