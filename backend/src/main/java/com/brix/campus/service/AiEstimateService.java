package com.brix.campus.service;

import com.brix.campus.model.dto.AiCopyDTO;
import com.brix.campus.model.dto.AiEstimateDTO;
import com.brix.campus.model.vo.CopyResultVO;
import com.brix.campus.model.vo.EstimateResultVO;

/**
 * AI 估价与文案服务（J01-J04）。
 */
public interface AiEstimateService {

    /**
     * 智能估价：限流 → 估价（LLM 失败自动降级）→ 结果清洗 → 记日志。
     *
     * @param userId 用户 ID
     * @param dto    入参
     * @return 估价结果（含来源与剩余额度）
     */
    EstimateResultVO estimate(Long userId, AiEstimateDTO dto);

    /**
     * 一键生成标题与描述。
     *
     * @param userId 用户 ID
     * @param dto    入参
     * @return 文案结果（含来源与剩余额度）
     */
    CopyResultVO copywriting(Long userId, AiCopyDTO dto);
}
