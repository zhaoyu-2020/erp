package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinanceReceiptVO {
    private Long id;
    private String receiptNo;
    private Long customerId;
    private String customerName; // Expanded field
    private BigDecimal amount;
    private String currency;
    private Integer status;
    private LocalDateTime claimTime;
    private LocalDateTime createTime;
}
