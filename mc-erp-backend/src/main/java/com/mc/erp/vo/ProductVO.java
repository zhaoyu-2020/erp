package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private String spuCode;
    private String hsCode;
    private String nameCn;
    private String nameEn;
    private BigDecimal taxRefundRate;
    private String unit;
    private LocalDateTime createTime;
}
