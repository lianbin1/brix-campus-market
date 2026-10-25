package com.brix.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.model.User;
import com.brix.campus.model.dto.ChangePasswordDTO;
import com.brix.campus.model.dto.UpdateProfileDTO;
import com.brix.campus.model.dto.UserStatusDTO;
import com.brix.campus.model.query.UserQuery;
import com.brix.campus.model.vo.AdminUserVO;
import com.brix.campus.model.vo.UploadVO;
import com.brix.campus.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务：个人资料、密码、头像，以及后台用户管理。
 */
public interface UserService {

    /**
     * 按 ID 取用户信息（不含个人数据统计）。
     *
     * <p>方法名不用 {@code getById}，避免与 MyBatis-Plus {@code IRepository#getById(Serializable)} 重载冲突。</p>
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserVO getUserInfo(Long userId);

    /**
     * 取当前登录用户信息（含发布数 / 在售数 / 卖出数）。
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserVO getMe(Long userId);

    /**
     * 修改个人资料，只允许改本人。
     *
     * @param userId 用户 ID（取自 token）
     * @param dto    入参
     * @return 更新后信息
     */
    UserVO updateProfile(Long userId, UpdateProfileDTO dto);

    /**
     * 修改密码（校验旧密码）。
     *
     * @param userId 用户 ID
     * @param dto    入参
     */
    void changePassword(Long userId, ChangePasswordDTO dto);

    /**
     * 更新头像。
     *
     * @param userId 用户 ID
     * @param file   头像文件
     * @return 上传结果
     */
    UploadVO updateAvatar(Long userId, MultipartFile file);

    /**
     * 后台用户分页。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<AdminUserVO> adminPage(UserQuery query);

    /**
     * 后台禁用 / 启用用户。
     *
     * @param operatorId 操作人 ID
     * @param userId     目标用户 ID
     * @param dto        入参
     * @return 更新后的用户
     */
    AdminUserVO updateStatus(Long operatorId, Long userId, UserStatusDTO dto);

    /**
     * 取用户实体，不存在返回 null。
     *
     * @param userId 用户 ID
     * @return 实体
     */
    User getEntity(Long userId);

    /**
     * 组装 UserVO（供其它服务复用，避免重复映射代码）。
     *
     * @param user 实体
     * @return VO
     */
    UserVO toVO(User user);
}
