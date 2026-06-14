package com.ordersys.product.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ordersys.product.entity.Dish;
import com.ordersys.product.factory.AbstractDish;
import com.ordersys.product.factory.DishFactory;
import com.ordersys.product.factory.DishType;
import com.ordersys.product.mapper.DishMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DishService extends ServiceImpl<DishMapper, Dish> {

    public Dish createDish(String type, String name, String description, double price) {
        AbstractDish abstractDish = DishFactory.create(DishType.valueOf(type), name, description, price);
        Dish dish = abstractDish.toDishEntity();
        this.baseMapper.insert(dish);
        return dish;
    }

    public List<Dish> listAvailable() {
        return this.lambdaQuery().eq(Dish::getStatus, 1).list();
    }
}
