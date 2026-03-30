package com.mc.erp.enums;

/**
 * 日志类型枚举
 */
public enum LogType {
    /** 用户操作日志 */
    OPERATION("操作日志"),
    /** 登录日志 */
    LOGIN("登录日志"),
    /** 异常日志 */
    EXCEPTION("异常日志");

    private final String label;

    LogType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
