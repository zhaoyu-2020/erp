package com.mc.erp.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerVO {
    private Long id;
    private String customerCode;
    private String name;
    private String country;
    private String continent;
    private String contactPerson;
    private String email;
    private String phone;
    private String level;
    private LocalDateTime createTime;
}
