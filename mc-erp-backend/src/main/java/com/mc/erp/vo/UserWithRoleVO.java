package com.mc.erp.vo;

import lombok.Data;
import java.util.List;

/**
 * 携带角色名称的用户 VO，供前端下拉选择使用
 */
@Data
public class UserWithRoleVO {
    private Long id;
    private String username;
    private String realName;
    /** 角色名称列表 */
    private List<String> roleNames;
    /** 角色编码列表，供前端权限判断（如 admin） */
    private List<String> roleCodes;
}
