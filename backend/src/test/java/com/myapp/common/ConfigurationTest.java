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
 * تطبیق شده با Pure Java + Hibernate + PostgreSQL Architecture
 */
@DisplayName("تست‌های پیکربندی")
class ConfigurationTest {

    private static Properties properties;

    /**
     * بارگذاری تنظیمات یکبار قبل از اجرای تمام تست‌ها
     * حالا از hibernate.cfg.xml استفاده می‌کند (نه application.properties)
     */
    @BeforeAll
    static void loadConfiguration() {
        properties = new Properties();
        
        // تلاش برای بررسی hibernate.cfg.xml (که واقعاً استفاده می‌شود)
        try (InputStream input = ConfigurationTest.class.getClassLoader()
                .getResourceAsStream("hibernate.cfg.xml")) {
            
            if (input != null) {
                System.out.println("✅ فایل hibernate.cfg.xml موجود است");
                System.out.println("✅ Pure Java + Hibernate Configuration فعال");
            } else {
                System.out.println("⚠️ فایل hibernate.cfg.xml یافت نشد");
                fail("hibernate.cfg.xml برای Pure Java + Hibernate لازم است");
            }
        } catch (Exception e) {
            System.out.println("⚠️ خطا در بررسی hibernate.cfg.xml: " + e.getMessage());
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
     * تطبیق شده با Pure Java + PostgreSQL Architecture
     */
    @Test
    @DisplayName("تمام کلاس‌های ضروری باید در classpath موجود باشند")
    void testRequiredClassesAvailable() {
        // فهرست کلاس‌های ضروری برای Pure Java + Hibernate + PostgreSQL
        String[] requiredClasses = {
            // Hibernate (Core dependency)
            "org.hibernate.Session",
            "org.hibernate.SessionFactory",
            "jakarta.persistence.Entity",
            
            // PostgreSQL Driver (Production database)
            "org.postgresql.Driver",
            
            // Jackson (JSON processing)
            "com.fasterxml.jackson.databind.ObjectMapper",
            "com.fasterxml.jackson.databind.JsonNode",
            
            // JWT (Authentication)
            "io.jsonwebtoken.Jwts",
            "io.jsonwebtoken.Claims",
            
            // JUnit 5 (Testing)
            "org.junit.jupiter.api.Test",
            
            // AssertJ (Testing assertions)
            "org.assertj.core.api.Assertions",
            
            // Mockito (Testing mocks)
            "org.mockito.Mockito"
        };

        // بررسی در دسترس بودن هر کلاس
        for (String className : requiredClasses) {
            assertDoesNotThrow(() -> {
                Class.forName(className);
            }, String.format("کلاس %s باید در classpath موجود باشد", className));
        }
        
        System.out.println("✅ تمام " + requiredClasses.length + " کلاس ضروری در دسترس هستند");
        System.out.println("✅ Pure Java + Hibernate + PostgreSQL Architecture تأیید شد");
    }

    /**
     * تست اتصال PostgreSQL (در محیط تست با H2 in-memory)
     * بررسی اینکه PostgreSQL driver موجود است
     */
    @Test
    @DisplayName("PostgreSQL driver باید در دسترس باشد")
    void testPostgreSQLDriver() {
        assertDoesNotThrow(() -> {
            // تست بارگذاری PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ PostgreSQL driver موجود است");
            
            // نمایش اطلاعات driver
            var driver = DriverManager.getDriver("jdbc:postgresql://localhost:5432/test");
            if (driver != null) {
                System.out.println("📋 PostgreSQL Driver Version: " + driver.getMajorVersion() + "." + driver.getMinorVersion());
            }
            
        }, "PostgreSQL driver باید در classpath موجود باشد");
    }

    /**
     * تست SessionFactory پایگاه داده
     * بررسی اینکه Hibernate صحیح پیکربندی شده است
     * این تست ممکن است fail شود اگر PostgreSQL در دسترس نباشد (که طبیعی است)
     */
    @Test
    @DisplayName("Hibernate Configuration باید قابل بارگذاری باشد")
    void testHibernateConfiguration() {
        // تست بارگذاری Configuration (نه SessionFactory که نیاز به DB دارد)
        assertDoesNotThrow(() -> {
            // تست بارگذاری hibernate configuration
            org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
            configuration.configure();
            
            // بررسی برخی properties مهم
            String dialect = configuration.getProperty("hibernate.dialect");
            String driver = configuration.getProperty("hibernate.connection.driver_class");
            
            assertNotNull(dialect, "Hibernate dialect باید تنظیم شده باشد");
            assertNotNull(driver, "Database driver باید تنظیم شده باشد");
            
            System.out.println("✅ Hibernate Configuration بارگذاری شد");
            System.out.println("📋 Database Driver: " + driver);
            System.out.println("📋 Hibernate Dialect: " + dialect);
            
        }, "بارگذاری Hibernate Configuration نباید خطا تولید کند");
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