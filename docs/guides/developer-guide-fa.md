# 💻 راهنمای توسعه‌دهندگان

راهنمای جامع برای توسعه‌دهندگان سیستم سفارش غذا.

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

---

## 🏗️ معماری سیستم

### معماری کلی
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │    Backend      │    │   Database      │
│   (JavaFX)      │◄──►│   (Java)        │◄──►│   (SQLite/      │
│                 │    │                 │    │   PostgreSQL)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### لایه‌های Backend
1. **Controller Layer**: مدیریت درخواست‌های HTTP
2. **Service Layer**: منطق کسب‌وکار
3. **Repository Layer**: دسترسی به داده
4. **Model Layer**: موجودیت‌های داده
5. **Utility Layer**: ابزارهای مشترک

### لایه‌های Frontend
1. **UI Layer**: رابط کاربری (FXML)
2. **Controller Layer**: کنترل‌کننده‌های UI
3. **Service Layer**: سرویس‌های ارتباط با API
4. **Model Layer**: مدل‌های داده
5. **Utility Layer**: ابزارهای مشترک

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
│   ├── payment/           # پردازش پرداخت
│   ├── restaurant/        # مدیریت رستوران‌ها
│   ├── menu/              # مدیریت منوها
│   ├── courier/           # سیستم تحویل
│   ├── notification/      # سیستم اطلاع‌رسانی
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
 * @author تیم توسعه
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
        // بررسی وجود کاربر
        if (userRepository.existsByPhone(userDto.getPhone())) {
            throw new UserExistsException("کاربر با این شماره تلفن قبلاً ثبت شده است");
        }
        
        // ایجاد کاربر جدید
        User user = new User();
        user.setName(userDto.getName());
        user.setPhone(userDto.getPhone());
        user.setPassword(encryptPassword(userDto.getPassword()));
        
        return userRepository.save(user);
    }
}
```

#### مدیریت خطا
```java
// استفاده از Exception های سفارشی
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

// مدیریت خطا در Controller
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(
        "USER_NOT_FOUND",
        ex.getMessage(),
        LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}
```

---

## 🔌 API Development

### ساختار Controller
```java
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * دریافت لیست کاربران
     * 
     * @param page شماره صفحه
     * @param size اندازه صفحه
     * @return لیست کاربران
     */
    @GetMapping
    public ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<UserDto> users = userService.getUsers(page, size);
        return ResponseEntity.ok(users);
    }
    
    /**
     * ایجاد کاربر جدید
     * 
     * @param userDto اطلاعات کاربر
     * @return کاربر ایجاد شده
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
```

### DTO Pattern
```java
/**
 * DTO برای انتقال اطلاعات کاربر
 */
public class UserDto {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    
    // Constructors, Getters, Setters
}

/**
 * DTO برای درخواست ایجاد کاربر
 */
public class CreateUserRequest {
    @NotBlank(message = "نام کاربر الزامی است")
    private String name;
    
    @Pattern(regexp = "^09\\d{9}$", message = "شماره تلفن باید با 09 شروع شود")
    private String phone;
    
    @Email(message = "ایمیل معتبر نیست")
    private String email;
    
    @Size(min = 4, message = "رمز عبور حداقل 4 کاراکتر باشد")
    private String password;
    
    // Getters, Setters
}
```

### Validation
```java
@PostMapping("/register")
public ResponseEntity<UserDto> register(@Valid @RequestBody CreateUserRequest request) {
    // درخواست به طور خودکار validate می‌شود
    UserDto user = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
}
```

---

## 🖥️ Frontend Development

### ساختار Controller
```java
/**
 * کنترل‌کننده صفحه ورود
 */
public class LoginController {
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private CheckBox rememberMeCheckBox;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
    private final AuthService authService;
    
    public LoginController() {
        this.authService = new AuthService();
    }
    
    /**
     * متد initialize که توسط JavaFX فراخوانی می‌شود
     */
    @FXML
    public void initialize() {
        // تنظیم event handlers
        loginButton.setOnAction(event -> handleLogin());
        
        // تنظیم validation
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePhone(newVal);
        });
    }
    
    /**
     * مدیریت رویداد ورود
     */
    @FXML
    private void handleLogin() {
        String phone = phoneField.getText();
        String password = passwordField.getText();
        
        if (!validateInputs(phone, password)) {
            return;
        }
        
        try {
            // نمایش loading
            loginButton.setDisable(true);
            loginButton.setText("در حال ورود...");
            
            // فراخوانی API
            LoginResponse response = authService.login(phone, password);
            
            // ذخیره token
            if (rememberMeCheckBox.isSelected()) {
                saveCredentials(phone, password);
            }
            
            // انتقال به صفحه اصلی
            navigateToMain();
            
        } catch (Exception e) {
            showError("خطا در ورود: " + e.getMessage());
        } finally {
            // بازگرداندن دکمه
            loginButton.setDisable(false);
            loginButton.setText("ورود");
        }
    }
    
    /**
     * اعتبارسنجی ورودی‌ها
     */
    private boolean validateInputs(String phone, String password) {
        if (phone == null || phone.trim().isEmpty()) {
            showError("شماره تلفن الزامی است");
            return false;
        }
        
        if (!phone.matches("^09\\d{9}$")) {
            showError("شماره تلفن معتبر نیست");
            return false;
        }
        
        if (password == null || password.length() < 4) {
            showError("رمز عبور حداقل 4 کاراکتر باشد");
            return false;
        }
        
        return true;
    }
    
    /**
     * نمایش خطا
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
```

### Service Layer
```java
/**
 * سرویس احراز هویت
 */
