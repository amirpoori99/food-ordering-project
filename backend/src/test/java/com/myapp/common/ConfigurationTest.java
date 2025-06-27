package com.myapp.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.Properties;
import java.sql.DriverManager;
import java.sql.Connection;
import com.myapp.common.utils.DatabaseUtil;

/**
 * کلاس تست برای بررسی پیکربندی‌های برنامه
 * این کلاس پیکربندی‌های Maven، dependencies و تنظیمات پایگاه داده را تست می‌کند
 */
@DisplayName("تست‌های پیکربندی")
class ConfigurationTest {

    private static Properties properties;

    /**
     * بارگذاری تنظیمات یکبار قبل از اجرای تمام تست‌ها
     */
    @BeforeAll
    static void loadConfiguration() {
        properties = new Properties();
        
        // تلاش برای بارگذاری فایل application.properties
        try (InputStream input = ConfigurationTest.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input != null) {
                properties.load(input);
                System.out.println("✅ فایل application.properties بارگذاری شد");
            } else {
                System.out.println("⚠️ فایل application.properties یافت نشد - از تنظیمات پیش‌فرض استفاده می‌شود");
            }
        } catch (Exception e) {
            System.out.println("⚠️ خطا در بارگذاری application.properties: " + e.getMessage());
        }
    }

    /**
     * تست بررسی نسخه Java
     * اطمینان از اینکه برنامه با Java 17+ اجرا می‌شود
     */
    @Test
    @DisplayName("نسخه Java باید 17 یا بالاتر باشد")
    void testJavaVersion() {
        // دریافت نسخه Java فعلی
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion, "نسخه Java باید قابل دریافت باشد");
        
        System.out.println("📋 نسخه Java: " + javaVersion);
        
        // بررسی اینکه نسخه Java حداقل 17 باشد
        String[] versionParts = javaVersion.split("\\.");
        int majorVersion;
        
        if (versionParts[0].equals("1")) {
            // فرمت قدیمی: 1.8.0_xxx
            majorVersion = Integer.parseInt(versionParts[1]);
        } else {
            // فرمت جدید: 17.0.1
            majorVersion = Integer.parseInt(versionParts[0]);
        }
        
        assertTrue(majorVersion >= 17, 
                String.format("نسخه Java باید حداقل 17 باشد، ولی %d است", majorVersion));
    }

    /**
     * تست بررسی در دسترس بودن کلاس‌های ضروری
     * اطمینان از اینکه تمام dependencies لازم موجود هستند
     */
    @Test
    @DisplayName("تمام کلاس‌های ضروری باید در classpath موجود باشند")
    void testRequiredClassesAvailable() {
        // فهرست کلاس‌های ضروری که باید در classpath موجود باشند
        String[] requiredClasses = {
            // Hibernate
            "org.hibernate.Session",
            "org.hibernate.SessionFactory",
            "jakarta.persistence.Entity",
            
            // SQLite
            "org.sqlite.JDBC",
            
            // Jackson (JSON processing)
            "com.fasterxml.jackson.databind.ObjectMapper",
            "com.fasterxml.jackson.databind.JsonNode",
            
            // JWT
            "io.jsonwebtoken.Jwts",
            "io.jsonwebtoken.Claims",
            
            // JUnit 5
            "org.junit.jupiter.api.Test",
            
            // AssertJ
            "org.assertj.core.api.Assertions",
            
            // Mockito
            "org.mockito.Mockito",
            
            // H2 Database
            "org.h2.Driver"
        };

        // بررسی در دسترس بودن هر کلاس
        for (String className : requiredClasses) {
            assertDoesNotThrow(() -> {
                Class.forName(className);
            }, String.format("کلاس %s باید در classpath موجود باشد", className));
        }
        
        System.out.println("✅ تمام " + requiredClasses.length + " کلاس ضروری در دسترس هستند");
    }

    /**
     * تست اتصال پایگاه داده SQLite
     * بررسی اینکه driver SQLite صحیح کار می‌کند
     */
    @Test
    @DisplayName("اتصال پایگاه داده SQLite باید کار کند")
    void testSQLiteConnection() {
        // تست اتصال مستقیم به SQLite با JDBC
        assertDoesNotThrow(() -> {
            String url = "jdbc:sqlite::memory:"; // پایگاه داده در حافظه برای تست
            
            try (Connection connection = DriverManager.getConnection(url)) {
                assertNotNull(connection, "اتصال SQLite نباید null باشد");
                assertFalse(connection.isClosed(), "اتصال SQLite باید باز باشد");
                
                // تست اجرای یک کوئری ساده
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT 1 as test");
                
                assertTrue(resultSet.next(), "نتایج کوئری باید موجود باشد");
                assertEquals(1, resultSet.getInt("test"), "مقدار برگشتی باید 1 باشد");
                
                System.out.println("✅ اتصال مستقیم SQLite تأیید شد");
            }
        }, "اتصال به پایگاه داده SQLite نباید خطا تولید کند");
    }

    /**
     * تست SessionFactory پایگاه داده
     * بررسی اینکه Hibernate صحیح پیکربندی شده است
     */
    @Test
    @DisplayName("Hibernate SessionFactory باید صحیح پیکربندی شده باشد")
    void testHibernateSessionFactory() {
        assertDoesNotThrow(() -> {
            // تلاش برای دریافت SessionFactory
            var sessionFactory = DatabaseUtil.getSessionFactory();
            
            assertNotNull(sessionFactory, "SessionFactory نباید null باشد");
            assertFalse(sessionFactory.isClosed(), "SessionFactory باید باز باشد");
            
            // تست ایجاد Session
            try (var session = sessionFactory.openSession()) {
                assertNotNull(session, "Session نباید null باشد");
                assertTrue(session.isOpen(), "Session باید باز باشد");
                
                System.out.println("✅ Hibernate SessionFactory صحیح کار می‌کند");
            }
            
        }, "راه‌اندازی Hibernate SessionFactory نباید خطا تولید کند");
    }

    /**
     * تست JWT utilities
     * بررسی اینکه ابزارهای JWT صحیح کار می‌کنند
     */
    @Test
    @DisplayName("JWT utilities باید صحیح کار کنند")
    void testJWTUtilities() {
        assertDoesNotThrow(() -> {
            // import کلاس JWT utility
            Class<?> jwtUtilClass = Class.forName("com.myapp.common.utils.JWTUtil");
            assertNotNull(jwtUtilClass, "کلاس JWTUtil باید موجود باشد");
            
            // بررسی وجود متدهای ضروری JWT
            var methods = jwtUtilClass.getDeclaredMethods();
            boolean hasGenerateMethod = false;
            boolean hasValidateMethod = false;
            
            for (var method : methods) {
                String methodName = method.getName().toLowerCase();
                if (methodName.contains("generate") || methodName.contains("create")) {
                    hasGenerateMethod = true;
                }
                if (methodName.contains("validate") || methodName.contains("verify")) {
                    hasValidateMethod = true;
                }
            }
            
            assertTrue(hasGenerateMethod, "JWT utility باید متد تولید token داشته باشد");
            assertTrue(hasValidateMethod, "JWT utility باید متد اعتبارسنجی token داشته باشد");
            
            System.out.println("✅ JWT utilities صحیح پیکربندی شده‌اند");
            
        }, "بررسی JWT utilities نباید خطا تولید کند");
    }

    /**
     * تست Jackson ObjectMapper
     * بررسی اینکه JSON processing صحیح کار می‌کند
     */
    @Test
    @DisplayName("Jackson ObjectMapper باید صحیح کار کند")
    void testJacksonObjectMapper() {
        assertDoesNotThrow(() -> {
            // ایجاد ObjectMapper با پشتیبانی از JSR310 (Java 8 time)
            var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            // تست تبدیل Object به JSON
            var testObject = new java.util.HashMap<String, Object>();
            testObject.put("message", "تست JSON");
            testObject.put("timestamp", java.time.Instant.now());
            testObject.put("number", 123);
            
            String json = objectMapper.writeValueAsString(testObject);
            assertNotNull(json, "JSON خروجی نباید null باشد");
            assertFalse(json.trim().isEmpty(), "JSON خروجی نباید خالی باشد");
            assertTrue(json.contains("تست JSON"), "JSON باید محتوای تست را داشته باشد");
            
            // تست تبدیل JSON به Object
            var jsonNode = objectMapper.readTree(json);
            assertEquals("تست JSON", jsonNode.get("message").asText(), 
                    "پیام JSON باید صحیح parse شود");
            assertEquals(123, jsonNode.get("number").asInt(), 
                    "عدد JSON باید صحیح parse شود");
            
            System.out.println("✅ Jackson ObjectMapper صحیح کار می‌کند");
            System.out.println("📋 نمونه JSON: " + json);
            
        }, "تست Jackson ObjectMapper نباید خطا تولید کند");
    }

    /**
     * تست متغیرهای محیط مهم
     * بررسی تنظیمات مهم سیستم
     */
    @Test
    @DisplayName("متغیرهای محیط مهم باید تنظیم شده باشند")
    void testEnvironmentVariables() {
        // بررسی متغیرهای محیط مهم Java
        String javaHome = System.getProperty("java.home");
        assertNotNull(javaHome, "JAVA_HOME باید تنظیم شده باشد");
        assertFalse(javaHome.trim().isEmpty(), "JAVA_HOME نباید خالی باشد");
        
        String userDir = System.getProperty("user.dir");
        assertNotNull(userDir, "Working directory باید تنظیم شده باشد");
        assertTrue(userDir.contains("food-ordering") || userDir.contains("backend"), 
                "Working directory باید مربوط به پروژه باشد");
        
        String osName = System.getProperty("os.name");
        assertNotNull(osName, "نام سیستم عامل باید مشخص باشد");
        
        System.out.println("💻 سیستم عامل: " + osName);
        System.out.println("📁 پوشه کاری: " + userDir);
        System.out.println("☕ Java Home: " + javaHome);
        
        // نمایش اطلاعات حافظه
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024); // تبدیل به MB
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        
        System.out.println("🧠 حافظه حداکثر: " + maxMemory + " MB");
        System.out.println("🧠 حافظه کل: " + totalMemory + " MB");
        System.out.println("🧠 حافظه آزاد: " + freeMemory + " MB");
        
        // اطمینان از اینکه حافظه کافی موجود است (حداقل 256 MB)
        assertTrue(maxMemory >= 256, 
                "حافظه حداکثر باید حداقل 256 MB باشد، ولی " + maxMemory + " MB است");
    }

    /**
     * تست کاراکترهای یونیکد و فارسی
     * اطمینان از پشتیبانی صحیح از متن فارسی
     */
    @Test
    @DisplayName("پشتیبانی از کاراکترهای فارسی باید صحیح باشد")
    void testUnicodeSupport() {
        // متن تست فارسی
        String persianText = "سیستم سفارش غذا - تست کاراکترهای فارسی ۱۲۳۴";
        String arabicNumbers = "۰۱۲۳۴۵۶۷۸۹";
        String englishNumbers = "0123456789";
        
        // بررسی طول صحیح رشته‌ها
        assertFalse(persianText.isEmpty(), "متن فارسی نباید خالی باشد");
        assertEquals(10, arabicNumbers.length(), "اعداد عربی باید 10 کاراکتر باشند");
        assertEquals(10, englishNumbers.length(), "اعداد انگلیسی باید 10 کاراکتر باشند");
        
        // تست encoding/decoding
        assertDoesNotThrow(() -> {
            byte[] utf8Bytes = persianText.getBytes("UTF-8");
            String decodedText = new String(utf8Bytes, "UTF-8");
            assertEquals(persianText, decodedText, "متن فارسی باید صحیح encode/decode شود");
            
            System.out.println("✅ پشتیبانی از یونیکد تأیید شد");
            System.out.println("📝 متن تست: " + persianText);
            System.out.println("🔢 اعداد فارسی: " + arabicNumbers);
            
        }, "تست یونیکد نباید خطا تولید کند");
    }
} 