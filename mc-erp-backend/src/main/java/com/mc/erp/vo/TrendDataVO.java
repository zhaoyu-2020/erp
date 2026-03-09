package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TrendDataVO {
    private String period;      // 时间周期
    private BigDecimal value;   // 数值
    private String label;       // 标签
}
