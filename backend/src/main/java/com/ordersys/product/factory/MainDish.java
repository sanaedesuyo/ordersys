package com.ordersys.product.factory;

/** 工厂模式 — 主食类型的具体产品 */
public class MainDish extends AbstractDish {
    public MainDish(String name, String description, double price) {
        super(name, description, price, DishType.MAIN_DISH);
    }
}
