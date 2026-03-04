package com.mc.erp.dto;

import lombok.Data;

@Data
public class SupplierQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String supplierCode;
    private String name;
}
