package com.mc.erp.controller;

import com.mc.erp.common.Result;
import com.mc.erp.enums.PurchaseOrderStatus;
import com.mc.erp.enums.SalesOrderStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 状态字典接口 —— 前端通过此接口获取所有状态的 label/tagType/allowedFromCodes，
 * 无需在前端硬编码任何状态值。
 */
@RestController
@RequestMapping("/api/v1/status-meta")
public class StatusMetaController {

    @GetMapping("/sales-order")
    public Result<List<Map<String, Object>>> salesOrderStatus() {
        return Result.success(SalesOrderStatus.toMetaList());
    }

    @GetMapping("/purchase-order")
    public Result<List<Map<String, Object>>> purchaseOrderStatus() {
        return Result.success(PurchaseOrderStatus.toMetaList());
    }
}
