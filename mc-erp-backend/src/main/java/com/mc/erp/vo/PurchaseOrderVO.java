package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderVO {
    private Long id;
    private String poNo;
    private Long supplierId;
    private String supplierName;
    private String salesOrderNo;
    private BigDecimal totalAmount;
    private BigDecimal actualAmount;
    private BigDecimal depositRate;
    private BigDecimal depositAmount;
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
}
