# 📁 Project Structure Documentation

Complete structure documentation for the Food Ordering System project.

## 🎯 Overview

This document provides a comprehensive overview of the project structure, including all directories, files, and their purposes. The project is a full-stack food ordering system built with Java backend and JavaFX frontend.

## 📊 Project Statistics

- **Total Java Files**: 250+ files
- **Entity Models**: 17 models
- **Controllers**: 45+ controllers
- **Services**: 14 main services
- **Repositories**: 14 repositories
- **Test Files**: 100+ test files
- **FXML Files**: 20 files
- **Documentation Files**: 11 files (bilingual)
- **Total Code Lines**: 50,000+ lines

---

## 📁 Complete Directory Structure

```
food-ordering-project/
│
├── 📁 backend/ (Java Server Application)
│   │
│   ├── 📁 src/
│   │   │
│   │   ├── 📁 main/
│   │   │   │
│   │   │   ├── 📁 java/com/myapp/
│   │   │   │   │
│   │   │   │   ├── 📁 admin/ (System Administration)
│   │   │   │   │   ├── AdminController.java (35KB, 775 lines)
│   │   │   │   │   ├── AdminRepository.java (34KB, 816 lines)
│   │   │   │   │   └── AdminService.java (29KB, 677 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 auth/ (Authentication & Authorization)
│   │   │   │   │   │
│   │   │   │   │   ├── 📁 dto/ (Data Transfer Objects)
│   │   │   │   │   │   ├── LoginRequest.java (2.5KB, 77 lines)
│   │   │   │   │   │   ├── ProfileResponse.java (1.4KB, 30 lines)
│   │   │   │   │   │   ├── RegisterRequest.java (2.5KB, 44 lines)
│   │   │   │   │   │   └── UpdateProfileRequest.java (1.7KB, 36 lines)
│   │   │   │   │   │
│   │   │   │   │   ├── AuthController.java (2.1KB, 77 lines)
│   │   │   │   │   ├── AuthMiddleware.java (11KB, 264 lines)
│   │   │   │   │   ├── AuthRepository.java (7.1KB, 159 lines)
│   │   │   │   │   ├── AuthResult.java (8.3KB, 224 lines)
│   │   │   │   │   └── AuthService.java (9.7KB, 213 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 common/ (Shared Components)
│   │   │   │   │   │
│   │   │   │   │   ├── 📁 constants/ (Application Constants)
│   │   │   │   │   │   └── ApplicationConstants.java (16KB, 229 lines)
│   │   │   │   │   │
│   │   │   │   │   ├── 📁 exceptions/ (Custom Exceptions)
│   │   │   │   │   │   ├── DuplicatePhoneException.java (810B, 18 lines)
│   │   │   │   │   │   ├── InsufficientFundsException.java (2.0KB, 63 lines)
│   │   │   │   │   │   ├── InvalidCredentialsException.java (748B, 17 lines)
│   │   │   │   │   │   └── NotFoundException.java (819B, 19 lines)
│   │   │   │   │   │
│   │   │   │   │   ├── 📁 models/ (Entity Models - Database Schema)
│   │   │   │   │   │   ├── Coupon.java (11KB, 319 lines)
│   │   │   │   │   │   ├── CouponUsage.java (4.0KB, 125 lines)
│   │   │   │   │   │   ├── Delivery.java (17KB, 479 lines)
│   │   │   │   │   │   ├── DeliveryStatus.java (1.9KB, 60 lines)
│   │   │   │   │   │   ├── Favorite.java (5.1KB, 178 lines)
│   │   │   │   │   │   ├── FoodItem.java (14KB, 323 lines)
│   │   │   │   │   │   ├── Notification.java (21KB, 591 lines)
│   │   │   │   │   │   ├── Order.java (16KB, 397 lines)
│   │   │   │   │   │   ├── OrderItem.java (2.6KB, 79 lines)
│   │   │   │   │   │   ├── OrderStatus.java (977B, 15 lines)
│   │   │   │   │   │   ├── Rating.java (7.5KB, 270 lines)
│   │   │   │   │   │   ├── Restaurant.java (6.4KB, 138 lines)
│   │   │   │   │   │   ├── RestaurantStatus.java (690B, 12 lines)
│   │   │   │   │   │   ├── Transaction.java (8.2KB, 235 lines)
│   │   │   │   │   │   ├── TransactionStatus.java (350B, 11 lines)
│   │   │   │   │   │   ├── TransactionType.java (1.2KB, 33 lines)
│   │   │   │   │   │   └── User.java (7.0KB, 156 lines)
│   │   │   │   │   │
│   │   │   │   │   └── 📁 utils/ (Utility Classes)
│   │   │   │   │       ├── DatabaseUtil.java (3.2KB, 70 lines)
│   │   │   │   │       ├── JsonUtil.java (2.4KB, 67 lines)
│   │   │   │   │       ├── JWTUtil.java (14KB, 341 lines)
│   │   │   │   │       ├── LoggerUtil.java (9.8KB, 260 lines)
│   │   │   │   │       ├── MapParsingUtil.java (7.6KB, 233 lines)
│   │   │   │   │       ├── PasswordUtil.java (10KB, 264 lines)
│   │   │   │   │       ├── PerformanceUtil.java (12KB, 377 lines)
│   │   │   │   │       └── ValidationUtil.java (12KB, 352 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 coupon/ (Discount Coupon System)
│   │   │   │   │   ├── CouponController.java (34KB, 805 lines)
│   │   │   │   │   ├── CouponRepository.java (26KB, 583 lines)
│   │   │   │   │   ├── CouponService.java (38KB, 878 lines)
│   │   │   │   │   └── CouponUsageRepository.java (6.1KB, 159 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 courier/ (Delivery Management)
│   │   │   │   │   ├── DeliveryController.java (35KB, 916 lines)
│   │   │   │   │   ├── DeliveryRepository.java (19KB, 461 lines)
│   │   │   │   │   └── DeliveryService.java (15KB, 411 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 favorites/ (User Favorites System)
│   │   │   │   │   ├── FavoritesController.java (25KB, 567 lines)
│   │   │   │   │   ├── FavoritesRepository.java (22KB, 504 lines)
│   │   │   │   │   └── FavoritesService.java (25KB, 607 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 item/ (Food Item Management)
│   │   │   │   │   ├── ItemController.java (24KB, 586 lines)
│   │   │   │   │   ├── ItemRepository.java (9.2KB, 237 lines)
│   │   │   │   │   └── ItemService.java (17KB, 430 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 menu/ (Menu Management)
│   │   │   │   │   ├── MenuController.java (30KB, 749 lines)
│   │   │   │   │   ├── MenuRepository.java (7.1KB, 202 lines)
│   │   │   │   │   └── MenuService.java (27KB, 636 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 notification/ (Notification System)
│   │   │   │   │   ├── NotificationController.java (62KB, 1314 lines)
│   │   │   │   │   ├── NotificationRepository.java (33KB, 712 lines)
│   │   │   │   │   └── NotificationService.java (36KB, 858 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 order/ (Order Management)
│   │   │   │   │   ├── OrderController.java (20KB, 477 lines)
│   │   │   │   │   ├── OrderRepository.java (12KB, 285 lines)
│   │   │   │   │   └── OrderService.java (24KB, 595 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 payment/ (Payment Processing)
│   │   │   │   │   ├── PaymentController.java (32KB, 715 lines)
│   │   │   │   │   ├── PaymentRepository.java (25KB, 557 lines)
│   │   │   │   │   ├── PaymentService.java (31KB, 742 lines)
│   │   │   │   │   ├── TransactionController.java (11KB, 287 lines)
│   │   │   │   │   ├── WalletController.java (25KB, 587 lines)
│   │   │   │   │   └── WalletService.java (29KB, 722 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 restaurant/ (Restaurant Management)
│   │   │   │   │   ├── RestaurantController.java (22KB, 504 lines)
│   │   │   │   │   ├── RestaurantRepository.java (7.6KB, 196 lines)
│   │   │   │   │   └── RestaurantService.java (21KB, 523 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 review/ (Rating & Review System)
│   │   │   │   │   ├── RatingController.java (16KB, 377 lines)
│   │   │   │   │   ├── RatingRepository.java (21KB, 517 lines)
│   │   │   │   │   └── RatingService.java (25KB, 621 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 vendor/ (Vendor Management)
│   │   │   │   │   ├── VendorController.java (14KB, 323 lines)
│   │   │   │   │   ├── VendorRepository.java (15KB, 320 lines)
│   │   │   │   │   └── VendorService.java (13KB, 315 lines)
│   │   │   │   │
│   │   │   │   └── ServerApp.java (35KB, 586 lines) [Main Entry Point]
│   │   │   │
│   │   │   └── 📁 resources/ (Configuration Files)
│   │   │       ├── application.properties (1.9KB, 41 lines)
│   │   │       ├── application-production.properties (9.3KB, 199 lines)
│   │   │       ├── hibernate.cfg.xml (2.2KB, 45 lines)
│   │   │       ├── logback.xml (6.2KB, 160 lines)
│   │   │       └── openapi.yaml (70KB, 2504 lines)
│   │   │
│   │   └── 📁 test/java/com/myapp/ (Backend Test Suite)
│   │       │
│   │       ├── 📁 admin/ (Admin Tests)
│   │       │   ├── AdminControllerTest.java (23KB, 537 lines)
│   │       │   └── AdminServiceTest.java (53KB, 1237 lines)
│   │       │
│   │       ├── 📁 api/ (API Tests)
│   │       │   └── RegistrationApiTest.java (37KB, 894 lines)
│   │       │
│   │       ├── 📁 auth/ (Authentication Tests)
│   │       │   ├── AuthControllerTest.java (17KB, 436 lines)
│   │       │   ├── AuthRepositoryTest.java (26KB, 586 lines)
│   │       │   ├── AuthResult.java (7.8KB, 194 lines)
│   │       │   ├── AuthServiceIntegrationTest.java (18KB, 409 lines)
│   │       │   ├── AuthServiceJWTTest.java (20KB, 443 lines)
│   │       │   ├── AuthServiceProfileTest.java (20KB, 388 lines)
│   │       │   ├── AuthServiceTest.java (44KB, 927 lines)
│   │       │   ├── JWTComprehensiveTest.java (12KB, 258 lines)
│   │       │   ├── JWTEndToEndTest.java (9.6KB, 187 lines)
│   │       │   ├── JWTMissingScenarios.java (12KB, 290 lines)
│   │       │   ├── JWTUtilAdvancedTest.java (12KB, 297 lines)
│   │       │   ├── JWTUtilTest.java (12KB, 316 lines)
│   │       │   ├── SecurityComprehensiveTest.java (21KB, 546 lines)
│   │       │   └── UserEntityTest.java (22KB, 491 lines)
│   │       │
│   │       ├── 📁 common/ (Common Component Tests)
│   │       │   ├── 📁 utils/ (Utility Tests)
│   │       │   │   ├── DatabaseRetryUtil.java (5.7KB, 143 lines)
│   │       │   │   ├── PasswordUtilTest.java (19KB, 486 lines)
│   │       │   │   └── PerformanceUtilTest.java (33KB, 845 lines)
│   │       │   ├── ApplicationStartupTest.java (14KB, 328 lines)
│   │       │   ├── ConfigurationTest.java (14KB, 313 lines)
│   │       │   ├── DatabaseConnectionTest.java (18KB, 390 lines)
│   │       │   └── TestDatabaseManager.java (9.4KB, 235 lines)
│   │       │
│   │       ├── 📁 coupon/ (Coupon System Tests)
│   │       │   ├── CouponCompleteTest.java (24KB, 520 lines)
│   │       │   ├── CouponControllerTest.java (26KB, 693 lines)
│   │       │   └── CouponServiceTest.java (32KB, 751 lines)
│   │       │
│   │       ├── 📁 courier/ (Delivery Tests)
│   │       │   ├── DeliveryControllerTest.java (37KB, 875 lines)
│   │       │   ├── DeliveryEntityTest.java (20KB, 539 lines)
│   │       │   └── DeliveryServiceTest.java (50KB, 1287 lines)
│   │       │
│   │       ├── 📁 favorites/ (Favorites Tests)
│   │       │   ├── FavoritesControllerTest.java (33KB, 797 lines)
│   │       │   ├── FavoritesRepositoryTest.java (26KB, 723 lines)
│   │       │   └── FavoritesServiceTest.java (24KB, 589 lines)
│   │       │
│   │       ├── 📁 item/ (Food Item Tests)
│   │       │   ├── FoodItemEntityTest.java (27KB, 586 lines)
│   │       │   ├── ItemRepositoryTest.java (23KB, 530 lines)
│   │       │   ├── ItemServiceEdgeCaseTest.java (34KB, 713 lines)
│   │       │   ├── ItemServiceEdgeCaseTest.java.bak (21KB, 543 lines)
│   │       │   └── ItemServiceTest.java (40KB, 963 lines)
│   │       │
│   │       ├── 📁 menu/ (Menu Tests)
│   │       │   ├── MenuControllerTest.java (33KB, 780 lines)
│   │       │   ├── MenuRepositoryTest.java (544B, 21 lines)
│   │       │   └── MenuServiceTest.java (41KB, 1022 lines)
│   │       │
│   │       ├── 📁 notification/ (Notification Tests)
│   │       │   ├── NotificationAdvancedTest.java (23KB, 445 lines)
│   │       │   ├── NotificationControllerTest.java (44KB, 1006 lines)
│   │       │   ├── NotificationEntityTest.java (19KB, 486 lines)
│   │       │   ├── NotificationIntegrationTest.java (42KB, 881 lines)
│   │       │   ├── NotificationRepositoryTest.java (35KB, 906 lines)
│   │       │   └── NotificationServiceTest.java (33KB, 816 lines)
│   │       │
│   │       ├── 📁 order/ (Order Tests)
│   │       │   ├── OrderControllerIntegrationTest.java (78KB, 1452 lines)
│   │       │   ├── OrderControllerTest.java (23KB, 665 lines)
│   │       │   ├── OrderEntityTest.java (16KB, 377 lines)
│   │       │   ├── OrderRepositoryTest.java (4.6KB, 137 lines)
│   │       │   └── OrderServiceTest.java (49KB, 1064 lines)
│   │       │
│   │       ├── 📁 payment/ (Payment Tests)
│   │       │   ├── PaymentControllerTest.java (27KB, 674 lines)
│   │       │   ├── PaymentEdgeCaseTest.java (50KB, 1037 lines)
│   │       │   ├── PaymentEdgeCaseTest.java.bak (30KB, 769 lines)
│   │       │   ├── PaymentServiceTest.java (8.0KB, 193 lines)
│   │       │   ├── TransactionControllerTest.java (19KB, 485 lines)
│   │       │   ├── WalletControllerTest.java (14KB, 344 lines)
│   │       │   └── WalletServiceTest.java (34KB, 735 lines)
│   │       │
│   │       ├── 📁 restaurant/ (Restaurant Tests)
│   │       │   ├── RestaurantRepositoryTest.java (23KB, 531 lines)
│   │       │   └── RestaurantServiceTest.java (50KB, 1272 lines)
│   │       │
│   │       ├── 📁 review/ (Review Tests)
│   │       │   ├── RatingControllerTest.java (30KB, 743 lines)
│   │       │   ├── RatingRepositoryTest.java (32KB, 911 lines)
│   │       │   └── RatingServiceTest.java (34KB, 857 lines)
│   │       │
│   │       ├── 📁 stress/ (Performance Tests)
│   │       │   ├── AdvancedStressTest.java (52KB, 1134 lines)
│   │       │   └── DatabasePerformanceTest.java (39KB, 853 lines)
│   │       │
│   │       └── 📁 vendor/ (Vendor Tests)
│   │           ├── VendorControllerTest.java (33KB, 783 lines)
│   │           ├── VendorRepositoryTest.java (28KB, 690 lines)
│   │           └── VendorServiceTest.java (48KB, 1166 lines)
│   │
│   ├── 📁 target/ (Build Output)
│   ├── pom.xml (7.6KB, 194 lines) [Maven Configuration]
│   ├── food_ordering.db (2.1MB) [SQLite Database]
│   ├── food_ordering.db-shm (32KB, 39 lines)
│   └── food_ordering.db-wal (4.5MB)
│
├── 📁 frontend-javafx/ (JavaFX Desktop Client)
│   │
│   ├── 📁 src/
│   │   │
│   │   ├── 📁 main/
│   │   │   │
│   │   │   ├── 📁 java/com/myapp/ui/
│   │   │   │   │
│   │   │   │   ├── 📁 admin/ (Admin UI Controllers)
│   │   │   │   │   ├── AdminOrderController.java (46B, 4 lines)
│   │   │   │   │   ├── AdminTransactionController.java (52B, 4 lines)
│   │   │   │   │   ├── AdminUserController.java (45B, 4 lines)
│   │   │   │   │   └── CouponManagementController.java (52B, 4 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 auth/ (Authentication UI)
│   │   │   │   │   ├── LoginController.java (17KB, 464 lines)
│   │   │   │   │   ├── ProfileController.java (17KB, 510 lines)
│   │   │   │   │   └── RegisterController.java (17KB, 449 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 common/ (Shared UI Components)
│   │   │   │   │   ├── FrontendConstants.java (18KB, 438 lines)
│   │   │   │   │   ├── HttpClientUtil.java (26KB, 655 lines)
│   │   │   │   │   ├── NavigationController.java (16KB, 470 lines)
│   │   │   │   │   └── NotificationService.java (45B, 4 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 coupon/ (Coupon UI)
│   │   │   │   │   └── CouponController.java (42B, 4 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 courier/ (Delivery UI)
│   │   │   │   │   ├── CourierAvailableController.java (52B, 4 lines)
│   │   │   │   │   └── CourierHistoryController.java (50B, 4 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 menu/ (Menu UI)
│   │   │   │   │   ├── ItemManagementController.java (50B, 4 lines)
│   │   │   │   │   └── MenuManagementController.java (50B, 4 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 order/ (Order UI)
│   │   │   │   │   ├── CartController.java (13KB, 358 lines)
│   │   │   │   │   ├── OrderDetailController.java (47B, 4 lines)
│   │   │   │   │   └── OrderHistoryController.java (29KB, 841 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 payment/ (Payment UI)
│   │   │   │   │   ├── PaymentController.java (21KB, 613 lines)
│   │   │   │   │   └── WalletController.java (42B, 4 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 restaurant/ (Restaurant UI)
│   │   │   │   │   ├── CreateRestaurantController.java (52B, 4 lines)
│   │   │   │   │   ├── EditRestaurantController.java (50B, 4 lines)
│   │   │   │   │   └── RestaurantListController.java (13KB, 388 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 review/ (Review UI)
│   │   │   │   │   └── ReviewController.java (42B, 4 lines)
│   │   │   │   │
│   │   │   │   ├── 📁 vendor/ (Vendor UI)
│   │   │   │   │   └── VendorSearchController.java (48B, 4 lines)
│   │   │   │   │
│   │   │   │   └── MainApp.java (3.0KB, 70 lines) [JavaFX Main Application]
│   │   │   │
│   │   │   └── 📁 resources/
│   │   │       └── 📁 fxml/ (UI Layout Files)
│   │   │           ├── AdminDashboard.fxml (8.7KB, 168 lines)
│   │   │           ├── Cart.fxml (10KB, 191 lines)
│   │   │           ├── CouponValidation.fxml (7.7KB, 187 lines)
│   │   │           ├── CourierAvailable.fxml (3.7KB, 95 lines)
│   │   │           ├── CourierHistory.fxml (7.4KB, 176 lines)
│   │   │           ├── CreateRestaurant.fxml (21KB, 442 lines)
│   │   │           ├── EditRestaurant.fxml (10KB, 228 lines)
│   │   │           ├── ItemManagement.fxml (19KB, 387 lines)
│   │   │           ├── Login.fxml (828B, 17 lines)
│   │   │           ├── Login_backup.fxml (1.3KB, 26 lines)
│   │   │           ├── MenuManagement.fxml (20KB, 399 lines)
│   │   │           ├── OrderDetail.fxml (19KB, 451 lines)
│   │   │           ├── OrderHistory.fxml (15KB, 249 lines)
│   │   │           ├── Payment.fxml (17KB, 276 lines)
│   │   │           ├── Profile.fxml (9.4KB, 175 lines)
│   │   │           ├── Register.fxml (1.3KB, 26 lines)
│   │   │           ├── RestaurantList.fxml (2.5KB, 61 lines)
│   │   │           ├── Review.fxml (23KB, 434 lines)
│   │   │           ├── VendorSearch.fxml (9.1KB, 199 lines)
│   │   │           └── Wallet.fxml (5.0KB, 119 lines)
│   │   │
│   │   └── 📁 test/java/com/myapp/ui/ (Frontend Test Suite)
│   │       │
│   │       ├── 📁 auth/ (Authentication UI Tests)
│   │       │   ├── LoginControllerMissingTest.java (13KB, 365 lines)
│   │       │   ├── LoginControllerTest.java (17KB, 469 lines)
│   │       │   ├── ProfileControllerSimpleTest.java (11KB, 263 lines)
│   │       │   ├── ProfileControllerTest.java (27KB, 691 lines)
│   │       │   ├── RegisterControllerSimpleTest.java (16KB, 445 lines)
│   │       │   └── RegisterControllerTest.java (8.4KB, 234 lines)
│   │       │
│   │       ├── 📁 common/ (Common UI Tests)
│   │       │   ├── BaseTestClass.java (9.2KB, 260 lines)
│   │       │   ├── HttpClientUtilTest.java (12KB, 319 lines)
│   │       │   ├── NavigationControllerTest.java (14KB, 366 lines)
│   │       │   └── TestFXBase.java (5.5KB, 171 lines)
│   │       │
│   │       ├── 📁 comprehensive/ (Comprehensive Tests)
│   │       │   ├── ComprehensiveTestSuite.java (17KB, 424 lines)
│   │       │   └── EndToEndFlowTest.java.disabled (19KB, 498 lines)
│   │       │
│   │       ├── 📁 edge/ (Edge Case Tests)
│   │       │   └── EdgeCaseTest.java (19KB, 477 lines)
│   │       │
│   │       ├── 📁 integration/ (Integration Tests)
│   │       │   ├── BackendIntegrationTest.java (14KB, 322 lines)
│   │       │   ├── FullStackIntegrationTest.java.disabled (12KB, 346 lines)
│   │       │   └── IntegrationTestSuite.java (16KB, 371 lines)
│   │       │
│   │       ├── 📁 order/ (Order UI Tests)
│   │       │   ├── CartControllerSimpleTest.java (13KB, 331 lines)
│   │       │   ├── CartControllerTest.java (21KB, 542 lines)
│   │       │   ├── OrderHistoryControllerSimpleTest.java (15KB, 343 lines)
│   │       │   └── OrderHistoryControllerTest.java (1.0B, 1 lines)
│   │       │
│   │       ├── 📁 payment/ (Payment UI Tests)
│   │       │   ├── PaymentControllerSimpleTest.java (12KB, 322 lines)
│   │       │   └── PaymentControllerTest.java (27KB, 709 lines)
│   │       │
│   │       ├── 📁 performance/ (Performance Tests)
│   │       │   ├── AdvancedPerformanceTest.java.disabled (14KB, 354 lines)
│   │       │   ├── PerformanceStressTest.java.disabled (23KB, 527 lines)
│   │       │   └── PerformanceTest.java (15KB, 391 lines)
│   │       │
│   │       ├── 📁 restaurant/ (Restaurant UI Tests)
│   │       │   └── RestaurantListControllerTest.java (15KB, 415 lines)
│   │       │
│   │       └── 📁 security/ (Security Tests)
│   │           ├── SecurityTest.java (16KB, 366 lines)
│   │           └── SecurityValidationTest.java.disabled (15KB, 409 lines)
│   │
│   ├── 📁 target/ (Build Output)
│   │   ├── 📁 classes/ (Compiled Classes)
│   │   ├── 📁 test-classes/ (Test Classes)
│   │   ├── 📁 maven-archiver/
│   │   │   └── pom.properties (76B, 4 lines)
│   │   ├── 📁 maven-status/
│   │   │   └── 📁 maven-compiler-plugin/
│   │   │       ├── 📁 compile/
│   │   │       └── 📁 testCompile/
│   │   └── food-ordering-frontend-0.1-SNAPSHOT.jar (134KB, 489 lines)
│   │
│   ├── pom.xml (8.7KB, 227 lines) [Maven Configuration]
│   ├── run-comprehensive-tests.bat (2.9KB, 102 lines)
│   └── run-comprehensive-tests.sh (3.7KB, 117 lines)
│
├── 📁 docs/ (Complete Documentation)
│   ├── README.md (5.9KB, 127 lines) [Documentation Index]
│   ├── admin-guide.md (9.4KB, 455 lines) [System Administration Guide]
│   ├── admin-guide-fa.md (12KB, 455 lines) [راهنمای مدیریت سیستم]
│   ├── api-reference.md (8.5KB, 476 lines) [REST API Documentation]
│   ├── api-reference-fa.md (10KB, 476 lines) [مرجع API]
│   ├── installation.md (4.7KB, 232 lines) [Installation Guide]
│   ├── installation-fa.md (6.1KB, 232 lines) [راهنمای نصب]
│   ├── troubleshooting.md (2.9KB, 146 lines) [Troubleshooting Guide]
│   ├── troubleshooting-fa.md (3.6KB, 146 lines) [راهنمای عیب‌یابی]
│   ├── user-guide.md (6.2KB, 278 lines) [User Guide]
│   └── user-guide-fa.md (11KB, 278 lines) [راهنمای کاربر]
│
├── 📁 scripts/ (Deployment & Maintenance Scripts)
│   ├── backup.conf (1.1KB, 48 lines) [Backup Configuration]
│   ├── backup-system.sh (11KB, 430 lines) [Backup Automation Script]
│   ├── database-setup.sql (10KB, 307 lines) [Database Initialization]
│   ├── deploy-production.bat (7.6KB, 197 lines) [Windows Deployment]
│   ├── deploy-production.sh (7.2KB, 239 lines) [Linux Deployment]
│   ├── food-ordering.service (1.2KB, 52 lines) [Linux SystemD Service]
│   └── food-ordering-windows.bat (2.5KB, 99 lines) [Windows Service Script]
│
├── 📁 .idea/ (IntelliJ IDEA Configuration)
│   ├── .gitignore (261B, 11 lines)
│   ├── compiler.xml (554B, 13 lines)
│   ├── encodings.xml (283B, 7 lines)
│   ├── food-ordering-project.iml (344B, 9 lines)
│   ├── jarRepositories.xml (864B, 20 lines)
│   ├── misc.xml (621B, 15 lines)
│   ├── modules.xml (443B, 9 lines)
│   └── vcs.xml (185B, 6 lines)
│
├── 📁 .vscode/ (VS Code Configuration)
│   └── settings.json (126B, 4 lines)
│
├── 📁 .git/ (Git Repository Data)
│
├── README.md (5.2KB, 181 lines) [English Project Overview]
├── README-fa.md (7.0KB, 181 lines) [Persian Project Overview]
├── .gitignore (41B, 2 lines) [Git Ignore Rules]
├── food_ordering.db (96KB, 19 lines) [Main SQLite Database]
└── start-backend.bat (1.0B, 1 lines) [Quick Start Script]
```

