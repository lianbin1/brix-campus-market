package com.brix.campus.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * LangChain4j 智能体接口：由 {@code AiConfig} 手工装配（不用 starter）。
 */
public interface ItemEstimationAgent {

    /**
     * 智能估价（模型会自行调用 3 个工具查库）。
     *
     * @param userPrompt 用户提示词
     * @return 结构化估价结果
     */
    @SystemMessage(AiPrompts.ESTIMATE_SYSTEM)
    EstimateResult estimate(@UserMessage String userPrompt);

    /**
     * 一键生成标题与描述。
     *
     * @param userPrompt 用户提示词
     * @return 结构化文案结果
     */
    @SystemMessage(AiPrompts.COPY_SYSTEM)
    CopyResult copywrite(@UserMessage String userPrompt);
}
