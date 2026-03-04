package com.mc.erp.dto;

import lombok.Data;

@Data
public class SalesOrderQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderNo;
    private String tradeTerm;
    private Integer status;
}
