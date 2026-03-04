package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.UserQuery;
import com.mc.erp.entity.User;
import com.mc.erp.dto.LoginDTO;
import com.mc.erp.vo.LoginVO;
import com.mc.erp.vo.UserVO;

public interface UserService extends IService<User> {
    PageResult<UserVO> getPage(UserQuery query);

    LoginVO login(LoginDTO loginDTO);
}
