package com.ordersys.product.controller;

import com.ordersys.common.Result;
import com.ordersys.product.entity.Dish;
import com.ordersys.product.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/dish")
@RequiredArgsConstructor
public class ClientDishController {

    private final DishService dishService;

    /** 公开接口：浏览上架菜品，支持 keyword、type 搜索 */
    @GetMapping
    public Result<List<Dish>> listDishes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        return Result.success(dishService.search(keyword, type, true));
    }

    @GetMapping("/{id}")
    public Result<Dish> getDish(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }
}