public class AuthService {
    
    private static final String API_BASE_URL = "http://localhost:8081/api";
    private final ObjectMapper objectMapper;
    
    public AuthService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * ورود کاربر
     * 
     * @param phone شماره تلفن
     * @param password رمز عبور
     * @return پاسخ ورود
     * @throws Exception در صورت خطا
     */
    public LoginResponse login(String phone, String password) throws Exception {
        String url = API_BASE_URL + "/auth/login";
        
        // ایجاد درخواست
        LoginRequest request = new LoginRequest(phone, password);
        String requestBody = objectMapper.writeValueAsString(request);
        
        // ارسال درخواست
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
        
        HttpResponse<String> response = client.send(httpRequest, 
            HttpResponse.BodyHandlers.ofString());
        
        // پردازش پاسخ
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), LoginResponse.class);
        } else {
            ErrorResponse error = objectMapper.readValue(response.body(), ErrorResponse.class);
            throw new RuntimeException(error.getMessage());
        }
    }
}
```

---

## 🧪 تست‌نویسی

### Unit Tests
```java
/**
 * تست‌های سرویس کاربران
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("ایجاد کاربر جدید با موفقیت")
    void createUser_Success() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setName("علی احمدی");
        request.setPhone("09123456789");
        request.setPassword("1234");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName(request.getName());
        savedUser.setPhone(request.getPhone());
        
        when(userRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Act
        UserDto result = userService.createUser(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getPhone(), result.getPhone());
        
        verify(userRepository).existsByPhone(request.getPhone());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("خطا در ایجاد کاربر تکراری")
    void createUser_DuplicateUser_ThrowsException() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setPhone("09123456789");
        
        when(userRepository.existsByPhone(request.getPhone())).thenReturn(true);
        
        // Act & Assert
        assertThrows(UserExistsException.class, () -> {
            userService.createUser(request);
        });
        
        verify(userRepository).existsByPhone(request.getPhone());
        verify(userRepository, never()).save(any(User.class));
    }
}
```

### Integration Tests
```java
/**
 * تست‌های یکپارچه API
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("دریافت لیست کاربران")
    void getUsers_Success() {
        // Arrange
        User user = new User();
        user.setName("تست کاربر");
        user.setPhone("09123456789");
        userRepository.save(user);
        
        // Act
        ResponseEntity<Page<UserDto>> response = restTemplate.getForEntity(
            "/api/users?page=0&size=10",
            new ParameterizedTypeReference<Page<UserDto>>() {}
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().size() > 0);
    }
}
```

### Frontend Tests
```java
/**
 * تست‌های کنترل‌کننده ورود
 */
class LoginControllerTest {
    
    private LoginController controller;
    private AuthService mockAuthService;
    
    @BeforeEach
    void setUp() {
        mockAuthService = mock(AuthService.class);
        controller = new LoginController();
        // تزریق mock service
    }
    
    @Test
    @DisplayName("ورود موفق")
    void handleLogin_Success() throws Exception {
        // Arrange
        LoginResponse mockResponse = new LoginResponse();
        mockResponse.setToken("test_token");
        mockResponse.setUser(new UserDto());
        
        when(mockAuthService.login("09123456789", "1234"))
            .thenReturn(mockResponse);
        
        // Act
        controller.handleLogin();
        
        // Assert
        verify(mockAuthService).login("09123456789", "1234");
        // بررسی navigation
    }
}
```

---

## 🐛 Debugging

### Backend Debugging
```java
// استفاده از Logger
private static final Logger logger = LoggerFactory.getLogger(UserService.class);

public UserDto createUser(CreateUserRequest request) {
    logger.info("درخواست ایجاد کاربر: {}", request.getPhone());
    
    try {
        // منطق ایجاد کاربر
        UserDto result = // ...
        
        logger.info("کاربر با موفقیت ایجاد شد: {}", result.getId());
        return result;
        
    } catch (Exception e) {
        logger.error("خطا در ایجاد کاربر: {}", e.getMessage(), e);
        throw e;
    }
}

// استفاده از Debug Points
public void debugMethod() {
    // نقطه توقف برای debug
    int debugPoint = 1;
    
    // منطق برنامه
    String result = processData();
    
    // بررسی نتیجه
    if (result == null) {
        logger.warn("نتیجه null است");
    }
}
```

### Frontend Debugging
```java
// استفاده از System.out.println (برای debug موقت)
public void handleButtonClick() {
    System.out.println("دکمه کلیک شد");
    
    String input = textField.getText();
    System.out.println("ورودی: " + input);
    
    // پردازش
    String result = processInput(input);
    System.out.println("نتیجه: " + result);
}

