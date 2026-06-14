package com.ordersys.order;

import com.ordersys.order.builder.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CustomDishBuilderTest {

    @Test
    void build_withAllOptions_producesCorrectCustomDish() {
        CustomDish dish = new CustomDishBuilder(1L, "红烧肉盖饭", 28.00)
            .size(Size.LARGE)
            .addExtra("加辣")
            .addExtra("加蛋")
            .note("不要香菜")
            .build();

        assertThat(dish.getDishId()).isEqualTo(1L);
        assertThat(dish.getDishName()).isEqualTo("红烧肉盖饭");
        assertThat(dish.getSize()).isEqualTo(Size.LARGE);
        assertThat(dish.getExtras()).containsExactly("加辣", "加蛋");
        assertThat(dish.getNote()).isEqualTo("不要香菜");
        assertThat(dish.getUnitPrice().doubleValue()).isEqualTo(28.00);
    }

    @Test
    void build_defaults_areLargeWithNoExtras() {
        CustomDish dish = new CustomDishBuilder(2L, "珍珠奶茶", 12.00).build();

        assertThat(dish.getSize()).isEqualTo(Size.LARGE);
        assertThat(dish.getExtras()).isEmpty();
        assertThat(dish.getNote()).isNull();
        assertThat(dish.getQuantity()).isEqualTo(1);
    }

    @Test
    void extrasToJson_producesValidJsonArray() {
        CustomDish dish = new CustomDishBuilder(3L, "珍珠奶茶", 12.00)
            .addExtra("去冰")
            .addExtra("少糖")
            .build();

        assertThat(dish.extrasToJson()).isEqualTo("[\"去冰\",\"少糖\"]");
    }

    @Test
    void extrasToJson_whenEmpty_returnsEmptyArray() {
        CustomDish dish = new CustomDishBuilder(3L, "西瓜汁", 10.00).build();
        assertThat(dish.extrasToJson()).isEqualTo("[]");
    }
}
