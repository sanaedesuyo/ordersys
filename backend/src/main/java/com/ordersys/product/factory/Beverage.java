package com.ordersys.product.factory;

/** 工厂模式 — 饮料类型的具体产品 */
public class Beverage extends AbstractDish {
    public Beverage(String name, String description, double price) {
        super(name, description, price, DishType.BEVERAGE);
    }
}
