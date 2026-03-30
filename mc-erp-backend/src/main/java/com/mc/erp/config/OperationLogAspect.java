package com.mc.erp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.erp.common.OperLog;
import com.mc.erp.entity.OperationLog;
import com.mc.erp.service.OperationLogService;
import com.mc.erp.util.IpUtil;
import com.mc.erp.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志 AOP 切面：拦截标注了 @OperLog 的 Controller 方法，自动记录操作日志。
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 切点：所有标注了 @OperLog 的方法
     */
    @Pointcut("@annotation(com.mc.erp.common.OperLog)")
    public void logPointcut() {
    }

    /**
     * 环绕通知：记录操作日志
     */
    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperLog operLog = method.getAnnotation(OperLog.class);

        // 构建日志对象
        OperationLog log = new OperationLog();
        log.setLogType(operLog.logType().name());
        log.setModule(operLog.module());
        log.setOperationType(operLog.type().name());

        // 描述：注解有值取注解，否则取操作类型的label
        String desc = operLog.description().isEmpty()
                ? operLog.type().getLabel()
                : operLog.description();
        log.setDescription(desc);

        // 当前用户信息
        log.setOperatorId(SecurityUtil.getCurrentUserId());
        log.setOperatorName(SecurityUtil.getCurrentUsername());

        // 请求信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setOperatorIp(IpUtil.getClientIp(request));
        }

        // 请求参数
        if (operLog.saveParams()) {
            log.setRequestParams(getRequestParams(joinPoint));
        }

        Object result = null;
        try {
            result = joinPoint.proceed();

            // 成功
            log.setStatus(1);
            if (operLog.saveResult() && result != null) {
                String resultJson = toJson(result);
                log.setResponseResult(truncate(resultJson, 2000));
            }
        } catch (Throwable ex) {
            // 失败
            log.setStatus(0);
            log.setErrorMsg(truncate(ex.getMessage(), 2000));
            throw ex; // 重新抛出让全局异常处理器处理
        } finally {
            log.setElapsedTime(System.currentTimeMillis() - startTime);
            log.setCreateTime(LocalDateTime.now());
            // 异步保存日志
            try {
                operationLogService.asyncSaveLog(log);
            } catch (Exception e) {
                logger.error("保存操作日志失败", e);
            }
        }

        return result;
    }

    /**
     * 获取请求参数（过滤掉 HttpServletRequest/Response/MultipartFile 等不可序列化对象）
     */
    private String getRequestParams(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (paramNames == null || paramNames.length == 0) {
                return null;
            }

            Map<String, Object> params = new LinkedHashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                Object arg = args[i];
                if (arg instanceof HttpServletRequest
                        || arg instanceof HttpServletResponse
                        || arg instanceof MultipartFile) {
                    params.put(paramNames[i], arg == null ? null : arg.getClass().getSimpleName());
                } else {
                    params.put(paramNames[i], arg);
                }
            }
            String json = toJson(params);
            return truncate(json, 2000);
        } catch (Exception e) {
            logger.warn("获取请求参数失败: {}", e.getMessage());
            return null;
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }
}
