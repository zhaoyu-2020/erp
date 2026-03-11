package com.mc.erp.dto;

import lombok.Data;

@Data
public class PurchaseOrderQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String poNo;
    private String supplierName;
    private String salesOrderNo;
    private Integer status;

    private Long salespersonId;
    private Long createId;
}
