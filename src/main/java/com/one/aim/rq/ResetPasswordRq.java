package com.one.aim.rq;

import lombok.Data;

@Data
public class ResetPasswordRq {
    private String token;
    private String newPassword;
}

