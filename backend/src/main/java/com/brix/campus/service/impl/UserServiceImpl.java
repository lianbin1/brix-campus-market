package com.brix.campus.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.brix.campus.common.BusinessException;
import com.brix.campus.mapper.ItemMapper;
import com.brix.campus.mapper.OrderMapper;
import com.brix.campus.mapper.UserMapper;
import com.brix.campus.model.Item;
import com.brix.campus.model.Order;
import com.brix.campus.model.User;
import com.brix.campus.model.dto.ChangePasswordDTO;
import com.brix.campus.model.dto.UpdateProfileDTO;
import com.brix.campus.model.dto.UserStatusDTO;
import com.brix.campus.model.enums.ItemStatusEnum;
import com.brix.campus.model.enums.OrderStatusEnum;
import com.brix.campus.model.enums.RoleEnum;
import com.brix.campus.model.enums.UserStatusEnum;
import com.brix.campus.model.query.UserQuery;
import com.brix.campus.model.vo.AdminUserVO;
import com.brix.campus.model.vo.UploadVO;
import com.brix.campus.model.vo.UserStatsVO;
import com.brix.campus.model.vo.UserVO;
import com.brix.campus.service.FileStorageService;
import com.brix.campus.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 用户服务实现。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final ItemMapper itemMapper;
    private final OrderMapper orderMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserServiceImpl(ItemMapper itemMapper,
                           OrderMapper orderMapper,
                           PasswordEncoder passwordEncoder,
                           FileStorageService fileStorageService) {
        this.itemMapper = itemMapper;
        this.orderMapper = orderMapper;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = getEntity(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return toVO(user);
    }

    @Override
    public UserVO getMe(Long userId) {
        User user = getEntity(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        UserVO vo = toVO(user);
        vo.setStats(buildStats(userId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = getEntity(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        user.setNickname(dto.getNickname().trim());
        user.setSchool(trimToNull(dto.getSchool()));
        user.setCampus(trimToNull(dto.getCampus()));
        user.setPhone(trimToNull(dto.getPhone()));
        user.setBio(dto.getBio() == null ? "" : dto.getBio().trim());
        updateById(user);
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = getEntity(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw BusinessException.param("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadVO updateAvatar(Long userId, MultipartFile file) {
        User user = getEntity(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        UploadVO upload = fileStorageService.store(file, "avatar");
        user.setAvatar(upload.getUrl());
        updateById(user);
        return upload;
    }

    @Override
    public IPage<AdminUserVO> adminPage(UserQuery query) {
        Page<AdminUserVO> page = new Page<>(
                query.getCurrentPage() == null ? 1 : query.getCurrentPage(),
                query.getPageSize() == null ? 10 : query.getPageSize());
        IPage<AdminUserVO> result = baseMapper.selectAdminPage(page, query);
        result.getRecords().forEach(vo -> {
            vo.setRoleDesc(RoleEnum.descOf(vo.getRole()));
            vo.setStatusDesc(UserStatusEnum.descOf(vo.getStatus()));
        });
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserVO updateStatus(Long operatorId, Long userId, UserStatusDTO dto) {
        if (!UserStatusEnum.isValid(dto.getStatus())) {
            throw BusinessException.param("状态不合法");
        }
        User user = getEntity(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (UserStatusEnum.DISABLED.getCode().equals(dto.getStatus()) && userId.equals(operatorId)) {
            throw BusinessException.param("不能禁用当前登录账号");
        }
        user.setStatus(dto.getStatus());
        if (UserStatusEnum.DISABLED.getCode().equals(dto.getStatus())) {
            user.setDisabledBy(operatorId);
            user.setDisabledAt(LocalDateTime.now());
        } else {
            user.setDisabledBy(null);
            user.setDisabledAt(null);
        }
        updateById(user);

        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setSchool(user.getSchool());
        vo.setCampus(user.getCampus());
        vo.setRole(user.getRole());
        vo.setRoleDesc(RoleEnum.descOf(user.getRole()));
        vo.setStatus(user.getStatus());
        vo.setStatusDesc(UserStatusEnum.descOf(user.getStatus()));
        vo.setCreditScore(user.getCreditScore());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setDisabledAt(user.getDisabledAt());
        return vo;
    }

    @Override
    public User getEntity(Long userId) {
        if (userId == null) {
            return null;
        }
        return getById(userId);
    }

    @Override
    public UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setSchool(user.getSchool());
        vo.setCampus(user.getCampus());
        vo.setPhone(user.getPhone());
        vo.setBio(user.getBio());
        vo.setRole(user.getRole());
        vo.setRoleDesc(RoleEnum.descOf(user.getRole()));
        vo.setStatus(user.getStatus());
        vo.setStatusDesc(UserStatusEnum.descOf(user.getStatus()));
        vo.setCreditScore(user.getCreditScore());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    /**
     * 组装个人数据统计。
     *
     * @param userId 用户 ID
     * @return 统计对象
     */
    private UserStatsVO buildStats(Long userId) {
        UserStatsVO stats = new UserStatsVO();
        stats.setItemCount(itemMapper.selectCount(Wrappers.<Item>lambdaQuery()
                .eq(Item::getSellerId, userId)));
        stats.setOnSaleCount(itemMapper.selectCount(Wrappers.<Item>lambdaQuery()
                .eq(Item::getSellerId, userId)
                .eq(Item::getStatus, ItemStatusEnum.ON_SALE.getCode())));
        stats.setSoldCount(orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
                .eq(Order::getSellerId, userId)
                .eq(Order::getStatus, OrderStatusEnum.DONE.getCode())));
        return stats;
    }

    /**
     * 空串转 null，便于数据库存 NULL 而不是空字符串。
     *
     * @param value 原值
     * @return trim 后的值或 null
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
