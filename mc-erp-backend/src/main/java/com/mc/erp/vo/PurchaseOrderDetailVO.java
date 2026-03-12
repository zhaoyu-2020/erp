package com.mc.erp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderDetailVO {
    private Long id;
    private Long purchaseOrderId;
    private String poNo;
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
}
