package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinanceReceiptDetailVO {
    private Long id;
    private Long receiptId;
    private Long salesOrderId;
    private String salesOrderNo;
    private BigDecimal boundAmount;
    private LocalDateTime createTime;
}
