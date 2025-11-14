package com.one.aim.controller;

import com.one.aim.bo.OrderBO;
import com.one.aim.repo.OrderRepo;
import com.one.security.LoggedUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = "/api")
@Slf4j
@RequiredArgsConstructor
public class OrderController {


	private final OrderService orderService;
    private final OrderRepo  orderRepo;

	@PostMapping("/order/save")
	public ResponseEntity<?> saveOrder(@RequestBody OrderRq rq) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /order/save]");
		}
		return new ResponseEntity<>(orderService.placeOrder(rq), HttpStatus.OK);
	}

	@GetMapping("/order/{id}")
	public ResponseEntity<?> retrieveCart(@PathVariable("id") long id) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [GET /order/id]");
		}
		return new ResponseEntity<>(orderService.retrieveOrder(id), HttpStatus.OK);
	}

	@GetMapping("/orders")
	public ResponseEntity<?> retrieveCartList() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /orders]");
		}
		return new ResponseEntity<>(orderService.retrieveOrders(), HttpStatus.OK);
	}

	@GetMapping("/orders/status/update")
	public ResponseEntity<?> updateDeliveryStatus(@RequestParam("orderId") String orderId,
			@RequestParam("status") String status) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [GET /orders] - updateDeliveryStatus");
		}

		// Call a method to update delivery status, assuming it returns updated order or
		// success message
		orderService.updateDeliveryStatus(orderId, status);
		return new ResponseEntity<>(MessageCodes.MC_UPDATED_SUCCESSFULLY, HttpStatus.OK);
	}

	@GetMapping("/orders/user")
	public ResponseEntity<?> orderUsers() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [GET /orders/user] - orderUsers");
		}

		// Call a method to update delivery status, assuming it returns updated order or
		// success message
		return new ResponseEntity<>(orderService.retrieveOrdersUser(), HttpStatus.OK);
	}

	@GetMapping("/orders/all/users")
	public ResponseEntity<?> retrieveAllUsersOrder() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /orders]");
		}
		return new ResponseEntity<>(orderService.retrieveAllOrders(), HttpStatus.OK);
	}

	@DeleteMapping("/orders/cancel/{orderId}")
	public ResponseEntity<?> orderCancel(@PathVariable("orderId") String orderId) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [DELETE /orders/cancel] - orderCancel");
		}

		// Call a method to update delivery status, assuming it returns updated order or
		// success message
		return new ResponseEntity<>(orderService.retrieveOrdersCancel(orderId), HttpStatus.OK);
	}

    @GetMapping("/invoice/{fileName:.+}")
    public ResponseEntity<?> getInvoiceFile(@PathVariable String fileName) {
        try {
            Long loggedInUserId = LoggedUserContext.getLoggedUserId();
            String role = LoggedUserContext.getLoggedUserRole();

            if (loggedInUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Unauthorized access");
            }

            // Allow only safe names
            if (!fileName.matches("[A-Za-z0-9._-]+\\.pdf")) {
                return ResponseEntity.badRequest().body("Invalid filename");
            }

            // Extract invoice number
            String invoiceNo = fileName.replace(".pdf", "");

            OrderBO order = orderRepo.findByInvoiceno(invoiceNo);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Invoice not found");
            }

            // ==============================
            // Authorization
            // ==============================
            boolean allowed = false;

            switch (role.toUpperCase()) {
                case "USER":
                    allowed = order.getUser() != null
                            && order.getUser().getId().equals(loggedInUserId);
                    break;

                case "SELLER":
                case "VENDOR":
                    allowed = order.getCartempids() != null
                            && order.getCartempids().contains(loggedInUserId);
                    break;

                case "ADMIN":
                    allowed = true;
                    break;
            }

            if (!allowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You do not have permission to download this invoice");
            }

            // ==============================
            // Locate file
            // ==============================
            Path base = Paths.get(System.getProperty("user.dir"),
                    "uploads", "downloads", "invoices");

            Path filePath = base.resolve(fileName).normalize();

            if (!filePath.startsWith(base)) {
                return ResponseEntity.badRequest().body("Invalid path");
            }

            if (!Files.exists(filePath)) {
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
