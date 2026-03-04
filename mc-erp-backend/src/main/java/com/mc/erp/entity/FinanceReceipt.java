package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_finance_receipt")
public class FinanceReceipt {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String receiptNo;
    private Long customerId;
    private BigDecimal amount;
    private String currency;
    private Integer status;
    private LocalDateTime claimTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
