package com.one.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

import java.util.List;

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

        log.debug("Attempting authentication for email: {}", email);

        // 1. ADMIN
        AdminBO admin = adminRepo.findByEmail(email).orElse(null);
        if (admin != null) {
            log.info("Authenticated [ADMIN] ({})", admin.getEmail());
            return new UserDetailsImpl(
                    admin.getId(),
                    admin.getEmail(),
                    admin.getFullName(),
                    admin.getPassword(),
                    true,       // emailVerified always true for admin
                    true,       // accountVerified always true
                    List.of(new SimpleGrantedAuthority("ADMIN"))
            );
        }

        // 2. SELLER
        SellerBO seller = sellerRepo.findByEmail(email).orElse(null);
        if (seller != null) {
            log.info("Authenticated [SELLER] ({})", seller.getEmail());
            return UserDetailsImpl.build(seller);
        }

        // 3. USER
        UserBO user = userRepo.findByEmail(email).orElse(null);
        if (user != null) {
            log.info("Authenticated [USER] ({})", user.getEmail());
            return UserDetailsImpl.build(user);
        }

        // 4. VENDOR
        VendorBO vendor = vendorRepo.findByEmail(email).orElse(null);
        if (vendor != null) {
            log.info("Authenticated [VENDOR] ({})", vendor.getEmail());
            return new UserDetailsImpl(
                    vendor.getId(),
                    vendor.getEmail(),
                    vendor.getFullName(),
                    vendor.getPassword(),
                    true,
                    true,
                    List.of(new SimpleGrantedAuthority("VENDOR"))
            );
        }

        // 5. DELIVERY PERSON
        DeliveryPersonBO delivery = deliveryPersonRepo.findByEmail(email).orElse(null);
        if (delivery != null) {
            log.info("Authenticated [DELIVERY_PERSON] ({})", delivery.getEmail());
            return new UserDetailsImpl(
                    delivery.getId(),
                    delivery.getEmail(),
                    delivery.getFullName(),
                    delivery.getPassword(),
                    true,
                    true,
                    List.of(new SimpleGrantedAuthority("DELIVERY_PERSON"))
            );
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
