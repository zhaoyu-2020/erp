package com.mc.erp.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FinanceReceiptQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String serialNo;
    private Integer status;
    /** 按关联销售订单号搜索（模糊匹配明细表） */
    private String salesOrderNo;
    /** 金额范围：最小值 */
    private BigDecimal amountMin;
    /** 金额范围：最大值 */
    private BigDecimal amountMax;
}
