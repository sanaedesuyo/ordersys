package com.ordersys.order.state;

import com.ordersys.order.entity.Order;

/** 状态模式 — 已支付/待接单：允许 accept() 和 cancel() */
public class PaidState implements OrderState {
    @Override public void accept(Order order) { order.setState(new PreparingState()); }
    @Override public void cancel(Order order) { order.setState(new CancelledState()); }
    @Override public String getStatusName()   { return "PAID"; }
}
