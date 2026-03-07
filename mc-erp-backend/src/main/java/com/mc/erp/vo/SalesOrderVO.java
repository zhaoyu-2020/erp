package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesOrderVO {
    private Long id;
    private String orderNo;
    private Long salespersonId;
    private String salespersonName;
    private Long customerId;
    private String customerName;
    private String tradeTerm;
    private String currency;
    private BigDecimal depositExchangeRate;
    private BigDecimal finalExchangeRate;
    private BigDecimal contractAmount;
    private BigDecimal actualAmount;
    private BigDecimal depositRate;
    private BigDecimal receivedAmount;
    private Integer expectedReceiptDays;
    private String transportType;
    private BigDecimal seaFreight;
    private BigDecimal portFee;
    private BigDecimal vat;
    private BigDecimal profit;
    private Integer status;
    private LocalDateTime createTime;
}
