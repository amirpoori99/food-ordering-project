package com.myapp.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Database Encryption Utility
 * تابع‌های مربوط به رمزگذاری پایگاه داده
 */
public class DatabaseEncryption {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEncryption.class);
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String DB_FILE = "backend/food_ordering.db";
    private static final String ENCRYPTED_DB_FILE = "backend/food_ordering_encrypted.db";
    private static final String KEY_FILE = "backend/db_encryption.key";
    
    /**
     * تولید کلید رمزگذاری جدید
     */
    public static SecretKey generateEncryptionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256, new SecureRandom()); // AES-256
        return keyGenerator.generateKey();
    }
    
    /**
     * ذخیره کلید رمزگذاری در فایل
     */
    public static void saveEncryptionKey(SecretKey key, String keyFilePath) throws IOException {
        byte[] encoded = key.getEncoded();
        String encodedKey = Base64.getEncoder().encodeToString(encoded);
        Files.write(Paths.get(keyFilePath), encodedKey.getBytes());
        logger.info("کلید رمزگذاری ذخیره شد: {}", keyFilePath);
    }
    
    /**
     * بارگذاری کلید رمزگذاری از فایل
     */
    public static SecretKey loadEncryptionKey(String keyFilePath) throws IOException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(keyFilePath));
        String encodedKey = new String(keyBytes);
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }
    
    /**
     * رمزگذاری فایل پایگاه داده
     */
    public static boolean encryptDatabase() {
        try {
            logger.info("شروع رمزگذاری پایگاه داده...");
            
            // بررسی وجود فایل پایگاه داده
            File dbFile = new File(DB_FILE);
            if (!dbFile.exists()) {
                logger.error("فایل پایگاه داده یافت نشد: {}", DB_FILE);
                return false;
            }
            
            // تولید کلید رمزگذاری
            SecretKey secretKey;
            File keyFile = new File(KEY_FILE);
            
            if (keyFile.exists()) {
                logger.info("استفاده از کلید موجود...");
                secretKey = loadEncryptionKey(KEY_FILE);
            } else {
                logger.info("تولید کلید جدید...");
                secretKey = generateEncryptionKey();
                saveEncryptionKey(secretKey, KEY_FILE);
            }
            
            // رمزگذاری پایگاه داده
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            try (FileInputStream fis = new FileInputStream(dbFile);
                 FileOutputStream fos = new FileOutputStream(ENCRYPTED_DB_FILE)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                long fileSize = dbFile.length();
                
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] output = cipher.update(buffer, 0, bytesRead);
                    if (output != null) {
                        fos.write(output);
                    }
                    totalBytes += bytesRead;
                    
                    // نمایش پیشرفت
                    int progress = (int) ((totalBytes * 100) / fileSize);
                    if (progress % 10 == 0) {
                        logger.info("پیشرفت رمزگذاری: {}%", progress);
                    }
                }
                
                // نهایی کردن رمزگذاری
                byte[] outputBytes = cipher.doFinal();
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
            }
            
            logger.info("رمزگذاری پایگاه داده با موفقیت انجام شد");
            logger.info("فایل رمزگذاری شده: {}", ENCRYPTED_DB_FILE);
            logger.info("کلید رمزگذاری: {}", KEY_FILE);
            
            return true;
            
        } catch (Exception e) {
            logger.error("خطا در رمزگذاری پایگاه داده: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * رمزگشایی فایل پایگاه داده
     */
    public static boolean decryptDatabase() {
        try {
            logger.info("شروع رمزگشایی پایگاه داده...");
            
            File encryptedFile = new File(ENCRYPTED_DB_FILE);
            if (!encryptedFile.exists()) {
                logger.error("فایل رمزگذاری شده یافت نشد: {}", ENCRYPTED_DB_FILE);
                return false;
            }
            
            File keyFile = new File(KEY_FILE);
            if (!keyFile.exists()) {
                logger.error("فایل کلید یافت نشد: {}", KEY_FILE);
                return false;
            }
            
            // بارگذاری کلید
            SecretKey secretKey = loadEncryptionKey(KEY_FILE);
            
            // رمزگشایی
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            String decryptedFile = DB_FILE + ".decrypted";
            
            try (FileInputStream fis = new FileInputStream(encryptedFile);
                 FileOutputStream fos = new FileOutputStream(decryptedFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                long fileSize = encryptedFile.length();
                
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] output = cipher.update(buffer, 0, bytesRead);
                    if (output != null) {
                        fos.write(output);
                    }
                    totalBytes += bytesRead;
                    
                    int progress = (int) ((totalBytes * 100) / fileSize);
                    if (progress % 10 == 0) {
                        logger.info("پیشرفت رمزگشایی: {}%", progress);
                    }
                }
                
                byte[] outputBytes = cipher.doFinal();
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
            }
            
            logger.info("رمزگشایی پایگاه داده با موفقیت انجام شد: {}", decryptedFile);
            return true;
            
        } catch (Exception e) {
            logger.error("خطا در رمزگشایی پایگاه داده: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * بررسی وضعیت رمزگذاری پایگاه داده
     */
    public static boolean isDatabaseEncrypted() {
        File encryptedFile = new File(ENCRYPTED_DB_FILE);
        File keyFile = new File(KEY_FILE);
        
        boolean encrypted = encryptedFile.exists() && keyFile.exists();
        logger.info("وضعیت رمزگذاری پایگاه داده: {}", encrypted ? "رمزگذاری شده" : "رمزگذاری نشده");
        
        return encrypted;
    }
    
    /**
     * تنظیم محدودیت‌های دسترسی فایل
     */
    public static void setFilePermissions() {
        try {
            // محدود کردن دسترسی به فایل کلید
            File keyFile = new File(KEY_FILE);
            if (keyFile.exists()) {
                keyFile.setReadable(false, false);  // حذف دسترسی خواندن از همه
                keyFile.setReadable(true, true);    // فقط صاحب فایل
                keyFile.setWritable(false, false);  // حذف دسترسی نوشتن از همه
                keyFile.setWritable(true, true);    // فقط صاحب فایل
                logger.info("محدودیت‌های دسترسی فایل کلید تنظیم شد");
            }
            
            // محدود کردن دسترسی به پایگاه داده رمزگذاری شده
            File encryptedFile = new File(ENCRYPTED_DB_FILE);
            if (encryptedFile.exists()) {
                encryptedFile.setReadable(false, false);
                encryptedFile.setReadable(true, true);
                encryptedFile.setWritable(false, false);
                encryptedFile.setWritable(true, true);
                logger.info("محدودیت‌های دسترسی پایگاه داده تنظیم شد");
            }
            
        } catch (Exception e) {
            logger.error("خطا در تنظیم محدودیت‌های دسترسی: {}", e.getMessage());
        }
    }
    
    /**
     * main method برای تست
     */
    public static void main(String[] args) {
        logger.info("=== ابزار رمزگذاری پایگاه داده ===");
        
        if (args.length > 0 && "encrypt".equals(args[0])) {
            boolean success = encryptDatabase();
            if (success) {
                setFilePermissions();
                logger.info("رمزگذاری با موفقیت انجام شد");
                System.exit(0);
            } else {
                logger.error("رمزگذاری ناموفق بود");
                System.exit(1);
            }
        } else if (args.length > 0 && "decrypt".equals(args[0])) {
            boolean success = decryptDatabase();
            System.exit(success ? 0 : 1);
        } else if (args.length > 0 && "status".equals(args[0])) {
            boolean encrypted = isDatabaseEncrypted();
            System.out.println("Database Encrypted: " + encrypted);
            System.exit(encrypted ? 0 : 1);
        } else {
            System.out.println("استفاده: java DatabaseEncryption [encrypt|decrypt|status]");
            System.exit(1);
        }
    }
} 