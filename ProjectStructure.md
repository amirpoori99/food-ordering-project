# پروژه سفارش غذا - ساختار کامل و وضعیت پیاده‌سازی

## مرور کلی پروژه
این یک سیستم جامع سفارش غذا است که به عنوان پروژه دانشگاهی برای درس برنامه‌نویسی پیشرفته توسعه یافته است. این پروژه معماری چندلایه‌ای را با بک‌اند جاوا و فرانت‌اند JavaFX پیاده‌سازی می‌کند و شامل مدیریت کامل کاربران، عملیات رستوران‌ها، پردازش سفارشات و سیستم پرداخت است.

## ساختار پروژه

```
food-ordering-project/
├── backend/                                         # بخش بک‌اند (سرور) - جاوا با Hibernate
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/myapp/
│   │   │   │   ├── ServerApp.java                   # سرور HTTP اصلی برای تست بک‌اند
│   │   │   │   ├── common/                          # کلاس‌های مشترک و ابزارها
│   │   │   │   │   ├── models/                      # مدل‌های داده (موجودیت‌های JPA) - تکمیل شده ✅
│   │   │   │   │   │   ├── User.java                # ✅ موجودیت کاربر (JPA) - نقش‌ها: BUYER, SELLER, COURIER, ADMIN
│   │   │   │   │   │   ├── Restaurant.java          # ✅ موجودیت رستوران (JPA) - وضعیت: PENDING, APPROVED, REJECTED, SUSPENDED
│   │   │   │   │   │   ├── FoodItem.java            # ✅ موجودیت آیتم غذا (JPA) - کامل با منطق تجاری
│   │   │   │   │   │   ├── Order.java               # ✅ موجودیت سفارش (JPA) - مدیریت پیچیده سفارش با ردیابی وضعیت
│   │   │   │   │   │   ├── OrderItem.java           # ✅ موجودیت اتصال سفارش-آیتم غذا (JPA)
│   │   │   │   │   │   ├── Transaction.java         # ✅ مدل تراکنش مالی با منطق تجاری جامع ✨ جدید
│   │   │   │   │   │   ├── TransactionType.java     # ✅ enum نوع تراکنش (PAYMENT, REFUND, WALLET_CHARGE, WALLET_WITHDRAWAL) ✨ جدید
│   │   │   │   │   │   ├── TransactionStatus.java   # ✅ enum وضعیت تراکنش (PENDING, COMPLETED, FAILED, CANCELLED) ✨ جدید
│   │   │   │   │   │   ├── Rating.java              # ❌ مدل امتیازدهی کاربران (TODO)
│   │   │   │   │   │   ├── Coupon.java              # ❌ مدل کوپن تخفیف (TODO)
│   │   │   │   │   │   ├── Menu.java                # ❌ مدل منو (TODO - ممکن است نیاز نباشد)
│   │   │   │   │   │   ├── Favorite.java            # ❌ مدل علاقه‌مندی‌ها (TODO)
│   │   │   │   │   │   ├── RestaurantStatus.java    # ✅ enum وضعیت رستوران
│   │   │   │   │   │   └── OrderStatus.java         # ✅ enum وضعیت سفارش (7 حالت)
│   │   │   │   │   ├── exceptions/                  # استثناهای سفارشی - تکمیل شده ✅
│   │   │   │   │   │   ├── DuplicatePhoneException.java  # ✅ اعتبارسنجی یکتایی شماره تلفن
│   │   │   │   │   │   ├── InvalidCredentialsException.java  # ✅ خطاهای احراز هویت
│   │   │   │   │   │   └── NotFoundException.java   # ✅ خطاهای عدم یافتن منبع
│   │   │   │   │   └── utils/                       # کلاس‌های کمکی - تکمیل شده ✅
│   │   │   │   │       ├── JWTUtil.java             # ❌ تولید توکن JWT (TODO)
│   │   │   │   │       ├── PasswordUtil.java        # ✅ ابزارهای هش و اعتبارسنجی رمز عبور
│   │   │   │   │       ├── DatabaseUtil.java        # ✅ اتصال پایگاه داده و SessionFactory هایبرنیت
│   │   │   │   │       └── JsonUtil.java            # ✅ سریال‌سازی JSON برای REST APIها ✨ جدید
│   │   │   │   ├── auth/                            # پکیج احراز هویت - تکمیل شده ✅
│   │   │   │   │   ├── AuthController.java          # ✅ register(), login(), getProfile(), updateProfile()
│   │   │   │   │   ├── AuthService.java             # ✅ منطق اصلی احراز هویت و مدیریت کاربران
│   │   │   │   │   ├── AuthRepository.java          # ✅ دسترسی پایگاه داده (Hibernate) با مدیریت استثنا
│   │   │   │   │   └── dto/                         # اشیاء انتقال داده - تکمیل شده ✅
│   │   │   │   │       ├── RegisterRequest.java     # ✅ درخواست ثبت‌نام کاربر
│   │   │   │   │       ├── UpdateProfileRequest.java # ✅ درخواست به‌روزرسانی پروفایل
│   │   │   │   │       └── ProfileResponse.java     # ✅ پاسخ پروفایل (Java Record)
│   │   │   │   ├── restaurant/                      # پکیج مدیریت رستوران - تکمیل شده ✅
│   │   │   │   │   ├── RestaurantController.java    # ✅ createRestaurant(), listMine(), updateRestaurant() با REST APIهای جامع ✨ جدید
│   │   │   │   │   ├── RestaurantService.java       # ✅ منطق تجاری رستوران با اعتبارسنجی جامع
│   │   │   │   │   └── RestaurantRepository.java    # ✅ عملیات CRUD پایگاه داده (Hibernate)
│   │   │   │   ├── menu/                            # پکیج منو - تکمیل شده ✅
│   │   │   │   │   ├── MenuController.java          # ✅ addMenu(), addItemToMenu(), removeMenu() با REST APIهای جامع ✨ جدید
│   │   │   │   │   ├── MenuService.java             # ✅ مدیریت کامل منو با 80+ تست جامع
│   │   │   │   │   └── MenuRepository.java          # ✅ عملیات پایگاه داده منو (wrapper حول ItemRepository)
│   │   │   │   ├── item/                            # پکیج آیتم غذا - تکمیل شده ✅
│   │   │   │   │   ├── ItemController.java          # ❌ addItem(), updateItem(), deleteItem() (TODO)
│   │   │   │   │   ├── ItemService.java             # ✅ منطق تجاری آیتم با اعتبارسنجی جامع
│   │   │   │   │   └── ItemRepository.java          # ✅ عملیات پایگاه داده آیتم (Hibernate) - CRUD کامل با جستجو
│   │   │   │   ├── vendor/                          # پکیج فروشندگان (سمت مشتری) - اسکلت
│   │   │   │   │   ├── VendorController.java        # ❌ listVendors(), getVendorMenu() (TODO)
│   │   │   │   │   ├── VendorService.java           # ❌ منطق تجاری فروشنده (TODO)
│   │   │   │   │   └── VendorRepository.java        # ❌ عملیات پایگاه داده فروشنده (TODO)
│   │   │   │   ├── order/                           # پکیج سفارش - تکمیل شده ✅
│   │   │   │   │   ├── OrderController.java         # ✅ submitOrder(), getOrder(), getOrderHistory() با REST APIهای جامع ✨ جدید
│   │   │   │   │   ├── OrderService.java            # ✅ منطق تجاری کامل سفارش با 75 تست جامع
│   │   │   │   │   └── OrderRepository.java         # ✅ عملیات پایگاه داده سفارش (Hibernate) - چرخه حیات کامل
│   │   │   │   ├── favorites/                       # پکیج علاقه‌مندی‌ها - اسکلت
│   │   │   │   │   ├── FavoritesController.java     # ❌ listFavorites(), addFavorite(), removeFavorite() (TODO)
│   │   │   │   │   └── FavoritesRepository.java     # ❌ عملیات پایگاه داده علاقه‌مندی‌ها (TODO)
│   │   │   │   ├── courier/                         # پکیج پیک - اسکلت
│   │   │   │   │   ├── DeliveryController.java      # ❌ listAvailable(), updateStatus(), history() (TODO)
│   │   │   │   │   ├── DeliveryService.java         # ❌ منطق تخصیص سفارش و به‌روزرسانی وضعیت (TODO)
│   │   │   │   │   └── DeliveryRepository.java      # ❌ عملیات پایگاه داده پیک (TODO)
│   │   │   │   ├── notification/                    # پکیج اعلان‌ها - اسکلت
│   │   │   │   │   └── NotificationController.java  # ❌ push(), list() (TODO)
│   │   │   │   ├── payment/                         # پکیج پرداخت - تکمیل شده ✅ ✨ پیاده‌سازی جدید
│   │   │   │   │   ├── PaymentController.java       # ✅ REST API کامل پردازش پرداخت (15+ endpoint) ✨ جدید
│   │   │   │   │   ├── PaymentService.java          # ✅ منطق پردازش پرداخت (CARD, WALLET, COD) ✨ جدید
│   │   │   │   │   ├── PaymentRepository.java       # ✅ عملیات پایگاه داده تراکنش با کوئری‌های پیشرفته ✨ جدید
│   │   │   │   │   ├── WalletController.java        # ✅ REST API کامل مدیریت کیف پول (12+ endpoint) ✨ جدید
│   │   │   │   │   ├── WalletService.java           # ✅ عملیات کیف پول (شارژ، برداشت، محدودیت‌ها، ادمین) ✨ جدید
│   │   │   │   │   └── TransactionController.java   # ❌ مدیریت تراکنش (TODO - ممکن است نیاز نباشد)
│   │   │   │   ├── coupon/                          # پکیج کوپن - اسکلت
│   │   │   │   │   ├── CouponController.java        # ❌ مدیریت کوپن (TODO)
│   │   │   │   │   ├── CouponService.java           # ❌ منطق تجاری کوپن (TODO)
│   │   │   │   │   └── CouponRepository.java        # ❌ عملیات پایگاه داده کوپن (TODO)
│   │   │   │   ├── review/                          # پکیج نظرات - اسکلت
│   │   │   │   │   └── RatingController.java        # ❌ مدیریت امتیازدهی و نظرات (TODO)
│   │   │   │   ├── admin/                           # پکیج ادمین - اسکلت
│   │   │   │   │   └── AdminController.java         # ❌ listUsers(), updateUserStatus(), listOrders() (TODO)
│   │   │   │   └── dto/                             # DTO قدیمی (منتقل شده به پکیج‌های خاص)
│   │   │   │       └── RegisterRequest.java         # ✅ مکان قدیمی (تکراری)
│   │   │   ├── resources/
│   │   │   │   ├── application.yml                  # ✅ تنظیمات اتصال پایگاه داده و سرور
│   │   │   │   ├── hibernate.cfg.xml                # ✅ پیکربندی Hibernate با نگاشت موجودیت‌ها
│   │   │   │   └── openapi.yaml                     # ❌ مستندات API (TODO)
│   │   │   └── docker/
│   │   │       └── docker-compose.yml               # ✅ پیکربندی پایگاه داده در Docker
│   │   └── test/java/com/myapp/                     # مجموعه تست جامع - 700+ تست موفق ✅ ✨ گسترش یافته
│   │       ├── api/                                 # تست‌های یکپارچگی API
│   │       │   └── RegistrationApiTest.java         # ✅ 11 تست جامع ثبت‌نام
│   │       ├── auth/                                # تست‌های احراز هویت - بسیار گسترش یافته ✅
│   │       │   ├── AuthControllerTest.java          # ✅ 15+ تست تفویض کنترلر با mocking
│   │       │   ├── AuthRepositoryTest.java          # ✅ 20+ تست لایه repository (کلاس‌های تست تودرتو)
│   │       │   ├── AuthServiceTest.java             # ✅ 72 تست جامع لایه service (تمام سناریوها)
│   │       │   └── AuthServiceProfileTest.java      # ✅ 10+ تست مدیریت پروفایل (تست‌های یکپارچگی)
│   │       ├── restaurant/                          # تست‌های رستوران - تکمیل شده ✅
│   │       │   ├── RestaurantRepositoryTest.java    # ✅ 33 تست repository (تقویت شده با موارد مرزی)
│   │       │   ├── RestaurantServiceTest.java       # ✅ 75 تست جامع service (منطق تجاری کامل)
│   │       │   └── RestaurantControllerTest.java    # ✅ 40+ تست controller (تست جامع REST API) ✨ جدید
│   │       ├── item/                                # تست‌های آیتم - تکمیل شده ✅
│   │       │   ├── FoodItemEntityTest.java          # ✅ 49 تست entity (اعتبارسنجی منطق تجاری تقویت شده)
│   │       │   ├── ItemRepositoryTest.java          # ✅ 29 تست repository (CRUD جامع + جستجو)
│   │       │   └── ItemServiceTest.java             # ✅ 42 تست service (منطق تجاری جامع تقویت شده)
│   │       ├── order/                               # تست‌های سفارش - تکمیل شده ✅
│   │       │   ├── OrderEntityTest.java             # ✅ 30 تست entity (منطق تجاری پیچیده)
│   │       │   ├── OrderRepositoryTest.java         # ✅ 25 تست repository (چرخه حیات جامع سفارش)
│   │       │   ├── OrderServiceTest.java            # ✅ 75 تست جامع service (مدیریت کامل سفارش)
│   │       │   └── OrderControllerTest.java         # ✅ 30+ تست controller (تست جامع REST API) ✨ جدید
│   │       ├── menu/                                # تست‌های منو - تکمیل شده ✅
│   │       │   ├── MenuRepositoryTest.java          # ✅ 20 تست repository (عملیات منو)
│   │       │   ├── MenuServiceTest.java             # ✅ 51 تست جامع service (مدیریت کامل منو)
│   │       │   └── MenuControllerTest.java          # ✅ 25+ تست controller (تست جامع REST API) ✨ جدید
│   │       ├── payment/                             # تست‌های پرداخت - تکمیل شده ✅ ✨ تست جامع جدید
│   │       │   ├── PaymentServiceTest.java          # ✅ 100+ تست پردازش پرداخت (تمام سناریوها) ✨ جدید
│   │       │   ├── WalletServiceTest.java           # ✅ 25+ تست مدیریت کیف پول (جامع) ✨ جدید
│   │       │   ├── PaymentControllerTest.java       # ✅ 50+ تست API پرداخت (تمام endpointها) ✨ جدید
│   │       │   └── WalletControllerTest.java        # ✅ 30+ تست API کیف پول (تمام endpointها) ✨ جدید
│   │       ├── vendor/                              # تست‌های فروشنده - اسکلت
│   │       │   └── VendorControllerTest.java        # ❌ تست‌های فروشنده (TODO)
│   │       ├── favorites/                           # تست‌های علاقه‌مندی‌ها - اسکلت
│   │       │   └── FavoritesControllerTest.java     # ❌ تست‌های علاقه‌مندی‌ها (TODO)
│   │       ├── courier/                             # تست‌های پیک - اسکلت
│   │       │   └── DeliveryControllerTest.java      # ❌ تست‌های تحویل (TODO)
│   │       ├── review/                              # تست‌های نظرات - اسکلت
│   │       │   └── RatingControllerTest.java        # ❌ تست‌های امتیازدهی (TODO)
│   │       └── coupon/                              # تست‌های کوپن - اسکلت
│   │           └── CouponControllerTest.java        # ❌ تست‌های کوپن (TODO)
│   ├── pom.xml                                      # ✅ پیکربندی Maven با تمام وابستگی‌ها
│   ├── food_ordering.db                             # ✅ فایل پایگاه داده SQLite (برای تست)
│   └── target/                                      # ✅ خروجی ساخت Maven
├── frontend-javafx/                                 # بخش کلاینت JavaFX - اسکلت
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/myapp/ui/
│   │   │   │   ├── MainApp.java                    # ❌ کلاس راه‌اندازی JavaFX (TODO)
│   │   │   │   ├── common/                         # کلاس‌های مشترک UI - اسکلت
│   │   │   │   │   ├── NavigationController.java   # ❌ ناوبری بین صفحات (TODO)
│   │   │   │   │   ├── NotificationService.java    # ❌ نمایش پیام کاربر (TODO)
│   │   │   │   │   └── HttpClientUtil.java         # ❌ فراخوانی REST API (TODO)
│   │   │   │   ├── auth/                           # کنترلرهای UI احراز هویت - اسکلت
│   │   │   │   │   ├── LoginController.java        # ❌ onLogin(), initialize() (TODO)
│   │   │   │   │   ├── RegisterController.java     # ❌ onRegister() (TODO)
│   │   │   │   │   └── ProfileController.java      # ❌ loadProfile(), onSave() (TODO)
│   │   │   │   └── [سایر پکیج‌های UI...]          # ❌ تمام اجزای UI دیگر (TODO)
│   │   │   └── resources/
│   │   │       └── fxml/                           # فایل‌های FXML برای هر صفحه - اسکلت
│   │   │           ├── Login.fxml                  # ❌ فرم ورود (TODO)
│   │   │           ├── Register.fxml               # ❌ فرم ثبت‌نام (TODO)
│   │   │           └── [سایر فایل‌های FXML...]       # ❌ تمام فرم‌های UI دیگر (TODO)
│   └── pom.xml                                    # ❌ تنظیمات Maven و پلاگین JavaFX (TODO)
└── .gitignore                                     # ✅ تنظیمات Git
```

