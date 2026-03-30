package com.mc.erp.enums;

/**
 * 操作类型枚举 — 参照需求文档中的操作类型字典
 */
public enum OperationType {
    /** 登录 */
    LOGIN("登录"),
    /** 退出 */
    LOGOUT("退出"),
    /** 新增 */
    ADD("新增"),
    /** 修改 */
    MODIFY("修改"),
    /** 删除 */
    DELETE("删除"),
    /** 查询 */
    QUERY("查询"),
    /** 导出 */
    EXPORT("导出"),
    /** 导入 */
    IMPORT("导入"),
    /** 权限变更 */
    AUTH_CHANGE("权限变更"),
    /** 状态变更 */
    STATUS_CHANGE("状态变更"),
    /** 其他 */
    OTHER("其他");

    private final String label;

    OperationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
