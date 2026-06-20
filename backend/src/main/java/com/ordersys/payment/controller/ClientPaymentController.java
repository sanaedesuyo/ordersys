package com.ordersys.payment.controller;

import com.ordersys.common.Result;
import com.ordersys.order.entity.Order;
import com.ordersys.order.service.OrderService;
import com.ordersys.payment.entity.Payment;
import com.ordersys.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/payment")
@RequiredArgsConstructor
public class ClientPaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    /**
     * 发起支付，校验订单归属
     * Body: { "orderId": 1, "amount": 68.00, "method": "WECHAT" | "ALIPAY" }
     */
    @PostMapping
    public Result<Payment> pay(@RequestBody Map<String, Object> body, Authentication auth) {
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

        Long userId = (Long) auth.getPrincipal();
        Order order = orderService.getOrderWithState(orderId);
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权支付该订单");
        }

        try {
            return Result.success(paymentService.pay(orderId, amount, methodObj.toString()));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 查询当前用户某订单的支付记录 */
    @GetMapping("/order/{orderId}")
    public Result<List<Payment>> getByOrderId(@PathVariable Long orderId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Order order = orderService.getOrderWithState(orderId);
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权查看该支付记录");
        }
        return Result.success(paymentService.getByOrderId(orderId));
    }
}
