package com.ordersys.order.controller;

import com.ordersys.common.Result;
import com.ordersys.order.builder.*;
import com.ordersys.order.entity.Order;
import com.ordersys.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/order")
@RequiredArgsConstructor
public class ClientOrderController {

    private final OrderService orderService;

    /** 下单：userId 从当前登录 Token 中取 */
    @PostMapping
    public Result<Order> createOrder(@RequestBody Map<String, Object> body,
                                     Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String remark = (String) body.getOrDefault("remark", "");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items == null || items.isEmpty()) {
            return Result.error("订单中至少需要一个菜品");
        }

        List<CustomDish> dishes = items.stream().map(item -> {
            Object dishIdObj = item.get("dishId");
            Object nameObj   = item.get("dishName");
            Object priceObj  = item.get("price");
            if (dishIdObj == null || nameObj == null || priceObj == null) {
                throw new IllegalArgumentException("订单项缺少必填字段: dishId、dishName、price");
            }
            Long dishId  = Long.parseLong(dishIdObj.toString());
            String name  = nameObj.toString();
            double price = Double.parseDouble(priceObj.toString());
            int qty      = Integer.parseInt(item.getOrDefault("quantity", 1).toString());
            Size size    = Size.valueOf(((String) item.getOrDefault("size", "LARGE")).toUpperCase());

            CustomDishBuilder builder = new CustomDishBuilder(dishId, name, price)
                .quantity(qty).size(size);

            Object extrasObj = item.get("extras");
            if (extrasObj instanceof List<?> extrasList) {
                extrasList.forEach(e -> builder.addExtra(e.toString()));
            }
            if (item.containsKey("note")) builder.note((String) item.get("note"));
            return builder.build();
        }).toList();

        return Result.success(orderService.createOrder(userId, dishes, remark));
    }

    /** 查询当前用户的订单列表 */
    @GetMapping
    public Result<List<Order>> myOrders(@RequestParam(required = false) String status,
                                        Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(orderService.listOrdersByUser(userId, status));
    }

    /** 查询单条订单（校验归属） */
    @GetMapping("/{id}")
    public Result<Order> getOrder(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Order order = orderService.getOrderWithState(id);
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权访问该订单");
        }
        return Result.success(order);
    }

    /** 用户取消订单 */
    @PutMapping("/{id}/cancel")
    public Result<Order> cancel(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Order order = orderService.getOrderWithState(id);
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权操作该订单");
        }
        return Result.success(orderService.transition(id, "cancel"));
    }
}
