package com.mc.erp.controller;

import com.alibaba.excel.EasyExcel;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.ImportResult;
import com.mc.erp.dto.PurchaseOrderDetailImportRow;
import com.mc.erp.dto.PurchaseOrderImportRow;
import com.mc.erp.dto.PurchaseOrderQuery;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.service.PurchaseOrderService;
import com.mc.erp.util.SecurityUtil;
import com.mc.erp.vo.PurchaseOrderVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@Validated
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping("/page")
    public Result<PageResult<PurchaseOrderVO>> getPage(PurchaseOrderQuery query) {
        return Result.success(purchaseOrderService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrder> getById(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody PurchaseOrder purchaseOrder) {
        return Result.success(purchaseOrderService.save(purchaseOrder));
    }

    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody PurchaseOrder purchaseOrder) {
        return Result.success(purchaseOrderService.updateById(purchaseOrder));
    }

    /**
     * 专用状态流转接口：PATCH /api/v1/purchase-orders/{id}/status
     * body: { "status": 2 } 或 { "status": 4, "totalFreight": 1200.00 }
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Integer targetStatus = (Integer) body.get("status");
        java.math.BigDecimal totalFreight = null;
        if (body.get("totalFreight") != null) {
            totalFreight = new java.math.BigDecimal(body.get("totalFreight").toString());
        }
        purchaseOrderService.updateStatus(id, targetStatus, totalFreight);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (!SecurityUtil.isAdmin()) {
            return Result.error(403, "仅管理员可删除采购订单");
        }
        return Result.success(purchaseOrderService.removeById(id));
    }

    // ---- Excel 导入 ----

    @PostMapping("/import/contract")
    public Result<ImportResult> importContract(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.success(purchaseOrderService.importContracts(file));
    }

    @PostMapping("/import/details")
    public Result<ImportResult> importDetails(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.success(purchaseOrderService.importDetails(file));
    }

    // ---- 模板下载 ----

    @GetMapping("/import/contract/template")
    public void downloadContractTemplate(HttpServletResponse response) throws Exception {
        String filename = URLEncoder.encode("采购订单合同导入模板.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + filename);
        EasyExcel.write(response.getOutputStream(), PurchaseOrderImportRow.class)
                .sheet("采购订单模板").doWrite(java.util.List.of());
    }

    @GetMapping("/import/details/template")
    public void downloadDetailsTemplate(HttpServletResponse response) throws Exception {
        String filename = URLEncoder.encode("采购订单明细导入模板.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + filename);
        EasyExcel.write(response.getOutputStream(), PurchaseOrderDetailImportRow.class)
                .sheet("采购明细模板").doWrite(java.util.List.of());
    }
}
