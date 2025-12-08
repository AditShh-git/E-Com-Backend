package com.one.aim.service.impl;

import com.itextpdf.html2pdf.HtmlConverter;
import com.one.aim.bo.*;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.controller.OrderNotificationController;
import com.one.aim.mapper.OrderMapper;
import com.one.aim.repo.*;
import com.one.aim.rq.OrderRq;
import com.one.aim.rs.OrderRs;
import com.one.aim.rs.UserRs;
import com.one.aim.rs.data.OrderDataRs;
import com.one.aim.rs.data.OrderDataRsList;
import com.one.aim.service.*;
import com.one.utils.AuthUtils;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final SellerRepo sellerRepo;
    private final AdminSettingService adminSettingService;
    private final CategoryRepo categoryRepo;
    //Notification
    private final NotificationService notificationService;

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
            return ResponseUtils.failure("AUTH_REQUIRED", "User not authenticated");
        }

        // ------------------------------------------------------------
        // LOAD CART ITEMS
        // ------------------------------------------------------------
        List<CartBO> cartItems = cartRepo.findAllByUserAddToCart_IdAndEnabled(userId, true);
        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseUtils.failure("CART_EMPTY", "Your cart is empty.");
        }

        long subTotal = 0L;
        long totalTax = 0L;
        long totalShipping = 0L;

        // Admin settings
        boolean discountEnabled = adminSettingService.isDiscountEngineEnabled();
        int globalDiscount = discountEnabled ? adminSettingService.getGlobalDiscount() : 0;
        long freeShippingAbove = adminSettingService.getLongValue("free_shipping_min_order_amount", 999);

        // ------------------------------------------------------------
        // STOCK VALIDATION + PRICE CALCULATION
        // ------------------------------------------------------------
        for (CartBO cart : cartItems) {

            ProductBO product = cart.getProduct();
            if (product == null || !product.isActive()) {
                return ResponseUtils.failure("PRODUCT_INVALID",
                        "Product " + cart.getPname() + " is unavailable");
            }

            int qty = Math.max(cart.getQuantity(), 1);
            int available = product.getStock() == null ? 0 : product.getStock();

            if (available < qty) {
                return ResponseUtils.failure("INSUFFICIENT_STOCK",
                        product.getName() + " - only " + available + " left.");
            }

            long linePrice = product.getPrice().longValue() * qty;
            subTotal += linePrice;

            // Category-based TAX & SHIPPING
            String category = product.getCategoryName().toLowerCase().replace(" ", "_");

            double taxPercent = adminSettingService.getDoubleValue("tax_" + category, 0.0);
            double shippingCharge = adminSettingService.getDoubleValue("shipping_" + category, 50.0);

            totalTax += Math.round(linePrice * taxPercent / 100);
            totalShipping += Math.round(shippingCharge);

            // Stock update
            product.setStock(available - qty);
            product.updateLowStock();
            productRepo.save(product);

            // Persist final price
            cart.setPrice(product.getPrice().longValue());
            cartRepo.save(cart);
        }

        // ------------------------------------------------------------
        // DISCOUNT
        // ------------------------------------------------------------
        long discountAmount = 0;
        if (globalDiscount > 0) {
            discountAmount = Math.round(subTotal * globalDiscount / 100);
        }

        // ------------------------------------------------------------
        // FREE SHIPPING RULE
        // ------------------------------------------------------------
        if (subTotal >= freeShippingAbove) {
            totalShipping = 0;
        }

        // ------------------------------------------------------------
        // FINAL TOTAL
        // ------------------------------------------------------------
        long grandTotal = subTotal + totalTax + totalShipping - discountAmount;

        // ------------------------------------------------------------
        // SHIPPING ADDRESS
        // ------------------------------------------------------------
        AddressBO shippingAddress = resolveShippingAddress(rq, userId);

        // ------------------------------------------------------------
        // CREATE ORDER
        // ------------------------------------------------------------
        UserBO user = userRepo.findById(userId).orElseThrow();

        OrderBO order = new OrderBO(); // orderId auto generated
        order.setUser(user);
        order.setOrderStatus("INITIAL");
        order.setOrderTime(LocalDateTime.now());
        order.setSubTotal(subTotal);
        order.setTaxAmount(totalTax);
        order.setDeliveryCharge(totalShipping);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(grandTotal);
        order.setShippingAddress(shippingAddress);

        String pm = normalizePaymentMethod(rq.getPaymentMethod());
        order.setPaymentMethod(pm);
        order.setPaymentStatus(paymentStatusFromMethod(pm));

        // Generate invoice number BEFORE save
        String invoiceNo = adminSettingService.get("order_prefix")
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        order.setInvoiceno(invoiceNo);

        orderRepo.save(order);  //  orderId auto-created here

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
        // GENERATE & STORE INVOICE PDF
        // ------------------------------------------------------------
        invoiceService.generateInvoice(order.getOrderId());

        // ------------------------------------------------------------
        // ACTIVITY LOG + RESPONSE
        // ------------------------------------------------------------
        userActivityService.log(
                userId,
                "ORDER_PLACED",
                "Order " + order.getOrderId() + " placed"
        );

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getOrderId());
        response.put("subTotal", subTotal);
        response.put("taxAmount", totalTax);
        response.put("shipping", totalShipping);
        response.put("discount", discountAmount);
        response.put("grandTotal", grandTotal);
        response.put("paymentMethod", pm);
        response.put("paymentStatus", order.getPaymentStatus());

        return ResponseUtils.success(response);
    }




    @Override
    public BaseRs retrieveOrder(Long orderId) throws Exception {
        log.debug("Executing retrieveOrder() for ID: {}", orderId);

        try {
            return orderRepo.findById(orderId)
                    .map(order -> {
                        OrderRs orderRs = OrderMapper.mapToOrderRs(order, fileService);
                        return ResponseUtils.success(
                                new OrderDataRs(MessageCodes.MC_RETRIEVED_SUCCESSFUL, orderRs)
                        );
                    })
                    .orElseGet(() -> {
                        log.error(ErrorCodes.EC_ORDER_NOT_FOUND);
                        return ResponseUtils.failure(ErrorCodes.EC_ORDER_NOT_FOUND);
                    });
        } catch (Exception e) {
            log.error("Exception in retrieveOrder()", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_SERVER_ERROR);
        }
    }


