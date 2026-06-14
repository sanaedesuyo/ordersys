package com.ordersys.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ordersys.order.state.OrderState;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 状态模式 — 当前状态对象（不持久化，@TableField(exist=false)）。
     * 调用 setState() 会同步更新 status 字符串供数据库存储。
     */
    @TableField(exist = false)
    private OrderState state;

    public void setState(OrderState state) {
        this.state = state;
        this.status = state.getStatusName();
    }
}
