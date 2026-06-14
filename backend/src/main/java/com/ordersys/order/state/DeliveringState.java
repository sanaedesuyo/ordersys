package com.ordersys.order.state;

import com.ordersys.order.entity.Order;

/** 状态模式 — 配送中：允许 complete() 和 cancel() */
public class DeliveringState implements OrderState {
    @Override public void complete(Order order) { order.setState(new CompletedState()); }
    @Override public void cancel(Order order)   { order.setState(new CancelledState()); }
    @Override public String getStatusName()     { return "DELIVERING"; }
}