//    @Override
//    public BaseRs retrieveOrder(Long id) throws Exception {
//
//        if (log.isDebugEnabled()) {
//            log.debug("Executing retrieveOrder() ->");
//        }
//        try {
////			Optional<UserBO> optUser = userRepo.findById(AuthUtils.findLoggedInUser().getDocId());
////			if (optUser.isEmpty()) {
////				log.error(ErrorCodes.EC_USER_NOT_FOUND);
////				return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
////			}
//            Optional<OrderBO> optOrder = orderRepo.findById(id);
//            if (optOrder.isEmpty()) {
//                log.error(ErrorCodes.EC_ORDER_NOT_FOUND);
//                return ResponseUtils.failure(ErrorCodes.EC_ORDER_NOT_FOUND);
//            }
//            OrderBO orderBO = optOrder.get();
//            OrderRs orderRs = OrderMapper.mapToOrderRs(orderBO);
//            String message = MessageCodes.MC_RETRIEVED_SUCCESSFUL;
//            return ResponseUtils.success(new OrderDataRs(message, orderRs));
//        } catch (Exception e) {
//            log.error("Exception in retrieveOrder() ->" + e);
//            return null;
//        }
//    }

    @Override
    public BaseRs retrieveOrders(int page, int size, String sortBy, String direction, String status) throws Exception {

        Sort sort = direction.equalsIgnoreCase("ASC") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderBO> pagedOrders;

        // If status filter exists
        if (status != null && !status.isBlank()) {
            pagedOrders = orderRepo.findByOrderStatusIgnoreCase(status, pageable);
        } else {
            pagedOrders = orderRepo.findAll(pageable);
        }

        List<Map<String, Object>> orderList = pagedOrders.getContent().stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("docId", o.getOrderId());
            m.put("orderTime", o.getOrderTime());
            m.put("totalAmount", o.getTotalAmount());
            m.put("status", o.getOrderStatus());
            m.put("itemCount", o.getOrderItems().size());
            m.put("user", Map.of(
                    "userName", o.getUser().getFullName(),
                    "email", o.getUser().getEmail()
            ));
            return m;
        }).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("orders", orderList);
        data.put("currentPage", pagedOrders.getNumber());
        data.put("totalPages", pagedOrders.getTotalPages());
        data.put("totalItems", pagedOrders.getTotalElements());
        data.put("pageSize", pagedOrders.getSize());

        return ResponseUtils.success(data);
    }

