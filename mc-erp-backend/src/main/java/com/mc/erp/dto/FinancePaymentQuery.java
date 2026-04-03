package com.mc.erp.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FinancePaymentQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String paymentNo;
    private Integer status;
    /** 按关联采购订单号搜索（模糊匹配） */
    private String purchaseOrderNo;
    /** 金额范围：最小值 */
    private BigDecimal amountMin;
    /** 金额范围：最大值 */
    private BigDecimal amountMax;
}
