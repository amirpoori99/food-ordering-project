package com.myapp.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for JSON serialization and deserialization
 */
public class JsonUtil {
    
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    
    /**
     * Convert object to JSON string
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
    
    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }
    
    /**
     * Get ObjectMapper instance for advanced usage
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
