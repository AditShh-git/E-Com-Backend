package com.one.aim.service.impl;

import com.one.aim.bo.AddressBO;
import com.one.aim.bo.CartBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.bo.UserBO;
import com.one.aim.controller.OrderNotificationController;
import com.one.aim.repo.AddressRepo;
import com.one.aim.repo.CartRepo;
import com.one.aim.repo.OrderRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.rq.OrderRq;
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

    // ============================================================
// PLACE ORDER  (Main Logic)
// ============================================================
    @Override
    @Transactional
    public BaseRs placeOrder(OrderRq rq) throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        log.info("Placing order for userId: {}", userId);

        if (rq.getTotalCarts() == null || rq.getTotalCarts().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<CartBO> cartItems = new ArrayList<>();
        long totalAmount = 0;

        // ------------------------------------------------------------
        // Build the cart list + validate stock + update quantity
        // ------------------------------------------------------------
        for (Map.Entry<String, Integer> entry : rq.getTotalCarts().entrySet()) {

            Long cartId = Long.valueOf(entry.getKey());
            int qtyRequested = entry.getValue();

            CartBO cartBO = cartRepo.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Invalid cart ID: " + cartId));

            int qtyToUse = qtyRequested > 0 ? qtyRequested : cartBO.getQuantity();

            if (cartBO.getTotalitem() < qtyToUse) {
                throw new RuntimeException("Insufficient stock for product: " + cartBO.getPname());
            }

            // Reduce stock
            cartBO.setTotalitem(cartBO.getTotalitem() - qtyToUse);
            cartBO.setSolditem(cartBO.getSolditem() + qtyToUse);

            // Freeze quantity snapshot for invoice
            cartBO.setQuantity(qtyToUse);

            // Add to order
            totalAmount += cartBO.getPrice() * qtyToUse;
            cartItems.add(cartBO);
        }

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // ------------------------------------------------------------
        // Save shipping address
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
        // Disable ordered items
        // ------------------------------------------------------------
        for (CartBO item : cartItems) {
            item.setEnabled(false);
        }
        cartRepo.saveAll(cartItems);
        cartRepo.flush();

        // ------------------------------------------------------------
        // REMOVE ordered items from user's Add-To-Cart (corrected)
        // ------------------------------------------------------------
        UserBO user = userRepo.findById(userId).orElseThrow();

        user.getAddtoCart().removeIf(
                c -> cartItems.stream().anyMatch(o -> o.getId().equals(c.getId()))
        );

        userRepo.save(user);

        // ------------------------------------------------------------
        // Create OrderBO
        // ------------------------------------------------------------
        OrderBO order = new OrderBO();
        userRepo.findById(userId).ifPresent(order::setUser);

        order.setOrderStatus("INITIAL");
        order.setPaymentMethod(rq.getPaymentMethod() == null ? "UNKNOWN" : rq.getPaymentMethod().toUpperCase());
        order.setOrderTime(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(address);
        order.setCartItems(cartItems);

        // Vendor tracking
        List<Long> vendorIds = cartItems.stream()
                .map(CartBO::getCartempid)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        order.setCartempids(vendorIds);

        // Invoice number
        String invoiceNo = "AIM" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        order.setInvoiceno(invoiceNo);

        orderRepo.save(order);

        // ------------------------------------------------------------
        // GENERATE INVOICE PDF
        // ------------------------------------------------------------
        try {
            Path pdfPath = invoiceGenerator.generateInvoicePdf(order);
            log.info("PDF generated at: {}", pdfPath);
        } catch (Exception e) {
            log.warn("Invoice generation failed for order {}: {}", order.getId(), e.getMessage());
        }

        // ------------------------------------------------------------
        // Notify vendor(s)
        // ------------------------------------------------------------
        try {
            for (Long vId : vendorIds) {
                orderNotificationController.notifyVendor(
                        String.valueOf(vId),
                        "You received a new order: " + invoiceNo
                );
            }
        } catch (Exception e) {
            log.warn("Notification failed: {}", e.getMessage());
        }

        log.info("Order saved successfully with ID = {}", order.getId());

        // ------------------------------------------------------------
        // Response
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
}
