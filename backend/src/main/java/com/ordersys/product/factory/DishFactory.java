package com.ordersys.product.factory;

/**
 * 工厂模式 — 工厂类（静态工厂方法）。
 * 调用方只需传入 DishType，无需知道具体子类；
 * 工厂负责实例化并调用 validate() 确保对象合法。
 */
public class DishFactory {

    private DishFactory() {}

    public static AbstractDish create(DishType type, String name, String description, double price) {
        AbstractDish dish = switch (type) {
            case MAIN_DISH  -> new MainDish(name, description, price);
            case BEVERAGE   -> new Beverage(name, description, price);
            case DESSERT    -> new Dessert(name, description, price);
            case SNACK      -> new Snack(name, description, price);
            case SIDE_DISH  -> new SideDish(name, description, price);
            case SOUP       -> new Soup(name, description, price);
        };
        dish.validate();
        return dish;
    }
}
