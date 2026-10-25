package com.brix.campus.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体公共字段：主键 + 审计时间 + 逻辑删除。
 *
 * <p>注意：表名不带 {@code @TableName}，由 {@code mybatis-plus.global-config.db-config.table-prefix: t_}
 * 自动加前缀（{@code User → t_user}）。若显式写 {@code @TableName("t_user")} 会变成 {@code t_t_user}。</p>
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建时间，插入时自动填充 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入与更新时自动填充 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除：0 未删除 / 1 已删除 */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
