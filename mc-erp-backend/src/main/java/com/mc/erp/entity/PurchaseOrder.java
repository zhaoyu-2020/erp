package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_purchase_order")
public class PurchaseOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "采购单号不能为空")
    private String poNo;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "关联销售订单号不能为空")
    private String salesOrderNo;

    // 业务员ID
    private Long salespersonId;

    // 创建人ID
    @TableField(fill = FieldFill.INSERT)
    private Long createId;

    // 更新人ID
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;

    /** 币种: RMB / USD */
    private String currency;

    /** 合同总金额（由明细合同吨数 × 结算单价自动计算） */
    private BigDecimal totalAmount;

    /** 合同总数量（由明细订货数量汇总） */
    private BigDecimal contractTotalQty;

    /** 结算总金额（由明细实际交货数据汇总） */
    private BigDecimal settlementTotalAmount;

    /** 结算总数量（由明细 actual_quantity 汇总） */
    private BigDecimal settlementTotalQty;

    // 实际金额
    private BigDecimal actualAmount;

    @NotNull(message = "定金比例不能为空")
    private BigDecimal depositRate;

    @NotNull(message = "定金金额不能为空")
    private BigDecimal depositAmount;

    @NotNull(message = "订单日期不能为空")
    private LocalDate orderDate;

    @NotNull(message = "交货日期不能为空")
    private LocalDate deliveryDate;
    private String transportRemark;
    private BigDecimal totalFreight;
    private String photos;
    private String materialSheet;
    private String invoice;
    private String depositSlip;
    private String finalPaymentSlip;
    private String freightSlip;

    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
