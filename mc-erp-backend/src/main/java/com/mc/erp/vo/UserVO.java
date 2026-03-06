package com.mc.erp.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private LocalDateTime createTime;
    /** 当前用户拥有的角色ID列表 */
    private List<Long> roleIds;
}
