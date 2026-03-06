package com.mc.erp.dto;

import lombok.Data;

@Data
public class UserQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String username;
    private String realName;
    private String phone;
    private String email;
}
