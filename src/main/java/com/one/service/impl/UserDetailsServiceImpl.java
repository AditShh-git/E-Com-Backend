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

        email = email.toLowerCase().trim();
        log.debug("Authenticating {}", email);

        // ADMIN
        AdminBO admin = adminRepo.findByEmail(email).orElse(null);
        if (admin != null) {
            if (!admin.isEmailVerified()) {
                throw new UsernameNotFoundException("Email not verified.");
            }
            return UserDetailsImpl.build(admin);
        }

        // SELLER
        SellerBO seller = sellerRepo.findByEmail(email).orElse(null);
        if (seller != null) {

            if (!seller.isEmailVerified()) {
                throw new UsernameNotFoundException("Email not verified.");
            }

            if (seller.isLocked()) {
                throw new UsernameNotFoundException("Seller account is locked.");
            }

            return UserDetailsImpl.build(seller);
        }

        // USER
        UserBO user = userRepo.findByEmail(email).orElse(null);
        if (user != null) {

            if (Boolean.TRUE.equals(user.getDeleted())) {
                throw new UsernameNotFoundException("Account deleted.");
            }

            if (!Boolean.TRUE.equals(user.getActive())) {
                throw new UsernameNotFoundException("Account disabled.");
            }

            if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                throw new UsernameNotFoundException("Email not verified.");
            }

            return UserDetailsImpl.build(user);
        }

        // VENDOR
        VendorBO vendor = vendorRepo.findByEmail(email).orElse(null);
        if (vendor != null) {
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

        // DELIVERY PERSON
        DeliveryPersonBO dp = deliveryPersonRepo.findByEmail(email).orElse(null);
        if (dp != null) {
            return new UserDetailsImpl(
                    dp.getId(),
                    dp.getEmail(),
                    dp.getFullName(),
                    dp.getPassword(),
                    true,
                    true,
                    List.of(new SimpleGrantedAuthority("DELIVERY_PERSON"))
            );
        }

        throw new UsernameNotFoundException("No account found with email: " + email);
    }
}
