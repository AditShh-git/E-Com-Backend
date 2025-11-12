package com.one.aim.rs;

import java.io.Serializable;
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

    private String docId;
    private String userName;
    private String email;
    private String phoneNo;
    private String gst;
    private String adhaar;
    private String panCard;
    private boolean isVerified;
    private String role;
    private byte[] image;
    private String imageUrl;

    public SellerRs(String userName, String docId) {
        this.userName = userName;
        this.docId = docId;
    }

}