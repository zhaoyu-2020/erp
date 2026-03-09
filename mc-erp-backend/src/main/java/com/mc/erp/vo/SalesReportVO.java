package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售报表视图对象
 */
@Data
public class SalesReportVO {
    private String period;                  // 时间周期
    private String orderNo;                 // 订单号
    private String customerName;            // 客户名称
    private String salespersonName;         // 业务员
    private BigDecimal salesAmount;         // 销售额
    private BigDecimal costAmount;          // 成本
    private BigDecimal profit;              // 利润
    private BigDecimal profitMargin;        // 利润率
    private BigDecimal receivedAmount;      // 已收款
    private BigDecimal receivableAmount;    // 应收款
    private Integer orderCount;             // 订单数量
    private LocalDateTime createTime;       // 创建时间
}
