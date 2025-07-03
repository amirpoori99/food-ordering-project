# راهنمای امنیت - سیستم سفارش غذا

## مقدمه

این راهنما شامل بهترین شیوه‌های امنیتی برای پروژه سیستم سفارش غذا است. تمام توسعه‌دهندگان باید این راهنما را مطالعه و رعایت کنند.

## کلاس‌های امنیتی موجود

### 1. AdvancedSecurityUtil
**مسیر**: `backend/src/main/java/com/myapp/common/utils/AdvancedSecurityUtil.java`

#### ویژگی‌ها:
- رمزگذاری AES-256
- مدیریت کلیدهای امنیتی
- validation های امنیتی
- محافظت در برابر حملات

#### استفاده:
```java
// رمزگذاری داده
String encrypted = AdvancedSecurityUtil.encrypt(sensitiveData);

// رمزگشایی داده
String decrypted = AdvancedSecurityUtil.decrypt(encryptedData);

// validation امنیتی
boolean isValid = AdvancedSecurityUtil.validateInput(input);
```

### 2. PasswordUtil
**مسیر**: `backend/src/main/java/com/myapp/common/utils/PasswordUtil.java`

#### ویژگی‌ها:
- هش کردن رمز عبور با BCrypt
- بررسی قدرت رمز عبور
- تولید رمز عبور تصادفی
- validation رمز عبور

#### استفاده:
```java
// هش کردن رمز عبور
String hashedPassword = PasswordUtil.hashPassword(plainPassword);

// بررسی رمز عبور
boolean isValid = PasswordUtil.verifyPassword(plainPassword, hashedPassword);

// بررسی قدرت رمز عبور
boolean isStrong = PasswordUtil.isStrongPassword(password);
```

### 3. ValidationUtil
**مسیر**: `backend/src/main/java/com/myapp/common/utils/ValidationUtil.java`

#### ویژگی‌ها:
- validation های امنیتی پیشرفته
- بررسی تزریق SQL
- validation XSS
- بررسی امنیت ورودی‌ها

#### استفاده:
```java
// validation ایمیل
boolean isValidEmail = ValidationUtil.isValidEmail(email);

// validation شماره تلفن
boolean isValidPhone = ValidationUtil.isValidPhone(phone);

// بررسی تزریق SQL
boolean isSafe = ValidationUtil.isSqlInjectionSafe(input);
```

## بهترین شیوه‌های امنیتی

### 1. مدیریت رمز عبور
- حداقل 8 کاراکتر
- شامل حروف بزرگ و کوچک
- شامل اعداد و کاراکترهای خاص
- عدم استفاده از رمزهای عبور رایج

### 2. اعتبارسنجی ورودی
- همیشه ورودی‌های کاربر را اعتبارسنجی کنید
- از SQL Injection محافظت کنید
- از XSS محافظت کنید
- ورودی‌ها را sanitize کنید

### 3. رمزگذاری داده‌ها
- داده‌های حساس را رمزگذاری کنید
- از الگوریتم‌های قوی استفاده کنید
- کلیدهای رمزگذاری را امن نگه دارید

### 4. مدیریت نشست
- نشست‌ها را با timeout مناسب تنظیم کنید
- نشست‌های غیرفعال را پاک کنید
- از HTTPS استفاده کنید

## تست‌های امنیتی

### تست‌های موجود:
1. **testPasswordHashing**: تست هش کردن رمز عبور
2. **testPasswordValidation**: تست validation رمز عبور
3. **testEncryptionDecryption**: تست رمزگذاری و رمزگشایی
4. **testSecurityValidation**: تست validation های امنیتی
5. **testInputSanitization**: تست sanitization ورودی‌ها

### اجرای تست‌ها:
```bash
# اجرای تمام تست‌های امنیتی
mvn test -Dtest=*Security*Test

# اجرای تست‌های خاص
mvn test -Dtest=AdvancedSecurityUtilTest
```

## چک‌لیست امنیتی

### قبل از commit:
- [ ] تمام ورودی‌ها اعتبارسنجی شده‌اند
- [ ] رمزهای عبور هش شده‌اند
- [ ] داده‌های حساس رمزگذاری شده‌اند
- [ ] تست‌های امنیتی اجرا شده‌اند
- [ ] کد بررسی امنیتی شده است

### قبل از deploy:
- [ ] تمام تست‌ها موفق هستند
- [ ] تنظیمات امنیتی بررسی شده‌اند
- [ ] لاگ‌های امنیتی فعال هستند
- [ ] monitoring امنیتی تنظیم شده است

## تنظیمات امنیتی

### application.properties:
```properties
# تنظیمات امنیتی
security.encryption.key=your-secure-key
security.password.min-length=8
security.session.timeout=3600
security.rate-limit.enabled=true
```

### hibernate.cfg.xml:
```xml
<!-- تنظیمات امنیتی Hibernate -->
<property name="hibernate.connection.provider_class">
    org.hibernate.connection.C3P0ConnectionProvider
</property>
```

## لاگ‌های امنیتی

### فعال‌سازی لاگ‌های امنیتی:
```properties
# لاگ‌های امنیتی
logging.level.com.myapp.security=DEBUG
logging.file.name=logs/security.log
```

### نمونه لاگ‌های امنیتی:
```
2025-06-15 10:30:15 - SECURITY - Failed login attempt for user: admin
2025-06-15 10:30:20 - SECURITY - Successful login for user: user123
2025-06-15 10:35:10 - SECURITY - Password changed for user: user456
```

## پاسخ به حوادث امنیتی

### مراحل پاسخ:
1. **تشخیص**: شناسایی حادثه امنیتی
2. **تحلیل**: بررسی ماهیت و گستره حادثه
3. **مهار**: جلوگیری از گسترش حادثه
4. **حذف**: حذف تهدید از سیستم
5. **بازیابی**: بازگرداندن سیستم به حالت عادی
6. **یادگیری**: بررسی و بهبود سیستم

### تماس‌های اضطراری:
- **مدیر امنیت**: security@foodordering.com
- **تیم فنی**: tech@foodordering.com
- **پشتیبانی**: support@foodordering.com

## منابع آموزشی

### مستندات:
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP Cheat Sheet Series](https://cheatsheetseries.owasp.org/)
- [Java Security Best Practices](https://docs.oracle.com/javase/8/docs/technotes/guides/security/)

### ابزارها:
- [OWASP ZAP](https://owasp.org/www-project-zap/)
- [SonarQube](https://www.sonarqube.org/)
- [Dependency Check](https://owasp.org/www-project-dependency-check/)

## به‌روزرسانی‌های امنیتی

### برنامه به‌روزرسانی:
- **ماهانه**: بررسی آسیب‌پذیری‌های جدید
- **فصلی**: به‌روزرسانی کتابخانه‌ها
- **سالانه**: بررسی جامع امنیتی

### فرآیند به‌روزرسانی:
1. شناسایی به‌روزرسانی‌های امنیتی
2. تست در محیط توسعه
3. تست در محیط staging
4. deploy در محیط production
5. نظارت بر عملکرد

## نتیجه‌گیری

امنیت یکی از مهم‌ترین جنبه‌های پروژه است. تمام اعضای تیم باید این راهنما را رعایت کنند و در صورت مشاهده هرگونه مشکل امنیتی، بلافاصله گزارش دهند.

---
**آخرین به‌روزرسانی**: 15 ژوئن 2025  
**مسئول امنیت**: Food Ordering System Security Team 