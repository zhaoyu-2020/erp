package com.mc.erp.controller;

import com.mc.erp.common.Result;
import com.mc.erp.dto.ReportQuery;
import com.mc.erp.service.ReportService;
import com.mc.erp.vo.DashboardVO;
import com.mc.erp.vo.FinanceReportVO;
import com.mc.erp.vo.IncompleteOrderStatusVO;
import com.mc.erp.vo.OrderStatusReportVO;
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

    /**
     * 获取订单状态报表（未完订单状态图）
     */
    @GetMapping("/order-status")
    public Result<List<OrderStatusReportVO>> getOrderStatusReport() {
        return Result.success(reportService.getOrderStatusReport());
    }

    /**
     * 获取所有未完成订单的状态进度列表
     */
    @GetMapping("/incomplete-orders")
    public Result<List<IncompleteOrderStatusVO>> getIncompleteOrdersStatus() {
        return Result.success(reportService.getIncompleteOrdersStatus());
    }
}
