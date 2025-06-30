package com.myapp.ui.order.internal;

import com.myapp.ui.order.OrderConfirmationController.OrderInfo;
import com.myapp.ui.order.OrderConfirmationController.OrderItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * پیاده‌سازی صادرات رسید به صورت فایل متنی
 * 
 * این کلاس رسید سفارش را به صورت فایل TXT تولید می‌کند
 * در فازهای آینده می‌تواند با PDFExporter جایگزین شود
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since فاز 26 - Order Confirmation UI
 */
public class TextReceiptExporter implements ReceiptExporter {
    
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("fa", "IR"));
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    
    public TextReceiptExporter() {
        currencyFormat.setGroupingUsed(true);
    }

    @Override
    public void export(OrderInfo order, File outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(generateReceiptText(order));
        }
    }
    
    /**
     * تولید متن رسید سفارش
     * 
     * @param order اطلاعات سفارش
     * @return متن کامل رسید
     */
    private String generateReceiptText(OrderInfo order) {
        StringBuilder receipt = new StringBuilder();
        
        // هدر رسید
        receipt.append("═══════════════════════════════════════════════════\n");
        receipt.append("                    رسید سفارش                    \n");
        receipt.append("═══════════════════════════════════════════════════\n\n");
        
        // اطلاعات سفارش
        receipt.append("شماره سفارش: ").append(order.getOrderId()).append("\n");
        receipt.append("تاریخ و زمان: ").append(order.getOrderDateTime().format(dateTimeFormatter)).append("\n");
        receipt.append("زمان تحویل: ").append(order.getEstimatedDelivery().format(dateTimeFormatter)).append("\n");
        receipt.append("رستوران: ").append(order.getRestaurantName()).append("\n");
        receipt.append("تلفن رستوران: ").append(order.getRestaurantPhone()).append("\n\n");
        
        // آدرس تحویل
        receipt.append("───────────────────────────────────────────────────\n");
        receipt.append("آدرس تحویل:\n");
        receipt.append(order.getDeliveryAddress()).append("\n");
        receipt.append("تلفن: ").append(order.getCustomerPhone()).append("\n");
        if (order.getOrderNotes() != null && !order.getOrderNotes().trim().isEmpty()) {
            receipt.append("یادداشت: ").append(order.getOrderNotes()).append("\n");
        }
        receipt.append("\n");
        
        // آیتم‌های سفارش
        receipt.append("───────────────────────────────────────────────────\n");
        receipt.append("آیتم‌های سفارش:\n");
        receipt.append("───────────────────────────────────────────────────\n");
        
        for (OrderItem item : order.getOrderItems()) {
            receipt.append(String.format("%-30s × %d\n", item.getItemName(), item.getQuantity()));
            if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().trim().isEmpty()) {
                receipt.append(String.format("  نکته: %s\n", item.getSpecialInstructions()));
            }
            receipt.append(String.format("  قیمت واحد: %s تومان\n", formatCurrency(item.getUnitPrice())));
            receipt.append(String.format("  جمع: %s تومان\n\n", formatCurrency(item.getTotalPrice())));
        }
        
        // خلاصه مالی
        receipt.append("───────────────────────────────────────────────────\n");
        receipt.append("خلاصه مالی:\n");
        receipt.append("───────────────────────────────────────────────────\n");
        receipt.append(String.format("جمع کل:                    %s تومان\n", formatCurrency(order.getSubtotal())));
        receipt.append(String.format("مالیات:                     %s تومان\n", formatCurrency(order.getTax())));
        
        if (order.getDeliveryFee() > 0) {
            receipt.append(String.format("هزینه ارسال:                %s تومان\n", formatCurrency(order.getDeliveryFee())));
        } else {
            receipt.append("هزینه ارسال:                رایگان\n");
        }
        
        if (order.getDiscount() > 0) {
            receipt.append(String.format("تخفیف:                    - %s تومان\n", formatCurrency(order.getDiscount())));
        }
        
        receipt.append("───────────────────────────────────────────────────\n");
        receipt.append(String.format("مبلغ نهایی:                 %s تومان\n", formatCurrency(order.getTotalAmount())));
        receipt.append("───────────────────────────────────────────────────\n\n");
        
        // اطلاعات پرداخت
        receipt.append("اطلاعات پرداخت:\n");
        receipt.append("روش پرداخت: ").append(order.getPaymentMethod()).append("\n");
        receipt.append("وضعیت پرداخت: ").append(order.getPaymentStatus()).append("\n\n");
        
        // فوتر
        receipt.append("═══════════════════════════════════════════════════\n");
        receipt.append("              از خرید شما متشکریم!               \n");
        receipt.append("        سیستم سفارش غذای آنلاین               \n");
        receipt.append("═══════════════════════════════════════════════════\n");
        
        return receipt.toString();
    }
    
    /**
     * فرمت کردن ارز به فارسی
     * 
     * @param amount مبلغ
     * @return مبلغ فرمت شده
     */
    private String formatCurrency(double amount) {
        return currencyFormat.format(amount);
    }
} 