package com.one.aim.rq;

import lombok.Data;

@Data
public class NotificationRq {
    private String receiverId;     // email or ID of admin/user/seller
    private String role;           // ADMIN / USER / SELLER
    private String title;
    private String message;
}