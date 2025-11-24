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

    private final OrderRepo orderRepo;
    private final CartRepo cartRepo;
    private final UserRepo userRepo;
    private final AddressRepo addressRepo;
    private final OrderNotificationController orderNotificationController;
    private final InvoiceService invoiceService;
    private final InvoiceRepo invoiceRepo;
    private final FileService fileService;
    private final UserActivityService userActivityService;
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
        // LOAD CART ITEMS (only enabled=true)
        // ------------------------------------------------------------
        List<CartBO> cartItems = cartRepo.findAllByUserAddToCart_IdAndEnabled(userId, true);

        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseUtils.failure("CART_EMPTY", "Your cart is empty.");
        }

        long totalAmount = 0L;

        // ------------------------------------------------------------
        // VALIDATE STOCK + UPDATE STOCK
        // ------------------------------------------------------------
        for (CartBO cart : cartItems) {

            ProductBO product = cart.getProduct();
            if (product == null)
                throw new RuntimeException("Product missing in cart");

            int available = product.getStock() == null ? 0 : product.getStock();
            int qty = cart.getQuantity() <= 0 ? 1 : cart.getQuantity();

            if (available < qty)
                throw new RuntimeException("Insufficient stock for: " + product.getName());

            // update stock
            product.setStock(available - qty);
            product.updateLowStock();
            productRepo.save(product);

            // use latest product price
            long price = product.getPrice() == null ? 0L : product.getPrice().longValue();
            cart.setPrice(price);
            cartRepo.save(cart);

            totalAmount += price * qty;
        }

        // ------------------------------------------------------------
        // SELECT SHIPPING ADDRESS
        // ------------------------------------------------------------
        AddressBO shippingAddress;

        if (rq.getAddressId() != null) {
            shippingAddress = addressRepo.findById(rq.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Invalid addressId"));

            if (!shippingAddress.getUserid().equals(userId)) {
                throw new RuntimeException("Address does not belong to user");
            }
        } else if (rq.getFullName() != null) {

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

        } else {
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

        // payment method
        String pm = rq.getPaymentMethod() == null ? "" : rq.getPaymentMethod().trim().toUpperCase();
        order.setPaymentMethod(pm);

        order.setPaymentStatus("COD".equals(pm) ? "COD_PENDING" : "CREATED");

        String invoiceNo = "AIM" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        order.setInvoiceno(invoiceNo);

        // save order first
        orderRepo.save(order);

        // ------------------------------------------------------------
        // ORDER ITEMS
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
        // INVOICE PDF
        // ------------------------------------------------------------
        String html = invoiceService.downloadInvoiceHtml(order.getOrderId());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);

        FileBO pdfFile = fileService.uploadBytes(out.toByteArray(), invoiceNo + ".pdf");

        InvoiceBO invoice = InvoiceBO.builder()
                .order(order)
                .user(user)
                .invoiceNumber(invoiceNo)
                .invoiceFileId(pdfFile.getId())
                .build();

        invoiceRepo.save(invoice);

        // ------------------------------------------------------------
        // USER ACTIVITY LOG
        // ------------------------------------------------------------
        userActivityService.log(
                userId,
                "ORDER_PLACED",
                "Order placed with ID: " + order.getOrderId()
        );

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
        // no-op
    }

    @Override
    public BaseRs retrieveOrdersUser() throws Exception {
        Long userId = AuthUtils.getLoggedUserId();

        List<OrderBO> orders =
                orderRepo.findAllByUserIdOrderByOrderTimeDesc(userId);

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
        if (userId == null)
            throw new RuntimeException("User not authenticated");

        OrderBO order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            return ResponseUtils.failure("NOT_ALLOWED", "You cannot cancel this order.");
        }

        if (!"INITIAL".equalsIgnoreCase(order.getOrderStatus())) {
            return ResponseUtils.failure("CANNOT_CANCEL", "Order already processed");
        }

        // restore stock
        for (CartBO cart : order.getCartItems()) {

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
        cartRepo.saveAll(order.getCartItems());

        order.setOrderStatus("CANCELLED");
        order.setPaymentStatus("CANCELLED");
        orderRepo.save(order);

        userActivityService.log(
                userId,
                "ORDER_CANCELLED",
                "Cancelled order ID: " + order.getOrderId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getOrderId());
        response.put("orderStatus", order.getOrderStatus());
        response.put("paymentStatus", order.getPaymentStatus());

        return ResponseUtils.success(response);
    }

    private SellerBO findSellerFromOrder(OrderBO order) {
        if (order.getCartItems() == null || order.getCartItems().isEmpty())
            return null;

        CartBO cart = order.getCartItems().get(0);
        ProductBO product = cart.getProduct();

        return (product == null) ? null : product.getSeller();
    }
}
