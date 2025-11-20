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

    // ==============================================================
    // SAFE STRING HELPERS
    // ==============================================================
    private String safe(String s) {
        return s == null ? "" : s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private UserBO safeUser(UserBO user) {
        return user == null ? new UserBO() : user;
    }

    private AddressBO safeAddress(AddressBO addr) {
        return addr == null ? new AddressBO() : addr;
    }

    // ==============================================================
    // CUSTOMER DETAILS (FULL)
    // ==============================================================
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

    // ==============================================================
    // CUSTOMER DETAILS (MASKED FOR SELLER)
    // ==============================================================
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


    // ==============================================================
// MULTI SELLER DETAILS BLOCK (Phone Completely Removed)
// ==============================================================
    private String buildMultiSellerSection(OrderBO order) {

        if (order == null || order.getCartItems() == null)
            return "<p><strong>Seller Details:</strong> Not Available</p>";

        String role = AuthUtils.getLoggedUserRole(); // USER / SELLER / ADMIN

        Map<Long, SellerBO> sellers = new LinkedHashMap<>();

        for (CartBO item : order.getCartItems()) {
            if (item != null &&
                    item.getProduct() != null &&
                    item.getProduct().getSeller() != null) {

                sellers.put(item.getProduct().getSeller().getId(),
                        item.getProduct().getSeller());
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

            // ===============================
            // ADMIN → show phone number
            // ===============================
            if ("ADMIN".equalsIgnoreCase(role)) {
                sb.append("""
            <br>
            %s<br>
            GST: %s<br>
            Email: %s<br>
            Phone: %s<br>
            """.formatted(
                        safe(s.getFullName()),
                        gst,
                        email,
                        phone
                ));
                continue;
            }

            // ===============================
            // USER / SELLER → no phone number
            // ===============================
            sb.append("""
        <br>
        %s<br>
        GST: %s<br>
        Email: %s<br>
        """.formatted(
                    safe(s.getFullName()),
                    gst,
                    email
            ));
        }

        sb.append("</td></tr></table>");
        return sb.toString();
    }



    // ==============================================================
    // FULL INVOICE HTML (USER/ADMIN)
    // ==============================================================
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

        for (CartBO c : order.getCartItems()) {

            long lineTotal = c.getPrice() * c.getQuantity();
            subTotal += lineTotal;

            rows.append("""
                <tr>
                  <td>%d</td>
                  <td>%s</td>
                  <td>%d</td>
                  <td>%d</td>
                  <td>%d</td>
                </tr>
            """.formatted(
                    index++,
                    safe(c.getPname()),
                    c.getPrice(),
                    c.getQuantity(),
                    lineTotal
            ));
        }

        long tax = subTotal * 18 / 100;
        long total = subTotal + tax;

        html = html.replace("@@invoiceNo@@", safe(order.getInvoiceno()))
                .replace("@@orderId@@", safe(order.getOrderId()))
                .replace("@@orderDate@@",
                        order.getOrderTime() == null ? "" :
                                order.getOrderTime().format(DATE_FORMAT))
                .replace("@@customerSection@@", buildFullCustomerSection(user, addr))
                .replace("@@sellerSection@@", buildMultiSellerSection(order))
                .replace("@@orderItems@@", rows.toString())
                .replace("@@subTotal@@", String.valueOf(subTotal))
                .replace("@@taxPercent@@", "18")
                .replace("@@taxAmount@@", String.valueOf(tax))
                .replace("@@totalAmount@@", String.valueOf(total));

        return html;
    }

    // ==============================================================
    // SELLER INVOICE (HTML)
    // ==============================================================
    @Override
    public String downloadSellerInvoiceHtml(String orderId, String sellerId) throws Exception {

        OrderBO order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<CartBO> sellerItems = order.getCartItems().stream()
                .filter(c -> c.getProduct() != null &&
                        c.getProduct().getSeller() != null &&
                        sellerId.equals(c.getProduct().getSeller().getSellerId()))
                .toList();

        if (sellerItems.isEmpty()) {
            throw new RuntimeException("Seller has no items in this order.");
        }

        String html = loadTemplate();
        AddressBO addr = safeAddress(order.getShippingAddress());

        long subTotal = 0;
        StringBuilder rows = new StringBuilder();
        int index = 1;

        for (CartBO c : sellerItems) {
            long lineTotal = c.getPrice() * c.getQuantity();
            subTotal += lineTotal;

            rows.append("""
                <tr>
                  <td>%d</td>
                  <td>%s</td>
                  <td>%d</td>
                  <td>%d</td>
                  <td>%d</td>
                </tr>
            """.formatted(
                    index++,
                    safe(c.getPname()),
                    c.getPrice(),
                    c.getQuantity(),
                    lineTotal
            ));
        }

        long tax = subTotal * 18 / 100;
        long total = subTotal + tax;

        html = html.replace("@@invoiceNo@@", safe(order.getInvoiceno()))
                .replace("@@orderId@@", safe(order.getOrderId()))
                .replace("@@orderDate@@",
                        order.getOrderTime() == null ? "" :
                                order.getOrderTime().format(DATE_FORMAT))
                .replace("@@customerSection@@", buildMaskedCustomerSection(addr))
                .replace("@@sellerSection@@", "") // seller does NOT see other sellers
                .replace("@@orderItems@@", rows.toString())
                .replace("@@subTotal@@", String.valueOf(subTotal))
                .replace("@@taxPercent@@", "18")
                .replace("@@taxAmount@@", String.valueOf(tax))
                .replace("@@totalAmount@@", String.valueOf(total));

        return html;
    }

    // ==============================================================
    // USER/ADMIN PDF
    // ==============================================================
    @Override
    public byte[] downloadInvoicePdf(String orderId) throws Exception {

        InvoiceBO invoice = invoiceRepo.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        return fileService.getContentFromGridFS(
                String.valueOf(invoice.getInvoiceFileId())
        );
    }

    // ==============================================================
    // SELLER PDF
    // ==============================================================
    @Override
    public byte[] downloadSellerInvoicePdf(String orderId, String sellerId) throws Exception {

        String html = downloadSellerInvoiceHtml(orderId, sellerId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);

        return out.toByteArray();
    }

    // ==============================================================
    // GENERATE INVOICE (USER)
    // ==============================================================
    @Override
    public InvoiceBO generateInvoice(String orderId) throws Exception {

        OrderBO order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Optional<InvoiceBO> existing = invoiceRepo.findByOrder_OrderId(orderId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String html = downloadInvoiceHtml(orderId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);

        FileBO f = fileService.uploadBytes(out.toByteArray(),
                order.getInvoiceno() + ".pdf");

        InvoiceBO invoice = InvoiceBO.builder()
                .order(order)
                .user(order.getUser())
                .invoiceFileId(f.getId())
                .invoiceNumber(order.getInvoiceno())
                .build();

        return invoiceRepo.save(invoice);
    }

    // ==============================================================
    // GET INVOICE BY PUBLIC ORDER CODE
    // ==============================================================
    @Override
    public InvoiceBO getInvoiceByOrderId(String orderId) {
        return invoiceRepo.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    // ==============================================================
    // LOAD TEMPLATE
    // ==============================================================
    private String loadTemplate() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:" + templatePath);
        return Files.readString(resource.getFile().toPath());
    }

    // ==============================================================
    // LIST APIs
    // ==============================================================
    @Override
    public List<InvoiceBO> getAllInvoicesForAdmin() {
        return invoiceRepo.findAll();
    }

    @Override
    public List<InvoiceBO> getInvoicesForUser(Long userId) {

        if (userId == null) return List.of();

        return invoiceRepo.findAll().stream()
                .filter(inv ->
                        inv.getUser() != null &&
                                inv.getUser().getId() != null &&
                                inv.getUser().getId().equals(userId)
                )
                .toList();
    }


    @Override
    public List<InvoiceBO> getInvoicesForSeller(String sellerId) {

        if (sellerId == null || sellerId.isBlank()) return List.of();

        return invoiceRepo.findAll().stream()
                .filter(inv ->
                        inv.getOrder() != null &&
                                inv.getOrder().getCartItems() != null &&
                                inv.getOrder().getCartItems().stream().anyMatch(ci ->
                                        ci != null &&
                                                ci.getProduct() != null &&
                                                ci.getProduct().getSeller() != null &&
                                                sellerId.equals(ci.getProduct().getSeller().getSellerId())
                                )
                )
                .toList();
    }

}
