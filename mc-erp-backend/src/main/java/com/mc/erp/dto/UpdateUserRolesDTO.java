package com.mc.erp.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRolesDTO {
    @NotEmpty(message = "角色列表不能为空")
    private List<Long> roleIds;
}

