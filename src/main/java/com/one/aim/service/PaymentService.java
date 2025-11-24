package com.one.aim.service;

import com.one.aim.rq.CancelPaymentRq;
import com.one.aim.rq.CreatePaymentRq;
import com.one.aim.rq.PaymentRq;
import com.one.aim.rq.VerifyPaymentRq;
import com.one.vm.core.BaseRs;

public interface PaymentService {

    /**
     * 1) Create Razorpay Order
     * Used for ONLINE payment (UPI / CARD)
     */
    BaseRs createRazorpayOrder(CreatePaymentRq rq) throws Exception;

    /**
     * 2) Verify Razorpay Payment Signature
     * Called after user finishes payment in Razorpay popup
     */
    BaseRs verifyRazorpayPayment(VerifyPaymentRq rq) throws Exception;

    BaseRs cancelPayment(CancelPaymentRq rq) throws Exception;

}
