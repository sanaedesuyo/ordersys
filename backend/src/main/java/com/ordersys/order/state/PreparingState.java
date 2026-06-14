package com.ordersys.order.state;

import com.ordersys.order.entity.Order;

/** 状态模式 — 制作中：允许 startDelivery() 和 cancel() */
public class PreparingState implements OrderState {
    @Override public void startDelivery(Order order) { order.setState(new DeliveringState()); }
    @Override public void cancel(Order order)        { order.setState(new CancelledState()); }
    @Override public String getStatusName()          { return "PREPARING"; }
}
