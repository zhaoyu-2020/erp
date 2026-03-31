package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.SalesOrderDetailQuery;
import com.mc.erp.entity.Product;
import com.mc.erp.entity.ProductType;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.entity.SalesOrderDetail;
import com.mc.erp.mapper.ProductMapper;
import com.mc.erp.mapper.ProductTypeMapper;
import com.mc.erp.mapper.SalesOrderDetailMapper;
import com.mc.erp.mapper.SalesOrderMapper;
import com.mc.erp.service.SalesOrderDetailService;
import com.mc.erp.vo.SalesOrderDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalesOrderDetailServiceImpl extends ServiceImpl<SalesOrderDetailMapper, SalesOrderDetail>
        implements SalesOrderDetailService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductTypeMapper productTypeMapper;

    @Autowired
    private SalesOrderMapper salesOrderMapper;

    @Override
    public PageResult<SalesOrderDetailVO> getPage(SalesOrderDetailQuery query) {
        // If filtering by orderNo, resolve to orderId first
        Long resolvedOrderId = query.getOrderId();
        if (StringUtils.hasText(query.getOrderNo()) && resolvedOrderId == null) {
            LambdaQueryWrapper<SalesOrder> soWrapper = new LambdaQueryWrapper<SalesOrder>()
                    .eq(SalesOrder::getOrderNo, query.getOrderNo().trim())
                    .last("LIMIT 1");
            SalesOrder so = salesOrderMapper.selectOne(soWrapper);
            if (so != null) {
                resolvedOrderId = so.getId();
            } else {
                // orderNo provided but not found — return empty
                return new PageResult<>(0L, List.of());
            }
        }

        Page<SalesOrderDetail> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SalesOrderDetail> wrapper = new LambdaQueryWrapper<>();
        if (resolvedOrderId != null) {
            wrapper.eq(SalesOrderDetail::getOrderId, resolvedOrderId);
        }
        if (StringUtils.hasText(query.getSpec())) {
            wrapper.like(SalesOrderDetail::getSpec, query.getSpec().trim());
        }
        if (StringUtils.hasText(query.getProductType())) {
            wrapper.like(SalesOrderDetail::getProductType, query.getProductType().trim());
        }
        if (StringUtils.hasText(query.getMaterial())) {
            wrapper.like(SalesOrderDetail::getMaterial, query.getMaterial().trim());
        }
        wrapper.orderByAsc(SalesOrderDetail::getOrderId).orderByAsc(SalesOrderDetail::getId);

        Page<SalesOrderDetail> resultPage = this.page(page, wrapper);

        // Resolve orderNo for each detail
        List<Long> orderIds = resultPage.getRecords().stream()
                .map(SalesOrderDetail::getOrderId).distinct().collect(Collectors.toList());
        Map<Long, String> orderNoMap = orderIds.isEmpty() ? Map.of() :
                salesOrderMapper.selectList(new LambdaQueryWrapper<SalesOrder>()
                        .in(SalesOrder::getId, orderIds))
                        .stream().collect(Collectors.toMap(SalesOrder::getId, SalesOrder::getOrderNo));

        List<SalesOrderDetailVO> voList = resultPage.getRecords().stream().map(d -> {
            SalesOrderDetailVO vo = toVO(d);
            vo.setOrderNo(orderNoMap.getOrDefault(d.getOrderId(), ""));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public List<SalesOrderDetailVO> listByOrderId(Long orderId) {
        List<SalesOrderDetail> list = this.list(new LambdaQueryWrapper<SalesOrderDetail>()
                .eq(SalesOrderDetail::getOrderId, orderId)
                .orderByAsc(SalesOrderDetail::getId));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDetail(SalesOrderDetail detail) {
        ensureProductType(detail.getProductType());
        detail.setProductId(findOrCreateProduct(detail));
        detail.setPriceTotal(computePriceTotal(detail));
        detail.setSettlementAmount(computeSettlementAmount(detail));
        boolean result = this.save(detail);
        if (result && detail.getOrderId() != null) {
            recalculateOrderSummary(detail.getOrderId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDetail(SalesOrderDetail detail) {
        ensureProductType(detail.getProductType());
        detail.setProductId(findOrCreateProduct(detail));
        detail.setPriceTotal(computePriceTotal(detail));
        detail.setSettlementAmount(computeSettlementAmount(detail));
        boolean result = this.updateById(detail);
        if (result && detail.getOrderId() != null) {
            recalculateOrderSummary(detail.getOrderId());
        }
        return result;
    }

    private BigDecimal computePriceTotal(SalesOrderDetail detail) {
        // 优先使用 orderedQuantity 计算价格汇总
        BigDecimal qty = detail.getOrderedQuantity();
        if (qty == null) {
            return detail.getPriceTotal();
        }
        // 使用结算价格计算
        if (detail.getSettlementPrice() != null) {
            return detail.getSettlementPrice().multiply(qty);
        }
        return detail.getPriceTotal();
    }

    private BigDecimal computeSettlementAmount(SalesOrderDetail detail) {
        BigDecimal qty = detail.getActualQuantity();
        if (qty == null) {
            return detail.getSettlementAmount();
        }
        if (detail.getSettlementPrice() != null) {
            return detail.getSettlementPrice().multiply(qty);
        }
        return detail.getSettlementAmount();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDetail(Long id) {
        SalesOrderDetail detail = this.getById(id);
        Long orderId = detail != null ? detail.getOrderId() : null;
        boolean result = this.removeById(id);
        if (result && orderId != null) {
            recalculateOrderSummary(orderId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateOrderSummary(Long orderId) {
        List<SalesOrderDetail> details = this.list(
                new LambdaQueryWrapper<SalesOrderDetail>().eq(SalesOrderDetail::getOrderId, orderId));

        BigDecimal totalOrdered = BigDecimal.ZERO;
        BigDecimal totalActual = BigDecimal.ZERO;
        BigDecimal contractAmount = BigDecimal.ZERO;
        BigDecimal settlementTotalAmount = BigDecimal.ZERO;

        for (SalesOrderDetail detail : details) {
            // 补全产品类型、产品 ID 以及 priceTotal
            ensureProductType(detail.getProductType());
            detail.setProductId(findOrCreateProduct(detail));
            detail.setPriceTotal(computePriceTotal(detail));
            detail.setSettlementAmount(computeSettlementAmount(detail));
            this.updateById(detail);

            if (detail.getOrderedQuantity() != null) {
                totalOrdered = totalOrdered.add(detail.getOrderedQuantity());
            }
            if (detail.getActualQuantity() != null) {
                totalActual = totalActual.add(detail.getActualQuantity());
            }
            if (detail.getOrderedQuantity() != null && detail.getSettlementPrice() != null) {
                contractAmount = contractAmount.add(detail.getOrderedQuantity().multiply(detail.getSettlementPrice()));
            }
            if (detail.getSettlementAmount() != null) {
                settlementTotalAmount = settlementTotalAmount.add(detail.getSettlementAmount());
            }
        }

        // 更新销售订单的汇总数量
        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setContractTotalQuantity(totalOrdered);
        order.setSettlementTotalQuantity(totalActual);
        order.setContractAmount(contractAmount);
        order.setSettlementTotalAmount(settlementTotalAmount);
        salesOrderMapper.updateById(order);
    }

    private void ensureProductType(String typeName) {
        if (!StringUtils.hasText(typeName)) return;
        String normalized = typeName.trim();
        long count = productTypeMapper.selectCount(new LambdaQueryWrapper<ProductType>()
                .eq(ProductType::getTypeName, normalized));
        if (count == 0) {
            ProductType pt = new ProductType();
            pt.setTypeName(normalized);
            productTypeMapper.insert(pt);
        }
    }

    private Long findOrCreateProduct(SalesOrderDetail detail) {
        String spec = detail.getSpec();
        String type = detail.getProductType();
        String material = detail.getMaterial();
        String length = detail.getLength();
        String tolerance = detail.getTolerance();

        // 五项标识字段全部为空时不创建产品记录
        if (!StringUtils.hasText(spec) && !StringUtils.hasText(type)
                && !StringUtils.hasText(material) && !StringUtils.hasText(length)
                && !StringUtils.hasText(tolerance)) {
            return null;
        }

        // 查找产品品名 ID
        Long productTypeId = null;
        if (StringUtils.hasText(type)) {
            ProductType pt = productTypeMapper.selectOne(new LambdaQueryWrapper<ProductType>()
                    .eq(ProductType::getTypeName, type.trim()).last("LIMIT 1"));
            if (pt != null) productTypeId = pt.getId();
        }

        // Only attempt deduplication when all 5 identifying fields are present
        if (StringUtils.hasText(spec) && productTypeId != null
                && StringUtils.hasText(material) && StringUtils.hasText(length)
                && StringUtils.hasText(tolerance)) {

            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                    .eq(Product::getSpec, spec.trim())
                    .eq(Product::getProductTypeId, productTypeId)
                    .eq(Product::getMaterial, material.trim())
                    .eq(Product::getLength, length.trim())
                    .eq(Product::getTolerance, tolerance.trim())
                    .last("LIMIT 1");

            Product existing = productMapper.selectOne(wrapper);
            if (existing != null) {
                return existing.getId();
            }
        }

        // Create a new product record
        Product product = new Product();
        product.setSpec(StringUtils.hasText(spec) ? spec.trim() : null);
        product.setProductTypeId(productTypeId);
        product.setMaterial(StringUtils.hasText(material) ? material.trim() : null);
        product.setLength(StringUtils.hasText(length) ? length.trim() : null);
        product.setTolerance(StringUtils.hasText(tolerance) ? tolerance.trim() : null);
        product.setMeterWeight("0");
        product.setUnit("T");
        product.setDeclaration("");
        product.setTaxRefundRate(BigDecimal.ZERO);
        productMapper.insert(product);
        return product.getId();
    }

    private SalesOrderDetailVO toVO(SalesOrderDetail detail) {
        SalesOrderDetailVO vo = new SalesOrderDetailVO();
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }
}
