package com.mc.erp.service;

import com.mc.erp.dto.ReportQuery;
import com.mc.erp.vo.DashboardVO;
import com.mc.erp.vo.FinanceReportVO;
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
}
