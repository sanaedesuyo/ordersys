package com.ordersys.order.controller;

import com.ordersys.common.Result;
import com.ordersys.order.entity.Order;
import com.ordersys.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /** 查看全部订单，可按状态筛选 */
    @GetMapping
    public Result<List<Order>> listOrders(@RequestParam(required = false) String status) {
        return Result.success(orderService.listOrders(status));
    }

    @GetMapping("/{id}")
    public Result<Order> getOrder(@PathVariable Long id) {
        return Result.success(orderService.getOrderWithState(id));
    }

    @PutMapping("/{id}/accept")
    public Result<Order> accept(@PathVariable Long id) {
        return Result.success(orderService.transition(id, "accept"));
    }

    @PutMapping("/{id}/deliver")
    public Result<Order> deliver(@PathVariable Long id) {
        return Result.success(orderService.transition(id, "deliver"));
    }

    @PutMapping("/{id}/complete")
    public Result<Order> complete(@PathVariable Long id) {
        return Result.success(orderService.transition(id, "complete"));
    }

    @PutMapping("/{id}/cancel")
    public Result<Order> cancel(@PathVariable Long id) {
        return Result.success(orderService.transition(id, "cancel"));
    }
}
