package com.one.aim.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.bo.OrderBO;
import com.one.aim.repo.OrderRepo;
import com.one.aim.rq.PaymentRq;
import com.one.aim.service.PaymentService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api")
@Slf4j
public class PaymentController {

	@Autowired
	PaymentService paymentService;

	@Autowired
	OrderRepo orderRepo;

	private RazorpayClient razorpayClient;

	@PostMapping("/payment")
	public ResponseEntity<?> payment(@RequestBody PaymentRq rq) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /payment]");
		}
		return new ResponseEntity<>(paymentService.processPayment(rq), HttpStatus.OK);
	}

	@PostMapping("auth/createPayment")
	public String createOrder(@RequestParam int amount, @RequestParam String currency, @RequestParam String orderId)
			throws Exception {

		try {
			return paymentService.makePayment(amount, currency, "recepient_100", orderId);
		} catch (RazorpayException e) {
			throw new RuntimeException(e);
		}
	}

//	@PostMapping("/razorpay/status")
//	public ResponseEntity<String> handleRazorpayWebhook(HttpServletRequest request) {
//		try {
//			// Read the payload from Razorpay
//			String payload = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
//
//			// Read signature header
//			String razorpaySignature = request.getHeader("X-Razorpay-Signature");
//
//			// Verify webhook signature
//			boolean isValid = verifySignature(payload, razorpaySignature, "your_webhook_secret");
//			if (!isValid) {
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
//			}
//
//			// Parse payload JSON
//			JSONObject webhookData = new JSONObject(payload);
//			String event = webhookData.getString("event");
//
//			if ("payment.captured".equals(event)) {
//				String razorpayOrderId = webhookData.getJSONObject("payload").getJSONObject("payment")
//						.getJSONObject("entity").getString("order_id");
//
//				// Update payment status to PAID
//				OrderBO paidOrder = orderRepo.findByRazorpayorderid(razorpayOrderId);
//				if (paidOrder != null) {
//					paidOrder.setPaymentstatus("PAID");
//					orderRepo.save(paidOrder);
//				}
//
//			} else if ("payment.failed".equals(event)) {
//				String razorpayOrderId = webhookData.getJSONObject("payload").getJSONObject("payment")
//						.getJSONObject("entity").getString("order_id");
//
//				// Update payment status to FAILED
//				OrderBO failedOrder = orderRepo.findByRazorpayorderid(razorpayOrderId);
//				if (failedOrder != null) {
//					failedOrder.setPaymentstatus("FAILED");
//					orderRepo.save(failedOrder);
//				}
//			}
//
//			return ResponseEntity.ok("Webhook processed successfully");
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
//		}
//	}
//
//	private boolean verifySignature(String payload, String signature, String secret) {
//		try {
//			String expectedSignature = hmacSha256(payload, secret);
//			return expectedSignature.equals(signature);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
//
//	private String hmacSha256(String data, String secret) throws Exception {
//		Mac sha256Hmac = Mac.getInstance("HmacSHA256");
//		SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
//		sha256Hmac.init(secretKey);
//		byte[] hash = sha256Hmac.doFinal(data.getBytes());
//		return Base64.getEncoder().encodeToString(hash);
//	}

    @GetMapping("auth/getPaymentStatus")
    public ResponseEntity<?> getPaymentStatus(@RequestParam String razorpayOrderId) {
        try {
            RazorpayClient razorpay = new RazorpayClient("rzp_live_vegmIuWT1fULsb", "B9CD2cf5PLjKDgQdqwYUrjxm");

            JSONObject paymentsResponse = (JSONObject) razorpay.orders.fetchPayments(razorpayOrderId);

            JSONArray items = paymentsResponse.getJSONArray("items");

            if (items.isEmpty()) {
                return ResponseEntity.ok("No payments found for this order.");
            }

            JSONObject payment = items.getJSONObject(0);
            String status = payment.getString("status");

            OrderBO orderBO = orderRepo.findByRazorpayorderid(razorpayOrderId);


            orderBO.setPaymentStatus(status);

            orderRepo.save(orderBO);

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching payment status: " + e.getMessage());
        }
    }


}
