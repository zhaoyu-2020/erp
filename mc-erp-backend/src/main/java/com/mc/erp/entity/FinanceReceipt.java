package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_finance_receipt")
public class FinanceReceipt {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "收款单号不能为空")
    private String receiptNo;

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;
    private String currency;
    private Integer status;
    private LocalDateTime claimTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
