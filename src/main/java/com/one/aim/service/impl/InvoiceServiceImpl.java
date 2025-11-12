package com.one.aim.service.impl;

import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.one.aim.bo.CartBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.repo.OrderRepo;
import com.one.aim.service.InvoiceService;
import com.one.utils.AuthUtils;

@Service
public class InvoiceServiceImpl implements InvoiceService {

	@Value("${invoice.template.path}")
	private String templatePath;

	private final ResourceLoader resourceLoader;

	public InvoiceServiceImpl(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Autowired
	OrderRepo orderRepo;

	@Override
	public String downloadInvoice(long orderId) throws Exception {

		Resource resource = resourceLoader.getResource("classpath:" + templatePath);
		String htmlContent = Files.readString(resource.getFile().toPath());
		Optional<OrderBO> optOrderBO = orderRepo.findById(orderId);

		OrderBO orderBO = optOrderBO.get();
		StringBuilder productRows = new StringBuilder();

		for (CartBO cartBO : orderBO.getCartItems()) {
			productRows.append("<tr>").append("<td style=\"padding: 8px; border: 1px solid #ddd;\">")
					.append(cartBO.getPname()).append("</td>")
					.append("<td style=\"padding: 8px; border: 1px solid #ddd;\">").append(1)
					.append("</td>").append("<td style=\"padding: 8px; border: 1px solid #ddd;\">₹")
					.append(cartBO.getPrice()).append("</td>")
					.append("<td style=\"padding: 8px; border: 1px solid #ddd;\">₹")
					.append(cartBO.getPrice() * 1).append("</td>").append("</tr>\n");
		}

		// htmlContent = htmlContent.replace("${productRows}", productRows);

		// 2. Replace placeholders with dynamic values
		htmlContent = htmlContent.replace("${orderNumber}", orderBO.getId().toString())
				.replace("${orderDate}", orderBO.getOrderTime().toString())
				.replace("${invoiceDate}", LocalDateTime.now().toString()).replace("${invoiceNumber}", orderBO.getInvoiceno())
				.replace("${billingName}", AuthUtils.findLoggedInUser().getFullName())
				.replace("${billingAddress}", orderBO.getShippingAddress().getStreet() + ", "
						+ orderBO.getShippingAddress().getCity() + ", " + orderBO.getShippingAddress().getState() + ", "
						+ orderBO.getShippingAddress().getCountry() + ", " + orderBO.getShippingAddress().getZip())
				.replace("${shippingName}", "John Doe")
				.replace("${shippingAddress}", orderBO.getShippingAddress().getStreet() + ", "
						+ orderBO.getShippingAddress().getCity() + ", " + orderBO.getShippingAddress().getState() + ", "
						+ orderBO.getShippingAddress().getCountry() + ", " + orderBO.getShippingAddress().getZip())
				.replace("${productRows}", productRows).replace("${grandTotal}", orderBO.getTotalAmount().toString());
		return htmlContent;
	}

}