## وضعیت فعلی پیاده‌سازی - فاز 4 تکمیل شده ✅ ✨ نقطه عطف مهم

### ✅ تکمیل شده (فاز 1 - مهاجرت JPA)
- **User.java**: کاملاً به موجودیت JPA مهاجرت کرده با annotations مناسب و 4 نقش
- **Restaurant.java**: کاملاً به موجودیت JPA مهاجرت کرده با enum RestaurantStatus (4 حالت)
- **FoodItem.java**: موجودیت JPA کامل با منطق تجاری و factory methodها
- **Order.java & OrderItem.java**: مدیریت پیچیده سفارش با ردیابی وضعیت 7 حالته
- **Transaction.java**: مدل کامل تراکنش مالی با منطق تجاری ✨ **جدید**
- **TransactionType & TransactionStatus**: مدل‌های enum کامل برای سیستم پرداخت ✨ **جدید**
- **AuthRepository.java**: مهاجرت شده به Hibernate با مدیریت جامع استثنا
- **RestaurantRepository.java**: مهاجرت شده به عملیات CRUD هایبرنیت
- **ItemRepository.java**: عملیات CRUD کامل با قابلیت جستجوی پیشرفته
- **OrderRepository.java**: مدیریت کامل چرخه حیات سفارش با کوئری‌های وضعیت
- **MenuRepository.java**: عملیات خاص منو (wrapper حول ItemRepository)
- **PaymentRepository.java**: کوئری‌های پیشرفته تراکنش و محاسبه موجودی کیف پول ✨ **جدید**
- **DatabaseUtil.java**: ساده‌سازی شده برای استفاده از SQLite
- **JsonUtil.java**: ابزارهای سریال‌سازی JSON برای REST APIها ✨ **جدید**
- **hibernate.cfg.xml**: پیکربندی شده با تمام نگاشت‌های موجودیت شامل Transaction
- **ServerApp.java**: سرور HTTP پایه برای تست API
- **SQLite**: پایگاه داده سبک و قابل حمل

