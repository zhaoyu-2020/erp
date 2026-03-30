package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统操作日志实体
 */
@Data
@TableName("sys_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 日志类型: OPERATION-操作日志, LOGIN-登录日志, EXCEPTION-异常日志 */
    private String logType;

    /** 操作模块 */
    private String module;

    /** 操作类型: LOGIN/LOGOUT/ADD/MODIFY/DELETE/QUERY/EXPORT/AUTH_CHANGE/STATUS_CHANGE/IMPORT */
    private String operationType;

    /** 操作描述 */
    private String description;

    /** 请求方法: GET/POST/PUT/DELETE/PATCH */
    private String requestMethod;

    /** 请求URL */
    private String requestUrl;

    /** 请求参数(JSON) */
    private String requestParams;

    /** 响应结果(JSON) */
    private String responseResult;

    /** 操作状态: 1成功 0失败 */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;

    /** 操作用户ID */
    private Long operatorId;

    /** 操作用户名 */
    private String operatorName;

    /** 操作IP */
    private String operatorIp;

    /** 耗时(毫秒) */
    private Long elapsedTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
