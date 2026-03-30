package com.mc.erp.common;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * 日期工具类
 */
public class DateUtils {

    /** yyyy-MM-dd 或 yyyy/MM/dd，月日可为单位数（如 2026/4/5、2026-4-30） */
    private static final DateTimeFormatter FLEXIBLE_DATE_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy")
                    .appendLiteral('-')
                    .appendValue(ChronoField.MONTH_OF_YEAR)
                    .appendLiteral('-')
                    .appendValue(ChronoField.DAY_OF_MONTH)
                    .toFormatter();

    /** yyyyMMdd 纯数字格式（如 20260330） */
    private static final DateTimeFormatter COMPACT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private DateUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 解析日期字符串，兼容以下格式：
     * <ul>
     *   <li>yyyy-MM-dd / yyyy/MM/dd</li>
     *   <li>yyyy-M-d  / yyyy/M/d（月日单位数）</li>
     *   <li>yyyyMMdd（纯8位数字，如 20260330）</li>
     * </ul>
     *
     * @param val 日期字符串
     * @return LocalDate，输入为空时返回 null
     */
    public static LocalDate parseLocalDate(String val) {
        if (!StringUtils.hasText(val)) return null;
        String trimmed = val.trim();
        // 纯8位数字 → yyyyMMdd
        if (trimmed.matches("\\d{8}")) {
            return LocalDate.parse(trimmed, COMPACT_DATE_FORMATTER);
        }
        // 统一将分隔符 "/" 替换为 "-"，再用弹性解析器
        return LocalDate.parse(trimmed.replace('/', '-'), FLEXIBLE_DATE_FORMATTER);
    }
}
