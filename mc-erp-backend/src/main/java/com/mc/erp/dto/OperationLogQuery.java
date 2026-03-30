package com.mc.erp.dto;

import lombok.Data;

/**
 * 操作日志查询条件
 */
@Data
public class OperationLogQuery {
    /** 日志类型: OPERATION/LOGIN/EXCEPTION */
    private String logType;
    /** 操作模块 */
    private String module;
    /** 操作类型 */
    private String operationType;
    /** 操作状态: 1成功 0失败 */
    private Integer status;
    /** 操作用户名（模糊） */
    private String operatorName;
    /** 操作IP */
    private String operatorIp;
    /** 开始时间 yyyy-MM-dd HH:mm:ss */
    private String startTime;
    /** 结束时间 yyyy-MM-dd HH:mm:ss */
    private String endTime;
    /** 关键词（模糊匹配描述/URL） */
    private String keyword;

    /** 分页 */
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}
