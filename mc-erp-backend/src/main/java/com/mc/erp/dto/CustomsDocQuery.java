package com.mc.erp.dto;

import lombok.Data;

@Data
public class CustomsDocQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String docNo;
    private Integer status;
    private Long salesOrderId;
}
