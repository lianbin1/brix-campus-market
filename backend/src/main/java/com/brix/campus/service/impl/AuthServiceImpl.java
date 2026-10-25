package com.brix.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.brix.campus.common.BusinessException;
import com.brix.campus.mapper.UserMapper;
import com.brix.campus.model.User;
import com.brix.campus.model.dto.LoginDTO;
import com.brix.campus.model.dto.RegisterDTO;
import com.brix.campus.model.enums.RoleEnum;
import com.brix.campus.model.enums.UserStatusEnum;
import com.brix.campus.model.vo.LoginVO;
import com.brix.campus.model.vo.MenuVO;
import com.brix.campus.model.vo.UserVO;
import com.brix.campus.security.JwtUtil;
import com.brix.campus.service.AuthService;
import com.brix.campus.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证服务实现。
 */
@Service
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    /** 初始信用分 */
    private static final int INITIAL_CREDIT_SCORE = 100;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        String username = dto.getUsername().trim();
        Long exists = baseMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username));
        if (exists != null && exists > 0) {
            throw BusinessException.param("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname().trim());
        user.setAvatar("");
        user.setSchool(dto.getSchool() == null ? null : dto.getSchool().trim());
        user.setCampus(dto.getCampus() == null ? null : dto.getCampus().trim());
        user.setPhone(dto.getPhone() == null || dto.getPhone().isBlank() ? null : dto.getPhone().trim());
        user.setBio("");
        user.setRole(RoleEnum.USER.getCode());
        user.setStatus(UserStatusEnum.NORMAL.getCode());
        user.setCreditScore(INITIAL_CREDIT_SCORE);
        save(user);

        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = loadForLogin(dto);
        return buildLoginVO(user);
    }

    @Override
    public LoginVO adminLogin(LoginDTO dto) {
        User user = loadForLogin(dto);
        if (!RoleEnum.isAdmin(user.getRole())) {
            throw BusinessException.param("无后台权限");
        }
        return buildLoginVO(user);
    }

    /**
     * 账号密码校验（不区分账号是否存在，统一提示）。
     *
     * @param dto 登录入参
     * @return 校验通过的用户实体
     */
    private User loadForLogin(LoginDTO dto) {
        User user = baseMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, dto.getUsername().trim())
                .last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw BusinessException.param("用户名或密码错误");
        }
        if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            throw BusinessException.param("账号已被禁用");
        }
        return user;
    }

    /**
     * 组装登录返回体：token + 用户信息 + 按角色过滤的菜单。
     *
     * @param user 用户实体
     * @return 登录信息
     */
    private LoginVO buildLoginVO(User user) {
        String token = jwtUtil.generate(user.getId(), user.getRole(), user.getNickname());
        UserVO userVO = userService.toVO(user);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(userVO);
        vo.setMenus(buildMenus(user.getRole()));
        return vo;
    }

    /**
     * 按角色返回后台菜单（前端刷新时按 role 本地重建同一份常量，架构 4.1 注）。
     *
     * @param role 角色码
     * @return 菜单列表；USER 返回空列表
     */
    private List<MenuVO> buildMenus(String role) {
        List<MenuVO> menus = new ArrayList<>();
        if (RoleEnum.OPERATOR.getCode().equals(role)) {
            menus.add(new MenuVO("dashboard", "数据大屏", "/admin/dashboard", "DataLine"));
            menus.add(new MenuVO("users", "用户管理", "/admin/users", "User"));
            menus.add(new MenuVO("orders", "订单管理", "/admin/orders", "List"));
            menus.add(new MenuVO("categories", "分类管理", "/admin/categories", "Menu"));
            // 私信记录：纠纷取证（只读），运营与审核员均可见（K13）
            menus.add(new MenuVO("chats", "私信记录", "/admin/chats", "ChatDotRound"));
            return menus;
        }
        if (RoleEnum.REVIEWER.getCode().equals(role)) {
            menus.add(new MenuVO("items", "商品审核", "/admin/items", "Goods"));
            menus.add(new MenuVO("reports", "举报处理", "/admin/reports", "Warning"));
            // 私信记录：纠纷取证（只读），运营与审核员均可见（K13）
            menus.add(new MenuVO("chats", "私信记录", "/admin/chats", "ChatDotRound"));
            return menus;
        }
        return menus;
    }
}
