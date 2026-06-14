package com.ordersys.order.builder;

import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

/**
 * 建造者模式 — 产品类（值对象，不映射数据库）。
 * 由 CustomDishBuilder 构建，封装用户对单个菜品的所有个性化选择。
 * 包的私有构造函数确保只能通过 Builder 创建。
 */
@Getter
public class CustomDish {
    private final Long dishId;
    private final String dishName;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final Size size;
    private final List<String> extras;
    private final String note;

    CustomDish(Long dishId, String dishName, double unitPrice, int quantity,
               Size size, List<String> extras, String note) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.unitPrice = BigDecimal.valueOf(unitPrice);
        this.quantity = quantity;
        this.size = size;
        this.extras = List.copyOf(extras);
        this.note = note;
    }

    /** 将加料列表序列化为 JSON 字符串，写入 order_item.extras 列 */
    public String extrasToJson() {
        if (extras.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < extras.size(); i++) {
            sb.append("\"").append(extras.get(i)).append("\"");
            if (i < extras.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }
}
