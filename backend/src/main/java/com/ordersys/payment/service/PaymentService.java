package com.ordersys.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ordersys.order.service.OrderService;
import com.ordersys.payment.entity.Payment;
import com.ordersys.payment.mapper.PaymentMapper;
import com.ordersys.payment.strategy.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {

    private final OrderService orderService;

    /**
     * 策略模式入口：根据 method 选择支付策略，完成支付后触发订单状态迁移。
     */
    @Transactional
    public Payment pay(Long orderId, BigDecimal amount, String method) {
        PaymentStrategy strategy = switch (method.toUpperCase()) {
            case "WECHAT" -> new WechatPayStrategy();
            case "ALIPAY" -> new AlipayStrategy();
            default -> throw new IllegalArgumentException("不支持的支付方式: " + method);
        };

        PaymentContext ctx = new PaymentContext(strategy);
        PaymentResult result = ctx.pay(amount, orderId);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setMethod(method.toUpperCase());
        payment.setTransactionId(result.getTransactionId());
        payment.setStatus(result.isSuccess() ? "SUCCESS" : "FAILED");
        save(payment);

        if (result.isSuccess()) {
            orderService.markPaid(orderId);
        }

        return payment;
    }

    public List<Payment> getByOrderId(Long orderId) {
        return list(new LambdaQueryWrapper<Payment>().eq(Payment::getOrderId, orderId));
    }
}
