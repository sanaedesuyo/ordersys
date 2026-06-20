package com.ordersys.product.factory;

/** 工厂模式 — 汤品类型的具体产品 */
public class Soup extends AbstractDish {
    public Soup(String name, String description, double price) {
        super(name, description, price, DishType.SOUP);
    }
}
