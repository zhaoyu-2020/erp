package com.mc.erp.dto;

import lombok.Data;

@Data
public class CustomerQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String customerCode;
    private String name;
    private String continent;
}
