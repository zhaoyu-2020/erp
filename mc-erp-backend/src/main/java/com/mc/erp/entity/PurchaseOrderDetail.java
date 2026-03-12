package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_purchase_order_detail")
public class PurchaseOrderDetail {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long purchaseOrderId;
    private Long productId;

    private String spec;
    private String productType;
    private String material;
    private String length;
    private String tolerance;

    private BigDecimal quantityTon;
    private Integer quantityPc;
    private BigDecimal quantityMeter;

    private BigDecimal priceTotal;

    private BigDecimal packagingWeight;
    private String packaging;
    private String coilInnerDiameter;
    private String processingItems;
    private String remark;

    private Integer detailSeq;
    private BigDecimal orderedQuantity;
    private BigDecimal actualQuantity;
    private Integer bundleCount;
    private BigDecimal netWeight;
    private BigDecimal grossWeight;
    private BigDecimal volume;
    private String originPlace;
    private BigDecimal actualTheoreticalWeight;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    @TableLogic
    private Integer isDeleted;
}
