package com.mc.erp.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * 未完订单资金视图对象
 */
@Data
@AllArgsConstructor
public class IncompleteOrderFinanceVO {
    private String orderNo;                     // 订单号
    private BigDecimal salesContractAmount;     // 销售合同额
    private BigDecimal salesReceivedAmount;     // 销售已收定金
    private BigDecimal purchaseContractAmount;  // 采购合同额（关联采购单合计）
    private BigDecimal purchaseDepositPaid;     // 采购已付定金（关联采购单合计）
}
