package com.ordersys.product.factory;

/** 工厂模式 — 配菜类型的具体产品 */
public class SideDish extends AbstractDish {
    public SideDish(String name, String description, double price) {
        super(name, description, price, DishType.SIDE_DISH);
    }
}
