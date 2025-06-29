package com.myapp.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * تست‌های کلاس MapParsingUtil
 */
public class MapParsingUtilTest {

    @Test
    @DisplayName("getStringFromMap should parse string values correctly")
    public void testGetStringFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "تست");
        map.put("number", 123);
        
        assertEquals("تست", MapParsingUtil.getStringFromMap(map, "name"));
        assertEquals("123", MapParsingUtil.getStringFromMap(map, "number"));
        
        // ناموجود - باید exception پرتاب شود
        assertThrows(IllegalArgumentException.class, 
            () -> MapParsingUtil.getStringFromMap(map, "missing"));
    }
    
    @Test
    @DisplayName("getOptionalStringFromMap should return default for missing values")
    public void testGetOptionalStringFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "تست");
        
        assertEquals("تست", MapParsingUtil.getOptionalStringFromMap(map, "name", "default"));
        assertEquals("default", MapParsingUtil.getOptionalStringFromMap(map, "missing", "default"));
    }
    
    @Test
    @DisplayName("getIntegerFromMap should parse integer values")
    public void testGetIntegerFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("age", 25);
        map.put("stringNumber", "30");
        
        assertEquals(Integer.valueOf(25), MapParsingUtil.getIntegerFromMap(map, "age"));
        assertEquals(Integer.valueOf(30), MapParsingUtil.getIntegerFromMap(map, "stringNumber"));
        
        assertThrows(IllegalArgumentException.class,
            () -> MapParsingUtil.getIntegerFromMap(map, "missing"));
    }
    
    @Test
    @DisplayName("getOptionalIntegerFromMap should return default for missing values")
    public void testGetOptionalIntegerFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("age", 25);
        
        assertEquals(Integer.valueOf(25), MapParsingUtil.getOptionalIntegerFromMap(map, "age", 0));
        assertEquals(Integer.valueOf(0), MapParsingUtil.getOptionalIntegerFromMap(map, "missing", 0));
    }
    
    @Test
    @DisplayName("getDoubleFromMap should parse double values")
    public void testGetDoubleFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("price", 25.5);
        map.put("stringPrice", "30.75");
        
        assertEquals(Double.valueOf(25.5), MapParsingUtil.getDoubleFromMap(map, "price"));
        assertEquals(Double.valueOf(30.75), MapParsingUtil.getDoubleFromMap(map, "stringPrice"));
        
        assertThrows(IllegalArgumentException.class,
            () -> MapParsingUtil.getDoubleFromMap(map, "missing"));
    }
    
    @Test
    @DisplayName("getOptionalDoubleFromMap should return default for missing values")
    public void testGetOptionalDoubleFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("price", 25.5);
        
        assertEquals(Double.valueOf(25.5), MapParsingUtil.getOptionalDoubleFromMap(map, "price", 0.0));
        assertEquals(Double.valueOf(0.0), MapParsingUtil.getOptionalDoubleFromMap(map, "missing", 0.0));
    }
    
    @Test
    @DisplayName("getBooleanFromMap should parse boolean values")
    public void testGetBooleanFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("active", true);
        map.put("stringTrue", "true");
        map.put("stringFalse", "false");
        
        assertEquals(Boolean.TRUE, MapParsingUtil.getBooleanFromMap(map, "active"));
        assertEquals(Boolean.TRUE, MapParsingUtil.getBooleanFromMap(map, "stringTrue"));
        assertEquals(Boolean.FALSE, MapParsingUtil.getBooleanFromMap(map, "stringFalse"));
        
        assertThrows(IllegalArgumentException.class,
            () -> MapParsingUtil.getBooleanFromMap(map, "missing"));
    }
    
    @Test
    @DisplayName("getOptionalBooleanFromMap should return default for missing values")
    public void testGetOptionalBooleanFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("active", true);
        
        assertEquals(Boolean.TRUE, MapParsingUtil.getOptionalBooleanFromMap(map, "active", false));
        assertEquals(Boolean.FALSE, MapParsingUtil.getOptionalBooleanFromMap(map, "missing", false));
    }
    
    @Test
    @DisplayName("getLongFromMap should parse long values")
    public void testGetLongFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 123L);
        map.put("stringId", "456");
        
        assertEquals(Long.valueOf(123L), MapParsingUtil.getLongFromMap(map, "id"));
        assertEquals(Long.valueOf(456L), MapParsingUtil.getLongFromMap(map, "stringId"));
        
        assertThrows(IllegalArgumentException.class,
            () -> MapParsingUtil.getLongFromMap(map, "missing"));
    }
    
    @Test
    @DisplayName("getOptionalLongFromMap should return default for missing values")
    public void testGetOptionalLongFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 123L);
        
        assertEquals(Long.valueOf(123L), MapParsingUtil.getOptionalLongFromMap(map, "id", 0L));
        assertEquals(Long.valueOf(0L), MapParsingUtil.getOptionalLongFromMap(map, "missing", 0L));
    }
    
    @Test
    @DisplayName("validateRequiredFields should check required fields")
    public void testValidateRequiredFields() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        map.put("age", 25);
        
        // همه فیلدهای مورد نیاز موجود
        assertDoesNotThrow(() -> MapParsingUtil.validateRequiredFields(map, "name", "age"));
        
        // فیلد ناموجود
        assertThrows(IllegalArgumentException.class,
            () -> MapParsingUtil.validateRequiredFields(map, "name", "missing"));
    }
    
    @Test
    @DisplayName("hasValue should check value existence")
    public void testHasValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        map.put("emptyString", "");
        map.put("nullValue", null);
        
        assertTrue(MapParsingUtil.hasValue(map, "name"));
        assertTrue(MapParsingUtil.hasValue(map, "emptyString")); // empty string is still a value
        assertFalse(MapParsingUtil.hasValue(map, "nullValue"));
        assertFalse(MapParsingUtil.hasValue(map, "missing"));
    }
} 