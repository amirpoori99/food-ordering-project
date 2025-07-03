package com.myapp.common.utils;

// وارد کردن کتابخانه‌های Jackson برای کار با JSON
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * کلاس ابزاری برای serialize و deserialize کردن JSON
 * این کلاس عملیات تبدیل شیء به JSON و برعکس را فراهم می‌کند
 * از کتابخانه Jackson استفاده می‌کند
 */
public class JsonUtil {
    
    // نمونه ObjectMapper برای کل کلاس
    private static final ObjectMapper objectMapper;
    
    // تنظیم اولیه ObjectMapper در static block
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());                     // پشتیبانی از Java 8 Time API
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);  // حذف فیلدهای null
    }
    
    /**
     * تبدیل شیء به رشته JSON
     * 
     * @param object شیء برای تبدیل به JSON
     * @return رشته JSON
     * @throws RuntimeException در صورت خطا در serialize
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
    
    /**
     * تبدیل رشته JSON به شیء
     * 
     * @param json رشته JSON
     * @param clazz کلاس مقصد
     * @param <T> نوع شیء مقصد
     * @return شیء deserialize شده
     * @throws RuntimeException در صورت خطا در deserialize
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }
    
    /**
     * تبدیل رشته JSON به شیء با استفاده از TypeReference
     * 
     * @param json رشته JSON
     * @param typeRef نوع مقصد با استفاده از TypeReference
     * @param <T> نوع شیء مقصد
     * @return شیء deserialize شده
     * @throws RuntimeException در صورت خطا در deserialize
     */
    public static <T> T fromJson(String json, com.fasterxml.jackson.core.type.TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }
    
    /**
     * دریافت نمونه ObjectMapper برای استفاده‌های پیشرفته
     * در صورت نیاز به تنظیمات خاص JSON
     * 
     * @return نمونه ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
