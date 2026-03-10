package com.mc.erp.dto;

import lombok.Data;

@Data
public class SalesOrderDetailQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderNo;
    private Long orderId;
    private String spec;
    private String productType;
    private String material;
}
