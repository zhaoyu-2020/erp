package com.mc.erp.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FreightForwarderVO {
    private Long id;
    private String forwarderCode;
    private String name;
    private String freightType;
    /**
     * 优势市场(多个用英文逗号分隔)
     */
    private String marketAdvantage;
    private String contactPerson;
    private String phone;
    private String address;
    private LocalDateTime createTime;
}
