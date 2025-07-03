# 💻 راهنمای توسعه‌دهندگان سیستم سفارش غذا

این راهنما برای توسعه‌دهندگان پروژه تهیه شده و شامل معماری، ساختار پروژه، استانداردهای کدنویسی، تست‌نویسی، مشارکت و مثال‌های کد با توضیحات فارسی است. پروژه در حال حاضر 100% تکمیل شده و تمام بخش‌های اصلی فعال هستند.

## 📋 فهرست مطالب

1. [معماری سیستم](#معماری-سیستم)
2. [محیط توسعه](#محیط-توسعه)
3. [ساختار پروژه](#ساختار-پروژه)
4. [راهنمای کدنویسی](#راهنمای-کدنویسی)
5. [API Development](#api-development)
6. [Frontend Development](#frontend-development)
7. [تست‌نویسی](#تست‌نویسی)
8. [Debugging](#debugging)
9. [Performance Optimization](#performance-optimization)
10. [Security Guidelines](#security-guidelines)
11. [Deployment](#deployment)
12. [Contributing](#contributing)
13. [کلاس‌های موجود](#کلاس‌های-موجود)
14. [وضعیت فعلی پروژه](#وضعیت-فعلی-پروژه)

---

## 🏗️ معماری سیستم
- **Frontend:** JavaFX (UI, Controller, Service, Model)
- **Backend:** Java (Spring-like structure: Controller, Service, Repository, Model)
- **Database:** SQLite (توسعه)، PostgreSQL (تولیدی)
- **ارتباط:** RESTful API با JWT
- **Security:** JWT Authentication, Role-based Access Control
- **Testing:** JUnit, Mockito, Integration Tests

### نمودار معماری
```
Frontend (JavaFX) <-> Backend (Java) <-> Database
```

### لایه‌های Backend
1. **Controller Layer**: مدیریت درخواست‌های HTTP
2. **Service Layer**: منطق کسب‌وکار
3. **Repository Layer**: دسترسی به داده
4. **Model Layer**: موجودیت‌های داده
5. **Utility Layer**: ابزارهای مشترک
6. **Security Layer**: احراز هویت و مجوزها

### لایه‌های Frontend
1. **UI Layer**: رابط کاربری (FXML)
2. **Controller Layer**: کنترل‌کننده‌های UI
3. **Service Layer**: سرویس‌های ارتباط با API
4. **Model Layer**: مدل‌های داده
5. **Utility Layer**: ابزارهای مشترک
6. **Security Layer**: مدیریت توکن و احراز هویت

---

## 🛠️ محیط توسعه

### پیش‌نیازها
```bash
# Java 17+
java -version

# Maven 3.6+
mvn -version

# Git
git --version

# IDE (IntelliJ IDEA یا Eclipse)
```

### راه‌اندازی محیط
```bash
# کلون پروژه
git clone <repository-url>
cd food-ordering-project

# نصب وابستگی‌ها
cd backend
mvn clean install

cd ../frontend-javafx
mvn clean install
```

### پیکربندی IDE
1. **IntelliJ IDEA**:
   - Import project as Maven project
   - Enable annotation processing
   - Configure Java 17 SDK
   - Install JavaFX plugin

2. **Eclipse**:
   - Import existing Maven projects
   - Configure Java 17
   - Install JavaFX support

---

## 📁 ساختار پروژه

### Backend Structure
```
backend/
├── src/main/java/com/myapp/
│   ├── auth/              # احراز هویت و مجوزها
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   ├── AuthRepository.java
│   │   └── dto/
│   ├── order/             # مدیریت سفارشات
│   │   ├── OrderController.java
│   │   ├── OrderService.java
│   │   └── OrderRepository.java
│   ├── payment/           # پردازش پرداخت
│   │   ├── PaymentController.java
│   │   ├── PaymentService.java
│   │   └── PaymentRepository.java
│   ├── restaurant/        # مدیریت رستوران‌ها
│   │   ├── RestaurantController.java
│   │   ├── RestaurantService.java
│   │   └── RestaurantRepository.java
│   ├── menu/              # مدیریت منوها
│   │   ├── MenuController.java
│   │   ├── MenuService.java
│   │   └── MenuRepository.java
│   ├── item/              # مدیریت آیتم‌ها
│   │   ├── ItemController.java
│   │   ├── ItemService.java
│   │   └── ItemRepository.java
│   ├── courier/           # سیستم تحویل
│   │   ├── DeliveryController.java
│   │   ├── DeliveryService.java
│   │   └── DeliveryRepository.java
│   ├── notification/      # سیستم اطلاع‌رسانی
│   │   ├── NotificationController.java
│   │   ├── NotificationService.java
│   │   └── NotificationRepository.java
│   ├── admin/             # داشبورد ادمین
│   │   ├── AdminController.java
│   │   ├── AdminService.java
│   │   └── AdminRepository.java
│   ├── vendor/            # مدیریت فروشندگان
│   │   ├── VendorController.java
│   │   ├── VendorService.java
│   │   └── VendorRepository.java
│   ├── review/            # نظرات و امتیازات
│   │   ├── ReviewController.java
│   │   ├── ReviewService.java
│   │   └── ReviewRepository.java
│   ├── coupon/            # سیستم کوپن
│   │   ├── CouponController.java
│   │   ├── CouponService.java
│   │   └── CouponRepository.java
│   ├── favorites/         # مورد علاقه‌ها
│   │   ├── FavoritesController.java
│   │   ├── FavoritesService.java
│   │   └── FavoritesRepository.java
│   ├── common/            # کلاس‌های مشترک
│   │   ├── models/        # مدل‌های داده
│   │   ├── utils/         # ابزارها
│   │   ├── exceptions/    # استثناها
│   │   └── constants/     # ثابت‌ها
│   └── ServerApp.java     # نقطه شروع
├── src/main/resources/
│   ├── application.properties
│   └── hibernate.cfg.xml
└── src/test/              # تست‌ها
```

### Frontend Structure
```
frontend-javafx/
├── src/main/java/com/myapp/ui/
│   ├── auth/              # صفحات احراز هویت
│   ├── order/             # مدیریت سفارشات
│   ├── payment/           # پردازش پرداخت
│   ├── restaurant/        # مدیریت رستوران‌ها
│   ├── menu/              # مدیریت منوها
│   ├── admin/             # داشبورد ادمین
│   ├── common/            # کلاس‌های مشترک
│   └── MainApp.java       # نقطه شروع
├── src/main/resources/fxml/
│   ├── Login.fxml
│   ├── Register.fxml
│   ├── Dashboard.fxml
│   └── ...
└── src/test/              # تست‌ها
```

### System Scripts
```
scripts/
├── backup-system.sh       # پشتیبان‌گیری خودکار
├── deployment.sh          # استقرار خودکار
├── monitoring.sh          # نظارت سیستم
├── security-check.sh      # بررسی امنیت
└── database-setup.sql     # راه‌اندازی پایگاه داده
```

---

## 📝 راهنمای کدنویسی

### استانداردهای کدنویسی

#### نام‌گذاری
```java
// کلاس‌ها: PascalCase
public class UserService { }

// متدها: camelCase
public void createUser() { }

// متغیرها: camelCase
private String userName;

// ثابت‌ها: UPPER_SNAKE_CASE
public static final String API_BASE_URL = "http://localhost:8081";

// پکیج‌ها: lowercase
package com.myapp.auth;
```

#### کامنت‌گذاری
```java
/**
 * سرویس مدیریت کاربران
 * این کلاس مسئول تمام عملیات مربوط به کاربران است
 * 
 * @author Food Ordering Team
 * @version 1.0
 */
public class UserService {
    
    /**
     * ایجاد کاربر جدید
     * 
     * @param userDto اطلاعات کاربر جدید
     * @return کاربر ایجاد شده
     * @throws UserExistsException اگر کاربر قبلاً وجود داشته باشد
     */
    public User createUser(UserDto userDto) {
        // implementation
    }
}
```

---

## 🔌 API Development

### ساختار Controller
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
        try {
            UserDto user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserDto user = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "کاربر با موفقیت ایجاد شد"));
        } catch (UserExistsException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
```

### ساختار Service
```java
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public UserDto createUser(CreateUserRequest request) {
        // Validation
        validateRequest(request);
        
        // Check if user exists
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserExistsException("کاربر با این شماره تلفن قبلاً ثبت شده است");
        }
        
        // Create user
        User user = buildUser(request);
        User savedUser = userRepository.save(user);
        
        return convertToDto(savedUser);
    }
    
    private void validateRequest(CreateUserRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("نام کاربر الزامی است");
        }
        if (request.getPhone() == null || !request.getPhone().matches("^09\\d{9}$")) {
            throw new IllegalArgumentException("شماره تلفن معتبر نیست");
        }
    }
}
```

### ساختار Repository
```java
@Repository
public class UserRepository {
    
    private final SessionFactory sessionFactory;
    
    public UserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    public User save(User user) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(user);
        return user;
    }
    
    public Optional<User> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.get(User.class, id);
        return Optional.ofNullable(user);
    }
    
    public boolean existsByPhone(String phone) {
        Session session = sessionFactory.getCurrentSession();
        String hql = "SELECT COUNT(u) FROM User u WHERE u.phone = :phone";
        Long count = session.createQuery(hql, Long.class)
            .setParameter("phone", phone)
            .uniqueResult();
        return count > 0;
    }
}
```

---

## 🎨 Frontend Development

### ساختار Controller
```java
public class LoginController {
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    private final AuthService authService;
    
    public LoginController() {
        this.authService = new AuthService();
    }
    
    @FXML
    private void initialize() {
        // Initialize UI components
        setupValidation();
        setupEventHandlers();
    }
    
    @FXML
    private void handleLogin() {
        String phone = phoneField.getText();
        String password = passwordField.getText();
        
        try {
            LoginResponse response = authService.login(phone, password);
            // Navigate to dashboard
            navigateToDashboard(response.getToken());
        } catch (Exception e) {
            showError("خطا در ورود: " + e.getMessage());
        }
    }
    
    private void setupValidation() {
        // Add validation listeners
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^09\\d{0,9}$")) {
                phoneField.setStyle("-fx-border-color: red;");
            } else {
                phoneField.setStyle("-fx-border-color: green;");
            }
        });
    }
}
```

### ساختار Service
```java
public class AuthService {
    
    private static final String API_BASE_URL = "http://localhost:8081";
    private final ObjectMapper objectMapper;
    
    public AuthService() {
        this.objectMapper = new ObjectMapper();
    }
    
    public LoginResponse login(String phone, String password) throws Exception {
        LoginRequest request = new LoginRequest(phone, password);
        
        String jsonRequest = objectMapper.writeValueAsString(request);
        
        URL url = new URL(API_BASE_URL + "/auth/login");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return objectMapper.readValue(response.toString(), LoginResponse.class);
            }
        } else {
            throw new RuntimeException("خطا در ورود: " + responseCode);
        }
    }
}
```

---

## 🧪 تست‌نویسی

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("باید کاربر جدید را با موفقیت ایجاد کند")
    void shouldCreateUserSuccessfully() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setName("علی احمدی");
        request.setPhone("09123456789");
        request.setPassword("password123");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("علی احمدی");
        savedUser.setPhone("09123456789");
        
        when(userRepository.existsByPhone("09123456789")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserDto result = userService.createUser(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("علی احمدی");
        assertThat(result.getPhone()).isEqualTo("09123456789");
        
        verify(userRepository).existsByPhone("09123456789");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureTestDatabase
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("باید کاربر جدید را از طریق API ایجاد کند")
    void shouldCreateUserViaApi() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setName("علی احمدی");
        request.setPhone("09123456789");
        request.setPassword("password123");
        
        // When
        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.postForEntity(
            "/api/users",
            request,
            new ParameterizedTypeReference<ApiResponse<UserDto>>() {}
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getName()).isEqualTo("علی احمدی");
    }
}
```

---

## 🐛 Debugging

### Logging
```java
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public UserDto createUser(CreateUserRequest request) {
        logger.info("درخواست ایجاد کاربر جدید: {}", request.getPhone());
        
        try {
            // Validation
            validateRequest(request);
            logger.debug("اعتبارسنجی موفق");
            
            // Check if user exists
            if (userRepository.existsByPhone(request.getPhone())) {
                logger.warn("تلاش برای ایجاد کاربر تکراری: {}", request.getPhone());
                throw new UserExistsException("کاربر با این شماره تلفن قبلاً ثبت شده است");
            }
            
            // Create user
            User user = buildUser(request);
            User savedUser = userRepository.save(user);
            
            logger.info("کاربر جدید با موفقیت ایجاد شد: {}", savedUser.getId());
            return convertToDto(savedUser);
            
        } catch (Exception e) {
            logger.error("خطا در ایجاد کاربر: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

### Exception Handling
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException e) {
        logger.warn("کاربر یافت نشد: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.getMessage()));
    }
    
    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserExists(UserExistsException e) {
        logger.warn("کاربر تکراری: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception e) {
        logger.error("خطای غیرمنتظره: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
            .body(ApiResponse.error("خطای داخلی سرور"));
    }
}
```

---

## ⚡ Performance Optimization

### Caching
```java
@Service
public class UserService {
    
    @Cacheable("users")
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("کاربر یافت نشد"));
        return convertToDto(user);
    }
    
    @CacheEvict("users")
    public void updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("کاربر یافت نشد"));
        updateUserFields(user, request);
        userRepository.save(user);
    }
}
```

### Database Optimization
```java
@Repository
public class UserRepository {
    
    public Page<User> findAll(Pageable pageable) {
        Session session = sessionFactory.getCurrentSession();
        String hql = "FROM User u ORDER BY u.createdAt DESC";
        
        Query<User> query = session.createQuery(hql, User.class);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<User> users = query.list();
        
        // Get total count
        String countHql = "SELECT COUNT(u) FROM User u";
        Long total = session.createQuery(countHql, Long.class).uniqueResult();
        
        return new PageImpl<>(users, pageable, total);
    }
}
```

---

## 🔒 Security Guidelines

### Password Hashing
```java
@Component
public class PasswordUtil {
    
    private final BCryptPasswordEncoder passwordEncoder;
    
    public PasswordUtil() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
}
```

### Input Validation
```java
@Component
public class ValidationUtil {
    
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[^@]+@[^@]+\\.[^@]+$");
    }
    
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.matches("^09\\d{9}$");
    }
    
    public boolean isSqlInjectionSafe(String input) {
        if (input == null) {
            return true;
        }
        
        String[] dangerousPatterns = {
            "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER",
            "UNION", "EXEC", "EXECUTE", "SCRIPT", "javascript:", "vbscript:"
        };
        
        String upperInput = input.toUpperCase();
        for (String pattern : dangerousPatterns) {
            if (upperInput.contains(pattern)) {
                return false;
            }
        }
        return true;
    }
}
```

---

## 🚀 Deployment

### Production Build
```bash
# Backend
cd backend
mvn clean package -Pproduction

# Frontend
cd frontend-javafx
mvn clean package -Pproduction
```

### Docker Deployment
```dockerfile
# Dockerfile for Backend
FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/food-ordering-backend.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
```

### Environment Configuration
```properties
# application-production.properties
server.port=8081
database.url=jdbc:postgresql://localhost:5432/food_ordering
database.username=production_user
database.password=secure_password
jwt.secret=your-production-secret-key
logging.level=INFO
```

---

## 🤝 Contributing

### Git Workflow
```bash
# Create feature branch
git checkout -b feature/user-management

# Make changes
git add .
git commit -m "Add user management functionality"

# Push to remote
git push origin feature/user-management

# Create pull request
```

### Code Review Checklist
- [ ] کد مطابق با استانداردهای پروژه است
- [ ] تست‌ها نوشته شده و موفق هستند
- [ ] مستندات به‌روزرسانی شده است
- [ ] امنیت رعایت شده است
- [ ] عملکرد بهینه است

---

## 📁 کلاس‌های موجود

### کلاس‌های احراز هویت:
- **AuthController**: `backend/src/main/java/com/myapp/auth/AuthController.java`
- **AuthService**: `backend/src/main/java/com/myapp/auth/AuthService.java`
- **AuthRepository**: `backend/src/main/java/com/myapp/auth/AuthRepository.java`
- **AuthMiddleware**: `backend/src/main/java/com/myapp/auth/AuthMiddleware.java`

### کلاس‌های مدیریت:
- **AdminController**: `backend/src/main/java/com/myapp/admin/AdminController.java`
- **AdminService**: `backend/src/main/java/com/myapp/admin/AdminService.java`
- **AdminRepository**: `backend/src/main/java/com/myapp/admin/AdminRepository.java`

### کلاس‌های سفارش:
- **OrderController**: `backend/src/main/java/com/myapp/order/OrderController.java`
- **OrderService**: `backend/src/main/java/com/myapp/order/OrderService.java`
- **OrderRepository**: `backend/src/main/java/com/myapp/order/OrderRepository.java`

### کلاس‌های پرداخت:
- **PaymentController**: `backend/src/main/java/com/myapp/payment/PaymentController.java`
- **PaymentService**: `backend/src/main/java/com/myapp/payment/PaymentService.java`
- **PaymentRepository**: `backend/src/main/java/com/myapp/payment/PaymentRepository.java`

### کلاس‌های رستوران:
- **RestaurantController**: `backend/src/main/java/com/myapp/restaurant/RestaurantController.java`
- **RestaurantService**: `backend/src/main/java/com/myapp/restaurant/RestaurantService.java`
- **RestaurantRepository**: `backend/src/main/java/com/myapp/restaurant/RestaurantRepository.java`

### کلاس‌های امنیتی:
- **AdvancedSecurityUtil**: `backend/src/main/java/com/myapp/common/utils/AdvancedSecurityUtil.java`
- **PasswordUtil**: `backend/src/main/java/com/myapp/common/utils/PasswordUtil.java`
- **ValidationUtil**: `backend/src/main/java/com/myapp/common/utils/ValidationUtil.java`

### کلاس‌های بهینه‌سازی:
- **PerformanceUtil**: `backend/src/main/java/com/myapp/common/utils/PerformanceUtil.java`
- **AdvancedOptimizer**: `backend/src/main/java/com/myapp/common/utils/AdvancedOptimizer.java`

---

## 📊 وضعیت فعلی پروژه

### پیشرفت کلی: 100% (40/40 فاز)
- ✅ **Backend**: 100% کامل (20/20 فاز)
- ✅ **Frontend**: 100% کامل (10/10 فاز)  
- ✅ **System Scripts**: 100% کامل (5/5 فاز)
- ✅ **Documentation**: 100% کامل (5/5 فاز)

### آخرین دستاوردها:
- ✅ **تمام فازها تکمیل شده**: سیستم کاملاً آماده
- 📚 **مستندات کامل**: راهنماهای جامع توسعه، معماری و استانداردها
- 🔧 **اسکریپت‌های سیستم**: تمام اسکریپت‌های استقرار و نگهداری
- 🛡️ **امنیت و مانیتورینگ**: سیستم‌های نظارت و امنیت کامل

### تست‌های موجود:
- **Auth Tests**: 52 تست احراز هویت
- **Admin Tests**: 9 تست مدیریت
- **Order Tests**: 14 تست سفارش
- **Payment Tests**: 40 تست پرداخت
- **Security Tests**: 35 تست امنیتی
- **Performance Tests**: تست‌های عملکرد

### اجرای تست‌ها:
```bash
# اجرای تمام تست‌ها
mvn test

# اجرای تست‌های خاص
mvn test -Dtest=*ControllerTest
mvn test -Dtest=*ServiceTest
mvn test -Dtest=*RepositoryTest
```

---

## نتیجه‌گیری

تمام ویژگی‌های توسعه‌دهندگان با موفقیت پیاده‌سازی شده‌اند و تیم توسعه از این راهنما برای توسعه و نگهداری پروژه استفاده می‌کند.

---
**آخرین به‌روزرسانی**: 15 ژوئن 2025  
**مسئول توسعه**: Food Ordering System Development Team