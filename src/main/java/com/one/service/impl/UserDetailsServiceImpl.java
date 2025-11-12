package com.one.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.DeliveryPersonBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.UserBO;
import com.one.aim.bo.VendorBO;
import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.DeliveryPersonRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.repo.VendorRepo;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepo userRepo;
    private final AdminRepo adminRepo;
    private final SellerRepo sellerRepo;
    private final VendorRepo vendorRepo;
    private final DeliveryPersonRepo deliveryPersonRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("🔍 Attempting authentication for email: {}", email);

        // ✅ Check each repository (email-based login only)
        UserBO user = userRepo.findByEmail(email).orElse(null);
        AdminBO admin = adminRepo.findByEmail(email).orElse(null);
        SellerBO seller = sellerRepo.findByEmail(email).orElse(null);
        VendorBO vendor = vendorRepo.findByEmail(email).orElse(null);
        DeliveryPersonBO delivery = deliveryPersonRepo.findByEmail(email).orElse(null);

        if (user == null && admin == null && seller == null && vendor == null && delivery == null) {
            log.error("❌ User not found with email: {}", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // ✅ Normalize to a unified user object
        UserBO unifiedUser = new UserBO();

        if (admin != null) {
            unifiedUser.setId(admin.getId());
            unifiedUser.setFullName(admin.getFullName());
            unifiedUser.setEmail(admin.getEmail());
            unifiedUser.setPassword(admin.getPassword());
            unifiedUser.setRole("ADMIN");
        } else if (seller != null) {
            unifiedUser.setId(seller.getId());
            unifiedUser.setFullName(seller.getFullName());
            unifiedUser.setEmail(seller.getEmail());
            unifiedUser.setPassword(seller.getPassword());
            unifiedUser.setRole("SELLER");
        } else if (vendor != null) {
            unifiedUser.setId(vendor.getId());
            unifiedUser.setFullName(vendor.getFullName());
            unifiedUser.setEmail(vendor.getEmail());
            unifiedUser.setPassword(vendor.getPassword());
            unifiedUser.setRole("VENDOR");
        } else if (delivery != null) {
            unifiedUser.setId(delivery.getId());
            unifiedUser.setFullName(delivery.getFullName());
            unifiedUser.setEmail(delivery.getEmail());
            unifiedUser.setPassword(delivery.getPassword());
            unifiedUser.setRole("DELIVERY_PERSON");
        } else if (user != null) {
            unifiedUser.setId(user.getId());
            unifiedUser.setFullName(user.getFullName());
            unifiedUser.setEmail(user.getEmail());
            unifiedUser.setPassword(user.getPassword());
            unifiedUser.setRole("USER");
        }

        log.info("✅ Authenticated [{}] ({})", unifiedUser.getRole(), unifiedUser.getEmail());
        return UserDetailsImpl.build(unifiedUser);
    }
}