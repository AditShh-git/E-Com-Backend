package com.one.aim.rq;

import lombok.Data;

@Data
public class UserFilterRequest {
    private Boolean active;     // filter active/inactive
    private Boolean emailVerified;
    private String keyword;     // search name/email/phone
    private String fromDate;    // yyyy-MM-dd
    private String toDate;      // yyyy-MM-dd
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "DESC";
}

