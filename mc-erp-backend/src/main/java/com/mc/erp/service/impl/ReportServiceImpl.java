package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.erp.dto.ReportQuery;
import com.mc.erp.entity.Customer;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.service.*;
import com.mc.erp.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private CustomerService customerService;

    @Override
    public DashboardVO getDashboard(ReportQuery query) {
        DashboardVO dashboard = new DashboardVO();
        
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfYear = now.withDayOfYear(1);
        
        // 查询所有销售订单
        List<SalesOrder> allOrders = salesOrderService.list();
        
        // 本月销售订单
        List<SalesOrder> currentMonthOrders = allOrders.stream()
                .filter(o -> o.getCreateTime() != null && 
                       o.getCreateTime().toLocalDate().isAfter(startOfMonth.minusDays(1)))
                .collect(Collectors.toList());
        
        // 本年销售订单
        List<SalesOrder> currentYearOrders = allOrders.stream()
                .filter(o -> o.getCreateTime() != null && 
                       o.getCreateTime().toLocalDate().isAfter(startOfYear.minusDays(1)))
                .collect(Collectors.toList());
        
        // 计算本月销售额和利润
        BigDecimal currentMonthSales = currentMonthOrders.stream()
                .map(o -> o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setCurrentMonthSales(currentMonthSales);
        
        BigDecimal currentMonthProfit = currentMonthOrders.stream()
                .map(o -> o.getProfit() != null ? o.getProfit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setCurrentMonthProfit(currentMonthProfit);
        
        // 计算本年销售额和利润
        BigDecimal currentYearSales = currentYearOrders.stream()
                .map(o -> o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setCurrentYearSales(currentYearSales);
        
        BigDecimal currentYearProfit = currentYearOrders.stream()
                .map(o -> o.getProfit() != null ? o.getProfit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setCurrentYearProfit(currentYearProfit);
        
        // 计算毛利率
        if (currentYearSales.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal grossMargin = currentYearProfit.multiply(BigDecimal.valueOf(100))
                    .divide(currentYearSales, 2, RoundingMode.HALF_UP);
            dashboard.setGrossMargin(grossMargin);
        } else {
            dashboard.setGrossMargin(BigDecimal.ZERO);
        }
        
        // 订单统计
        dashboard.setTotalOrders(allOrders.size());
        dashboard.setPendingOrders((int) allOrders.stream().filter(o -> o.getStatus() == 1).count());
        dashboard.setShippedOrders((int) allOrders.stream().filter(o -> o.getStatus() == 4).count());
        dashboard.setCompletedOrders((int) allOrders.stream().filter(o -> o.getStatus() == 5).count());
        
        // 应收应付统计
        BigDecimal totalReceivables = allOrders.stream()
                .filter(o -> o.getStatus() != 5) // 未完成订单
                .map(o -> {
                    BigDecimal actual = o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO;
                    BigDecimal received = o.getReceivedAmount() != null ? o.getReceivedAmount() : BigDecimal.ZERO;
                    BigDecimal finalPayment = o.getFinalPaymentAmount() != null ? o.getFinalPaymentAmount() : BigDecimal.ZERO;
                    return actual.subtract(received).subtract(finalPayment);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setTotalReceivables(totalReceivables);
        
        List<PurchaseOrder> allPurchaseOrders = purchaseOrderService.list();
        BigDecimal totalPayables = allPurchaseOrders.stream()
                .filter(p -> p.getStatus() != 6) // 未完结
                .map(p -> {
                    BigDecimal actual = p.getActualAmount() != null ? p.getActualAmount() : BigDecimal.ZERO;
                    BigDecimal deposit = p.getDepositAmount() != null ? p.getDepositAmount() : BigDecimal.ZERO;
                    return actual.subtract(deposit);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setTotalPayables(totalPayables);
        
        // 销售趋势（最近12个月）
        List<TrendDataVO> salesTrend = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            LocalDate start = month.atDay(1);
            LocalDate end = month.atEndOfMonth();
            
            BigDecimal monthSales = allOrders.stream()
                    .filter(o -> o.getCreateTime() != null && 
                           !o.getCreateTime().toLocalDate().isBefore(start) &&
                           !o.getCreateTime().toLocalDate().isAfter(end))
                    .map(o -> o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            TrendDataVO trend = new TrendDataVO();
            trend.setPeriod(month.toString());
            trend.setValue(monthSales);
            trend.setLabel(month.getMonth().toString());
            salesTrend.add(trend);
        }
        dashboard.setSalesTrend(salesTrend);
        
        // 客户排行（前10）
        Map<Long, BigDecimal> customerSales = allOrders.stream()
                .filter(o -> o.getCustomerId() != null)
                .collect(Collectors.groupingBy(
                        SalesOrder::getCustomerId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                o -> o.getActualAmount() != null ? o.getActualAmount() : BigDecimal.ZERO,
                                BigDecimal::add
                        )
                ));
        
        List<Customer> customers = customerService.listByIds(customerSales.keySet());
        Map<Long, String> customerNames = customers.stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getName));
        
        List<RankingVO> customerRanking = customerSales.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(entry -> {
                    RankingVO ranking = new RankingVO();
                    ranking.setId(entry.getKey());
                    ranking.setName(customerNames.getOrDefault(entry.getKey(), "未知客户"));
                    ranking.setAmount(entry.getValue());
                    return ranking;
                })
                .collect(Collectors.toList());
        
        for (int i = 0; i < customerRanking.size(); i++) {
            customerRanking.get(i).setRank(i + 1);
        }
        dashboard.setCustomerRanking(customerRanking);
        
        // 预警信息
        dashboard.setOverdueReceivableCount(0); // 需要根据实际账期计算
        dashboard.setHighReceivableCount(0);
        dashboard.setHighPayableCount(0);
        
        return dashboard;
    }

    @Override
    public List<SalesReportVO> getSalesReport(ReportQuery query) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (query.getStartDate() != null) {
            wrapper.ge(SalesOrder::getCreateTime, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.le(SalesOrder::getCreateTime, query.getEndDate().atTime(23, 59, 59));
        }
        if (query.getSalespersonId() != null) {
            wrapper.eq(SalesOrder::getSalespersonId, query.getSalespersonId());
        }
        if (query.getCustomerId() != null) {
            wrapper.eq(SalesOrder::getCustomerId, query.getCustomerId());
        }
        
        List<SalesOrder> orders = salesOrderService.list(wrapper);
        
        return orders.stream().map(order -> {
            SalesReportVO vo = new SalesReportVO();
            vo.setOrderNo(order.getOrderNo());
            vo.setSalesAmount(order.getActualAmount());
            vo.setProfit(order.getProfit());
            vo.setReceivedAmount(order.getReceivedAmount());
            vo.setCreateTime(order.getCreateTime());
            
            // 计算利润率
            if (order.getActualAmount() != null && order.getActualAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal profitMargin = order.getProfit() != null 
                        ? order.getProfit().multiply(BigDecimal.valueOf(100))
                                .divide(order.getActualAmount(), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                vo.setProfitMargin(profitMargin);
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FinanceReportVO> getFinanceReport(ReportQuery query) {
        // 简化实现，返回空列表
        return new ArrayList<>();
    }
}
