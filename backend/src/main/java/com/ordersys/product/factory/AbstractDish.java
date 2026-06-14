package com.ordersys.product.factory;

import com.ordersys.product.entity.Dish;
import lombok.Getter;
import java.math.BigDecimal;

/**
 * 工厂模式 — 抽象产品。
 * 定义所有菜品类型的公共字段和行为契约；
 * 子类（MainDish / Beverage / Dessert）通过继承获得校验与实体转换能力。
 */
@Getter
public abstract class AbstractDish {
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final DishType type;

    protected AbstractDish(String name, String description, double price, DishType type) {
        this.name = name;
        this.description = description;
        this.price = BigDecimal.valueOf(price);
        this.type = type;
    }

    public void validate() {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException(type.name() + " 名称不能为空");
        if (price.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("价格必须大于0");
    }

    public Dish toDishEntity() {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setDescription(description);
        dish.setPrice(price);
        dish.setType(type.name());
        dish.setStatus(1);
        return dish;
    }
}
