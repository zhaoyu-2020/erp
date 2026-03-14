package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("erp_freight_order")
public class FreightOrder {
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    private String orderCode;

    @NotBlank(message = "销售订单号不能为空")
    private String saleOrderCode;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String supplierName;

    @NotNull(message = "运输类型不能为空")
    private Integer transportType;

    private String containerType;
    private Integer containerQty;
    private Integer isLcl;
    private String containerNo;
    private BigDecimal bulkWeight;
    private BigDecimal bulkVolume;
    private String shippingSpace;

    private Integer needInsurance;
    private BigDecimal insuredAmount;
    private BigDecimal premium;
    private String insuranceCurrency;
    private String insuranceRemark;

    private Integer orderStatus;
    private BigDecimal totalOceanFreight;
    private BigDecimal totalGroundFee;
    private BigDecimal totalAmount;
    private String orderCurrency;
    private String departurePort;
    private String destinationPort;
    private LocalDateTime shipDate;
    private LocalDateTime estimatedArrivalDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createUser;
    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    private String remark;
    private String cancelReason;
    @TableLogic
    private Integer isDeleted;
}
