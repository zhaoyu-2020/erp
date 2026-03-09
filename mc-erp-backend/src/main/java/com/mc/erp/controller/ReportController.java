package com.mc.erp.controller;

import com.mc.erp.common.Result;
import com.mc.erp.dto.ReportQuery;
import com.mc.erp.service.ReportService;
import com.mc.erp.vo.DashboardVO;
import com.mc.erp.vo.FinanceReportVO;
import com.mc.erp.vo.SalesReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表控制器
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取经营驾驶舱
     */
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard(ReportQuery query) {
        return Result.success(reportService.getDashboard(query));
    }

    /**
     * 获取销售报表
     */
    @GetMapping("/sales")
    public Result<List<SalesReportVO>> getSalesReport(ReportQuery query) {
        return Result.success(reportService.getSalesReport(query));
    }

    /**
     * 获取财务报表
     */
    @GetMapping("/finance")
    public Result<List<FinanceReportVO>> getFinanceReport(ReportQuery query) {
        return Result.success(reportService.getFinanceReport(query));
    }
}
