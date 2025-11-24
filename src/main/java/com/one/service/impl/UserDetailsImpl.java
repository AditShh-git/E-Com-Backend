package com.one.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.SellerBO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.one.aim.bo.UserBO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String email;
    private String fullName;

    @JsonIgnore
    private String password;

    private boolean emailVerified;      // must verify email
    private boolean accountVerified;    // for seller = !locked, for user = active

    private Collection<? extends GrantedAuthority> authorities;

    // ======================================================
    // BUILD FOR USER
    // ======================================================
    public static UserDetailsImpl build(UserBO user) {

        List<GrantedAuthority> roles =
                List.of(new SimpleGrantedAuthority(user.getRole()));

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPassword(),
                user.getEmailVerified(),   // must verify email
                user.isActive(),           // must be active
                roles
        );
    }

    // ======================================================
    // BUILD FOR SELLER
    // ======================================================
    public static UserDetailsImpl build(SellerBO seller) {

        List<GrantedAuthority> roles =
                List.of(new SimpleGrantedAuthority(seller.getRole()));

        return new UserDetailsImpl(
                seller.getId(),
                seller.getEmail(),
                seller.getFullName(),
                seller.getPassword(),
                seller.isEmailVerified(),     // must verify email
                !seller.isLocked(),           // seller must NOT be locked
                roles
        );
    }

    // ======================================================
    // BUILD FOR ADMIN
    // ======================================================
    public static UserDetailsImpl build(AdminBO admin) {

        List<GrantedAuthority> roles =
                List.of(new SimpleGrantedAuthority(admin.getRole()));

        return new UserDetailsImpl(
                admin.getId(),
                admin.getEmail(),
                admin.getFullName(),
                admin.getPassword(),
                admin.isEmailVerified(),   // must verify email
                true,                      // admin always active
                roles
        );
    }

    // ======================================================
    // ROLE HELPERS
    // ======================================================
    public String getRole() {
        return authorities.stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

    // ======================================================
    // SPRING SECURITY OVERRIDES
    // ======================================================
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // 👇 IMPORTANT: uses our computed value
    @Override
    public boolean isAccountNonLocked() {
        return accountVerified;   // user active, seller not locked, admin auto true
    }

    // 👇 IMPORTANT: LOGIN RULES
    @Override
    public boolean isEnabled() {

        String role = getRole();

        if ("ADMIN".equals(role)) {
            return emailVerified;    // email must be verified
        }

        if ("USER".equals(role)) {
            return emailVerified && accountVerified;
        }

        if ("SELLER".equals(role)) {
            return emailVerified && accountVerified;
            // email verified + not locked
            // admin approval NOT required here (checked elsewhere)
        }

        return true;
    }

    // ======================================================
    // BASIC EQUALS
    // ======================================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetailsImpl)) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id);
    }
}
