package com.ordersys.product.controller;

import com.ordersys.common.Result;
import com.ordersys.product.entity.Dish;
import com.ordersys.product.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @PostMapping
    public Result<Dish> createDish(@RequestBody Map<String, Object> body) {
        String type        = (String) body.get("type");
        String name        = (String) body.get("name");
        String description = (String) body.getOrDefault("description", "");
        Object priceObj    = body.get("price");
        if (type == null || name == null || priceObj == null) {
            return Result.error("type、name、price 不能为空");
        }
        double price;
        try {
            price = Double.parseDouble(priceObj.toString());
        } catch (NumberFormatException e) {
            return Result.error("price 格式错误");
        }
        try {
            return Result.success(dishService.createDish(type, name, description, price));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<List<Dish>> listDishes() {
        return Result.success(dishService.listAvailable());
    }

    @GetMapping("/{id}")
    public Result<Dish> getDish(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }
}
