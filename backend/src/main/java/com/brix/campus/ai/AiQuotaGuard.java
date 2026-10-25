package com.brix.campus.ai;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.brix.campus.common.BusinessException;
import com.brix.campus.common.ResultCode;
import com.brix.campus.mapper.AiEstimateLogMapper;
import com.brix.campus.model.AiEstimateLog;
import com.brix.campus.model.enums.AiSceneEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * AI 每日额度守卫：每用户每天 20 次，估价与文案<b>共用同一计数池</b>（PRD J04）。
 *
 * <p>计数来源为 {@code t_ai_estimate_log}（{@code user_id + biz_date}），不使用 Redis。</p>
 */
@Component
public class AiQuotaGuard {

    private final AiEstimateLogMapper aiEstimateLogMapper;
    private final int dailyLimit;

    public AiQuotaGuard(AiEstimateLogMapper aiEstimateLogMapper,
                        @Value("${brix.ai.daily-limit:20}") int dailyLimit) {
        this.aiEstimateLogMapper = aiEstimateLogMapper;
        this.dailyLimit = dailyLimit;
    }

    /**
     * 校验并占用一次额度：第 21 次抛 429。
     *
     * @param userId 用户 ID
     * @param scene  场景（仅用于日志，额度不区分场景）
     */
    public void checkAndReserve(Long userId, AiSceneEnum scene) {
        long used = usedToday(userId);
        if (used >= dailyLimit) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "今日 AI 次数已用完");
        }
    }

    /**
     * 今日剩余额度。
     *
     * @param userId 用户 ID
     * @return 剩余次数（不小于 0）
     */
    public int remaining(Long userId) {
        return (int) Math.max(0L, dailyLimit - usedToday(userId));
    }

    /**
     * 今日已用次数。
     *
     * @param userId 用户 ID
     * @return 已用次数
     */
    public long usedToday(Long userId) {
        Long count = aiEstimateLogMapper.selectCount(Wrappers.<AiEstimateLog>lambdaQuery()
                .eq(AiEstimateLog::getUserId, userId)
                .eq(AiEstimateLog::getBizDate, LocalDate.now()));
        return count == null ? 0L : count;
    }

    /**
     * 写入调用日志（降级调用同样计入 1 次）。
     *
     * @param userId      用户 ID
     * @param scene       场景
     * @param source      来源 LLM / RULE
     * @param model       模型名
     * @param inputDigest 入参摘要
     * @param success     是否成功
     * @param costMs      耗时毫秒
     */
    public void record(Long userId, AiSceneEnum scene, String source, String model,
                       String inputDigest, boolean success, long costMs) {
        AiEstimateLog log = new AiEstimateLog();
        log.setUserId(userId);
        log.setScene(scene.getCode());
        log.setSource(source);
        log.setModel(model);
        log.setInputDigest(inputDigest);
        log.setSuccess(success ? 1 : 0);
        log.setCostMs((int) Math.min(Integer.MAX_VALUE, costMs));
        log.setBizDate(LocalDate.now());
        aiEstimateLogMapper.insert(log);
    }
}
