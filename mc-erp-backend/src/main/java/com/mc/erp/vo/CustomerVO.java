package com.mc.erp.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerVO {
    private Long id;
    private String name;
    private String country;
    private String continent;
    private String consignee;
    private String notify;
    private String email;
    private String phone;
    private Long salesUserId;
    private String salesUserName;
    private String level;
    private LocalDateTime createTime;
}
