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
    private final UserActivityService  userActivityService;
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

        // ------------------------------------------------------------
        // LOAD CART
        // ------------------------------------------------------------
        List<CartBO> cartItems = cartRepo.findAllByUserAddToCart_IdAndEnabled(userId, true);

        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseUtils.failure("CART_EMPTY", "Your cart is empty.");
        }

        long totalAmount = 0L;

        // ------------------------------------------------------------
        // VALIDATE + UPDATE STOCK
        // ------------------------------------------------------------
        for (CartBO cart : cartItems) {

            ProductBO product = cart.getProduct();
            if (product == null) throw new RuntimeException("Missing product in cart");

            int available = product.getStock() == null ? 0 : product.getStock();
            int qty = Math.max(cart.getQuantity(), 1);

            if (available < qty)
                throw new RuntimeException("Insufficient stock: " + product.getName());

            // update stock
            product.setStock(available - qty);
            product.updateLowStock();
            productRepo.save(product);

            long productPrice =
                    product.getPrice() == null ? 0L : product.getPrice().longValue();


            cart.setPrice(productPrice);
            cartRepo.save(cart);

            totalAmount += productPrice * qty;
        }

        // ------------------------------------------------------------
        // SELECT SHIPPING ADDRESS
        // ------------------------------------------------------------
        AddressBO shippingAddress;

        // CASE 1 → User selected an existing saved address
        if (rq.getAddressId() != null) {

            shippingAddress = addressRepo.findById(rq.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Invalid addressId"));

            // Ensure this address belongs to logged-in user
            if (!shippingAddress.getUserid().equals(userId)) {
                throw new RuntimeException("Address does not belong to logged user");
            }

        }
        // CASE 2 → User typed a NEW address in checkout
        else if (rq.getFullName() != null) {

            shippingAddress = new AddressBO();
            shippingAddress.setFullName(rq.getFullName());
            shippingAddress.setStreet(rq.getStreet());
            shippingAddress.setCity(rq.getCity());
            shippingAddress.setState(rq.getState());
            shippingAddress.setZip(rq.getZip());
            shippingAddress.setCountry(rq.getCountry());
            shippingAddress.setPhone(rq.getPhone());
            shippingAddress.setUserid(userId);
            shippingAddress.setIsDefault(false);
            addressRepo.save(shippingAddress);

        }
        // CASE 3 → No address provided → use DEFAULT
        else {

            shippingAddress = addressRepo
                    .findFirstByUseridAndIsDefault(userId, true)
                    .orElseThrow(() -> new RuntimeException("No default address found"));

        }

        // ------------------------------------------------------------
        // CREATE ORDER
        // ------------------------------------------------------------
        UserBO user = userRepo.findById(userId).orElseThrow();

        OrderBO order = new OrderBO();
        order.setUser(user);
        order.setOrderStatus("INITIAL");
        order.setOrderTime(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setCartItems(cartItems);

        String pm = rq.getPaymentMethod() == null ? "" : rq.getPaymentMethod().toUpperCase();
        order.setPaymentMethod(pm);

        // COD vs ONLINE payment status
        order.setPaymentStatus("COD".equals(pm) ? "COD_PENDING" : "CREATED");

        // Invoice number
        String invoiceNo = "AIM" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
        );
        order.setInvoiceno(invoiceNo);

        orderRepo.save(order);

        // ------------------------------------------------------------
        // CREATE ORDER ITEMS
        // ------------------------------------------------------------
        List<OrderItemBO> orderItemList = new ArrayList<>();

        for (CartBO cart : cartItems) {

            ProductBO product = cart.getProduct();

            OrderItemBO item = OrderItemBO.builder()
                    .order(order)
                    .product(product)
                    .sellerId(product.getSeller().getId())
                    .productName(product.getName())
                    .productCategory(product.getCategoryName())
                    .unitPrice(cart.getPrice())
                    .quantity(cart.getQuantity())
                    .totalPrice(cart.getPrice() * cart.getQuantity())
                    .build();

            orderItemList.add(item);
        }

        order.setOrderItems(orderItemList);
        orderRepo.save(order);

        // ------------------------------------------------------------
        // DISABLE CART ITEMS
        // ------------------------------------------------------------
        cartItems.forEach(c -> c.setEnabled(false));
        cartRepo.saveAll(cartItems);

        // ------------------------------------------------------------
        // GENERATE PDF INVOICE
        // ------------------------------------------------------------
        String html = invoiceService.downloadInvoiceHtml(order.getOrderId());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);

        FileBO pdfFile = fileService.uploadBytes(out.toByteArray(), invoiceNo + ".pdf");

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
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getOrderId());
        response.put("invoiceNo", invoiceNo);
        response.put("totalAmount", totalAmount);
        response.put("paymentMethod", pm);
        response.put("paymentStatus", order.getPaymentStatus());

        return ResponseUtils.success(response);
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

        // Activity Log
        userActivityService.log(
                userId,
                "VIEW_ORDERS",
                "Viewed order history"
        );

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

        // Load order
        OrderBO order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Only owner can cancel
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            return ResponseUtils.failure("NOT_ALLOWED", "You cannot cancel this order");
        }

        // Block cancellation after processing
        String status = order.getOrderStatus() == null ? "" : order.getOrderStatus().toUpperCase();
        if (!status.equals("INITIAL")) {
            return ResponseUtils.failure("CANNOT_CANCEL", "Order already processed");
        }

        // ------------------------------------------------------------
        // 1. RESTORE STOCK + ENABLE CART ITEMS
        // ------------------------------------------------------------
        List<CartBO> carts = order.getCartItems();
        if (carts != null && !carts.isEmpty()) {
            for (CartBO cart : carts) {

                cart.setEnabled(true);

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

        // ------------------------------------------------------------
        // 2. MARK ORDER AS CANCELLED
        // ------------------------------------------------------------
        order.setOrderStatus("CANCELLED");
        order.setPaymentStatus("CANCELLED");
        orderRepo.save(order);

        // ------------------------------------------------------------
        // USER ACTIVITY LOG — ORDER CANCELLED
        // ------------------------------------------------------------
        userActivityService.log(
                userId,
                "ORDER_CANCELLED",
                "Cancelled order ID: " + order.getOrderId()
        );

        // ------------------------------------------------------------
        // 3. RESPONSE
        // ------------------------------------------------------------
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
