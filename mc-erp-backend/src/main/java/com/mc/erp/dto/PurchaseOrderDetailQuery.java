package com.mc.erp.dto;

import lombok.Data;

@Data
public class PurchaseOrderDetailQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String poNo;
    private Long purchaseOrderId;
    private String spec;
    private String productType;
    private String material;
}
