package com.mc.erp.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 海运订单状态枚举
 * 状态流转：草稿 → 已提交 → 已结算
 *          草稿/已提交 → 已作废
 *          已结算 → 已作废（仅管理员）
 */
@Getter
public enum FreightOrderStatus {

    DRAFT(0, "草稿", "info"),
    SUBMITTED(1, "已提交", "primary"),
    SETTLED(2, "已结算", "success"),
    CANCELLED(3, "已作废", "danger");

    private final int code;
    private final String label;
    private final String tagType;

    FreightOrderStatus(int code, String label, String tagType) {
        this.code = code;
        this.label = label;
        this.tagType = tagType;
    }

    public static FreightOrderStatus of(int code) {
        return Arrays.stream(values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知海运订单状态: " + code));
    }

    public static void validateTransition(Integer fromCode, Integer toCode) {
        if (toCode == null || fromCode == null) return;
        FreightOrderStatus from = of(fromCode);
        FreightOrderStatus to = of(toCode);
        boolean valid = switch (to) {
            case SUBMITTED -> from == DRAFT;
            case SETTLED -> from == SUBMITTED;
            case CANCELLED -> from == DRAFT || from == SUBMITTED || from == SETTLED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    String.format("状态不能从「%s」直接变更为「%s」", from.label, to.label));
        }
    }

    public static List<Map<String, Object>> toMetaList() {
        return Arrays.stream(values())
                .map(s -> Map.<String, Object>of(
                        "code", s.code,
                        "label", s.label,
                        "tagType", s.tagType
                ))
                .collect(Collectors.toList());
    }
}
