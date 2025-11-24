package com.one.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.SellerBO;
import lombok.*;
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

    private boolean emailVerified;     // must verify email
    private boolean accountVerified;   // seller: not locked, user: active+not deleted

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
                !seller.isLocked(),      // seller must NOT be locked
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
                true,
                roles
        );
    }

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

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() {

        String role = getRole();

        if ("ADMIN".equals(role)) return true;
        if ("SELLER".equals(role)) return accountVerified; // seller locked check
        if ("USER".equals(role)) return true;              // user never locked

        return true;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override public String getUsername() { return email; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserDetailsImpl)) return false;
        return Objects.equals(id, ((UserDetailsImpl) obj).getId());
    }
}
