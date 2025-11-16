package com.one.aim.service.impl;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.one.aim.bo.*;
import com.one.aim.controller.OrderNotificationController;
import com.one.aim.mapper.OrderMapper;
import com.one.aim.repo.*;
import com.one.aim.rq.OrderRq;
import com.one.aim.rs.data.OrderDataRsList;
import com.one.aim.service.FileService;
import com.one.aim.service.InvoiceService;
import com.one.aim.service.OrderService;
import com.one.utils.AuthUtils;
import com.one.utils.InvoiceGenerator;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final InvoiceGenerator invoiceGenerator;
    private final OrderRepo orderRepo;
    private final CartRepo cartRepo;
    private final UserRepo userRepo;
    private final AddressRepo addressRepo;
    private final OrderNotificationController orderNotificationController;
    private final InvoiceService invoiceService;
    private final InvoiceRepo invoiceRepo;
    private final FileService fileService;
    private final SellerRepo sellerRepo;
    private final ProductRepo productRepo;

    public BaseRs getOrders() {
        List<OrderBO> list = orderRepo.findAll();
        return ResponseUtils.success(
                new OrderDataRsList("Orders loaded",
                        OrderMapper.mapToOrderRsList(list, fileService))
        );
    }

    @Override
    @Transactional
    public BaseRs placeOrder(OrderRq rq) throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();
        if (userId == null) throw new RuntimeException("User not authenticated");

        if (rq.getTotalCarts() == null || rq.getTotalCarts().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<CartBO> cartItems = new ArrayList<>();
        long totalAmount = 0;

        // ------------------------------------------------------------
        // BUILD CART LIST + VALIDATE STOCK
        // ------------------------------------------------------------
        for (Map.Entry<String, Integer> entry : rq.getTotalCarts().entrySet()) {

            Long cartId = Long.valueOf(entry.getKey());
            int qtyRequested = entry.getValue();

            CartBO cartBO = cartRepo.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Invalid cart ID: " + cartId));

            ProductBO product = cartBO.getProduct();
            if (product == null) throw new RuntimeException("Product missing for cart ID: " + cartId);

            int available = product.getStock() == null ? 0 : product.getStock();
            int qtyToUse = qtyRequested > 0 ? qtyRequested : cartBO.getQuantity();

            if (available < qtyToUse) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }

            // Reduce stock
            product.setStock(available - qtyToUse);
            product.updateLowStock();
            productRepo.save(product);

            // Freeze quantity for invoice
            cartBO.setQuantity(qtyToUse);

            totalAmount += cartBO.getPrice() * qtyToUse;
            cartItems.add(cartBO);
        }

        // ------------------------------------------------------------
        // SAVE SHIPPING ADDRESS
        // ------------------------------------------------------------
        AddressBO address = new AddressBO();
        address.setFullName(rq.getFullName());
        address.setStreet(rq.getStreet());
        address.setCity(rq.getCity());
        address.setState(rq.getState());
        address.setZip(rq.getZip());
        address.setCountry(rq.getCountry());
        address.setPhone(rq.getPhone());
        address.setUserid(userId);
        addressRepo.save(address);

        // ------------------------------------------------------------
        // DISABLE ORDERED CART ITEMS
        // ------------------------------------------------------------
        for (CartBO item : cartItems) item.setEnabled(false);
        cartRepo.saveAll(cartItems);

        // Remove from user's Add-To-Cart
        UserBO user = userRepo.findById(userId).orElseThrow();
        user.getAddtoCart().removeIf(c -> cartItems.stream().anyMatch(o -> o.getId().equals(c.getId())));
        userRepo.save(user);

        // ------------------------------------------------------------
        // CREATE ORDER
        // ------------------------------------------------------------
        OrderBO order = new OrderBO();
        order.setUser(user);
        order.setOrderStatus("INITIAL");
        order.setPaymentMethod(
                rq.getPaymentMethod() == null ? "UNKNOWN" : rq.getPaymentMethod().toUpperCase()
        );
        order.setOrderTime(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(address);
        order.setCartItems(cartItems);

        // Seller for order (first product seller)
        SellerBO seller = findSellerFromOrder(order);

        // Invoice number
        String invoiceNo = "AIM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        order.setInvoiceno(invoiceNo);

        orderRepo.save(order);

        // ------------------------------------------------------------
        // GENERATE AND STORE PDF
        // ------------------------------------------------------------
        String html = invoiceService.downloadInvoiceHtml(order.getId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);
        byte[] pdfBytes = out.toByteArray();

        FileBO pdfFile = fileService.uploadBytes(pdfBytes, invoiceNo + ".pdf");

        // ------------------------------------------------------------
        // SAVE INVOICE ENTRY
        // ------------------------------------------------------------
        InvoiceBO invoice = InvoiceBO.builder()
                .order(order)
                .user(order.getUser())
                .invoiceNumber(invoiceNo)
                .invoiceFileId(pdfFile.getId())
                .build();


        invoiceRepo.save(invoice);

        // ------------------------------------------------------------
        // RESPONSE
        // ------------------------------------------------------------
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("invoiceNo", invoiceNo);
        data.put("totalAmount", totalAmount);

        return ResponseUtils.success(data);
    }




    // ============================================================
    // OTHER METHODS (Empty for now — you will fill them later)
    // ============================================================

    @Override
    public BaseRs retrieveOrder(Long id) throws Exception {
        return ResponseUtils.success("Not implemented yet");
    }

    @Override
    public BaseRs retrieveOrders() throws Exception {
        return ResponseUtils.success("Not implemented yet");
    }

    @Override
    public void updateDeliveryStatus(String orderId, String status) {
        // no-op for now
    }

    @Override
    public BaseRs retrieveOrdersUser() throws Exception {
        return ResponseUtils.success("Not implemented yet");
    }

    @Override
    public BaseRs retrieveAllOrders() throws Exception {
        return ResponseUtils.success("Not implemented yet");
    }

    @Override
    public BaseRs retrieveOrdersCancel(String orderId) throws Exception {
        return ResponseUtils.success("Not implemented yet");
    }


    /**
     * Extract seller for this order.
     * We don’t store sellerId in CartBO anymore.
     * Seller now comes from ProductBO (product → seller).
     * So we pick the seller of the first cart item.
     * Works because one order = one seller in current design.
     */
    private SellerBO findSellerFromOrder(OrderBO order) {
        if (order.getCartItems() == null || order.getCartItems().isEmpty())
            return null;

        CartBO cart = order.getCartItems().get(0);
        ProductBO product = cart.getProduct();

        if (product == null || product.getSeller() == null)
            return null;

        return product.getSeller();
    }


}
