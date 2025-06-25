package com.myapp.common.utils;

import java.util.Map;

/**
 * Utility class for parsing values from Maps with proper type conversion and validation
 */
public class MapParsingUtil {
    
    /**
     * Get required string value from map
     */
    public static String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        return value.toString().trim();
    }
    
    /**
     * Get optional string value from map with default
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
     * Get required Long value from map
     */
    public static Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for " + key + ": " + value);
        }
    }
    
    /**
     * Get optional Long value from map with default
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
     * Get required Double value from map
     */
    public static Double getDoubleFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        
        return parseDoubleValue(value, key);
    }
    
    /**
     * Get optional Double value from map with default
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
     * Get required Integer value from map
     */
    public static Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        
        return parseIntegerValue(value, key);
    }
    
    /**
     * Get optional Integer value from map with default
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
     * Get required Boolean value from map
     */
    public static Boolean getBooleanFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        String stringValue = value.toString().trim().toLowerCase();
        return "true".equals(stringValue) || "1".equals(stringValue) || "yes".equals(stringValue);
    }
    
    /**
     * Get optional Boolean value from map with default
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
     * Parse Double value with proper validation
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
            throw new IllegalArgumentException("Invalid number format for " + key + ": " + value);
        }
    }
    
    /**
     * Parse Integer value with proper validation
     */
    private static Integer parseIntegerValue(Object value, String key) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            Long longValue = (Long) value;
            if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
                throw new IllegalArgumentException("Value too large for integer: " + value);
            }
            return longValue.intValue();
        }
        if (value instanceof Double || value instanceof Float) {
            double doubleValue = ((Number) value).doubleValue();
            if (doubleValue != Math.floor(doubleValue) || doubleValue > Integer.MAX_VALUE || doubleValue < Integer.MIN_VALUE) {
                throw new IllegalArgumentException("Invalid integer value: " + value);
            }
            return (int) doubleValue;
        }
        
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for " + key + ": " + value);
        }
    }
    
    /**
     * Validate that all required fields are present in the map
     */
    public static void validateRequiredFields(Map<String, Object> map, String... requiredFields) {
        for (String field : requiredFields) {
            if (!map.containsKey(field) || map.get(field) == null) {
                throw new IllegalArgumentException("Missing required field: " + field);
            }
        }
    }
    
    /**
     * Check if a key exists and has a non-null value
     */
    public static boolean hasValue(Map<String, Object> map, String key) {
        return map.containsKey(key) && map.get(key) != null;
    }
} 