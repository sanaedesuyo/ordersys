package com.ordersys.payment.strategy;

import java.util.UUID;

/** 策略模式 — 微信支付具体策略（Mock 实现，不调用真实 API） */
public class WechatPayStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(double amount, Long orderId) {
        String txId = "WECHAT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return PaymentResult.success(txId, getMethodName());
    }

    @Override
    public String getMethodName() {
        return "WECHAT";
    }
}
