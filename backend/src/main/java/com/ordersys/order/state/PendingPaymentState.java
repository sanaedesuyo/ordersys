package com.ordersys.order.state;

import com.ordersys.order.entity.Order;

/** 状态模式 — 待支付：允许 pay() 和 cancel() */
public class PendingPaymentState implements OrderState {
    @Override public void pay(Order order)    { order.setState(new PaidState()); }
    @Override public void cancel(Order order) { order.setState(new CancelledState()); }
    @Override public String getStatusName()   { return "PENDING_PAYMENT"; }
}
