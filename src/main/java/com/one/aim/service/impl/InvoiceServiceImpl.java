package com.one.aim.service.impl;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import com.itextpdf.html2pdf.HtmlConverter;
import com.one.aim.bo.*;
import com.one.aim.repo.InvoiceRepo;
import com.one.aim.service.FileService;
import com.one.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
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

    // ---------------------------------------------------
    // USER invoice HTML (full customer details, no seller block)
    // ---------------------------------------------------
    @Override
    public String downloadInvoiceHtml(long orderId) throws Exception {

        Resource resource = resourceLoader.getResource("classpath:" + templatePath);
        String html = Files.readString(resource.getFile().toPath());

        OrderBO order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        UserBO user = order.getUser();
        AddressBO addr = order.getShippingAddress();

        StringBuilder rows = new StringBuilder();
        long subTotal = 0;
        int index = 1;

        for (CartBO cart : order.getCartItems()) {

            SellerBO seller = cart.getProduct().getSeller();  // per-product seller
            long lineTotal = cart.getPrice() * cart.getQuantity();
            subTotal += lineTotal;

            rows.append("<tr>")
                    .append("<td>").append(index++).append("</td>")
                    .append("<td>")
                    .append(cart.getPname())
                    .append("<br><small>Sold by (Seller ID): ").append(seller.getId()).append("</small>")
                    .append("</td>")
                    .append("<td>").append(cart.getPrice()).append("</td>")
                    .append("<td>").append(cart.getQuantity()).append("</td>")
                    .append("<td>").append(lineTotal).append("</td>")
                    .append("</tr>");
        }

        long tax = subTotal * 18 / 100;
        long total = subTotal + tax;

        html = html.replace("{{invoiceNo}}", order.getInvoiceno())
                .replace("{{orderId}}", String.valueOf(order.getId()))
                .replace("{{orderDate}}", order.getOrderTime().toString())
                .replace("{{customerName}}", user.getFullName())
                .replace("{{street}}", addr.getStreet())
                .replace("{{city}}", addr.getCity())
                .replace("{{state}}", addr.getState())
                .replace("{{zip}}", addr.getZip())
                .replace("{{country}}", addr.getCountry())
                .replace("{{phone}}", addr.getPhone())
                .replace("{{orderItems}}", rows.toString())
                .replace("{{subTotal}}", String.valueOf(subTotal))
                .replace("{{taxPercent}}", "18")
                .replace("{{taxAmount}}", String.valueOf(tax))
                .replace("{{totalAmount}}", String.valueOf(total));

        // Hide seller block for USER invoice (comment that <td> out)
        html = html.replace("{{sellerBlock}}", "<!--")
                .replace("{{/sellerBlock}}", "-->");

        return html;
    }

    // ---------------------------------------------------
    // SELLER invoice HTML (only their items, no customer details)
    // ---------------------------------------------------
    @Override
    public String downloadSellerInvoiceHtml(long orderId, long sellerId) throws Exception {

        Resource resource = resourceLoader.getResource("classpath:" + templatePath);
        String html = Files.readString(resource.getFile().toPath());

        OrderBO order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Filter items belonging to this seller
        List<CartBO> sellerItems = order.getCartItems().stream()
                .filter(ci -> ci.getProduct() != null
                        && ci.getProduct().getSeller() != null
                        && ci.getProduct().getSeller().getId().equals(sellerId))
                .toList();

        if (sellerItems.isEmpty()) {
            throw new RuntimeException("Not allowed: this order does not contain your products");
        }

        SellerBO seller = sellerItems.get(0).getProduct().getSeller();

        StringBuilder rows = new StringBuilder();
        long subTotal = 0;
        int index = 1;

        for (CartBO cart : sellerItems) {
            long lineTotal = cart.getPrice() * cart.getQuantity();
            subTotal += lineTotal;

            rows.append("<tr>")
                    .append("<td>").append(index++).append("</td>")
                    .append("<td>").append(cart.getPname()).append("</td>")
                    .append("<td>").append(cart.getPrice()).append("</td>")
                    .append("<td>").append(cart.getQuantity()).append("</td>")
                    .append("<td>").append(lineTotal).append("</td>")
                    .append("</tr>");
        }

        long tax = subTotal * 18 / 100;
        long total = subTotal + tax;

        // Customer details → HIDDEN (Option C)
        html = html.replace("{{invoiceNo}}", order.getInvoiceno())
                .replace("{{orderId}}", String.valueOf(order.getId()))
                .replace("{{orderDate}}", order.getOrderTime().toString())
                .replace("{{customerName}}", "Hidden (privacy policy)")
                .replace("{{street}}", "Hidden")
                .replace("{{city}}", "Hidden")
                .replace("{{state}}", "Hidden")
                .replace("{{zip}}", "Hidden")
                .replace("{{country}}", "Hidden")
                .replace("{{phone}}", "Hidden")

                // Seller details are shown for seller invoice
                .replace("{{sellerName}}", seller.getFullName())
                .replace("{{sellerAddress}}", "GST: " + seller.getGst())
                .replace("{{sellerPhone}}", seller.getPhoneNo())
                .replace("{{sellerEmail}}", seller.getEmail())

                .replace("{{orderItems}}", rows.toString())
                .replace("{{subTotal}}", String.valueOf(subTotal))
                .replace("{{taxPercent}}", "18")
                .replace("{{taxAmount}}", String.valueOf(tax))
                .replace("{{totalAmount}}", String.valueOf(total));

        // Enable seller block (remove markers)
        html = html.replace("{{sellerBlock}}", "")
                .replace("{{/sellerBlock}}", "");

        return html;
    }

    // ---------------------------------------------------
    // Stored PDF from DB (user/admin)
    // ---------------------------------------------------
    @Override
    public byte[] downloadInvoicePdf(Long orderId) throws Exception {

        InvoiceBO invoice = invoiceRepo.findByOrder_Id(orderId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        return fileService.getContentFromGridFS(String.valueOf(invoice.getInvoiceFileId()));
    }

    // ---------------------------------------------------
    // On-the-fly SELLER PDF
    // ---------------------------------------------------
    @Override
    public byte[] downloadSellerInvoicePdf(Long orderId, Long sellerId) throws Exception {

        String html = downloadSellerInvoiceHtml(orderId, sellerId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);
        return out.toByteArray();
    }

    // ---------------------------------------------------
    // Generate master invoice (user invoice stored in DB)
    // ---------------------------------------------------
    @Override
    public InvoiceBO generateInvoice(Long orderId) throws Exception {

        OrderBO order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Optional<InvoiceBO> existing = invoiceRepo.findByOrder_Id(orderId);
        if (existing.isPresent()) return existing.get();

        // Create USER invoice PDF
        String html = downloadInvoiceHtml(orderId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);

        FileBO file = fileService.uploadBytes(out.toByteArray(), order.getInvoiceno() + ".pdf");

        InvoiceBO invoice = InvoiceBO.builder()
                .order(order)
                .user(order.getUser())
                .invoiceNumber(order.getInvoiceno())
                .invoiceFileId(file.getId())
                .build();

        invoiceRepo.save(invoice);
        return invoice;
    }

    // ---------------------------------------------------
    // ADMIN invoices
    // ---------------------------------------------------
    @Override
    public List<InvoiceBO> getAllInvoicesForAdmin() {
        return invoiceRepo.findAll();
    }

    // ---------------------------------------------------
    // SELLER invoices — only orders containing this seller's items
    // ---------------------------------------------------
    @Override
    public List<InvoiceBO> getInvoicesForSeller(Long sellerId) {

        return invoiceRepo.findAll().stream()
                .filter(inv ->
                        inv.getOrder().getCartItems().stream()
                                .anyMatch(ci -> ci.getProduct() != null
                                        && ci.getProduct().getSeller() != null
                                        && ci.getProduct().getSeller().getId().equals(sellerId))
                )
                .toList();
    }

    // ---------------------------------------------------
    // USER invoices
    // ---------------------------------------------------
    @Override
    public List<InvoiceBO> getInvoicesForUser(Long userId) {
        return invoiceRepo.findAll().stream()
                .filter(inv -> inv.getUser().getId().equals(userId))
                .toList();
    }
}