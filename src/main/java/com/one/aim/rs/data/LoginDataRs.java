package com.one.aim.rs.data;

import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDataRs extends BaseDataRs {

    private static final long serialVersionUID = 9207579984347254263L;

    private String accessToken;
    private String refreshToken;

    private Long empId;      // logged-in entity ID (user/admin)
    private String sellerId;
    private String username; // usually email (for backward compatibility)
    private String fullname; // full name
    private String email;    // actual email
    private String role;     // USER / SELLER / ADMIN

    public LoginDataRs(String message,
                       String accessToken,
                       String refreshToken,
                       Long empId,
                       String email,
                       String fullname,
                       String role) {
        super(message);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.empId = empId;
        this.username = email;   // keep as email (same as before)
        this.fullname = fullname;
        this.email = email;
        this.role = role;
    }

    public LoginDataRs(String message) {
        super(message);
    }
}
