package com.myapp.common.utils;

import java.util.Map;

/**
 * کلاس ابزاری برای تجزیه مقادیر از Map با تبدیل نوع صحیح و اعتبارسنجی
 * این کلاس متدهای امنی برای دریافت انواع داده‌ها از Map فراهم می‌کند
 * شامل پشتیبانی از مقادیر اجباری، اختیاری و مقادیر پیش‌فرض
 */
public class MapParsingUtil {
    
    /**
     * دریافت مقدار رشته‌ای اجباری از Map
     * 
     * @param map نقشه حاوی داده‌ها
     * @param key کلید مورد نظر
     * @return مقدار رشته‌ای تمیز شده
     * @throws IllegalArgumentException اگر کلید موجود نباشد یا null باشد
     */
    public static String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("فیلد اجباری موجود نیست: " + key);
        }
        return value.toString().trim();
    }
    
    /**
     * دریافت مقدار رشته‌ای اختیاری از Map با مقدار پیش‌فرض
     * 
     * @param map نقشه حاوی داده‌ها
     * @param key کلید مورد نظر
     * @param defaultValue مقدار پیش‌فرض در صورت نبودن کلید
     * @return مقدار رشته‌ای یا مقدار پیش‌فرض
     */
    public static String getOptionalStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        String stringValue = value.toString().trim();
        return stringValue.isEmpty() ? defaultValue : stringValue;
    }
    
    /**
     * دریافت مقدار Long اجباری از Map
     * 
     * @param map نقشه حاوی داده‌ها
     * @param key کلید مورد نظر
     * @return مقدار Long
     * @throws IllegalArgumentException اگر کلید موجود نباشد یا فرمت نامعتبر باشد
     */
    public static Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("فیلد اجباری موجود نیست: " + key);
        }
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("فرمت عددی نامعتبر برای " + key + ": " + value);
        }
    }
    
    /**
     * دریافت مقدار Long اختیاری از Map با مقدار پیش‌فرض
     * 
     * @param map نقشه حاوی داده‌ها
     * @param key کلید مورد نظر
     * @param defaultValue مقدار پیش‌فرض در صورت نبودن کلید
     * @return مقدار Long یا مقدار پیش‌فرض
     */
    public static Long getOptionalLongFromMap(Map<String, Object> map, String key, Long defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * دریافت مقدار Double اجباری از Map
     * 
     * @param map نقشه حاوی داده‌ها
     * @param key کلید مورد نظر
     * @return مقدار Double
     * @throws IllegalArgumentException اگر کلید موجود نباشد یا فرمت نامعتبر باشد
     */
    public static Double getDoubleFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("فیلد اجباری موجود نیست: " + key);
        }
        
        return parseDoubleValue(value, key);
    }
    
    /**
     * دریافت مقدار Double اختیاری از Map با مقدار پیش‌فرض
     * 
     * @param map نقشه حاوی داده‌ها
     * @param key کلید مورد نظر
     * @param defaultValue مقدار پیش‌فرض در صورت نبودن کلید
     * @return مقدار Double یا مقدار پیش‌فرض
     */
    public static Double getOptionalDoubleFromMap(Map<String, Object> map, String key, Double defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return parseDoubleValue(value, key);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
    
    /**
     * دریافت مقدار Integer اجباری از Map
     * 
     * @param map نقشه حاوی داده‌ها
     * @param key کلید مورد نظر
     * @return مقدار Integer
     * @throws IllegalArgumentException اگر کلید موجود نباشد یا فرمت نامعتبر باشد
     */
    public static Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("فیلد اجباری موجود نیست: " + key);
        }
        
        return parseIntegerValue(value, key);
    }
    
    /**
     * دریافت مقدار Integer اختیاری از Map با مقدار پیش‌فرض
     * 
     * @param map نقشه حاوی داده‌ها
     * @param key کلید مورد نظر
     * @param defaultValue مقدار پیش‌فرض در صورت نبودن کلید
     * @return مقدار Integer یا مقدار پیش‌فرض
     */
    public static Integer getOptionalIntegerFromMap(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return parseIntegerValue(value, key);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
    
    /**
     * دریافت مقدار Boolean اجباری از Map
     * 
     * @param map نقشه حاوی داده‌ها
     * @param key کلید مورد نظر
     * @return مقدار Boolean
     * @throws IllegalArgumentException اگر کلید موجود نباشد
     */
    public static Boolean getBooleanFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("فیلد اجباری موجود نیست: " + key);
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        String stringValue = value.toString().trim().toLowerCase();
        return "true".equals(stringValue) || "1".equals(stringValue) || "yes".equals(stringValue);
    }
    
    /**
     * دریافت مقدار Boolean اختیاری از Map با مقدار پیش‌فرض
     * 
     * @param map نقشه حاوی داده‌ها
     * @param key کلید مورد نظر
     * @param defaultValue مقدار پیش‌فرض در صورت نبودن کلید
     * @return مقدار Boolean یا مقدار پیش‌فرض
     */
    public static Boolean getOptionalBooleanFromMap(Map<String, Object> map, String key, Boolean defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        String stringValue = value.toString().trim().toLowerCase();
        return "true".equals(stringValue) || "1".equals(stringValue) || "yes".equals(stringValue);
    }
    
    /**
     * تجزیه مقدار Double با اعتبارسنجی مناسب
     * متد کمکی برای تبدیل انواع مختلف به Double
     * 
     * @param value مقدار برای تبدیل
     * @param key نام کلید برای پیام خطا
     * @return مقدار Double تبدیل شده
     * @throws IllegalArgumentException در صورت فرمت نامعتبر
     */
    private static Double parseDoubleValue(Object value, String key) {
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Float) {
            return ((Float) value).doubleValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("فرمت عددی نامعتبر برای " + key + ": " + value);
        }
    }
    
    /**
     * تجزیه مقدار Integer با اعتبارسنجی مناسب
     * متد کمکی برای تبدیل انواع مختلف به Integer
     * 
     * @param value مقدار برای تبدیل
     * @param key نام کلید برای پیام خطا
     * @return مقدار Integer تبدیل شده
     * @throws IllegalArgumentException در صورت فرمت نامعتبر یا خارج از محدوده
     */
    private static Integer parseIntegerValue(Object value, String key) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            Long longValue = (Long) value;
            if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
                throw new IllegalArgumentException("مقدار خیلی بزرگ برای integer: " + value);
            }
            return longValue.intValue();
        }
        if (value instanceof Double || value instanceof Float) {
            double doubleValue = ((Number) value).doubleValue();
            if (doubleValue != Math.floor(doubleValue) || doubleValue > Integer.MAX_VALUE || doubleValue < Integer.MIN_VALUE) {
                throw new IllegalArgumentException("مقدار integer نامعتبر: " + value);
            }
            return (int) doubleValue;
        }
        
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("فرمت عددی نامعتبر برای " + key + ": " + value);
        }
    }
    
    /**
     * اعتبارسنجی وجود تمام فیلدهای اجباری در نقشه
     * 
     * @param map نقشه برای بررسی
     * @param requiredFields فیلدهای اجباری
     * @throws IllegalArgumentException اگر هر یک از فیلدهای اجباری موجود نباشد
     */
    public static void validateRequiredFields(Map<String, Object> map, String... requiredFields) {
        for (String field : requiredFields) {
            if (!map.containsKey(field) || map.get(field) == null) {
                throw new IllegalArgumentException("فیلد اجباری موجود نیست: " + field);
            }
        }
    }
    
    /**
     * بررسی وجود کلید و داشتن مقدار غیر null
     * 
     * @param map نقشه برای بررسی
     * @param key کلید مورد نظر
     * @return true اگر کلید موجود و مقدار آن غیر null باشد
     */
    public static boolean hasValue(Map<String, Object> map, String key) {
        return map.containsKey(key) && map.get(key) != null;
    }
} 
