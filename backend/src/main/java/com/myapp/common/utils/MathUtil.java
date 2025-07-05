package com.myapp.common.utils;

import java.util.List;

/**
 * کلاس کمکی برای عملیات ریاضی
 */
public class MathUtil {
    
    /**
     * محاسبه میانگین
     */
    public static double average(List<Double> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return 0.0;
        }
        return numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    /**
     * محاسبه میانگین آرایه
     */
    public static double average(double[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return 0.0;
        }
        double sum = 0;
        for (double num : numbers) {
            sum += num;
        }
        return sum / numbers.length;
    }
    
    /**
     * محاسبه مجموع
     */
    public static double sum(List<Double> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return 0.0;
        }
        return numbers.stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * محاسبه مجموع آرایه
     */
    public static double sum(double[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return 0.0;
        }
        double sum = 0;
        for (double num : numbers) {
            sum += num;
        }
        return sum;
    }
    
    /**
     * محاسبه درصد
     */
    public static double percentage(double part, double total) {
        if (total == 0) {
            return 0.0;
        }
        return (part / total) * 100;
    }
    
    /**
     * محاسبه درصد تغییر
     */
    public static double percentageChange(double oldValue, double newValue) {
        if (oldValue == 0) {
            return newValue > 0 ? 100.0 : 0.0;
        }
        return ((newValue - oldValue) / oldValue) * 100;
    }
    
    /**
     * گرد کردن به دو رقم اعشار
     */
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
    
    /**
     * گرد کردن به تعداد ارقام مشخص
     */
    public static double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
    
    /**
     * محاسبه حداقل
     */
    public static double min(List<Double> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return 0.0;
        }
        return numbers.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
    }
    
    /**
     * محاسبه حداکثر
     */
    public static double max(List<Double> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return 0.0;
        }
        return numbers.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }
    
    /**
     * محاسبه انحراف معیار
     */
    public static double standardDeviation(List<Double> numbers) {
        if (numbers == null || numbers.size() < 2) {
            return 0.0;
        }
        
        double mean = average(numbers);
        double variance = numbers.stream()
                .mapToDouble(num -> Math.pow(num - mean, 2))
                .average()
                .orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    /**
     * محاسبه ضریب تغییرات
     */
    public static double coefficientOfVariation(List<Double> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return 0.0;
        }
        
        double mean = average(numbers);
        if (mean == 0) {
            return 0.0;
        }
        
        return (standardDeviation(numbers) / mean) * 100;
    }
    
    /**
     * محاسبه میانه
     */
    public static double median(List<Double> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return 0.0;
        }
        
        List<Double> sorted = numbers.stream().sorted().toList();
        int size = sorted.size();
        
        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }
    
    /**
     * بررسی اینکه آیا عدد در محدوده است
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
    
    /**
     * محدود کردن عدد به بازه مشخص
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * محاسبه نرخ رشد سالانه
     */
    public static double annualGrowthRate(double initialValue, double finalValue, int years) {
        if (initialValue <= 0 || years <= 0) {
            return 0.0;
        }
        return (Math.pow(finalValue / initialValue, 1.0 / years) - 1) * 100;
    }
} 