---

## 🔍 Module Breakdown

### Backend Modules (14 main modules)
1. **admin** - System administration and management
2. **auth** - Authentication and authorization with JWT
3. **common** - Shared utilities, constants, models, exceptions
4. **coupon** - Discount coupon system with usage tracking
5. **courier** - Delivery management and tracking
6. **favorites** - User favorites and wishlist functionality
7. **item** - Food item management and inventory
8. **menu** - Menu management for restaurants
9. **notification** - Real-time notification system
10. **order** - Order processing and management
11. **payment** - Payment processing with multiple methods
12. **restaurant** - Restaurant registration and management
13. **review** - Rating and review system
14. **vendor** - Vendor-specific operations

### Frontend UI Modules (11 modules)
1. **admin** - Administrative interface
2. **auth** - Login, registration, profile management
3. **common** - Shared UI components and utilities
4. **coupon** - Coupon validation interface
5. **courier** - Delivery tracking interface
6. **menu** - Menu browsing and management
7. **order** - Order placement and tracking
8. **payment** - Payment processing interface
9. **restaurant** - Restaurant listing and details
10. **review** - Rating and review interface
11. **vendor** - Vendor search and management

---

## 📚 File Type Distribution

- **Java Source Files**: 180+ files
- **Java Test Files**: 70+ files
- **FXML UI Files**: 20 files
- **Configuration Files**: 15+ files
- **Documentation Files**: 11 files
- **Script Files**: 7 files
- **Build Files**: 2 POM.xml files

---

## 🏗️ Architecture Overview

The project follows a **layered architecture** with clear separation of concerns:

1. **Presentation Layer** (JavaFX Controllers + FXML)
2. **Service Layer** (Business Logic)
3. **Repository Layer** (Data Access)
4. **Model Layer** (Entity Classes)
5. **Infrastructure Layer** (Utilities, Configuration)

Each module is self-contained with its own Controller, Service, and Repository classes, following the **MVC pattern** consistently throughout the application.

---

**Generated**: December 2024  
**Last Updated**: December 2024  
**Project Version**: 1.0.0 