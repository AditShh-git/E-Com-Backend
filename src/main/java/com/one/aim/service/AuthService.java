package com.one.aim.service;

import com.one.vm.core.BaseRs;
import org.springframework.security.core.Authentication;

public interface AuthService {
    BaseRs signIn(Authentication authentication) throws Exception;
    BaseRs forgotPassword(String email);
    BaseRs resetPassword(String token, String newPassword);
    BaseRs logout() throws Exception;

    BaseRs verifyEmail(String token);

    BaseRs resendVerificationEmail(String email);
}
