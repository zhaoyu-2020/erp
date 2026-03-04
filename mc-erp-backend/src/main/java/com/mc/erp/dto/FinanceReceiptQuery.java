package com.mc.erp.dto;

import lombok.Data;

@Data
public class FinanceReceiptQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String receiptNo;
    private Long customerId;
    private Integer status;
}
