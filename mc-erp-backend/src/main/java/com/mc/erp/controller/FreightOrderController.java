package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.FreightOrderQuery;
import com.mc.erp.entity.FreightFeeItem;
import com.mc.erp.entity.FreightOrder;
import com.mc.erp.entity.FreightOrderLog;
import com.mc.erp.enums.FreightOrderStatus;
import com.mc.erp.service.FreightFeeItemService;
import com.mc.erp.service.FreightOrderLogService;
import com.mc.erp.service.FreightOrderService;
import com.mc.erp.vo.FreightOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/v1/freight-orders")
public class FreightOrderController {

    @Autowired
    private FreightOrderService freightOrderService;

    @Autowired
    private FreightFeeItemService feeItemService;

    @Autowired
    private FreightOrderLogService logService;

    /**
     * 分页查询货代订单
     */
    @GetMapping("/page")
    public Result<PageResult<FreightOrderVO>> getPage(FreightOrderQuery query) {
        return Result.success(freightOrderService.getPage(query));
    }

    /**
     * 查询货代订单详情
     */
    @GetMapping("/{orderId}")
    public Result<FreightOrderVO> getDetail(@PathVariable Long orderId) {
        return Result.success(freightOrderService.getDetail(orderId));
    }

    /**
     * 创建货代订单
     * 请求体：{ order: {...}, feeItems: [...] }
     */
    @PostMapping
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        FreightOrder order = mapToFreightOrder(body);
        List<FreightFeeItem> feeItems = mapToFeeItems(body);
        return Result.success(freightOrderService.createOrder(order, feeItems));
    }

    /**
     * 修改货代订单
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Map<String, Object> body) {
        FreightOrder order = mapToFreightOrder(body);
        List<FreightFeeItem> feeItems = mapToFeeItems(body);
        return Result.success(freightOrderService.updateOrder(order, feeItems));
    }

    /**
     * 删除货代订单（仅草稿）
     */
    @DeleteMapping("/{orderId}")
    public Result<Boolean> delete(@PathVariable Long orderId) {
        return Result.success(freightOrderService.deleteOrder(orderId));
    }

    /**
     * 提交订单（草稿 → 已提交）
     */
    @PutMapping("/{orderId}/submit")
    public Result<Boolean> submit(@PathVariable Long orderId) {
        return Result.success(freightOrderService.submitOrder(orderId));
    }

    /**
     * 结算订单（已提交 → 已结算）
     */
    @PutMapping("/{orderId}/settle")
    public Result<Boolean> settle(@PathVariable Long orderId) {
        return Result.success(freightOrderService.settleOrder(orderId));
    }

    /**
     * 作废订单
     */
    @PutMapping("/{orderId}/cancel")
    public Result<Boolean> cancel(@PathVariable Long orderId, @RequestBody Map<String, String> body) {
        String cancelReason = body.get("cancelReason");
        return Result.success(freightOrderService.cancelOrder(orderId, cancelReason));
    }

    /**
     * 获取费用明细列表
     */
    @GetMapping("/{orderId}/fee-items")
    public Result<List<FreightFeeItem>> getFeeItems(@PathVariable Long orderId) {
        return Result.success(feeItemService.listByOrderId(orderId));
    }

    /**
     * 保存费用明细（批量替换）
     */
    @PostMapping("/{orderId}/fee-items")
    public Result<Boolean> saveFeeItems(@PathVariable Long orderId, @RequestBody List<FreightFeeItem> items) {
        return Result.success(feeItemService.saveFeeItems(orderId, items));
    }

    /**
     * 删除单个费用明细
     */
    @DeleteMapping("/fee-items/{itemId}")
    public Result<Boolean> deleteFeeItem(@PathVariable Long itemId) {
        return Result.success(feeItemService.deleteFeeItem(itemId));
    }

    /**
     * 获取操作日志列表
     */
    @GetMapping("/{orderId}/logs")
    public Result<List<FreightOrderLog>> getLogs(@PathVariable Long orderId) {
        return Result.success(logService.listByOrderId(orderId));
    }

    /**
     * 获取订单状态元数据
     */
    @GetMapping("/status-meta")
    public Result<List<Map<String, Object>>> getStatusMeta() {
        return Result.success(FreightOrderStatus.toMetaList());
    }

    // ============ 工具方法：将Map转为实体 ============

    @SuppressWarnings("unchecked")
    private FreightOrder mapToFreightOrder(Map<String, Object> body) {
        Map<String, Object> orderMap = (Map<String, Object>) body.getOrDefault("order", body);
        FreightOrder order = new FreightOrder();

        if (orderMap.get("orderId") != null) order.setOrderId(toLong(orderMap.get("orderId")));
        order.setSaleOrderCode((String) orderMap.get("saleOrderCode"));
        if (orderMap.get("supplierId") != null) order.setSupplierId(toLong(orderMap.get("supplierId")));
        order.setSupplierName((String) orderMap.get("supplierName"));
        if (orderMap.get("transportType") != null) order.setTransportType(toInt(orderMap.get("transportType")));
        order.setContainerType((String) orderMap.get("containerType"));
        if (orderMap.get("containerQty") != null) order.setContainerQty(toInt(orderMap.get("containerQty")));
        if (orderMap.get("isLcl") != null) order.setIsLcl(toInt(orderMap.get("isLcl")));
        order.setContainerNo((String) orderMap.get("containerNo"));
        if (orderMap.get("bulkWeight") != null) order.setBulkWeight(toBigDecimal(orderMap.get("bulkWeight")));
        if (orderMap.get("bulkVolume") != null) order.setBulkVolume(toBigDecimal(orderMap.get("bulkVolume")));
        order.setShippingSpace((String) orderMap.get("shippingSpace"));
        if (orderMap.get("needInsurance") != null) order.setNeedInsurance(toInt(orderMap.get("needInsurance")));
        if (orderMap.get("insuredAmount") != null) order.setInsuredAmount(toBigDecimal(orderMap.get("insuredAmount")));
        if (orderMap.get("premium") != null) order.setPremium(toBigDecimal(orderMap.get("premium")));
        order.setInsuranceCurrency((String) orderMap.get("insuranceCurrency"));
        order.setInsuranceRemark((String) orderMap.get("insuranceRemark"));
        order.setOrderCurrency((String) orderMap.get("orderCurrency"));
        order.setDeparturePort((String) orderMap.get("departurePort"));
        order.setDestinationPort((String) orderMap.get("destinationPort"));
        order.setRemark((String) orderMap.get("remark"));
        order.setCreateUser((String) orderMap.get("createUser"));

        return order;
    }

    @SuppressWarnings("unchecked")
    private List<FreightFeeItem> mapToFeeItems(Map<String, Object> body) {
        Object feeItemsObj = body.get("feeItems");
        if (feeItemsObj == null) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) feeItemsObj;
        return list.stream().map(m -> {
            FreightFeeItem item = new FreightFeeItem();
            if (m.get("itemId") != null) item.setItemId(toLong(m.get("itemId")));
            if (m.get("orderId") != null) item.setOrderId(toLong(m.get("orderId")));
            if (m.get("feeType") != null) item.setFeeType(toInt(m.get("feeType")));
            item.setFeeName((String) m.get("feeName"));
            if (m.get("feeAmount") != null) item.setFeeAmount(toBigDecimal(m.get("feeAmount")));
            item.setCurrency((String) m.get("currency"));
            item.setBillingMethod((String) m.get("billingMethod"));
            item.setRemark((String) m.get("remark"));
            return item;
        }).toList();
    }

    private Long toLong(Object val) {
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) return Long.parseLong((String) val);
        return null;
    }

    private Integer toInt(Object val) {
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) return Integer.parseInt((String) val);
        return null;
    }

    private java.math.BigDecimal toBigDecimal(Object val) {
        if (val instanceof Number) return new java.math.BigDecimal(val.toString());
        if (val instanceof String) return new java.math.BigDecimal((String) val);
        return null;
    }
}