// استفاده از Platform.runLater برای debug UI
Platform.runLater(() -> {
    System.out.println("UI Thread: " + Thread.currentThread().getName());
    System.out.println("Button enabled: " + button.isDisable());
});
```

### Database Debugging
```sql
-- فعال‌سازی SQL logging در application.properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

-- بررسی کوئری‌های اجرا شده
SELECT * FROM users WHERE phone = ?;

-- بررسی عملکرد
EXPLAIN QUERY PLAN SELECT * FROM users WHERE phone = '09123456789';
```

---

## ⚡ Performance Optimization

### Backend Optimization
```java
// استفاده از Caching
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
}

// استفاده از Pagination
@GetMapping("/users")
public ResponseEntity<Page<UserDto>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<UserDto> users = userService.getUsers(pageable);
    
    return ResponseEntity.ok(users);
}

// استفاده از Async Processing
@Async
public CompletableFuture<String> processLargeData() {
    // پردازش داده‌های بزرگ
    return CompletableFuture.completedFuture("پردازش کامل شد");
}
```

### Frontend Optimization
```java
// استفاده از Lazy Loading
public class LazyUserList {
    
    private final List<User> users = new ArrayList<>();
    private int currentIndex = 0;
    private static final int BATCH_SIZE = 20;
    
    public List<User> loadNextBatch() {
        List<User> batch = new ArrayList<>();
        int endIndex = Math.min(currentIndex + BATCH_SIZE, users.size());
        
        for (int i = currentIndex; i < endIndex; i++) {
            batch.add(users.get(i));
        }
        
        currentIndex = endIndex;
        return batch;
    }
}

// استفاده از Background Tasks
public class BackgroundTask {
    
    public void performHeavyTask() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // کار سنگین
                for (int i = 0; i < 100; i++) {
                    updateProgress(i, 100);
                    Thread.sleep(100);
                }
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            // کار تمام شد
            showSuccessMessage("کار با موفقیت انجام شد");
        });
        
        new Thread(task).start();
    }
}
```

---

## 🔒 Security Guidelines

### Authentication & Authorization
```java
// استفاده از JWT
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
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        
        return claims.getSubject();
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

// استفاده از Security Annotations
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        // فقط ادمین‌ها می‌توانند دسترسی داشته باشند
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
```

### Input Validation
```java
// Validation در DTO
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

// SQL Injection Prevention
@Repository
public class UserRepository {
    
    public User findByPhone(String phone) {
        // استفاده از PreparedStatement
        String sql = "SELECT * FROM users WHERE phone = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("خطا در جستجوی کاربر", e);
        }
        
        return null;
    }
}
```

---

## 🚀 Deployment

### Production Build
```bash
# Backend
cd backend
mvn clean package -DskipTests
java -jar target/food-ordering-backend.jar

# Frontend
cd frontend-javafx
mvn clean package -DskipTests
java -jar target/food-ordering-frontend.jar
```

### Docker Deployment
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

### Environment Configuration
```properties
# application-production.properties
# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Server
server.port=${SERVER_PORT:8081}

# Logging
logging.level.root=INFO
logging.level.com.myapp=DEBUG
logging.file.name=logs/application.log

# Security
spring.security.user.name=${ADMIN_USERNAME}
spring.security.user.password=${ADMIN_PASSWORD}
```

---

## 🤝 Contributing

### Git Workflow
```bash
# ایجاد branch جدید
git checkout -b feature/new-feature

# تغییرات
git add .
git commit -m "feat: اضافه کردن ویژگی جدید"

# Push
git push origin feature/new-feature

# ایجاد Pull Request
```

### Commit Message Convention
```
type(scope): description

feat: ویژگی جدید
fix: رفع باگ
docs: تغییر مستندات
style: تغییرات ظاهری
refactor: بازنویسی کد
test: اضافه کردن تست
chore: تغییرات ابزاری
```

### Code Review Checklist
- [ ] کد قابل خواندن و فهم است
- [ ] کامنت‌های مناسب اضافه شده
- [ ] تست‌ها نوشته شده‌اند
- [ ] مستندات به‌روزرسانی شده
- [ ] اصول امنیتی رعایت شده
- [ ] عملکرد بهینه است

---

## 📞 پشتیبانی

### منابع مفید
- [Java Documentation](https://docs.oracle.com/en/java/)
- [Spring Boot Guide](https://spring.io/guides)
- [JavaFX Documentation](https://openjfx.io/)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)

### تماس با تیم
- **ایمیل**: dev@foodordering.com
- **Slack**: #food-ordering-dev
- **GitHub Issues**: برای گزارش باگ‌ها

---

**آخرین به‌روزرسانی**: خرداد ۱۴۰۴  
**نسخه**: 1.0  
**وضعیت**: فعال 