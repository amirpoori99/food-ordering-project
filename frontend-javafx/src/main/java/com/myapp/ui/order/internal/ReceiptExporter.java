package com.myapp.ui.order.internal;

import com.myapp.ui.order.OrderConfirmationController.OrderInfo;
import java.io.File;
import java.io.IOException;

/**
 * اینترفیس تولید رسید سفارش به فرمت دلخواه (PDF، تصویر، ...).
 * پیاده‌سازی پیش‌فرض فقط فایل متنی نمونه تولید می‌کند اما در فازهای آینده
 * می‌تواند با کتابخانه PDFBox یا iText جایگزین شود.
 */
public interface ReceiptExporter {
    /**
     * رسید را در مسیر داده شده صادر می‌کند.
     * @param order  اطلاعات سفارش
     * @param outputFile فایل خروجی (ممکن است از نوع PDF یا TXT باشد)
     * @throws IOException در صورت خطای نوشتن
     */
    void export(OrderInfo order, File outputFile) throws IOException;
} 