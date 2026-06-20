package com.ordersys.product.factory;

/** 工厂模式 — 小吃类型的具体产品 */
public class Snack extends AbstractDish {
    public Snack(String name, String description, double price) {
        super(name, description, price, DishType.SNACK);
    }
}
