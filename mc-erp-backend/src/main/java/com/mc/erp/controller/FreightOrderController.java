package com.mc.erp.controller;

import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.FreightOrderQuery;
import com.mc.erp.dto.FreightOrderRequest;
import com.mc.erp.entity.FreightOrder;
import com.mc.erp.entity.FreightOrderLog;
import com.mc.erp.enums.FreightOrderStatus;
import com.mc.erp.service.FreightOrderLogService;
import com.mc.erp.service.FreightOrderService;
import com.mc.erp.vo.FreightOrderVO;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/v1/freight-orders")
public class FreightOrderController {

    @Autowired
    private FreightOrderService freightOrderService;

    @Autowired
    private FreightOrderLogService logService;

    /**
     * 分页查询海运订单
     */
    @GetMapping("/page")
    public Result<PageResult<FreightOrderVO>> getPage(FreightOrderQuery query) {
        return Result.success(freightOrderService.getPage(query));
    }

    /**
     * 查询海运订单详情
     */
    @GetMapping("/{orderId}")
    public Result<FreightOrderVO> getDetail(@PathVariable Long orderId) {
        return Result.success(freightOrderService.getDetail(orderId));
    }

    /**
     * 创建海运订单
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody FreightOrderRequest request) {
        FreightOrder order = toOrder(request);
        return Result.success(freightOrderService.createOrder(order));
    }

    /**
     * 修改海运订单
     */
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody FreightOrderRequest request) {
        FreightOrder order = toOrder(request);
        return Result.success(freightOrderService.updateOrder(order));
    }

    /**
     * 删除海运订单（仅草稿）
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

    private FreightOrder toOrder(FreightOrderRequest request) {
        FreightOrder order = new FreightOrder();
        BeanUtils.copyProperties(request, order);
        // 将请求中的 oceanFreight/groundFee 映射到实体的 totalOceanFreight/totalGroundFee
        if (request.getOceanFreight() != null) {
            order.setTotalOceanFreight(request.getOceanFreight());
        }
        if (request.getGroundFee() != null) {
            order.setTotalGroundFee(request.getGroundFee());
        }
        return order;
    }
}
