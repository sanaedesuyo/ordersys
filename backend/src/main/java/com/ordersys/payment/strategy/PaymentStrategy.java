package com.ordersys.payment.strategy;

import java.math.BigDecimal;

/**
 * 策略模式 — 策略接口。
 * 定义统一的支付行为契约；每种支付方式实现此接口，
 * 让 PaymentContext 可以在运行时动态切换支付方式。
 */
public interface PaymentStrategy {
    PaymentResult pay(BigDecimal amount, Long orderId);
    String getMethodName();
}
