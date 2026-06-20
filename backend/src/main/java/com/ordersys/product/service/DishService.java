package com.ordersys.product.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ordersys.product.entity.Dish;
import com.ordersys.product.factory.AbstractDish;
import com.ordersys.product.factory.DishFactory;
import com.ordersys.product.factory.DishType;
import com.ordersys.product.mapper.DishMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DishService extends ServiceImpl<DishMapper, Dish> {

    public Dish createDish(String type, String name, String description, double price) {
        DishType dishType = parseType(type);
        AbstractDish abstractDish = DishFactory.create(dishType, name, description, price);
        Dish dish = abstractDish.toDishEntity();
        this.baseMapper.insert(dish);
        return dish;
    }

    public Dish updateDish(Long id, String type, String name, String description,
                           double price, Integer status) {
        Dish dish = getById(id);
        if (dish == null) {
            throw new IllegalArgumentException("菜品不存在: " + id);
        }
        DishType dishType = parseType(type);
        DishFactory.create(dishType, name, description, price);

        dish.setName(name);
        dish.setDescription(description);
        dish.setPrice(BigDecimal.valueOf(price));
        dish.setType(dishType.name());
        if (status != null) {
            dish.setStatus(status);
        }
        updateById(dish);
        return dish;
    }

    public List<Dish> listAvailable() {
        return search(null, null, true);
    }

    public List<Dish> search(String keyword, String type, boolean availableOnly) {
        var query = lambdaQuery();
        if (availableOnly) {
            query.eq(Dish::getStatus, 1);
        }
        if (StringUtils.hasText(type)) {
            query.eq(Dish::getType, type.toUpperCase());
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            query.and(w -> w.like(Dish::getName, kw).or().like(Dish::getDescription, kw));
        }
        return query.orderByDesc(Dish::getCreateTime).list();
    }

    private DishType parseType(String type) {
        try {
            return DishType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支持的菜品类型: " + type);
        }
    }
}