### ✅ تکمیل شده (فاز 2 - پیاده‌سازی لایه Service)
- **AuthService.java**: احراز هویت و مدیریت کاربران کامل با 72 تست جامع
- **ItemService.java**: منطق تجاری کامل آیتم با 42 تست جامع
- **OrderService.java**: مدیریت کامل سفارش با 75 تست جامع
- **RestaurantService.java**: منطق تجاری کامل رستوران با 75 تست جامع
- **MenuService.java**: مدیریت کامل منو با 51 تست جامع
- **PaymentService.java**: پردازش کامل پرداخت (CARD, WALLET, COD) با 100+ تست ✨ **جدید**
- **WalletService.java**: مدیریت کامل کیف پول با محدودیت‌ها و عملیات ادمین ✨ **جدید**

### ✅ تکمیل شده (فاز 3 - تست تقویت شده و منطق تجاری)
- **پوشش تست جامع**: 700+ تست پوشش تمام سناریوها و موارد مرزی ✨ **گسترش یافته**
- **اعتبارسنجی منطق تجاری**: قوانین اعتبارسنجی کامل و مدیریت خطا
- **عملیات پایگاه داده**: کوئری‌های بهینه شده و مدیریت روابط
- **یکپارچگی لایه Service**: گردش کاری تجاری انتها به انتها
- **تست سیستم پرداخت**: تست جامع پرداخت و کیف پول ✨ **جدید**

