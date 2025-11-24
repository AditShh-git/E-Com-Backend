package com.one.utils;

import java.io.Serializable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.one.aim.rs.UserRs;
import com.one.service.impl.UserDetailsImpl;

public class AuthUtils implements Serializable {

    private static final long serialVersionUID = -5948653276588477749L;

    // ==========================================================
    // Get Logged User ID
    // ==========================================================
    public static Long getLoggedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            return null;
        }

        return ((UserDetailsImpl) auth.getPrincipal()).getId();
    }

    // ==========================================================
    // Get Logged User Email
    // ==========================================================
    public static String getLoggedUserEmail() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            return null;
        }

        return ((UserDetailsImpl) auth.getPrincipal()).getUsername();
    }

    // ==========================================================
    // Get Logged User Role
    // ==========================================================
    public static String getLoggedUserRole() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            return null;
        }

        return ((UserDetailsImpl) auth.getPrincipal()).getRole();
    }

    // ==========================================================
    // Get Logged User Details → UserRs (used everywhere)
    // ==========================================================
    public static UserRs findLoggedInUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return null;
        }

        UserDetailsImpl ud = (UserDetailsImpl) authentication.getPrincipal();

        UserRs rs = new UserRs();
        rs.setDocId(ud.getId());
        rs.setEmail(ud.getUsername());
        rs.setFullName(ud.getFullName());
        rs.setRoll(ud.getRole());

        return rs;
    }
}
