package com.one.aim.rs;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.one.aim.bo.UserBO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRs implements Serializable {

    private static final long serialVersionUID = 1L;

    private String docId;
    private String orderId;

    private Long totalAmount;
    private String paymentMethod;      
    private String paymentStatus;
    private LocalDateTime orderTime;
    private UserRs user;
    private List<CartRs> orderedItems;
    private String orderStatus;
}

