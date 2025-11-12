package com.one.aim.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.one.aim.bo.AddressBO;
import com.one.aim.bo.AdminBO;
import com.one.aim.bo.CartBO;
import com.one.aim.bo.DeliveryPersonBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.UserBO;
import com.one.aim.bo.VendorBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.controller.OrderNotificationController;
import com.one.aim.mapper.OrderMapper;
import com.one.aim.repo.AddressRepo;
import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.CartRepo;
import com.one.aim.repo.DeliveryPersonRepo;
import com.one.aim.repo.OrderRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.repo.VendorRepo;
import com.one.aim.rq.OrderRq;
import com.one.aim.rs.OrderRs;
import com.one.aim.rs.data.OrderDataRs;
import com.one.aim.rs.data.OrderDataRsList;
import com.one.aim.service.OrderService;
import com.one.aim.service.PaymentService;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OderServiceImpl implements OrderService {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private AdminRepo adminRepo;

	@Autowired
	private SellerRepo sellerRepo;

	@Autowired
	private VendorRepo vendorRepo;

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private OrderRepo orderRepo;

	@Autowired
	private AddressRepo addressRepo;

	@Autowired
	DeliveryPersonRepo deliveryPersonRepo;

	@Autowired
	private OrderNotificationController orderNotificationController;

	@Autowired
	PaymentService paymentService;

	@Override
	@Transactional
	public BaseRs placeOrder(OrderRq rq) throws Exception {
		Long userId = AuthUtils.findLoggedInUser().getDocId();

		// Fetch enabled cart items for the current user
//		List<CartBO> cartItems = cartRepo.findByUserId(userId).stream().filter(CartBO::isEnabled)
//				.collect(Collectors.toList());

		List<CartBO> cartItems = new ArrayList<>();
		long total = 0;
		for (Entry<String, Integer> entry : rq.getTotalCarts().entrySet()) {
			Optional<CartBO> optCartBO = cartRepo.findById(Long.valueOf(entry.getKey()));
			CartBO cartBO = optCartBO.get();
			cartBO.setTotalitem(cartBO.getTotalitem() - entry.getValue());
			cartBO.setSolditem(cartBO.getSolditem() + entry.getValue());
			total += cartBO.getPrice() * entry.getValue();
			cartItems.add(cartBO);

		}

		if (cartItems.isEmpty()) {
			throw new RuntimeException("Cart is empty");
		}

		// Calculate total amount
		// Long total = cartItems.stream().map(CartBO::getPrice).reduce(0L, Long::sum);

//		for (CartBO cartBO : cartItems) {
//			total += cartBO.getPrice() * cartBO.getTotalitem();
//		}

		// Save shipping address
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

		// Mark items as ordered/disabled
		for (CartBO item : cartItems) {
			item.setEnabled(false);
		}
		cartRepo.saveAll(cartItems);
		// Create order
		OrderBO order = new OrderBO();
		Optional<UserBO> optUser = userRepo.findById(userId);
		if (!optUser.isEmpty()) {
			order.setUser(optUser.get());
		}
		order.setOrderStatus("INITIAL");
		order.setTotalAmount(total);
		order.setPaymentMethod(rq.getPaymentMethod().toUpperCase());
		order.setOrderTime(LocalDateTime.now());
		order.setShippingAddress(address);
		order.setCartItems(cartItems);
		List<Long> cartEmpIds = new ArrayList<>();
		for (CartBO cartBO : cartItems) {
			cartEmpIds.add(cartBO.getCartempid());
		}
		order.setCartempids(cartEmpIds);

		// List<DeliveryPersonBO> deliveryPersonBOs =
		// deliveryPersonRepo.findByCityAndStatusIsAvailable(address.getCity());
//		if (Utils.isNotEmpty(deliveryPersonBOs)) {
//			int randomIndex = ThreadLocalRandom.current().nextInt(deliveryPersonBOs.size());
//			DeliveryPersonBO deliveryPersonBO=deliveryPersonBOs.get(randomIndex);
//			assignDeliveryPerson(order);
//		}
//		order.setStatus("PENDING");
		LocalDateTime now = LocalDateTime.now();

		// Format: HHmmssSSS → e.g., 114532789
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMDDHHmmssSSS");

		String timeOnly = now.format(formatter);
		String invoiceno = "AIM" + timeOnly;
		order.setInvoiceno(invoiceno);
		orderRepo.save(order);
		orderNotificationController.notifyVendor(AuthUtils.findLoggedInUser().getFullName(),
				"You have received a new order.");
		return ResponseUtils.success(new OrderDataRs(MessageCodes.MC_SAVED_SUCCESSFUL));
	}

	@Override
	public BaseRs retrieveOrder(Long id) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing retrieveOrder() ->");
		}
		try {
//			Optional<UserBO> optUser = userRepo.findById(AuthUtils.findLoggedInUser().getDocId());
//			if (optUser.isEmpty()) {
//				log.error(ErrorCodes.EC_USER_NOT_FOUND);
//				return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
//			}
			Optional<OrderBO> optOrder = orderRepo.findById(id);
			if (optOrder.isEmpty()) {
				log.error(ErrorCodes.EC_ORDER_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_ORDER_NOT_FOUND);
			}
			OrderBO orderBO = optOrder.get();
			OrderRs orderRs = OrderMapper.mapToOrderRs(orderBO);
			String message = MessageCodes.MC_RETRIEVED_SUCCESSFUL;
			return ResponseUtils.success(new OrderDataRs(message, orderRs));
		} catch (Exception e) {
			log.error("Exception in retrieveOrder() ->" + e);
			return null;
		}
	}

	@Override
	public BaseRs retrieveOrders() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing retrieveOrders() ->");
		}

		try {
			Long userId = AuthUtils.findLoggedInUser().getDocId();

			Optional<UserBO> userOpt = userRepo.findById(userId);
			if (userOpt.isEmpty()) {
				log.error(ErrorCodes.EC_USER_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
			}

			// Fetch orders for the logged-in user
			List<OrderBO> orderBOs = orderRepo.findByUser_Id(userId);
			if (Utils.isEmpty(orderBOs)) {
				log.error(ErrorCodes.EC_RECORD_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_RECORD_NOT_FOUND);
			}

			List<OrderRs> rsList = OrderMapper.mapToOrderRsList(orderBOs);
			return ResponseUtils.success(new OrderDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rsList));

		} catch (Exception e) {
			log.error("Exception in retrieveOrders(): ", e);
			return ResponseUtils.failure("Unexpected error occurred");
		}
	}

	// Assign product to delivery person
