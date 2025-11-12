package com.one.aim.rq;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRq {

    private String fullName;
    private String phoneNo;

    // Optional for password change
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    private String email;

    private MultipartFile image;
}
