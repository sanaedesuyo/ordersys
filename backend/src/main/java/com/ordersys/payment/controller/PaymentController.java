package com.ordersys.payment.controller;

import com.ordersys.common.Result;
import com.ordersys.payment.entity.Payment;
import com.ordersys.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/payment
     * Body: { "orderId": 1, "amount": 68.00, "method": "WECHAT" | "ALIPAY" }
     */
    @PostMapping
    public Result<Payment> pay(@RequestBody Map<String, Object> body) {
        Object orderIdObj = body.get("orderId");
        Object amountObj  = body.get("amount");
        Object methodObj  = body.get("method");
        if (orderIdObj == null || amountObj == null || methodObj == null) {
            return Result.error("orderId、amount、method 均为必填项");
        }
        Long orderId;
        BigDecimal amount;
        try {
            orderId = Long.parseLong(orderIdObj.toString());
            amount  = new BigDecimal(amountObj.toString());
        } catch (NumberFormatException e) {
            return Result.error("orderId 或 amount 格式错误");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("支付金额必须大于0");
        }
        try {
            Payment payment = paymentService.pay(orderId, amount, methodObj.toString());
            return Result.success(payment);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * GET /api/payment/order/{orderId}
     * 查询某订单的支付记录列表。
     */
    @GetMapping("/order/{orderId}")
    public Result<List<Payment>> getByOrderId(@PathVariable Long orderId) {
        return Result.success(paymentService.getByOrderId(orderId));
    }
}
