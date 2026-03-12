package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_finance_receipt_detail")
public class FinanceReceiptDetail {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "收款单ID不能为空")
    private Long receiptId;

    @NotNull(message = "销售订单ID不能为空")
    private Long salesOrderId;

    @NotNull(message = "销售订单号不能为空")
    private String salesOrderNo;

    @NotNull(message = "绑定金额不能为空")
    private BigDecimal boundAmount;

    /** 绑定类型: 1=定金, 2=尾款 */
    private Integer bindType;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    @TableLogic
    private Integer isDeleted;
}
