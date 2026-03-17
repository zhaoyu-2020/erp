package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.ChangePasswordDTO;
import com.mc.erp.dto.UserQuery;
import com.mc.erp.entity.User;
import com.mc.erp.dto.LoginDTO;
import com.mc.erp.vo.LoginVO;
import com.mc.erp.vo.UserVO;
import com.mc.erp.vo.UserWithRoleVO;

import java.util.List;

public interface UserService extends IService<User> {
    PageResult<UserVO> getPage(UserQuery query);

    LoginVO login(LoginDTO loginDTO);

    /** 查询用户拥有的角色ID */
    List<Long> getRoleIds(Long userId);

    /** 设置用户角色（全量覆盖） */
    boolean updateUserRoles(Long userId, List<Long> roleIds);

    /** 查询所有用户（含角色名称），用于前端下拉搜索 */
    List<UserWithRoleVO> listWithRoles();

    /** 修改当前登录用户密码 */
    void changePassword(Long userId, ChangePasswordDTO dto);
}
