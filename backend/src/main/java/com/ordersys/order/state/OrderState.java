package com.ordersys.order.state;

import com.ordersys.order.entity.Order;

/**
 * 状态模式 — 状态接口。
 * 每个状态实现类决定当前状态下哪些操作合法，并负责迁移到下一状态。
 * Order 实体持有 OrderState 引用并委托给它，自身不含任何 if-else/switch。
 * default 实现统一抛出 IllegalStateException，子类只重写允许的操作。
 */
public interface OrderState {

    default void pay(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许支付操作");
    }

    default void accept(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许接单操作");
    }

    default void startDelivery(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许开始配送操作");
    }

    default void complete(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许完成操作");
    }

    default void cancel(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许取消操作");
    }

    String getStatusName();
}
