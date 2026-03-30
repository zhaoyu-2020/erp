package com.mc.erp.config;

import com.mc.erp.common.Result;
import com.mc.erp.entity.OperationLog;
import com.mc.erp.service.OperationLogService;
import com.mc.erp.util.IpUtil;
import com.mc.erp.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 全局异常处理器：统一返回 {code, message, data} 格式，并记录异常日志
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private OperationLogService operationLogService;

    /** 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        saveExceptionLog(e);
        return Result.error(403, "无操作权限");
    }

    /** 参数校验失败（@Valid 注解） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        saveExceptionLog(e, msg);
        return Result.error(400, msg);
    }

    /** 约束违反 */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        saveExceptionLog(e);
        return Result.error(400, e.getMessage());
    }

    /** 数据库唯一键冲突 */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleDuplicateKey(SQLIntegrityConstraintViolationException e) {
        saveExceptionLog(e, "数据已存在，请勿重复添加");
        return Result.error(400, "数据已存在，请勿重复添加");
    }

    /** 业务规则校验失败（如金额超限）*/
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        saveExceptionLog(e);
        return Result.error(400, e.getMessage());
    }

    /** 状态机流转校验失败 */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleIllegalState(IllegalStateException e) {
        saveExceptionLog(e);
        return Result.error(400, e.getMessage());
    }

    /** 业务异常（用 RuntimeException 承载） */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusiness(RuntimeException e) {
        // 向上找根因，若是唯一键冲突则给友好提示
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                saveExceptionLog(e, "数据已存在，请勿重复添加");
                return Result.error(400, "数据已存在，请勿重复添加");
            }
            cause = cause.getCause();
        }
        saveExceptionLog(e);
        return Result.error(500, e.getMessage());
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGeneral(Exception e) {
        saveExceptionLog(e);
        return Result.error(500, "服务器内部错误：" + e.getMessage());
    }

    // ==================== 异常日志记录 ====================

    /**
     * 记录异常日志（使用异常本身的message）
     */
    private void saveExceptionLog(Exception e) {
        saveExceptionLog(e, e.getMessage());
    }

    /**
     * 记录异常日志到 sys_operation_log 表
     */
    private void saveExceptionLog(Exception e, String description) {
        try {
            OperationLog log = new OperationLog();
            log.setLogType("EXCEPTION");
            log.setModule(extractModule(e));
            log.setOperationType("OTHER");
            log.setDescription(truncate(description, 500));
            log.setStatus(0);

            // 异常堆栈信息
            String stackTrace = getStackTrace(e);
            log.setErrorMsg(truncate(stackTrace, 2000));

            // 当前用户信息（可能为空，如未登录时的异常）
            try {
                log.setOperatorId(SecurityUtil.getCurrentUserId());
                log.setOperatorName(SecurityUtil.getCurrentUsername());
            } catch (Exception ignored) {
            }

            // 请求信息
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setRequestMethod(request.getMethod());
                log.setRequestUrl(request.getRequestURI());
                log.setOperatorIp(IpUtil.getClientIp(request));
            }

            log.setCreateTime(LocalDateTime.now());

            // 异步保存，避免影响异常响应
            operationLogService.asyncSaveLog(log);
        } catch (Exception ex) {
            // 日志记录失败不应影响正常异常处理流程
            logger.error("记录异常日志失败", ex);
        }
    }

    /**
     * 从异常堆栈中提取模块名（取第一个 com.mc.erp 包下的类名）
     */
    private String extractModule(Exception e) {
        for (StackTraceElement element : e.getStackTrace()) {
            String className = element.getClassName();
            if (className.startsWith("com.mc.erp.service.") || className.startsWith("com.mc.erp.controller.")) {
                // 取简单类名，去掉 Impl/Controller 后缀
                String simpleName = className.substring(className.lastIndexOf('.') + 1);
                simpleName = simpleName.replace("ServiceImpl", "")
                        .replace("Controller", "")
                        .replace("Service", "");
                return simpleName;
            }
        }
        return "系统";
    }

    /**
     * 获取异常堆栈字符串
     */
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }
}
