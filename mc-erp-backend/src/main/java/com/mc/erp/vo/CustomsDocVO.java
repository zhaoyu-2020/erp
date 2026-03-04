package com.mc.erp.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomsDocVO {
    private Long id;
    private String docNo;
    private Long salesOrderId;
    private String salesOrderNo; // Extended field for display
    private LocalDate declareDate;
    private Integer status;
    private LocalDateTime createTime;
}
