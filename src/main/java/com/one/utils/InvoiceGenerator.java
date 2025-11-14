package com.one.utils;


import com.one.aim.bo.CartBO;
import com.one.aim.bo.OrderBO;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceGenerator {

    private final ResourceLoader resourceLoader;
    private final Path baseDir = Paths.get(
            System.getProperty("user.dir"),
            "uploads", "downloads", "invoices"
    );

    // ================================================
// PUBLIC METHOD — Generate PDF for Order
// ================================================
    public Path generateInvoicePdf(OrderBO order) {

        try {
            Files.createDirectories(baseDir);

            String invoiceNo = order.getInvoiceno();
            if (invoiceNo == null || invoiceNo.trim().isEmpty()) {
                throw new RuntimeException("Order has no invoice number");
            }

            String html = buildInvoiceHtml(order);

            Path pdfPath = baseDir.resolve(invoiceNo + ".pdf");

            try (OutputStream os = Files.newOutputStream(pdfPath)) {

                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();

                //  FIXED — this line was breaking PDF generation
                builder.withHtmlContent(html, null);

                builder.toStream(os);
                builder.run();
            }

            log.info("Invoice PDF generated: {}", pdfPath.toAbsolutePath());
            return pdfPath;

        } catch (Exception e) {
            log.error("Error while generating invoice PDF", e);
            throw new RuntimeException("Invoice generation failed: " + e.getMessage(), e);
        }
    }

    // ================================================
// BUILD HTML
// ================================================
    private String buildInvoiceHtml(OrderBO order) throws IOException {


        String template = loadTemplate("templates/invoice_template.html");

        String orderItemsHtml = buildProductRows(order);

        Map<String, String> tokens = new HashMap<>();

        tokens.put("invoiceNo", order.getInvoiceno());
        tokens.put("orderId", String.valueOf(order.getId()));

        tokens.put("orderDate",
                order.getOrderTime() == null ? "" :
                        order.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );

        String customerName = order.getUser() != null
                ? order.getUser().getFullName()
                : "Customer";

        tokens.put("customerName", customerName);

        if (order.getShippingAddress() != null) {
            tokens.put("street", safe(order.getShippingAddress().getStreet()));
            tokens.put("city", safe(order.getShippingAddress().getCity()));
            tokens.put("state", safe(order.getShippingAddress().getState()));
            tokens.put("zip", safe(order.getShippingAddress().getZip()));
            tokens.put("country", safe(order.getShippingAddress().getCountry()));
            tokens.put("phone", safe(order.getShippingAddress().getPhone()));
        } else {
            tokens.put("street", "");
            tokens.put("city", "");
            tokens.put("state", "");
            tokens.put("zip", "");
            tokens.put("country", "");
            tokens.put("phone", "");
        }

        // Seller details — empty for now
        tokens.put("sellerName", "");
        tokens.put("sellerAddress", "");
        tokens.put("sellerPhone", "");
        tokens.put("sellerEmail", "");

        tokens.put("orderItems", orderItemsHtml);

        tokens.put("totalAmount", String.valueOf(order.getTotalAmount()));
        tokens.put("subTotal", String.valueOf(order.getTotalAmount()));
        tokens.put("taxPercent", "0");
        tokens.put("taxAmount", "0");

        System.out.println("TEMPLATE LENGTH = " + template.length());


        return applyTokens(template, tokens);


    }

    // ================================================
// PRODUCT ROWS
// ================================================
    private String buildProductRows(OrderBO order) {
        StringBuilder sb = new StringBuilder();
        List<CartBO> items = order.getCartItems();
        if (items != null) {
            int index = 1;
            for (CartBO item : items) {
                int qty = item.getQuantity();
                long price = item.getPrice();
                long total = price * qty;

                sb.append("<tr>")
                        .append("<td>").append(index++).append("</td>")
                        .append("<td>").append(safe(item.getPname())).append("</td>")
                        .append("<td>").append(price).append("</td>")
                        .append("<td>").append(qty).append("</td>")
                        .append("<td>").append(total).append("</td>")
                        .append("</tr>");
            }
        }
        return sb.toString();
    }

    // ================================================
// Load template
// ================================================
    private String loadTemplate(String resourcePath) throws IOException {
        String fullPath = "classpath:" + resourcePath;
        log.info(" Trying to load template: {}", fullPath);

        Resource resource = resourceLoader.getResource(fullPath);

        if (!resource.exists()) {
            log.error(" TEMPLATE NOT FOUND: {}", fullPath);
            throw new FileNotFoundException("Template missing: " + fullPath);
        }

        try (InputStream is = resource.getInputStream()) {
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            log.info(" TEMPLATE LOADED SUCCESSFULLY ({} characters)", html.length());
            return html;
        }
    }

    private String applyTokens(String template, Map<String, String> tokens) {
        String out = template;
        for (var entry : tokens.entrySet()) {
            out = out.replace("{{" + entry.getKey() + "}}",
                    entry.getValue() == null ? "" : entry.getValue()
            );
        }
        return out;
    }

    private String safe(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

}
