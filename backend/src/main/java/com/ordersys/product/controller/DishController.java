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
        double price       = Double.parseDouble(body.get("price").toString());
        return Result.success(dishService.createDish(type, name, description, price));
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
