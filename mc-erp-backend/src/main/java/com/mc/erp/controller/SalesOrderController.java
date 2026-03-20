package com.mc.erp.controller;

import com.alibaba.excel.EasyExcel;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.ImportResult;
import com.mc.erp.dto.SalesOrderDetailImportRow;
import com.mc.erp.dto.SalesOrderImportRow;
import com.mc.erp.dto.SalesOrderQuery;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.service.SalesOrderService;
import com.mc.erp.util.SecurityUtil;
import com.mc.erp.vo.SalesOrderVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@Validated
@RequestMapping("/api/v1/sales-orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @GetMapping("/page")
    public Result<PageResult<SalesOrderVO>> getOrderPage(SalesOrderQuery query) {
        return Result.success(salesOrderService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<SalesOrder> getById(@PathVariable Long id) {
        return Result.success(salesOrderService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody SalesOrder salesOrder) {
        boolean success = salesOrderService.save(salesOrder);
        if (success && StringUtils.hasText(salesOrder.getOrderNo())) {
            salesOrderService.syncClaimAmounts(salesOrder.getOrderNo());
        }
        return Result.success(success);
    }

    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody SalesOrder salesOrder) {
        // 获取更新前的订单信息
        SalesOrder oldOrder = salesOrderService.getById(salesOrder.getId());

        // 更新订单
        boolean success = salesOrderService.updateById(salesOrder);

        if (success && StringUtils.hasText(salesOrder.getOrderNo())) {
            salesOrderService.syncClaimAmounts(salesOrder.getOrderNo());
        }
        
        // 如果状态变更为已完成（7），自动计算利润和损耗
        if (success && salesOrder.getStatus() != null && salesOrder.getStatus() == 7) {
            // 检查是否是状态发生变更
            if (oldOrder == null || !Integer.valueOf(7).equals(oldOrder.getStatus())) {
                salesOrderService.calculateAndUpdateProfit(salesOrder.getId());
                salesOrderService.calculateAndUpdateLoss(salesOrder.getId());
            }
        }
        
        return Result.success(success);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (!SecurityUtil.isAdmin()) {
            return Result.error(403, "仅管理员可删除销售订单");
        }
        return Result.success(salesOrderService.removeById(id));
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        salesOrderService.updateStatus(id, body.get("status"));
        return Result.success(null);
    }

    // ---- Excel 导入 ----

    @PostMapping("/import/contract")
    public Result<ImportResult> importContract(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.success(salesOrderService.importContracts(file));
    }

    @PostMapping("/import/details")
    public Result<ImportResult> importDetails(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.success(salesOrderService.importDetails(file));
    }

    // ---- 模板下载 ----

    @GetMapping("/import/contract/template")
    public void downloadContractTemplate(HttpServletResponse response) throws Exception {
        String filename = URLEncoder.encode("销售订单合同导入模板.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + filename);
        EasyExcel.write(response.getOutputStream(), SalesOrderImportRow.class)
                .sheet("销售订单模板").doWrite(java.util.List.of());
    }

    @GetMapping("/import/details/template")
    public void downloadDetailsTemplate(HttpServletResponse response) throws Exception {
        String filename = URLEncoder.encode("销售订单明细导入模板.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + filename);
        EasyExcel.write(response.getOutputStream(), SalesOrderDetailImportRow.class)
                .sheet("销售明细模板").doWrite(java.util.List.of());
    }
}
