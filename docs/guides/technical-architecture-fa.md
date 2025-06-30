# 🏗️ مستندات معماری فنی

مستندات جامع معماری سیستم سفارش غذا.

## 📋 فهرست مطالب

1. [معماری کلی سیستم](#معماری-کلی-سیستم)
2. [معماری Backend](#معماری-backend)
3. [معماری Frontend](#معماری-frontend)
4. [معماری پایگاه داده](#معماری-پایگاه-داده)
5. [معماری امنیت](#معماری-امنیت)
6. [معماری عملکرد](#معماری-عملکرد)
7. [معماری مقیاس‌پذیری](#معماری-مقیاس‌پذیری)
8. [معماری استقرار](#معماری-استقرار)

---

## 🏗️ معماری کلی سیستم

### نمودار معماری
```
┌─────────────────────────────────────────────────────────────┐
│                    سیستم سفارش غذا                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   Frontend      │    │    Backend      │                │
│  │   (JavaFX)      │◄──►│   (Java)        │                │
│  │                 │    │                 │                │
│  │ • UI Layer      │    │ • Controller    │                │
│  │ • Controller    │    │ • Service       │                │
│  │ • Service       │    │ • Repository    │                │
│  │ • Model         │    │ • Model         │                │
│  └─────────────────┘    └─────────────────┘                │
│           │                       │                        │
│           │                       │                        │
│           └───────────────────────┼────────────────────────┘
│                                   │
│  ┌─────────────────────────────────┴─────────────────────┐  │
│  │              پایگاه داده                             │  │
│  │                                                       │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │  │
│  │  │   Users     │  │  Orders     │  │ Restaurants │   │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘   │  │
│  │                                                       │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │  │
│  │  │   Menus     │  │  Payments   │  │  Couriers   │   │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘   │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### لایه‌های معماری
1. **Presentation Layer**: رابط کاربری JavaFX
2. **Business Logic Layer**: منطق کسب‌وکار در Backend
3. **Data Access Layer**: دسترسی به پایگاه داده
4. **Data Storage Layer**: ذخیره‌سازی داده‌ها

---

## 🔧 معماری Backend

### ساختار لایه‌ای
```
┌─────────────────────────────────────────────────────────────┐
│                    Backend Architecture                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Controller Layer                        │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │ AuthController│ │OrderController│ │UserController│ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                              │                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Service Layer                           │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │ AuthService │ │OrderService │ │UserService  │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                              │                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Repository Layer                        │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │ AuthRepository│ │OrderRepository│ │UserRepository│ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                              │                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Model Layer                             │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │     User    │ │    Order    │ │ Restaurant  │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### الگوهای طراحی استفاده شده

#### 1. MVC Pattern
```java
// Model
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String phone;
    // ...
}

// View (REST API)
@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}

// Controller (Service)
@Service
public class UserService {
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("کاربر یافت نشد"));
        return convertToDto(user);
    }
}
```

#### 2. Repository Pattern
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    List<User> findByRole(UserRole role);
}
```

#### 3. DTO Pattern
```java
public class UserDto {
    private Long id;
    private String name;
    private String phone;
    private UserRole role;
    private UserStatus status;
    
    // Getters and Setters
}

public class CreateUserRequest {
    @NotBlank(message = "نام کاربر الزامی است")
    private String name;
    
    @Pattern(regexp = "^09\\d{9}$", message = "شماره تلفن معتبر نیست")
    private String phone;
    
    @Size(min = 4, message = "رمز عبور حداقل 4 کاراکتر باشد")
    private String password;
}
```

---

## 🖥️ معماری Frontend

### ساختار لایه‌ای
```
┌─────────────────────────────────────────────────────────────┐
│                   Frontend Architecture                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              UI Layer (FXML)                        │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │  Login.fxml │ │ Dashboard.fxml│ │ Order.fxml  │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                              │                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Controller Layer                        │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │LoginController│ │DashboardController│ │OrderController│ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                              │                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Service Layer                           │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │ AuthService │ │OrderService │ │UserService  │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                              │                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Model Layer                             │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │     User    │ │    Order    │ │ Restaurant  │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### الگوهای طراحی استفاده شده

#### 1. MVC Pattern در JavaFX
```java
// Model
public class User {
    private Long id;
    private String name;
    private String phone;
    private UserRole role;
    
    // Getters and Setters
}

// View (FXML)
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.control.*?>

<VBox xmlns="http://javafx.com/javafx/11.0.1" xmlns:fx="http://javafx.com/fxml/1">
    <TextField fx:id="nameField" promptText="نام کاربر"/>
    <TextField fx:id="phoneField" promptText="شماره تلفن"/>
    <Button fx:id="saveButton" text="ذخیره" onAction="#handleSave"/>
</VBox>

// Controller
public class UserController {
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private void handleSave() {
        User user = new User();
        user.setName(nameField.getText());
        user.setPhone(phoneField.getText());
        
        userService.saveUser(user);
    }
}
```

#### 2. Service Layer Pattern
```java
public class AuthService {
    private static final String API_BASE_URL = "http://localhost:8081/api";
    private final ObjectMapper objectMapper;
    
    public AuthService() {
        this.objectMapper = new ObjectMapper();
    }
    
    public LoginResponse login(String phone, String password) throws Exception {
        String url = API_BASE_URL + "/auth/login";
        
        LoginRequest request = new LoginRequest(phone, password);
        String requestBody = objectMapper.writeValueAsString(request);
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
        
        HttpResponse<String> response = client.send(httpRequest, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), LoginResponse.class);
        } else {
            throw new RuntimeException("خطا در ورود");
        }
    }
}
```

---

## 🗄️ معماری پایگاه داده

### نمودار ERD
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      Users      │    │    Orders       │    │   Restaurants   │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ id (PK)         │    │ id (PK)         │    │ id (PK)         │
│ name            │    │ user_id (FK)    │    │ name            │
│ phone           │    │ restaurant_id   │    │ address         │
│ email           │    │ (FK)            │    │ phone           │
│ password        │    │ total_amount    │    │ status          │
│ role            │    │ status          │    │ created_at      │
│ status          │    │ created_at      │    │ created_at      │
│ created_at      │    └─────────────────┘    └─────────────────┘
└─────────────────┘              │                       │
                                 │                       │
                    ┌─────────────────┐    ┌─────────────────┐
                    │   OrderItems    │    │      Menus      │
                    ├─────────────────┤    ├─────────────────┤
                    │ id (PK)         │    │ id (PK)         │
                    │ order_id (FK)   │    │ menu_id (FK)    │
                    │ quantity        │    │ restaurant_id   │
                    │ price           │    │ (FK)            │
                    │ created_at      │    │ description     │
                    │ created_at      │    │ price           │
                    └─────────────────┘    │ status          │
                                           │ created_at      │
                                           └─────────────────┘
```

### جداول اصلی

#### 1. جدول Users
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(11) UNIQUE NOT NULL,
    email VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. جدول Orders
```sql
CREATE TABLE orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    restaurant_id INTEGER NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    delivery_address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);
```

#### 3. جدول Restaurants
```sql
CREATE TABLE restaurants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(11) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    rating DECIMAL(3,2) DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Indexing Strategy
```sql
-- Index برای جستجوی سریع
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_restaurants_name ON restaurants(name);
CREATE INDEX idx_restaurants_status ON restaurants(status);
```

---

## 🔒 معماری امنیت

### لایه‌های امنیتی
```
┌─────────────────────────────────────────────────────────────┐
│                    Security Architecture                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Authentication Layer                   │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │   JWT       │ │   Session    │ │   OAuth2     │ │   │
│  │  │   Token     │ │ Management   │ │   (Future)   │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                              │                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Authorization Layer                     │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │ Role-Based  │ │ Permission- │ │ Resource-    │ │   │
│  │  │ Access      │ │ Based Access│ │ Based Access │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                              │                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Data Protection Layer                  │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │ Encryption  │ │ Input       │ │ SQL         │ │   │
│  │  │ (Password)  │ │ Validation  │ │ Injection   │ │   │
│  │  │             │ │             │ │ Prevention   │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### JWT Authentication
```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### Role-Based Authorization
```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Input Validation
```java
public class CreateUserRequest {
    
    @NotBlank(message = "نام کاربر الزامی است")
    @Size(min = 2, max = 50, message = "نام باید بین 2 تا 50 کاراکتر باشد")
    private String name;
    
    @NotBlank(message = "شماره تلفن الزامی است")
    @Pattern(regexp = "^09\\d{9}$", message = "شماره تلفن معتبر نیست")
    private String phone;
    
    @Email(message = "ایمیل معتبر نیست")
    private String email;
    
    @NotBlank(message = "رمز عبور الزامی است")
    @Size(min = 4, max = 100, message = "رمز عبور باید بین 4 تا 100 کاراکتر باشد")
    private String password;
}
```

---

## ⚡ معماری عملکرد

### Caching Strategy
```java
@Service
public class UserService {
    
    @Cacheable("users")
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
            .map(this::convertToDto)
            .orElseThrow(() -> new UserNotFoundException("کاربر یافت نشد"));
    }
    
    @CacheEvict("users")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    @CachePut("users")
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("کاربر یافت نشد"));
        
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
}
```

### Database Optimization
```sql
-- استفاده از Prepared Statements
SELECT * FROM users WHERE phone = ? AND status = ?;

-- استفاده از Indexes
CREATE INDEX idx_users_phone_status ON users(phone, status);

-- استفاده از Pagination
SELECT * FROM orders 
WHERE user_id = ? 
ORDER BY created_at DESC 
LIMIT ? OFFSET ?;
```

### Connection Pooling
```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

---

## 📈 معماری مقیاس‌پذیری

### Horizontal Scaling
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │    │   Backend 1     │    │   Backend 2     │
│                 │◄──►│                 │◄──►│                 │
│ • Nginx         │    │ • Java App      │    │ • Java App      │
│ • HAProxy       │    │ • Port 8081     │    │ • Port 8082     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Database      │
                    │                 │
                    │ • PostgreSQL    │
                    │ • Master-Slave  │
                    └─────────────────┘
```

### Microservices Architecture (Future)
```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   API Gateway   │  │  User Service   │  │  Order Service  │
│                 │◄─┤                 │  │                 │
│ • Routing       │  │ • User Mgmt     │  │ • Order Mgmt    │
│ • Auth          │  │ • Profile       │  │ • Payment       │
│ • Rate Limiting │  └─────────────────┘  └─────────────────┘
└─────────────────┘
         │
         ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ Restaurant      │  │  Notification   │  │  Analytics      │
│ Service         │  │  Service        │  │  Service        │
│                 │  │                 │  │                 │
│ • Restaurant    │  │ • Email         │  │ • Reports       │
│ • Menu          │  │ • SMS           │  │ • Push          │
│ • Reviews       │  │ • Push          │  │ • Dashboard     │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

---

## 🚀 معماری استقرار

### Deployment Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    Production Environment                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   Web Server    │    │   Application   │                │
│  │                 │    │   Server        │                │
│  │ • Nginx         │◄──►│                 │                │
│  │ • SSL/TLS       │    │ • Java 17       │                │
│  │ • Static Files  │    │ • Spring Boot   │                │
│  │ • Load Balance  │    │ • Port 8081     │                │
│  └─────────────────┘    └─────────────────┘                │
│           │                       │                        │
│           │                       │                        │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   Database      │    │   File Storage  │                │
│  │   Server        │    │                 │                │
│  │                 │    │ • Images        │                │
│  │ • PostgreSQL    │    │ • Documents     │                │
│  │ • Backup        │    │ • Logs          │                │
│  │ • Replication   │    │ • Reports       │                │
│  └─────────────────┘    └─────────────────┘                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### CI/CD Pipeline
```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│    Git      │  │    Build    │  │    Test     │  │   Deploy    │
│ Repository  │──┤   (Maven)   │──┤  (JUnit)    │──┤  (Docker)   │
│             │  │             │  │             │  │             │
│ • Code      │  │ • Compile   │  │ • Unit      │  │ • Container │
│ • Version   │  │ • Package   │  │ • Integration│ │ • Deploy    │
│ • Branch    │  │ • Artifact  │  │ • Security  │  │ • Monitor   │
└─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘
```

### Docker Configuration
```dockerfile
# Dockerfile برای Backend
FROM openjdk:17-jre-slim

WORKDIR /app
COPY target/food-ordering-backend.jar app.jar

EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8081:8081"
    environment:
      - DATABASE_URL=jdbc:postgresql://db:5432/food_ordering
    depends_on:
      - db
  
  frontend:
    build: ./frontend-javafx
    ports:
      - "8080:8080"
    depends_on:
      - backend
  
  db:
    image: postgres:13
    environment:
      - POSTGRES_DB=food_ordering
      - POSTGRES_USER=food_user
      - POSTGRES_PASSWORD=food_pass
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

---

## 📊 Monitoring & Logging

### Application Monitoring
```java
// Health Check Endpoint
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<HealthStatus> health() {
        HealthStatus status = new HealthStatus();
        status.setStatus("UP");
        status.setTimestamp(LocalDateTime.now());
        status.setVersion("1.0.0");
        
        return ResponseEntity.ok(status);
    }
}

// Metrics Endpoint
@RestController
public class MetricsController {
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("activeUsers", userService.getActiveUserCount());
        metrics.put("totalOrders", orderService.getTotalOrderCount());
        metrics.put("systemUptime", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(metrics);
    }
}
```

### Logging Strategy
```java
// Structured Logging
private static final Logger logger = LoggerFactory.getLogger(UserService.class);

public UserDto createUser(CreateUserRequest request) {
    logger.info("درخواست ایجاد کاربر", Map.of(
        "phone", request.getPhone(),
        "timestamp", LocalDateTime.now()
    ));
    
    try {
        UserDto result = // منطق ایجاد کاربر
        logger.info("کاربر با موفقیت ایجاد شد", Map.of(
            "userId", result.getId(),
            "phone", result.getPhone()
        ));
        return result;
    } catch (Exception e) {
        logger.error("خطا در ایجاد کاربر", Map.of(
            "phone", request.getPhone(),
            "error", e.getMessage()
        ), e);
        throw e;
    }
}
```

---

## 🔄 Backup & Recovery

### Backup Strategy
```bash
#!/bin/bash
# backup-system.sh

# Database Backup
pg_dump -h localhost -U food_user -d food_ordering > backup_$(date +%Y%m%d_%H%M%S).sql

# File Backup
tar -czf files_backup_$(date +%Y%m%d_%H%M%S).tar.gz /var/food-ordering/files/

# Configuration Backup
cp /etc/food-ordering/config.properties backup_config_$(date +%Y%m%d_%H%M%S).properties
```

### Recovery Strategy
```bash
#!/bin/bash
# recovery-system.sh

# Database Recovery
psql -h localhost -U food_user -d food_ordering < backup_20241201_120000.sql

# File Recovery
tar -xzf files_backup_20241201_120000.tar.gz -C /

# Configuration Recovery
cp backup_config_20241201_120000.properties /etc/food-ordering/config.properties
```

---

## 📈 Performance Benchmarks

### Response Time Targets
- **API Response Time**: < 200ms (95th percentile)
- **Database Query Time**: < 50ms (95th percentile)
- **UI Loading Time**: < 2 seconds
- **Search Response Time**: < 500ms

### Throughput Targets
- **Concurrent Users**: 1000+
- **Orders per Second**: 100+
- **API Requests per Second**: 500+

### Resource Usage
- **CPU Usage**: < 70% average
- **Memory Usage**: < 80% average
- **Disk I/O**: < 100 MB/s average
- **Network I/O**: < 50 MB/s average

---

**آخرین به‌روزرسانی**: خرداد ۱۴۰۴  
**نسخه**: 1.0  
**وضعیت**: فعال 