//	private void assignDeliveryPerson(OrderBO order) {
//		OrderBO order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
//		DeliveryPersonBO person = deliveryPersonRepo.findById(deliveryPersonId)
//				.orElseThrow(() -> new RuntimeException("Delivery person not found"));
//
//		order.setDeliveryPerson(person);
//		order.setStatus("ASSIGNED");
//
//		person.setStatus("ON_DELIVERY");
//
//		orderRepo.save(order);
//		deliveryPersonRepo.save(person);
//	}

	@Override
	public void updateDeliveryStatus(String orderId, String status) {
		OrderBO order = orderRepo.findById(Long.parseLong(orderId))
				.orElseThrow(() -> new RuntimeException("Order not found"));
		order.setDeliverystatus(status);

		if (status.equals("DELIVERED")) {
			DeliveryPersonBO dp = order.getDeliveryPerson();
			dp.setStatus("AVAILABLE");
			deliveryPersonRepo.save(dp);
		}

		orderRepo.save(order);
	}

	@Override
	public BaseRs retrieveOrdersUser() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing retrieveOrders() ->");
		}

		try {
			Long userId = AuthUtils.findLoggedInUser().getDocId();

			Optional<SellerBO> optSeller = sellerRepo.findById(userId);
			Optional<VendorBO> optVendor = vendorRepo.findById(userId);
			if (optSeller.isEmpty() && optVendor.isEmpty()) {
				log.error(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
				return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
			}

			// Fetch orders for the logged-in user
			List<OrderBO> orderBOs = orderRepo.findByCartempid(userId);
			if (Utils.isEmpty(orderBOs)) {
				log.error(ErrorCodes.EC_RECORD_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_RECORD_NOT_FOUND);
			}

			List<OrderRs> rsList = OrderMapper.mapToOrderRsList(orderBOs);
			return ResponseUtils.success(new OrderDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rsList));

		} catch (Exception e) {
			log.error("Exception in retrieveOrders(): ", e);
			return ResponseUtils.failure("Unexpected error occurred");
		}
	}

	@Override
	public BaseRs retrieveAllOrders() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing retrieveAllOrders() ->");
		}

		try {
			Long adminId = AuthUtils.findLoggedInUser().getDocId();

			Optional<AdminBO> userOpt = adminRepo.findById(adminId);
			if (userOpt.isEmpty()) {
				log.error(ErrorCodes.EC_ADMIN_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_ADMIN_NOT_FOUND);
			}

			// Fetch orders for the logged-in user
			List<OrderBO> orderBOs = orderRepo.findAll();
			if (Utils.isEmpty(orderBOs)) {
				log.error(ErrorCodes.EC_RECORD_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_RECORD_NOT_FOUND);
			}

			List<OrderRs> rsList = OrderMapper.mapToOrderRsList(orderBOs);
			return ResponseUtils.success(new OrderDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rsList));

		} catch (Exception e) {
			log.error("Exception in retrieveAllOrders(): ", e);
			return ResponseUtils.failure("Unexpected error occurred");
		}
	}

	@Override
	public BaseRs retrieveOrdersCancel(String orderId) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing retrieveOrdersCancel() ->");
		}

		try {
			Long userId = AuthUtils.findLoggedInUser().getDocId();

			Optional<UserBO> optUser = userRepo.findById(userId);
			if (optUser.isEmpty()) {
				log.error(ErrorCodes.EC_USER_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
			}

			Optional<OrderBO> optOrderBO = orderRepo.findById(Long.valueOf(orderId));
			if (optOrderBO.isEmpty()) {
				log.error(ErrorCodes.EC_ORDER_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_ORDER_NOT_FOUND);
			}
			OrderBO orderBO = optOrderBO.get();
			orderBO.setOrderStatus("CANCELLED");
			orderRepo.save(orderBO);
			// orderRepo.deleteById(Long.valueOf(orderId));
			OrderRs orderRs = OrderMapper.mapToOrderRs(optOrderBO.get());
			return ResponseUtils.success(new OrderDataRs(MessageCodes.MC_CANCELLED_SUCCESSFUL, orderRs));

		} catch (Exception e) {
			log.error("Exception in retrieveOrdersCancel(): ", e);
			return ResponseUtils.failure("Unexpected error occurred");
		}
	}

}
