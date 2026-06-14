package com.ordersys.order.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * 建造者模式 — 建造者类。
 * 提供流式 API，让调用方灵活组合规格（大/小份）和加料；
 * build() 最终生成不可变的 CustomDish 对象。
 *
 * 使用示例：
 *   new CustomDishBuilder(dishId, dishName, price)
 *       .size(Size.LARGE)
 *       .addExtra("加辣")
 *       .note("不要香菜")
 *       .build();
 */
public class CustomDishBuilder {
    private final Long dishId;
    private final String dishName;
    private final double unitPrice;
    private int quantity = 1;
    private Size size = Size.LARGE;
    private final List<String> extras = new ArrayList<>();
    private String note;

    public CustomDishBuilder(Long dishId, String dishName, double unitPrice) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.unitPrice = unitPrice;
    }

    public CustomDishBuilder quantity(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("quantity 必须 >= 1，实际: " + quantity);
        this.quantity = quantity;
        return this;
    }

    public CustomDishBuilder size(Size size) {
        this.size = size;
        return this;
    }

    public CustomDishBuilder addExtra(String extra) {
        if (extra == null) throw new IllegalArgumentException("extra 不能为 null");
        this.extras.add(extra);
        return this;
    }

    public CustomDishBuilder note(String note) {
        this.note = note;
        return this;
    }

    public CustomDish build() {
        return new CustomDish(dishId, dishName, unitPrice, quantity, size, extras, note);
    }
}
