package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FinancePaymentAuditDTO;
import com.mc.erp.dto.FinancePaymentQuery;
import com.mc.erp.dto.FinancePaymentSaveDTO;
import com.mc.erp.entity.FinancePayment;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.entity.User;
import com.mc.erp.mapper.FinancePaymentMapper;
import com.mc.erp.service.FinancePaymentService;
import com.mc.erp.service.OperationLogService;
import com.mc.erp.service.PurchaseOrderService;
import com.mc.erp.service.UserService;
import com.mc.erp.util.LogHelper;
import com.mc.erp.enums.OperationType;
import com.mc.erp.util.SecurityUtil;
import com.mc.erp.vo.FinancePaymentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FinancePaymentServiceImpl extends ServiceImpl<FinancePaymentMapper, FinancePayment>
        implements FinancePaymentService {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    /** 状态标签 */
    private String statusLabel(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 1 -> "待审核";
            case 2 -> "已审核";
            case 3 -> "已驳回";
            default -> "未知";
        };
    }

    /** 获取用户真实姓名 */
    private String getUserName(Long userId) {
        if (userId == null) return null;
        User user = userService.getById(userId);
        return user != null ? user.getRealName() : null;
    }

    private FinancePaymentVO toVO(FinancePayment r, Map<Long, String> userNameMap) {
        FinancePaymentVO vo = new FinancePaymentVO();
        BeanUtils.copyProperties(r, vo);
        vo.setStatusLabel(statusLabel(r.getStatus()));
        vo.setCreatorName(userNameMap.getOrDefault(r.getCreateId(), null));
        vo.setAuditByName(userNameMap.getOrDefault(r.getAuditBy(), null));
        return vo;
    }

    @Override
    public PageResult<FinancePaymentVO> getPage(FinancePaymentQuery query) {
        Page<FinancePayment> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<FinancePayment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getPaymentNo()), FinancePayment::getPaymentNo, query.getPaymentNo());
        wrapper.eq(query.getStatus() != null, FinancePayment::getStatus, query.getStatus());
        wrapper.ge(query.getAmountMin() != null, FinancePayment::getAmount, query.getAmountMin());
        wrapper.le(query.getAmountMax() != null, FinancePayment::getAmount, query.getAmountMax());
        wrapper.like(StringUtils.hasText(query.getPurchaseOrderNo()), FinancePayment::getPurchaseOrderNo, query.getPurchaseOrderNo());
        wrapper.orderByDesc(FinancePayment::getCreateTime);

        Page<FinancePayment> resultPage = this.page(page, wrapper);

        // 收集所有创建人和审核人ID，批量查询用户名
        List<Long> userIds = resultPage.getRecords().stream()
                .flatMap(r -> {
                    List<Long> ids = new java.util.ArrayList<>();
                    if (r.getCreateId() != null) ids.add(r.getCreateId());
                    if (r.getAuditBy() != null) ids.add(r.getAuditBy());
                    return ids.stream();
                })
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> userNameMap = userIds.isEmpty() ? Collections.emptyMap()
                : userService.listByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u.getRealName() != null ? u.getRealName() : u.getUsername()));

        List<FinancePaymentVO> voList = resultPage.getRecords().stream()
                .map(r -> toVO(r, userNameMap))
                .collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public FinancePaymentVO getDetail(Long id) {
        FinancePayment payment = this.getById(id);
        if (payment == null) return null;

        FinancePaymentVO vo = new FinancePaymentVO();
        BeanUtils.copyProperties(payment, vo);
        vo.setStatusLabel(statusLabel(payment.getStatus()));
        vo.setCreatorName(getUserName(payment.getCreateId()));
        vo.setAuditByName(getUserName(payment.getAuditBy()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePayment(FinancePaymentSaveDTO dto) {
        FinancePayment payment = new FinancePayment();
        BeanUtils.copyProperties(dto, payment);
        payment.setStatus(1); // 新建 → 待审核
        if (payment.getCurrency() == null || payment.getCurrency().isEmpty()) {
            payment.setCurrency("CNY");
        }
        this.save(payment);

        operationLogService.asyncSaveLog(LogHelper.buildSuccessLog("付款管理", OperationType.ADD,
                "新增付款单成功，金额：" + dto.getAmount() + "，采购订单号：" + dto.getPurchaseOrderNo()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePayment(FinancePaymentSaveDTO dto) {
        FinancePayment existing = this.getById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("付款单不存在: " + dto.getId());
        }
        // 已审核的付款单，非管理员不允许修改
        if (Integer.valueOf(2).equals(existing.getStatus()) && !SecurityUtil.isAdmin()) {
            operationLogService.asyncSaveLog(LogHelper.buildErrorLog("付款管理", OperationType.MODIFY, "修改付款单失败", "付款已审核，无权修改"));
            throw new AccessDeniedException("付款已审核，无权修改");
        }

        FinancePayment payment = new FinancePayment();
        BeanUtils.copyProperties(dto, payment);
        // 如果是被驳回的订单重新编辑，则重置为待审核
        if (Integer.valueOf(3).equals(existing.getStatus())) {
            payment.setStatus(1);
            payment.setAuditBy(null);
            payment.setAuditTime(null);
            payment.setAuditRemark(null);
        }
        this.updateById(payment);

        operationLogService.asyncSaveLog(LogHelper.buildSuccessLog("付款管理", OperationType.MODIFY,
                "修改付款单成功，ID：" + dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePayment(Long id) {
        FinancePayment existing = this.getById(id);
        if (existing != null && Integer.valueOf(2).equals(existing.getStatus())
                && StringUtils.hasText(existing.getPurchaseOrderNo())) {
            // 删除已审核的付款单后需要重新同步采购订单
            String poNo = existing.getPurchaseOrderNo();
            this.removeById(id);
            syncPurchaseOrderPayments(poNo);
        } else {
            this.removeById(id);
        }

        operationLogService.asyncSaveLog(LogHelper.buildSuccessLog("付款管理", OperationType.DELETE,
                "删除付款单成功，ID：" + id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(FinancePaymentAuditDTO dto) {
        if (!SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("只有管理员可以审核付款单");
        }

        FinancePayment payment = this.getById(dto.getId());
        if (payment == null) {
            throw new RuntimeException("付款单不存在: " + dto.getId());
        }
        if (!Integer.valueOf(1).equals(payment.getStatus())) {
            throw new IllegalStateException("只有待审核状态的付款单才能审核，当前状态：" + statusLabel(payment.getStatus()));
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (Boolean.TRUE.equals(dto.getApproved())) {
            // 审核通过
            payment.setStatus(2);
            payment.setAuditBy(currentUserId);
            payment.setAuditTime(LocalDateTime.now());
            payment.setAuditRemark(dto.getAuditRemark());
            this.updateById(payment);

            // 审核通过后，同步付款金额到采购订单
            if (StringUtils.hasText(payment.getPurchaseOrderNo())) {
                syncPurchaseOrderPayments(payment.getPurchaseOrderNo());
            }

            operationLogService.asyncSaveLog(LogHelper.buildSuccessLog("付款管理", OperationType.STATUS_CHANGE,
                    "审核通过付款单，ID：" + dto.getId()));
        } else {
            // 驳回
            payment.setStatus(3);
            payment.setAuditBy(currentUserId);
            payment.setAuditTime(LocalDateTime.now());
            payment.setAuditRemark(dto.getAuditRemark());
            this.updateById(payment);

            operationLogService.asyncSaveLog(LogHelper.buildSuccessLog("付款管理", OperationType.STATUS_CHANGE,
                    "驳回付款单，ID：" + dto.getId() + "，原因：" + dto.getAuditRemark()));
        }
    }

    /**
     * 同步某个采购订单的所有已审核付款金额
     * 汇总所有已审核付款单中该采购订单的定金、尾款、运费，更新到采购订单字段
     */
    private void syncPurchaseOrderPayments(String poNo) {
        PurchaseOrder po = purchaseOrderService.getOne(
                new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getPoNo, poNo));
        if (po == null) return;

        // 查询所有已审核的、关联该采购订单号的付款单
        List<FinancePayment> approvedPayments = this.list(
                new LambdaQueryWrapper<FinancePayment>()
                        .eq(FinancePayment::getStatus, 2)
                        .eq(FinancePayment::getPurchaseOrderNo, poNo));

        // 汇总定金付款
        BigDecimal totalDeposit = approvedPayments.stream()
                .filter(p -> Integer.valueOf(1).equals(p.getBindType()))
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 汇总尾款付款（暂不使用，但预留）
        // BigDecimal totalFinal = approvedPayments.stream()
        //         .filter(p -> Integer.valueOf(2).equals(p.getBindType()))
        //         .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
        //         .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 汇总运费付款
        BigDecimal totalFreight = approvedPayments.stream()
                .filter(p -> Integer.valueOf(3).equals(p.getBindType()))
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 更新采购订单的付款相关字段
        LambdaUpdateWrapper<PurchaseOrder> uw = new LambdaUpdateWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getId, po.getId())
                .set(PurchaseOrder::getDepositAmount, totalDeposit);

        if (totalFreight.compareTo(BigDecimal.ZERO) > 0) {
            uw.set(PurchaseOrder::getTotalFreight, totalFreight);
        }

        purchaseOrderService.update(null, uw);
    }
}
