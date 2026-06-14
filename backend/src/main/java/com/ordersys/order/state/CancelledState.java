package com.ordersys.order.state;

/** 状态模式 — 已取消：终态，所有操作均继承 default 抛出 IllegalStateException */
public class CancelledState implements OrderState {
    @Override public String getStatusName() { return "CANCELLED"; }
}
