package com.mc.erp.util;

import com.mc.erp.entity.OperationLog;
import com.mc.erp.enums.LogType;
import com.mc.erp.enums.OperationType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 日志辅助工具 — 供 Service 层在没有 @OperLog 注解的场景下
 * 手动构建 OperationLog 实体，再通过 OperationLogService.asyncSaveLog() 持久化到数据库。
 */
public class LogHelper {

    private LogHelper() {}

    /**
     * 构建一条成功的操作日志
     *
     * @param module        模块名称，如 "销售订单"
     * @param operationType 操作类型枚举
     * @param description   操作描述
     * @return OperationLog 实体（已填充当前用户、IP、时间等公共字段）
     */
    public static OperationLog buildSuccessLog(String module, OperationType operationType, String description) {
        OperationLog log = createBase(module, operationType, LogType.OPERATION);
        log.setDescription(description);
        log.setStatus(1);
        return log;
    }

    /**
     * 构建一条异常/失败日志
     *
     * @param module        模块名称
     * @param operationType 操作类型枚举
     * @param description   操作描述
     * @param errorMsg      错误信息
     * @return OperationLog 实体
     */
    public static OperationLog buildErrorLog(String module, OperationType operationType, String description, String errorMsg) {
        OperationLog log = createBase(module, operationType, LogType.EXCEPTION);
        log.setDescription(description);
        log.setStatus(0);
        log.setErrorMsg(truncate(errorMsg, 2000));
        return log;
    }

    // ---------- 内部方法 ----------

    private static OperationLog createBase(String module, OperationType operationType, LogType logType) {
        OperationLog log = new OperationLog();
        log.setLogType(logType.name());
        log.setModule(module);
        log.setOperationType(operationType.name());
        log.setCreateTime(LocalDateTime.now());

        // 当前用户（Service 层仍处于同一请求线程，SecurityContext 可用）
        log.setOperatorId(SecurityUtil.getCurrentUserId());
        log.setOperatorName(SecurityUtil.getCurrentUsername());

        // 请求信息（如果存在 HTTP 上下文）
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setOperatorIp(IpUtil.getClientIp(request));
        }

        return log;
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }
}
