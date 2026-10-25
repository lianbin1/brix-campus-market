package com.brix.campus.controller;

import com.brix.campus.common.R;
import com.brix.campus.model.dto.AiCopyDTO;
import com.brix.campus.model.dto.AiEstimateDTO;
import com.brix.campus.model.vo.CopyResultVO;
import com.brix.campus.model.vo.EstimateResultVO;
import com.brix.campus.security.SecurityUtils;
import com.brix.campus.service.AiEstimateService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 接口（J01-J04）：估价与文案共用每天 20 次的计数池。
 */
@RestController
@RequestMapping("/ai")
@Validated
public class AiController {

    private final AiEstimateService aiEstimateService;

    public AiController(AiEstimateService aiEstimateService) {
        this.aiEstimateService = aiEstimateService;
    }

    /**
     * 智能估价（无 Key 时同结构返回规则估算结果）。
     *
     * @param dto 入参
     * @return 估价结果
     */
    @PostMapping("/estimate")
    public R<EstimateResultVO> estimate(@RequestBody @Valid AiEstimateDTO dto) {
        return R.ok(aiEstimateService.estimate(SecurityUtils.currentUserId(), dto));
    }

    /**
     * 一键生成标题与描述。
     *
     * @param dto 入参
     * @return 文案结果
     */
    @PostMapping("/copywriting")
    public R<CopyResultVO> copywriting(@RequestBody @Valid AiCopyDTO dto) {
        return R.ok(aiEstimateService.copywriting(SecurityUtils.currentUserId(), dto));
    }
}
