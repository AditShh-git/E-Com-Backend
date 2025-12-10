package com.one.aim.service.impl;

import com.one.aim.bo.CartBO;
import com.one.aim.bo.ProductBO;
import com.one.aim.repo.CartRepo;
import com.one.aim.service.AdminSettingService;
import com.one.aim.service.ChargesService;
import com.one.utils.AuthUtils;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChargesServiceImpl implements ChargesService {

    private final CartRepo cartRepo;
    private final AdminSettingService adminSettingService;

    @Override
    public ResponseEntity<?> calculate(Double subtotal, String shippingMethod, String state) {
        try {
            Long userId = AuthUtils.findLoggedInUser().getDocId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ResponseUtils.failure("AUTH_REQUIRED", "User not authenticated"));
            }

            long calculatedSubtotal = 0L;
            long totalTax = 0L;
            long totalShipping = 0L;

            if (subtotal == null || subtotal <= 0) {
                List<CartBO> cartItems = cartRepo.findAllByUserAddToCart_IdAndEnabled(userId, true);

                for (CartBO cart : cartItems) {
                    ProductBO product = cart.getProduct();
                    if (product != null && product.isActive()) {
                        long price = product.getPrice().longValue();
                        int qty = Math.max(cart.getQuantity(), 1);
                        long itemSubtotal = price * qty;
                        calculatedSubtotal += itemSubtotal;

                        String category = product.getCategoryName().toLowerCase();

                        double taxPercent = getDynamicTax(category);
                        double shippingCharge = getDynamicShipping(category);

                        totalTax += Math.round(itemSubtotal * taxPercent / 100);
                        totalShipping += Math.round(shippingCharge * qty);
                    }
                }
            } else {
                calculatedSubtotal = subtotal.longValue();
                double defaultTax = adminSettingService.getDoubleValue("default_tax_percent", 0.0);
                double defaultShipping = adminSettingService.getDoubleValue("delivery_charges_fixed", 0.0);
                totalTax = Math.round(calculatedSubtotal * defaultTax / 100);
                totalShipping = Math.round(defaultShipping);
            }

            if (calculatedSubtotal <= 0) {
                return ResponseEntity.ok(Map.of("subtotal", 0, "tax", 0, "shipping", 0, "total", 0));
            }

            long freeShippingAbove =
                    adminSettingService.getLongValue("free_shipping_min_order_amount", 0);

            if (calculatedSubtotal >= freeShippingAbove) {
                totalShipping = 0;
            }

            long grandTotal = calculatedSubtotal + totalTax + totalShipping;

            return ResponseEntity.ok(Map.of(
                    "subtotal", calculatedSubtotal,
                    "tax", totalTax,
                    "shipping", totalShipping,
                    "total", grandTotal,
                    "freeShippingThreshold", freeShippingAbove
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ResponseUtils.failure("CALCULATION_ERROR", e.getMessage()));
        }
    }

    private double getDynamicTax(String category) {
        String key = "tax_" + category.toLowerCase();
        return adminSettingService.getDoubleValue(key,
                adminSettingService.getDoubleValue("default_tax_percent", 0.0));
    }

    private double getDynamicShipping(String category) {
        String key = "shipping_" + category.toLowerCase();
        return adminSettingService.getDoubleValue(key,
                adminSettingService.getDoubleValue("delivery_charges_fixed", 0.0));
    }

}

