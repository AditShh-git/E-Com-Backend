package com.one.aim.rq;

import lombok.Data;

@Data
public class SellerFilterRequest {
    private Boolean verified;    // filter by admin approval
    private Boolean locked;      // filter by lock status
    private String keyword;      // search name/email/phone
    private String fromDate;     // yyyy-MM-dd
    private String toDate;       // yyyy-MM-dd
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "DESC";
}

