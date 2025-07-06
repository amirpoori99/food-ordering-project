# Penetration Test Report

**Date**: 07/05/2025 01:58:51
**Tests Performed**: 93
**Overall Risk**: CRITICAL

## Executive Summary
- Critical: 20
- High: 72
- Medium: 0
- Low: 0

## Test Results

### Authentication Bypass [CRITICAL]
**Result**: VULNERABLE
**Details**: Hardcoded credentials found in AuthService.java
**Recommendation**: Remove hardcoded credentials and use secure authentication

### Authentication Bypass [CRITICAL]
**Result**: VULNERABLE
**Details**: Hardcoded credentials found in AuthControllerTest.java
**Recommendation**: Remove hardcoded credentials and use secure authentication

### Authentication Bypass [CRITICAL]
**Result**: VULNERABLE
**Details**: Hardcoded credentials found in AuthServiceProfileTest.java
**Recommendation**: Remove hardcoded credentials and use secure authentication

### Authentication Bypass [CRITICAL]
**Result**: VULNERABLE
**Details**: Hardcoded credentials found in AuthServiceTest.java
**Recommendation**: Remove hardcoded credentials and use secure authentication

### SQL Injection [CRITICAL]
**Result**: VULNERABLE
**Details**: SQL injection vulnerability in AnalyticsRepository.java at line 219
**Recommendation**: Use PreparedStatement to prevent SQL injection

### Input Validation [INFO]
**Result**: SECURE
**Details**: No obvious input validation issues detected

### Missing Authorization [HIGH]
**Result**: VULNERABLE
**Details**: Endpoint without authorization checks in AuthController.java
**Recommendation**: Add proper authorization to all endpoints

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in RegisterRequest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in User.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in RegistrationApiTest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in AuthRepositoryTest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in AuthServiceTest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in UserEntityTest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in DatabaseTest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in CouponCompleteTest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in CouponServiceTest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in OrderControllerIntegrationTest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in PaymentEdgeCaseTest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in PaymentServiceTest.java
**Recommendation**: Restrict role modification to administrators only

### Privilege Escalation [CRITICAL]
**Result**: VULNERABLE
**Details**: Potential privilege escalation in DatabasePerformanceTest.java
**Recommendation**: Restrict role modification to administrators only

### Database Encryption [HIGH]
**Result**: VULNERABLE
**Details**: Database is not encrypted
**Recommendation**: Implement database encryption using SQLCipher

### HTTP in Production [HIGH]
**Result**: VULNERABLE
**Details**: HTTP URLs found in production configuration
**Recommendation**: Use HTTPS for all production communications

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in AdminController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in AdminRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in AnalyticsRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in UpdateProfileRequest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in RedisCacheManager.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in ApplicationConstants.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Hardcoded Crypto Key [CRITICAL]
**Result**: VULNERABLE
**Details**: Hardcoded cryptographic key in ApplicationConstants.java
**Recommendation**: Use proper key management system

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in DuplicatePhoneException.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in InvalidCredentialsException.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in NotFoundException.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in Coupon.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in FoodItem.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in Transaction.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in JsonUtil.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Hardcoded Crypto Key [CRITICAL]
**Result**: VULNERABLE
**Details**: Hardcoded cryptographic key in JWTUtil.java
**Recommendation**: Use proper key management system

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in PasswordUtil.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in ValidationUtil.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in CouponController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in CouponRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in CouponService.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in CouponUsageRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in DeliveryController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in DeliveryRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in FavoritesController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in FavoritesRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in ItemController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in ItemService.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in MenuController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in MenuService.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in NotificationController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in NotificationRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in OrderController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in OrderRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in PaymentController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in PaymentRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in PaymentService.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in WalletController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in WalletService.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in RatingController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in RatingRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in VendorController.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in VendorRepository.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in AdminControllerTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in RegistrationApiTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in ApplicationConstantsTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in ConfigurationTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in TestDatabaseManager.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in DatabaseRetryUtil.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in JsonUtilTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in PasswordUtilTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in CouponCompleteTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in CouponControllerTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in CouponServiceTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in DeliveryEntityTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in FoodItemEntityTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in ItemRepositoryTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in ItemServiceEdgeCaseTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in ItemServiceTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in MenuControllerTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in MenuServiceTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in OrderControllerIntegrationTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in PaymentEdgeCaseTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in PaymentServiceTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in TransactionControllerTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in WalletControllerTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in WalletServiceTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in RestaurantServiceTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in RatingRepositoryTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in DatabasePerformanceTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in VendorControllerTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

### Weak Cryptography [HIGH]
**Result**: VULNERABLE
**Details**: Weak cryptographic algorithms in VendorRepositoryTest.java
**Recommendation**: Use strong algorithms like AES-256, SHA-256

## Priority Remediation

### Critical Issues (Immediate Action Required)
- **Authentication Bypass**: Remove hardcoded credentials and use secure authentication
- **Authentication Bypass**: Remove hardcoded credentials and use secure authentication
- **Authentication Bypass**: Remove hardcoded credentials and use secure authentication
- **Authentication Bypass**: Remove hardcoded credentials and use secure authentication
- **SQL Injection**: Use PreparedStatement to prevent SQL injection
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Privilege Escalation**: Restrict role modification to administrators only
- **Hardcoded Crypto Key**: Use proper key management system
- **Hardcoded Crypto Key**: Use proper key management system

### High Priority Issues
- Missing Authorization: Add proper authorization to all endpoints
- Database Encryption: Implement database encryption using SQLCipher
- HTTP in Production: Use HTTPS for all production communications
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256
- Weak Cryptography: Use strong algorithms like AES-256, SHA-256

---
**Next Test Recommended**: 2025-08-04

