package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 财务报表视图对象
 */
@Data
public class FinanceReportVO {
    private LocalDate date;                 // 日期
    private BigDecimal income;              // 收入
    private BigDecimal expense;             // 支出
    private BigDecimal balance;             // 余额
    
    // 应收账款
    private BigDecimal receivable0to30;     // 0-30天
    private BigDecimal receivable30to60;    // 30-60天
    private BigDecimal receivable60to90;    // 60-90天
    private BigDecimal receivableOver90;    // 90天以上
    
    // 应付账款
    private BigDecimal payable0to30;        // 0-30天
    private BigDecimal payable30to60;       // 30-60天
    private BigDecimal payable60to90;       // 60-90天
    private BigDecimal payableOver90;       // 90天以上
}
