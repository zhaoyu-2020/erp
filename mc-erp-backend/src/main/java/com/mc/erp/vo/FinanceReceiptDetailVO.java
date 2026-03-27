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
    /** 本笔收款汇率 */
    private BigDecimal exchangeRate;
    /** 绑定类型: 1=定金, 2=尾款 */
    private Integer bindType;
    private LocalDateTime createTime;
}
