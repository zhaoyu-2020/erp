package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.PurchaseOrderDetailQuery;
import com.mc.erp.entity.Product;
import com.mc.erp.entity.ProductType;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.entity.PurchaseOrderDetail;
import com.mc.erp.mapper.ProductMapper;
import com.mc.erp.mapper.ProductTypeMapper;
import com.mc.erp.mapper.PurchaseOrderDetailMapper;
import com.mc.erp.mapper.PurchaseOrderMapper;
import com.mc.erp.service.PurchaseOrderDetailService;
import com.mc.erp.vo.PurchaseOrderDetailVO;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PurchaseOrderDetailServiceImpl extends ServiceImpl<PurchaseOrderDetailMapper, PurchaseOrderDetail>
        implements PurchaseOrderDetailService {

    private final ProductMapper productMapper;
    private final ProductTypeMapper productTypeMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;

    @Override
    public PageResult<PurchaseOrderDetailVO> getPage(PurchaseOrderDetailQuery query) {
        Long resolvedOrderId = query.getPurchaseOrderId();
        if (StringUtils.hasText(query.getPoNo()) && resolvedOrderId == null) {
            LambdaQueryWrapper<PurchaseOrder> poWrapper = new LambdaQueryWrapper<PurchaseOrder>()
                    .eq(PurchaseOrder::getPoNo, query.getPoNo().trim())
                    .last("LIMIT 1");
            PurchaseOrder po = purchaseOrderMapper.selectOne(poWrapper);
            if (po != null) {
                resolvedOrderId = po.getId();
            } else {
                return new PageResult<>(0L, List.of());
            }
        }

        Page<PurchaseOrderDetail> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PurchaseOrderDetail> wrapper = new LambdaQueryWrapper<>();
        if (resolvedOrderId != null) {
            wrapper.eq(PurchaseOrderDetail::getPurchaseOrderId, resolvedOrderId);
        }
        if (StringUtils.hasText(query.getSpec())) {
            wrapper.like(PurchaseOrderDetail::getSpec, query.getSpec().trim());
        }
        if (StringUtils.hasText(query.getProductType())) {
            wrapper.like(PurchaseOrderDetail::getProductType, query.getProductType().trim());
        }
        if (StringUtils.hasText(query.getMaterial())) {
            wrapper.like(PurchaseOrderDetail::getMaterial, query.getMaterial().trim());
        }
        wrapper.orderByAsc(PurchaseOrderDetail::getPurchaseOrderId)
                .orderByAsc(PurchaseOrderDetail::getDetailSeq)
                .orderByAsc(PurchaseOrderDetail::getId);

        Page<PurchaseOrderDetail> resultPage = this.page(page, wrapper);

        List<Long> orderIds = resultPage.getRecords().stream()
                .map(PurchaseOrderDetail::getPurchaseOrderId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> poNoMap = orderIds.isEmpty() ? Map.of() :
                purchaseOrderMapper.selectList(new LambdaQueryWrapper<PurchaseOrder>()
                        .in(PurchaseOrder::getId, orderIds))
                        .stream()
                        .collect(Collectors.toMap(PurchaseOrder::getId, PurchaseOrder::getPoNo));

        List<PurchaseOrderDetailVO> voList = resultPage.getRecords().stream().map(d -> {
            PurchaseOrderDetailVO vo = toVO(d);
            vo.setPoNo(poNoMap.getOrDefault(d.getPurchaseOrderId(), ""));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public List<PurchaseOrderDetailVO> listByOrderId(Long orderId) {
        List<PurchaseOrderDetail> list = this.list(new LambdaQueryWrapper<PurchaseOrderDetail>()
                .eq(PurchaseOrderDetail::getPurchaseOrderId, orderId)
                .orderByAsc(PurchaseOrderDetail::getId));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean saveDetail(PurchaseOrderDetail detail) {
        ensureProductType(detail.getProductType());
        detail.setProductId(findOrCreateProduct(detail));
        detail.setPriceTotal(computePriceTotal(detail));
        return this.save(detail);
    }

    @Override
    @Transactional
    public boolean updateDetail(PurchaseOrderDetail detail) {
        ensureProductType(detail.getProductType());
        detail.setProductId(findOrCreateProduct(detail));
        detail.setPriceTotal(computePriceTotal(detail));
        return this.updateById(detail);
    }

    private BigDecimal computePriceTotal(PurchaseOrderDetail detail) {
        return detail.getPriceTotal();
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

    private Long findOrCreateProduct(PurchaseOrderDetail detail) {
        String spec = detail.getSpec();
        String type = detail.getProductType();
        String material = detail.getMaterial();
        String length = detail.getLength();
        String tolerance = detail.getTolerance();

        if (StringUtils.hasText(spec) && StringUtils.hasText(type)
                && StringUtils.hasText(material) && StringUtils.hasText(length)
                && StringUtils.hasText(tolerance)) {

            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                    .eq(Product::getSpec, spec.trim())
                    .eq(Product::getType, type.trim())
                    .eq(Product::getMaterial, material.trim())
                    .eq(Product::getLength, length.trim())
                    .eq(Product::getTolerance, tolerance.trim())
                    .last("LIMIT 1");

            Product existing = productMapper.selectOne(wrapper);
            if (existing != null) {
                return existing.getId();
            }
        }

        Product product = new Product();
        product.setSpec(StringUtils.hasText(spec) ? spec.trim() : null);
        product.setType(StringUtils.hasText(type) ? type.trim() : null);
        product.setMaterial(StringUtils.hasText(material) ? material.trim() : null);
        product.setLength(StringUtils.hasText(length) ? length.trim() : null);
        product.setTolerance(StringUtils.hasText(tolerance) ? tolerance.trim() : null);
        product.setMeterWeight("0");
        String nameParts = List.of(
                StringUtils.hasText(type) ? type.trim() : "",
                StringUtils.hasText(spec) ? spec.trim() : "",
                StringUtils.hasText(material) ? material.trim() : ""
        ).stream().filter(StringUtils::hasText).collect(Collectors.joining(" "));
        product.setNameCn(StringUtils.hasText(nameParts) ? nameParts : "未命名产品");
        product.setUnit("T");
        product.setDeclaration("");
        product.setTaxRefundRate(BigDecimal.ZERO);
        productMapper.insert(product);
        return product.getId();
    }

    private PurchaseOrderDetailVO toVO(PurchaseOrderDetail detail) {
        PurchaseOrderDetailVO vo = new PurchaseOrderDetailVO();
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }
}
