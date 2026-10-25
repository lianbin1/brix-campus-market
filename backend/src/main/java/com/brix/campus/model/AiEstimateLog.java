package com.brix.campus.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 调用日志实体（物理表 {@code t_ai_estimate_log}）。
 *
 * <p>既是留痕表，也是每日 20 次限流的计数来源（{@code user_id + biz_date}）。</p>
 */
@Data
public class AiEstimateLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 调用人 */
    @TableField("user_id")
    private Long userId;

    /** 场景：ESTIMATE / COPY */
    private String scene;

    /** 来源：LLM / RULE */
    private String source;

    /** 模型名，降级为 rule-based */
    private String model;

    /** 入参摘要 */
    @TableField("input_digest")
    private String inputDigest;

    /** 是否成功：1/0 */
    private Integer success;

    /** 耗时毫秒 */
    @TableField("cost_ms")
    private Integer costMs;

    /** 自然日（限流按此列 count） */
    @TableField("biz_date")
    private LocalDate bizDate;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
