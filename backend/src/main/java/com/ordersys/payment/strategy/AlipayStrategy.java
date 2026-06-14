package com.ordersys.payment.strategy;

import java.math.BigDecimal;
import java.util.UUID;

/** 策略模式 — 支付宝具体策略（Mock 实现，不调用真实 API） */
public class AlipayStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(BigDecimal amount, Long orderId) {
        String txId = "ALIPAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return PaymentResult.success(txId, getMethodName());
    }

    @Override
    public String getMethodName() {
        return "ALIPAY";
    }
}
