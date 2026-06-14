package com.ordersys.payment;

import com.ordersys.payment.strategy.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class PaymentStrategyTest {

    @Test
    void wechatPay_returnsSuccessWithWechatPrefix() {
        PaymentContext ctx = new PaymentContext(new WechatPayStrategy());
        PaymentResult result = ctx.pay(new BigDecimal("28.00"), 1001L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTransactionId()).startsWith("WECHAT_");
        assertThat(result.getMethod()).isEqualTo("WECHAT");
    }

    @Test
    void alipay_returnsSuccessWithAlipayPrefix() {
        PaymentContext ctx = new PaymentContext(new AlipayStrategy());
        PaymentResult result = ctx.pay(new BigDecimal("28.00"), 1001L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTransactionId()).startsWith("ALIPAY_");
        assertThat(result.getMethod()).isEqualTo("ALIPAY");
    }

    @Test
    void paymentContext_setStrategy_switchesBehavior() {
        PaymentContext ctx = new PaymentContext(new WechatPayStrategy());
        ctx.setStrategy(new AlipayStrategy());
        PaymentResult result = ctx.pay(new BigDecimal("15.00"), 1002L);

        assertThat(result.getMethod()).isEqualTo("ALIPAY");
        assertThat(result.getTransactionId()).startsWith("ALIPAY_");
    }

    @Test
    void paymentContext_nullStrategy_throwsIllegalArgument() {
        assertThatThrownBy(() -> new PaymentContext(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
