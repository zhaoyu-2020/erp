package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private Long productTypeId;
    private String typeName;
    private String typeNameEn;
    private String spec;
    private String material;
    private String length;
    private String meterWeight;
    private String tolerance;
    private String hsCode;
    private BigDecimal taxRefundRate;
    private String nameCn;
    private String nameEn;
    private String unit;
    private String declaration;
    private LocalDateTime createTime;
}
