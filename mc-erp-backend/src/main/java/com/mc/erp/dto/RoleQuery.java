package com.mc.erp.dto;

import lombok.Data;

@Data
public class RoleQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String roleCode;
    private String roleName;
}
