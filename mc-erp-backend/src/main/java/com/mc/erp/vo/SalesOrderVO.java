package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesOrderVO {
    private Long id;
    private String orderNo;
    private String tradeTerm;
    private String currency;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;
}
