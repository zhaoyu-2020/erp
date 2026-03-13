package com.mc.erp.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.ImportResult;
import com.mc.erp.dto.PurchaseOrderDetailImportRow;
import com.mc.erp.dto.PurchaseOrderImportRow;
import com.mc.erp.dto.PurchaseOrderQuery;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.entity.PurchaseOrderDetail;
import com.mc.erp.entity.Supplier;
import com.mc.erp.entity.User;
import com.mc.erp.mapper.PurchaseOrderMapper;
import com.mc.erp.service.PurchaseOrderDetailService;
import com.mc.erp.service.PurchaseOrderService;
import com.mc.erp.service.SupplierService;
import com.mc.erp.service.UserService;
import com.mc.erp.vo.PurchaseOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import com.mc.erp.enums.PurchaseOrderStatus;

@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
        implements PurchaseOrderService {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private UserService userService;

    @Override
    public PageResult<PurchaseOrderVO> getPage(PurchaseOrderQuery query) {
        Page<PurchaseOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getPoNo()), PurchaseOrder::getPoNo, query.getPoNo());
        wrapper.like(StringUtils.hasText(query.getSalesOrderNo()), PurchaseOrder::getSalesOrderNo, query.getSalesOrderNo());
        wrapper.eq(query.getStatus() != null, PurchaseOrder::getStatus, query.getStatus());
        wrapper.eq(query.getSalespersonId() != null, PurchaseOrder::getSalespersonId, query.getSalespersonId());
        wrapper.eq(query.getCreateId() != null, PurchaseOrder::getCreateId, query.getCreateId());
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);

        Page<PurchaseOrder> resultPage = this.page(page, wrapper);

        Set<Long> supplierIds = resultPage.getRecords().stream().map(PurchaseOrder::getSupplierId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> supplierNameMap = supplierIds.isEmpty()
                ? Collections.emptyMap()
                : supplierService.listByIds(supplierIds).stream().collect(Collectors.toMap(Supplier::getId, Supplier::getName));

        Set<Long> userIds = resultPage.getRecords().stream()
                .flatMap(po -> java.util.stream.Stream.of(po.getSalespersonId(), po.getCreateId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(userIds).stream().collect(Collectors.toMap(u -> u.getId(), u -> u.getRealName()));

        var voList = resultPage.getRecords().stream().map(po -> {
            PurchaseOrderVO vo = new PurchaseOrderVO();
            BeanUtils.copyProperties(po, vo);
            vo.setSupplierName(supplierNameMap.get(po.getSupplierId()));
            vo.setSalespersonName(userNameMap.get(po.getSalespersonId()));
            vo.setCreateUserName(userNameMap.get(po.getCreateId()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public void updateStatus(Long id, Integer targetStatus) {
        PurchaseOrder order = this.getById(id);
        if (order == null) {
            throw new RuntimeException("采购订单不存在: " + id);
        }
        PurchaseOrderStatus.validateTransition(order.getStatus(), targetStatus);

        PurchaseOrder update = new PurchaseOrder();
        update.setId(id);
        update.setStatus(targetStatus);
        this.updateById(update);
    }

    @Autowired
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Override
    public ImportResult importContracts(MultipartFile file) throws Exception {
        List<PurchaseOrderImportRow> rows = EasyExcel.read(file.getInputStream())
                .head(PurchaseOrderImportRow.class)
                .sheet()
                .doReadSync();

        Map<String, Long> supplierNameToId = supplierService.list().stream()
                .filter(s -> StringUtils.hasText(s.getName()))
                .collect(Collectors.toMap(Supplier::getName, Supplier::getId, (a, b) -> a));
        Map<String, Long> userNameToId = userService.list().stream()
                .filter(u -> StringUtils.hasText(u.getRealName()))
                .collect(Collectors.toMap(User::getRealName, User::getId, (a, b) -> a));

        ImportResult result = new ImportResult();
        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            PurchaseOrderImportRow row = rows.get(i);
            try {
                if (!StringUtils.hasText(row.getPoNo())) {
                    result.addError(rowNum, "采购单号不能为空");
                    continue;
                }
                PurchaseOrder order = new PurchaseOrder();
                order.setPoNo(row.getPoNo().trim());
                order.setSalesOrderNo(row.getSalesOrderNo());
                if (StringUtils.hasText(row.getSupplierName())) {
                    Long sid = supplierNameToId.get(row.getSupplierName().trim());
                    if (sid == null) {
                        result.addError(rowNum, "供应商名称不存在: " + row.getSupplierName());
                        continue;
                    }
                    order.setSupplierId(sid);
                } else {
                    result.addError(rowNum, "供应商名称不能为空");
                    continue;
                }
                if (StringUtils.hasText(row.getSalespersonName())) {
                    Long uid = userNameToId.get(row.getSalespersonName().trim());
                    order.setSalespersonId(uid);
                }
                order.setTotalAmount(parseBD(row.getTotalAmount()));
                order.setActualAmount(parseBD(row.getActualAmount()));
                order.setDepositRate(parseBD(row.getDepositRate()));
                order.setDepositAmount(parseBD(row.getDepositAmount()));
                order.setTransportRemark(row.getTransportRemark());
                order.setTotalFreight(parseBD(row.getTotalFreight()));
                if (StringUtils.hasText(row.getOrderDate())) {
                    order.setOrderDate(LocalDate.parse(row.getOrderDate().trim()));
                }
                if (StringUtils.hasText(row.getDeliveryDate())) {
                    order.setDeliveryDate(LocalDate.parse(row.getDeliveryDate().trim()));
                }
                order.setStatus(1);
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
        List<PurchaseOrderDetailImportRow> rows = EasyExcel.read(file.getInputStream())
                .head(PurchaseOrderDetailImportRow.class)
                .sheet()
                .doReadSync();

        Map<String, Long> poNoToId = this.list().stream()
                .collect(Collectors.toMap(PurchaseOrder::getPoNo, PurchaseOrder::getId, (a, b) -> a));

        ImportResult result = new ImportResult();
        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            PurchaseOrderDetailImportRow row = rows.get(i);
            try {
                if (!StringUtils.hasText(row.getPoNo())) {
                    result.addError(rowNum, "采购单号不能为空");
                    continue;
                }
                Long purchaseOrderId = poNoToId.get(row.getPoNo().trim());
                if (purchaseOrderId == null) {
                    result.addError(rowNum, "采购单号不存在: " + row.getPoNo());
                    continue;
                }
                PurchaseOrderDetail detail = new PurchaseOrderDetail();
                detail.setPurchaseOrderId(purchaseOrderId);
                detail.setSpec(row.getSpec());
                detail.setProductType(row.getProductType());
                detail.setMaterial(row.getMaterial());
                detail.setLength(row.getLength());
                detail.setTolerance(row.getTolerance());
                detail.setQuantityTon(parseBD(row.getQuantityTon()));
                detail.setQuantityPc(parseInteger(row.getQuantityPc()));
                detail.setQuantityMeter(parseBD(row.getQuantityMeter()));
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
                detail.setActualTheoreticalWeight(parseBD(row.getActualTheoreticalWeight()));
                purchaseOrderDetailService.save(detail);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
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
