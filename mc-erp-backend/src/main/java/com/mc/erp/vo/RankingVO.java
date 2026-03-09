package com.mc.erp.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RankingVO {
    private Long id;
    private String name;
    private BigDecimal amount;
    private Integer count;
    private Integer rank;
}
