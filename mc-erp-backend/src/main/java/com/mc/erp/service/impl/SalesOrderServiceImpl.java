package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.SalesOrderQuery;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.entity.SalesOrderDetail;
import com.mc.erp.entity.User;
import com.mc.erp.service.CustomerService;
import com.mc.erp.service.PurchaseOrderService;
import com.mc.erp.service.SalesOrderDetailService;
import com.mc.erp.service.SalesOrderService;
import com.mc.erp.service.UserService;
import com.mc.erp.mapper.SalesOrderMapper;
import com.mc.erp.vo.SalesOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.mc.erp.enums.SalesOrderStatus;

@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesOrderService {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private SalesOrderDetailService salesOrderDetailService;

    @Override
    public PageResult<SalesOrderVO> getPage(SalesOrderQuery query) {
        Page<SalesOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getOrderNo()), SalesOrder::getOrderNo, query.getOrderNo());
        wrapper.eq(query.getSalespersonId() != null, SalesOrder::getSalespersonId, query.getSalespersonId());
        wrapper.eq(query.getOperatorId() != null, SalesOrder::getOperatorId, query.getOperatorId());
        wrapper.eq(query.getCustomerId() != null, SalesOrder::getCustomerId, query.getCustomerId());
        wrapper.eq(StringUtils.hasText(query.getTradeTerm()), SalesOrder::getTradeTerm, query.getTradeTerm());
        wrapper.eq(StringUtils.hasText(query.getPaymentMethod()), SalesOrder::getPaymentMethod, query.getPaymentMethod());
        wrapper.eq(query.getStatus() != null, SalesOrder::getStatus, query.getStatus());
        wrapper.eq(query.getCreateId() != null, SalesOrder::getCreateId, query.getCreateId());
        wrapper.orderByDesc(SalesOrder::getCreateTime);

        Page<SalesOrder> resultPage = this.page(page, wrapper);

        Set<Long> userIds = resultPage.getRecords().stream()
                .flatMap(order -> Stream.of(order.getSalespersonId(), order.getCreateId(), order.getOperatorId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName));

        Set<Long> customerIds = resultPage.getRecords().stream().map(SalesOrder::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> customerNameMap = customerIds.isEmpty()
                ? Collections.emptyMap()
                : customerService.listByIds(customerIds).stream().collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));

        var voList = resultPage.getRecords().stream().map(order -> {
            SalesOrderVO vo = new SalesOrderVO();
            BeanUtils.copyProperties(order, vo);
            vo.setSalespersonName(userNameMap.get(order.getSalespersonId()));
            vo.setOperatorName(userNameMap.get(order.getOperatorId()));
            vo.setCreateUserName(userNameMap.get(order.getCreateId()));
            vo.setCustomerName(customerNameMap.get(order.getCustomerId()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public void calculateAndUpdateProfit(Long salesOrderId) {
        // 获取销售订单信息
        SalesOrder salesOrder = this.getById(salesOrderId);
        if (salesOrder == null) {
            throw new RuntimeException("销售订单不存在: " + salesOrderId);
        }

        // 查询所有关联到此销售订单的采购订单
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getSalesOrderNo, salesOrder.getOrderNo());
        List<PurchaseOrder> purchaseOrders = purchaseOrderService.list(wrapper);

        // 计算采购成本：所有采购订单的实际金额+运费总和
        BigDecimal totalPurchaseCost = purchaseOrders.stream()
                .map(po -> {
                    BigDecimal actualAmount = po.getActualAmount() != null ? po.getActualAmount() : BigDecimal.ZERO;
                    BigDecimal freight = po.getTotalFreight() != null ? po.getTotalFreight() : BigDecimal.ZERO;
                    return actualAmount.add(freight);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 利润计算公式：
        // 利润 = 定金收款金额*定金汇率 + (尾款收款金额-海运费-保险费用)*尾款汇率 - 港杂费 - 采购成本
        BigDecimal depositIncome = (salesOrder.getReceivedAmount() != null ? salesOrder.getReceivedAmount() : BigDecimal.ZERO)
                .multiply(salesOrder.getDepositExchangeRate() != null ? salesOrder.getDepositExchangeRate() : BigDecimal.ZERO);

        BigDecimal finalPayment = salesOrder.getFinalPaymentAmount() != null ? salesOrder.getFinalPaymentAmount() : BigDecimal.ZERO;
        BigDecimal seaFreight = salesOrder.getSeaFreight() != null ? salesOrder.getSeaFreight() : BigDecimal.ZERO;
        BigDecimal insuranceFee = salesOrder.getInsuranceFee() != null ? salesOrder.getInsuranceFee() : BigDecimal.ZERO;
        BigDecimal finalExchangeRate = salesOrder.getFinalExchangeRate() != null ? salesOrder.getFinalExchangeRate() : BigDecimal.ZERO;
        
        BigDecimal finalIncome = finalPayment.subtract(seaFreight).subtract(insuranceFee)
                .multiply(finalExchangeRate);

        BigDecimal portFee = salesOrder.getPortFee() != null ? salesOrder.getPortFee() : BigDecimal.ZERO;

        BigDecimal profit = depositIncome.add(finalIncome).subtract(portFee).subtract(totalPurchaseCost);
        System.out.println("Calculated profit for Sales Order " + salesOrder.getOrderNo() + ": " + depositIncome + " (deposit) + " + finalIncome + " (final) - " + portFee + " (port fee) - " + totalPurchaseCost + " (purchase cost) = " + profit);
        // 更新销售订单的利润
        salesOrder.setProfit(profit);
        this.updateById(salesOrder);
    }

    @Override
    public void calculateAndUpdateLoss(Long salesOrderId) {
        // 获取销售订单信息
        SalesOrder salesOrder = this.getById(salesOrderId);
        if (salesOrder == null) {
            throw new RuntimeException("销售订单不存在: " + salesOrderId);
        }

        // 查询该订单所有明细，汇总价格
        LambdaQueryWrapper<SalesOrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrderDetail::getOrderId, salesOrderId);
        List<SalesOrderDetail> details = salesOrderDetailService.list(wrapper);

        BigDecimal totalDetailPrice = details.stream()
                .map(d -> d.getPriceTotal() != null ? d.getPriceTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 损耗 = 定金收款金额 + 尾款金额 - 明细价格汇总之和
        BigDecimal receivedAmount = salesOrder.getReceivedAmount() != null ? salesOrder.getReceivedAmount() : BigDecimal.ZERO;
        BigDecimal finalPayment = salesOrder.getFinalPaymentAmount() != null ? salesOrder.getFinalPaymentAmount() : BigDecimal.ZERO;

        BigDecimal loss = receivedAmount.add(finalPayment).subtract(totalDetailPrice);
        System.out.println("Calculated loss for Sales Order " + salesOrder.getOrderNo() + ": "
                + receivedAmount + " (deposit) + " + finalPayment + " (final) - " + totalDetailPrice + " (detail total) = " + loss);

        salesOrder.setLoss(loss);
        this.updateById(salesOrder);
    }

    @Override
    public void updateStatus(Long id, Integer targetStatus) {
        SalesOrder order = this.getById(id);
        if (order == null) {
            throw new RuntimeException("销售订单不存在: " + id);
        }
        // 状态机校验：非法流转直接抛异常，由 GlobalExceptionHandler 统一返回 400
        SalesOrderStatus.validateTransition(order.getStatus(), targetStatus);

        SalesOrder update = new SalesOrder();
        update.setId(id);
        update.setStatus(targetStatus);
        this.updateById(update);

        // 流转到「已完成」时自动计算利润和损耗
        if (SalesOrderStatus.COMPLETED.getCode() == targetStatus) {
            calculateAndUpdateProfit(id);
            calculateAndUpdateLoss(id);
        }
    }
}
