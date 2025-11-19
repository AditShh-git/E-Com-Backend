package com.one.aim.service.impl;

import com.itextpdf.html2pdf.HtmlConverter;
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
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

//    private final InvoiceGenerator invoiceGenerator;
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
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        // ------------------------------------------------------------
        // LOAD CART ITEMS (only enabled = true)   FIXED
        // ------------------------------------------------------------
        List<CartBO> cartItems = cartRepo.findAllByUserAddToCart_IdAndEnabled(userId, true);

        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseUtils.failure("CART_EMPTY", "Your cart is empty.");
        }

        long totalAmount = 0L;

        // ------------------------------------------------------------
        // VALIDATE STOCK + UPDATE STOCK + CALCULATE TOTAL
        // ------------------------------------------------------------
        for (CartBO cart : cartItems) {

            ProductBO product = cart.getProduct();
            if (product == null) {
                throw new RuntimeException("Product missing for cart id: " + cart.getId());
            }

            int available = product.getStock() == null ? 0 : product.getStock();
            int qty = cart.getQuantity() <= 0 ? 1 : cart.getQuantity();

            if (available < qty) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }

            // update stock
            product.setStock(available - qty);
            product.updateLowStock();
            productRepo.save(product);

            // line total
            // always use latest product price (not old cart snapshot)
            long productPrice = product.getPrice() == null ? 0L : product.getPrice().longValue();

            // update snapshot so invoice + UI show correct price
            cart.setPrice(productPrice);
            cartRepo.save(cart);

            // calculate correct total
            totalAmount += productPrice * qty;

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
        // CREATE ORDER
        // ------------------------------------------------------------
        UserBO user = userRepo.findById(userId).orElseThrow();

        OrderBO order = new OrderBO();
        order.setUser(user);
        order.setOrderStatus("INITIAL");
        order.setOrderTime(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(address);
        order.setCartItems(cartItems);

        // Normalize payment method
        String pm = rq.getPaymentMethod() == null ? "" : rq.getPaymentMethod().trim().toUpperCase();
        order.setPaymentMethod(pm);

        // ------------------------------------------------------------
        // PAYMENT LOGIC (COD vs ONLINE)
        // ------------------------------------------------------------
        if ("COD".equals(pm)) {
            order.setPaymentStatus("COD_PENDING");
        } else {
            order.setPaymentStatus("CREATED");
        }

        // ------------------------------------------------------------
        // INVOICE NO
        // ------------------------------------------------------------
        String invoiceNo = "AIM" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        order.setInvoiceno(invoiceNo);

        // Save order to DB
        orderRepo.save(order);

        // ------------------------------------------------------------
        // DISABLE CART ITEMS
        // ------------------------------------------------------------
        for (CartBO cart : cartItems) {
            cart.setEnabled(false);
        }
        cartRepo.saveAll(cartItems);

        // ------------------------------------------------------------
        // GENERATE AND STORE INVOICE PDF
        // ------------------------------------------------------------
        String html = invoiceService.downloadInvoiceHtml(order.getOrderId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);
        byte[] pdfBytes = out.toByteArray();

        FileBO pdfFile = fileService.uploadBytes(pdfBytes, invoiceNo + ".pdf");

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
//        data.put("orderId", order.getId());
        data.put("orderId", order.getOrderId());
        data.put("invoiceNo", invoiceNo);
        data.put("totalAmount", totalAmount);
        data.put("paymentMethod", pm);
        data.put("paymentStatus", order.getPaymentStatus());

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
        Long userId = AuthUtils.getLoggedUserId();

        List<OrderBO> orders = orderRepo.findAllByUserIdOrderByOrderTimeDesc(userId);

        return ResponseUtils.success(
                OrderMapper.mapToOrderRsList(orders, fileService)
        );
    }


    @Override
    public BaseRs retrieveAllOrders() throws Exception {
        return ResponseUtils.success("Not implemented yet");
    }

    @Override
    @Transactional
    public BaseRs cancelOrder(String orderId) throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        // Load order by business orderId: ORD-XXXXXX
        OrderBO order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Only owner can cancel
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            return ResponseUtils.failure("NOT_ALLOWED", "You cannot cancel this order");
        }

        // Optional: block cancel if already processed
        String status = order.getOrderStatus() == null ? "" : order.getOrderStatus().toUpperCase();
        if (!status.equals("INITIAL")) {
            // you can relax this if you want
            return ResponseUtils.failure("CANNOT_CANCEL", "Order already processed");
        }

        // ----------------------------------------------------
        // 1. Restore stock + re-enable cart items
        // ----------------------------------------------------
        List<CartBO> carts = order.getCartItems();
        if (carts != null && !carts.isEmpty()) {
            for (CartBO cart : carts) {

                // put item back into user's active cart
                cart.setEnabled(true);

                // restore stock
                ProductBO product = cart.getProduct();
                if (product != null) {
                    int current = product.getStock() == null ? 0 : product.getStock();
                    int qty = cart.getQuantity() <= 0 ? 1 : cart.getQuantity();
                    product.setStock(current + qty);
                    product.updateLowStock();
                    productRepo.save(product);
                }
            }
            cartRepo.saveAll(carts);
        }

        // ----------------------------------------------------
        // 2. Mark order as CANCELLED
        // ----------------------------------------------------
        order.setOrderStatus("CANCELLED");
        order.setPaymentStatus("CANCELLED");
        orderRepo.save(order);

        // ----------------------------------------------------
        // 3. Response
        // ----------------------------------------------------
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getOrderId());
        data.put("orderStatus", order.getOrderStatus());
        data.put("paymentStatus", order.getPaymentStatus());

        return ResponseUtils.success(data);
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
