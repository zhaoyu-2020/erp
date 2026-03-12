package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FinanceReceiptVO {
    private Long id;
    private String receiptNo;
    private String serialNo;
    private BigDecimal amount;
    private String currency;
    private LocalDate receiptDate;
    private String receivingBank;
    /** 状态: 1=新建, 2=认领中, 3=完成 */
    private Integer status;
    private String statusLabel;
    private LocalDateTime createTime;
    private List<FinanceReceiptDetailVO> details;
}
