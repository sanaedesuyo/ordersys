package com.ordersys.order;

import com.ordersys.order.entity.Order;
import com.ordersys.order.state.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OrderStateTest {

    private Order newPendingOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setState(new PendingPaymentState());
        return order;
    }

    @Test
    void pay_fromPendingPayment_transitionsToPaid() {
        Order order = newPendingOrder();
        order.getState().pay(order);

        assertThat(order.getState()).isInstanceOf(PaidState.class);
        assertThat(order.getStatus()).isEqualTo("PAID");
    }

    @Test
    void accept_fromPaid_transitionsToPreparing() {
        Order order = newPendingOrder();
        order.getState().pay(order);
        order.getState().accept(order);

        assertThat(order.getState()).isInstanceOf(PreparingState.class);
        assertThat(order.getStatus()).isEqualTo("PREPARING");
    }

    @Test
    void fullFlow_pendingToCompleted() {
        Order order = newPendingOrder();
        order.getState().pay(order);
        order.getState().accept(order);
        order.getState().startDelivery(order);
        order.getState().complete(order);

        assertThat(order.getState()).isInstanceOf(CompletedState.class);
        assertThat(order.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void cancel_fromPendingPayment_transitionsToCancelled() {
        Order order = newPendingOrder();
        order.getState().cancel(order);

        assertThat(order.getState()).isInstanceOf(CancelledState.class);
        assertThat(order.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void cancel_fromCompleted_throwsIllegalState() {
        Order order = newPendingOrder();
        order.getState().pay(order);
        order.getState().accept(order);
        order.getState().startDelivery(order);
        order.getState().complete(order);

        assertThatThrownBy(() -> order.getState().cancel(order))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pay_whenAlreadyPaid_throwsIllegalState() {
        Order order = newPendingOrder();
        order.getState().pay(order);

        assertThatThrownBy(() -> order.getState().pay(order))
            .isInstanceOf(IllegalStateException.class);
    }
}
