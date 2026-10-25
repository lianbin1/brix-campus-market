package com.brix.campus.controller;

import com.brix.campus.common.R;
import com.brix.campus.model.dto.LoginDTO;
import com.brix.campus.model.dto.RegisterDTO;
import com.brix.campus.model.vo.LoginVO;
import com.brix.campus.model.vo.UserVO;
import com.brix.campus.security.SecurityUtils;
import com.brix.campus.service.AuthService;
import com.brix.campus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口（A01-A03、A07 无后端接口）。
 */
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * 注册（注册即登录）。
     *
     * @param dto 注册入参
     * @return token + 用户信息 + 菜单
     */
    @PostMapping("/register")
    public R<LoginVO> register(@RequestBody @Valid RegisterDTO dto) {
        return R.ok(authService.register(dto), "注册成功");
    }

    /**
     * 登录。
     *
     * @param dto 登录入参
     * @return token + 用户信息 + 菜单
     */
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return R.ok(authService.login(dto), "登录成功");
    }

    /**
     * 获取当前登录用户（含个人数据统计）。
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    public R<UserVO> me() {
        return R.ok(userService.getMe(SecurityUtils.currentUserId()));
    }
}
