package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 经营驾驶舱视图对象
 */
@Data
public class DashboardVO {
    // 核心指标
    private BigDecimal currentMonthSales;        // 本月销售额
    private BigDecimal currentYearSales;         // 本年销售额
    private BigDecimal currentMonthProfit;       // 本月利润
    private BigDecimal currentYearProfit;        // 本年利润
    private BigDecimal grossMargin;              // 毛利率
    
    // 订单统计
    private Integer totalOrders;                 // 订单总数
    private Integer pendingOrders;               // 新建订单
    private Integer shippedOrders;               // 已发运订单
    private Integer completedOrders;             // 已完成订单
    
    // 资金状况
    private BigDecimal totalReceivables;         // 应收总额
    private BigDecimal totalPayables;            // 应付总额
    private BigDecimal overdueReceivables;       // 逾期应收
    
    // 同比环比
    private BigDecimal salesGrowthRate;          // 销售额增长率
    private BigDecimal profitGrowthRate;         // 利润增长率
    
    // 预警信息
    private Integer overdueReceivableCount;      // 超期应收数量
    private Integer highReceivableCount;         // 应收过高数量
    private Integer highPayableCount;            // 应付过高数量
    
    // 图表数据
    private List<TrendDataVO> salesTrend;        // 销售趋势
    private List<TrendDataVO> profitTrend;       // 利润趋势
    private List<RankingVO> customerRanking;     // 客户排行
    private List<RankingVO> productRanking;      // 产品排行
}
