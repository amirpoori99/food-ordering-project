package com.myapp.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * تست‌های کلاس JsonUtil
 */
public class JsonUtilTest {

    static class TestObject {
        private String name;
        private int age;
        
        public TestObject() {}
        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestObject that = (TestObject) obj;
            return age == that.age && Objects.equals(name, that.name);
        }
    }

    @Test
    @DisplayName("toJson should serialize objects correctly")
    public void testToJson() {
        TestObject obj = new TestObject("Ali", 25);
        String json = JsonUtil.toJson(obj);
        
        assertNotNull(json);
        assertTrue(json.contains("Ali"));
        assertTrue(json.contains("25"));
    }
    
    @Test
    @DisplayName("fromJson should deserialize objects correctly")
    public void testFromJson() {
        String json = "{\"name\":\"Ahmad\",\"age\":30}";
        TestObject obj = JsonUtil.fromJson(json, TestObject.class);
        
        assertNotNull(obj);
        assertEquals("Ahmad", obj.getName());
        assertEquals(30, obj.getAge());
    }
    
    @Test
    @DisplayName("round-trip serialization should preserve object")
    public void testRoundTrip() {
        TestObject original = new TestObject("Test User", 35);
        String json = JsonUtil.toJson(original);
        TestObject restored = JsonUtil.fromJson(json, TestObject.class);
        
        assertEquals(original, restored);
    }
    
    @Test
    @DisplayName("getObjectMapper should return valid mapper")
    public void testGetObjectMapper() {
        var mapper = JsonUtil.getObjectMapper();
        assertNotNull(mapper);
        
        TestObject obj = new TestObject("Test", 20);
        assertDoesNotThrow(() -> mapper.writeValueAsString(obj));
    }
    
    @Test
    @DisplayName("should handle null values")
    public void testNullHandling() {
        String result = JsonUtil.toJson(null);
        assertEquals("null", result);
    }
    
    @Test
    @DisplayName("should throw exception for invalid JSON")
    public void testInvalidJson() {
        assertThrows(RuntimeException.class, 
            () -> JsonUtil.fromJson("invalid json", TestObject.class));
    }
}