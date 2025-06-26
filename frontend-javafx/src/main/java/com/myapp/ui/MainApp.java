package com.myapp.ui;

// وارد کردن کلاس Navigation برای مدیریت صفحات
import com.myapp.ui.common.NavigationController;

// وارد کردن کلاس‌های اصلی JavaFX
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * کلاس اصلی برنامه JavaFX سیستم سفارش غذا
 * نقطه شروع اصلی برای برنامه frontend
 * این کلاس مسئول راه‌اندازی رابط کاربری گرافیکی است
 */
public class MainApp extends Application {

    /**
     * متد start که نقطه شروع اصلی JavaFX application است
     * این متد توسط JavaFX runtime فراخوانی می‌شود
     * 
     * @param primaryStage صفحه اصلی برنامه که تمام UI در آن نمایش داده می‌شود
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // راه‌اندازی Navigation Controller برای مدیریت انتقال بین صفحات
            NavigationController navigationController = NavigationController.getInstance();
            
            // مقداردهی اولیه navigation controller با stage اصلی
            navigationController.initialize(primaryStage);
            
            // بررسی وضعیت احراز هویت کاربر و هدایت به صفحه مناسب
            // اگر کاربر وارد شده باشد به داشبورد، در غیر اینصورت به صفحه ورود هدایت می‌شود
            navigationController.checkAuthenticationAndRedirect();
            
        } catch (Exception e) {
            // در صورت بروز خطا در راه‌اندازی، نمایش خطا و خروج از برنامه
            e.printStackTrace();
            System.exit(1); // خروج با کد خطا
        }
    }

    /**
     * متد stop که هنگام بسته شدن برنامه فراخوانی می‌شود
     * برای پاکسازی منابع و ذخیره تنظیمات استفاده می‌شود
     */
    @Override
    public void stop() {
        // پاکسازی منابع هنگام بسته شدن برنامه
        System.out.println("Food Ordering Application is closing...");
        
        // می‌توان در اینجا عملیات پاکسازی اضافی انجام داد:
        // - بستن اتصالات شبکه
        // - ذخیره تنظیمات کاربر
        // - پاکسازی cache ها
    }

    /**
     * متد main که نقطه شروع اصلی برنامه Java است
     * این متد JavaFX application را راه‌اندازی می‌کند
     * 
     * @param args آرگومان‌های خط فرمان (معمولاً استفاده نمی‌شود)
     */
    public static void main(String[] args) {
        // راه‌اندازی JavaFX application
        // این متد start() را فراخوانی می‌کند
        launch(args);
    }
}
