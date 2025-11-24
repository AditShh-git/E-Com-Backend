package com.one.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.one.aim.bo.AdminBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.UserBO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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

    private boolean emailVerified;     // email must be verified
    private boolean accountVerified;   // USER: active+not deleted, SELLER: not locked, ADMIN: always true

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
                Boolean.TRUE.equals(user.getEmailVerified()),
                Boolean.TRUE.equals(user.getActive()) &&
                        Boolean.FALSE.equals(user.getDeleted()),
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
                seller.isEmailVerified(),
                !seller.isLocked(),
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
                admin.isEmailVerified(),
                true,     // admin accounts are always considered verified
                roles
        );
    }

    // ======================================================
    // ROLE HELPER
    // ======================================================
    public String getRole() {
        return authorities.stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

    // ======================================================
    // SPRING SECURITY CHECKS
    // ======================================================
    @Override
    public boolean isEnabled() {

        String role = getRole();

        if ("ADMIN".equals(role)) {
            return emailVerified;
        }

        if ("SELLER".equals(role)) {
            return emailVerified && accountVerified;
        }

        if ("USER".equals(role)) {
            return emailVerified && accountVerified;
        }

        return true;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() {

        String role = getRole();

        if ("ADMIN".equals(role)) return true;
        if ("SELLER".equals(role)) return accountVerified;  // seller locked = accountVerified = false
        if ("USER".equals(role)) return true;               // users are not “locked”

        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserDetailsImpl)) return false;
        return Objects.equals(id, ((UserDetailsImpl) obj).getId());
    }
}
