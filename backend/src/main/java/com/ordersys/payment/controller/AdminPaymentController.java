package com.ordersys.payment.controller;

import com.ordersys.common.Result;
import com.ordersys.payment.entity.Payment;
import com.ordersys.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payment")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    /** 查询指定订单的支付记录（管理员全量） */
    @GetMapping("/order/{orderId}")
    public Result<List<Payment>> getByOrderId(@PathVariable Long orderId) {
        return Result.success(paymentService.getByOrderId(orderId));
    }
}
