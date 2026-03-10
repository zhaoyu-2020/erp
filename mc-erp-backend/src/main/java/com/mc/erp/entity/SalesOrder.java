package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_sales_order")
public class SalesOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "业务员ID不能为空")
    private Long salespersonId;

    private Long createUserId;

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    private String tradeTerm;
    private String currency;

    @NotNull(message = "定金汇率不能为空")
    private BigDecimal depositExchangeRate;

    @NotNull(message = "尾款汇率不能为空")
    private BigDecimal finalExchangeRate;

    @NotNull(message = "合同金额不能为空")
    private BigDecimal contractAmount;

    @NotNull(message = "实际金额不能为空")
    private BigDecimal actualAmount;

    @NotNull(message = "定金比例不能为空")
    private BigDecimal depositRate;

    @NotNull(message = "定金收款金额不能为空")
    private BigDecimal receivedAmount;

    private BigDecimal finalPaymentAmount;
    private BigDecimal insuranceFee;
    private BigDecimal insuranceAmount;

    /** 预计收尾款日期 */
    @NotNull(message = "预计收尾款日期不能为空")
    private java.time.LocalDate expectedReceiptDays;

    private String transportType;
    private BigDecimal seaFreight;
    private BigDecimal portFee;
    private BigDecimal vat;
    private BigDecimal profit;

    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
