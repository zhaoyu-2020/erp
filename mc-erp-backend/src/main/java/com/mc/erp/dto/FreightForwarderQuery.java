package com.mc.erp.dto;

import lombok.Data;

@Data
public class FreightForwarderQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String forwarderCode;
    private String name;
    private String freightType;
    /**
     * 优势市场(多个用英文逗号分隔)
     */
    private String marketAdvantage;
}
