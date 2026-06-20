package com.ordersys.product.controller;

import com.ordersys.common.Result;
import com.ordersys.product.entity.Dish;
import com.ordersys.product.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dish")
@RequiredArgsConstructor
public class AdminDishController {

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

    /** 搜索菜品（含下架），支持 keyword、type 筛选 */
    @GetMapping
    public Result<List<Dish>> listDishes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        return Result.success(dishService.search(keyword, type, false));
    }

    @GetMapping("/{id}")
    public Result<Dish> getDish(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }

    /** 修改菜品信息 */
    @PutMapping("/{id}")
    public Result<Dish> updateDish(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String type        = (String) body.get("type");
        String name        = (String) body.get("name");
        String description = (String) body.getOrDefault("description", "");
        Object priceObj    = body.get("price");
        Object statusObj   = body.get("status");
        if (type == null || name == null || priceObj == null) {
            return Result.error("type、name、price 不能为空");
        }
        double price;
        Integer status = null;
        try {
            price = Double.parseDouble(priceObj.toString());
            if (statusObj != null) {
                status = Integer.parseInt(statusObj.toString());
            }
        } catch (NumberFormatException e) {
            return Result.error("price 或 status 格式错误");
        }
        try {
            return Result.success(dishService.updateDish(id, type, name, description, price, status));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
