package com.brix.campus.controller.admin;

import com.brix.campus.common.R;
import com.brix.campus.model.dto.LoginDTO;
import com.brix.campus.model.vo.LoginVO;
import com.brix.campus.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台登录（K01、页面 13）：仅 REVIEWER / OPERATOR 可进入，USER 返回 400「无后台权限」。
 */
@RestController
@RequestMapping("/admin")
@Validated
public class AdminAuthController {

    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 后台登录。
     *
     * @param dto 登录入参
     * @return token + 用户信息 + 后台菜单
     */
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return R.ok(authService.adminLogin(dto), "登录成功");
    }
}
