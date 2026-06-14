package com.ordersys.product.factory;

/** 工厂模式 — 甜点类型的具体产品 */
public class Dessert extends AbstractDish {
    public Dessert(String name, String description, double price) {
        super(name, description, price, DishType.DESSERT);
    }
}
