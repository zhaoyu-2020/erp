package com.mc.erp.common;

import com.mc.erp.enums.LogType;
import com.mc.erp.enums.OperationType;

import java.lang.annotation.*;

/**
 * 自定义操作日志注解 — 标注在 Controller 方法上，由 AOP 切面自动采集日志。
 *
 * <pre>
 *   @OperLog(module = "客户管理", type = OperationType.ADD, description = "新增客户")
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 操作模块名称 */
    String module() default "";

    /** 操作类型 */
    OperationType type() default OperationType.OTHER;

    /** 操作描述（可选，留空时根据 type 自动生成） */
    String description() default "";

    /** 日志类型（默认操作日志） */
    LogType logType() default LogType.OPERATION;

    /** 是否保存请求参数（默认 true） */
    boolean saveParams() default true;

    /** 是否保存响应结果（默认 false，查询类通常不保存） */
    boolean saveResult() default false;
}
