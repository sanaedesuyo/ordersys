package com.ordersys.payment.strategy;

import lombok.Data;

@Data
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private String method;
    private String message;

    public static PaymentResult success(String transactionId, String method) {
        PaymentResult r = new PaymentResult();
        r.success = true;
        r.transactionId = transactionId;
        r.method = method;
        r.message = "支付成功";
        return r;
    }

    public static PaymentResult fail(String method, String message) {
        PaymentResult r = new PaymentResult();
        r.success = false;
        r.method = method;
        r.message = message;
        return r;
    }
}
