package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FreightOrderQuery;
import com.mc.erp.entity.*;
import com.mc.erp.enums.FreightOrderStatus;
import com.mc.erp.mapper.FreightOrderMapper;
import com.mc.erp.service.*;
import com.mc.erp.util.SecurityUtil;
import com.mc.erp.vo.FreightOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FreightOrderServiceImpl extends ServiceImpl<FreightOrderMapper, FreightOrder>
        implements FreightOrderService {

    @Autowired
    private FreightOrderLogService logService;

    @Autowired
    private FreightForwarderService freightForwarderService;

    @Autowired
    private SalesOrderService salesOrderService;

    // ============ 分页查询 ============

    @Override
    public PageResult<FreightOrderVO> getPage(FreightOrderQuery query) {
        Page<FreightOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<FreightOrder> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getOrderCode()), FreightOrder::getOrderCode, query.getOrderCode());
        wrapper.like(StringUtils.hasText(query.getSaleOrderCode()), FreightOrder::getSaleOrderCode, query.getSaleOrderCode());
        wrapper.eq(query.getForwarderId() != null, FreightOrder::getForwarderId, query.getForwarderId());
        wrapper.like(StringUtils.hasText(query.getForwarderName()), FreightOrder::getForwarderName, query.getForwarderName());
        wrapper.eq(query.getTransportType() != null, FreightOrder::getTransportType, query.getTransportType());
        wrapper.eq(StringUtils.hasText(query.getContainerType()), FreightOrder::getContainerType, query.getContainerType());
        wrapper.eq(query.getIsLcl() != null, FreightOrder::getIsLcl, query.getIsLcl());
        wrapper.eq(query.getNeedInsurance() != null, FreightOrder::getNeedInsurance, query.getNeedInsurance());
        wrapper.eq(query.getOrderStatus() != null, FreightOrder::getOrderStatus, query.getOrderStatus());
        wrapper.like(StringUtils.hasText(query.getCreateUser()), FreightOrder::getCreateUser, query.getCreateUser());

        if (StringUtils.hasText(query.getCreateTimeStart())) {
            wrapper.ge(FreightOrder::getCreateTime, query.getCreateTimeStart());
        }
        if (StringUtils.hasText(query.getCreateTimeEnd())) {
            wrapper.le(FreightOrder::getCreateTime, query.getCreateTimeEnd() + " 23:59:59");
        }

        // 模糊搜索：订单号 / 销售订单号 / 货代名称
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(FreightOrder::getOrderCode, query.getKeyword())
                    .or().like(FreightOrder::getSaleOrderCode, query.getKeyword())
                    .or().like(FreightOrder::getForwarderName, query.getKeyword()));
        }

        wrapper.orderByDesc(FreightOrder::getCreateTime);

        Page<FreightOrder> resultPage = this.page(page, wrapper);

        List<FreightOrderVO> voList = resultPage.getRecords().stream().map(this::toVO).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    // ============ 详情查询 ============

    @Override
    public FreightOrderVO getDetail(Long orderId) {
        FreightOrder order = this.getById(orderId);
        if (order == null) return null;

        FreightOrderVO vo = toVO(order);
        vo.setLogs(logService.listByOrderId(orderId));
        return vo;
    }

    // ============ 创建订单 ============

    @Override
    @Transactional
    public Long createOrder(FreightOrder order) {
        // 校验销售订单
        validateSaleOrderCode(order.getSaleOrderCode());
        // 校验货代
        FreightForwarder forwarder = validateForwarder(order.getForwarderId());
        order.setForwarderName(forwarder.getName());
        // 校验运输类型
        validateTransportType(order);
        // 校验保险
        validateInsurance(order);
        // 校验是否重复创建
        validateDuplicateOrder(order.getSaleOrderCode(), order.getForwarderId(), null);

        // 生成订单编号
        order.setOrderCode(generateOrderCode());
        order.setOrderStatus(FreightOrderStatus.DRAFT.getCode());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // 设置创建人
        if (!StringUtils.hasText(order.getCreateUser())) {
            order.setCreateUser(SecurityUtil.getCurrentUsername());
        }

        // 初始化费用
        initializeFees(order);

        // 计算合计
        recalculateTotalsFromFields(order);

        this.save(order);

        // 记录日志
        logService.log(order.getOrderId(), order.getOrderCode(), "CREATE", null, null, "创建海运订单");

        return order.getOrderId();
    }

    // ============ 修改订单 ============

    @Override
    @Transactional
    public boolean updateOrder(FreightOrder order) {
        FreightOrder existing = this.getById(order.getOrderId());
        if (existing == null) {
            throw new IllegalArgumentException("海运订单不存在");
        }

        int status = existing.getOrderStatus();

        if (status == FreightOrderStatus.CANCELLED.getCode()) {
            throw new IllegalStateException("已作废订单不允许修改");
        }

        if (status == FreightOrderStatus.SETTLED.getCode()) {
            // 已结算订单仅可新增备注
            if (StringUtils.hasText(order.getRemark())) {
                existing.setRemark(order.getRemark());
            }
            existing.setUpdateTime(LocalDateTime.now());
            this.updateById(existing);
            logService.log(existing.getOrderId(), existing.getOrderCode(), "UPDATE", null, null, "已结算订单修改备注");
            return true;
        }

        if (status == FreightOrderStatus.SUBMITTED.getCode()) {
            // 已提交订单仅可修改非核心字段
            if (StringUtils.hasText(order.getRemark())) existing.setRemark(order.getRemark());
            if (StringUtils.hasText(order.getDeparturePort())) existing.setDeparturePort(order.getDeparturePort());
            if (StringUtils.hasText(order.getDestinationPort())) existing.setDestinationPort(order.getDestinationPort());
            if (order.getShipDate() != null) existing.setShipDate(order.getShipDate());
            if (order.getEstimatedArrivalDate() != null) existing.setEstimatedArrivalDate(order.getEstimatedArrivalDate());
            if (StringUtils.hasText(order.getInsuranceRemark())) existing.setInsuranceRemark(order.getInsuranceRemark());
            existing.setUpdateTime(LocalDateTime.now());
            this.updateById(existing);

            logService.log(existing.getOrderId(), existing.getOrderCode(), "UPDATE", null, null, "修改已提交订单非核心字段");
            return true;
        }

        // 草稿状态：可修改所有字段
        if (StringUtils.hasText(order.getSaleOrderCode())) {
            validateSaleOrderCode(order.getSaleOrderCode());
            existing.setSaleOrderCode(order.getSaleOrderCode());
        }
        if (order.getForwarderId() != null) {
            FreightForwarder forwarder = validateForwarder(order.getForwarderId());
            existing.setForwarderId(order.getForwarderId());
            existing.setForwarderName(forwarder.getName());
        }
        if (order.getTransportType() != null) {
            existing.setTransportType(order.getTransportType());
        }

        // 更新运输相关字段
        existing.setContainerType(order.getContainerType());
        existing.setContainerQty(order.getContainerQty());
        existing.setIsLcl(order.getIsLcl());
        existing.setContainerNo(order.getContainerNo());
        existing.setBulkWeight(order.getBulkWeight());
        existing.setBulkVolume(order.getBulkVolume());
        existing.setShippingSpace(order.getShippingSpace());

        // 更新保险
        if (order.getNeedInsurance() != null) {
            existing.setNeedInsurance(order.getNeedInsurance());
            if (order.getNeedInsurance() == 1) {
                existing.setInsuredAmount(order.getInsuredAmount());
                existing.setPremium(order.getPremium());
                existing.setInsuranceCurrency(order.getInsuranceCurrency());
                existing.setInsuranceRemark(order.getInsuranceRemark());
            } else {
                existing.setInsuredAmount(BigDecimal.ZERO);
                existing.setPremium(BigDecimal.ZERO);
                existing.setInsuranceCurrency(null);
                existing.setInsuranceRemark(null);
            }
        }

        // 更新其他字段
        if (StringUtils.hasText(order.getOrderCurrency())) existing.setOrderCurrency(order.getOrderCurrency());
        if (StringUtils.hasText(order.getDeparturePort())) existing.setDeparturePort(order.getDeparturePort());
        if (StringUtils.hasText(order.getDestinationPort())) existing.setDestinationPort(order.getDestinationPort());
        if (order.getShipDate() != null) existing.setShipDate(order.getShipDate());
        if (order.getEstimatedArrivalDate() != null) existing.setEstimatedArrivalDate(order.getEstimatedArrivalDate());
        if (order.getRemark() != null) existing.setRemark(order.getRemark());

        validateTransportType(existing);
        validateInsurance(existing);
        validateDuplicateOrder(existing.getSaleOrderCode(), existing.getForwarderId(), existing.getOrderId());

        // 更新海运费和地面费用
        if (order.getTotalOceanFreight() != null) existing.setTotalOceanFreight(order.getTotalOceanFreight());
        if (order.getTotalGroundFee() != null) existing.setTotalGroundFee(order.getTotalGroundFee());
        recalculateTotalsFromFields(existing);

        existing.setUpdateTime(LocalDateTime.now());
        this.updateById(existing);

        logService.log(existing.getOrderId(), existing.getOrderCode(), "UPDATE", null, null, "修改草稿订单");
        return true;
    }

    // ============ 删除订单 ============

    @Override
    @Transactional
    public boolean deleteOrder(Long orderId) {
        FreightOrder order = this.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("海运订单不存在");
        }
        if (order.getOrderStatus() != FreightOrderStatus.DRAFT.getCode()) {
            throw new IllegalStateException("仅草稿状态的订单支持删除");
        }

        logService.log(orderId, order.getOrderCode(), "DELETE", null, null, "删除海运订单");
        return this.removeById(orderId);
    }

    // ============ 提交订单 ============

    @Override
    @Transactional
    public boolean submitOrder(Long orderId) {
        FreightOrder order = this.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("海运订单不存在");
        }

        FreightOrderStatus.validateTransition(order.getOrderStatus(), FreightOrderStatus.SUBMITTED.getCode());

        // 提交前验证必填字段
        validateForSubmit(order);

        order.setOrderStatus(FreightOrderStatus.SUBMITTED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);

        logService.log(orderId, order.getOrderCode(), "SUBMIT", null, null, "提交海运订单");
        return true;
    }

    // ============ 结算订单 ============

    @Override
    @Transactional
    public boolean settleOrder(Long orderId) {
        FreightOrder order = this.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("海运订单不存在");
        }

        FreightOrderStatus.validateTransition(order.getOrderStatus(), FreightOrderStatus.SETTLED.getCode());

        order.setOrderStatus(FreightOrderStatus.SETTLED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);

        // 结算后将关键费用数据回写到对应的销售订单
        syncToSalesOrder(order);

        logService.log(orderId, order.getOrderCode(), "SETTLE", null, null, "结算海运订单");
        return true;
    }

    /**
     * 将海运订单的结算数据同步回销售订单：
     *  - 海运费（totalOceanFreight） → seaFreight
     *  - 地面费用（totalGroundFee）  → portFee
     *  - 保险费（premium）          → insuranceFee
     *  - 目的港（destinationPort）   → destinationPort（仅当销售订单该字段为空时）
     * 同步完成后触发利润重新计算。
     */
    private void syncToSalesOrder(FreightOrder order) {
        if (!StringUtils.hasText(order.getSaleOrderCode())) return;

        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getOrderNo, order.getSaleOrderCode());
        SalesOrder salesOrder = salesOrderService.getOne(wrapper);
        if (salesOrder == null) return;

        // 海运费回写
        BigDecimal oceanFreight = order.getTotalOceanFreight() != null ? order.getTotalOceanFreight() : BigDecimal.ZERO;
        salesOrder.setSeaFreight(oceanFreight);

        // 地面费用 → 港杂费回写
        BigDecimal groundFee = order.getTotalGroundFee() != null ? order.getTotalGroundFee() : BigDecimal.ZERO;
        salesOrder.setPortFee(groundFee);

        // 保险费回写
        BigDecimal premium = order.getPremium() != null ? order.getPremium() : BigDecimal.ZERO;
        salesOrder.setInsuranceFee(premium);

        // 目的港回写（仅当销售订单目的港为空时填充）
        if (!StringUtils.hasText(salesOrder.getDestinationPort()) && StringUtils.hasText(order.getDestinationPort())) {
            salesOrder.setDestinationPort(order.getDestinationPort());
        }

        salesOrderService.updateById(salesOrder);
        // 重新计算利润
        salesOrderService.calculateAndUpdateProfit(salesOrder.getId());
    }

    // ============ 作废订单 ============

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, String cancelReason) {
        FreightOrder order = this.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("海运订单不存在");
        }
        if (!StringUtils.hasText(cancelReason)) {
            throw new IllegalArgumentException("作废原因不能为空");
        }

        FreightOrderStatus.validateTransition(order.getOrderStatus(), FreightOrderStatus.CANCELLED.getCode());

        // 已结算 → 已作废仅管理员可操作
        if (order.getOrderStatus() == FreightOrderStatus.SETTLED.getCode()) {
            if (!SecurityUtil.isAdmin()) {
                throw new IllegalStateException("仅管理员可作废已结算订单");
            }
        }

        order.setOrderStatus(FreightOrderStatus.CANCELLED.getCode());
        order.setCancelReason(cancelReason);
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);

        logService.log(orderId, order.getOrderCode(), "CANCEL", null, null, "作废海运订单，原因：" + cancelReason);
        return true;
    }

    // ============ 重新计算费用（基于字段直接计算）============

    private void recalculateTotalsFromFields(FreightOrder order) {
        BigDecimal oceanFreight = order.getTotalOceanFreight() != null ? order.getTotalOceanFreight() : BigDecimal.ZERO;
        BigDecimal groundFee = order.getTotalGroundFee() != null ? order.getTotalGroundFee() : BigDecimal.ZERO;
        BigDecimal premium = order.getPremium() != null ? order.getPremium() : BigDecimal.ZERO;
        order.setTotalOceanFreight(oceanFreight);
        order.setTotalGroundFee(groundFee);
        order.setTotalAmount(oceanFreight.add(groundFee).add(premium));
    }

    // ============ 私有方法 ============

    private FreightOrderVO toVO(FreightOrder order) {
        FreightOrderVO vo = new FreightOrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setTransportTypeLabel(order.getTransportType() == 1 ? "集装箱船" : "散货船");
        try {
            vo.setOrderStatusLabel(FreightOrderStatus.of(order.getOrderStatus()).getLabel());
        } catch (Exception ignored) {
        }
        return vo;
    }

    private String generateOrderCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "HD" + dateStr;

        // 查询当天最大流水号
        LambdaQueryWrapper<FreightOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(FreightOrder::getOrderCode, prefix);
        wrapper.orderByDesc(FreightOrder::getOrderCode);
        wrapper.last("LIMIT 1");

        FreightOrder last = this.getOne(wrapper, false);
        int seq = 1;
        if (last != null && last.getOrderCode() != null && last.getOrderCode().length() > prefix.length()) {
            try {
                seq = Integer.parseInt(last.getOrderCode().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
            }
        }

        return prefix + String.format("%06d", seq);
    }

    private void validateSaleOrderCode(String saleOrderCode) {
        if (!StringUtils.hasText(saleOrderCode)) {
            throw new IllegalArgumentException("销售订单号不能为空");
        }
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getOrderNo, saleOrderCode);
        long count = salesOrderService.count(wrapper);
        if (count == 0) {
            throw new IllegalArgumentException("销售订单不存在或已失效，请重新选择");
        }
    }

    private FreightForwarder validateForwarder(Long forwarderId) {
        if (forwarderId == null) {
            throw new IllegalArgumentException("货代ID不能为空");
        }
        FreightForwarder forwarder = freightForwarderService.getById(forwarderId);
        if (forwarder == null) {
            throw new IllegalArgumentException("货代不存在");
        }
        return forwarder;
    }

    private void validateTransportType(FreightOrder order) {
        if (order.getTransportType() == null || (order.getTransportType() != 1 && order.getTransportType() != 2)) {
            throw new IllegalArgumentException("运输类型必须为集装箱船(1)或散货船(2)");
        }
        if (order.getTransportType() == 1) {
            if (!StringUtils.hasText(order.getContainerType())) {
                throw new IllegalArgumentException("集装箱船运输必须录入柜型");
            }
            if (order.getContainerQty() == null || order.getContainerQty() <= 0) {
                throw new IllegalArgumentException("集装箱船运输必须录入柜量（正整数）");
            }
        }
        if (order.getTransportType() == 2) {
            if (order.getBulkWeight() == null || order.getBulkWeight().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("散货船运输必须录入散货重量");
            }
            if (order.getBulkVolume() == null || order.getBulkVolume().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("散货船运输必须录入散货体积");
            }
        }
    }

    private void validateInsurance(FreightOrder order) {
        if (order.getNeedInsurance() != null && order.getNeedInsurance() == 1) {
            if (order.getInsuredAmount() == null || order.getInsuredAmount().signum() < 0) {
                throw new IllegalArgumentException("保额不能为负数");
            }
            if (order.getPremium() == null || order.getPremium().signum() < 0) {
                throw new IllegalArgumentException("保费不能为负数");
            }
        }
    }

    private void validateDuplicateOrder(String saleOrderCode, Long forwarderId, Long excludeOrderId) {
        LambdaQueryWrapper<FreightOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FreightOrder::getSaleOrderCode, saleOrderCode);
        wrapper.eq(FreightOrder::getForwarderId, forwarderId);
        if (excludeOrderId != null) {
            wrapper.ne(FreightOrder::getOrderId, excludeOrderId);
        }
        long count = this.count(wrapper);
        if (count > 0) {
            throw new IllegalArgumentException("该销售订单已关联此货代的订单，不允许重复创建");
        }
    }

    private void validateForSubmit(FreightOrder order) {
        if (!StringUtils.hasText(order.getSaleOrderCode())) {
            throw new IllegalArgumentException("提交失败：销售订单号缺失");
        }
        if (order.getForwarderId() == null) {
            throw new IllegalArgumentException("提交失败：货代ID缺失");
        }
        if (order.getTransportType() == null) {
            throw new IllegalArgumentException("提交失败：运输类型缺失");
        }
        validateTransportType(order);
        if (order.getNeedInsurance() != null && order.getNeedInsurance() == 1) {
            if (order.getInsuredAmount() == null || order.getPremium() == null) {
                throw new IllegalArgumentException("提交失败：购买保险时保额和保费为必填项");
            }
        }
    }

    private void initializeFees(FreightOrder order) {
        if (order.getTotalOceanFreight() == null) order.setTotalOceanFreight(BigDecimal.ZERO);
        if (order.getTotalGroundFee() == null) order.setTotalGroundFee(BigDecimal.ZERO);
        if (order.getTotalAmount() == null) order.setTotalAmount(BigDecimal.ZERO);
        if (order.getNeedInsurance() == null) order.setNeedInsurance(0);
        if (order.getNeedInsurance() == 0) {
            order.setInsuredAmount(BigDecimal.ZERO);
            order.setPremium(BigDecimal.ZERO);
        }
        if (!StringUtils.hasText(order.getOrderCurrency())) {
            order.setOrderCurrency("USD");
        }
    }
}
