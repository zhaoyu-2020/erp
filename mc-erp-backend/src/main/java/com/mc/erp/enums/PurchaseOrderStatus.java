package com.mc.erp.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购订单状态枚举
 * 状态流转：新建 → 生产中 → 待发货 → 已完成
 */
@Getter
public enum PurchaseOrderStatus {

    NEW(1, "新建", "info",
            List.of()),

    IN_PRODUCTION(2, "生产中", "primary",
            List.of(1)),

    PENDING_DELIVERY(3, "待发货", "warning",
            List.of(2)),

    COMPLETED(4, "已完成", "success",
            List.of(3));

    private final int code;
    private final String label;
    private final String tagType;
    private final List<Integer> allowedFromCodes;

    PurchaseOrderStatus(int code, String label, String tagType, List<Integer> allowedFromCodes) {
        this.code = code;
        this.label = label;
        this.tagType = tagType;
        this.allowedFromCodes = allowedFromCodes;
    }

    public static PurchaseOrderStatus of(int code) {
        return Arrays.stream(values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知采购订单状态: " + code));
    }

    public static void validateTransition(Integer fromCode, Integer toCode) {
        if (toCode == null || fromCode == null) return;
        PurchaseOrderStatus to = of(toCode);
        if (!to.allowedFromCodes.isEmpty() && !to.allowedFromCodes.contains(fromCode)) {
            PurchaseOrderStatus from = of(fromCode);
            throw new IllegalStateException(
                    String.format("状态不能从「%s」直接变更为「%s」", from.label, to.label));
        }
    }

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
