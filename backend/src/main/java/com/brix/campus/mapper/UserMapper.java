package com.brix.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brix.campus.model.User;
import com.brix.campus.model.query.UserQuery;
import com.brix.campus.model.vo.AdminUserVO;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper：单表用 {@link BaseMapper}，仅后台联表分页写 XML。
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 后台用户分页查询（含角色 / 状态筛选、关键词模糊匹配用户名或昵称）。
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<AdminUserVO> selectAdminPage(Page<AdminUserVO> page, @Param("q") UserQuery query);
}
