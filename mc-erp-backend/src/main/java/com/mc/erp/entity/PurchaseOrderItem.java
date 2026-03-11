package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_purchase_order_item")
public class PurchaseOrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "采购订单ID不能为空")
    private Long purchaseOrderId;

    @NotBlank(message = "加工内容不能为空")
    private String content;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    @TableLogic
    private Integer isDeleted;
}
