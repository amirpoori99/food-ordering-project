package com.myapp.common.utils;

import java.util.UUID;

public class TestDataHelper {
    public static String generateUniqueEmail() {
        return "testuser_" + UUID.randomUUID().toString().replace("-", "") + "@example.com";
    }

    public static String generateUniquePhone() {
        return "+989" + (long)(Math.random() * 1_000_000_000L);
    }
} 