//    @Override
//    public BaseRs retrieveOrders() throws Exception {
//        return null;
//    }


    @Override
    public void updateDeliveryStatus(String orderId, String status) {
        // no-op
    }

    @Override
    public BaseRs retrieveOrdersUser() throws Exception {
        Long userId = AuthUtils.getLoggedUserId();

        List<OrderBO> orders =
                orderRepo.findAllByUser_IdOrderByOrderTimeDesc(userId);

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

    @Override
    public BaseRs retrieveOrdersForSeller(
            int page,
            int size,
            String sortBy,
            String direction,
            String status
    ) throws Exception {

        String email = AuthUtils.findLoggedInUser().getEmail();

        SellerBO seller = sellerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Long sellerId = seller.getId();

        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderBO> pagedOrders =
                orderRepo.findOrdersForSeller(sellerId, status, pageable);

        List<OrderRs> orderList =
                OrderMapper.mapToOrderRsList(pagedOrders.getContent(), fileService);

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderList);
        response.put("page", pagedOrders.getNumber());
        response.put("size", pagedOrders.getSize());
        response.put("totalPages", pagedOrders.getTotalPages());
        response.put("totalElements", pagedOrders.getTotalElements());
        response.put("isLast", pagedOrders.isLast());

        return ResponseUtils.success(response);
    }


    private double getTaxPercent(ProductBO product) {
        String cat = product.getCategoryName() == null ? "" : product.getCategoryName().toLowerCase();
        return switch (cat) {
            case "electronics" -> parseDoubleSafe("tax_electronics", 18);
            case "fashion"     -> parseDoubleSafe("tax_fashion", 5);
            case "grocery"     -> parseDoubleSafe("tax_grocery", 0);
            default            -> parseDoubleSafe("default_tax_percent", 0);
        };
    }

    private double getDeliveryCharge(ProductBO product) {
        String cat = product.getCategoryName() == null ? "" : product.getCategoryName().toLowerCase();
        return switch (cat) {
            case "electronics" -> parseDoubleSafe("shipping_electronics", 100);
            case "fashion"     -> parseDoubleSafe("shipping_fashion", 50);
            case "grocery"     -> parseDoubleSafe("shipping_grocery", 20);
            default            -> parseDoubleSafe("delivery_charges_fixed", 50);
        };
    }

    private double parseDoubleSafe(String key, double defaultValue) {
        try {
            String v = adminSettingService.get(key);
            return (v == null || v.isBlank()) ? defaultValue : Double.parseDouble(v);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String normalizePaymentMethod(String pm) {
        if (pm == null) return "COD";
        return pm.trim().toUpperCase();
    }

    private String paymentStatusFromMethod(String pm) {
        return pm.equals("COD") ? "COD_PENDING" : "CREATED";
    }

    private AddressBO resolveShippingAddress(OrderRq rq, Long userId) {
        if (rq.getAddressId() != null) {
            AddressBO address = addressRepo.findById(rq.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Invalid addressId"));

            if (!address.getUserid().equals(userId))
                throw new RuntimeException("Address does not belong to user");

            return address;
        }

        return addressRepo.findFirstByUseridAndIsDefault(userId, true)
                .orElseThrow(() -> new RuntimeException("No default address found"));
    }


    private SellerBO findSellerFromOrder(OrderBO order) {
        if (order.getCartItems() == null || order.getCartItems().isEmpty())
            return null;

        CartBO cart = order.getCartItems().get(0);
        ProductBO product = cart.getProduct();

        return (product == null) ? null : product.getSeller();
    }

}