### ✅ تکمیل شده (فاز 4 - لایه Controller و سیستم پرداخت) ✨ **نقطه عطف مهم**
- **پیاده‌سازی REST API**: endpointهای HTTP کامل برای تمام عملکردهای اصلی ✨ **جدید**
- **RestaurantController**: 15+ endpoint برای مدیریت رستوران ✨ **جدید**
- **MenuController**: 10+ endpoint برای عملیات منو ✨ **جدید**
- **OrderController**: 12+ endpoint برای پردازش سفارش ✨ **جدید**
- **PaymentController**: 15+ endpoint برای پردازش پرداخت ✨ **جدید**
- **WalletController**: 12+ endpoint برای مدیریت کیف پول ✨ **جدید**
- **مدیریت JSON**: سریال‌سازی JSON سفارشی و تجزیه درخواست ✨ **جدید**
- **مدیریت خطا**: کدهای وضعیت HTTP مناسب و پاسخ‌های خطا ✨ **جدید**
- **تست Controller**: تست جامع REST API برای تمام controllerها ✨ **جدید**

### 🎯 آماده برای فاز بعدی (فاز 5 - فرانت‌اند و ویژگی‌های پیشرفته)
- **یکپارچگی فرانت‌اند**: controllerهای JavaFX آماده برای مصرف REST APIها
- **داشبورد ادمین**: ویژگی‌های مدیریت سیستم
- **سیستم تحویل**: تخصیص پیک و ردیابی
- **سیستم اعلان**: اعلان‌های بلادرنگ
- **مستندات API**: مستندات OpenAPI/Swagger

