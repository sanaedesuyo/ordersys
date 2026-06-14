package com.ordersys.product;

import com.ordersys.product.factory.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DishFactoryTest {

    @Test
    void create_mainDish_returnsMainDishInstance() {
        AbstractDish dish = DishFactory.create(DishType.MAIN_DISH, "红烧肉盖饭", "经典红烧肉", 28.00);
        assertThat(dish).isInstanceOf(MainDish.class);
        assertThat(dish.getType()).isEqualTo(DishType.MAIN_DISH);
        assertThat(dish.getPrice().doubleValue()).isEqualTo(28.00);
    }

    @Test
    void create_beverage_returnsBeverageInstance() {
        AbstractDish dish = DishFactory.create(DishType.BEVERAGE, "珍珠奶茶", "手工奶茶", 12.00);
        assertThat(dish).isInstanceOf(Beverage.class);
        assertThat(dish.getType()).isEqualTo(DishType.BEVERAGE);
    }

    @Test
    void create_dessert_returnsDessertInstance() {
        AbstractDish dish = DishFactory.create(DishType.DESSERT, "芒果布丁", "香甜布丁", 15.00);
        assertThat(dish).isInstanceOf(Dessert.class);
        assertThat(dish.getType()).isEqualTo(DishType.DESSERT);
    }

    @Test
    void create_withZeroPrice_throwsIllegalArgument() {
        assertThatThrownBy(() -> DishFactory.create(DishType.MAIN_DISH, "测试", "描述", 0.00))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("价格");
    }

    @Test
    void create_withBlankName_throwsIllegalArgument() {
        assertThatThrownBy(() -> DishFactory.create(DishType.BEVERAGE, "", "描述", 10.00))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("名称");
    }
}
