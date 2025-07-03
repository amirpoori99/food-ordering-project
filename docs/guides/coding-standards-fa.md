# 📝 راهنمای استانداردهای کدنویسی - سیستم سفارش غذا

راهنمای جامع استانداردهای کدنویسی برای تیم توسعه سیستم سفارش غذا. این راهنما با تکمیل تمام فازها به‌روزرسانی شده و شامل تمام استانداردهای فعلی پروژه است.

## 📋 فهرست مطالب

1. [نام‌گذاری](#نام‌گذاری)
2. [ساختار کد](#ساختار-کد)
3. [کامنت‌گذاری](#کامنت‌گذاری)
4. [مدیریت خطا](#مدیریت-خطا)
5. [بهینه‌سازی](#بهینه‌سازی)
6. [امنیت](#امنیت)
7. [تست‌نویسی](#تست‌نویسی)
8. [کلاس‌های موجود](#کلاس‌های-موجود)
9. [وضعیت فعلی پروژه](#وضعیت-فعلی-پروژه)

---

## 🏷️ نام‌گذاری

### کلاس‌ها
```java
// ✅ درست - PascalCase
public class UserService { }
public class OrderController { }
public class RestaurantRepository { }
public class AuthMiddleware { }
public class PaymentProcessor { }

// ❌ اشتباه
public class userService { }
public class order_controller { }
public class RestaurantRepo { }
public class authmiddleware { }
```

### متدها
```java
// ✅ درست - camelCase
public void createUser() { }
public UserDto getUserById(Long id) { }
public boolean isValidPhone(String phone) { }
public void processPayment(PaymentRequest request) { }
public List<Order> getOrdersByStatus(OrderStatus status) { }

// ❌ اشتباه
public void CreateUser() { }
public UserDto get_user_by_id(Long id) { }
public boolean IsValidPhone(String phone) { }
public void process_payment(PaymentRequest request) { }
```

### متغیرها
```java
// ✅ درست - camelCase
private String userName;
private int orderCount;
private boolean isActive;
private List<User> userList;
private Map<String, Object> requestData;
private LocalDateTime createdAt;

// ❌ اشتباه
private String user_name;
private int ordercount;
private boolean is_active;
private List<User> users;
private Map<String, Object> request_data;
private LocalDateTime created_at;
```

### ثابت‌ها
```java
// ✅ درست - UPPER_SNAKE_CASE
public static final String API_BASE_URL = "http://localhost:8081";
public static final int MAX_RETRY_COUNT = 3;
public static final String DEFAULT_TIMEZONE = "Asia/Tehran";
public static final int MIN_PASSWORD_LENGTH = 8;
public static final String JWT_SECRET_KEY = "your-secret-key";

// ❌ اشتباه
public static final String apiBaseUrl = "http://localhost:8081";
public static final int maxRetryCount = 3;
public static final String defaultTimezone = "Asia/Tehran";
```

### پکیج‌ها
```java
// ✅ درست - lowercase
package com.myapp.auth;
package com.myapp.order.service;
package com.myapp.common.utils;
package com.myapp.payment.processor;
package com.myapp.restaurant.management;

// ❌ اشتباه
package com.myapp.Auth;
package com.myapp.Order.Service;
package com.myapp.Common.Utils;
```

---

## 🏗️ ساختار کد

### ترتیب اعضای کلاس
```java
public class UserService {
    
    // 1. ثابت‌ها
    private static final String DEFAULT_ROLE = "USER";
    private static final int MAX_NAME_LENGTH = 50;
    private static final String PHONE_REGEX = "^09\\d{9}$";
    
    // 2. متغیرهای instance
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // 3. Constructor
    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    
    // 4. متدهای public
    public UserDto createUser(CreateUserRequest request) {
        validateRequest(request);
        User user = buildUser(request);
        User savedUser = userRepository.save(user);
        sendWelcomeEmail(savedUser);
        return convertToDto(savedUser);
    }
    
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("کاربر یافت نشد"));
        return convertToDto(user);
    }
    
    public void updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("کاربر یافت نشد"));
        updateUserFields(user, request);
        userRepository.save(user);
    }
    
    // 5. متدهای private
    private void validateRequest(CreateUserRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("نام کاربر الزامی است");
        }
        if (request.getPhone() == null || !request.getPhone().matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("شماره تلفن معتبر نیست");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserExistsException("کاربر با این شماره تلفن قبلاً ثبت شده است");
        }
    }
    
    private User buildUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
    
    private void sendWelcomeEmail(User user) {
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        } catch (Exception e) {
            logger.warn("خطا در ارسال ایمیل خوش‌آمدگویی: {}", e.getMessage());
        }
    }
    
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
```

### طول متدها
```java
// ✅ درست - متد کوتاه و قابل فهم
public UserDto createUser(CreateUserRequest request) {
    validateRequest(request);
    User user = buildUser(request);
    User savedUser = userRepository.save(user);
    sendWelcomeEmail(savedUser);
    return convertToDto(savedUser);
}

// ❌ اشتباه - متد طولانی و پیچیده
public UserDto createUser(CreateUserRequest request) {
    // 50+ خط کد در یک متد
}
```

---

## 💬 کامنت‌گذاری

### کامنت‌های کلاس
```java
/**
 * سرویس مدیریت کاربران
 * 
 * این کلاس مسئول تمام عملیات مربوط به کاربران است شامل:
 * - ایجاد کاربر جدید
 * - ویرایش اطلاعات کاربر
 * - حذف کاربر
 * - جستجو و فیلتر کاربران
 * 
 * @author Food Ordering Team
 * @version 1.0
 * @since 2025-06-15
 */
public class UserService {
    // implementation
}
```

### کامنت‌های متد
```java
/**
 * ایجاد کاربر جدید
 * 
 * این متد یک کاربر جدید در سیستم ثبت می‌کند. قبل از ثبت،
 * اطلاعات ورودی اعتبارسنجی می‌شود و در صورت تکراری بودن
 * شماره تلفن، خطا برمی‌گرداند.
 * 
 * @param request درخواست ایجاد کاربر شامل نام، تلفن، ایمیل و رمز عبور
 * @return اطلاعات کاربر ایجاد شده
 * @throws IllegalArgumentException اگر اطلاعات ورودی نامعتبر باشد
 * @throws UserExistsException اگر کاربر با این شماره تلفن قبلاً ثبت شده باشد
 */
public UserDto createUser(CreateUserRequest request) {
    // implementation
}
```

### کامنت‌های خطی
```java
// بررسی وجود کاربر
if (userRepository.existsByPhone(phone)) {
    throw new UserExistsException("کاربر با این شماره تلفن قبلاً ثبت شده است");
}

// ارسال ایمیل خوش‌آمدگویی
try {
    emailService.sendWelcomeEmail(user.getEmail(), user.getName());
} catch (Exception e) {
    // در صورت خطا در ارسال ایمیل، فقط لاگ می‌کنیم و ادامه می‌دهیم
    logger.warn("خطا در ارسال ایمیل خوش‌آمدگویی: {}", e.getMessage());
}
```

---

## ⚠️ مدیریت خطا

### Exception های سفارشی
```java
// ✅ درست - Exception های مشخص و معنادار
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

public class UserExistsException extends RuntimeException {
    public UserExistsException(String message) {
        super(message);
    }
}

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
```

### مدیریت خطا در Controller
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserDto user = userService.createUser(request);
            return ResponseEntity.ok(ApiResponse.success(user, "کاربر با موفقیت ایجاد شد"));
        } catch (UserExistsException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("کاربر با این شماره تلفن قبلاً ثبت شده است"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("خطا در ایجاد کاربر: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("خطای داخلی سرور"));
        }
    }
}
```

### Global Exception Handler
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.getMessage()));
    }
    
    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserExists(UserExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(e.getMessage()));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(e.getMessage()));
    }
}
```

---

## ⚡ بهینه‌سازی

### استفاده از Stream API
```java
// ✅ درست - استفاده بهینه از Stream
public List<UserDto> getActiveUsers() {
    return userRepository.findByStatus(UserStatus.ACTIVE)
        .stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
}

// ❌ اشتباه - حلقه سنتی
public List<UserDto> getActiveUsers() {
    List<User> users = userRepository.findByStatus(UserStatus.ACTIVE);
    List<UserDto> dtos = new ArrayList<>();
    for (User user : users) {
        dtos.add(convertToDto(user));
    }
    return dtos;
}
```

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
        // implementation
    }
}
```

### Pagination
```java
// ✅ درست - استفاده از Pageable
public Page<UserDto> getUsers(Pageable pageable) {
    return userRepository.findAll(pageable)
        .map(this::convertToDto);
}

// ❌ اشتباه - بارگذاری تمام داده‌ها
public List<UserDto> getAllUsers() {
    return userRepository.findAll()
        .stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
}
```

---

## 🔒 امنیت

### رمزگذاری رمز عبور
```java
// ✅ درست - استفاده از BCrypt
@Service
public class UserService {
    
    private final PasswordEncoder passwordEncoder;
    
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    public void createUser(CreateUserRequest request) {
        User user = new User();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // ...
    }
    
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

### Validation
```java
// ✅ درست - Validation کامل
public void validateUserRequest(CreateUserRequest request) {
    if (request.getName() == null || request.getName().trim().isEmpty()) {
        throw new IllegalArgumentException("نام کاربر الزامی است");
    }
    
    if (request.getPhone() == null || !request.getPhone().matches("^09\\d{9}$")) {
        throw new IllegalArgumentException("شماره تلفن معتبر نیست");
    }
    
    if (request.getPassword() == null || request.getPassword().length() < 8) {
        throw new IllegalArgumentException("رمز عبور باید حداقل 8 کاراکتر باشد");
    }
    
    if (request.getEmail() != null && !request.getEmail().matches("^[^@]+@[^@]+\\.[^@]+$")) {
        throw new IllegalArgumentException("ایمیل معتبر نیست");
    }
}
```

### SQL Injection Prevention
```java
// ✅ درست - استفاده از PreparedStatement
@Repository
public class UserRepository {
    
    public User findByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            // process result
        }
    }
}

// ❌ اشتباه - استفاده از String concatenation
public User findByPhone(String phone) {
    String sql = "SELECT * FROM users WHERE phone = '" + phone + "'";
    // این روش در برابر SQL Injection آسیب‌پذیر است
}
```

---

## 🧪 تست‌نویسی

### ساختار تست
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private EmailService emailService;
    
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
        verify(emailService).sendWelcomeEmail(anyString(), anyString());
    }
    
    @Test
    @DisplayName("باید خطا بدهد اگر شماره تلفن تکراری باشد")
    void shouldThrowExceptionWhenPhoneExists() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setPhone("09123456789");
        
        when(userRepository.existsByPhone("09123456789")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(UserExistsException.class)
            .hasMessage("کاربر با این شماره تلفن قبلاً ثبت شده است");
        
        verify(userRepository).existsByPhone("09123456789");
        verify(userRepository, never()).save(any(User.class));
    }
}
```

### تست‌های Integration
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

تمام استانداردهای کدنویسی با موفقیت پیاده‌سازی شده‌اند و تیم توسعه از این راهنما برای حفظ کیفیت کد استفاده می‌کند.

---
**آخرین به‌روزرسانی**: 15 ژوئن 2025  
**مسئول استانداردها**: Food Ordering System Development Team