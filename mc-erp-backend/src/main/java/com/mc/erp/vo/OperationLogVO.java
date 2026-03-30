package com.mc.erp.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志展示 VO
 */
@Data
public class OperationLogVO {
    private Long id;

    @ExcelProperty("日志类型")
    @ColumnWidth(15)
    private String logType;

    @ExcelProperty("操作模块")
    @ColumnWidth(15)
    private String module;

    @ExcelProperty("操作类型")
    @ColumnWidth(15)
    private String operationType;

    @ExcelProperty("操作描述")
    @ColumnWidth(30)
    private String description;

    @ExcelProperty("请求方法")
    @ColumnWidth(10)
    private String requestMethod;

    @ExcelProperty("请求URL")
    @ColumnWidth(40)
    private String requestUrl;

    private String requestParams;

    private String responseResult;

    @ExcelProperty("操作状态")
    @ColumnWidth(10)
    private String statusLabel;

    private Integer status;

    private String errorMsg;

    @ExcelProperty("操作用户")
    @ColumnWidth(15)
    private String operatorName;

    private Long operatorId;

    @ExcelProperty("操作IP")
    @ColumnWidth(18)
    private String operatorIp;

    @ExcelProperty("耗时(ms)")
    @ColumnWidth(12)
    private Long elapsedTime;

    @ExcelProperty("操作时间")
    @ColumnWidth(22)
    private LocalDateTime createTime;
}
