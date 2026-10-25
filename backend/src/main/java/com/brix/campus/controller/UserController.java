package com.brix.campus.controller;

import com.brix.campus.common.R;
import com.brix.campus.model.dto.ChangePasswordDTO;
import com.brix.campus.model.dto.UpdateProfileDTO;
import com.brix.campus.model.query.CommentQuery;
import com.brix.campus.model.vo.ReviewPageVO;
import com.brix.campus.model.vo.UploadVO;
import com.brix.campus.model.vo.UserVO;
import com.brix.campus.security.SecurityUtils;
import com.brix.campus.service.ReviewService;
import com.brix.campus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户接口（A04-A06 + G02 卖家评价列表）。
 */
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;

    public UserController(UserService userService, ReviewService reviewService) {
        this.userService = userService;
        this.reviewService = reviewService;
    }

    /**
     * 修改个人资料（只改本人，用户 ID 取自 token）。
     *
     * @param dto 入参
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    public R<UserVO> updateProfile(@RequestBody @Valid UpdateProfileDTO dto) {
        return R.ok(userService.updateProfile(SecurityUtils.currentUserId(), dto), "保存成功");
    }

    /**
     * 修改密码（需校验旧密码）。
     *
     * @param dto 入参
     * @return 空
     */
    @PutMapping("/me/password")
    public R<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        userService.changePassword(SecurityUtils.currentUserId(), dto);
        return R.ok(null, "密码修改成功");
    }

    /**
     * 上传头像。
     *
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping("/me/avatar")
    public R<UploadVO> updateAvatar(@RequestParam("file") MultipartFile file) {
        return R.ok(userService.updateAvatar(SecurityUtils.currentUserId(), file), "头像已更新");
    }

    /**
     * 卖家评价列表（含平均分与总数，无需登录）。
     *
     * @param id    卖家 ID
     * @param query 分页条件
     * @return 评价分页 + 汇总
     */
    @GetMapping("/{id}/reviews")
    public R<ReviewPageVO> reviews(@PathVariable("id") Long id,
                                   @Valid CommentQuery query) {
        return R.ok(reviewService.pageBySeller(id, query));
    }
}
