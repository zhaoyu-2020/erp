package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.erp.dto.ReportQuery;
import com.mc.erp.entity.Customer;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.enums.SalesOrderStatus;
import com.mc.erp.service.*;
import com.mc.erp.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
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
        dashboard.setShippedOrders((int) allOrders.stream().filter(o -> o.getStatus() == 5).count());
        dashboard.setCompletedOrders((int) allOrders.stream().filter(o -> o.getStatus() == 7).count());
        
        // 应收应付统计
        BigDecimal totalReceivables = allOrders.stream()
                .filter(o -> o.getStatus() != 7) // 未完成订单
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
                .filter(p -> p.getStatus() != 4) // 未完结
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
        
        List<Customer> customers;
        Map<Long, String> customerNames;
        if (customerSales.isEmpty()) {
            customers = new ArrayList<>();
            customerNames = new java.util.HashMap<>();
        } else {
            customers = customerService.listByIds(customerSales.keySet());
            customerNames = customers.stream()
                    .collect(Collectors.toMap(Customer::getId, Customer::getName));
        }
        
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

    @Override
    public List<OrderStatusReportVO> getOrderStatusReport() {
        List<SalesOrder> allOrders = salesOrderService.list();
        Map<Integer, Long> countByStatus = allOrders.stream()
                .filter(o -> o.getStatus() != null)
                .collect(Collectors.groupingBy(SalesOrder::getStatus, Collectors.counting()));

        return java.util.Arrays.stream(SalesOrderStatus.values())
                .map(s -> new OrderStatusReportVO(
                        s.getCode(),
                        s.getLabel(),
                        countByStatus.getOrDefault(s.getCode(), 0L).intValue(),
                        s.getCode() == SalesOrderStatus.COMPLETED.getCode()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<IncompleteOrderStatusVO> getIncompleteOrdersStatus() {
        return salesOrderService.list().stream()
                .filter(o -> o.getStatus() != null && o.getStatus() != SalesOrderStatus.COMPLETED.getCode())
                .sorted(java.util.Comparator.comparing(SalesOrder::getOrderNo))
                .map(o -> {
                    String label;
                    try {
                        label = SalesOrderStatus.of(o.getStatus()).getLabel();
                    } catch (Exception e) {
                        label = String.valueOf(o.getStatus());
                    }
                    return new IncompleteOrderStatusVO(o.getOrderNo(), o.getStatus(), label);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<IncompleteOrderFinanceVO> getIncompleteOrdersFinance() {
        List<SalesOrder> incompleteSales = salesOrderService.list().stream()
                .filter(o -> o.getStatus() != null && o.getStatus() != SalesOrderStatus.COMPLETED.getCode())
                .sorted(java.util.Comparator.comparing(SalesOrder::getOrderNo))
                .collect(Collectors.toList());

        List<PurchaseOrder> allPurchase = purchaseOrderService.list();
        Map<String, List<PurchaseOrder>> purchaseByOrderNo = allPurchase.stream()
                .filter(p -> p.getSalesOrderNo() != null)
                .collect(Collectors.groupingBy(PurchaseOrder::getSalesOrderNo));

        return incompleteSales.stream().map(o -> {
            BigDecimal salesContract = o.getContractAmount() != null ? o.getContractAmount() : BigDecimal.ZERO;
            BigDecimal salesReceived = o.getReceivedAmount() != null ? o.getReceivedAmount() : BigDecimal.ZERO;

            List<PurchaseOrder> linked = purchaseByOrderNo.getOrDefault(o.getOrderNo(), java.util.Collections.emptyList());
            BigDecimal purchaseContract = linked.stream()
                    .map(p -> {
                        BigDecimal actual = p.getActualAmount();
                        BigDecimal total = p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO;
                        // actual_amount 为 null 或 0 时，使用 total_amount（合同总金额）
                        return (actual != null && actual.compareTo(BigDecimal.ZERO) > 0) ? actual : total;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal purchaseDeposit = linked.stream()
                    .map(p -> p.getDepositAmount() != null ? p.getDepositAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new IncompleteOrderFinanceVO(o.getOrderNo(), salesContract, salesReceived, purchaseContract, purchaseDeposit);
        }).collect(Collectors.toList());
    }

    @Override
    public List<HalfMonthCashFlowVO> getHalfMonthCashFlow() {
        // 查询所有未完成的销售订单
        List<SalesOrder> incompleteSales = salesOrderService.list().stream()
                .filter(o -> o.getStatus() != null && o.getStatus() != SalesOrderStatus.COMPLETED.getCode())
                .collect(Collectors.toList());

        // 查询所有采购订单（关联未完成销售订单）
        Set<String> incompleteOrderNos = incompleteSales.stream()
                .map(SalesOrder::getOrderNo)
                .collect(Collectors.toSet());
        List<PurchaseOrder> linkedPurchases = purchaseOrderService.list().stream()
                .filter(p -> p.getSalesOrderNo() != null && incompleteOrderNos.contains(p.getSalesOrderNo()))
                .collect(Collectors.toList());

        // 确定时间范围：从当前月初开始，到数据中最远日期后再加一个月
        LocalDate now = LocalDate.now();
        LocalDate rangeStart = now.withDayOfMonth(1);
        LocalDate rangeEnd = now.plusMonths(6); // 默认至少6个月

        for (SalesOrder o : incompleteSales) {
            if (o.getExpectedReceiptDays() != null && o.getExpectedReceiptDays().isAfter(rangeEnd)) {
                rangeEnd = o.getExpectedReceiptDays();
            }
        }
        for (PurchaseOrder p : linkedPurchases) {
            if (p.getDeliveryDate() != null && p.getDeliveryDate().isAfter(rangeEnd)) {
                rangeEnd = p.getDeliveryDate();
            }
        }
        rangeEnd = rangeEnd.plusMonths(1).withDayOfMonth(1).minusDays(1); // 延伸到月末

        // 生成半月时间段
        List<HalfMonthCashFlowVO> result = new ArrayList<>();
        LocalDate cursor = rangeStart;
        while (!cursor.isAfter(rangeEnd)) {
            LocalDate periodStart;
            LocalDate periodEnd;
            String label;

            if (cursor.getDayOfMonth() <= 15) {
                periodStart = cursor.withDayOfMonth(1);
                periodEnd = cursor.withDayOfMonth(15);
                label = cursor.getMonthValue() + "月15日";
            } else {
                periodStart = cursor.withDayOfMonth(16);
                periodEnd = YearMonth.from(cursor).atEndOfMonth();
                label = cursor.getMonthValue() + "月" + periodEnd.getDayOfMonth() + "日";
            }

            // 计算该时间段的预计收款（销售订单 expectedReceiptDays 落在此区间）
            BigDecimal collection = BigDecimal.ZERO;
            List<String> collectionOrders = new ArrayList<>();
            for (SalesOrder o : incompleteSales) {
                if (o.getExpectedReceiptDays() != null
                        && !o.getExpectedReceiptDays().isBefore(periodStart)
                        && !o.getExpectedReceiptDays().isAfter(periodEnd)) {
                    BigDecimal contract = o.getContractAmount() != null ? o.getContractAmount() : BigDecimal.ZERO;
                    BigDecimal received = o.getReceivedAmount() != null ? o.getReceivedAmount() : BigDecimal.ZERO;
                    BigDecimal remaining = contract.subtract(received);
                    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                        collection = collection.add(remaining);
                        collectionOrders.add(o.getOrderNo());
                    }
                }
            }

            // 计算该时间段的预计付款（采购订单 deliveryDate 落在此区间 → 到期需付尾款）
            BigDecimal payment = BigDecimal.ZERO;
            List<String> paymentOrders = new ArrayList<>();
            for (PurchaseOrder p : linkedPurchases) {
                if (p.getDeliveryDate() != null
                        && !p.getDeliveryDate().isBefore(periodStart)
                        && !p.getDeliveryDate().isAfter(periodEnd)) {
                    BigDecimal total = p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO;
                    BigDecimal deposit = p.getDepositAmount() != null ? p.getDepositAmount() : BigDecimal.ZERO;
                    BigDecimal remaining = total.subtract(deposit);
                    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                        payment = payment.add(remaining);
                        paymentOrders.add(p.getSalesOrderNo() + "(" + p.getPoNo() + ")");
                    }
                }
            }

            HalfMonthCashFlowVO vo = new HalfMonthCashFlowVO();
            vo.setPeriodLabel(label);
            vo.setPeriodStart(periodStart);
            vo.setPeriodEnd(periodEnd);
            vo.setExpectedCollection(collection);
            vo.setExpectedPayment(payment);
            vo.setCollectionOrderNos(collectionOrders);
            vo.setPaymentOrderNos(paymentOrders);
            result.add(vo);

            // 移动到下一个半月
            if (cursor.getDayOfMonth() <= 15) {
                cursor = cursor.withDayOfMonth(16);
            } else {
                cursor = cursor.plusMonths(1).withDayOfMonth(1);
            }
        }

        return result;
    }
}
