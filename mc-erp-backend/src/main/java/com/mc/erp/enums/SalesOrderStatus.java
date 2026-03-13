package com.mc.erp.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售订单状态枚举
 * 状态流转：新建 → 已收定金 → 已采购 → 待发运 → 已发运 → 已收款 → 已完成
 */
@Getter
public enum SalesOrderStatus {

    NEW(1, "新建", "info",
            List.of()),                          // 创建时初始状态，无前置

    DEPOSIT_RECEIVED(2, "已收定金", "warning",
            List.of(1)),                         // 财务系统收款后可流转

    PURCHASED(3, "已采购", "primary",
            List.of(2)),                         // 至少有一条采购合同后可流转

    PENDING_SHIPMENT(4, "待发运", "default",
            List.of(3)),                         // 所有采购合同状态为"待发货"时可流转

    SHIPPED(5, "已发运", "default",
            List.of(4)),                         // 完成报关、货物发走后流转

    PAYMENT_RECEIVED(6, "已收款", "success",
            List.of(5)),                         // 收款完成后流转，校验实际金额

    COMPLETED(7, "已完成", "success",
            List.of(6));                         // 管理员归档

    private final int code;
    private final String label;
    /** Element Plus tag type: info / warning / primary / success / danger / default(空字符串) */
    private final String tagType;
    /** 允许从哪些状态流转过来 */
    private final List<Integer> allowedFromCodes;

    SalesOrderStatus(int code, String label, String tagType, List<Integer> allowedFromCodes) {
        this.code = code;
        this.label = label;
        this.tagType = "default".equals(tagType) ? "" : tagType;
        this.allowedFromCodes = allowedFromCodes;
    }

    public static SalesOrderStatus of(int code) {
        return Arrays.stream(values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知销售订单状态: " + code));
    }

    /**
     * 校验状态流转是否合法
     */
    public static void validateTransition(Integer fromCode, Integer toCode) {
        if (toCode == null) return;
        SalesOrderStatus to = of(toCode);
        // 新建状态（创建时 fromCode 为 null）
        if (fromCode == null) return;
        if (!to.allowedFromCodes.isEmpty() && !to.allowedFromCodes.contains(fromCode)) {
            SalesOrderStatus from = of(fromCode);
            throw new IllegalStateException(
                    String.format("状态不能从「%s」直接变更为「%s」", from.label, to.label));
        }
    }

    /**
     * 返回前端所需的状态字典列表
     */
    public static List<Map<String, Object>> toMetaList() {
        return Arrays.stream(values())
                .map(s -> Map.<String, Object>of(
                        "code", s.code,
                        "label", s.label,
                        "tagType", s.tagType,
                        "allowedFromCodes", s.allowedFromCodes
                ))
                .collect(Collectors.toList());
    }
}
