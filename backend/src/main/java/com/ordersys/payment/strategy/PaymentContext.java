package com.ordersys.payment.strategy;

/**
 * 策略模式 — 上下文类。
 * 持有 PaymentStrategy 引用，对调用方屏蔽具体支付实现；
 * 通过 setStrategy() 在运行时动态切换支付方式。
 */
public class PaymentContext {
    private PaymentStrategy strategy;

    public PaymentContext(PaymentStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("strategy 不能为 null");
        this.strategy = strategy;
    }

    public void setStrategy(PaymentStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("strategy 不能为 null");
        this.strategy = strategy;
    }

    public PaymentResult pay(java.math.BigDecimal amount, Long orderId) {
        return strategy.pay(amount, orderId);
    }
}
