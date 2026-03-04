package com.mc.erp.vo;

import lombok.Data;

/**
 * 登录成功响应 VO
 */
@Data
public class LoginVO {
    /** JWT Token（前端存 localStorage，后续请求放 Authorization 头） */
    private String token;
    private Long userId;
    private String username;
    private String realName;
}
