package com.one.aim.controller;

import com.one.aim.bo.OrderBO;
import com.one.aim.mapper.OrderMapper;
import com.one.aim.repo.OrderRepo;
import com.one.aim.service.FileService;
import com.one.utils.AuthUtils;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.constants.MessageCodes;
import com.one.aim.rq.OrderRq;
import com.one.aim.service.OrderService;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepo orderRepo;
    private final FileService fileService;

    // ---------------------------------------------------------
    // PLACE ORDER (USER)
    // ---------------------------------------------------------
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRq rq) throws Exception {
        return ResponseEntity.ok(orderService.placeOrder(rq));
    }

    // ---------------------------------------------------------
    // GET SINGLE ORDER
    // ---------------------------------------------------------
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderByOrderId(@PathVariable String orderId) throws Exception {
        OrderBO order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return ResponseEntity.ok(
                ResponseUtils.success(OrderMapper.mapToOrderRs(order, fileService))
        );
    }


    // ---------------------------------------------------------
    // GET ALL ORDERS (ADMIN)
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<?> getAllOrders() throws Exception {
        return ResponseEntity.ok(orderService.retrieveOrders());
    }

    // ---------------------------------------------------------
    // UPDATE DELIVERY STATUS
    // ---------------------------------------------------------
    @GetMapping("/status/update")
    public ResponseEntity<?> updateDeliveryStatus(
            @RequestParam String orderId,
            @RequestParam String status) throws Exception {

        orderService.updateDeliveryStatus(orderId, status);
        return ResponseEntity.ok(MessageCodes.MC_UPDATED_SUCCESSFULLY);
    }

    // ---------------------------------------------------------
    // GET ORDERS OF LOGGED-IN USER
    // ---------------------------------------------------------
    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders() throws Exception {
        return ResponseEntity.ok(orderService.retrieveOrdersUser());
    }

    // ---------------------------------------------------------
    // GET ALL USERS' ORDERS (ADMIN)
    // ---------------------------------------------------------
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsersOrders() throws Exception {
        return ResponseEntity.ok(orderService.retrieveAllOrders());
    }

    // ---------------------------------------------------------
    // CANCEL ORDER (USER)
    // ---------------------------------------------------------
    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId) throws Exception {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }


    // ---------------------------------------------------------
    // DOWNLOAD INVOICE (SECURED)
    // ---------------------------------------------------------
    @GetMapping("/invoice/{fileName:.+}")
    public ResponseEntity<?> downloadInvoice(@PathVariable String fileName) {
        try {
            Long loggedInUserId = AuthUtils.getLoggedUserId();
            String role = AuthUtils.getLoggedUserRole();

            if (loggedInUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Unauthorized access");
            }

            // Validate filename (avoid path traversal)
            if (!fileName.matches("[A-Za-z0-9._-]+\\.pdf")) {
                return ResponseEntity.badRequest().body("Invalid filename");
            }

            // Extract invoice number (without .pdf)
            String invoiceNo = fileName.replace(".pdf", "");
            OrderBO order = orderRepo.findByInvoiceno(invoiceNo);

            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Invoice not found");
            }

            // ----------------------------------------------------
            // AUTHORIZATION
            // ----------------------------------------------------
            boolean allowed = false;

            switch (role.toUpperCase()) {

                case "USER":
                    allowed = order.getUser() != null
                            && order.getUser().getId().equals(loggedInUserId);
                    break;

                case "SELLER":
                    allowed = order.getCartItems().stream()
                            .anyMatch(c -> c.getProduct() != null
                                    && c.getProduct().getSeller() != null
                                    && c.getProduct().getSeller().getId().equals(loggedInUserId));
                    break;

                case "ADMIN":
                    allowed = true;
                    break;
            }

            if (!allowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You do not have permission to download this invoice");
            }

            // ----------------------------------------------------
            // READ PDF FILE
            // ----------------------------------------------------
            Path base = Paths.get(System.getProperty("user.dir"),
                    "uploads", "downloads", "invoices");

            Path filePath = base.resolve(fileName).normalize();

            if (!filePath.startsWith(base) || !Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Invoice file missing");
            }

            byte[] pdfBytes = Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", fileName);
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Invoice download error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve invoice");
        }
    }

}
