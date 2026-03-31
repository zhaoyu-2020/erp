package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_finance_receipt")
public class FinanceReceipt {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 银行流水号 */
    private String serialNo;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @NotBlank(message = "币种不能为空")
    private String currency;

    @NotNull(message = "收款日期不能为空")
    private LocalDate receiptDate;

    private String receivingBank;

    /** 收款汇率 */
    private BigDecimal exchangeRate;

    /** 状态: 1=新建, 2=认领中, 3=完成 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    @TableLogic
    private Integer isDeleted;
}
