package com.mc.erp.dto;

import lombok.Data;

@Data
public class ProductQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long productTypeId;
}
