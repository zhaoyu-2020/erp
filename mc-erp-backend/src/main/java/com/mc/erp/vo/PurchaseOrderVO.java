package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderVO {
    private Long id;
    private String poNo;
    private Long salesOrderId;
    private Long supplierId;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;
}
