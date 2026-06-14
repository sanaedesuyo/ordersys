package com.ordersys.order;

import com.ordersys.order.builder.*;
import com.ordersys.order.entity.Order;
import com.ordersys.order.entity.OrderItem;
import com.ordersys.order.mapper.OrderItemMapper;
import com.ordersys.order.mapper.OrderMapper;
import com.ordersys.order.service.OrderService;
import com.ordersys.order.state.PendingPaymentState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_setsStatusToPendingPaymentAndCalculatesTotal() {
        List<CustomDish> dishes = List.of(
            new CustomDishBuilder(1L, "红烧肉盖饭", 28.00).quantity(2).size(Size.LARGE).addExtra("加辣").build(),
            new CustomDishBuilder(3L, "珍珠奶茶",   12.00).size(Size.SMALL).build()
        );

        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);

        Order order = orderService.createOrder(1L, dishes, "测试备注");

        assertThat(order.getStatus()).isEqualTo("PENDING_PAYMENT");
        assertThat(order.getState()).isInstanceOf(PendingPaymentState.class);
        // 2 × 28 + 1 × 12 = 68
        assertThat(order.getTotalAmount().doubleValue()).isEqualTo(68.00);
        verify(orderItemMapper, times(2)).insert(any(OrderItem.class));
    }
}
