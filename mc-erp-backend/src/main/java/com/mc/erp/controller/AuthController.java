package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.Result;
import com.mc.erp.dto.LoginDTO;
import com.mc.erp.enums.LogType;
import com.mc.erp.enums.OperationType;
import com.mc.erp.service.UserService;
import com.mc.erp.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @OperLog(module = "系统认证", type = OperationType.LOGIN, description = "用户登录", logType = LogType.LOGIN)
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }
}
