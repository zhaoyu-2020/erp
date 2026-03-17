package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.FinanceReceiptDetailDTO;
import com.mc.erp.dto.FinanceReceiptQuery;
import com.mc.erp.dto.FinanceReceiptSaveDTO;
import com.mc.erp.entity.FinanceReceipt;
import com.mc.erp.entity.FinanceReceiptDetail;
import com.mc.erp.mapper.FinanceReceiptDetailMapper;
import com.mc.erp.mapper.FinanceReceiptMapper;
import com.mc.erp.service.FinanceReceiptService;
import com.mc.erp.util.SecurityUtil;
import com.mc.erp.vo.FinanceReceiptDetailVO;
import com.mc.erp.vo.FinanceReceiptVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinanceReceiptServiceImpl extends ServiceImpl<FinanceReceiptMapper, FinanceReceipt>
        implements FinanceReceiptService {

    @Autowired
    private FinanceReceiptDetailMapper detailMapper;

    /** 状态标签 */
    private String statusLabel(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 1 -> "新建";
            case 2 -> "认领中";
            case 3 -> "完成";
            default -> "未知";
        };
    }

    /** 根据明细计算状态: 无明细→新建, 部分→认领中, 全额→完成 */
    private int calcStatus(BigDecimal totalAmount, List<FinanceReceiptDetailDTO> details) {
        if (details == null || details.isEmpty()) return 1;
        BigDecimal boundSum = details.stream()
                .map(d -> d.getBoundAmount() != null ? d.getBoundAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (boundSum.compareTo(totalAmount) >= 0) return 3;
        return 2;
    }

    @Override
    public PageResult<FinanceReceiptVO> getPage(FinanceReceiptQuery query) {
        Page<FinanceReceipt> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<FinanceReceipt> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getSerialNo()), FinanceReceipt::getSerialNo, query.getSerialNo());
        wrapper.eq(query.getStatus() != null, FinanceReceipt::getStatus, query.getStatus());
        wrapper.ge(query.getAmountMin() != null, FinanceReceipt::getAmount, query.getAmountMin());
        wrapper.le(query.getAmountMax() != null, FinanceReceipt::getAmount, query.getAmountMax());
        // 按销售订单号搜索：过滤存在关联明细的收款单
        if (StringUtils.hasText(query.getSalesOrderNo())) {
            LambdaQueryWrapper<FinanceReceiptDetail> subWrapper = new LambdaQueryWrapper<>();
            subWrapper.like(FinanceReceiptDetail::getSalesOrderNo, query.getSalesOrderNo().trim());
            List<Long> matchedReceiptIds = detailMapper.selectList(subWrapper)
                    .stream().map(FinanceReceiptDetail::getReceiptId).distinct().collect(Collectors.toList());
            if (matchedReceiptIds.isEmpty()) {
                return new PageResult<>(0L, Collections.emptyList());
            }
            wrapper.in(FinanceReceipt::getId, matchedReceiptIds);
        }
        wrapper.orderByDesc(FinanceReceipt::getCreateTime);

        Page<FinanceReceipt> resultPage = this.page(page, wrapper);

        // 批量查询所有记录的认领明细
        List<Long> receiptIds = resultPage.getRecords().stream().map(FinanceReceipt::getId).collect(Collectors.toList());
        List<FinanceReceiptDetail> allDetails = receiptIds.isEmpty() ? Collections.emptyList()
                : detailMapper.selectList(new LambdaQueryWrapper<FinanceReceiptDetail>()
                        .in(FinanceReceiptDetail::getReceiptId, receiptIds)
                        .orderByAsc(FinanceReceiptDetail::getId));

        List<FinanceReceiptVO> voList = resultPage.getRecords().stream().map(r -> {
            FinanceReceiptVO vo = new FinanceReceiptVO();
            BeanUtils.copyProperties(r, vo);
            vo.setStatusLabel(statusLabel(r.getStatus()));
            List<FinanceReceiptDetailVO> detailVOs = allDetails.stream()
                    .filter(d -> d.getReceiptId().equals(r.getId()))
                    .map(d -> {
                        FinanceReceiptDetailVO dvo = new FinanceReceiptDetailVO();
                        BeanUtils.copyProperties(d, dvo);
                        return dvo;
                    }).collect(Collectors.toList());
            vo.setDetails(detailVOs);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public FinanceReceiptVO getWithDetails(Long id) {
        FinanceReceipt receipt = this.getById(id);
        if (receipt == null) return null;

        FinanceReceiptVO vo = new FinanceReceiptVO();
        BeanUtils.copyProperties(receipt, vo);
        vo.setStatusLabel(statusLabel(receipt.getStatus()));

        LambdaQueryWrapper<FinanceReceiptDetail> dw = new LambdaQueryWrapper<>();
        dw.eq(FinanceReceiptDetail::getReceiptId, id).orderByAsc(FinanceReceiptDetail::getId);
        List<FinanceReceiptDetail> details = detailMapper.selectList(dw);
        List<FinanceReceiptDetailVO> detailVOs = details.stream().map(d -> {
            FinanceReceiptDetailVO dvo = new FinanceReceiptDetailVO();
            BeanUtils.copyProperties(d, dvo);
            return dvo;
        }).collect(Collectors.toList());
        vo.setDetails(detailVOs);
        return vo;
    }

    /** 校验明细绑定金额总和不超过收款金额 */
    private void validateBoundSum(BigDecimal totalAmount, List<FinanceReceiptDetailDTO> details) {
        if (details == null || details.isEmpty()) return;
        BigDecimal boundSum = details.stream()
                .map(d -> d.getBoundAmount() != null ? d.getBoundAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (boundSum.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException(
                    "明细绑定金额合计（" + boundSum.toPlainString() + "）超过收款金额（" + totalAmount.toPlainString() + "），请调整后重试");
        }
    }

    @Override
    @Transactional
    public void saveWithDetails(FinanceReceiptSaveDTO dto) {
        validateBoundSum(dto.getAmount(), dto.getDetails());
        FinanceReceipt receipt = new FinanceReceipt();
        BeanUtils.copyProperties(dto, receipt);
        receipt.setStatus(calcStatus(dto.getAmount(), dto.getDetails()));
        this.save(receipt);

        saveDetails(receipt.getId(), dto);
    }

    @Override
    @Transactional
    public void updateWithDetails(FinanceReceiptSaveDTO dto) {
        // 收款已完成时，非管理员不允许修改认领明细
        FinanceReceipt existing = this.getById(dto.getId());
        if (existing != null && Integer.valueOf(3).equals(existing.getStatus()) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("收款已完成，无权修改认领明细");
        }
        validateBoundSum(dto.getAmount(), dto.getDetails());
        FinanceReceipt receipt = new FinanceReceipt();
        BeanUtils.copyProperties(dto, receipt);
        receipt.setStatus(calcStatus(dto.getAmount(), dto.getDetails()));
        this.updateById(receipt);

        // Replace all details
        LambdaQueryWrapper<FinanceReceiptDetail> dw = new LambdaQueryWrapper<>();
        dw.eq(FinanceReceiptDetail::getReceiptId, dto.getId());
        detailMapper.delete(dw);

        saveDetails(dto.getId(), dto);
    }

    private void saveDetails(Long receiptId, FinanceReceiptSaveDTO dto) {
        List<FinanceReceiptDetailDTO> details = dto.getDetails();
        if (details == null || details.isEmpty()) return;
        for (FinanceReceiptDetailDTO d : details) {
            FinanceReceiptDetail detail = new FinanceReceiptDetail();
            BeanUtils.copyProperties(d, detail);
            detail.setReceiptId(receiptId);
            detail.setId(null); // always insert new
            detailMapper.insert(detail);
        }
    }
}