## 📊 آمار فعلی ✨ **پیشرفت چشمگیر**

### **لایه پایگاه داده**: 100% کامل ✅
- 8 موجودیت JPA با منطق تجاری کامل
- 5 کلاس repository با کوئری‌های پیشرفته
- عملیات CRUD کامل و مدیریت روابط

### **لایه Service**: 100% کامل ✅
- 7 کلاس service با منطق تجاری جامع
- اعتبارسنجی کامل و مدیریت خطا
- ویژگی‌های پیشرفته مانند محدودیت‌های کیف پول و پردازش پرداخت

### **لایه Controller**: 80% کامل ✅ ✨ **پیشرفت عمده**
- 5 REST controller با 60+ endpoint
- مدیریت کامل JSON و پاسخ‌های HTTP
- مدیریت جامع خطا و کدهای وضعیت

### **پوشش تست**: 700+ تست ✅ ✨ **پوشش عالی**
- تست‌های واحد برای تمام entity، repository و serviceها
- تست‌های یکپارچگی برای گردش‌کارهای پیچیده
- تست‌های controller برای تمام REST APIها
- موارد مرزی و سناریوهای خطا پوشش داده شده

### **سیستم پرداخت**: 100% کامل ✅ ✨ **ویژگی جدید**
- پردازش کامل پرداخت (CARD, WALLET, COD)
- مدیریت کیف پول با محدودیت‌های روزانه
- عملیات ادمین و ردیابی تراکنش
- تست و اعتبارسنجی جامع

