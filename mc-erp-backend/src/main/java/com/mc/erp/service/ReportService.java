package com.mc.erp.service;

import com.mc.erp.dto.ReportQuery;
import com.mc.erp.vo.DashboardVO;
import com.mc.erp.vo.FinanceReportVO;
import com.mc.erp.vo.IncompleteOrderFinanceVO;
import com.mc.erp.vo.IncompleteOrderStatusVO;
import com.mc.erp.vo.OrderStatusReportVO;
import com.mc.erp.vo.SalesReportVO;

import java.util.List;

/**
 * 报表服务接口
 */
public interface ReportService {
    
    /**
     * 获取经营驾驶舱数据
     */
    DashboardVO getDashboard(ReportQuery query);
    
    /**
     * 获取销售报表
     */
    List<SalesReportVO> getSalesReport(ReportQuery query);
    
    /**
     * 获取财务报表
     */
    List<FinanceReportVO> getFinanceReport(ReportQuery query);

    /**
     * 获取订单状态报表（未完订单状态图）
     */
    List<OrderStatusReportVO> getOrderStatusReport();

    /**
     * 获取所有未完成订单的状态列表（用于进度表）
     */
    List<IncompleteOrderStatusVO> getIncompleteOrdersStatus();

    /**
     * 获取所有未完成订单的资金数据（用于资金图）
     */
    List<IncompleteOrderFinanceVO> getIncompleteOrdersFinance();
}
