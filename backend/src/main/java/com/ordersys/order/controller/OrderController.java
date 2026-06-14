package com.ordersys.order.controller;

import com.ordersys.common.Result;
import com.ordersys.order.builder.*;
import com.ordersys.order.entity.Order;
import com.ordersys.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 下单接口。
     * 请求体示例：
     * {
     *   "remark": "不要辣",
     *   "items": [
     *     { "dishId":1, "dishName":"红烧肉盖饭", "price":28.0,
     *       "quantity":1, "size":"LARGE", "extras":["加辣","加蛋"] }
     *   ]
     * }
     */
    @PostMapping
    public Result<Order> createOrder(@RequestParam Long userId,
                                     @RequestBody Map<String, Object> body) {
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
