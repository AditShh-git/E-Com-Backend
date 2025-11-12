package com.one.aim.rq;

import com.one.vm.core.BaseVM;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class SellerRq extends BaseVM {

    private static final long serialVersionUID = 1L;

    private String docId;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be under 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be under 100 characters")
    private String email;

    @Size(max = 15, message = "Phone number must be under 15 characters")
    private String phoneNo;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Size(max = 20, message = "GST number must be under 20 characters")
    private String gst;

    @Size(max = 20, message = "Aadhaar must be under 20 characters")
    private String adhaar;

    @Size(max = 20, message = "PAN Card must be under 20 characters")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format")
    private String panCard;

    private String isVerified;


    private MultipartFile image;
}
