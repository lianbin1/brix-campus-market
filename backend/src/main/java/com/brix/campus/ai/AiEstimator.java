package com.brix.campus.ai;

/**
 * 估价能力统一入口：Service 只依赖本接口，不关心背后是 LLM 还是本地规则。
 */
public interface AiEstimator {

    /**
     * 智能估价。
     *
     * @param profile 归一化入参
     * @return 估价结果
     */
    EstimateResult estimate(ItemProfile profile);

    /**
     * 一键生成标题与描述。
     *
     * @param profile 归一化入参
     * @return 文案结果
     */
    CopyResult copywrite(ItemProfile profile);

    /**
     * 数据来源标识。
     *
     * @return LLM 或 RULE
     */
    String source();
}
