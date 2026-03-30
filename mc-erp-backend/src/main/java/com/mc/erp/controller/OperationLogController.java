package com.mc.erp.controller;

import com.alibaba.excel.EasyExcel;
import com.mc.erp.common.OperLog;
import com.mc.erp.common.PageResult;
import com.mc.erp.common.Result;
import com.mc.erp.dto.OperationLogQuery;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.OperationLogService;
import com.mc.erp.vo.OperationLogVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 操作日志管理接口
 */
@RestController
@RequestMapping("/api/v1/operation-logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    public Result<PageResult<OperationLogVO>> getPage(OperationLogQuery query) {
        return Result.success(operationLogService.getPage(query));
    }

    /**
     * 查看日志详情
     */
    @GetMapping("/{id}")
    public Result<OperationLogVO> getDetail(@PathVariable Long id) {
        return Result.success(operationLogService.getDetail(id));
    }

    /**
     * 导出操作日志
     */
    @OperLog(module = "日志管理", type = OperationType.EXPORT, description = "导出操作日志")
    @GetMapping("/export")
    public void export(OperationLogQuery query, HttpServletResponse response) throws Exception {
        String filename = URLEncoder.encode("操作日志.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + filename);

        List<OperationLogVO> list = operationLogService.listForExport(query);
        EasyExcel.write(response.getOutputStream(), OperationLogVO.class)
                .sheet("操作日志")
                .doWrite(list);
    }

    /**
     * 手动清理指定天数之前的日志
     */
    @OperLog(module = "日志管理", type = OperationType.DELETE, description = "清理操作日志")
    @DeleteMapping("/clean")
    public Result<Integer> clean(@RequestParam(defaultValue = "90") int days) {
        return Result.success(operationLogService.cleanBefore(days));
    }
}
