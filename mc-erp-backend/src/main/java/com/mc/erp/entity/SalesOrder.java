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

    @NotNull(message = "操作员ID不能为空")
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private Long createId;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    private String tradeTerm;
    private String paymentMethod;
    private String currency;

    private BigDecimal depositExchangeRate;

    private BigDecimal finalExchangeRate;

    private BigDecimal contractAmount;

    private BigDecimal actualAmount;

    @NotNull(message = "定金比例不能为空")
    private BigDecimal depositRate;

    private BigDecimal receivedAmount;

    private BigDecimal finalPaymentAmount;
    private BigDecimal insuranceFee;
    private BigDecimal insuranceAmount;

    /** 预计收尾款日期 */
    @NotNull(message = "预计收尾款日期不能为空")
    private java.time.LocalDate expectedReceiptDays;

    /** 交货期 */
    private java.time.LocalDate deliveryDate;

    @NotBlank(message = "目的港不能为空")
    private String destinationPort;

    private String transportType;
    private BigDecimal seaFreight;
    private BigDecimal portFee;
    private BigDecimal vat;
    private BigDecimal profit;
    private BigDecimal loss;
    private BigDecimal contractTotalQuantity;
    private BigDecimal settlementTotalQuantity;

    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
