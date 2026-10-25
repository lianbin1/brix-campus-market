package com.brix.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brix.campus.model.AiEstimateLog;

/**
 * AI 调用日志 Mapper：既是留痕也是每日限流计数来源。
 */
public interface AiEstimateLogMapper extends BaseMapper<AiEstimateLog> {
}
