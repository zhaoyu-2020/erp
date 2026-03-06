package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.UserQuery;
import com.mc.erp.entity.User;
import com.mc.erp.dto.LoginDTO;
import com.mc.erp.vo.LoginVO;
import com.mc.erp.vo.UserVO;

import java.util.List;

public interface UserService extends IService<User> {
    PageResult<UserVO> getPage(UserQuery query);

    LoginVO login(LoginDTO loginDTO);

    /** 查询用户拥有的角色ID */
    List<Long> getRoleIds(Long userId);

    /** 设置用户角色（全量覆盖） */
    boolean updateUserRoles(Long userId, List<Long> roleIds);
}
