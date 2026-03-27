package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName("biz_purchase_order_detail")
public class PurchaseOrderDetail {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属采购订单ID */
    private Long purchaseOrderId;
    /** 关联产品ID */
    private Long productId;

    /** 产品规格 */
    private String spec;
    /** 产品类型 */
    private String productType;
    /** 材质 */
    private String material;
    /** 长度(m) */
    private String length;
    /** 公差 */
    private String tolerance;

    /** 结算价格 */
    private BigDecimal settlementPrice;
    /** 计量方式 */
    private String measurementMethod;
    /** 合同金额小计 */
    private BigDecimal priceTotal;
    /** 结算金额小计 */
    private BigDecimal settlementAmount;

    /** 包装重量 */
    private BigDecimal packagingWeight;
    /** 包装方式 */
    private String packaging;
    /** 卷内径(mm) */
    private String coilInnerDiameter;
    /** 加工项 */
    private String processingItems;
    /** 备注 */
    private String remark;

    /** 订货数量 */
    private BigDecimal orderedQuantity;
    /** 实际数量 */
    private BigDecimal actualQuantity;
    /** 捆数（卷数） */
    private Integer bundleCount;
    /** 净重 */
    private BigDecimal netWeight;
    /** 毛重 */
    private BigDecimal grossWeight;
    /** 体积 */
    private BigDecimal volume;
    /** 货源地 */
    private String originPlace;
    /** 实际理论重量 */
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
