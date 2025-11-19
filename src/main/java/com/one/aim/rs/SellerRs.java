package com.one.aim.rs;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerRs implements Serializable {

    private static final long serialVersionUID = 1L;

    private String docId;        // DB id
    private String sellerId;   // Seller Id

    private String userName;
    private String email;
    private String phoneNo;
    private String gst;
    private String adhaar;
    private String panCard;
    private String role;
    private String imageUrl;
    private boolean verified;
    private boolean locked;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
