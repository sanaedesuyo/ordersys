package com.ordersys.product.factory;

import com.ordersys.product.entity.Dish;

/** 工厂模式 — 饮料类型的具体产品 */
public class Beverage extends AbstractDish {

    public Beverage(String name, String description, double price) {
        super(name, description, price, DishType.BEVERAGE);
    }

    @Override
    public void validate() {
        if (getName() == null || getName().isBlank())
            throw new IllegalArgumentException("饮料名称不能为空");
        if (getPrice().doubleValue() <= 0)
            throw new IllegalArgumentException("价格必须大于0");
    }

    @Override
    public Dish toDishEntity() {
        Dish dish = new Dish();
        dish.setName(getName());
        dish.setDescription(getDescription());
        dish.setPrice(getPrice());
        dish.setType(getType().name());
        dish.setStatus(1);
        return dish;
    }
}
