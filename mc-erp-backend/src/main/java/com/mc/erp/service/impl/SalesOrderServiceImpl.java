package com.mc.erp.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.ImportResult;
import com.mc.erp.dto.SalesOrderDetailImportRow;
import com.mc.erp.dto.SalesOrderImportRow;
import com.mc.erp.dto.SalesOrderQuery;
import com.mc.erp.entity.Customer;
import com.mc.erp.entity.FinanceReceipt;
import com.mc.erp.entity.FinanceReceiptDetail;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.entity.SalesOrderDetail;
import com.mc.erp.entity.User;
import com.mc.erp.mapper.FinanceReceiptDetailMapper;
import com.mc.erp.mapper.FinanceReceiptMapper;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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

    @Autowired
    private FinanceReceiptDetailMapper financeReceiptDetailMapper;

    @Autowired
    private FinanceReceiptMapper financeReceiptMapper;

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
    public void syncClaimAmounts(String orderNo) {
        SalesOrder order = this.getOne(new LambdaQueryWrapper<SalesOrder>().eq(SalesOrder::getOrderNo, orderNo));
        if (order == null) return;

        // 先查出所有未删除的收款单ID（selectList 自动过滤 is_deleted=0）
        List<Long> validReceiptIds = financeReceiptMapper.selectList(
                new LambdaQueryWrapper<FinanceReceipt>().select(FinanceReceipt::getId))
                .stream().map(FinanceReceipt::getId).collect(Collectors.toList());

        // 查询该订单号下所有收款明细，且其所属收款单未被删除
        List<FinanceReceiptDetail> allDetails;
        if (validReceiptIds.isEmpty()) {
            allDetails = Collections.emptyList();
        } else {
            allDetails = financeReceiptDetailMapper.selectList(
                    new LambdaQueryWrapper<FinanceReceiptDetail>()
                            .eq(FinanceReceiptDetail::getSalesOrderNo, orderNo)
                            .in(FinanceReceiptDetail::getReceiptId, validReceiptIds));
        }

        // 分别汇总定金和尾款
        BigDecimal totalDeposit = allDetails.stream()
                .filter(d -> Integer.valueOf(1).equals(d.getBindType()))
                .map(d -> d.getBoundAmount() != null ? d.getBoundAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFinal = allDetails.stream()
                .filter(d -> Integer.valueOf(2).equals(d.getBindType()))
                .map(d -> d.getBoundAmount() != null ? d.getBoundAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算定金加权平均汇率
        BigDecimal depositWeightedSum = allDetails.stream()
                .filter(d -> Integer.valueOf(1).equals(d.getBindType()) && d.getExchangeRate() != null)
                .map(d -> d.getBoundAmount() != null ? d.getBoundAmount().multiply(d.getExchangeRate()) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgDepositRate = totalDeposit.compareTo(BigDecimal.ZERO) > 0
                ? depositWeightedSum.divide(totalDeposit, 4, java.math.RoundingMode.HALF_UP)
                : null;

        // 计算尾款加权平均汇率
        BigDecimal finalWeightedSum = allDetails.stream()
                .filter(d -> Integer.valueOf(2).equals(d.getBindType()) && d.getExchangeRate() != null)
                .map(d -> d.getBoundAmount() != null ? d.getBoundAmount().multiply(d.getExchangeRate()) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgFinalRate = totalFinal.compareTo(BigDecimal.ZERO) > 0
                ? finalWeightedSum.divide(totalFinal, 4, java.math.RoundingMode.HALF_UP)
                : null;

        // 计算应收定金和应收尾款（用于状态判断）
        BigDecimal contractAmount = order.getContractAmount() != null ? order.getContractAmount() : BigDecimal.ZERO;
        BigDecimal depositRate = order.getDepositRate() != null ? order.getDepositRate() : BigDecimal.ZERO;
        BigDecimal expectedDeposit = contractAmount.multiply(depositRate)
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal expectedFinal = contractAmount.subtract(expectedDeposit);

        // 只更新认领相关字段，不覆盖其他字段
        LambdaUpdateWrapper<SalesOrder> uw = new LambdaUpdateWrapper<SalesOrder>()
                .eq(SalesOrder::getId, order.getId())
                .set(SalesOrder::getReceivedAmount, totalDeposit)
                .set(SalesOrder::getFinalPaymentAmount, totalFinal)
                .set(SalesOrder::getActualAmount, totalDeposit.add(totalFinal));

        // 同步加权平均汇率（有收款数据时才覆盖，否则保留原值）
        if (avgDepositRate != null) {
            uw.set(SalesOrder::getDepositExchangeRate, avgDepositRate);
        }
        if (avgFinalRate != null) {
            uw.set(SalesOrder::getFinalExchangeRate, avgFinalRate);
        }

        // 更新状态：尾款足额 → 6，定金足额 → 2，否则不改
        if (expectedFinal.compareTo(BigDecimal.ZERO) > 0 && totalFinal.compareTo(expectedFinal) >= 0) {
            uw.set(SalesOrder::getStatus, 6);
        } else if (expectedDeposit.compareTo(BigDecimal.ZERO) > 0 && totalDeposit.compareTo(expectedDeposit) >= 0 && order.getStatus() < 2) {
            uw.set(SalesOrder::getStatus, 2);
        }

        this.update(null, uw);
    }

    @Override
    public void updateStatus(Long id, Integer targetStatus) {
        SalesOrder order = this.getById(id);
        if (order == null) {
            throw new RuntimeException("销售订单不存在: " + id);
        }
        // 状态机校验：非法流转直接抛异常，由 GlobalExceptionHandler 统一返回 400
        SalesOrderStatus.validateTransition(order.getStatus(), targetStatus);

        // 当状态被更改为收定金时，校验收款单必须大于0
        if (SalesOrderStatus.DEPOSIT_RECEIVED.getCode() == targetStatus) {
            if (order.getReceivedAmount() == null || order.getReceivedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("订单 " + order.getOrderNo() + " 的收款金额必须大于0，才能流转到「收定金」状态");
            }
        }
        // 当状态被更改为已采购时，校验至少有一条关联的采购合同
        if (SalesOrderStatus.PURCHASED.getCode() == targetStatus) {
            LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PurchaseOrder::getSalesOrderNo, order.getOrderNo());
            if (purchaseOrderService.count(wrapper) == 0) {
                throw new RuntimeException("订单 " + order.getOrderNo() + " 必须至少有一条关联的采购合同，才能流转到「已采购」状态");
            }
        }
        // 当状态被更改为已收款时，校验尾款金额必须大于0
        if (SalesOrderStatus.PAYMENT_RECEIVED.getCode() == targetStatus) {
            if (order.getFinalPaymentAmount() == null || order.getFinalPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("订单 " + order.getOrderNo() + " 的尾款金额必须大于0，才能流转到「收尾款」状态");
            }
        }



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

    @Override
    public ImportResult importContracts(MultipartFile file) throws Exception {
        List<SalesOrderImportRow> rows = EasyExcel.read(file.getInputStream())
                .head(SalesOrderImportRow.class)
                .sheet()
                .doReadSync();

        // 预加载用户和客户，建立name->id映射
        Map<String, Long> userNameToId = userService.list().stream()
                .filter(u -> StringUtils.hasText(u.getRealName()))
                .collect(Collectors.toMap(User::getRealName, User::getId, (a, b) -> a));
        Map<String, Long> customerNameToId = customerService.list().stream()
                .filter(c -> StringUtils.hasText(c.getName()))
                .collect(Collectors.toMap(Customer::getName, Customer::getId, (a, b) -> a));

        ImportResult result = new ImportResult();
        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2; // Excel第1行是表头，数据从第2行开始
            SalesOrderImportRow row = rows.get(i);
            try {
                if (!StringUtils.hasText(row.getOrderNo())) {
                    result.addError(rowNum, "订单号不能为空");
                    continue;
                }
                SalesOrder order = new SalesOrder();
                order.setOrderNo(row.getOrderNo().trim());
                // 业务员
                if (StringUtils.hasText(row.getSalespersonName())) {
                    Long uid = userNameToId.get(row.getSalespersonName().trim());
                    if (uid == null) {
                        result.addError(rowNum, "业务员姓名不存在: " + row.getSalespersonName());
                        continue;
                    }
                    order.setSalespersonId(uid);
                } else {
                    result.addError(rowNum, "业务员姓名不能为空");
                    continue;
                }
                // 操作员（可选，默认用业务员）
                if (StringUtils.hasText(row.getOperatorName())) {
                    Long uid = userNameToId.get(row.getOperatorName().trim());
                    if (uid == null) {
                        result.addError(rowNum, "操作员姓名不存在: " + row.getOperatorName());
                        continue;
                    }
                    order.setOperatorId(uid);
                } else {
                    order.setOperatorId(order.getSalespersonId());
                }
                // 客户
                if (StringUtils.hasText(row.getCustomerName())) {
                    Long cid = customerNameToId.get(row.getCustomerName().trim());
                    if (cid == null) {
                        result.addError(rowNum, "客户名称不存在: " + row.getCustomerName());
                        continue;
                    }
                    order.setCustomerId(cid);
                } else {
                    result.addError(rowNum, "客户名称不能为空");
                    continue;
                }
                order.setTradeTerm(row.getTradeTerm());
                order.setPaymentMethod(row.getPaymentMethod());
                order.setCurrency(row.getCurrency());
                order.setDepositExchangeRate(parseBD(row.getDepositExchangeRate()));
                order.setFinalExchangeRate(parseBD(row.getFinalExchangeRate()));
                order.setContractAmount(parseBD(row.getContractAmount()));
                order.setDepositRate(parseBD(row.getDepositRate()));
                order.setReceivedAmount(parseBD(row.getReceivedAmount()));
                order.setDestinationPort(StringUtils.hasText(row.getDestinationPort()) ? row.getDestinationPort() : "");
                order.setTransportType(row.getTransportType());
                order.setSeaFreight(parseBD(row.getSeaFreight()));
                order.setPortFee(parseBD(row.getPortFee()));
                if (StringUtils.hasText(row.getExpectedReceiptDays())) {
                    order.setExpectedReceiptDays(LocalDate.parse(row.getExpectedReceiptDays().trim()));
                }
                if (StringUtils.hasText(row.getDeliveryDate())) {
                    order.setDeliveryDate(LocalDate.parse(row.getDeliveryDate().trim()));
                }
                order.setStatus(1); // 新建
                this.save(order);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
        }
        return result;
    }

    @Override
    public ImportResult importDetails(MultipartFile file) throws Exception {
        List<SalesOrderDetailImportRow> rows = EasyExcel.read(file.getInputStream())
                .head(SalesOrderDetailImportRow.class)
                .sheet()
                .doReadSync();

        // 预加载销售订单，建立orderNo->id映射
        Map<String, Long> orderNoToId = this.list().stream()
                .collect(Collectors.toMap(SalesOrder::getOrderNo, SalesOrder::getId, (a, b) -> a));

        ImportResult result = new ImportResult();

        Set<Long> orderIds = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            SalesOrderDetailImportRow row = rows.get(i);
            try {
                // 校验必填项
                if (!StringUtils.hasText(row.getOrderNo())) {
                    result.addError(rowNum, "订单号不能为空");
                    continue;
                }
                if (!StringUtils.hasText(row.getSpec()) || !StringUtils.hasText(row.getProductType())
                        || !StringUtils.hasText(row.getMaterial()) || !StringUtils.hasText(row.getLength())
                        || !StringUtils.hasText(row.getTolerance())) {
                    result.addError(rowNum, "必填项不完整（产品规格/产品类型/材质/长度/公差）");
                    continue;
                }
                Long orderId = orderNoToId.get(row.getOrderNo().trim());
                if (orderId == null) {
                    result.addError(rowNum, "订单号不存在: " + row.getOrderNo());
                    continue;
                }
                orderIds.add(orderId);

                // 根据必填项组合查找已有记录（订单号+规格+产品类型+材质+长度+公差）
                LambdaQueryWrapper<SalesOrderDetail> existWrapper = new LambdaQueryWrapper<SalesOrderDetail>()
                        .eq(SalesOrderDetail::getOrderId, orderId)
                        .eq(SalesOrderDetail::getSpec, row.getSpec().trim())
                        .eq(SalesOrderDetail::getProductType, row.getProductType().trim())
                        .eq(SalesOrderDetail::getMaterial, row.getMaterial().trim())
                        .eq(SalesOrderDetail::getLength, row.getLength().trim())
                        .eq(SalesOrderDetail::getTolerance, row.getTolerance().trim())
                        .last("LIMIT 1");
                SalesOrderDetail existing = salesOrderDetailService.getOne(existWrapper);

                if (existing != null) {
                    // 老数据 —— 仅补充原来为空的字段，不覆盖已有数据
                    boolean changed = false;
                    if (existing.getSettlementPrice() == null && parseBD(row.getSettlementPrice()) != null) {
                        existing.setSettlementPrice(parseBD(row.getSettlementPrice())); changed = true;
                    }
                    if (existing.getMeasurementMethod() == null && StringUtils.hasText(row.getMeasurementMethod())) {
                        existing.setMeasurementMethod(row.getMeasurementMethod()); changed = true;
                    }
                    if (existing.getOrderedQuantity() == null && parseBD(row.getOrderedQuantity()) != null) {
                        existing.setOrderedQuantity(parseBD(row.getOrderedQuantity())); changed = true;
                    }
                    if (existing.getActualQuantity() == null && parseBD(row.getActualQuantity()) != null) {
                        existing.setActualQuantity(parseBD(row.getActualQuantity())); changed = true;
                    }
                    if (existing.getBundleCount() == null && parseInteger(row.getBundleCount()) != null) {
                        existing.setBundleCount(parseInteger(row.getBundleCount())); changed = true;
                    }
                    if (existing.getNetWeight() == null && parseBD(row.getNetWeight()) != null) {
                        existing.setNetWeight(parseBD(row.getNetWeight())); changed = true;
                    }
                    if (existing.getGrossWeight() == null && parseBD(row.getGrossWeight()) != null) {
                        existing.setGrossWeight(parseBD(row.getGrossWeight())); changed = true;
                    }
                    if (existing.getVolume() == null && parseBD(row.getVolume()) != null) {
                        existing.setVolume(parseBD(row.getVolume())); changed = true;
                    }
                    if (existing.getPackagingWeight() == null && parseBD(row.getPackagingWeight()) != null) {
                        existing.setPackagingWeight(parseBD(row.getPackagingWeight())); changed = true;
                    }
                    if (existing.getPackaging() == null && StringUtils.hasText(row.getPackaging())) {
                        existing.setPackaging(row.getPackaging()); changed = true;
                    }
                    if (existing.getCoilInnerDiameter() == null && StringUtils.hasText(row.getCoilInnerDiameter())) {
                        existing.setCoilInnerDiameter(row.getCoilInnerDiameter()); changed = true;
                    }
                    if (existing.getProcessingItems() == null && StringUtils.hasText(row.getProcessingItems())) {
                        existing.setProcessingItems(row.getProcessingItems()); changed = true;
                    }
                    if (existing.getRemark() == null && StringUtils.hasText(row.getRemark())) {
                        existing.setRemark(row.getRemark()); changed = true;
                    }
                    if (existing.getOriginPlace() == null && StringUtils.hasText(row.getOriginPlace())) {
                        existing.setOriginPlace(row.getOriginPlace()); changed = true;
                    }
                    if (changed) {
                        salesOrderDetailService.updateById(existing);
                    }
                    result.setUpdateCount(result.getUpdateCount() + 1);
                } else {
                    // 新数据 —— 插入
                    SalesOrderDetail detail = new SalesOrderDetail();
                    detail.setOrderId(orderId);
                    detail.setSpec(row.getSpec());
                    detail.setProductType(row.getProductType());
                    detail.setMaterial(row.getMaterial());
                    detail.setLength(row.getLength());
                    detail.setTolerance(row.getTolerance());
                    detail.setSettlementPrice(parseBD(row.getSettlementPrice()));
                    detail.setMeasurementMethod(row.getMeasurementMethod());
                    detail.setPackagingWeight(parseBD(row.getPackagingWeight()));
                    detail.setPackaging(row.getPackaging());
                    detail.setCoilInnerDiameter(row.getCoilInnerDiameter());
                    detail.setProcessingItems(row.getProcessingItems());
                    detail.setRemark(row.getRemark());
                    detail.setOrderedQuantity(parseBD(row.getOrderedQuantity()));
                    detail.setActualQuantity(parseBD(row.getActualQuantity()));
                    detail.setBundleCount(parseInteger(row.getBundleCount()));
                    detail.setNetWeight(parseBD(row.getNetWeight()));
                    detail.setGrossWeight(parseBD(row.getGrossWeight()));
                    detail.setVolume(parseBD(row.getVolume()));
                    detail.setOriginPlace(row.getOriginPlace());
                    salesOrderDetailService.saveDetail(detail);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                }
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
        }
        // 批量更新相关订单的总数量和总金额
        for (Long orderId : orderIds) {
            salesOrderDetailService.recalculateOrderSummary(orderId);
        }
        return result;
    }

    private BigDecimal parseBD(String val) {
        if (!StringUtils.hasText(val)) return null;
        try { return new BigDecimal(val.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Integer parseInteger(String val) {
        if (!StringUtils.hasText(val)) return null;
        try { return Integer.parseInt(val.trim()); } catch (NumberFormatException e) { return null; }
    }
}
