package com.one.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.SellerBO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.one.aim.bo.UserBO;
public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String email;
    private String fullName;

    @JsonIgnore
    private String password;

    private boolean emailVerified;      // NEW
    private boolean accountVerified;    // NEW → seller approved / user active

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id,
                           String email,
                           String fullName,
                           String password,
                           boolean emailVerified,
                           boolean accountVerified,
                           Collection<? extends GrantedAuthority> authorities) {

        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.emailVerified = emailVerified;
        this.accountVerified = accountVerified;
        this.authorities = authorities;
    }

    // ======================================================
    // BUILD FOR USER
    // ======================================================
    public static UserDetailsImpl build(UserBO user) {

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(user.getRole()));

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPassword(),
                user.getEmailVerified(),   // REQUIRED FOR LOGIN
                user.isActive(),           // user account active
                authorities
        );
    }

    // ======================================================
    // BUILD FOR SELLER
    // ======================================================
    public static UserDetailsImpl build(SellerBO seller) {

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(seller.getRole()));

        return new UserDetailsImpl(
                seller.getId(),
                seller.getEmail(),
                seller.getFullName(),
                seller.getPassword(),
                seller.isEmailVerified(),   // must verify email
                seller.isVerified(),        // must be admin approved
                authorities
        );
    }

    public static UserDetailsImpl build(AdminBO admin) {

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(admin.getRole()));

        return new UserDetailsImpl(
                admin.getId(),
                admin.getEmail(),
                admin.getFullName(),
                admin.getPassword(),
                admin.isEmailVerified(),   // ✔ Admin must verify email
                true,                      // ✔ Admin is always approved
                authorities
        );
    }



    // ======================================================
    // GETTERS
    // ======================================================
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isAccountVerified() {
        return accountVerified;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getRole() {
        return authorities.stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

    // ======================================================
    // Spring Security Account Status Logic
    // ======================================================
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;   // if you later add locked flag
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // IMPORTANT — LOGIN RESTRICTIONS
    @Override
    public boolean isEnabled() {

        String role = getRole();

        if ("ADMIN".equals(role)) {
            return true; // admin always allowed unless locked system wide
        }

        if ("USER".equals(role)) {
            return emailVerified; // MUST verify email
        }

        if ("SELLER".equals(role)) {
            return emailVerified && accountVerified; // MUST verify email + admin approval
        }

        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetailsImpl)) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id);
    }
}