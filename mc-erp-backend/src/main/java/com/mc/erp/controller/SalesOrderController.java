package com.mc.erp.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.erp.common.OperLog;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.ImportResult;
import com.mc.erp.dto.SalesOrderDetailImportRow;
import com.mc.erp.dto.SalesOrderImportRow;
import com.mc.erp.dto.SalesOrderQuery;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.entity.SalesOrderDetail;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.SalesOrderDetailService;
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

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/v1/sales-orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private SalesOrderDetailService salesOrderDetailService;

    @GetMapping("/page")
    public Result<PageResult<SalesOrderVO>> getOrderPage(SalesOrderQuery query) {
        return Result.success(salesOrderService.getPage(query));
    }

    @GetMapping("/{id}")
    public Result<SalesOrder> getById(@PathVariable Long id) {
        return Result.success(salesOrderService.getById(id));
    }

    /**
     * 根据销售订单号查询创建海运订单所需的默认值：
     * - transportType: 销售订单的运输方式（含"散货"则为散货船）
     * - tradeTerm: 贸易条款
     * - settlementTotalQuantity: 结算总数量（散货重量默认值）
     * - totalVolume: 明细体积合计（散货体积默认值）
     */
    @GetMapping("/freight-defaults/{orderNo}")
    public Result<Map<String, Object>> getFreightDefaults(@PathVariable String orderNo) {
        SalesOrder order = salesOrderService.getOne(
                new LambdaQueryWrapper<SalesOrder>().eq(SalesOrder::getOrderNo, orderNo));
        if (order == null) {
            return Result.error(404, "销售订单不存在");
        }

        List<SalesOrderDetail> details = salesOrderDetailService.list(
                new LambdaQueryWrapper<SalesOrderDetail>().eq(SalesOrderDetail::getOrderId, order.getId()));
        BigDecimal totalVolume = details.stream()
                .map(d -> d.getVolume() != null ? d.getVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("transportType", order.getTransportType());
        result.put("tradeTerm", order.getTradeTerm());
        result.put("settlementTotalQuantity", order.getSettlementTotalQuantity());
        result.put("totalVolume", totalVolume);
        return Result.success(result);
    }

    @OperLog(module = "销售订单", type = OperationType.ADD, description = "新增销售订单")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody SalesOrder salesOrder) {
        boolean success = salesOrderService.save(salesOrder);
        if (success && StringUtils.hasText(salesOrder.getOrderNo())) {
            salesOrderService.syncClaimAmounts(salesOrder.getOrderNo());
        }
        return Result.success(success);
    }

    @OperLog(module = "销售订单", type = OperationType.MODIFY, description = "修改销售订单")
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

    @OperLog(module = "销售订单", type = OperationType.DELETE, description = "删除销售订单")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (!SecurityUtil.isAdmin()) {
            return Result.error(403, "仅管理员可删除销售订单");
        }
        return Result.success(salesOrderService.removeById(id));
    }

    @OperLog(module = "销售订单", type = OperationType.STATUS_CHANGE, description = "变更销售订单状态")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        salesOrderService.updateStatus(id, body.get("status"));
        return Result.success(null);
    }

    // ---- Excel 导入 ----

    @OperLog(module = "销售订单", type = OperationType.IMPORT, description = "导入销售订单合同")
    @PostMapping("/import/contract")
    public Result<ImportResult> importContract(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.success(salesOrderService.importContracts(file));
    }

    @OperLog(module = "销售订单", type = OperationType.IMPORT, description = "导入销售订单明细")
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
