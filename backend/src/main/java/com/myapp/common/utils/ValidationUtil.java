package com.myapp.common.utils;

import com.myapp.common.constants.ApplicationConstants;

import java.util.regex.Pattern;

/**
 * کلاس ابزاری اعتبارسنجی متمرکز - ولیدیشن یکپارچه در کل برنامه
 * این کلاس مجموعه کاملی از متدهای اعتبارسنجی برای انواع داده‌ها فراهم می‌کند
 * شامل ولیدیشن ایمیل، تلفن، رمز عبور، نام، شماره، قیمت و logic تجاری
 */
public class ValidationUtil {
    
    // الگوهای regex پیش‌کامپایل شده برای بهبود عملکرد
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"  // فرمت استاندارد ایمیل
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^09\\d{9}$"  // فرمت شماره موبایل ایرانی (09xxxxxxxxx)
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"  // رمز عبور قوی
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF\\s\\w.-]+$"  // کاراکترهای فارسی + انگلیسی
    );
    
    // سازنده خصوصی برای جلوگیری از ایجاد نمونه از کلاس utility
    private ValidationUtil() {
        throw new UnsupportedOperationException("این کلاس utility است و نمی‌توان از آن نمونه ایجاد کرد");
    }
    
    // ==================== اعتبارسنجی رشته متن ====================
    
    /**
     * بررسی اینکه رشته null یا خالی نباشد
     * 
     * @param value رشته برای بررسی
     * @return true اگر رشته معتبر باشد (نه null و نه خالی)
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * بررسی طول رشته در محدوده مشخص
     * 
     * @param value رشته برای بررسی
     * @param minLength حداقل طول مجاز
     * @param maxLength حداکثر طول مجاز
     * @return true اگر طول رشته در محدوده باشد
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) return false;
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * اعتبارسنجی فیلد اجباری رشته‌ای
     * 
     * @param value مقدار برای بررسی
     * @param fieldName نام فیلد برای پیام خطا
     * @throws IllegalArgumentException اگر فیلد null یا خالی باشد
     */
    public static void validateRequiredString(String value, String fieldName) {
        if (!isNotEmpty(value)) {
            throw new IllegalArgumentException(fieldName + " نمی‌تواند null یا خالی باشد");
        }
    }
    
    /**
     * اعتبارسنجی طول رشته با پیام خطای سفارشی
     * 
     * @param value رشته برای بررسی
     * @param fieldName نام فیلد برای پیام خطا
     * @param minLength حداقل طول مجاز
     * @param maxLength حداکثر طول مجاز
     * @throws IllegalArgumentException اگر طول نامعتبر باشد
     */
    public static void validateStringLength(String value, String fieldName, int minLength, int maxLength) {
        if (value != null && !isValidLength(value, minLength, maxLength)) {
            throw new IllegalArgumentException(String.format(
                "%s باید بین %d تا %d کاراکتر باشد", fieldName, minLength, maxLength
            ));
        }
    }
    
    // ==================== اعتبارسنجی ایمیل ====================
    
    /**
     * بررسی صحت فرمت ایمیل
     * 
     * @param email آدرس ایمیل برای بررسی
     * @return true اگر فرمت ایمیل معتبر باشد
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * اعتبارسنجی ایمیل با پرتاب استثنا در صورت نامعتبر بودن
     * 
     * @param email آدرس ایمیل برای بررسی
     * @throws IllegalArgumentException اگر فرمت ایمیل نامعتبر باشد
     */
    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException(ApplicationConstants.ERROR_MESSAGES.INVALID_EMAIL_FORMAT);
        }
    }
    
    // ==================== اعتبارسنجی شماره تلفن ====================
    
    /**
     * بررسی صحت فرمت شماره موبایل ایرانی
     * فرمت قابل قبول: 09xxxxxxxxx (11 رقم شروع با 09)
     * 
     * @param phone شماره تلفن برای بررسی
     * @return true اگر فرمت شماره تلفن معتبر باشد
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * اعتبارسنجی شماره تلفن با پرتاب استثنا در صورت نامعتبر بودن
     * 
     * @param phone شماره تلفن برای بررسی
     * @throws IllegalArgumentException اگر فرمت شماره تلفن نامعتبر باشد
     */
    public static void validatePhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException(ApplicationConstants.ERROR_MESSAGES.INVALID_PHONE_FORMAT);
        }
    }
    
    // ==================== اعتبارسنجی رمز عبور ====================
    
    /**
     * بررسی قدرت رمز عبور
     * رمز عبور باید حداقل 8 کاراکتر، شامل حروف بزرگ، کوچک، عدد و کاراکتر خاص باشد
     * 
     * @param password رمز عبور برای بررسی
     * @return true اگر رمز عبور قوی باشد
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * اعتبارسنجی رمز عبور با پرتاب استثنا در صورت ضعیف بودن
     * 
     * @param password رمز عبور برای بررسی
     * @throws IllegalArgumentException اگر رمز عبور ضعیف باشد
     */
    public static void validatePassword(String password) {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(ApplicationConstants.ERROR_MESSAGES.WEAK_PASSWORD);
        }
    }
    
    /**
     * دریافت شرایط رمز عبور معتبر
     * 
     * @return توضیح کامل شرایط رمز عبور قوی
     */
    public static String getPasswordRequirements() {
        return "رمز عبور باید شامل موارد زیر باشد:\n" +
               "- حداقل 8 کاراکتر\n" +
               "- حداقل یک حرف بزرگ انگلیسی (A-Z)\n" +
               "- حداقل یک حرف کوچک انگلیسی (a-z)\n" +
               "- حداقل یک عدد (0-9)\n" +
               "- حداقل یک کاراکتر خاص (@#$%^&+=)\n" +
               "- بدون فاصله یا space";
    }
    
    // ==================== اعتبارسنجی نام ====================
    
    /**
     * بررسی اعتبار نام (پشتیبانی از کاراکترهای فارسی و انگلیسی)
     * 
     * @param name نام برای بررسی
     * @return true اگر نام معتبر باشد
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches() && 
               isValidLength(name, 2, ApplicationConstants.VALIDATION.MAX_NAME_LENGTH);
    }
    
    /**
     * اعتبارسنجی نام با پرتاب استثنا
     * 
     * @param name نام برای بررسی
     * @param fieldName نام فیلد برای پیام خطا
     * @throws IllegalArgumentException اگر نام نامعتبر باشد
     */
    public static void validateName(String name, String fieldName) {
        validateRequiredString(name, fieldName);
        if (!isValidName(name)) {
            throw new IllegalArgumentException(fieldName + " حاوی کاراکترهای نامعتبر یا طول نامناسب است");
        }
    }
    
    // ==================== اعتبارسنجی عددی ====================
    
    /**
     * بررسی مثبت بودن عدد
     * 
     * @param number عدد برای بررسی
     * @return true اگر عدد مثبت باشد
     */
    public static boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }
    
    /**
     * بررسی غیرمنفی بودن عدد
     * 
     * @param number عدد برای بررسی
     * @return true اگر عدد غیرمنفی باشد
     */
    public static boolean isNonNegative(Number number) {
        return number != null && number.doubleValue() >= 0;
    }
    
    /**
     * بررسی قرار گیری عدد در محدوده مشخص
     * 
     * @param number عدد برای بررسی
     * @param min حداقل مقدار
     * @param max حداکثر مقدار
     * @return true اگر عدد در محدوده باشد
     */
    public static boolean isInRange(Number number, double min, double max) {
        if (number == null) return false;
        double value = number.doubleValue();
        return value >= min && value <= max;
    }
    
    /**
     * اعتبارسنجی عدد مثبت با پرتاب استثنا
     * 
     * @param number عدد برای بررسی
     * @param fieldName نام فیلد برای پیام خطا
     * @throws IllegalArgumentException اگر عدد مثبت نباشد
     */
    public static void validatePositiveNumber(Number number, String fieldName) {
        if (!isPositive(number)) {
            throw new IllegalArgumentException(fieldName + " باید مثبت باشد");
        }
    }
    
    /**
     * اعتبارسنجی قیمت
     * 
     * @param price قیمت برای بررسی
     * @throws IllegalArgumentException اگر قیمت نامعتبر باشد
     */
    public static void validatePrice(Double price) {
        if (price == null) {
            throw new IllegalArgumentException("قیمت نمی‌تواند null باشد");
        }
        if (!isInRange(price, ApplicationConstants.VALIDATION.MIN_PRICE, ApplicationConstants.VALIDATION.MAX_PRICE)) {
            throw new IllegalArgumentException(String.format(
                "قیمت باید بین %.2f و %.2f باشد", 
                ApplicationConstants.VALIDATION.MIN_PRICE, 
                ApplicationConstants.VALIDATION.MAX_PRICE
            ));
        }
    }
    
    /**
     * اعتبارسنجی تعداد
     * 
     * @param quantity تعداد برای بررسی
     * @throws IllegalArgumentException اگر تعداد نامعتبر باشد
     */
    public static void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("تعداد نمی‌تواند null باشد");
        }
        if (!isInRange(quantity, ApplicationConstants.VALIDATION.MIN_QUANTITY, ApplicationConstants.VALIDATION.MAX_QUANTITY)) {
            throw new IllegalArgumentException(String.format(
                "تعداد باید بین %d و %d باشد", 
                ApplicationConstants.VALIDATION.MIN_QUANTITY, 
                ApplicationConstants.VALIDATION.MAX_QUANTITY
            ));
        }
    }
    
    /**
     * اعتبارسنجی امتیاز
     * 
     * @param rating امتیاز برای بررسی
     * @throws IllegalArgumentException اگر امتیاز نامعتبر باشد
     */
    public static void validateRating(Integer rating) {
        if (rating == null) {
            throw new IllegalArgumentException("امتیاز نمی‌تواند null باشد");
        }
        if (!isInRange(rating, ApplicationConstants.VALIDATION.MIN_RATING, ApplicationConstants.VALIDATION.MAX_RATING)) {
            throw new IllegalArgumentException(String.format(
                "امتیاز باید بین %d و %d باشد", 
                ApplicationConstants.VALIDATION.MIN_RATING, 
                ApplicationConstants.VALIDATION.MAX_RATING
            ));
        }
    }
    
    // ==================== اعتبارسنجی شناسه ====================
    
    /**
     * بررسی اعتبار شناسه entity
     * 
     * @param id شناسه برای بررسی
     * @return true اگر شناسه معتبر باشد
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }
    
    /**
     * اعتبارسنجی شناسه با پرتاب استثنا
     * 
     * @param id شناسه برای بررسی
     * @param entityName نام entity برای پیام خطا
     * @throws IllegalArgumentException اگر شناسه نامعتبر باشد
     */
    public static void validateId(Long id, String entityName) {
        if (!isValidId(id)) {
            throw new IllegalArgumentException(entityName + " شناسه باید مثبت باشد");
        }
    }
    
    // ==================== اعتبارسنجی منطق تجاری ====================
    
    /**
     * بررسی اعتبار نقش کاربر
     * 
     * @param role نقش کاربر برای بررسی
     * @return true اگر نقش معتبر باشد
     */
    public static boolean isValidUserRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return false;
        }
        String upperRole = role.trim().toUpperCase();
        return upperRole.equals(ApplicationConstants.BUSINESS.ROLE_BUYER) ||
               upperRole.equals(ApplicationConstants.BUSINESS.ROLE_SELLER) ||
               upperRole.equals(ApplicationConstants.BUSINESS.ROLE_COURIER) ||
               upperRole.equals(ApplicationConstants.BUSINESS.ROLE_ADMIN);
    }
    
    /**
     * بررسی اعتبار روش پرداخت
     * 
     * @param paymentMethod روش پرداخت برای بررسی
     * @return true اگر روش پرداخت معتبر باشد
     */
    public static boolean isValidPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            return false;
        }
        String upperMethod = paymentMethod.trim().toUpperCase();
        return upperMethod.equals(ApplicationConstants.BUSINESS.PAYMENT_CARD) ||
               upperMethod.equals(ApplicationConstants.BUSINESS.PAYMENT_WALLET) ||
               upperMethod.equals(ApplicationConstants.BUSINESS.PAYMENT_COD);
    }
    
    /**
     * بررسی اعتبار وضعیت سفارش
     * 
     * @param status وضعیت سفارش برای بررسی
     * @return true اگر وضعیت معتبر باشد
     */
    public static boolean isValidOrderStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String upperStatus = status.trim().toUpperCase();
        return upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_PENDING) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_CONFIRMED) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_PREPARING) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_READY) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_PICKED_UP) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_DELIVERED) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_CANCELLED);
    }
    
    // ==================== اعتبارسنجی ترکیبی ====================
    
    /**
     * اعتبارسنجی داده‌های ثبت‌نام کاربر
     * 
     * @param fullName نام کامل
     * @param phone شماره تلفن
     * @param password رمز عبور
     * @param email ایمیل (اختیاری)
     * @param role نقش کاربر
     * @throws IllegalArgumentException اگر هر یک از داده‌ها نامعتبر باشد
     */
    public static void validateUserRegistration(String fullName, String phone, String password, String email, String role) {
        validateName(fullName, "نام کامل");
        validatePhone(phone);
        validatePassword(password);
        
        if (isNotEmpty(email)) {
            validateEmail(email);
        }
        
        if (!isValidUserRole(role)) {
            throw new IllegalArgumentException("نقش کاربر نامعتبر: " + role);
        }
    }
    
    /**
     * اعتبارسنجی داده‌های آیتم غذا
     * 
     * @param name نام آیتم
     * @param description توضیحات (اختیاری)
     * @param price قیمت
     * @param category دسته‌بندی
     * @param quantity تعداد (اختیاری)
     * @throws IllegalArgumentException اگر هر یک از داده‌ها نامعتبر باشد
     */
    public static void validateFoodItem(String name, String description, Double price, String category, Integer quantity) {
        validateName(name, "نام آیتم");
        validatePrice(price);
        validateRequiredString(category, "دسته‌بندی");
        
        if (description != null) {
            validateStringLength(description, "توضیحات", 0, ApplicationConstants.VALIDATION.MAX_DESCRIPTION_LENGTH);
        }
        
        if (quantity != null) {
            validateQuantity(quantity);
        }
    }
} 
