package com.ordersys.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordersys.order.builder.CustomDish;
import com.ordersys.order.entity.Order;
import com.ordersys.order.entity.OrderItem;
import com.ordersys.order.mapper.OrderItemMapper;
import com.ordersys.order.mapper.OrderMapper;
import com.ordersys.order.state.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    /**
     * 创建订单，整合建造者模式（CustomDish 列表）和状态模式（初始状态 PendingPayment）。
     */
    @Transactional
    public Order createOrder(Long userId, List<CustomDish> dishes, String remark) {
        BigDecimal total = dishes.stream()
            .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setRemark(remark);
        order.setState(new PendingPaymentState());
        orderMapper.insert(order);

        for (CustomDish dish : dishes) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setDishId(dish.getDishId());
            item.setDishName(dish.getDishName());
            item.setQuantity(dish.getQuantity());
            item.setUnitPrice(dish.getUnitPrice());
            item.setSize(dish.getSize().name());
            item.setExtras(dish.extrasToJson());
            orderItemMapper.insert(item);
        }
        return order;
    }

    /** 从数据库加载订单并恢复状态对象（状态模式要求） */
    public Order getOrderWithState(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new IllegalArgumentException("订单不存在: " + orderId);
        order.setState(resolveState(order.getStatus()));
        return order;
    }

    private OrderState resolveState(String status) {
        return switch (status) {
            case "PENDING_PAYMENT" -> new PendingPaymentState();
            case "PAID"            -> new PaidState();
            case "PREPARING"       -> new PreparingState();
            case "DELIVERING"      -> new DeliveringState();
            case "COMPLETED"       -> new CompletedState();
            case "CANCELLED"       -> new CancelledState();
            default -> throw new IllegalArgumentException("未知状态: " + status);
        };
    }

    /** 商家操作：接单 / 开始配送 / 完成 / 取消，由状态对象处理迁移 */
    @Transactional
    public Order transition(Long orderId, String action) {
        Order order = getOrderWithState(orderId);
        switch (action) {
            case "accept"   -> order.getState().accept(order);
            case "deliver"  -> order.getState().startDelivery(order);
            case "complete" -> order.getState().complete(order);
            case "cancel"   -> order.getState().cancel(order);
            default -> throw new IllegalArgumentException("未知操作: " + action);
        }
        orderMapper.updateById(order);
        return order;
    }

    /** 支付成功后由 PaymentService 调用，触发 PendingPayment → Paid 的状态迁移 */
    @Transactional
    public Order markPaid(Long orderId) {
        Order order = getOrderWithState(orderId);
        order.getState().pay(order);
        orderMapper.updateById(order);
        return order;
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }

    public List<Order> listOrders(String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
            .orderByDesc(Order::getCreateTime);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Order::getStatus, status);
        }
        return orderMapper.selectList(wrapper);
    }

    public List<Order> listOrdersByUser(Long userId, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
            .eq(Order::getUserId, userId)
            .orderByDesc(Order::getCreateTime);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Order::getStatus, status);
        }
        return orderMapper.selectList(wrapper);
    }
}