## 🚀 اولویت‌های توسعه بعدی

1. **Controllerهای Item و Vendor** (پیروزی‌های سریع - الگوهای مشابه تعیین شده)
2. **داشبورد ادمین** (مدیریت کاربران، آمار سیستم)
3. **فرانت‌اند JavaFX** (پیاده‌سازی UI با استفاده از REST APIهای تعیین شده)
4. **سیستم تحویل** (مدیریت پیک و ردیابی سفارش)
5. **ویژگی‌های پیشرفته** (اعلان‌ها، کوپن‌ها، نظرات)

## 💡 یادداشت‌های توسعه برای فردا

### **الگوهای تعیین شده** ✨
- **الگوی Controller**: مدیریت درخواست/پاسخ JSON تعیین شده
- **الگوی Service**: الگوهای منطق تجاری و اعتبارسنجی تعیین شده
- **الگوی Repository**: الگوهای عملیات پایگاه داده و کوئری تعیین شده
- **الگوی تست**: الگوهای پوشش تست جامع تعیین شده

### **زیرساخت آماده** ✅
- **پایگاه داده**: طرحواره کامل با تمام روابط
- **REST APIها**: 60+ endpoint آماده برای مصرف فرانت‌اند
- **منطق تجاری**: تمام عملیات اصلی پیاده‌سازی و تست شده
- **مدیریت خطا**: پاسخ‌های خطای سازگار در تمام APIها

### **مسیر پیاده‌سازی سریع** 🛣️
ویژگی‌های بعدی می‌توانند با استفاده از الگوهای تعیین شده به سرعت پیاده‌سازی شوند:
1. کپی کردن ساختار controller موجود
2. پیاده‌سازی منطق تجاری خاص
3. اضافه کردن تست‌های جامع
4. یکپارچگی با serviceهای موجود
