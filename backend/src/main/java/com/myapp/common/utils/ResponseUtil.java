package com.myapp.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * ابزار پاسخ‌دهی API
 * این کلاس برای استانداردسازی پاسخ‌های API استفاده می‌شود
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
public class ResponseUtil {
    
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * ایجاد پاسخ موفق
     */
    public static String success(Object data, String message) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("message", message);
            response.set("data", objectMapper.valueToTree(data));
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return error("خطا در تولید پاسخ: " + e.getMessage());
        }
    }
    
    /**
     * ایجاد پاسخ موفق بدون داده
     */
    public static String success(String message) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("message", message);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return error("خطا در تولید پاسخ: " + e.getMessage());
        }
    }
    
    /**
     * ایجاد پاسخ خطا
     */
    public static String error(String message) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", false);
            response.put("error", message);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"success\":false,\"error\":\"خطای داخلی سرور\"}";
        }
    }
    
    /**
     * ایجاد پاسخ خطا با کد خطا
     */
    public static String error(String message, String errorCode) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", false);
            response.put("error", message);
            response.put("errorCode", errorCode);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"success\":false,\"error\":\"خطای داخلی سرور\"}";
        }
    }
    
    /**
     * ایجاد پاسخ با وضعیت سفارشی
     */
    public static String custom(boolean success, String message, Object data) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", success);
            response.put("message", message);
            if (data != null) {
                response.set("data", objectMapper.valueToTree(data));
            }
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return error("خطا در تولید پاسخ: " + e.getMessage());
        }
    }
} 