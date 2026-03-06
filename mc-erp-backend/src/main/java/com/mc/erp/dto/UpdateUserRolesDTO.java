package com.mc.erp.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRolesDTO {
    private List<Long> roleIds;
}

