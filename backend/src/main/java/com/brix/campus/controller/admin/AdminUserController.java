package com.brix.campus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.common.R;
import com.brix.campus.model.dto.UserStatusDTO;
import com.brix.campus.model.query.UserQuery;
import com.brix.campus.model.vo.AdminUserVO;
import com.brix.campus.security.SecurityUtils;
import com.brix.campus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台用户管理（K05、K06，仅 OPERATOR）。
 */
@RestController
@RequestMapping("/admin/users")
@Validated
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户列表（支持关键词 / 角色 / 状态筛选）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    public R<IPage<AdminUserVO>> list(@Valid UserQuery query) {
        return R.ok(userService.adminPage(query));
    }

    /**
     * 禁用 / 启用用户（不能禁用自己）。
     *
     * @param id  用户 ID
     * @param dto 入参
     * @return 更新后的用户
     */
    @PatchMapping("/{id}/status")
    public R<AdminUserVO> updateStatus(@PathVariable("id") Long id,
                                       @RequestBody @Valid UserStatusDTO dto) {
        return R.ok(userService.updateStatus(SecurityUtils.currentUserId(), id, dto), "操作成功");
    }
}
