package com.one.aim.service.impl;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.itextpdf.html2pdf.HtmlConverter;
import com.one.aim.bo.*;
import com.one.aim.repo.InvoiceRepo;
import com.one.aim.service.FileService;
import com.one.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.one.aim.repo.OrderRepo;
import com.one.aim.service.InvoiceService;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepo invoiceRepo;
    private final OrderRepo orderRepo;
    private final FileService fileService;
    private final ResourceLoader resourceLoader;

    @Value("${invoice.template.path}")
    private String templatePath;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==============================================================  SAFE HELPERS
    private String safe(String s) {
        return s == null ? "" : s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private UserBO safeUser(UserBO u) { return u == null ? new UserBO() : u; }
    private AddressBO safeAddress(AddressBO a) { return a == null ? new AddressBO() : a; }

    // ==============================================================  CUSTOMER SECTIONS (FULL & MASKED)
    private String buildFullCustomerSection(UserBO user, AddressBO addr) {
        user = safeUser(user);
        addr = safeAddress(addr);

        return """
            <table class="details-table">
              <tr><td>
                <strong>Customer Details</strong><br>
                %s<br>
                %s, %s<br>
                %s - %s<br>
                %s<br>
                Phone: %s
              </td></tr>
            </table>
        """.formatted(
                safe(user.getFullName()),
                safe(addr.getStreet()), safe(addr.getCity()),
                safe(addr.getState()), safe(addr.getZip()),
                safe(addr.getCountry()),
                safe(addr.getPhone())
        );
    }

    private String buildMaskedCustomerSection(AddressBO addr) {
        addr = safeAddress(addr);

        return """
            <table class="details-table">
              <tr><td>
                <strong>Customer Details</strong><br>
                Customer<br>
                %s - %s<br>
                Phone: %s
              </td></tr>
            </table>
        """.formatted(
                safe(addr.getCity()),
                safe(addr.getZip()),
                safe(addr.getPhone())
        );
    }

    // ==============================================================  SELLER BLOCK (SAFE)
    private String buildMultiSellerSection(OrderBO order) {
        if (order == null || order.getOrderItems() == null)
            return "<p><strong>Seller Details:</strong> Not Available</p>";

        String role = AuthUtils.getLoggedUserRole();

        Map<Long, SellerBO> sellers = new LinkedHashMap<>();
        for (OrderItemBO item : order.getOrderItems()) {
            if (item != null && item.getProduct() != null) {
                SellerBO s = item.getProduct().getSeller();
                if (s != null) sellers.put(s.getId(), s);
            }
        }

        if (sellers.isEmpty())
            return "<p><strong>Seller Details:</strong> Not Available</p>";

        StringBuilder sb = new StringBuilder();
        sb.append("<table class='details-table'><tr><td>");
        sb.append("<strong>Seller Details</strong><br>");

        for (SellerBO s : sellers.values()) {
            String gst = safe(s.getGst());
            String email = safe(s.getEmail());
            String phone = safe(s.getPhoneNo());

            if ("ADMIN".equalsIgnoreCase(role)) {
                sb.append("<br>%s<br>GST: %s<br>Email: %s<br>Phone: %s<br>"
                        .formatted(safe(s.getFullName()), gst, email, phone));
            } else {
                sb.append("<br>%s<br>GST: %s<br>Email: %s<br>"
                        .formatted(safe(s.getFullName()), gst, email));
            }
        }

        sb.append("</td></tr></table>");
        return sb.toString();
    }

    // ==============================================================  USER/ADMIN FULL HTML
    @Override
    public String downloadInvoiceHtml(String orderId) throws Exception {

        OrderBO order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String html = loadTemplate();
        UserBO user = safeUser(order.getUser());
        AddressBO addr = safeAddress(order.getShippingAddress());

        long subTotal = 0;
        StringBuilder rows = new StringBuilder();
        int index = 1;

        for (OrderItemBO item : order.getOrderItems()) {
            long lineTotal = item.getTotalPrice();
            subTotal += lineTotal;

            rows.append("""
                <tr>
                  <td>%d</td>
                  <td>%s</td>
                  <td>%d</td>
                  <td>%d</td>
                  <td>%d</td>
                </tr>
            """.formatted(index++,
                    safe(item.getProductName()),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    lineTotal));
        }

        long tax = subTotal * 18 / 100;
        long total = subTotal + tax;

        return html
                .replace("@@invoiceNo@@", safe(order.getInvoiceno()))
                .replace("@@orderId@@", safe(order.getOrderId()))
                .replace("@@orderDate@@", order.getOrderTime() == null ?
                        "" : order.getOrderTime().format(DATE_FORMAT))
                .replace("@@customerSection@@", buildFullCustomerSection(user, addr))
                .replace("@@sellerSection@@", buildMultiSellerSection(order))
                .replace("@@orderItems@@", rows.toString())
                .replace("@@subTotal@@", String.valueOf(subTotal))
                .replace("@@taxPercent@@", "18")
                .replace("@@taxAmount@@", String.valueOf(tax))
                .replace("@@totalAmount@@", String.valueOf(total));
    }

    // ==============================================================  SELLER HTML (FILTERED)
    @Override
    public String downloadSellerInvoiceHtml(String orderId, Long sellerDbId) throws Exception {

        OrderBO order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItemBO> sellerItems = order.getOrderItems().stream()
                .filter(oi -> oi.getProduct() != null
                        && oi.getProduct().getSeller() != null
                        && oi.getProduct().getSeller().getId().equals(sellerDbId))
                .toList();

        if (sellerItems.isEmpty()) return null;

        String html = loadTemplate();
        AddressBO addr = safeAddress(order.getShippingAddress());

        long subTotal = 0;
        StringBuilder rows = new StringBuilder();
        int index = 1;

        for (OrderItemBO item : sellerItems) {
            long lineTotal = item.getTotalPrice();
            subTotal += lineTotal;

            rows.append("""
                <tr>
                  <td>%d</td>
                  <td>%s</td>
                  <td>%d</td>
                  <td>%d</td>
                  <td>%d</td>
                </tr>
            """.formatted(index++,
                    safe(item.getProductName()),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    lineTotal));
        }

        long tax = subTotal * 18 / 100;
        long total = subTotal + tax;

        return html
                .replace("@@invoiceNo@@", safe(order.getInvoiceno()))
                .replace("@@orderId@@", safe(order.getOrderId()))
                .replace("@@orderDate@@", order.getOrderTime() == null ?
                        "" : order.getOrderTime().format(DATE_FORMAT))
                .replace("@@customerSection@@", buildMaskedCustomerSection(addr))
                .replace("@@sellerSection@@", "") //  No other sellers shown here
                .replace("@@orderItems@@", rows.toString())
                .replace("@@subTotal@@", String.valueOf(subTotal))
                .replace("@@taxPercent@@", "18")
                .replace("@@taxAmount@@", String.valueOf(tax))
                .replace("@@totalAmount@@", String.valueOf(total));
    }

    // ==============================================================  USER/ADMIN MASTER PDF
    @Override
    public byte[] downloadInvoicePdf(String orderId) throws Exception {

        InvoiceBO invoice = invoiceRepo.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        return fileService.getContentFromGridFS(
                String.valueOf(invoice.getInvoiceFileId()));
    }

    // ==============================================================  SELLER LIVE PDF
    @Override
    public byte[] downloadSellerInvoicePdf(String orderId, Long sellerDbId) throws Exception {

        String html = downloadSellerInvoiceHtml(orderId, sellerDbId);
        if (html == null) return null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);
        return out.toByteArray();
    }

    // ==============================================================  USER → SAVE MASTER PDF
    @Override
    public InvoiceBO generateInvoice(String orderId) throws Exception {

        OrderBO order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (invoiceRepo.findByOrder_OrderId(orderId).isPresent()) {
            return invoiceRepo.findByOrder_OrderId(orderId).get();
        }

        String html = downloadInvoiceHtml(orderId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);

        FileBO file = fileService.uploadBytes(
                out.toByteArray(),
                order.getInvoiceno() + ".pdf"
        );

        InvoiceBO invoice = InvoiceBO.builder()
                .order(order)
                .user(order.getUser())
                .invoiceFileId(file.getId())
                .invoiceNumber(order.getInvoiceno())
                .build();

        return invoiceRepo.save(invoice);
    }

    // ==============================================================  GET SINGLE
    @Override
    public InvoiceBO getInvoiceByOrderId(String orderId) {
        return invoiceRepo.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    // ==============================================================  TEMPLATE LOADER
    private String loadTemplate() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:" + templatePath);
        return Files.readString(resource.getFile().toPath());
    }

    // ==============================================================  LISTING APIS
    @Override
    public List<InvoiceBO> getAllInvoicesForAdmin() {
        return invoiceRepo.findAll();
    }

    @Override
    public List<InvoiceBO> getInvoicesForUser(Long userId) {
        return invoiceRepo.findByUser_Id(userId);
    }

    @Override
    public List<InvoiceBO> getInvoicesForSeller(Long sellerDbId) {
        return invoiceRepo.findInvoicesForSeller(sellerDbId);
    }

    @Override
    public byte[] downloadAdminInvoice(String orderId) throws Exception {

        String html = downloadInvoiceHtml(orderId); // same base template
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);
        return out.toByteArray();
    }

}
